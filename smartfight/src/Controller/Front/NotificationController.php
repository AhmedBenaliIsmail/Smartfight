<?php

namespace App\Controller\Front;

use App\Repository\FanNotificationRepository;
use Knp\Component\Pager\PaginatorInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

class NotificationController extends AbstractController
{
    #[Route('/notifications', name: 'front_notification_index', methods: ['GET'])]
    public function index(
        Request $request,
        FanNotificationRepository $repo,
        PaginatorInterface $paginator,
    ): Response {
        $user = $this->getUser();
        if (!$user) {
            return $this->redirectToRoute('app_login');
        }

        $type = $request->query->get('type');
        $unreadOnly = $request->query->getBoolean('unread');

        $qb = $repo->findByFanQueryBuilder($user->getId(), $type, $unreadOnly);
        $pagination = $paginator->paginate($qb, $request->query->getInt('page', 1), 20);

        $unreadCount = $repo->countUnreadForFan($user->getId());

        return $this->render('front/notification/index.html.twig', [
            'pagination' => $pagination,
            'unreadCount' => $unreadCount,
            'activeTab' => $unreadOnly ? 'unread' : ($type ?? 'all'),
        ]);
    }

    #[Route('/notifications/mark-all-read', name: 'front_notification_mark_all_read', methods: ['POST'])]
    public function markAllRead(FanNotificationRepository $repo): Response
    {
        $this->denyAccessUnlessGranted('ROLE_FAN');
        $user = $this->getUser();

        $repo->markAllReadForFan($user->getId());

        $this->addFlash('success', 'All notifications marked as read.');
        return $this->redirectToRoute('front_notification_index');
    }
}
