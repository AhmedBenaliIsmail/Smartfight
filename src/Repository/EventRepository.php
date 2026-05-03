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

    /**
     * Returns a QueryBuilder (not an array) so KnpPaginator can slice it.
     */
    public function createOrderedQueryBuilder(): \Doctrine\ORM\QueryBuilder
    {
        return $this->createQueryBuilder('e')
            ->orderBy('e.eventDate', 'DESC');
    }

    /**
     * Returns a QueryBuilder filtered by a search term across name / venue / city.
     * When $search is empty it behaves identically to createOrderedQueryBuilder().
     */
    public function createSearchQueryBuilder(string $search = ''): \Doctrine\ORM\QueryBuilder
    {
        $qb = $this->createQueryBuilder('e')
            ->orderBy('e.eventDate', 'DESC');

        if ($search !== '') {
            $qb->andWhere(
                    $qb->expr()->orX(
                        $qb->expr()->like('LOWER(e.eventName)', ':s'),
                        $qb->expr()->like('LOWER(e.venue)',     ':s'),
                        $qb->expr()->like('LOWER(e.city)',      ':s'),
                        $qb->expr()->like('LOWER(e.organization)', ':s')
                    )
                )
                ->setParameter('s', '%' . mb_strtolower($search) . '%');
        }

        return $qb;
    }

    /**
     * Returns up to $limit events whose name / venue / city match $query.
     * Used by the autocomplete JSON endpoint.
     */
    public function findSuggestions(string $query, int $limit = 8): array
    {
        if ($query === '') {
            return [];
        }

        $term = '%' . mb_strtolower($query) . '%';

        return $this->createQueryBuilder('e')
            ->select('e.eventId, e.eventName, e.venue, e.city, e.organization')
            ->where(
                'LOWER(e.eventName) LIKE :t
                 OR LOWER(e.venue) LIKE :t
                 OR LOWER(e.city) LIKE :t
                 OR LOWER(e.organization) LIKE :t'
            )
            ->setParameter('t', $term)
            ->orderBy('e.eventDate', 'DESC')
            ->setMaxResults($limit)
            ->getQuery()
            ->getArrayResult();
    }


    public function countUpcoming(): int
    {
        return (int) $this->createQueryBuilder('e')
            ->select('COUNT(e.eventId)')
            ->where('e.eventDate >= :today')
            ->setParameter('today', new \DateTime('today'))
            ->getQuery()
            ->getSingleScalarResult();
    }

    public function countPast(): int
    {
        return (int) $this->createQueryBuilder('e')
            ->select('COUNT(e.eventId)')
            ->where('e.eventDate < :today')
            ->setParameter('today', new \DateTime('today'))
            ->getQuery()
            ->getSingleScalarResult();
    }

    public function countChampions(): int
    {
        return (int) $this->createQueryBuilder('e')
            ->select('COUNT(e.eventId)')
            ->where('e.isChampionsEvent = :val')
            ->setParameter('val', true)
            ->getQuery()
            ->getSingleScalarResult();
    }
}
