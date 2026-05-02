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
        return $this->findBy(['fighter' => $fighterId]);
    }

    public function findByFightResult(int $fightResultId): array
    {
        return $this->findBy(['fightResult' => $fightResultId], ['round' => 'ASC']);
    }

    public function findByFighterAndFightResult(int $fighterId, int $fightResultId): ?FightStatistic
    {
        return $this->findOneBy(['fighter' => $fighterId, 'fightResult' => $fightResultId]);
    }
}

