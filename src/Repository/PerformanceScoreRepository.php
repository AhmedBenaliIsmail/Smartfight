<?php
namespace App\Repository;

use App\Entity\PerformanceScore;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

class PerformanceScoreRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, PerformanceScore::class);
    }

    public function findByFighter(int $fighterId): array
    {
        return $this->findBy(['fighter' => $fighterId], ['calculatedAt' => 'DESC']);
    }

    public function getLatestByFighter(int $fighterId): ?PerformanceScore
    {
        return $this->findOneBy(['fighter' => $fighterId], ['calculatedAt' => 'DESC']);
    }
}
