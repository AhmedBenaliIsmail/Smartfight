<?php

namespace App\Repository;

use App\Entity\Ranking;
use App\Entity\WeightClass;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

class RankingRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Ranking::class);
    }

    public function findByWeightClassAndSeason(?WeightClass $weightClass, string $season = 'CURRENT'): array
    {
        return $this->createQueryBuilder('r')
            ->where('r.weightClassEntity = :weightClass')
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
            ->leftJoin('r.weightClassEntity', 'wc')
            ->addSelect('wc')
            ->where('r.season = :season')
            ->setParameter('season', $season)
            ->orderBy('wc.displayOrder', 'ASC')
            ->addOrderBy('r.weightClass', 'ASC')
            ->addOrderBy('r.rankPosition', 'ASC')
            ->getQuery()
            ->getResult();
    }
}
