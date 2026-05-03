<?php

namespace App\Service;

use App\Entity\Fighter;
use App\Entity\FightStatistic;
use App\Repository\FightStatisticRepository;

class PerformanceAnalyzer
{
    private $statRepo;

    public function __construct(FightStatisticRepository $statRepo)
    {
        $this->statRepo = $statRepo;
    }

    /**
     * Calculate a comprehensive Efficiency Score (0-100)
     */
    public function calculateEfficiencyScore(Fighter $fighter): float
    {
        $accuracy = $fighter->getStrikeAccuracy(); // 0-100
        $winRate = $fighter->getTotalFights() > 0 ? ($fighter->getWins() / $fighter->getTotalFights()) * 100 : 0;
        $streakBonus = min($fighter->getWinStreak() * 5, 25); // Max 25 points bonus for streak

        $score = ($accuracy * 0.4) + ($winRate * 0.4) + $streakBonus;
        
        return round(min($score, 100), 2);
    }

    /**
     * Determine fighter's current "Momentum"
     */
    public function getMomentum(Fighter $fighter): string
    {
        $streak = $fighter->getWinStreak();
        if ($streak >= 3) return 'SCORCHING';
        if ($streak >= 1) return 'RISING';
        if ($fighter->getLosses() > 0 && $fighter->getWins() == 0) return 'STRUGGLING';
        return 'STABLE';
    }

    /**
     * Analyze stylistic compatibility for an upcoming fight
     */
    public function getStyleMatchupAnalysis(Fighter $f1, Fighter $f2): array
    {
        $style1 = $f1->getCalculatedFightingStyle();
        $style2 = $f2->getCalculatedFightingStyle();

        $analysis = [
            'f1_style' => $style1,
            'f2_style' => $style2,
            'compatibility' => 'NEUTRAL',
            'tactical_note' => ''
        ];

        if ($style1 === 'SLUGGER' && $style2 === 'OUT-BOXER') {
            $analysis['compatibility'] = 'VOLATILE';
            $analysis['tactical_note'] = "Classic 'Bull vs Matador' matchup. Speed will be the deciding factor.";
        } elseif ($style1 === 'SHARPSHOOTER' && $style1 === $style2) {
            $analysis['compatibility'] = 'TACTICAL';
            $analysis['tactical_note'] = "High-level chess match expected. Mistake-free boxing is required.";
        }

        return $analysis;
    }
}
