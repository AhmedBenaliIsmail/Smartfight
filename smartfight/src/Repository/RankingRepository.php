<?php

namespace App\Repository;

use App\Entity\Ranking;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

class RankingRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Ranking::class);
    }

    public function findByWeightClassAndSeason(string $weightClass, string $season = 'CURRENT'): array
    {
        return $this->createQueryBuilder('r')
            ->where('r.weightClass = :weightClass')
            ->andWhere('r.season = :season')
            ->setParameter('weightClass', $weightClass)
            ->setParameter('season', $season)
            ->orderBy('r.rankPosition', 'ASC')
            ->getQuery()
            ->getResult();
    }

    public function findGroupedByWeightClass(string $season = 'CURRENT'): array
    {
        return $this->createQueryBuilder('r')
            ->where('r.season = :season')
            ->setParameter('season', $season)
            ->orderBy('r.weightClass', 'ASC')
            ->addOrderBy('r.rankPosition', 'ASC')
            ->getQuery()
            ->getResult();
    }
}
