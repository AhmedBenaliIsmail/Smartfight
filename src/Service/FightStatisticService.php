<?php
namespace App\Service;

use App\Entity\FightStatistic;
use App\Repository\FightStatisticRepository;
use Doctrine\ORM\EntityManagerInterface;

/**
 * FightStatisticService — port of Java FightStatisticService.java
 */
class FightStatisticService
{
    public function __construct(
        private EntityManagerInterface $em,
        private FightStatisticRepository $statRepo,
        private AnalyticsEngine $analyticsEngine,
    ) {}

    public function getAllStatistics(): array
    {
        return $this->statRepo->findAll();
    }

    public function addFightStatistic(FightStatistic $stat): bool
    {
        $this->validate($stat);
        $this->em->persist($stat);
        $this->em->flush();
        // Auto-recalculate performance after adding stats
        $this->analyticsEngine->calculatePerformanceScore($stat->getFighterId());
        return true;
    }

    public function updateFightStatistic(FightStatistic $stat): bool
    {
        $this->validate($stat);
        $this->em->persist($stat);
        $this->em->flush();
        $this->analyticsEngine->calculatePerformanceScore($stat->getFighterId());
        return true;
    }

    public function deleteFightStatistic(int $id): bool
    {
        $stat = $this->statRepo->find($id);
        if (!$stat) return false;
        $fighterId = $stat->getFighterId();
        $this->em->remove($stat);
        $this->em->flush();
        $this->analyticsEngine->calculatePerformanceScore($fighterId);
        return true;
    }

    public function recalculateForFighter(int $fighterId): void
    {
        $this->analyticsEngine->calculatePerformanceScore($fighterId);
    }

    private function validate(FightStatistic $stat): void
    {
        if ($stat->getStrikesLanded() < 0 || $stat->getStrikesThrown() < 0) {
            throw new \RuntimeException('Strike values cannot be negative.');
        }
        if ($stat->getStrikesLanded() > $stat->getStrikesThrown()) {
            throw new \RuntimeException('Strikes landed cannot exceed strikes thrown.');
        }
        if ($stat->getTakedowns() < 0 || $stat->getTakedownAttempts() < 0) {
            throw new \RuntimeException('Takedown values cannot be negative.');
        }
        if ($stat->getTakedowns() > $stat->getTakedownAttempts()) {
            throw new \RuntimeException('Takedowns landed cannot exceed attempts.');
        }
    }
}
