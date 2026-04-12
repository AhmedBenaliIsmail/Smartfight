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

    public function findAllOrderedByName(): array
    {
        return $this->createQueryBuilder('f')
            ->orderBy('f.firstName', 'ASC')
            ->getQuery()->getResult();
    }

    public function findByWeightClass(string $wc): array
    {
        return $this->findBy(['weightClass' => $wc]);
    }

    public function findRankedFighters(): array
    {
        return $this->createQueryBuilder('f')
            ->orderBy('f.eloRating', 'DESC')
            ->addOrderBy('f.performanceScore', 'DESC')
            ->getQuery()->getResult();
    }

    public function search(string $q): array
    {
        return $this->createQueryBuilder('f')
            ->where('LOWER(f.firstName) LIKE :q OR LOWER(f.lastName) LIKE :q OR LOWER(f.nickname) LIKE :q OR LOWER(f.weightClass) LIKE :q')
            ->setParameter('q', '%' . strtolower($q) . '%')
            ->orderBy('f.firstName', 'ASC')
            ->getQuery()->getResult();
    }
}
