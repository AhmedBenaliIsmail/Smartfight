<?php
namespace App\Service;

use App\Entity\FightResult;
use App\Entity\Event;
use App\Repository\FightResultRepository;
use App\Repository\FighterRepository;
use Doctrine\ORM\EntityManagerInterface;

/**
 * FightResultService — Remodeled for Boxing
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
     * Schedule a new fight.
     */
    public function addScheduledFight(
        int $eventId, 
        int $fightNumber, 
        int $fighter1Id, 
        int $fighter2Id,
        int $scheduledRounds = 12,
        bool $isBeltFight = false,
        ?string $beltOrganization = null,
        ?float $fighter1Odds = null,
        ?float $fighter2Odds = null
    ): bool {
        // Enforce 3-fight limit
        $existingCount = $this->resultRepo->countByEvent($eventId);
        if ($existingCount >= 3) {
            throw new \RuntimeException('This event has reached the maximum capacity of 3 bouts.');
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
        $event = $this->em->getRepository(Event::class)->find($eventId);

        if (!$f1 || !$f2 || !$event) {
            throw new \RuntimeException('One or both fighters or event not found.');
        }

        if ($f1->getWeightDivision() && $f2->getWeightDivision() && $f1->getWeightDivision()->getId() !== $f2->getWeightDivision()->getId()) {
            throw new \RuntimeException('Fighters must be in the same weight division to be scheduled.');
        }

        $fr = new FightResult();
        $fr->setEvent($event);
        $fr->setFightNumber($fightNumber);
        $fr->setFighter1($f1);
        $fr->setFighter2($f2);
        $fr->setScheduledRounds($scheduledRounds);
        $fr->setIsBeltFight($isBeltFight);
        $fr->setBeltOrganization($beltOrganization);
        $fr->setFighter1Odds($fighter1Odds);
        $fr->setFighter2Odds($fighter2Odds);
        $fr->setStatus('SCHEDULED');

        $this->em->persist($fr);
        $this->em->flush();
        return true;
    }

    /**
     * Enter result for a scheduled fight.
     */
    public function enterResult(
        int $resultId, 
        ?int $winnerId, 
        string $method, 
        int $round, 
        ?string $decisionType = null,
        ?int $knockdownRound = null,
        ?\DateTimeInterface $fightDate = null
    ): bool {
        $fr = $this->resultRepo->find($resultId);
        if (!$fr || $fr->getStatus() !== 'SCHEDULED') return false;

        if ($winnerId !== null) {
            $winner = $this->fighterRepo->find($winnerId);
            $fr->setWinner($winner);
        } else {
            $fr->setWinner(null);
        }

        $fr->setMethodOfVictory($method);
        $fr->setRoundNumber($round);
        $fr->setDecisionType($decisionType);
        $fr->setKnockdownRound($knockdownRound);
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
        // Only 3 fight slots exist per event (enforced in addScheduledFight)
        $used = $this->resultRepo->createQueryBuilder('r')
            ->select('r.fightNumber')
            ->where('r.event = :eid')
            ->setParameter('eid', $eventId)
            ->getQuery()->getSingleColumnResult();

        $available = [];
        for ($i = 1; $i <= 3; $i++) {
            if (!in_array($i, $used)) $available[] = $i;
        }
        return $available;
    }
}
