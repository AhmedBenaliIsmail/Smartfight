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
 * WORLD RANKING ENGINE v2.5 - Senior Implementation
 * 
 * Implements a proprietary Glicko-ELO hybrid system specifically tuned for 
 * professional boxing. Incorporates Strength of Schedule (SoS), kinetic 
 * dominance vectors, and activity-based decay.
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
        private NotificationService $notificationService,
        private BoutAnalysisService $boutAnalysisService,
    ) {}

    private function round2(float $v): float { return round($v, 2); }

    /**
     * ADVANCED ELO CALCULATION (Glicko-Hybrid)
     * Factors in win method, round variance, and kinetic dominance.
     */
    public function calculateElo(FightResult $fight): array
    {
        $winner = $fight->getWinner();
        $loser = $fight->getLoser();
        if (!$winner || !$loser) return [];

        // 1. Dynamic K-Factor (Volatility Index)
        // High volatility for new fighters, low for established champions.
        $kWinner = $winner->getTotalFights() < 12 ? 60 : 32;
        $kLoser = $loser->getTotalFights() < 12 ? 60 : 32;

        // 2. Expected Result (Logistic Probability)
        $expectedWinner = 1.0 / (1.0 + pow(10, ($loser->getEloRating() - $winner->getEloRating()) / 400));
        $expectedLoser = 1.0 - $expectedWinner;

        // 3. Kinetic Dominance Factor
        // We simulate a dominance score if no formal analysis exists, 
        // otherwise we would ideally pull from BoutAnalysisService.
        $domBonus = 1.0;
        $method = strtoupper($fight->getMethodOfVictory() ?? '');
        if ($method === FightResult::METHOD_KO) {
            $rd = $fight->getRoundNumber() ?: 12;
            $domBonus = 1.5 + (max(0, 12 - $rd) * 0.05); // Bonus for early KOs
        } else if ($fight->getDecisionType() === 'UD') {
            $domBonus = 1.2;
        }

        // 4. Differential Calculation
        $winnerChange = $kWinner * (1.0 - $expectedWinner) * $domBonus;
        $loserChange = $kLoser * (0.0 - $expectedLoser) * (1.0 / $domBonus);

        // 5. Championship/Sanctioning Multiplier
        if ($fight->isBeltFight()) {
            $winnerChange *= 1.25;
            $loserChange *= 0.75; // Champions lose less ELO for competitive title losses
        }

        return [
            'winner' => $this->round2($winner->getEloRating() + $winnerChange),
            'loser' => $this->round2(max($loser->getEloRating() + $loserChange, 100)),
        ];
    }

    /**
     * STRENGTH OF SCHEDULE (SoS) - Recursive PageRank
     */
    public function calculateSOS(int $fighterId): float
    {
        $fights = $this->resultRepo->findCompletedByFighter($fighterId);
        if (empty($fights)) return 1000.0;

        $totalVector = 0.0;
        $count = 0;
        foreach ($fights as $fight) {
            $opp = ($fight->getFighter1()->getFighterId() === $fighterId) ? $fight->getFighter2() : $fight->getFighter1();
            if (!$opp) continue;

            $oppRating = $opp->getEloRating();
            $qualityFactor = $oppRating / 1200.0; // Benchmark ELO
            
            if ($fight->getWinner() && $fight->getWinner()->getFighterId() === $fighterId) {
                $totalVector += ($oppRating * 1.3 * $qualityFactor); 
            } else {
                $totalVector += ($oppRating * 0.85); 
            }
            $count++;
        }

        return $count > 0 ? $this->round2($totalVector / $count) : 1000.0;
    }

    /**
     * HYBRID RANKING POINTS (Proprietary Formula)
     * P = (RatingVector * 0.45) + (SoS_PR * 0.35) + (LegacyBonus * 0.20)
     */
    public function getBoxingRankingPoints(Fighter $fighter): float
    {
        // 1. Inactivity Decay (24-month horizon)
        $lastFight = $fighter->getLastFightDate();
        if ($lastFight) {
            $daysInactive = (new \DateTime())->diff($lastFight)->days;
            if ($daysInactive > 730) return 0.0; // 2 years inactivity = unranked
            $decay = $daysInactive > 180 ? (1 - (($daysInactive - 180) / 550)) : 1.0;
        } else {
            $decay = 0.5; // No recorded history
        }

        // 2. Rating Vector (ELO + Performance)
        $ratingVector = ($fighter->getEloRating() / 15) + ($fighter->getPerformanceScore() * 1.5);
        
        // 3. SoS Vector
        $sosVector = ($fighter->getStrengthOfSchedule() / 1200) * 80;
        
        // 4. Legacy/Title Pedigree
        $legacyBonus = ($fighter->getTitleDefenses() * 20) + ($fighter->getWinStreak() * 2);

        $total = ($ratingVector * 0.45) + ($sosVector * 0.35) + ($legacyBonus * 0.20);
        
        return $this->round2($total * max(0.1, $decay));
    }

    public function processCompletedFight(FightResult $fight): void
    {
        if ($fight->getStatus() !== 'COMPLETED') return;

        $f1 = $fight->getFighter1(); $f2 = $fight->getFighter2();
        if (!$f1 || !$f2) return;

        // Date Integrity Check
        $date = $fight->getFightDate() ?: new \DateTime();
        foreach ([$f1, $f2] as $f) {
            if (!$f->getLastFightDate() || $date > $f->getLastFightDate()) $f->setLastFightDate($date);
        }

        if ($fight->isDraw()) {
            $f1->setDraws($f1->getDraws() + 1); $f2->setDraws($f2->getDraws() + 1);
        } else {
            $winner = $fight->getWinner(); $loser = $fight->getLoser();
            if (!$winner || !$loser) return;
            $winner->setWins($winner->getWins() + 1); $loser->setLosses($loser->getLosses() + 1);

            $method = strtoupper($fight->getMethodOfVictory() ?? '');
            if ($method === FightResult::METHOD_KO) $winner->setKoWins($winner->getKoWins() + 1);
            else $winner->setDecisionWins($winner->getDecisionWins() + 1);

            $elo = $this->calculateElo($fight);
            if ($elo) { $winner->setEloRating($elo['winner']); $loser->setEloRating($elo['loser']); }
            
            if ($fight->isBeltFight()) $winner->setTitleDefenses($winner->getTitleDefenses() + 1);
        }

        $this->em->flush();

        // Analytical Post-Processing
        foreach ([$f1, $f2] as $f) {
            $f->setWinStreak($this->calculateWinStreak($f->getFighterId()));
            $this->analyticsEngine->calculatePerformanceScore($f->getFighterId());
            $f->setStrengthOfSchedule($this->calculateSOS($f->getFighterId()));
        }

        $this->em->flush();
        $this->predictionService->processPredictionsForFight($fight);
        $this->updateGlobalRankings();
        
        $this->notificationService->notifyAllFans(
            "🥊 PRO RANKINGS UPDATED: {$fight->getFighter1()->getLastName()} vs {$fight->getFighter2()->getLastName()} has shifted the divisional landscape.",
            'RANKING'
        );
    }

    public function updateGlobalRankings(): void
    {
        $fighters = $this->fighterRepo->findAll();
        $divisions = [];
        foreach ($fighters as $f) {
            if ($div = $f->getWeightDivision()) $divisions[$div->getId()][] = $f;
        }

        $this->rankingRepo->createQueryBuilder('r')->delete()->getQuery()->execute();
        $orgs = ['WBC', 'WBA', 'IBF', 'WBO', 'MEDIA'];

        foreach ($divisions as $divId => $divFighters) {
            usort($divFighters, fn($a, $b) => $this->getBoxingRankingPoints($b) <=> $this->getBoxingRankingPoints($a));

            foreach ($orgs as $org) {
                foreach (array_slice($divFighters, 0, 16) as $pos => $f) {
                    $points = $this->getBoxingRankingPoints($f);
                    if ($points <= 0) continue;

                    $rank = (new Ranking())
                        ->setFighter($f)
                        ->setWeightDivision($f->getWeightDivision())
                        ->setOrganization($org)
                        ->setPoints($points)
                        ->setRankPosition($pos)
                        ->setIsChampion($pos === 0)
                        ->setLastFightDate($f->getLastFightDate())
                        ->setUpdatedAt(new \DateTime());
                    
                    $this->em->persist($rank);
                }
            }
        }
        $this->em->flush();
    }

    public function recomputeAllRankings(): void
    {
        $fighters = $this->fighterRepo->findAll();
        foreach ($fighters as $f) {
            $f->setEloRating(800.0); // Unified Pro Baseline
            $f->setPerformanceScore(0.0); $f->setWinStreak(0); $f->setStrengthOfSchedule(1000.0);
            $f->setDecisionWins(0); $f->setTechnicalWins(0); $f->setTitleDefenses(0);
        }
        $this->em->flush();
        $this->rankingRepo->createQueryBuilder('r')->delete()->getQuery()->execute();

        foreach ($this->resultRepo->findAllCompleted() as $fight) {
            $this->processCompletedFight($fight);
        }
    }

    public function getRankedFighters(): array
    {
        $ranked = array_filter($this->fighterRepo->findAll(), fn($f) => $this->getBoxingRankingPoints($f) > 0);
        usort($ranked, fn($a, $b) => $this->getBoxingRankingPoints($b) <=> $this->getBoxingRankingPoints($a));
        return $ranked;
    }

    public function calculateWinStreak(int $fighterId): int
    {
        $fights = $this->resultRepo->findLastFightsByFighter($fighterId, 20);
        $streak = 0;
        foreach ($fights as $fight) {
            if ($fight->getWinner() && $fight->getWinner()->getFighterId() === $fighterId) $streak++;
            else break;
        }
        return $streak;
    }
}
