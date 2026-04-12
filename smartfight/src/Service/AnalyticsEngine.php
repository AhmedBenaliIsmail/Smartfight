<?php

namespace App\Service;

use App\Entity\Fighter;
use App\Entity\PerformanceScore;
use App\Repository\FighterRepository;
use App\Repository\PerformanceScoreRepository;
use Doctrine\ORM\EntityManagerInterface;

class AnalyticsEngine
{
    public function __construct(
        private readonly EntityManagerInterface $entityManager,
        private readonly FighterRepository $fighterRepository,
        private readonly PerformanceScoreRepository $performanceScoreRepository,
    ) {
    }

    public function recomputeFighter(Fighter $fighter, string $season = 'CURRENT'): PerformanceScore
    {
        $totalFights = max(1, $fighter->getTotalFights());
        $aggression = min(100.0, ($fighter->getKoWins() + $fighter->getSubmissionWins()) / $totalFights * 100);
        $defense = min(100.0, max(0.0, 100 - ($fighter->getLosses() / $totalFights * 100)));
        $technique = min(100.0, ($fighter->getDecisionWins() / $totalFights * 100));
        $experience = min(100.0, $totalFights * 5.0);

        $score = (
            $fighter->getEloRating() * 0.35 +
            $aggression * 3.0 +
            $defense * 2.5 +
            $technique * 2.0 +
            $experience * 1.5 +
            $fighter->getWinStreak() * 10
        ) / 10;

        if ($fighter->getChampionsEventWinStreak() > 0) {
            $score *= 1.05;
        }

        $performance = $this->performanceScoreRepository->findByFighterAndSeason((int) $fighter->getId(), $season)
            ?? new PerformanceScore();

        $performance
            ->setFighterId((int) $fighter->getId())
            ->setSeason($season)
            ->setAggression(round($aggression, 2))
            ->setDefense(round($defense, 2))
            ->setTechnique(round($technique, 2))
            ->setExperience(round($experience, 2))
            ->setScore(round($score, 2))
            ->setComputedAt(new \DateTime());

        $fighter->setPerformanceScore(round($score, 2));

        $this->entityManager->persist($performance);
        $this->entityManager->persist($fighter);
        $this->entityManager->flush();

        return $performance;
    }

    public function recomputeAll(string $season = 'CURRENT'): void
    {
        $fighters = $this->fighterRepository->findAll();

        foreach ($fighters as $fighter) {
            $this->recomputeFighter($fighter, $season);
        }
    }
}
