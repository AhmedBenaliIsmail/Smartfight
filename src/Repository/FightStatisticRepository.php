<?php
namespace App\Repository;

use App\Entity\FightStatistic;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

class FightStatisticRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, FightStatistic::class);
    }

    public function findByFighter(int $fighterId): array
    {
        return $this->findBy(['fighterId' => $fighterId]);
    }

    public function findByFightResult(int $fightResultId): array
    {
        return $this->findBy(['fightResultId' => $fightResultId]);
    }

    public function findByFighterAndFightResult(int $fighterId, int $fightResultId): ?FightStatistic
    {
        return $this->findOneBy(['fighterId' => $fighterId, 'fightResultId' => $fightResultId]);
    }
}
