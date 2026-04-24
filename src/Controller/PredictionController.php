<?php
namespace App\Controller;

use App\Entity\Prediction;
use App\Repository\FightResultRepository;
use App\Repository\FighterRepository;
use App\Repository\PredictionRepository;
use App\Repository\UserRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

class PredictionController extends AbstractController
{
    #[Route('/predictions', name: 'app_predictions')]
    public function index(FightResultRepository $resultRepo, PredictionRepository $predictionRepo): Response
    {
        $this->denyAccessUnlessGranted('ROLE_USER');
        $user = $this->getUser();
        
        $upcomingFights = $resultRepo->findBy(['status' => 'SCHEDULED'], ['fightDate' => 'ASC']);
        $myPredictions = $predictionRepo->findByUser($user->getUserId());
        
        $predictionMap = [];
        foreach ($myPredictions as $p) {
            $predictionMap[$p->getFight()->getResultId()] = $p;
        }

        return $this->render('prediction/index.html.twig', [
            'upcomingFights' => $upcomingFights,
            'predictionMap' => $predictionMap,
            'myPredictions' => $myPredictions,
        ]);
    }

    #[Route('/predictions/submit/{fightId}', name: 'app_prediction_submit', methods: ['POST'])]
    public function submit(int $fightId, Request $request, FightResultRepository $resultRepo, FighterRepository $fighterRepo, PredictionRepository $predictionRepo, EntityManagerInterface $em): Response
    {
        $this->denyAccessUnlessGranted('ROLE_USER');
        if ($this->isGranted('ROLE_ADMIN')) {
            $this->addFlash('error', 'Admins cannot submit predictions.');
            return $this->redirectToRoute('app_dashboard');
        }
        $user = $this->getUser();
        $fight = $resultRepo->find($fightId);

        if (!$fight || $fight->getStatus() !== 'SCHEDULED') {
            $this->addFlash('error', 'Predictions can only be made for upcoming fights.');
            return $this->redirectToRoute('app_predictions');
        }

        // Check if already predicted
        $existing = $predictionRepo->findOneByUserAndFight($user->getUserId(), $fightId);
        $prediction = $existing ?: new Prediction();
        
        $winnerId = $request->request->get('predictedWinnerId');
        if ($winnerId) {
            $prediction->setPredictedWinner($fighterRepo->find($winnerId));
        } else {
            $prediction->setPredictedWinner(null); // Draw
        }

        $prediction->setUser($user);
        $prediction->setFight($fight);
        $prediction->setPredictedMethod($request->request->get('method'));
        $prediction->setPredictedRound($request->request->get('round') ? (int)$request->request->get('round') : null);

        $em->persist($prediction);
        $em->flush();

        $this->addFlash('success', 'Prediction saved!');
        return $this->redirectToRoute('app_predictions');
    }

    #[Route('/leaderboard', name: 'app_leaderboard')]
    public function leaderboard(UserRepository $userRepo): Response
    {
        $topUsers = $userRepo->findFansOrderedByPoints(50);

        return $this->render('prediction/leaderboard.html.twig', [
            'users' => $topUsers,
        ]);
    }
}
