<?php

namespace App\Service;

use App\Entity\Event;
use App\Enum\EventStatus;
use App\Repository\EventRepository;
use App\Repository\FightResultRepository;
use App\Repository\MatchProposalRepository;
use App\Repository\SystemMetaRepository;
use Doctrine\ORM\EntityManagerInterface;

class EventStatusService
{
    private const THROTTLE_SECONDS = 300;
    private const LIVE_WINDOW_HOURS = 6;
    private const META_KEY = 'last_status_refresh';

    public function __construct(
        private readonly EventRepository $eventRepository,
        private readonly MatchProposalRepository $matchProposalRepository,
        private readonly FightResultRepository $fightResultRepository,
        private readonly SystemMetaRepository $systemMetaRepository,
        private readonly EntityManagerInterface $entityManager,
    ) {}

    public function refreshAllThrottled(): void
    {
        $lastRefresh = $this->systemMetaRepository->getValue(self::META_KEY);

        if ($lastRefresh !== null) {
            $secondsSince = (new \DateTime())->getTimestamp() - (new \DateTime($lastRefresh))->getTimestamp();
            if ($secondsSince < self::THROTTLE_SECONDS) {
                return;
            }
        }

        $this->refreshAll();
        $this->systemMetaRepository->setValue(self::META_KEY, (new \DateTime())->format('Y-m-d H:i:s'));
    }

    public function refreshAll(): int
    {
        $updated = 0;

        foreach ($this->eventRepository->findAll() as $event) {
            if ($this->refreshEvent($event)) {
                ++$updated;
            }
        }

        if ($updated > 0) {
            $this->entityManager->flush();
        }

        return $updated;
    }

    public function refreshAndUpdateMeta(): int
    {
        $count = $this->refreshAll();
        $this->systemMetaRepository->setValue(self::META_KEY, (new \DateTime())->format('Y-m-d H:i:s'));
        return $count;
    }

    public function refreshEvent(Event $event): bool
    {
        if ($event->getStatus() === EventStatus::CANCELLED->value) {
            return false;
        }

        $newStatus = $this->computeStatus($event);

        if ($event->getStatus() === $newStatus) {
            return false;
        }

        $event->setStatus($newStatus);
        return true;
    }

    private function computeStatus(Event $event): string
    {
        $now = new \DateTime();
        $startsAt = $event->getStartsAt();

        if ($startsAt === null) {
            return EventStatus::SCHEDULED->value;
        }

        if ($now < $startsAt) {
            return EventStatus::SCHEDULED->value;
        }

        // Within the live window (startsAt to startsAt + 6 hours) or beyond it
        if ($this->allMatchesHaveResults($event)) {
            return EventStatus::COMPLETED->value;
        }

        return EventStatus::LIVE->value;
    }

    private function allMatchesHaveResults(Event $event): bool
    {
        $matches = $this->matchProposalRepository->findBy(['event' => $event]);

        if (empty($matches)) {
            return false;
        }

        foreach ($matches as $match) {
            if ($this->fightResultRepository->findOneBy(['match' => $match]) === null) {
                return false;
            }
        }

        return true;
    }
}
