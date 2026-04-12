<?php

namespace App\Repository;

use App\Entity\FightStatistic;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

class FightStatisticRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, FightStatistic::class);
    }

    public function findByFightResult(int $fightResultId): array
    {
        return $this->createQueryBuilder('fs')
            ->where('fs.fightResultId = :fightResultId')
            ->setParameter('fightResultId', $fightResultId)
            ->orderBy('fs.fighterId', 'ASC')
            ->getQuery()
            ->getResult();
    }

    public function findByFighter(int $fighterId, int $limit = 50): array
    {
        return $this->createQueryBuilder('fs')
            ->where('fs.fighterId = :fighterId')
            ->setParameter('fighterId', $fighterId)
            ->orderBy('fs.updatedAt', 'DESC')
            ->setMaxResults($limit)
            ->getQuery()
            ->getResult();
    }
}
