<?php

namespace App\Controller;

use App\Entity\Event;
use App\Form\EventType;
use App\Repository\EventRepository;
use App\Repository\UserRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/admin/events', name: 'app_admin_event_')]
class EventController extends AbstractController
{
    // ── ADMIN ROUTES ─────────────────────────────────────────────────────

    #[Route('', name: 'index', methods: ['GET'])]
    public function index(Request $request, EventRepository $eventRepository): Response
    {
        $q = $request->query->get('q');
        if ($q) {
            $events = $eventRepository->searchByName($q);
        } else {
            $events = $eventRepository->findBy([], ['startDate' => 'DESC']);
        }

        return $this->render('admin/event/index.html.twig', [
            'events' => $events,
            'q'      => $q,
        ]);
    }

    #[Route('/new', name: 'new', methods: ['GET', 'POST'])]
    public function new(Request $request, EntityManagerInterface $em, UserRepository $userRepository): Response
    {
        $event = new Event();
        $form  = $this->createForm(EventType::class, $event);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $event->setCreatedAt(new \DateTime());
            $event->setUpdatedAt(new \DateTime());

            // Use the first user from DB as organizer (session-based auth will refine this)
            $organizer = $userRepository->find(1);
            if ($organizer) {
                $event->setOrganizer($organizer);
            }

            $em->persist($event);
            $em->flush();

            $this->addFlash('success', 'Event created successfully!');
            return $this->redirectToRoute('app_admin_event_index');
        }

        return $this->render('admin/event/new.html.twig', [
            'form' => $form->createView(),
        ]);
    }

    #[Route('/{id}', name: 'show', methods: ['GET'], requirements: ['id' => '\d+'])]
    public function show(Event $event, EventRepository $eventRepository): Response
    {
        $registeredCount = $eventRepository->countRegisteredFighters($event->getId());

        return $this->render('admin/event/show.html.twig', [
            'event'           => $event,
            'registeredCount' => $registeredCount,
        ]);
    }

    #[Route('/{id}/edit', name: 'edit', methods: ['GET', 'POST'], requirements: ['id' => '\d+'])]
    public function edit(Request $request, Event $event, EntityManagerInterface $em): Response
    {
        $form = $this->createForm(EventType::class, $event);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $event->setUpdatedAt(new \DateTime());
            $em->flush();

            $this->addFlash('success', 'Event modified successfully!');
            return $this->redirectToRoute('app_admin_event_show', ['id' => $event->getId()]);
        }

        return $this->render('admin/event/edit.html.twig', [
            'form'  => $form->createView(),
            'event' => $event,
        ]);
    }

    #[Route('/{id}', name: 'delete', methods: ['POST'], requirements: ['id' => '\d+'])]
    public function delete(Request $request, Event $event, EntityManagerInterface $em): Response
    {
        $token = $request->request->get('_token');
        if ($this->isCsrfTokenValid('delete' . $event->getId(), $token)) {
            $em->remove($event);
            $em->flush();
            $this->addFlash('success', 'Event deleted.');
        }

        return $this->redirectToRoute('app_admin_event_index');
    }
}
