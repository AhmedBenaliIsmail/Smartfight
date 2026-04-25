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

    public function findAllOrderedByDate(): array
    {
        return $this->createQueryBuilder('e')
            ->orderBy('e.eventDate', 'DESC')
            ->getQuery()->getResult();
    }

    public function findUpcoming(): array
    {
        return $this->createQueryBuilder('e')
            ->where('e.eventDate >= :today')
            ->setParameter('today', new \DateTime('today'))
            ->orderBy('e.eventDate', 'ASC')
            ->getQuery()->getResult();
    }

    public function findFinishedEvents(): array
    {
        return $this->createQueryBuilder('e')
            ->where('e.eventDate < :today')
            ->setParameter('today', new \DateTime('today'))
            ->orderBy('e.eventDate', 'DESC')
            ->getQuery()->getResult();
    }

    public function findChampionsEvents(bool $onlyPast = false): array
    {
        $qb = $this->createQueryBuilder('e')
            ->where('e.isChampionsEvent = :isCE')
            ->setParameter('isCE', true);

        if ($onlyPast) {
            $qb->andWhere('e.eventDate <= :today')
               ->setParameter('today', new \DateTime('now'));
        }

        return $qb->orderBy('e.eventDate', 'DESC')
            ->getQuery()->getResult();
    }
    public function findBookableEvents(): array
    {
        return $this->createQueryBuilder('e')
            ->where('e.status = :status')
            ->setParameter('status', 'SCHEDULED')
            ->orderBy('e.eventDate', 'ASC')
            ->getQuery()->getResult();
    }
}
