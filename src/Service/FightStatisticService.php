<?php
namespace App\Service;

use App\Entity\FightStatistic;
use App\Repository\FightStatisticRepository;
use Doctrine\ORM\EntityManagerInterface;

/**
 * FightStatisticService — Remodeled for Boxing
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
        
        $fid = $stat->getFighter() ? $stat->getFighter()->getFighterId() : null;
        if ($fid) $this->analyticsEngine->calculatePerformanceScore($fid);
        return true;
    }

    public function updateFightStatistic(FightStatistic $stat): bool
    {
        $this->validate($stat);
        $this->em->persist($stat);
        $this->em->flush();
        
        $fid = $stat->getFighter() ? $stat->getFighter()->getFighterId() : null;
        if ($fid) $this->analyticsEngine->calculatePerformanceScore($fid);
        return true;
    }

    public function deleteFightStatistic(int $id): bool
    {
        $stat = $this->statRepo->find($id);
        if (!$stat) return false;
        
        $fid = $stat->getFighter() ? $stat->getFighter()->getFighterId() : null;
        $this->em->remove($stat);
        $this->em->flush();
        if ($fid) $this->analyticsEngine->calculatePerformanceScore($fid);
        return true;
    }

    public function recalculateForFighter(int $fighterId): void
    {
        $this->analyticsEngine->calculatePerformanceScore($fighterId);
    }

    private function validate(FightStatistic $stat): void
    {
        if ($stat->getPunchesLanded() < 0 || $stat->getPunchesThrown() < 0) {
            throw new \RuntimeException('Punch values cannot be negative.');
        }
        if ($stat->getPunchesLanded() > $stat->getPunchesThrown()) {
            throw new \RuntimeException('Punches landed cannot exceed thrown.');
        }
        if ($stat->getPowerPunchesLanded() > $stat->getPowerPunchesThrown()) {
            throw new \RuntimeException('Power punches landed cannot exceed thrown.');
        }
        if ($stat->getJabAccuracy() > 100) {
            throw new \RuntimeException('Jab accuracy error.');
        }
    }
}

