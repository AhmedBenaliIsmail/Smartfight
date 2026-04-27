<?php
namespace App\Service;

use App\Entity\Notification;
use App\Entity\User;
use App\Repository\UserRepository;
use Doctrine\ORM\EntityManagerInterface;

class NotificationService
{
    public function __construct(
        private EntityManagerInterface $em,
        private UserRepository $userRepo
    ) {}

    public function notifyUser(User $user, string $message, string $type = 'GENERAL'): void
    {
        $n = new Notification();
        $n->setUser($user);
        $n->setMessage($message);
        $n->setType($type);
        $n->setIsRead(false);
        
        $this->em->persist($n);
        $this->em->flush();
    }

    public function notifyAllFans(string $message, string $type = 'GENERAL'): void
    {
        // For simplicity, we notify all users with ROLE_USER who are not ADMIN
        $fans = $this->userRepo->findAll(); // In a real app, we'd filter for fans
        foreach ($fans as $fan) {
            // Notify all users with ROLE_USER, including admins for testing
            $roles = $fan->getRoles();
            if (in_array('ROLE_USER', $roles)) {
                $this->notifyUser($fan, $message, $type);
            }
        }
    }

    public function notifyPredictionResult(User $user, bool $success, string $fightDesc): void
    {
        $status = $success ? "Correct!" : "Incorrect.";
        $message = "Your prediction for $fightDesc was $status";
        $this->notifyUser($user, $message, 'PREDICTION');
    }

    public function notifyRankingUpdate(User $user, string $fighterName, int $newRank): void
    {
        $message = "$fighterName is now ranked #$newRank in their division.";
        $this->notifyUser($user, $message, 'RANKING');
    }

    public function notifyNewArticle(string $title): void
    {
        $message = "New article published: $title. Check it out!";
        $this->notifyAllFans($message, 'NEW_ARTICLE');
    }

    public function notifyFanFavoriteEvent(string $eventName): void
    {
        $message = "🔥 The Fan Favorite Event is here: $eventName! Get your tickets now with 10% OFF!";
        $this->notifyAllFans($message, 'EVENT_ANNOUNCEMENT');
    }
}
