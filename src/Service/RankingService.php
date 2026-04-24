<?php
namespace App\Service;

use App\Entity\Fighter;
use App\Entity\FightResult;
use App\Entity\Ranking;
use App\Repository\FighterRepository;
use App\Repository\FightResultRepository;
use App\Repository\RankingRepository;
use Doctrine\ORM\EntityManagerInterface;

/**
 * RankingService — Remodeled for Boxing
 * Handles ELO calculation, 18-month activity rule, multi-organization rankings.
 */
class RankingService
{
    public function __construct(
        private EntityManagerInterface $em,
        private FighterRepository $fighterRepo,
        private FightResultRepository $resultRepo,
        private RankingRepository $rankingRepo,
        private AnalyticsEngine $analyticsEngine,
        private PredictionService $predictionService,
    ) {}

    private function round2(float $v): float { return round($v, 2); }

    /**
     * Calculate ELO rating change after a fight.
     * Incorporates FightFax's System: Win Type, Match Type, Decision Round Factor.
     */
    public function calculateElo(FightResult $fight): array
    {
        $winner = $fight->getWinner();
        $loser = $fight->getLoser();
        if (!$winner || !$loser) return [];

        $kWinner = $winner->getTotalFights() < 10 ? 48 : 24; // Higher K for faster movement
        $kLoser = $loser->getTotalFights() < 10 ? 48 : 24;

        $expectedWinner = 1.0 / (1.0 + pow(10, ($loser->getEloRating() - $winner->getEloRating()) / 400));
        $expectedLoser = 1.0 - $expectedWinner;

        $perfBonusWinner = $winner->getPerformanceScore() / 10;
        $perfPenaltyLoser = $loser->getPerformanceScore() / 20;

        // Base Change
        $winnerChange = $kWinner * (1.0 - $expectedWinner) + $perfBonusWinner;
        $loserChange = $kLoser * (0.0 - $expectedLoser) - $perfPenaltyLoser;

        // 1. Win Type Multiplier (FightFax)
        $method = strtoupper($fight->getMethodOfVictory() ?? '');
        $methodMult = match ($method) {
            FightResult::METHOD_KO => 1.5,
            FightResult::METHOD_DECISION => ($fight->getDecisionType() === 'UD' ? 1.2 : 1.0),
            default => 1.0,
        };
        $winnerChange *= $methodMult;

        // 2. Match Type (Major Bouts reward more points)
        if ($fight->isBeltFight()) {
            $winnerChange *= 1.3;
            $loserChange *= 0.8; // Lose less for losing in a high-level title bout
        }

        // 3. Decision Round Factor (FightFax)
        // Bonus for earlier rounds to reward dominance
        if ($method === FightResult::METHOD_KO) {
            $endRound = $fight->getRoundNumber() ?: 12;
            $roundBonus = max(0, (12 - $endRound)) * 2;
            $winnerChange += $roundBonus;
        }

        $winnerNew = $winner->getEloRating() + $winnerChange;
        $loserNew = $loser->getEloRating() + $loserChange;

        return [
            'winner' => $this->round2($winnerNew),
            'loser' => $this->round2(max($loserNew, 100)),
        ];
    }

    public function calculateWinStreak(int $fighterId): int
    {
        $fights = $this->resultRepo->findLastFightsByFighter($fighterId, 20);
        $streak = 0;
        foreach ($fights as $fight) {
            $winner = $fight->getWinner();
            if ($winner && $winner->getFighterId() === $fighterId) {
                $streak++;
            } else {
                break;
            }
        }
        return $streak;
    }

    /**
     * Academic PageRank Analysis: Strength of Schedule
     * A win over a highly-ranked boxer is a more valuable "vote".
     */
    public function calculateSOS(int $fighterId): float
    {
        $fights = $this->resultRepo->findCompletedByFighter($fighterId);
        if (empty($fights)) return 1000.0;

        $totalScore = 0.0;
        $count = 0;
        foreach ($fights as $fight) {
            $opp = ($fight->getFighter1()->getFighterId() === $fighterId) ? $fight->getFighter2() : $fight->getFighter1();
            if (!$opp) continue;

            // PageRank Concept: Opponent's ELO as weight
            $oppRating = $opp->getEloRating();
            $weight = ($oppRating / 1500); // Normalized weight
            
            if ($fight->getWinner() && $fight->getWinner()->getFighterId() === $fighterId) {
                $totalScore += ($oppRating * 1.2); // Win over high ELO is huge
            } else {
                $totalScore += ($oppRating * 0.8); // Loss to high ELO is less penalizing
            }
            $count++;
        }

        return $count > 0 ? $this->round2($totalScore / $count) : 1000.0;
    }

    public function processCompletedFight(FightResult $fight): void
    {
        if ($fight->getStatus() !== 'COMPLETED') return;

        $fighter1 = $fight->getFighter1();
        $fighter2 = $fight->getFighter2();
        if (!$fighter1 || !$fighter2) return;

        // Update Last Fight Date
        $fightDate = $fight->getFightDate() ?: new \DateTime();
        if ($fighter1->getLastFightDate() === null || $fightDate > $fighter1->getLastFightDate()) {
            $fighter1->setLastFightDate($fightDate);
        }
        if ($fighter2->getLastFightDate() === null || $fightDate > $fighter2->getLastFightDate()) {
            $fighter2->setLastFightDate($fightDate);
        }

        if ($fight->isDraw()) {
            $fighter1->setDraws($fighter1->getDraws() + 1);
            $fighter2->setDraws($fighter2->getDraws() + 1);
        } else {
            $winner = $fight->getWinner();
            $loser = $fight->getLoser();
            if (!$winner || !$loser) return;

            $winner->setWins($winner->getWins() + 1);
            $loser->setLosses($loser->getLosses() + 1);

            $method = strtoupper($fight->getMethodOfVictory() ?? '');
            if ($method === FightResult::METHOD_KO) {
                $winner->setKoWins($winner->getKoWins() + 1);
            } elseif ($method === FightResult::METHOD_DECISION) {
                $winner->setDecisionWins($winner->getDecisionWins() + 1);
            }

            $elo = $this->calculateElo($fight);
            if (!empty($elo)) {
                $winner->setEloRating($elo['winner']);
                $loser->setEloRating($elo['loser']);
            }
            
            // Belt fight logic
            if ($fight->isBeltFight()) {
                $winner->setTitleDefenses($winner->getTitleDefenses() + 1);
            }
        }

        $this->em->persist($fighter1);
        $this->em->persist($fighter2);
        $this->em->flush();

        $fighter1->setWinStreak($this->calculateWinStreak($fighter1->getFighterId()));
        $fighter2->setWinStreak($this->calculateWinStreak($fighter2->getFighterId()));

        $this->analyticsEngine->calculatePerformanceScore($fighter1->getFighterId());
        $this->analyticsEngine->calculatePerformanceScore($fighter2->getFighterId());

        $fighter1->setStrengthOfSchedule($this->calculateSOS($fighter1->getFighterId()));
        $fighter2->setStrengthOfSchedule($this->calculateSOS($fighter2->getFighterId()));

        $this->em->persist($fighter1);
        $this->em->persist($fighter2);
        $this->em->flush();

        $this->predictionService->processPredictionsForFight($fight);
        $this->updateGlobalRankings();
    }

    public function recomputeAllRankings(): void
    {
        $fighters = $this->fighterRepo->findAll();
        foreach ($fighters as $f) {
            $f->setEloRating(1200.0); // Reset to base boxing ELO
            $f->setPerformanceScore(0.0);
            $f->setWinStreak(0);
            $f->setStrengthOfSchedule(1000.0);
            $f->setWins(0);
            $f->setLosses(0);
            $f->setDraws(0);
            $f->setKoWins(0);
            $f->setDecisionWins(0);
            $f->setTechnicalWins(0);
            $f->setTitleDefenses(0);
            $this->em->persist($f);
        }
        $this->em->flush();

        // Clear rankings before recalculating
        $this->rankingRepo->createQueryBuilder('r')->delete()->getQuery()->execute();

        $completedFights = $this->resultRepo->findAllCompleted();
        foreach ($completedFights as $fight) {
            $this->processCompletedFight($fight);
        }
    }

    /**
     * Hybrid Ranking Points Formula:
     * points = (BoxRecPoints * 0.40) + (SOS_PageRank * 0.30) + (TitlePedigree * 0.20) + (ActivityBonus * 0.10)
     */
    public function getBoxingRankingPoints(Fighter $fighter): float
    {
        // 18-month inactivity rule
        $lastFight = $fighter->getLastFightDate();
        if ($lastFight) {
            $monthsInactive = (new \DateTime())->diff($lastFight)->m + ((new \DateTime())->diff($lastFight)->y * 12);
            if ($monthsInactive > 18) {
                return 0.0; // Inactive
            }
        }

        // 1. BoxRec Style: Computerized ratings based on ELO and performance
        $boxRecPoints = ($fighter->getEloRating() / 20) + ($fighter->getPerformanceScore() * 2);
        
        // 2. SOS PageRank: Quality of opposition
        $sosPoints = ($fighter->getStrengthOfSchedule() / 1500) * 100;
        
        // 3. Title Pedigree
        $pedigreePoints = $fighter->getTitleDefenses() * 15;
        
        // 4. Activity Bonus
        $activityBonus = 0;
        if ($lastFight) {
            $monthsInactive = (new \DateTime())->diff($lastFight)->m + ((new \DateTime())->diff($lastFight)->y * 12);
            $activityBonus = max(0, (18 - $monthsInactive)) * 5;
        }

        $totalPoints = ($boxRecPoints * 0.40) + ($sosPoints * 0.30) + ($pedigreePoints * 0.20) + ($activityBonus * 0.10);

        return $this->round2($totalPoints);
    }

    public function updateGlobalRankings(): void
    {
        $fighters = $this->fighterRepo->findAll();
        
        // Group by division
        $divisions = [];
        foreach ($fighters as $f) {
            $wd = $f->getWeightDivision();
            if (!$wd) continue;
            $divisions[$wd->getId()][] = $f;
        }

        // Wipe old rankings
        $this->rankingRepo->createQueryBuilder('r')->delete()->getQuery()->execute();

        $orgs = ['WBC', 'WBA', 'IBF', 'WBO', 'MEDIA'];

        foreach ($divisions as $divId => $divFighters) {
            // Sort fighters by our boxing points
            usort($divFighters, function(Fighter $a, Fighter $b) {
                return $this->getBoxingRankingPoints($b) <=> $this->getBoxingRankingPoints($a);
            });

            // For now, we simulate org rankings by putting everyone in every org list.
            // In a more complex setup, fighters would hold specific belts.
            foreach ($orgs as $org) {
                $pos = 0;
                foreach ($divFighters as $f) {
                    $points = $this->getBoxingRankingPoints($f);
                    if ($points <= 0) continue; // Unranked due to inactivity or zero points

                    $rank = new Ranking();
                    $rank->setFighter($f);
                    $rank->setWeightDivision($f->getWeightDivision());
                    $rank->setOrganization($org);
                    $rank->setPoints($points);
                    $rank->setRankPosition($pos);
                    $rank->setIsChampion($pos === 0);
                    $rank->setLastFightDate($f->getLastFightDate());
                    $rank->setUpdatedAt(new \DateTime());
                    
                    $this->em->persist($rank);
                    $pos++;
                    
                    if ($pos > 15) break; // Top 15 + Champion
                }
            }
        }
        $this->em->flush();
    }
    public function getRankedFighters(): array
    {
        $fighters = $this->fighterRepo->findAll();
        // We only return fighters who have points > 0
        $ranked = array_filter($fighters, fn($f) => $this->getBoxingRankingPoints($f) > 0);
        
        // Sort by points descending
        usort($ranked, function(Fighter $a, Fighter $b) {
            return $this->getBoxingRankingPoints($b) <=> $this->getBoxingRankingPoints($a);
        });
        
        return $ranked;
    }
}
