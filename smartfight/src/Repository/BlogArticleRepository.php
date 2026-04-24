<?php

namespace App\Repository;

use App\Entity\BlogArticle;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\ORM\QueryBuilder;
use Doctrine\Persistence\ManagerRegistry;

class BlogArticleRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, BlogArticle::class);
    }

    public function findPublishedQueryBuilder(): QueryBuilder
    {
        return $this->createQueryBuilder('a')
            ->where('a.status = :status')
            ->setParameter('status', 'PUBLISHED')
            ->orderBy('a.createdAt', 'DESC');
    }

    public function findFilteredForAdmin(?string $search, ?int $categoryId, ?string $status): QueryBuilder
    {
        $qb = $this->createQueryBuilder('a')
            ->leftJoin('a.author', 'u')
            ->leftJoin('a.category', 'c')
            ->orderBy('a.createdAt', 'DESC');

        if ($search) {
            $qb->andWhere('a.title LIKE :search OR u.firstName LIKE :search OR u.lastName LIKE :search')
               ->setParameter('search', '%' . $search . '%');
        }
        if ($categoryId) {
            $qb->andWhere('a.category = :catId')->setParameter('catId', $categoryId);
        }
        if ($status) {
            $qb->andWhere('a.status = :status')->setParameter('status', $status);
        }

        return $qb;
    }

    public function findFeatured(): ?BlogArticle
    {
        return $this->createQueryBuilder('a')
            ->where('a.status = :status')
            ->setParameter('status', 'PUBLISHED')
            ->orderBy('a.createdAt', 'DESC')
            ->setMaxResults(1)
            ->getQuery()
            ->getOneOrNullResult();
    }

    public function findRelatedArticles(int $categoryId, int $excludeId, int $limit = 3): array
    {
        return $this->createQueryBuilder('a')
            ->where('a.category = :catId')
            ->andWhere('a.id != :excludeId')
            ->andWhere('a.status = :status')
            ->setParameter('catId', $categoryId)
            ->setParameter('excludeId', $excludeId)
            ->setParameter('status', 'PUBLISHED')
            ->orderBy('a.createdAt', 'DESC')
            ->setMaxResults($limit)
            ->getQuery()
            ->getResult();
    }

    public function incrementViewCount(int $id): void
    {
        $this->createQueryBuilder('a')
            ->update()
            ->set('a.viewCount', 'a.viewCount + 1')
            ->where('a.id = :id')
            ->setParameter('id', $id)
            ->getQuery()
            ->execute();
    }
}
