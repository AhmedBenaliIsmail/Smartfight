<?php

namespace App\Repository;

use App\Entity\FanNotification;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\ORM\QueryBuilder;
use Doctrine\Persistence\ManagerRegistry;

class FanNotificationRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, FanNotification::class);
    }

    public function findByFanQueryBuilder(int $fanId, ?string $type = null, bool $unreadOnly = false): QueryBuilder
    {
        $qb = $this->createQueryBuilder('n')
            ->where('n.fan = :fanId')
            ->setParameter('fanId', $fanId)
            ->orderBy('n.createdAt', 'DESC');

        if ($type) {
            $qb->andWhere('n.type = :type')->setParameter('type', $type);
        }
        if ($unreadOnly) {
            $qb->andWhere('n.isRead = false');
        }

        return $qb;
    }

    public function findAllForAdminQueryBuilder(?string $type = null): QueryBuilder
    {
        $qb = $this->createQueryBuilder('n')
            ->leftJoin('n.fan', 'u')
            ->orderBy('n.createdAt', 'DESC');

        if ($type) {
            $qb->andWhere('n.type = :type')->setParameter('type', $type);
        }

        return $qb;
    }

    public function markAllReadForFan(int $fanId): void
    {
        $this->createQueryBuilder('n')
            ->update()
            ->set('n.isRead', 'true')
            ->where('n.fan = :fanId')
            ->setParameter('fanId', $fanId)
            ->getQuery()
            ->execute();
    }

    public function markReadForFan(int $notificationId, int $fanId): int
    {
        return (int) $this->createQueryBuilder('n')
            ->update()
            ->set('n.isRead', 'true')
            ->where('n.id = :notificationId')
            ->andWhere('n.fan = :fanId')
            ->andWhere('n.isRead = false')
            ->setParameter('notificationId', $notificationId)
            ->setParameter('fanId', $fanId)
            ->getQuery()
            ->execute();
    }

    public function countUnreadForFan(int $fanId): int
    {
        return (int) $this->createQueryBuilder('n')
            ->select('COUNT(n.id)')
            ->where('n.fan = :fanId')
            ->andWhere('n.isRead = false')
            ->setParameter('fanId', $fanId)
            ->getQuery()
            ->getSingleScalarResult();
    }

    public function findLatestForFan(int $fanId): ?FanNotification
    {
        return $this->createQueryBuilder('n')
            ->where('n.fan = :fanId')
            ->setParameter('fanId', $fanId)
            ->orderBy('n.createdAt', 'DESC')
            ->setMaxResults(1)
            ->getQuery()
            ->getOneOrNullResult();
    }

    public function getAdminStats(): array
    {
        $conn = $this->getEntityManager()->getConnection();
        $sql = "
            SELECT
                COUNT(*) AS total_sent,
                SUM(is_read) AS read_count,
                SUM(CASE WHEN type = 'ADMIN_BROADCAST' THEN 1 ELSE 0 END) AS broadcast_count,
                SUM(CASE WHEN is_read = 0 THEN 1 ELSE 0 END) AS unread_count
            FROM fan_notification
        ";
        return $conn->executeQuery($sql)->fetchAssociative();
    }
}
