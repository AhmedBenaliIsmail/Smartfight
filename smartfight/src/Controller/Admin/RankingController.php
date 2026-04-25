<?php

namespace App\Controller\Admin;

use App\Repository\FighterRepository;
use App\Repository\RankingRepository;
use App\Service\RankingService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpFoundation\StreamedResponse;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/admin/rankings', name: 'admin_ranking_')]
class RankingController extends AbstractController
{
    #[Route('', name: 'index', methods: ['GET'])]
    public function index(
        Request $request,
        RankingRepository $rankingRepository,
        FighterRepository $fighterRepository,
    ): Response
    {
        $season = (string) $request->query->get('season', 'CURRENT');
        $rankings = $rankingRepository->findGroupedByWeightClass($season);
        $fightersById = [];

        foreach ($fighterRepository->findAll() as $fighter) {
            $fightersById[(int) $fighter->getId()] = $fighter;
        }

        return $this->render('admin/ranking/index.html.twig', [
            'rankings' => $rankings,
            'fightersById' => $fightersById,
            'season' => $season,
            'active_sidebar' => 'rankings',
        ]);
    }

    #[Route('/recompute', name: 'recompute', methods: ['POST'])]
    public function recompute(RankingService $rankingService): Response
    {
        $rankingService->recomputeRankings();
        $this->addFlash('success', 'Rankings recomputed.');

        return $this->redirectToRoute('admin_ranking_index');
    }

    #[Route('/export.csv', name: 'export_csv', methods: ['GET'])]
    public function exportCsv(
        Request $request,
        RankingRepository $rankingRepository,
        FighterRepository $fighterRepository,
    ): StreamedResponse
    {
        $season = (string) $request->query->get('season', 'CURRENT');
        $rows = $rankingRepository->findGroupedByWeightClass($season);
        $fightersById = [];

        foreach ($fighterRepository->findAll() as $fighter) {
            $fightersById[(int) $fighter->getId()] = $fighter;
        }

        $response = new StreamedResponse(function () use ($rows, $fightersById): void {
            $handle = fopen('php://output', 'w');
            fputcsv($handle, ['fighter_id', 'fighter_name', 'weight_class', 'rank_position', 'points', 'season']);

            foreach ($rows as $row) {
                $fighter = $fightersById[$row->getFighterId()] ?? null;
                fputcsv($handle, [
                    $row->getFighterId(),
                    $fighter?->getDisplayName(),
                    $row->getWeightClassEntity()?->getName() ?? $row->getWeightClass(),
                    $row->getRankPosition(),
                    $row->getPoints(),
                    $row->getSeason(),
                ]);
            }

            fclose($handle);
        });

        $response->headers->set('Content-Type', 'text/csv; charset=UTF-8');
        $response->headers->set('Content-Disposition', 'attachment; filename="rankings.csv"');

        return $response;
    }
}
