<?php

namespace App\Controller;

use App\Repository\DisciplineRepository;
use App\Repository\EventRepository;
use App\Repository\VenueRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

class HomeController extends AbstractController
{
    #[Route('/', name: 'app_home')]
    public function index(
        EventRepository $eventRepository,
        VenueRepository $venueRepository,
        DisciplineRepository $disciplineRepository
    ): Response {
        return $this->render('home/index.html.twig', [
            'upcomingEvents'   => $eventRepository->findUpcoming(6),
            'totalEvents'      => count($eventRepository->findAll()),
            'totalVenues'      => count($venueRepository->findAll()),
            'totalDisciplines' => count($disciplineRepository->findAll()),
        ]);
    }
}
