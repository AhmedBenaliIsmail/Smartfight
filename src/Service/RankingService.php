<?php
namespace App\Service;

use App\Entity\Fighter;
use App\Entity\FightResult;
use App\Entity\Ranking;
use App\Repository\FighterRepository;
use App\Repository\FightResultRepository;
use App\Repository\RankingRepository;
use App\Entity\Event;
use Doctrine\ORM\EntityManagerInterface;

/**
 * RankingService — exact port of Java RankingService.java
 * Handles ELO calculation, win streaks, strength of schedule, rankings.
 */
class RankingService
{
    public function __construct(
        private EntityManagerInterface $em,
        private FighterRepository $fighterRepo,
        private FightResultRepository $resultRepo,
        private RankingRepository $rankingRepo,
        private AnalyticsEngine $analyticsEngine,
    ) {}

    private function round2(float $v): float { return round($v, 2); }

    /**
     * Calculate ELO rating change after a fight.
     * K-factor: 32 for new fighters (<10 fights), 16 for established.
     */
    public function calculateElo(Fighter $winner, Fighter $loser, string $method): array
    {
        $kWinner = $winner->getTotalFights() < 10 ? 32 : 16;
        $kLoser = $loser->getTotalFights() < 10 ? 32 : 16;

        // Use standard ELO expected value
        $expectedWinner = 1.0 / (1.0 + pow(10, ($loser->getEloRating() - $winner->getEloRating()) / 400));
        $expectedLoser = 1.0 - $expectedWinner;

        // Modify Elo calculation to be heavily influenced by Performance Score (which tracks career strikes/takedowns/control time)
        $perfBonusWinner = $winner->getPerformanceScore() / 10;
        $perfPenaltyLoser = $loser->getPerformanceScore() / 20;

        $winnerNew = $winner->getEloRating() + $kWinner * (1.0 - $expectedWinner) + $perfBonusWinner;
        $loserNew = $loser->getEloRating() + $kLoser * (0.0 - $expectedLoser) - $perfPenaltyLoser;

        // Method bonus
        $bonus = match (strtoupper($method)) {
            'KO', 'TKO', 'KO/TKO' => 10,
            'SUBMISSION' => 8,
            'DECISION' => 5,
            default => 3,
        };
        $winnerNew += $bonus;

        return [
            'winner' => $this->round2($winnerNew),
            'loser' => $this->round2(max($loserNew, 100)),
        ];
    }

    /**
     * Calculate win streak for a fighter.
     */
    public function calculateWinStreak(int $fighterId): int
    {
        $fights = $this->resultRepo->findLastFightsByFighter($fighterId, 20);
        $streak = 0;
        foreach ($fights as $fight) {
            if ($fight->getWinnerId() === $fighterId) {
                $streak++;
            } else {
                break;
            }
        }
        return $streak;
    }

    /**
     * Calculate Strength of Schedule (avg ELO of last 5 opponents).
     */
    public function calculateSOS(int $fighterId): float
    {
        $fights = $this->resultRepo->findLastFightsByFighter($fighterId, 5);
        if (empty($fights)) return 1500.0;

        $totalElo = 0.0;
        $count = 0;
        foreach ($fights as $fight) {
            $oppId = ($fight->getFighter1Id() === $fighterId) ? $fight->getFighter2Id() : $fight->getFighter1Id();
            $opp = $this->fighterRepo->find($oppId);
            if ($opp) {
                $totalElo += $opp->getEloRating();
                $count++;
            }
        }

        return $count > 0 ? $this->round2($totalElo / $count) : 1500.0;
    }

    /**
     * Process a completed fight — the main workflow.
     * Updates W/L, ELO, win streak, performance, SOS, rankings.
     */
    public function processCompletedFight(FightResult $fight): void
    {
        if ($fight->getStatus() !== 'COMPLETED') return;

        $fighter1 = $this->fighterRepo->find($fight->getFighter1Id());
        $fighter2 = $this->fighterRepo->find($fight->getFighter2Id());
        if (!$fighter1 || !$fighter2) return;

        if ($fight->isDraw()) {
            $fighter1->setDraws($fighter1->getDraws() + 1);
            $fighter2->setDraws($fighter2->getDraws() + 1);
        } else {
            $winnerId = $fight->getWinnerId();
            $winner = ($winnerId === $fighter1->getFighterId()) ? $fighter1 : $fighter2;
            $loser = ($winnerId === $fighter1->getFighterId()) ? $fighter2 : $fighter1;

            // Update W/L counts
            $winner->setWins($winner->getWins() + 1);
            $loser->setLosses($loser->getLosses() + 1);

            // Update win type counts
            $method = strtoupper($fight->getMethodOfVictory() ?? '');
            if (in_array($method, ['KO', 'TKO', 'KO/TKO'])) {
                $winner->setKoWins($winner->getKoWins() + 1);
            } elseif ($method === 'SUBMISSION') {
                $winner->setSubmissionWins($winner->getSubmissionWins() + 1);
            } elseif ($method === 'DECISION') {
                $winner->setDecisionWins($winner->getDecisionWins() + 1);
            }

            // ELO
            $elo = $this->calculateElo($winner, $loser, $fight->getMethodOfVictory() ?? 'DECISION');
            $winner->setEloRating($elo['winner']);
            $loser->setEloRating($elo['loser']);
        }

        $this->em->persist($fighter1);
        $this->em->persist($fighter2);
        $this->em->flush();

        // Win streaks
        $fighter1->setWinStreak($this->calculateWinStreak($fighter1->getFighterId()));
        $fighter2->setWinStreak($this->calculateWinStreak($fighter2->getFighterId()));

        // Win streak bonus to ELO
        if (!$fight->isDraw()) {
            $winnerId = $fight->getWinnerId();
            $winner = ($winnerId === $fighter1->getFighterId()) ? $fighter1 : $fighter2;
            $streak = $winner->getWinStreak();
            if ($streak >= 5) {
                $winner->setEloRating($winner->getEloRating() * 1.10);
            } elseif ($streak >= 3) {
                $winner->setEloRating($winner->getEloRating() * 1.05);
            }
        }

        // Performance scores (this recalculates career averages for strikes/takedowns)
        $this->analyticsEngine->calculatePerformanceScore($fighter1->getFighterId());
        $this->analyticsEngine->calculatePerformanceScore($fighter2->getFighterId());

        // SOS
        $fighter1->setStrengthOfSchedule($this->calculateSOS($fighter1->getFighterId()));
        $fighter2->setStrengthOfSchedule($this->calculateSOS($fighter2->getFighterId()));

        // Champions Event Title Defense logic
        $event = $this->em->getRepository(Event::class)->find($fight->getEventId());
        if ($event && $event->isChampionsEvent() && !$fight->isDraw()) {
            $winnerId = $fight->getWinnerId();
            $winner = ($winnerId === $fighter1->getFighterId()) ? $fighter1 : $fighter2;
            $loser = ($winnerId === $fighter1->getFighterId()) ? $fighter2 : $fighter1;
            
            $winner->setChampionsEventWinStreak($winner->getChampionsEventWinStreak() + 1);
            $winner->setTitleDefenses($winner->getTitleDefenses() + 1);
            
            $loser->setChampionsEventWinStreak(0);
            
            // Re-calculate winner's performance score to apply the new 1.5x Multiplier for Title Defenses
            $this->analyticsEngine->calculatePerformanceScore($winner->getFighterId());
        }

        $this->em->persist($fighter1);
        $this->em->persist($fighter2);
        $this->em->flush();

        // Update global rankings
        $this->updateGlobalRankings();
    }

    /**
     * Recompute all rankings from scratch — reset all fighters then replay.
     */
    public function recomputeAllRankings(): void
    {
        // Reset all fighters
        $fighters = $this->fighterRepo->findAll();
        foreach ($fighters as $f) {
            $f->setEloRating(1500.0);
            $f->setPerformanceScore(0.0);
            $f->setWinStreak(0);
            $f->setStrengthOfSchedule(1500.0);
            $f->setWins(0);
            $f->setLosses(0);
            $f->setDraws(0);
            $f->setKoWins(0);
            $f->setSubmissionWins(0);
            $f->setDecisionWins(0);
            $this->em->persist($f);
        }
        $this->em->flush();

        // Replay all completed fights in chronological order
        $completedFights = $this->resultRepo->findAllCompleted();
        foreach ($completedFights as $fight) {
            $this->processCompletedFight($fight);
        }

        $this->updateGlobalRankings();
    }

    /**
     * Get all fighters sorted by ranking criteria (Wins - Losses), grouped roughly.
     */
    public function getRankedFighters(): array
    {
        $fighters = $this->fighterRepo->findAll();
        usort($fighters, function (Fighter $a, Fighter $b) {
            $diffA = $a->getWins() - $a->getLosses();
            $diffB = $b->getWins() - $b->getLosses();
            if ($diffA === $diffB) {
                // Tie-breaker: Performance Score
                return $b->getPerformanceScore() <=> $a->getPerformanceScore();
            }
            return $diffB <=> $diffA;
        });
        return $fighters;
    }

    /**
     * Update the ranking table for the current season, per weight class.
     */
    public function updateGlobalRankings(): void
    {
        $season = date('Y');
        $fighters = $this->getRankedFighters();

        // Clear old rankings for season
        $this->rankingRepo->deleteAllBySeason($season);

        // Group by weight class
        $grouped = [];
        foreach ($fighters as $f) {
            $wc = $f->getWeightClass() ?: 'Unclassified';
            $grouped[$wc][] = $f;
        }

        foreach ($grouped as $wc => $wcFighters) {
            foreach ($wcFighters as $pos => $fighter) {
                $ranking = new Ranking();
                $ranking->setFighterId($fighter->getFighterId());
                $ranking->setRankPosition($pos + 1);
                $ranking->setPoints($this->round2($fighter->getEloRating() + $fighter->getPerformanceScore() * 10));
                $ranking->setSeason($season);
                $ranking->setWeightClass($wc);
                $ranking->setUpdatedAt(new \DateTime());
                $this->em->persist($ranking);
            }
        }
        
        $this->em->flush();
    }
}
