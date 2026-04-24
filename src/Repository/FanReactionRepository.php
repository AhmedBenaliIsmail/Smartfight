<?php

namespace App\Repository;

use App\Entity\FanReaction;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\ORM\QueryBuilder;
use Doctrine\Persistence\ManagerRegistry;

class FanReactionRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, FanReaction::class);
    }

    public function findFilteredForAdmin(?int $fightResultId, ?string $type, ?string $status): QueryBuilder
    {
        $qb = $this->createQueryBuilder('r')
            ->leftJoin('r.fan', 'u')
            ->leftJoin('r.fightResult', 'fr')
            ->where('r.isDeleted = false')
            ->orderBy('r.reactedAt', 'DESC');

        if ($fightResultId) {
            $qb->andWhere('r.fightResult = :frId')->setParameter('frId', $fightResultId);
        }
        if ($type) {
            $qb->andWhere('r.reactionType = :type')->setParameter('type', $type);
        }
        if ($status === 'pinned') {
            $qb->andWhere('r.isPinned = true');
        } elseif ($status === 'unpinned') {
            $qb->andWhere('r.isPinned = false');
        }

        return $qb;
    }

    public function countByFightResult(int $fightResultId): array
    {
        $conn = $this->getEntityManager()->getConnection();
        $sql = "
            SELECT
                COUNT(*) AS total,
                SUM(is_pinned) AS pinned
            FROM fan_reaction
            WHERE fight_result_id = :frId AND is_deleted = 0
        ";
        return $conn->executeQuery($sql, ['frId' => $fightResultId])->fetchAssociative();
    }
}
