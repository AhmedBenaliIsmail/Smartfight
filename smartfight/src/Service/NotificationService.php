<?php

namespace App\Service;

use App\Entity\Event;
use App\Entity\FanNotification;
use App\Entity\User;
use App\Repository\FanNotificationRepository;
use App\Repository\UserRepository;
use Doctrine\ORM\EntityManagerInterface;

class NotificationService
{
    public function __construct(
        private EntityManagerInterface $em,
        private FanNotificationRepository $notifRepo,
        private UserRepository $userRepo,
    ) {}

    public function sendToFan(User $fan, string $type, string $title, string $message, ?int $eventId = null): void
    {
        $notification = new FanNotification();
        $notification->setFan($fan);
        $notification->setType($type);
        $notification->setTitle($title);
        $notification->setMessage($message);

        if ($eventId) {
            /** @var Event $event */
            $event = $this->em->getReference(Event::class, $eventId);
            $notification->setRelatedEvent($event);
        }

        $this->em->persist($notification);
        $this->em->flush();
    }

    public function broadcast(string $audience, string $type, string $title, string $message): int
    {
        $fans = $this->userRepo->findFans();

        $count = 0;
        foreach ($fans as $fan) {
            $notification = new FanNotification();
            $notification->setFan($fan);
            $notification->setType($type);
            $notification->setTitle($title);
            $notification->setMessage($message);
            $this->em->persist($notification);
            $count++;

            if ($count % 50 === 0) {
                $this->em->flush();
                $this->em->clear(FanNotification::class);
            }
        }

        $this->em->flush();
        return $count;
    }

    public function getUnreadCount(int $fanId): int
    {
        return $this->notifRepo->countUnreadForFan($fanId);
    }
}
