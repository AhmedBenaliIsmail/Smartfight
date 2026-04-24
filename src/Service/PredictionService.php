<?php
namespace App\Service;

use App\Entity\FightResult;
use App\Entity\Prediction;
use App\Entity\User;
use App\Repository\PredictionRepository;
use Doctrine\ORM\EntityManagerInterface;

class PredictionService
{
    public function __construct(
        private EntityManagerInterface $em,
        private PredictionRepository $predictionRepo
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
                    $this->notify($user, "🎯 You earned {$points} pts for your prediction on {$fight->getFighter1()->getLastName()} vs {$fight->getFighter2()->getLastName()}!", 'PREDICTION');
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
                $this->notify($user, "📈 Great job! Your fantasy rank moved up from #{$oldRank} to #{$newRank}!", 'RANKING');
            } elseif ($newRank > $oldRank) {
                $this->notify($user, "📉 Watch out! Your fantasy rank dropped from #{$oldRank} to #{$newRank}.", 'RANKING');
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

    private function notify(User $user, string $message, string $type): void
    {
        $n = new \App\Entity\Notification();
        $n->setUser($user);
        $n->setMessage($message);
        $n->setType($type);
        $this->em->persist($n);
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
