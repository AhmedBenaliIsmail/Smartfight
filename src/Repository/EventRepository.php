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
            ->innerJoin('App\Entity\FightResult', 'fr', 'WITH', 'fr.event = e')
            ->where('fr.status = :status')
            ->setParameter('status', 'COMPLETED')
            ->groupBy('e.eventId')
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

    public function findFilteredAndSortedEvents(?string $query, ?string $organization, ?string $status, ?string $sort): \Doctrine\ORM\QueryBuilder
    {
        $qb = $this->createQueryBuilder('e');

        if (!empty($query)) {
            $qb->andWhere('e.eventName LIKE :query OR e.venue LIKE :query OR e.city LIKE :query')
               ->setParameter('query', '%' . $query . '%');
        }

        if (!empty($organization)) {
            $qb->andWhere('e.organization = :org')
               ->setParameter('org', $organization);
        }

        if (!empty($status)) {
            $qb->andWhere('e.status = :status')
               ->setParameter('status', $status);
        }

        switch ($sort) {
            case 'date_asc':
                $qb->orderBy('e.eventDate', 'ASC');
                break;
            case 'date_desc':
                $qb->orderBy('e.eventDate', 'DESC');
                break;
            case 'capacity_desc':
                $qb->orderBy('e.seatCapacity', 'DESC');
                break;
            case 'capacity_asc':
                $qb->orderBy('e.seatCapacity', 'ASC');
                break;
            default:
                $qb->orderBy('e.eventDate', 'DESC');
                break;
        }

        return $qb;
    }
}
