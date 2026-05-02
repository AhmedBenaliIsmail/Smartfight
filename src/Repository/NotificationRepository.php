<?php
namespace App\Repository;

use App\Entity\Notification;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

class NotificationRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Notification::class);
    }

    public function findUnreadByUser(int $userId): array
    {
        return $this->findBy(['user' => $userId, 'isRead' => false], ['createdAt' => 'DESC']);
    }

    public function findRecentByUser(int $userId, int $limit = 5): array
    {
        return $this->findBy(['user' => $userId], ['createdAt' => 'DESC'], $limit);
    }
}
