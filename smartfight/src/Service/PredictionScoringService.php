<?php

namespace App\Service;

use App\Entity\FanPrediction;
use App\Entity\FightResult;
use App\Repository\FanPredictionRepository;
use Doctrine\ORM\EntityManagerInterface;

class PredictionScoringService
{
    public function __construct(
        private EntityManagerInterface $em,
        private FanPredictionRepository $predictionRepo,
        private NotificationService $notificationService,
    ) {}

    public function scoreFight(FightResult $result, int $basePoints = 1, int $methodBonus = 2): array
    {
        $matchProposal = $result->getMatch();
        if (!$matchProposal) {
            return ['correct' => 0, 'wrong' => 0, 'totalPoints' => 0];
        }

        $predictions = $this->predictionRepo->findByMatchProposal($matchProposal->getId());
        $correct = 0;
        $wrong = 0;
        $totalPoints = 0;

        foreach ($predictions as $prediction) {
            /** @var FanPrediction $prediction */
            if ($prediction->isScored()) {
                continue;
            }

            $points = 0;
            $winnerId = $result->getWinner()?->getId();
            $predictedWinnerId = $prediction->getPredictedWinner()?->getId();

            if ($winnerId && $predictedWinnerId === $winnerId) {
                $points = $basePoints;
                if (strtoupper($prediction->getPredictedMethod()) === strtoupper($result->getMethod())) {
                    $points += $methodBonus;
                }
                $correct++;
            } else {
                $wrong++;
            }

            $prediction->setPointsEarned($points);
            $prediction->setIsScored(true);
            $totalPoints += $points;

            $pointsText = $points > 0 ? "+{$points}" : '0';
            $this->notificationService->sendToFan(
                $prediction->getFan(),
                'PREDICTION_SCORED',
                'Prediction result',
                "Your prediction for {$result->getFightLabel()} earned you {$pointsText} point(s)!"
            );
        }

        $this->em->flush();

        return ['correct' => $correct, 'wrong' => $wrong, 'totalPoints' => $totalPoints];
    }
}
