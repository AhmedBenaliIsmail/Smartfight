<?php

namespace App\Controller\Admin;

use App\Repository\FighterRepository;
use App\Repository\PerformanceScoreRepository;
use App\Service\AnalyticsEngine;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/admin/performance', name: 'admin_performance_')]
class PerformanceController extends AbstractController
{
    #[Route('', name: 'index', methods: ['GET'])]
    public function index(
        Request $request,
        PerformanceScoreRepository $performanceScoreRepository,
        FighterRepository $fighterRepository,
    ): Response
    {
        $season = (string) $request->query->get('season', 'CURRENT');
        $fightersById = [];

        foreach ($fighterRepository->findAll() as $fighter) {
            $fightersById[(int) $fighter->getId()] = $fighter;
        }

        return $this->render('admin/performance/index.html.twig', [
            'scores' => $performanceScoreRepository->findLeaderboard($season),
            'fightersById' => $fightersById,
            'season' => $season,
            'active_sidebar' => 'performance',
        ]);
    }

    #[Route('/show/{fighterId}', name: 'show', methods: ['GET'])]
    public function show(
        int $fighterId,
        Request $request,
        FighterRepository $fighterRepository,
        PerformanceScoreRepository $performanceScoreRepository,
    ): Response {
        $season = (string) $request->query->get('season', 'CURRENT');
        $fighter = $fighterRepository->find($fighterId);

        if ($fighter === null) {
            throw $this->createNotFoundException('Fighter not found.');
        }

        return $this->render('admin/performance/show.html.twig', [
            'fighter' => $fighter,
            'score' => $performanceScoreRepository->findByFighterAndSeason($fighterId, $season),
            'season' => $season,
            'active_sidebar' => 'performance',
        ]);
    }

    #[Route('/recompute', name: 'recompute', methods: ['POST'])]
    public function recompute(AnalyticsEngine $analyticsEngine): Response
    {
        $analyticsEngine->recomputeAll();
        $this->addFlash('success', 'Performance scores recomputed.');

        return $this->redirectToRoute('admin_performance_index');
    }
}
