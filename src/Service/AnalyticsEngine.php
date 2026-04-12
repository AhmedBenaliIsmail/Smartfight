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
 * AnalyticsEngine — exact port of Java AnalyticsEngine.java
 * Calculates performance scores and ranking points.
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
     * Incorporates Champions Event streak multipliers.
     */
    public function calculatePerformanceScore(int $fighterId): float
    {
        $fighter = $this->fighterRepo->find($fighterId);
        if (!$fighter) return 0.0;

        $fights = $this->resultRepo->findCompletedByFighter($fighterId);
        if (empty($fights)) return 0.0;

        // Champions Event Bonus Logic
        // Base multipliers: Winner 1.5x, Loser 1.25x
        // Streak bonus: +0.25 (winner) / +0.15 (loser) per consecutive appearance
        $finalScore = 0.0;
        foreach ($fights as $fight) {
            $stats = $this->statRepo->findByFighterAndFightResult($fighterId, $fight->getResultId());
            if (!$stats) continue;

            $fightScore = $this->calculateIndividualFightScore($stats, $fighterId);

            // DQ Penalty Logic: Heavy deduction for losing by Disqualification
            $isWinner = ($fight->getWinnerId() === $fighterId);
            $method = strtoupper($fight->getMethodOfVictory() ?? '');
            if (!$isWinner && (str_contains($method, 'DISQUALIFICATION') || str_contains($method, 'DQ'))) {
                $fightScore -= 50.0;
            }
            
            // Check if this was a Champions Event
            $event = $this->em->getRepository(\App\Entity\Event::class)->find($fight->getEventId());
            if ($event && $event->isChampionsEvent()) {
                $isWinner = ($fight->getWinnerId() === $fighterId);
                $streak = $this->calculateParticipationStreak($fighterId, $event->getEventDate());
                
                $multiplier = $isWinner ? (1.5 + (0.25 * $streak)) : (1.25 + (0.15 * $streak));
                $fightScore *= $multiplier;
            }
            
            $finalScore += $fightScore;
        }

        // Career Average normalized by fight count
        $baseScore = count($fights) > 0 ? ($finalScore / count($fights)) : 0.0;

        // Final normalization and save
        $score = $this->round2($baseScore);

        // Update fighter entity
        $fighter->setPerformanceScore($score);
        
        // Save history
        $ps = new PerformanceScore();
        $ps->setFighterId($fighterId);
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
    private function calculateIndividualFightScore(FightStatistic $s, int $fid): float
    {
        $strikeAcc = $s->getStrikesThrown() > 0 ? ($s->getStrikesLanded() / $s->getStrikesThrown()) * 100 : 0;
        $tdAcc = $s->getTakedownAttempts() > 0 ? ($s->getTakedowns() / $s->getTakedownAttempts()) * 100 : 0;
        
        // Basic contribution components
        $strikesPart = $s->getStrikesLanded() * 0.5;
        $tdPart = $s->getTakedowns() * 5.0;
        $subPart = $s->getSubmissions() * 15.0;
        $kdPart = $s->getKnockdowns() * 10.0;
        $controlPart = ($s->getControlTimeSeconds() / 60) * 2.0;

        return ($strikesPart + $tdPart + $subPart + $kdPart + $controlPart);
    }

    /**
     * Calculate how many CONSECUTIVE Champions Events the fighter has been in UP TO a certain date.
     */
    private function calculateParticipationStreak(int $fighterId, \DateTimeInterface $upToDate): int
    {
        $eventRepo = $this->em->getRepository(\App\Entity\Event::class);
        $resultRepo = $this->em->getRepository(\App\Entity\FightResult::class);
        
        // Get all Champions Events before (and including) this fight date, ordered by date DESC
        $qb = $eventRepo->createQueryBuilder('e')
            ->where('e.isChampionsEvent = :isCE')
            ->andWhere('e.eventDate <= :date')
            ->setParameter('isCE', true)
            ->setParameter('date', $upToDate)
            ->orderBy('e.eventDate', 'DESC');
        
        $ceEvents = $qb->getQuery()->getResult();
        
        $streak = 0;
        foreach ($ceEvents as $index => $event) {
            // Participation check (we don't count the current one as "streak bonus" for the first appearance,
            // so C-1 logic effectively starts from 0 for the first one).
            if ($index === 0) continue; // Current event
            
            if ($resultRepo->didFighterParticipateInEvent($fighterId, $event->getEventId())) {
                $streak++;
            } else {
                break; // Streak broken
            }
        }
        
        return $streak;
    }

    /**
     * Calculate ranking points for a fighter.
     * Base points per win type with recency decay (0.9^N).
     */
    public function calculateRankingPoints(int $fighterId): float
    {
        $fights = $this->resultRepo->findCompletedByFighter($fighterId);
        $totalPoints = 0.0;

        foreach ($fights as $index => $fight) {
            if ($fight->getWinnerId() === $fighterId) {
                // Method bonus calculation logic (standard ELO style)
                $method = strtoupper($fight->getMethodOfVictory() ?? '');
                $basePoints = match($method) {
                    'KO', 'TKO', 'KO/TKO' => 10,
                    'SUBMISSION' => 8,
                    'DECISION' => 5,
                    default => 3
                };
                $decay = pow(0.9, $index); // Recency decay
                $totalPoints += $basePoints * $decay;
            }
        }

        return $this->round2($totalPoints);
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
