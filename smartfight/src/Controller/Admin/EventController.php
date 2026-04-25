<?php

namespace App\Controller\Admin;

use App\Entity\Event;
use App\Form\EventType;
use App\Repository\EventRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/admin/events', name: 'admin_event_')]
class EventController extends AbstractController
{
    #[Route('', name: 'index', methods: ['GET'])]
    public function index(EventRepository $eventRepository): Response
    {
        $events = $eventRepository->findBy([], ['startsAt' => 'DESC']);

        return $this->render('admin/event/index.html.twig', [
            'events' => $events,
            'active_sidebar' => 'events',
        ]);
    }

    #[Route('/new', name: 'new', methods: ['GET', 'POST'])]
    public function new(Request $request, EntityManagerInterface $entityManager): Response
    {
        $event = new Event();
        $form = $this->createForm(EventType::class, $event);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $entityManager->persist($event);
            $entityManager->flush();

            $this->addFlash('success', 'Event created successfully.');

            return $this->redirectToRoute('admin_event_index');
        }

        return $this->render('admin/event/form.html.twig', [
            'form' => $form->createView(),
            'event' => $event,
            'is_edit' => false,
            'active_sidebar' => 'events',
        ]);
    }

    #[Route('/{id}', name: 'show', methods: ['GET'])]
    public function show(Event $event): Response
    {
        return $this->render('admin/event/show.html.twig', [
            'event' => $event,
            'active_sidebar' => 'events',
        ]);
    }

    #[Route('/{id}/edit', name: 'edit', methods: ['GET', 'POST'])]
    public function edit(Request $request, Event $event, EntityManagerInterface $entityManager): Response
    {
        $form = $this->createForm(EventType::class, $event);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $entityManager->flush();

            $this->addFlash('success', 'Event updated successfully.');

            return $this->redirectToRoute('admin_event_index');
        }

        return $this->render('admin/event/form.html.twig', [
            'form' => $form->createView(),
            'event' => $event,
            'is_edit' => true,
            'active_sidebar' => 'events',
        ]);
    }

    #[Route('/{id}/delete', name: 'delete', methods: ['POST'])]
    public function delete(Request $request, Event $event, EntityManagerInterface $entityManager): Response
    {
        if ($this->isCsrfTokenValid('delete_event_' . $event->getId(), (string) $request->request->get('_token'))) {
            $entityManager->remove($event);
            $entityManager->flush();
            $this->addFlash('success', 'Event deleted.');
        }

        return $this->redirectToRoute('admin_event_index');
    }
}
