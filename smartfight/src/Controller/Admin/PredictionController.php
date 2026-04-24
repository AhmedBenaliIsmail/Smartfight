<?php

namespace App\Controller\Admin;

use App\Repository\EventRepository;
use App\Repository\FanPredictionRepository;
use App\Repository\FightResultRepository;
use App\Service\PredictionScoringService;
use Knp\Component\Pager\PaginatorInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/admin/predictions', name: 'admin_prediction_')]
class PredictionController extends AbstractController
{
    #[Route('', name: 'index', methods: ['GET'])]
    public function index(
        Request $request,
        FanPredictionRepository $repo,
        EventRepository $eventRepo,
        PaginatorInterface $paginator,
    ): Response {
        $qb = $repo->findFilteredForAdmin(
            $request->query->get('search'),
            $request->query->getInt('event') ?: null,
            $request->query->get('result'),
            $request->query->get('method'),
        );

        $pagination = $paginator->paginate($qb, $request->query->getInt('page', 1), 15);
        $stats = $repo->getStats();

        return $this->render('admin/prediction/index.html.twig', [
            'pagination' => $pagination,
            'stats' => $stats,
            'events' => $eventRepo->findAll(),
            'filters' => $request->query->all(),
            'active_sidebar' => 'predictions',
        ]);
    }

    #[Route('/score', name: 'score', methods: ['GET'])]
    public function scoreForm(
        EventRepository $eventRepo,
        FightResultRepository $resultRepo,
    ): Response {
        return $this->render('admin/prediction/score.html.twig', [
            'events' => $eventRepo->findAll(),
            'fightResults' => $resultRepo->findAll(),
            'active_sidebar' => 'predictions',
        ]);
    }

    #[Route('/score/load', name: 'score_load', methods: ['POST'])]
    public function loadPredictions(Request $request, FanPredictionRepository $repo): JsonResponse
    {
        $matchProposalId = $request->request->getInt('match_proposal_id');
        $predictions = $repo->findByMatchProposal($matchProposalId);

        $data = [];
        foreach ($predictions as $p) {
            $data[] = [
                'id' => $p->getId(),
                'fan_name' => $p->getFan()->getFullName(),
                'predicted_winner' => $p->getPredictedWinner()->getDisplayName(),
                'predicted_method' => $p->getPredictedMethod(),
                'is_scored' => $p->isScored(),
                'points_earned' => $p->getPointsEarned(),
            ];
        }

        return $this->json($data);
    }

    #[Route('/score/confirm', name: 'score_confirm', methods: ['POST'])]
    public function confirmScores(
        Request $request,
        FightResultRepository $resultRepo,
        PredictionScoringService $scoringService,
    ): Response {
        $fightResultId = $request->request->getInt('fight_result_id');
        $fightResult = $resultRepo->find($fightResultId);

        if (!$fightResult) {
            $this->addFlash('error', 'Fight result not found.');
            return $this->redirectToRoute('admin_prediction_score');
        }

        $summary = $scoringService->scoreFight($fightResult);

        $this->addFlash('success', sprintf(
            'Scores published! %d correct, %d wrong, %d total points distributed.',
            $summary['correct'],
            $summary['wrong'],
            $summary['totalPoints'],
        ));

        return $this->redirectToRoute('admin_prediction_index');
    }
}
