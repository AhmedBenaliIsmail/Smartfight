<?php
namespace App\Service;

use App\Entity\FightResult;
use App\Repository\FightResultRepository;
use App\Repository\FighterRepository;
use Doctrine\ORM\EntityManagerInterface;

/**
 * FightResultService — port of Java FightResultservice.java
 */
class FightResultService
{
    public function __construct(
        private EntityManagerInterface $em,
        private FightResultRepository $resultRepo,
        private FighterRepository $fighterRepo,
        private RankingService $rankingService,
    ) {}

    public function getAllFightResults(): array
    {
        return $this->resultRepo->findAllOrdered();
    }

    public function getFightResultById(int $id): ?FightResult
    {
        return $this->resultRepo->find($id);
    }

    public function getFightResultsByEvent(int $eventId): array
    {
        return $this->resultRepo->findByEvent($eventId);
    }

    /**
     * Schedule a new fight (max 3 per event, no duplicate fighters).
     */
    public function addScheduledFight(int $eventId, int $fightNumber, int $fighter1Id, int $fighter2Id): bool
    {
        // Validate max 3 fights
        if ($this->resultRepo->countByEvent($eventId) >= 3) {
            throw new \RuntimeException('Event already has 3 fights (maximum).');
        }

        // Validate fighters not already in event
        $usedIds = $this->resultRepo->getFighterIdsInEvent($eventId);
        if (in_array($fighter1Id, $usedIds) || in_array($fighter2Id, $usedIds)) {
            throw new \RuntimeException('One or both fighters are already scheduled in this event.');
        }

        if ($fighter1Id === $fighter2Id) {
            throw new \RuntimeException('A fighter cannot fight themselves.');
        }

        $f1 = $this->fighterRepo->find($fighter1Id);
        $f2 = $this->fighterRepo->find($fighter2Id);

        if (!$f1 || !$f2) {
            throw new \RuntimeException('One or both fighters not found.');
        }

        if ($f1->getWeightClass() !== $f2->getWeightClass()) {
            throw new \RuntimeException('Fighters must be in the same weight class to be scheduled.');
        }

        $fr = new FightResult();
        $fr->setEventId($eventId);
        $fr->setFightNumber($fightNumber);
        $fr->setFighter1Id($fighter1Id);
        $fr->setFighter2Id($fighter2Id);
        $fr->setStatus('SCHEDULED');

        $this->em->persist($fr);
        $this->em->flush();
        return true;
    }

    /**
     * Enter result for a scheduled fight.
     */
    public function enterResult(int $resultId, ?int $winnerId, string $method, int $round, ?\DateTimeInterface $fightDate = null): bool
    {
        $fr = $this->resultRepo->find($resultId);
        if (!$fr || $fr->getStatus() !== 'SCHEDULED') return false;

        $fr->setWinnerId($winnerId);
        $fr->setMethodOfVictory($method);
        $fr->setRoundNumber($round);
        $fr->setFightDate($fightDate ?? new \DateTime());
        $fr->setStatus('COMPLETED');

        $this->em->persist($fr);
        $this->em->flush();

        // Process rankings
        $this->rankingService->processCompletedFight($fr);

        return true;
    }

    public function cancelFight(int $resultId): bool
    {
        $fr = $this->resultRepo->find($resultId);
        if (!$fr) return false;
        $fr->setStatus('CANCELLED');
        $this->em->persist($fr);
        $this->em->flush();
        return true;
    }

    public function deleteFightResult(int $resultId): bool
    {
        $fr = $this->resultRepo->find($resultId);
        if (!$fr) return false;
        $this->em->remove($fr);
        $this->em->flush();
        return true;
    }

    public function getAvailableFightNumbers(int $eventId): array
    {
        return $this->resultRepo->getAvailableFightNumbers($eventId);
    }
}
