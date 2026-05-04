<?php
namespace App\Service;

use App\Entity\FightResult;
use App\Entity\FightStatistic;

/**
 * BOUT ANALYSIS ENGINE v2.0 - Senior Implementation
 * 
 * Provides professional-grade statistical modeling for boxing matches,
 * calculating dominance scores, efficiency indices, and tactical insights.
 */
class BoutAnalysisService
{
    /**
     * Executes a comprehensive multi-vector analysis of the bout statistics.
     */
    public function analyzeBout(FightResult $result, array $aggregates, array $allStats): array
    {
        if (count($aggregates) < 2) {
            return $this->buildEmptyAnalysis();
        }

        $s1 = $aggregates[0];
        $s2 = $aggregates[1];
        
        $f1 = $s1->getFighter();
        $f2 = $s2->getFighter();

        // ─── CORE METRICS ───────────────────────────────────────────────────
        $totalLanded = $s1->getPunchesLanded() + $s2->getPunchesLanded();
        $totalThrown = $s1->getPunchesThrown() + $s2->getPunchesThrown();
        $totalKds    = $s1->getKnockdowns() + $s2->getKnockdowns();
        $overallAcc  = $totalThrown > 0 ? ($totalLanded / $totalThrown) * 100 : 0;

        // ─── ADVANCED MODELING ──────────────────────────────────────────────
        $winnerStat = null;
        $domScore = 5.0; // Baseline parity

        if (!$result->isDraw() && $result->getWinner()) {
            $isF1Winner = ($result->getWinner()->getFighterId() === $f1->getFighterId());
            $winnerStat = $isF1Winner ? $s1 : $s2;
            $loserStat  = $isF1Winner ? $s2 : $s1;
            
            // Efficiency Index (Landed Ratio)
            $efficiencyIndex = $loserStat->getPunchesLanded() > 0 ? 
                ($winnerStat->getPunchesLanded() / $loserStat->getPunchesLanded()) : 2.5;
            
            // Tactical Components
            $kdVector  = $winnerStat->getKnockdowns() * 2.0;
            $accVector = ($winnerStat->getPunchAccuracy() - $loserStat->getPunchAccuracy()) * 0.1;
            $volVector = ($winnerStat->getPunchesThrown() > $loserStat->getPunchesThrown()) ? 0.5 : -0.5;
            
            $domScore = min(10.0, 5.0 + ($efficiencyIndex * 1.5) + $kdVector + $accVector + $volVector);
        }

        // ─── NARRATIVE CONSTRUCTION ──────────────────────────────────────────
        $analysis = [];
        $insights = [];

        // 1. Tactical Conclusion
        if ($result->isDraw()) {
            $analysis[] = "A grueling stalemate defined by extreme statistical parity. Neither combatant could definitively solve the other's defensive structure, resulting in a razor-close deadlock.";
        } else {
            $winner = $result->getWinner();
            $method = $result->getMethodOfVictory();
            
            if ($method === FightResult::METHOD_KO) {
                $analysis[] = "A clinical stoppage victory for {$winner->getLastName()}, who translated statistical pressure into a terminal finish in Round {$result->getRoundNumber()}.";
                $insights[] = "{$winner->getLastName()} demonstrated elite 'Stop-Start' kinetic efficiency, concluding the bout before it reached the judges.";
            } else {
                $type = $result->getDecisionType() ?: 'Decision';
                $analysis[] = "A strategic masterclass by {$winner->getLastName()}, utilizing superior ring generalship to secure a {$type} over the distance.";
                $insights[] = "Tactical discipline and range management were the primary differentiators for the victor.";
            }
        }

        // 2. Efficiency & Output Analysis
        $higherVol = ($s1->getPunchesThrown() > $s2->getPunchesThrown()) ? $s1 : $s2;
        $higherAcc = ($s1->getPunchAccuracy() > $s2->getPunchAccuracy()) ? $s1 : $s2;

        $analysis[] = "{$higherVol->getFighter()->getLastName()} dictated the tempo with {$higherVol->getPunchesThrown()} attempts, while {$higherAcc->getFighter()->getLastName()} provided the clinical counter-balance, landing with {$higherAcc->getPunchAccuracy()}% precision.";

        // 3. Kinetic Trends (Round-by-Round)
        $roundsWonByF1 = 0;
        $roundsWonByF2 = 0;
        $lateSurge = ['fighter' => null, 'count' => 0];

        $roundMap = [];
        foreach ($allStats as $st) { $roundMap[$st->getRound()][] = $st; }

        foreach ($roundMap as $r => $stats) {
            if (count($stats) === 2) {
                $r1 = $stats[0]; $r2 = $stats[1];
                $winnerId = ($r1->getPunchesLanded() >= $r2->getPunchesLanded()) ? $r1->getFighter()->getFighterId() : $r2->getFighter()->getFighterId();
                
                if ($winnerId === $f1->getFighterId()) $roundsWonByF1++; else $roundsWonByF2++;
                
                if ($r > 6) {
                    $rWinner = ($winnerId === $f1->getFighterId()) ? $f1 : $f2;
                    if ($lateSurge['fighter'] === $rWinner) $lateSurge['count']++;
                    else { $lateSurge['fighter'] = $rWinner; $lateSurge['count'] = 1; }
                }
            }
        }

        // 4. Detailed Tactical Insights
        $this->generateTacticalInsights($insights, $f1, $f2, $s1, $s2, $roundsWonByF1, $roundsWonByF2, $lateSurge);

        return [
            'summary' => implode(" ", $analysis),
            'dominanceScore' => round($domScore, 1),
            'totalPunchesLanded' => $totalLanded,
            'knockdowns' => $totalKds,
            'overallAccuracy' => round($overallAcc, 1),
            'insights' => $insights
        ];
    }

    private function generateTacticalInsights(array &$insights, $f1, $f2, $s1, $s2, $r1, $r2, $surge): void
    {
        // Connectivity Dominance
        if (abs($r1 - $r2) > 3) {
            $dom = $r1 > $r2 ? $f1 : $f2;
            $insights[] = "{$dom->getLastName()} controlled the connective architecture, outlanding the opponent in " . max($r1, $r2) . " distinct rounds.";
        }

        // Championship Conditioning
        if ($surge['count'] >= 3 && $surge['fighter']) {
            $insights[] = "{$surge['fighter']->getLastName()} demonstrated elite cardiovascular reserves, seizing total kinetic control during the championship rounds.";
        }

        // Power Distribution
        $p1 = $s1->getPowerPunchesLanded(); $p2 = $s2->getPowerPunchesLanded();
        if (abs($p1 - $p2) > 12) {
            $pDom = $p1 > $p2 ? $f1 : $f2;
            $pCount = ($pDom === $f1) ? $p1 : $p2;
            $insights[] = "{$pDom->getLastName()} dominated the heavy-artillery exchanges, landing {$pCount} significant power shots.";
        }

        // Defensive Efficiency
        $acc1 = $s1->getPunchAccuracy(); $acc2 = $s2->getPunchAccuracy();
        if (abs($acc1 - $acc2) > 15) {
            $eff = $acc1 > $acc2 ? $f1 : $f2;
            $insights[] = "Precision gap identified: {$eff->getLastName()} operated at a much higher efficiency threshold than the opposition.";
        }
    }

    private function buildEmptyAnalysis(): array
    {
        return [
            'summary' => "Insufficient kinetic data for professional modeling.",
            'dominanceScore' => 0,
            'totalPunchesLanded' => 0,
            'knockdowns' => 0,
            'overallAccuracy' => 0,
            'insights' => ["Data integrity verification required for full tactical breakdown."]
        ];
    }
}
