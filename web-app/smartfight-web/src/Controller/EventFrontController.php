<?php

namespace App\Controller;

use App\Entity\Event;
use App\Repository\EventRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/events', name: 'app_front_event_')]
class EventFrontController extends AbstractController
{
    #[Route('', name: 'index', methods: ['GET'])]
    public function index(Request $request, EventRepository $eventRepository): Response
    {
        $status = $request->query->get('status');

        if ($status) {
            $events = $eventRepository->createQueryBuilder('e')
                ->where('e.visibility = :pub')
                ->andWhere('e.status = :status')
                ->setParameter('pub', 'PUBLIC')
                ->setParameter('status', $status)
                ->orderBy('e.startDate', 'ASC')
                ->getQuery()
                ->getResult();
        } else {
            $events = $eventRepository->createQueryBuilder('e')
                ->where('e.visibility = :pub')
                ->setParameter('pub', 'PUBLIC')
                ->orderBy('e.startDate', 'ASC')
                ->getQuery()
                ->getResult();
        }

        return $this->render('front/event/index.html.twig', [
            'events' => $events,
            'status' => $status,
        ]);
    }

    #[Route('/{id}', name: 'show', methods: ['GET'], requirements: ['id' => '\d+'])]
    public function show(Event $event, EventRepository $eventRepository): Response
    {
        $registeredCount = $eventRepository->countRegisteredFighters($event->getId());

        return $this->render('front/event/show.html.twig', [
            'event'           => $event,
            'registeredCount' => $registeredCount,
        ]);
    }
}
