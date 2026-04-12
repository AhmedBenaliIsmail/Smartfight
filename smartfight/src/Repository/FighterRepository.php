<?php

namespace App\Repository;

use App\Entity\Fighter;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

class FighterRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Fighter::class);
    }

    public function findByWeightClass(string $weightClass): array
    {
        return $this->createQueryBuilder('f')
            ->where('f.weightClass = :weightClass')
            ->setParameter('weightClass', $weightClass)
            ->orderBy('f.eloRating', 'DESC')
            ->getQuery()
            ->getResult();
    }

    public function findRankedFighters(?string $weightClass = null, int $limit = 50): array
    {
        $qb = $this->createQueryBuilder('f')
            ->orderBy('f.eloRating', 'DESC')
            ->addOrderBy('f.performanceScore', 'DESC')
            ->setMaxResults($limit);

        if ($weightClass !== null) {
            $qb->andWhere('f.weightClass = :weightClass')
                ->setParameter('weightClass', $weightClass);
        }

        return $qb->getQuery()->getResult();
    }

    public function search(string $term, ?string $weightClass = null): array
    {
        $qb = $this->createQueryBuilder('f')
            ->leftJoin('f.user', 'u')
            ->where('LOWER(f.nickname) LIKE :term')
            ->orWhere('LOWER(f.nationality) LIKE :term')
            ->orWhere('LOWER(f.weightClass) LIKE :term')
            ->orWhere('LOWER(u.firstName) LIKE :term')
            ->orWhere('LOWER(u.lastName) LIKE :term')
            ->setParameter('term', '%' . strtolower($term) . '%')
            ->orderBy('f.eloRating', 'DESC');

        if ($weightClass !== null) {
            $qb->andWhere('f.weightClass = :weightClass')
                ->setParameter('weightClass', $weightClass);
        }

        return $qb->getQuery()->getResult();
    }
}
