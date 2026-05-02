<?php

namespace App\Repository;

use App\Entity\FighterContract;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<FighterContract>
 */
class FighterContractRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, FighterContract::class);
    }

    public function getFinancialStats(): array
    {
        return $this->createQueryBuilder('fc')
            ->select('COALESCE(SUM(fc.basePay + fc.winBonus), 0) as total_committed')
            ->addSelect('COALESCE(SUM(CASE WHEN fc.isPaid = true THEN fc.calculatedPayout ELSE 0 END), 0) as total_paid')
            ->addSelect('COALESCE(SUM(CASE WHEN fc.isPaid = false AND fc.calculatedPayout IS NOT NULL THEN fc.calculatedPayout ELSE 0 END), 0) as total_pending')
            ->addSelect('COALESCE(SUM(fc.calculatedPayout), 0) as total_calculated_payouts')
            ->getQuery()
            ->getSingleResult();
    }
}
