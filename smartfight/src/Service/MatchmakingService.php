<?php
namespace App\Service;

use App\Entity\Combattant;
use App\Entity\Combat;
use App\Repository\CombattantRepository;

class MatchmakingService
{
    public function __construct(
        private CombattantRepository $combattantRepo
    ) {}

    /**
     * Calcule le score IA entre 2 combattants
     */
    public function calculateScore(Combattant $c1, Combattant $c2): float
    {
        $score = 0;

        // 1. Même weight class = +40 points
        if ($c1->getWeightClass() === $c2->getWeightClass()) {
            $score += 40;
        }

        // 2. Différence de win rate (plus proche = meilleur)
        $total1 = $c1->getWins() + $c1->getLosses() + $c1->getDraws();
        $total2 = $c2->getWins() + $c2->getLosses() + $c2->getDraws();

        $wr1 = $total1 > 0 ? ($c1->getWins() / $total1) * 100 : 0;
        $wr2 = $total2 > 0 ? ($c2->getWins() / $total2) * 100 : 0;

        $wrDiff = abs($wr1 - $wr2);
        $score += max(0, 30 - $wrDiff * 0.5);

        // 3. Expérience similaire (nombre de combats)
        $exp1 = $total1;
        $exp2 = $total2;
        $expDiff = abs($exp1 - $exp2);
        $score += max(0, 20 - $expDiff * 0.3);

        // 4. Même discipline = +10 points
        if ($c1->getDiscipline() === $c2->getDiscipline()) {
            $score += 10;
        }

        return min(100, round($score, 1));
    }

    /**
     * Génère les N meilleurs matchs
     */
    public function generateMatches(int $max = 10): array
    {
        $combattants = $this->combattantRepo->findAll();
        $matches = [];

        for ($i = 0; $i < count($combattants); $i++) {
            for ($j = $i + 1; $j < count($combattants); $j++) {
                $c1 = $combattants[$i];
                $c2 = $combattants[$j];
                $score = $this->calculateScore($c1, $c2);

                $matches[] = [
                    'combattant1' => $c1,
                    'combattant2' => $c2,
                    'score'       => $score,
                    'qualite'     => $this->getQualite($score),
                    'analyse'     => $this->getAnalyse($c1, $c2, $score),
                ];
            }
        }

        // Trier par score décroissant
        usort($matches, fn($a, $b) => $b['score'] <=> $a['score']);

        return array_slice($matches, 0, $max);
    }

    /**
     * Trouve le meilleur adversaire pour un combattant
     */
    public function findBestOpponent(Combattant $combattant): ?array
    {
        $tous = $this->combattantRepo->findAll();
        $best = null;
        $bestScore = -1;

        foreach ($tous as $adversaire) {
            if ($adversaire->getId() === $combattant->getId()) continue;

            $score = $this->calculateScore($combattant, $adversaire);
            if ($score > $bestScore) {
                $bestScore = $score;
                $best = [
                    'combattant'  => $adversaire,
                    'score'       => $score,
                    'qualite'     => $this->getQualite($score),
                    'analyse'     => $this->getAnalyse($combattant, $adversaire, $score),
                ];
            }
        }

        return $best;
    }

    private function getQualite(float $score): string
    {
        if ($score >= 85) return 'EXCELLENT';
        if ($score >= 70) return 'BON';
        if ($score >= 50) return 'ACCEPTABLE';
        return 'FAIBLE';
    }

    private function getAnalyse(Combattant $c1, Combattant $c2, float $score): string
    {
        $total1 = $c1->getWins() + $c1->getLosses() + $c1->getDraws();
        $total2 = $c2->getWins() + $c2->getLosses() + $c2->getDraws();
        $wr1 = $total1 > 0 ? round(($c1->getWins() / $total1) * 100, 1) : 0;
        $wr2 = $total2 > 0 ? round(($c2->getWins() / $total2) * 100, 1) : 0;

        return "═══════ ANALYSE DU MATCH (IA) ═══════\n\n" .
               "  {$c1->getNickname()}   VS   {$c2->getNickname()}\n\n" .
               "Combattant 1 : {$c1->getWins()}-{$c1->getLosses()}-{$c1->getDraws()} | WR: {$wr1}% | {$total1} combats\n" .
               "Combattant 2 : {$c2->getWins()}-{$c2->getLosses()}-{$c2->getDraws()} | WR: {$wr2}% | {$total2} combats\n\n" .
               "SCORE IA : {$score} / 100\n" .
               "QUALITÉ  : {$this->getQualite($score)}\n\n" .
               "ANALYSE :\n" .
               ($c1->getWeightClass() === $c2->getWeightClass() ? "✅ Même catégorie de poids\n" : "⚠️ Catégories différentes\n") .
               ($c1->getDiscipline() === $c2->getDiscipline() ? "✅ Même discipline\n" : "⚠️ Disciplines différentes\n") .
               (abs($wr1 - $wr2) < 15 ? "✅ Niveaux équilibrés\n" : "⚠️ Écart de niveau significatif\n");
    }
}