<?php

namespace App\Service;

use App\Entity\Fighter;
use App\Entity\FightResult;
use Doctrine\ORM\EntityManagerInterface;

class RankingService
{
    private $em;

    public function __construct(EntityManagerInterface $em)
    {
        $this->em = $em;
    }

    /**
     * Advanced Elo-based ranking update logic
     */
    public function processCompletedFight(FightResult $result): void
    {
        $winner = $result->getWinner();
        if (!$winner) return; // Draw or NC doesn't change ELO in this simple model

        $f1 = $result->getFighter1();
        $f2 = $result->getFighter2();
        
        $isF1Winner = ($winner->getFighterId() === $f1->getFighterId());
        $loser = $isF1Winner ? $f2 : $f1;

        // Base K-factor (Sensitivity of the ranking)
        $kFactor = 32;

        // Method Bonus (KO > Decision)
        $methodBonus = 1.0;
        if (str_contains(strtoupper($result->getMethodOfVictory()), 'KO')) {
            $methodBonus = 1.2;
        }

        // Expected Score calculation
        $expectedWinner = 1 / (1 + pow(10, ($loser->getEloRating() - $winner->getEloRating()) / 400));
        $expectedLoser = 1 - $expectedWinner;

        // New Ratings
        $pointsGained = ($kFactor * (1 - $expectedWinner)) * $methodBonus;
        $pointsLost = $kFactor * (0 - $expectedLoser);

        $winner->setEloRating($winner->getEloRating() + $pointsGained);
        $loser->setEloRating($loser->getEloRating() + $pointsLost);

        // Update win streaks
        $winner->setWinStreak($winner->getWinStreak() + 1);
        $loser->setWinStreak(0);

        // Update performance score (Moving average)
        $currentPerformance = ($pointsGained / $kFactor) * 100;
        $winner->setPerformanceScore(($winner->getPerformanceScore() * 0.7) + ($currentPerformance * 0.3));

        $this->em->persist($winner);
        $this->em->persist($loser);
        $this->em->flush();
    }

    public function getWeightDivisionRankings(int $divisionId): array
    {
        return $this->em->getRepository(Fighter::class)->findBy(
            ['weightDivision' => $divisionId],
            ['eloRating' => 'DESC', 'fighterId' => 'ASC']
        );
    }

    public function getRankedFighters(): array
    {
        return $this->em->getRepository(Fighter::class)->findBy(
            [],
            ['eloRating' => 'DESC', 'fighterId' => 'ASC']
        );
    }
}
