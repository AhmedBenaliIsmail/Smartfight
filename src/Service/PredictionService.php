<?php
namespace App\Service;

use App\Entity\Fighter;
use App\Entity\FightResult;
use App\Entity\Prediction;
use App\Entity\User;
use App\Repository\PredictionRepository;
use Doctrine\ORM\EntityManagerInterface;

class PredictionService
{
    public function __construct(
        private EntityManagerInterface $em,
        private PredictionRepository $predictionRepo,
        private NotificationService $notificationService
    ) {}

    public function processPredictionsForFight(FightResult $fight): void
    {
        if ($fight->getStatus() !== 'COMPLETED') return;

        // 1. Capture old ranks for all users who have a prediction for this fight
        $predictions = $this->predictionRepo->findPendingByFight($fight->getResultId());
        $usersToNotify = [];
        foreach ($predictions as $p) {
            $u = $p->getUser();
            if ($u) {
                $usersToNotify[$u->getUserId()] = [
                    'user' => $u,
                    'oldRank' => $this->getUserRank($u),
                    'prediction' => $p
                ];
            }
        }

        // 2. Process points
        foreach ($predictions as $p) {
            $points = $this->calculatePoints($p, $fight);
            $p->setPointsAwarded($points);
            $p->setIsProcessed(true);

            $user = $p->getUser();
            if ($user) {
                $user->setPredictionPoints($user->getPredictionPoints() + $points);
                $this->em->persist($user);
                
                if ($points > 0) {
                    $this->notificationService->notifyUser($user, "🎯 Awesome! You earned {$points} pts for your prediction on {$fight->getFighter1()->getLastName()} vs {$fight->getFighter2()->getLastName()}!", 'PREDICTION');
                } else {
                    $this->notificationService->notifyUser($user, "❌ Tough luck! You earned 0 pts for your prediction on {$fight->getFighter1()->getLastName()} vs {$fight->getFighter2()->getLastName()}.", 'PREDICTION');
                }
            }
            $this->em->persist($p);
        }
        $this->em->flush();

        // 3. Check rank changes
        foreach ($usersToNotify as $data) {
            $user = $data['user'];
            $newRank = $this->getUserRank($user);
            $oldRank = $data['oldRank'];

            if ($newRank < $oldRank) {
                $this->notificationService->notifyUser($user, "📈 Great job! Your fantasy rank moved up from #{$oldRank} to #{$newRank}!", 'RANKING');
            } elseif ($newRank > $oldRank) {
                $this->notificationService->notifyUser($user, "📉 Watch out! Your fantasy rank dropped from #{$oldRank} to #{$newRank}.", 'RANKING');
            }
        }
        $this->em->flush();
    }

    private function getUserRank(User $user): int
    {
        // Simple DQL to count users with more points
        return (int) $this->em->createQuery('SELECT COUNT(u.userId) FROM App\Entity\User u 
            LEFT JOIN u.roles r 
            WHERE (r.roleName != :adminRole OR r.roleName IS NULL) 
            AND u.predictionPoints > :pts')
            ->setParameter('adminRole', 'ADMIN')
            ->setParameter('pts', $user->getPredictionPoints())
            ->getSingleScalarResult() + 1;
    }



    public function calculateWinProbability(Fighter $f1, Fighter $f2): array
    {
        $t1 = max($f1->getTotalFights(), 1);
        $t2 = max($f2->getTotalFights(), 1);

        // Normalize ELO to [0,1] over the practical range [800, 2500]
        $eloNorm1 = (max(800.0, min(2500.0, $f1->getEloRating())) - 800) / 1700;
        $eloNorm2 = (max(800.0, min(2500.0, $f2->getEloRating())) - 800) / 1700;

        $winRate1 = $f1->getWins() / $t1;
        $winRate2 = $f2->getWins() / $t2;

        $koRate1 = $f1->getKoWins() / $t1;
        $koRate2 = $f2->getKoWins() / $t2;

        $sa1 = $f1->getStrikesThrown() > 0
            ? min($f1->getStrikesLanded() / $f1->getStrikesThrown(), 1.0) : 0.5;
        $sa2 = $f2->getStrikesThrown() > 0
            ? min($f2->getStrikesLanded() / $f2->getStrikesThrown(), 1.0) : 0.5;

        $form1 = min($f1->getWinStreak(), 10) / 10.0;
        $form2 = min($f2->getWinStreak(), 10) / 10.0;

        $perf1 = max(0.0, min($f1->getPerformanceScore(), 100.0)) / 100.0;
        $perf2 = max(0.0, min($f2->getPerformanceScore(), 100.0)) / 100.0;

        // Physical advantage: sum of height and reach differentials, clamped to [-60, 60]
        $physDiff = (($f1->getHeight() ?? 175) - ($f2->getHeight() ?? 175))
                  + (($f1->getReach()  ?? 175) - ($f2->getReach()  ?? 175));
        $physScore1 = 0.5 + max(-60, min(60, $physDiff)) / 120.0;
        $physScore2 = 1.0 - $physScore1;

        $score1 = 0.30 * $eloNorm1
                + 0.20 * $winRate1
                + 0.10 * $koRate1
                + 0.10 * $sa1
                + 0.10 * $form1
                + 0.10 * $perf1
                + 0.10 * $physScore1;

        $score2 = 0.30 * $eloNorm2
                + 0.20 * $winRate2
                + 0.10 * $koRate2
                + 0.10 * $sa2
                + 0.10 * $form2
                + 0.10 * $perf2
                + 0.10 * $physScore2;

        $eloDiff = abs($f1->getEloRating() - $f2->getEloRating());
        $drawProb = 4.0 + ($eloDiff < 50 ? 2.0 : 0.0);

        $total = max($score1 + $score2, 0.001);
        $f1Prob = round(($score1 / $total) * (100 - $drawProb), 1);
        $f2Prob = round(100 - $drawProb - $f1Prob, 1);

        return [
            'fighter1Probability' => $f1Prob,
            'fighter2Probability' => $f2Prob,
            'drawProbability'     => round($drawProb, 1),
            'breakdown' => [
                'fighter1' => [
                    'elo'      => round($eloNorm1 * 100, 1),
                    'record'   => round($winRate1 * 100, 1),
                    'form'     => round($form1 * 100, 1),
                    'physical' => round($physScore1 * 100, 1),
                    'strikes'  => round($sa1 * 100, 1),
                ],
                'fighter2' => [
                    'elo'      => round($eloNorm2 * 100, 1),
                    'record'   => round($winRate2 * 100, 1),
                    'form'     => round($form2 * 100, 1),
                    'physical' => round($physScore2 * 100, 1),
                    'strikes'  => round($sa2 * 100, 1),
                ],
            ],
        ];
    }

    private function calculatePoints(Prediction $p, FightResult $r): int
    {
        $points = 0;
        $winner = $r->getWinner();
        $predictedWinner = $p->getPredictedWinner();

        // 1. Correct Winner
        $correctWinner = false;
        if ($r->isDraw()) {
            if ($predictedWinner === null) $correctWinner = true;
        } else {
            if ($predictedWinner && $winner && $predictedWinner->getFighterId() === $winner->getFighterId()) {
                $correctWinner = true;
            }
        }

        if (!$correctWinner) return 0; // If you got the winner wrong, 0 points total (harsh fantasy style)

        $points += 10;

        // 2. Correct Method
        $actualMethod = strtoupper($r->getMethodOfVictory() ?? '');
        $predMethod = strtoupper($p->getPredictedMethod() ?? '');
        $correctMethod = ($actualMethod === $predMethod);

        if ($correctMethod) {
            $points += 10;
        }

        // 3. Correct Round (KO only)
        $correctRound = false;
        if ($actualMethod === FightResult::METHOD_KO) {
            if ($p->getPredictedRound() === $r->getRoundNumber()) {
                $points += 20;
                $correctRound = true;
            }
        }

        // 4. Perfect Prediction Bonus
        if ($correctMethod && ($actualMethod === FightResult::METHOD_DECISION || $correctRound)) {
            $points += 10;
        }

        return $points;
    }
}
