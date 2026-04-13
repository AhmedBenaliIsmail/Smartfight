<?php

namespace App\Controller;

use App\Entity\Venue;
use App\Form\VenueType;
use App\Repository\VenueRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/admin/venues', name: 'app_admin_venue_')]
class VenueController extends AbstractController
{
    #[Route('', name: 'index', methods: ['GET'])]
    public function index(VenueRepository $venueRepository): Response
    {
        return $this->render('admin/venue/index.html.twig', [
            'venues' => $venueRepository->findAll(),
        ]);
    }

    #[Route('/new', name: 'new', methods: ['GET', 'POST'])]
    public function new(Request $request, EntityManagerInterface $em): Response
    {
        $venue = new Venue();
        $form  = $this->createForm(VenueType::class, $venue);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $em->persist($venue);
            $em->flush();
            $this->addFlash('success', 'Venue created successfully!');
            return $this->redirectToRoute('app_admin_venue_index');
        }

        return $this->render('admin/venue/new.html.twig', [
            'form' => $form->createView(),
        ]);
    }

    #[Route('/{id}', name: 'show', methods: ['GET'], requirements: ['id' => '\d+'])]
    public function show(Venue $venue): Response
    {
        return $this->render('admin/venue/show.html.twig', [
            'venue' => $venue,
        ]);
    }

    #[Route('/{id}/edit', name: 'edit', methods: ['GET', 'POST'], requirements: ['id' => '\d+'])]
    public function edit(Request $request, Venue $venue, EntityManagerInterface $em): Response
    {
        $form = $this->createForm(VenueType::class, $venue);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $em->flush();
            $this->addFlash('success', 'Venue modified successfully!');
            return $this->redirectToRoute('app_admin_venue_show', ['id' => $venue->getId()]);
        }

        return $this->render('admin/venue/edit.html.twig', [
            'form'  => $form->createView(),
            'venue' => $venue,
        ]);
    }

    #[Route('/{id}', name: 'delete', methods: ['POST'], requirements: ['id' => '\d+'])]
    public function delete(Request $request, Venue $venue, EntityManagerInterface $em): Response
    {
        $token = $request->request->get('_token');
        if ($this->isCsrfTokenValid('delete' . $venue->getId(), $token)) {
            $em->remove($venue);
            $em->flush();
            $this->addFlash('success', 'Venue deleted.');
        }

        return $this->redirectToRoute('app_admin_venue_index');
    }
}
