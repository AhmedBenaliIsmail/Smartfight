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

    public function findByFighterAndSeason(int $fighterId, string $season = 'CURRENT'): ?PerformanceScore
    {
        return $this->createQueryBuilder('ps')
            ->where('ps.fighterId = :fighterId')
            ->andWhere('ps.season = :season')
            ->setParameter('fighterId', $fighterId)
            ->setParameter('season', $season)
            ->setMaxResults(1)
            ->getQuery()
            ->getOneOrNullResult();
    }

    public function findLeaderboard(string $season = 'CURRENT', int $limit = 100): array
    {
        return $this->createQueryBuilder('ps')
            ->where('ps.season = :season')
            ->setParameter('season', $season)
            ->orderBy('ps.score', 'DESC')
            ->addOrderBy('ps.updatedAt', 'DESC')
            ->setMaxResults($limit)
            ->getQuery()
            ->getResult();
    }
}
