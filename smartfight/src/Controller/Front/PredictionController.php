<?php

namespace App\Controller\Front;

use App\Entity\FanPrediction;
use App\Repository\EventRepository;
use App\Repository\FanPredictionRepository;
use App\Repository\FighterRepository;
use App\Repository\MatchProposalRepository;
use App\Service\LeaderboardService;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

class PredictionController extends AbstractController
{
    #[Route('/predictions', name: 'front_prediction_index', methods: ['GET'])]
    public function index(
        FanPredictionRepository $predRepo,
        EventRepository $eventRepo,
        LeaderboardService $leaderboardService,
    ): Response {
        $user = $this->getUser();

        $myPredictions = $user
            ? $predRepo->findByFanQueryBuilder($user->getId())->getQuery()->getResult()
            : [];

        return $this->render('front/prediction/index.html.twig', [
            'leaderboard' => $leaderboardService->getRankings('2026', 10),
            'myPredictions' => $myPredictions,
            'events' => $eventRepo->findBy(['status' => 'SCHEDULED'], ['startDate' => 'ASC']),
        ]);
    }

    #[Route('/predictions/fights/{eventId}', name: 'front_prediction_fights', methods: ['GET'])]
    public function getFightsForEvent(int $eventId, MatchProposalRepository $mpRepo): JsonResponse
    {
        $matches = $mpRepo->findBy(['event' => $eventId, 'status' => 'ACCEPTED']);

        $data = [];
        foreach ($matches as $mp) {
            $data[] = [
                'id' => $mp->getId(),
                'label' => $mp->getFightLabel(),
                'fighter1' => [
                    'id' => $mp->getFighter1()->getId(),
                    'name' => $mp->getFighter1()->getDisplayName(),
                    'record' => $mp->getFighter1()->getRecord(),
                ],
                'fighter2' => [
                    'id' => $mp->getFighter2()->getId(),
                    'name' => $mp->getFighter2()->getDisplayName(),
                    'record' => $mp->getFighter2()->getRecord(),
                ],
            ];
        }

        return $this->json($data);
    }

    #[Route('/predictions/submit', name: 'front_prediction_submit', methods: ['POST'])]
    public function submit(
        Request $request,
        EntityManagerInterface $em,
        MatchProposalRepository $mpRepo,
        FighterRepository $fighterRepo,
        FanPredictionRepository $predRepo,
    ): Response {
        $this->denyAccessUnlessGranted('ROLE_FAN');
        $user = $this->getUser();

        $predictions = $request->request->all('predictions');

        $count = 0;
        foreach ($predictions as $pred) {
            $mpId = (int) ($pred['match_proposal_id'] ?? 0);
            $winnerId = (int) ($pred['predicted_winner_id'] ?? 0);
            $method = $pred['predicted_method'] ?? '';

            if (!$mpId || !$winnerId || !$method) continue;

            $existing = $predRepo->findOneBy(['matchProposal' => $mpId, 'fan' => $user->getId()]);
            if ($existing) continue;

            $mp = $mpRepo->find($mpId);
            $fighter = $fighterRepo->find($winnerId);
            if (!$mp || !$fighter) continue;

            $prediction = new FanPrediction();
            $prediction->setMatchProposal($mp);
            $prediction->setFan($user);
            $prediction->setPredictedWinner($fighter);
            $prediction->setPredictedMethod($method);

            $em->persist($prediction);
            $count++;
        }

        $em->flush();

        $this->addFlash('success', "{$count} prediction(s) submitted! Picks lock 30 minutes before the event.");
        return $this->redirectToRoute('front_prediction_index');
    }
}
