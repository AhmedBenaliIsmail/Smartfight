<?php

namespace App\Repository;

use App\Entity\EventBooking;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\ORM\QueryBuilder;
use Doctrine\Persistence\ManagerRegistry;

class EventBookingRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, EventBooking::class);
    }

    public function findByUserWithEvent(int $userId): array
    {
        return $this->createQueryBuilder('b')
            ->leftJoin('b.event', 'e')->addSelect('e')
            ->where('b.user = :userId')
            ->setParameter('userId', $userId)
            ->orderBy('b.bookingDate', 'DESC')
            ->getQuery()
            ->getResult();
    }

    public function findOneByEventAndUser(int $eventId, int $userId): ?EventBooking
    {
        return $this->createQueryBuilder('b')
            ->where('b.event = :eventId')
            ->andWhere('b.user = :userId')
            ->setParameter('eventId', $eventId)
            ->setParameter('userId', $userId)
            ->getQuery()
            ->getOneOrNullResult();
    }

    public function getTotalConfirmedQty(int $eventId): int
    {
        return (int) $this->createQueryBuilder('b')
            ->select('COALESCE(SUM(b.ticketQuantity), 0)')
            ->where('b.event = :eventId')
            ->andWhere('b.bookingStatus = :status')
            ->setParameter('eventId', $eventId)
            ->setParameter('status', EventBooking::STATUS_CONFIRMED)
            ->getQuery()
            ->getSingleScalarResult();
    }

    public function findForAdmin(?int $eventId = null, ?string $status = null): QueryBuilder
    {
        $qb = $this->createQueryBuilder('b')
            ->leftJoin('b.event', 'e')->addSelect('e')
            ->leftJoin('b.user', 'u')->addSelect('u')
            ->orderBy('b.createdAt', 'DESC');

        if ($eventId) {
            $qb->andWhere('e.eventId = :eventId')->setParameter('eventId', $eventId);
        }

        if ($status) {
            $qb->andWhere('b.bookingStatus = :status')->setParameter('status', $status);
        }

        return $qb;
    }

    public function getAdminStats(): array
    {
        $conn = $this->getEntityManager()->getConnection();

        $row = $conn->executeQuery(
            'SELECT
                COUNT(*) AS total_bookings,
                SUM(CASE WHEN booking_status = :confirmed THEN 1 ELSE 0 END) AS confirmed_count,
                SUM(CASE WHEN booking_status = :cancelled THEN 1 ELSE 0 END) AS cancelled_count,
                COALESCE(SUM(CASE WHEN booking_status = :confirmed THEN total_price ELSE 0 END), 0) AS total_revenue
             FROM event_booking',
            [
                'confirmed' => EventBooking::STATUS_CONFIRMED,
                'cancelled' => EventBooking::STATUS_CANCELLED,
            ]
        )->fetchAssociative();

        return $row ?: [
            'total_bookings' => 0,
            'confirmed_count' => 0,
            'cancelled_count' => 0,
            'total_revenue' => 0,
        ];
    }
}
