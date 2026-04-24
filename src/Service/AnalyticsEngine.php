<?php
namespace App\Service;

use App\Entity\Fighter;
use App\Entity\FightResult;
use App\Entity\FightStatistic;
use App\Entity\PerformanceScore;
use App\Repository\FighterRepository;
use App\Repository\FightResultRepository;
use App\Repository\FightStatisticRepository;
use App\Repository\PerformanceScoreRepository;
use Doctrine\ORM\EntityManagerInterface;

/**
 * AnalyticsEngine — Remodeled for Boxing
 * Calculates performance scores based on punch accuracy and power stats.
 */
class AnalyticsEngine
{
    public function __construct(
        private EntityManagerInterface $em,
        private FighterRepository $fighterRepo,
        private FightResultRepository $resultRepo,
        private FightStatisticRepository $statRepo,
        private PerformanceScoreRepository $perfRepo,
    ) {}

    private function round2(float $v): float { return round($v, 2); }
    private function clamp(float $v, float $min, float $max): float { return max($min, min($max, $v)); }

    /**
     * Calculate performance score for a fighter.
     * Uses CompuBox punch metrics.
     */
    public function calculatePerformanceScore(int $fighterId): float
    {
        $fighter = $this->fighterRepo->find($fighterId);
        if (!$fighter) return 0.0;

        $fights = $this->resultRepo->findCompletedByFighter($fighterId);
        if (empty($fights)) return 0.0;

        $finalScore = 0.0;
        foreach ($fights as $fight) {
            // Find total stats row (round = NULL usually indicates the total)
            // Or just sum the rounds. The implementation plan assumes round=NULL is total.
            // But just in case, findBy() might return multiple if they have per-round stats.
            $allStats = $this->statRepo->findByFighterAndFightResult($fighterId, $fight->getResultId());
            if (empty($allStats)) continue;

            // Take the total row (round is null) or just the first one if we haven't implemented totals properly yet
            $totalStats = null;
            if (is_array($allStats)) {
                foreach ($allStats as $s) {
                    if ($s->getRound() === null) {
                        $totalStats = $s;
                        break;
                    }
                }
                if (!$totalStats) $totalStats = $allStats[0]; // Fallback
            } else {
                $totalStats = $allStats;
            }

            $fightScore = $this->calculateIndividualFightScore($totalStats);

            // DQ Penalty Logic: Heavy deduction for losing by Disqualification
            $isWinner = ($fight->getWinner() && $fight->getWinner()->getFighterId() === $fighterId);
            $method = strtoupper($fight->getMethodOfVictory() ?? '');
            if (!$isWinner && $method === FightResult::METHOD_DQ) {
                $fightScore -= 50.0;
            }
            
            // Belt fight multipliers (Winner 1.5x, Loser 1.25x)
            if ($fight->isBeltFight()) {
                $multiplier = $isWinner ? 1.5 : 1.25;
                $fightScore *= $multiplier;
            }
            
            $finalScore += $fightScore;
        }

        // Career Average normalized by fight count
        $baseScore = count($fights) > 0 ? ($finalScore / count($fights)) : 0.0;
        $score = $this->round2($baseScore);

        $fighter->setPerformanceScore($score);
        
        $ps = new PerformanceScore();
        $ps->setFighter($fighter);
        $ps->setScore($score);
        $ps->setCalculatedAt(new \DateTime());
        $this->em->persist($ps);
        $this->em->persist($fighter);
        $this->em->flush();

        return $score;
    }

    /**
     * Helper to calculate a raw score for a single fight's stats.
     */
    private function calculateIndividualFightScore(FightStatistic $s): float
    {
        $punchAcc = $s->getPunchAccuracy(); // already handles /0
        $powerAcc = $s->getPowerAccuracy(); // already handles /0
        
        $kdBonus = $this->clamp($s->getKnockdowns() * 15.0, 0, 45.0);

        return ($punchAcc * 0.5) + ($powerAcc * 0.3) + ($kdBonus * 0.2);
    }

    /**
     * Calculate ranking points for a fighter.
     * Legacy method, points are now calculated in RankingService directly.
     */
    public function calculateRankingPoints(int $fighterId): float
    {
        return 0.0;
    }

    /**
     * Recalculate all fighters' performance scores.
     */
    public function recalculateAll(): void
    {
        $fighters = $this->fighterRepo->findAll();
        foreach ($fighters as $fighter) {
            $this->calculatePerformanceScore($fighter->getFighterId());
        }
    }
}
