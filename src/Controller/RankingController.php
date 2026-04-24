<?php
namespace App\Controller;

use App\Service\RankingService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpFoundation\StreamedResponse;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/rankings')]
class RankingController extends AbstractController
{
    #[Route('', name: 'app_rankings')]
    public function index(Request $request, RankingService $rankingService): Response
    {
        $this->denyAccessUnlessGranted('ROLE_USER');
        $fighters = $rankingService->getRankedFighters();
        $q = strtolower(trim($request->query->get('q', '')));
        if ($q) {
            $fighters = array_filter($fighters, fn($f) => str_contains(strtolower($f->getFullName()), $q));
            $fighters = array_values($fighters);
        }

        // Group by weight division
        $groupedRankings = [];
        foreach ($fighters as $f) {
            $wd = $f->getWeightDivision() ? $f->getWeightDivision()->getName() : 'Unclassified';
            $groupedRankings[$wd][] = $f;
        }

        return $this->render('ranking/index.html.twig', [
            'groupedRankings' => $groupedRankings,
            'q' => $request->query->get('q', ''),
        ]);
    }

    #[Route('/recalculate', name: 'app_ranking_recalc', methods: ['POST'])]
    public function recalculate(RankingService $rankingService): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        $rankingService->recomputeAllRankings();
        $this->addFlash('success', 'Global boxing rankings recalculated from match results!');
        return $this->redirectToRoute('app_rankings');
    }

    #[Route('/export', name: 'app_ranking_export')]
    public function export(RankingService $rankingService): StreamedResponse
    {
        $this->denyAccessUnlessGranted('ROLE_USER');
        $fighters = $rankingService->getRankedFighters();
        $response = new StreamedResponse(function() use ($fighters) {
            $out = fopen('php://output', 'w');
            fputcsv($out, ['Rank','Boxer','Division','ELO','Performance','Streak','SOS','Wins','Losses']);
            foreach ($fighters as $i => $f) {
                fputcsv($out, [
                    $i + 1, $f->getFullName(), 
                    $f->getWeightDivision() ? $f->getWeightDivision()->getName() : 'N/A',
                    (int)$f->getEloRating(), (int)$f->getPerformanceScore(),
                    $f->getWinStreak(), (int)$f->getStrengthOfSchedule(),
                    $f->getWins(), $f->getLosses(),
                ]);
            }
            fclose($out);
        });
        $response->headers->set('Content-Type', 'text/csv');
        $response->headers->set('Content-Disposition', 'attachment; filename="boxing_rankings.csv"');
        return $response;
    }
}

