<?php
namespace App\Controller;

use App\Repository\FighterRepository;
use App\Repository\EventRepository;
use App\Repository\FightResultRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

class DashboardController extends AbstractController
{
    #[Route('/', name: 'app_dashboard')]
    public function index(FighterRepository $fighterRepo, EventRepository $eventRepo, FightResultRepository $resultRepo): Response
    {
        $fighters = $fighterRepo->findAllOrderedByName();
        $events = $eventRepo->findAllOrderedByDate();
        $results = $resultRepo->findAllOrdered();
        $upcomingEvents = $eventRepo->findUpcoming();

        $koCount = count(array_filter($results, fn($r) => in_array(strtoupper($r->getMethodOfVictory() ?? ''), ['KO', 'TKO', 'KO/TKO'])));

        return $this->render('dashboard/index.html.twig', [
            'totalFighters' => count($fighters),
            'totalEvents' => count($events),
            'totalFights' => count($results),
            'koCount' => $koCount,
            'fighters' => array_slice($fighters, 0, 10),
            'upcomingEvents' => array_slice($upcomingEvents, 0, 8),
            'recentResults' => array_slice($results, 0, 8),
            'fighterMap' => $this->buildFighterMap($fighterRepo),
        ]);
    }

    private function buildFighterMap(FighterRepository $repo): array
    {
        $map = [];
        foreach ($repo->findAll() as $f) {
            $map[$f->getFighterId()] = $f->getFullName();
        }
        return $map;
    }
}
