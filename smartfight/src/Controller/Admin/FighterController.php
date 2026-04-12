<?php

namespace App\Controller\Admin;

use App\Repository\FighterRepository;
use App\Service\AnalyticsEngine;
use App\Service\RankingService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/admin/fighters', name: 'admin_fighter_')]
class FighterController extends AbstractController
{
    #[Route('', name: 'index', methods: ['GET'])]
    public function index(Request $request, FighterRepository $fighterRepository): Response
    {
        $search = trim((string) $request->query->get('search', ''));
        $weightClass = $request->query->get('weightClass');

        $fighters = $search !== ''
            ? $fighterRepository->search($search, $weightClass ?: null)
            : $fighterRepository->findRankedFighters($weightClass ?: null);

        return $this->render('admin/fighter/index.html.twig', [
            'fighters' => $fighters,
            'search' => $search,
            'weightClass' => $weightClass,
            'active_sidebar' => 'fighters',
        ]);
    }

    #[Route('/recompute', name: 'recompute', methods: ['POST'])]
    public function recompute(
        Request $request,
        FighterRepository $fighterRepository,
        RankingService $rankingService,
        AnalyticsEngine $analyticsEngine,
    ): Response {
        $fighterId = $request->request->getInt('fighterId');

        if ($fighterId > 0) {
            $fighter = $fighterRepository->find($fighterId);
            if ($fighter !== null) {
                $analyticsEngine->recomputeFighter($fighter);
                $rankingService->recomputeRankings();
                $this->addFlash('success', 'Fighter analytics recomputed.');
            }
        } else {
            $analyticsEngine->recomputeAll();
            $rankingService->recomputeRankings();
            $this->addFlash('success', 'All fighter analytics recomputed.');
        }

        return $this->redirectToRoute('admin_fighter_index');
    }
}
