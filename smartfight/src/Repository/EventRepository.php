<?php

namespace App\Repository;

use App\Entity\Event;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

class EventRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Event::class);
    }

    public function findUpcoming(): array
    {
        return $this->createQueryBuilder('e')
            ->where('e.status = :status')
            ->setParameter('status', 'SCHEDULED')
            ->orderBy('e.startsAt', 'ASC')
            ->getQuery()
            ->getResult();
    }

    public function findBookableEvents(): array
    {
        return $this->createQueryBuilder('e')
            ->where('e.status = :status')
            ->andWhere('e.visibility = :visibility')
            ->setParameter('status', 'SCHEDULED')
            ->setParameter('visibility', 'PUBLIC')
            ->orderBy('e.startsAt', 'ASC')
            ->getQuery()
            ->getResult();
    }
}
