<?php
namespace App\Controller;

use App\Service\RankingService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/performance')]
class PerformanceController extends AbstractController
{
    #[Route('', name: 'app_performance')]
    public function index(Request $request, RankingService $rankingService, \App\Repository\FighterRepository $fighterRepo): Response
    {
        $this->denyAccessUnlessGranted('ROLE_USER');
        $fighters = $fighterRepo->findAll();
        $sort = $request->query->get('sort', 'performance_desc');

        usort($fighters, function($a, $b) use ($sort) {
            return match($sort) {
                'performance_asc' => $a->getPerformanceScore() <=> $b->getPerformanceScore(),
                'elo_desc' => $b->getEloRating() <=> $a->getEloRating(),
                'streak_desc' => $b->getWinStreak() <=> $a->getWinStreak(),
                'sos_desc' => $b->getStrengthOfSchedule() <=> $a->getStrengthOfSchedule(),
                default => $b->getPerformanceScore() <=> $a->getPerformanceScore(),
            };
        });

        $q = strtolower(trim($request->query->get('q', '')));
        if ($q) {
            $fighters = array_filter($fighters, fn($f) => str_contains(strtolower($f->getFullName()), $q));
        }

        return $this->render('performance/index.html.twig', [
            'fighters' => $fighters,
            'sort' => $sort,
            'q' => $request->query->get('q', ''),
        ]);
    }

    #[Route('/recalculate', name: 'app_performance_recalc', methods: ['POST'])]
    public function recalculate(RankingService $rankingService): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        $rankingService->recomputeAllRankings();
        $this->addFlash('success', 'All performance scores & ELO ratings recalculated!');
        return $this->redirectToRoute('app_performance');
    }

    #[Route('/{id}', name: 'app_performance_show')]
    public function show(int $id, RankingService $rankingService, \App\Repository\FighterRepository $fighterRepo, \App\Repository\FightResultRepository $resultRepo, \App\Repository\FightStatisticRepository $statRepo, \App\Repository\EventRepository $eventRepo): Response
    {
        $this->denyAccessUnlessGranted('ROLE_USER');
        $fighter = $fighterRepo->find($id);
        if (!$fighter) throw $this->createNotFoundException();

        $fights = $resultRepo->findCompletedByFighter($id);
        $fightData = [];
        foreach ($fights as $fight) {
            $stats = $statRepo->findByFighterAndFightResult($id, $fight->getResultId());
            $event = $fight->getEvent();
            $fightData[] = [
                'fight' => $fight,
                'stats' => $stats,
                'event' => $event,
            ];
        }

        return $this->render('performance/show.html.twig', [
            'fighter' => $fighter,
            'fightData' => $fightData,
        ]);
    }
}
