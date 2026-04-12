<?php

namespace App\Controller\Front;

use App\Repository\FanNotificationRepository;
use Knp\Component\Pager\PaginatorInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
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
    public function markAllRead(Request $request, FanNotificationRepository $repo): Response
    {
        $this->denyAccessUnlessGranted('ROLE_FAN');

        if (!$this->isCsrfTokenValid('mark_all_read', (string) $request->request->get('_token'))) {
            $this->addFlash('error', 'Invalid security token. Please try again.');
            return $this->redirectToRoute('front_notification_index');
        }

        $user = $this->getUser();

        $repo->markAllReadForFan($user->getId());

        $this->addFlash('success', 'All notifications marked as read.');
        return $this->redirectToRoute('front_notification_index');
    }

    #[Route('/notifications/{id}/mark-read', name: 'front_notification_mark_read', methods: ['POST'])]
    public function markRead(int $id, Request $request, FanNotificationRepository $repo): Response
    {
        $this->denyAccessUnlessGranted('ROLE_FAN');

        if (!$this->isCsrfTokenValid('mark_read_' . $id, (string) $request->request->get('_token'))) {
            $this->addFlash('error', 'Invalid security token. Please try again.');
            return $this->redirectToRoute('front_notification_index');
        }

        $user = $this->getUser();
        $repo->markReadForFan($id, $user->getId());

        return $this->redirectToRoute('front_notification_index');
    }

    #[Route('/notifications/snapshot', name: 'front_notification_snapshot', methods: ['GET'])]
    public function snapshot(FanNotificationRepository $repo): JsonResponse
    {
        $this->denyAccessUnlessGranted('ROLE_FAN');
        $user = $this->getUser();

        $unreadCount = $repo->countUnreadForFan($user->getId());
        $latest = $repo->findLatestForFan($user->getId());

        return $this->json([
            'unreadCount' => $unreadCount,
            'latest' => $latest ? [
                'id' => $latest->getId(),
                'title' => $latest->getTitle(),
                'type' => $latest->getType(),
                'createdAt' => $latest->getCreatedAt()->format(\DateTimeInterface::ATOM),
            ] : null,
        ]);
    }
}
