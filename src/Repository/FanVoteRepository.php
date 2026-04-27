<?php

namespace App\Repository;

use App\Entity\FanVote;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

class FanVoteRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, FanVote::class);
    }

    public function hasUserVoted(int $userId, int $proposalId): bool
    {
        return (bool) $this->createQueryBuilder('v')
            ->select('COUNT(v.id)')
            ->where('v.user = :userId')
            ->andWhere('v.matchProposal = :proposalId')
            ->setParameter('userId', $userId)
            ->setParameter('proposalId', $proposalId)
            ->getQuery()
            ->getSingleScalarResult();
    }

    public function getVoteCountByProposal(int $proposalId): int
    {
        return (int) $this->createQueryBuilder('v')
            ->select('COUNT(v.id)')
            ->where('v.matchProposal = :proposalId')
            ->setParameter('proposalId', $proposalId)
            ->getQuery()
            ->getSingleScalarResult();
    }

    /**
     * Get all proposal IDs that a user has voted on.
     */
    public function findVotedProposalIdsByUser(int $userId): array
    {
        $results = $this->createQueryBuilder('v')
            ->select('IDENTITY(v.matchProposal) as proposalId')
            ->where('v.user = :userId')
            ->setParameter('userId', $userId)
            ->getQuery()
            ->getScalarResult();

        return array_column($results, 'proposalId');
    }
}
