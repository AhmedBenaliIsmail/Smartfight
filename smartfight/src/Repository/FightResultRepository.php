<?php

namespace App\Repository;

use App\Entity\Event;
use App\Entity\Fighter;
use App\Entity\FightResult;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

class FightResultRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, FightResult::class);
    }

    public function findByEvent(Event $event): array
    {
        return $this->createQueryBuilder('fr')
            ->where('fr.event = :event')
            ->setParameter('event', $event)
            ->orderBy('fr.fightNumber', 'ASC')
            ->addOrderBy('fr.fightDate', 'ASC')
            ->getQuery()
            ->getResult();
    }

    public function findCompletedByFighter(Fighter $fighter): array
    {
        return $this->createQueryBuilder('fr')
            ->where('(fr.fighterRed = :fighter OR fr.fighterBlue = :fighter)')
            ->andWhere('fr.status = :status')
            ->setParameter('fighter', $fighter)
            ->setParameter('status', 'COMPLETED')
            ->orderBy('fr.fightDate', 'DESC')
            ->getQuery()
            ->getResult();
    }

    public function findAllCompleted(): array
    {
        return $this->createQueryBuilder('fr')
            ->where('fr.status = :status')
            ->setParameter('status', 'COMPLETED')
            ->orderBy('fr.fightDate', 'DESC')
            ->getQuery()
            ->getResult();
    }

    public function findByStatus(string $status): array
    {
        return $this->createQueryBuilder('fr')
            ->where('fr.status = :status')
            ->setParameter('status', strtoupper($status))
            ->orderBy('fr.fightDate', 'DESC')
            ->getQuery()
            ->getResult();
    }
}
