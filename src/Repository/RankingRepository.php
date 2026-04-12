<?php
namespace App\Repository;

use App\Entity\Ranking;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

class RankingRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Ranking::class);
    }

    public function findByFighterAndSeason(int $fighterId, string $season): ?Ranking
    {
        return $this->findOneBy(['fighterId' => $fighterId, 'season' => $season]);
    }

    public function findAllBySeason(string $season): array
    {
        return $this->findBy(['season' => $season], ['rankPosition' => 'ASC']);
    }

    public function findByFighter(int $fighterId): ?Ranking
    {
        return $this->findOneBy(['fighterId' => $fighterId], ['updatedAt' => 'DESC']);
    }

    public function deleteAllBySeason(string $season): void
    {
        $this->createQueryBuilder('r')
            ->delete()
            ->where('r.season = :s')
            ->setParameter('s', $season)
            ->getQuery()->execute();
    }
}
