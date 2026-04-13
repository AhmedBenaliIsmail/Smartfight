<?php

namespace App\Repository;

use App\Entity\Event;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Event>
 */
class EventRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Event::class);
    }

    /**
     * Find upcoming events ordered by start date
     */
    public function findUpcoming(int $limit = 6): array
    {
        return $this->createQueryBuilder('e')
            ->where('e.startDate >= :today')
            ->setParameter('today', new \DateTime('today'))
            ->orderBy('e.startDate', 'ASC')
            ->setMaxResults($limit)
            ->getQuery()
            ->getResult();
    }

    /**
     * Find events by status
     */
    public function findByStatus(string $status): array
    {
        return $this->createQueryBuilder('e')
            ->where('e.status = :status')
            ->setParameter('status', $status)
            ->orderBy('e.startDate', 'DESC')
            ->getQuery()
            ->getResult();
    }

    /**
     * Find events by year and month
     */
    public function findByMonth(int $year, int $month): array
    {
        return $this->createQueryBuilder('e')
            ->where('YEAR(e.startDate) = :year')
            ->andWhere('MONTH(e.startDate) = :month')
            ->setParameter('year', $year)
            ->setParameter('month', $month)
            ->getQuery()
            ->getResult();
    }

    /**
     * Count registered fighters for an event using raw SQL
     */
    public function countRegisteredFighters(int $eventId): int
    {
        $conn = $this->getEntityManager()->getConnection();
        $sql = 'SELECT COUNT(ef.fighter_id) as cnt FROM event_fighter ef WHERE ef.event_id = :id';
        $result = $conn->executeQuery($sql, ['id' => $eventId]);
        return (int) $result->fetchOne();
    }

    /**
     * Search events by name (LIKE)
     */
    public function searchByName(string $query): array
    {
        return $this->createQueryBuilder('e')
            ->where('e.name LIKE :q')
            ->setParameter('q', '%' . $query . '%')
            ->orderBy('e.startDate', 'DESC')
            ->getQuery()
            ->getResult();
    }
}
