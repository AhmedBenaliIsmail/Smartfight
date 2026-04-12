<?php

namespace App\Repository;

use App\Entity\BlogCategory;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

class BlogCategoryRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, BlogCategory::class);
    }

    public function findAllWithArticleCount(): array
    {
        return $this->createQueryBuilder('c')
            ->select('c', 'COUNT(a.id) AS articleCount')
            ->leftJoin('c.articles', 'a', 'WITH', 'a.status = :status')
            ->setParameter('status', 'PUBLISHED')
            ->groupBy('c.id')
            ->orderBy('c.name', 'ASC')
            ->getQuery()
            ->getResult();
    }
}
