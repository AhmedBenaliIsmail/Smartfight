<?php

namespace App\Repository;

use App\Entity\MatchProposal;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

class MatchProposalRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, MatchProposal::class);
    }

    public function findByEvent(int $eventId): array
    {
        return $this->createQueryBuilder('mp')
            ->where('mp.event = :eventId')
            ->setParameter('eventId', $eventId)
            ->orderBy('mp.proposedAt', 'DESC')
            ->getQuery()
            ->getResult();
    }
}
