<?php

namespace App\Repository;

use App\Entity\FanPrediction;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\DBAL\ParameterType;
use Doctrine\ORM\QueryBuilder;
use Doctrine\Persistence\ManagerRegistry;

class FanPredictionRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, FanPrediction::class);
    }

    public function findByFanQueryBuilder(int $fanId): QueryBuilder
    {
        return $this->createQueryBuilder('p')
            ->leftJoin('p.matchProposal', 'mp')
            ->leftJoin('p.predictedWinner', 'fw')
            ->where('p.fan = :fanId')
            ->setParameter('fanId', $fanId)
            ->orderBy('p.submittedAt', 'DESC');
    }

    public function findFilteredForAdmin(?string $search, ?int $eventId, ?string $resultStatus, ?string $method): QueryBuilder
    {
        $qb = $this->createQueryBuilder('p')
            ->leftJoin('p.fan', 'u')
            ->leftJoin('p.matchProposal', 'mp')
            ->leftJoin('mp.event', 'e')
            ->orderBy('p.submittedAt', 'DESC');

        if ($search) {
            $qb->andWhere('u.firstName LIKE :s OR u.lastName LIKE :s')
               ->setParameter('s', '%' . $search . '%');
        }
        if ($eventId) {
            $qb->andWhere('mp.event = :eventId')->setParameter('eventId', $eventId);
        }
        if ($resultStatus === 'correct') {
            $qb->andWhere('p.isScored = true AND p.pointsEarned > 0');
        } elseif ($resultStatus === 'wrong') {
            $qb->andWhere('p.isScored = true AND (p.pointsEarned = 0 OR p.pointsEarned IS NULL)');
        } elseif ($resultStatus === 'pending') {
            $qb->andWhere('p.isScored = false');
        }
        if ($method) {
            $qb->andWhere('p.predictedMethod = :method')->setParameter('method', $method);
        }

        return $qb;
    }

    public function findByMatchProposal(int $matchProposalId): array
    {
        return $this->createQueryBuilder('p')
            ->leftJoin('p.fan', 'u')
            ->leftJoin('p.predictedWinner', 'fw')
            ->where('p.matchProposal = :mpId')
            ->setParameter('mpId', $matchProposalId)
            ->getQuery()
            ->getResult();
    }

    public function findLeaderboard(string $season = '2026', int $limit = 50): array
    {
        $conn = $this->getEntityManager()->getConnection();
        $sql = "
            SELECT
                u.id AS fan_id,
                u.first_name,
                u.last_name,
                COALESCE(SUM(fp.points_earned), 0) AS total_points,
                COUNT(fp.id) AS total_predictions,
                SUM(CASE WHEN fp.points_earned > 0 THEN 1 ELSE 0 END) AS correct_predictions
            FROM fan_prediction fp
            JOIN user u ON fp.fan_id = u.id
            WHERE fp.is_scored = 1 AND fp.season = :season
            GROUP BY u.id
            ORDER BY total_points DESC
            LIMIT :lim
        ";

        return $conn->executeQuery($sql, ['season' => $season, 'lim' => $limit], ['lim' => ParameterType::INTEGER])->fetchAllAssociative();
    }

    public function getStats(): array
    {
        $conn = $this->getEntityManager()->getConnection();
        $sql = "
            SELECT
                COUNT(*) AS total_predictions,
                SUM(CASE WHEN is_scored = 1 AND points_earned > 0 THEN 1 ELSE 0 END) AS correct_picks,
                SUM(CASE WHEN is_scored = 0 THEN 1 ELSE 0 END) AS pending_count,
                COUNT(DISTINCT fan_id) AS participating_fans
            FROM fan_prediction
        ";

        return $conn->executeQuery($sql)->fetchAssociative();
    }
}
