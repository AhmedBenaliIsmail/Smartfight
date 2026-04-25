<?php
namespace App\Service;

use App\Entity\FightResult;
use App\Entity\FightStatistic;

class BoutAnalysisService
{
    public function analyzeBout(FightResult $result, array $stats): string
    {
        if (count($stats) < 2) return "Insufficient data for detailed analysis.";

        $s1 = $stats[0];
        $s2 = $stats[1];
        
        $f1 = $s1->getFighter();
        $f2 = $s2->getFighter();

        $analysis = [];

        // 1. Overall Performance Intro
        if ($result->isDraw()) {
            $analysis[] = "A grueling stalemate where neither boxer could definitively pull ahead in the eyes of the judges.";
        } else {
            $winner = $result->getWinner();
            $loser = ($winner === $f1) ? $f2 : $f1;
            $winnerStat = ($winner === $f1) ? $s1 : $s2;
            $loserStat = ($winner === $f1) ? $s2 : $s1;

            if ($result->getMethodOfVictory() === FightResult::METHOD_KO) {
                $analysis[] = "A dominant stoppage victory for {$winner->getLastName()}, who found the finishing blow in round {$result->getRoundNumber()}.";
            } else {
                $decisionType = $result->getDecisionType() ?: 'decision';
                $analysis[] = "A tactical masterclass by {$winner->getLastName()}, securing a {$decisionType} victory over {$result->getRoundNumber()} rounds.";
            }
        }

        // 2. Punch Accuracy & Volume
        $higherVolume = ($s1->getPunchesThrown() > $s2->getPunchesThrown()) ? $s1 : $s2;
        $higherAccuracy = ($s1->getPunchAccuracy() > $s2->getPunchAccuracy()) ? $s1 : $s2;

        $analysis[] = "{$higherVolume->getFighter()->getLastName()} led the volume with {$higherVolume->getPunchesThrown()} total punches thrown, while {$higherAccuracy->getFighter()->getLastName()} was more efficient, landing at a {$higherAccuracy->getPunchAccuracy()}% clip.";

        // 3. Power Punches & Body Work
        if ($s1->getPowerPunchesLanded() > $s2->getPowerPunchesLanded() + 10) {
            $analysis[] = "{$f1->getLastName()} held a significant connect advantage in power punches ({$s1->getPowerPunchesLanded()} to {$s2->getPowerPunchesLanded()}).";
        } elseif ($s2->getPowerPunchesLanded() > $s1->getPowerPunchesLanded() + 10) {
            $analysis[] = "{$f2->getLastName()} dictated the pace with heavy artillery, landing {$s2->getPowerPunchesLanded()} power shots.";
        }

        if ($s1->getBodyShotsLanded() > 15 || $s2->getBodyShotsLanded() > 15) {
            $heavyBodyBoxer = ($s1->getBodyShotsLanded() > $s2->getBodyShotsLanded()) ? $f1 : $f2;
            $analysis[] = "Investments to the body by {$heavyBodyBoxer->getLastName()} played a crucial factor in the middle rounds.";
        }

        // 4. Knockdowns
        if ($s1->getKnockdowns() > 0 || $s2->getKnockdowns() > 0) {
            $totalKD = $s1->getKnockdowns() + $s2->getKnockdowns();
            $analysis[] = "The bout featured {$totalKD} total knockdown(s), swinging the momentum of the fight.";
        }

        return implode(" ", $analysis);
    }
}
