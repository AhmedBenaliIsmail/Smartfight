<?php
namespace App\Controller;

use App\Service\RankingService;
use App\Service\AIService;
use App\Service\PdfService;
use App\Repository\FighterRepository;
use App\Repository\FightResultRepository;
use App\Repository\FightStatisticRepository;
use App\Repository\EventRepository;
use App\Repository\RankingRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/performance')]
class PerformanceController extends AbstractController
{
    #[Route('', name: 'app_performance')]
    public function index(Request $request, RankingService $rankingService, FighterRepository $fighterRepo): Response
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

    #[Route('/injury-predictions', name: 'app_injury_predictions')]
    public function injuryPredictions(FighterRepository $fighterRepo, AIService $aiService): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        $fighters = $fighterRepo->findAll();

        $predictions = [];
        foreach ($fighters as $fighter) {
            $fighterData = [
                'name' => $fighter->getFullName(),
                'age' => $fighter->getAge() ?? 28,
                'total_fights' => $fighter->getTotalFights(),
                'ko_losses' => $fighter->getKoLosses(),
                'wins' => $fighter->getWins(),
                'losses' => $fighter->getLosses(),
                'style' => $fighter->getCalculatedFightingStyle(),
                'elo' => $fighter->getEloRating(),
            ];

            $result = $aiService->predictInjuryRisk($fighterData);
            $data = $result['data'] ?? [];

            $predictions[] = [
                'fighter' => $fighter,
                'risk_percentage' => $data['risk_percentage'] ?? 0,
                'risk_level' => $data['risk_level'] ?? 'Low',
                'vulnerable_zone' => $data['vulnerable_zone'] ?? 'N/A',
                'mitigation_strategy' => $data['mitigation_strategy'] ?? 'Standard maintenance',
                'days_to_alert' => $data['days_to_alert'] ?? 30,
                'accuracy' => $data['accuracy'] ?? 92,
                'roi' => $data['roi'] ?? 300,
                'detailed_analysis' => $data['detailed_analysis'] ?? '',
            ];
        }

        // Sort by risk_percentage descending — top 3 most at risk
        usort($predictions, fn($a, $b) => $b['risk_percentage'] <=> $a['risk_percentage']);
        $top3 = array_slice($predictions, 0, 3);

        return $this->render('performance/injury_predictions.html.twig', [
            'predictions' => $top3,
            'allPredictions' => $predictions,
        ]);
    }

    #[Route('/injury-pdf/{id}', name: 'app_injury_pdf')]
    public function injuryPdf(int $id, FighterRepository $fighterRepo, AIService $aiService, PdfService $pdfService): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        $fighter = $fighterRepo->find($id);
        if (!$fighter) throw $this->createNotFoundException();

        $fighterData = [
            'name' => $fighter->getFullName(),
            'age' => $fighter->getAge() ?? 28,
            'total_fights' => $fighter->getTotalFights(),
            'ko_losses' => $fighter->getKoLosses(),
            'wins' => $fighter->getWins(),
            'losses' => $fighter->getLosses(),
            'style' => $fighter->getCalculatedFightingStyle(),
            'elo' => $fighter->getEloRating(),
        ];

        $result = $aiService->predictInjuryRisk($fighterData);
        $data = $result['data'] ?? [];

        $html = $this->renderView('performance/injury_pdf.html.twig', [
            'fighter' => $fighter,
            'prediction' => $data,
        ]);

        $pdfContent = $pdfService->generateBinaryPdf($html);

        return new Response($pdfContent, 200, [
            'Content-Type' => 'application/pdf',
            'Content-Disposition' => 'inline; filename="injury_report_' . $fighter->getLastName() . '.pdf"',
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
    public function show(int $id, RankingService $rankingService, FighterRepository $fighterRepo, FightResultRepository $resultRepo, FightStatisticRepository $statRepo, EventRepository $eventRepo): Response
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
