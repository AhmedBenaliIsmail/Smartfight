<?php
namespace App\Controller;

use App\Repository\NotificationRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

class NotificationController extends AbstractController
{
    public function list(NotificationRepository $repo): Response
    {
        $user = $this->getUser();
        if (!$user) return new Response('');
        
        $unread = $repo->findUnreadByUser($user->getUserId());
        return $this->render('notification/_list.html.twig', [
            'notifications' => $unread
        ]);
    }

    #[Route('/notifications/read-all', name: 'app_notifications_read_all', methods: ['POST'])]
    public function readAll(NotificationRepository $repo, EntityManagerInterface $em): Response
    {
        $user = $this->getUser();
        if ($user) {
            $unread = $repo->findUnreadByUser($user->getUserId());
            foreach ($unread as $n) {
                $n->setIsRead(true);
            }
            $em->flush();
        }
        return $this->json(['success' => true]);
    }
}
