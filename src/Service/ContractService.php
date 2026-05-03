<?php

namespace App\Service;

use App\Entity\FighterContract;
use App\Entity\FightResult;
use Doctrine\ORM\EntityManagerInterface;

class ContractService
{
    private $em;

    public function __construct(EntityManagerInterface $em)
    {
        $this->em = $em;
    }

    /**
     * Advanced Purse Calculation Metier
     */
    public function calculateFinalPurse(FighterContract $contract, FightResult $result): float
    {
        $basePay = $contract->getBasePay();
        $winBonus = 0.0;

        // 1. Check for Win Bonus
        $winner = $result->getWinner();
        if ($winner && $winner->getFighterId() === $contract->getFighter()->getFighterId()) {
            $winBonus = $contract->getWinBonus();
        }

        // 2. Apply Weight-Miss Penalty (20% of Base Pay)
        if ($contract->isMissedWeight()) {
            $basePay = $basePay * 0.8;
        }

        $total = $basePay + $winBonus;
        
        $contract->setCalculatedPayout($total);
        $this->em->persist($contract);
        $this->em->flush();

        return $total;
    }

    public function getNetToFighter(FighterContract $contract): float
    {
        $payout = $contract->getCalculatedPayout() ?? 0.0;
        return $payout * (1 - $contract->getManagerFeePercent());
    }

    public function getManagerFee(FighterContract $contract): float
    {
        $payout = $contract->getCalculatedPayout() ?? 0.0;
        return $payout * $contract->getManagerFeePercent();
    }
}
