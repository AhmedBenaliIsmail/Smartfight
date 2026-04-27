<?php
namespace App\Repository;

use App\Entity\Prediction;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

class PredictionRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Prediction::class);
    }

    public function findByUser(int $userId): array
    {
        return $this->findBy(['user' => $userId], ['createdAt' => 'DESC']);
    }

    public function findPendingByFight(int $fightId): array
    {
        return $this->findBy(['fight' => $fightId, 'isProcessed' => false]);
    }

    public function findOneByUserAndFight(int $userId, int $fightId): ?Prediction
    {
        return $this->findOneBy(['user' => $userId, 'fight' => $fightId]);
    }
}
