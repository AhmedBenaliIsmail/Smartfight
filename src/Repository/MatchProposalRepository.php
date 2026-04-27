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

    public function findTopVoted(int $limit = 3): array
    {
        return $this->createQueryBuilder('mp')
            ->where('mp.status = :status')
            ->setParameter('status', 'PENDING')
            ->orderBy('mp.voteCount', 'DESC')
            ->setMaxResults($limit)
            ->getQuery()
            ->getResult();
    }
}
