<?php

namespace App\Service;

use App\Entity\FightStatistic;
use App\Entity\FightResult;
use App\Repository\FightStatisticRepository;
use Doctrine\ORM\EntityManagerInterface;

class FightStatisticService
{
    public function __construct(
        private readonly EntityManagerInterface $entityManager,
        private readonly FightStatisticRepository $fightStatisticRepository,
        private readonly AnalyticsEngine $analyticsEngine,
    ) {
    }

    public function upsertForFighter(FightResult $fightResult, int $fighterId, array $payload): FightStatistic
    {
        $stat = $this->entityManager->getRepository(FightStatistic::class)->findOneBy([
            'fightResultId' => $fightResult->getId(),
            'fighterId' => $fighterId,
        ]) ?? new FightStatistic();

        $stat
            ->setFightResultId((int) $fightResult->getId())
            ->setFighterId($fighterId)
            ->setStrikesLanded((int) ($payload['strikesLanded'] ?? 0))
            ->setStrikesAttempted((int) ($payload['strikesAttempted'] ?? 0))
            ->setTakedownsLanded((int) ($payload['takedownsLanded'] ?? 0))
            ->setTakedownsAttempted((int) ($payload['takedownsAttempted'] ?? 0))
            ->setSubmissionAttempts((int) ($payload['submissionAttempts'] ?? 0))
            ->setKnockdowns((int) ($payload['knockdowns'] ?? 0))
            ->setControlTimeSeconds((int) ($payload['controlTimeSeconds'] ?? 0));

        $this->entityManager->persist($stat);
        $this->entityManager->flush();

        if ($fightResult->getFighterRed()->getId() === $fighterId) {
            $this->analyticsEngine->recomputeFighter($fightResult->getFighterRed());
        }

        if ($fightResult->getFighterBlue()->getId() === $fighterId) {
            $this->analyticsEngine->recomputeFighter($fightResult->getFighterBlue());
        }

        return $stat;
    }

    public function listByFightResult(int $fightResultId): array
    {
        return $this->fightStatisticRepository->findByFightResult($fightResultId);
    }
}
