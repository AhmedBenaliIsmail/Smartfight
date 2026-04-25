<?php

namespace App\Repository;

use App\Entity\WeightClass;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

class WeightClassRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, WeightClass::class);
    }

    /** @return WeightClass[] */
    public function findByDisciplineName(string $disciplineName): array
    {
        return $this->createQueryBuilder('w')
            ->join('w.discipline', 'd')
            ->where('d.name = :name')
            ->setParameter('name', $disciplineName)
            ->orderBy('w.displayOrder', 'ASC')
            ->getQuery()
            ->getResult();
    }
}
