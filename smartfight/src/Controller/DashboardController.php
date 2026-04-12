<?php
namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use App\Repository\CombattantRepository;
use App\Repository\EventRepository;
use App\Repository\FightResultRepository;
use App\Repository\FighterRepository;

class DashboardController extends AbstractController
{
    #[Route('/', name: 'home')]
    public function home(): Response
    {
        return $this->redirectToRoute('app_login');
    }

    #[Route('/admin/dashboard', name: 'dashboard')]
    public function index(
        CombattantRepository $combattantRepository,
        FighterRepository $fighterRepository,
        EventRepository $eventRepository,
        FightResultRepository $fightResultRepository,
    ): Response
    {
        $totalCombattants = count($combattantRepository->findAll());
        $totalFighters = count($fighterRepository->findAll());
        $totalEvents = count($eventRepository->findAll());
        $totalResults = count($fightResultRepository->findAll());

        return $this->render('dashboard/index.html.twig', [
            'totalCombattants' => $totalCombattants,
            'totalFighters' => $totalFighters,
            'totalEvents' => $totalEvents,
            'totalResults' => $totalResults,
        ]);
    }
}