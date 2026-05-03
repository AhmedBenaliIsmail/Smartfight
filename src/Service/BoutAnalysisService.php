<?php
namespace App\Service;

use App\Entity\FightResult;
use App\Entity\FightStatistic;

class BoutAnalysisService
{
    public function analyzeBout(FightResult $result, array $aggregates, array $allStats): array
    {
        if (count($aggregates) < 2) {
            return [
                'summary' => "Insufficient data for detailed analysis.",
                'dominanceScore' => 0,
                'totalPunchesLanded' => 0,
                'knockdowns' => 0,
                'overallAccuracy' => 0,
                'insights' => []
            ];
        }

        $s1 = $aggregates[0];
        $s2 = $aggregates[1];
        
        $f1 = $s1->getFighter();
        $f2 = $s2->getFighter();

        $totalLanded = $s1->getPunchesLanded() + $s2->getPunchesLanded();
        $totalThrown = $s1->getPunchesThrown() + $s2->getPunchesThrown();
        $totalKds = $s1->getKnockdowns() + $s2->getKnockdowns();
        $overallAcc = $totalThrown > 0 ? ($totalLanded / $totalThrown) * 100 : 0;

        // Dominance Score logic (0 to 10 scale)
        $winnerStat = null;
        if (!$result->isDraw() && $result->getWinner()) {
            $winnerStat = ($result->getWinner()->getFighterId() === $f1->getFighterId()) ? $s1 : $s2;
            $loserStat = ($winnerStat === $s1) ? $s2 : $s1;
            
            $landedAdvantage = $loserStat->getPunchesLanded() > 0 ? 
                ($winnerStat->getPunchesLanded() / $loserStat->getPunchesLanded()) : 2;
            
            $kdBonus = $winnerStat->getKnockdowns() * 1.5;
            $accBonus = ($winnerStat->getPunchAccuracy() > 40) ? 1.5 : 0;
            
            $domScore = min(10, 5 + ($landedAdvantage * 1.2) + $kdBonus + $accBonus);
        } else {
            $domScore = 5.0; // Draw or unknown
        }

        $analysis = [];
        $insights = [];

        // 1. Overall Performance Intro
        if ($result->isDraw()) {
            $analysis[] = "A grueling stalemate where neither boxer could definitively pull ahead. Both fighters had their moments, resulting in a razor-close split on the scorecards.";
        } else {
            $winner = $result->getWinner();
            $loser = ($winner === $f1) ? $f2 : $f1;

            if ($result->getMethodOfVictory() === FightResult::METHOD_KO) {
                $analysis[] = "A dominant stoppage victory for {$winner->getLastName()}, who found the finishing blow in round {$result->getRoundNumber()}.";
                $insights[] = "{$winner->getLastName()} possessed absolute finishing power, not allowing the fight to go to the judges.";
            } else {
                $decisionType = $result->getDecisionType() ?: 'decision';
                $analysis[] = "A tactical masterclass by {$winner->getLastName()}, securing a {$decisionType} victory over {$result->getRoundNumber()} rounds.";
            }
        }

        // 2. Punch Accuracy & Volume
        $higherVolume = ($s1->getPunchesThrown() > $s2->getPunchesThrown()) ? $s1 : $s2;
        $higherAccuracy = ($s1->getPunchAccuracy() > $s2->getPunchAccuracy()) ? $s1 : $s2;

        $acc1 = round($s1->getPunchAccuracy(), 1);
        $acc2 = round($s2->getPunchAccuracy(), 1);
        $analysis[] = "{$higherVolume->getFighter()->getLastName()} led the volume with {$higherVolume->getPunchesThrown()} total punches thrown, while {$higherAccuracy->getFighter()->getLastName()} was more efficient, landing at a {$higherAccuracy->getPunchAccuracy()}% clip.";

        // 3. Round-by-Round Trends (Analyzing $allStats)
        $f1RoundsWon = 0;
        $f2RoundsWon = 0;
        
        $roundsData = [];
        foreach ($allStats as $stat) {
            $roundsData[$stat->getRound()][] = $stat;
        }
        
        $lateSurgeFighter = null;
        $lateSurgeCount = 0;

        foreach ($roundsData as $r => $roundStats) {
            if (count($roundStats) == 2) {
                $rs1 = $roundStats[0];
                $rs2 = $roundStats[1];
                $rs1FighterId = $rs1->getFighter()->getFighterId();
                
                $rs1Landed = $rs1->getPunchesLanded();
                $rs2Landed = $rs2->getPunchesLanded();
                
                if ($rs1Landed > $rs2Landed) {
                    if ($rs1FighterId === $f1->getFighterId()) $f1RoundsWon++; else $f2RoundsWon++;
                    if ($r > 6) { $lateSurgeFighter = $rs1->getFighter(); $lateSurgeCount++; }
                } elseif ($rs2Landed > $rs1Landed) {
                    if ($rs1FighterId === $f1->getFighterId()) $f2RoundsWon++; else $f1RoundsWon++;
                    if ($r > 6) { $lateSurgeFighter = $rs2->getFighter(); $lateSurgeCount++; }
                }
            }
        }

        if ($f1RoundsWon > $f2RoundsWon + 3) {
            $insights[] = "{$f1->getLastName()} dominated the CompuBox round-by-round metrics, outlanding {$f2->getLastName()} in {$f1RoundsWon} distinct rounds.";
        } elseif ($f2RoundsWon > $f1RoundsWon + 3) {
            $insights[] = "{$f2->getLastName()} dominated the CompuBox round-by-round metrics, outlanding {$f1->getLastName()} in {$f2RoundsWon} distinct rounds.";
        } else {
            $insights[] = "The round-by-round connect margins were incredibly close, reflecting a highly contested bout.";
        }

        if ($lateSurgeCount >= 3 && $lateSurgeFighter) {
            $insights[] = "{$lateSurgeFighter->getLastName()} demonstrated elite conditioning, taking over the fight and outlanding the opponent consistently in the championship rounds.";
        }

        // 4. Power Punches & Body Work
        if ($s1->getPowerPunchesLanded() > $s2->getPowerPunchesLanded() + 10) {
            $insights[] = "{$f1->getLastName()} held a significant connect advantage in power punches ({$s1->getPowerPunchesLanded()} to {$s2->getPowerPunchesLanded()}).";
        } elseif ($s2->getPowerPunchesLanded() > $s1->getPowerPunchesLanded() + 10) {
            $insights[] = "{$f2->getLastName()} dictated the pace with heavy artillery, landing {$s2->getPowerPunchesLanded()} power shots.";
        }

        return [
            'summary' => implode(" ", $analysis),
            'dominanceScore' => round($domScore, 1),
            'totalPunchesLanded' => $totalLanded,
            'knockdowns' => $totalKds,
            'overallAccuracy' => round($overallAcc, 1),
            'insights' => $insights
        ];
    }
}
