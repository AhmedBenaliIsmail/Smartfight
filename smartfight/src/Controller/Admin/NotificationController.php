<?php

namespace App\Controller\Admin;

use App\Form\BroadcastType;
use App\Repository\FanNotificationRepository;
use App\Service\NotificationService;
use Knp\Component\Pager\PaginatorInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/admin/notifications', name: 'admin_notification_')]
class NotificationController extends AbstractController
{
    #[Route('', name: 'index', methods: ['GET'])]
    public function index(
        Request $request,
        FanNotificationRepository $repo,
        PaginatorInterface $paginator,
    ): Response {
        $type = $request->query->get('type');
        $qb = $repo->findAllForAdminQueryBuilder($type);
        $pagination = $paginator->paginate($qb, $request->query->getInt('page', 1), 15);
        $stats = $repo->getAdminStats();

        return $this->render('admin/notification/index.html.twig', [
            'pagination' => $pagination,
            'stats' => $stats,
            'activeTab' => $type ?? 'all',
            'active_sidebar' => 'notifications',
        ]);
    }

    #[Route('/broadcast', name: 'broadcast', methods: ['GET', 'POST'])]
    public function broadcast(Request $request, NotificationService $notifService): Response
    {
        $form = $this->createForm(BroadcastType::class);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $data = $form->getData();
            $count = $notifService->broadcast(
                $data['audience'],
                $data['type'],
                $data['title'],
                $data['message'],
            );

            $this->addFlash('success', "Broadcast sent to {$count} fans.");
            return $this->redirectToRoute('admin_notification_index');
        }

        return $this->render('admin/notification/broadcast.html.twig', [
            'form' => $form->createView(),
            'active_sidebar' => 'notifications',
        ]);
    }
}
