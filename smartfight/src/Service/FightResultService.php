<?php

namespace App\Service;

use App\Entity\Event;
use App\Entity\FightResult;
use App\Entity\Fighter;
use Doctrine\ORM\EntityManagerInterface;

class FightResultService
{
    public function __construct(
        private readonly EntityManagerInterface $entityManager,
        private readonly RankingService $rankingService,
        private readonly AnalyticsEngine $analyticsEngine,
    ) {
    }

    public function schedule(Event $event, Fighter $red, Fighter $blue, \DateTimeInterface $fightDate, int $fightNumber = 1): FightResult
    {
        $result = (new FightResult())
            ->setEvent($event)
            ->setFighterRed($red)
            ->setFighterBlue($blue)
            ->setFightDate($fightDate)
            ->setFightNumber($fightNumber)
            ->setStatus('SCHEDULED')
            ->setMethod('DECISION');

        $this->entityManager->persist($result);
        $this->entityManager->flush();

        return $result;
    }

    public function enterResult(FightResult $result, ?Fighter $winner, string $method, ?int $round = null, ?\DateTimeInterface $timeEnded = null): FightResult
    {
        $result
            ->setWinner($winner)
            ->setMethod(strtoupper($method))
            ->setRoundEnded($round)
            ->setTimeEnded($timeEnded)
            ->setStatus('COMPLETED');

        $this->entityManager->persist($result);
        $this->entityManager->flush();

        $this->rankingService->processCompletedResult($result);
        $this->analyticsEngine->recomputeFighter($result->getFighterRed());
        $this->analyticsEngine->recomputeFighter($result->getFighterBlue());

        return $result;
    }

    public function cancel(FightResult $result, ?string $reason = null): FightResult
    {
        $result
            ->setStatus('CANCELLED')
            ->setNotes($reason);

        $this->entityManager->persist($result);
        $this->entityManager->flush();

        return $result;
    }
}
