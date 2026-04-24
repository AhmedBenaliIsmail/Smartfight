<?php
namespace App\Repository;

use App\Entity\FightResult;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

class FightResultRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, FightResult::class);
    }

    public function findAllOrdered(): array
    {
        return $this->createQueryBuilder('r')
            ->orderBy('r.fightDate', 'DESC')
            ->getQuery()->getResult();
    }

    public function findByEvent(int $eventId): array
    {
        return $this->findBy(['event' => $eventId], ['fightNumber' => 'ASC']);
    }

    public function didFighterParticipateInEvent(int $fighterId, int $eventId): bool
    {
        $count = $this->createQueryBuilder('fr')
            ->select('COUNT(fr.resultId)')
            ->where('fr.event = :eid')
            ->andWhere('(fr.fighter1 = :fid OR fr.fighter2 = :fid)')
            ->setParameter('eid', $eventId)
            ->setParameter('fid', $fighterId)
            ->getQuery()->getSingleScalarResult();

        return $count > 0;
    }

    public function findCompletedFightsByEvent(int $eventId): array
    {
        return $this->findBy(['event' => $eventId, 'status' => 'COMPLETED'], ['fightNumber' => 'ASC']);
    }

    public function countByEvent(int $eventId): int
    {
        return $this->count(['event' => $eventId]);
    }

    public function findCompletedByFighter(int $fighterId): array
    {
        return $this->createQueryBuilder('r')
            ->where('(r.fighter1 = :fid OR r.fighter2 = :fid) AND r.status = :s')
            ->setParameter('fid', $fighterId)
            ->setParameter('s', 'COMPLETED')
            ->orderBy('r.fightDate', 'DESC')
            ->getQuery()->getResult();
    }

    public function findAllCompleted(): array
    {
        return $this->createQueryBuilder('r')
            ->where('r.status = :s')
            ->setParameter('s', 'COMPLETED')
            ->orderBy('r.fightDate', 'ASC')
            ->getQuery()->getResult();
    }

    public function findLastFightsByFighter(int $fighterId, int $limit = 5): array
    {
        return $this->createQueryBuilder('r')
            ->where('(r.fighter1 = :fid OR r.fighter2 = :fid) AND r.status = :s')
            ->setParameter('fid', $fighterId)
            ->setParameter('s', 'COMPLETED')
            ->orderBy('r.fightDate', 'DESC')
            ->setMaxResults($limit)
            ->getQuery()->getResult();
    }

    public function getAvailableFightNumbers(int $eventId): array
    {
        $used = $this->createQueryBuilder('r')
            ->select('r.fightNumber')
            ->where('r.event = :eid')
            ->setParameter('eid', $eventId)
            ->getQuery()->getSingleColumnResult();

        $available = [];
        for ($i = 1; $i <= 3; $i++) {
            if (!in_array($i, $used)) $available[] = $i;
        }
        return $available;
    }

    public function getFighterIdsInEvent(int $eventId): array
    {
        $results = $this->findByEvent($eventId);
        $ids = [];
        foreach ($results as $r) {
            if ($r->getFighter1()) $ids[] = $r->getFighter1()->getFighterId();
            if ($r->getFighter2()) $ids[] = $r->getFighter2()->getFighterId();
        }
        return array_unique($ids);
    }
}

