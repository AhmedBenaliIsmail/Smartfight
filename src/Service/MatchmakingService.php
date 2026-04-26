<?php
namespace App\Service;

use App\Entity\Fighter;
use App\Entity\FightResult;
use App\Repository\FighterRepository;
use App\Repository\FightResultRepository;
use Doctrine\ORM\EntityManagerInterface;

class MatchmakingService
{
    public function __construct(
        private EntityManagerInterface $em,
        private FighterRepository $fighterRepo,
        private FightResultRepository $resultRepo,
        private AIService $aiService,
    ) {}

    /**
     * Get AI-suggested matches for a card
     */
    public function suggestMatches(int $count = 5): array
    {
        $availableFighters = $this->fighterRepo->findAll();
        
        if (count($availableFighters) < 2) {
            return [
                'success' => false,
                'error' => 'Not enough fighters available',
                'matches' => []
            ];
        }

        // Prepare fighter data for AI
        $fighterData = [];
        foreach ($availableFighters as $f) {
            $record = $this->getRecentFightRecord($f->getFighterId());
            $fighterData[] = [
                'id' => $f->getFighterId(),
                'name' => $f->getFullName(),
                'weight' => $f->getWeight(),
                'division' => $f->getWeightDivision() ? $f->getWeightDivision()->getName() : 'N/A',
                'wins' => $f->getWins(),
                'losses' => $f->getLosses(),
                'draws' => $f->getDraws(),
                'win_rate' => $record['win_rate'],
                'elo' => $f->getEloRating(),
                'style' => $f->getCalculatedFightingStyle(),
                'form' => $record['form_summary']
            ];
        }

        // Call AI service
        $aiResult = $this->aiService->suggestMatches($fighterData);

        if (!$aiResult['success']) {
            return [
                'success' => false,
                'error' => $aiResult['error'],
                'matches' => $this->getFallbackMatches($availableFighters, $count)
            ];
        }

        return [
            'success' => true,
            'matches' => array_slice($aiResult['matches'], 0, $count)
        ];
    }

    /**
     * Get smart matchmaking using ELO rating and weight class
     */
    public function findBalancedOpponent(Fighter $fighter, int $eloTolerance = 100): ?Fighter
    {
        $potentialOpponents = [];
        $fighterElo = $fighter->getEloRating();
        $fighterWeight = $fighter->getWeightDivision();

        foreach ($this->fighterRepo->findAll() as $opponent) {
            // Skip self
            if ($opponent->getFighterId() === $fighter->getFighterId()) continue;

            // Prefer same weight class
            $sameWeight = $opponent->getWeightDivision() && 
                          $fighterWeight && 
                          $opponent->getWeightDivision()->getId() === $fighterWeight->getId();

            // Check ELO compatibility
            $eloDiff = abs($opponent->getEloRating() - $fighterElo);
            if ($eloDiff > $eloTolerance) continue;

            // Check if already fought recently
            $recentMatch = $this->resultRepo->findRecentMatch($fighter->getFighterId(), $opponent->getFighterId());
            if ($recentMatch) continue;

            $potentialOpponents[] = [
                'opponent' => $opponent,
                'score' => $this->calculateMatchScore($fighter, $opponent, $sameWeight, $eloDiff)
            ];
        }

        if (empty($potentialOpponents)) {
            return null;
        }

        // Sort by match score
        usort($potentialOpponents, fn($a, $b) => $b['score'] <=> $a['score']);

        return $potentialOpponents[0]['opponent'];
    }

    /**
     * Calculate match quality score (higher is better)
     */
    private function calculateMatchScore(Fighter $f1, Fighter $f2, bool $sameWeight, float $eloDiff): float
    {
        $score = 100.0;

        // Weight class bonus
        if ($sameWeight) {
            $score += 25.0;
        }

        // ELO compatibility (closer = better)
        $score -= ($eloDiff / 10.0);

        // Competitive balance
        $wr1 = $f1->getWins() > 0 ? $f1->getWins() / ($f1->getWins() + $f1->getLosses()) : 0.5;
        $wr2 = $f2->getWins() > 0 ? $f2->getWins() / ($f2->getWins() + $f2->getLosses()) : 0.5;
        $wrDiff = abs($wr1 - $wr2);
        
        if ($wrDiff < 0.15) {
            $score += 20.0; // Very balanced
        } elseif ($wrDiff < 0.30) {
            $score += 10.0; // Decent balance
        }

        // Style matchup interest (diverse styles = interesting fight)
        if ($f1->getCalculatedFightingStyle() !== $f2->getCalculatedFightingStyle()) {
            $score += 15.0;
        }

        return $score;
    }

    /**
     * Fallback matchmaking when AI is unavailable
     */
    private function getFallbackMatches(array $fighters, int $count = 5): array
    {
        $matches = [];
        $used = [];

        foreach ($fighters as $f1) {
            if (count($matches) >= $count) break;
            if (isset($used[$f1->getFighterId()])) continue;

            $opponent = $this->findBalancedOpponent($f1, 150);
            if ($opponent && !isset($used[$opponent->getFighterId()])) {
                $matches[] = [
                    'fighter1_id' => $f1->getFighterId(),
                    'fighter2_id' => $opponent->getFighterId(),
                    'reason' => 'Algorithmically balanced matchup',
                    'excitement_level' => 'medium'
                ];
                $used[$f1->getFighterId()] = true;
                $used[$opponent->getFighterId()] = true;
            }
        }

        return $matches;
    }

    /**
     * Get recent fight record for a fighter
     */
    private function getRecentFightRecord(int $fighterId): array
    {
        $fights = $this->resultRepo->findCompletedByFighter($fighterId, 10);
        
        $wins = 0;
        $losses = 0;
        $draws = 0;

        foreach ($fights as $fight) {
            if ($fight->isDraw()) {
                $draws++;
            } elseif ($fight->getWinner() && $fight->getWinner()->getFighterId() === $fighterId) {
                $wins++;
            } else {
                $losses++;
            }
        }

        $total = $wins + $losses + $draws;
        $winRate = $total > 0 ? round(($wins / $total) * 100, 1) : 0;

        // Form summary based on last 5 fights
        $recentFights = array_slice($fights, 0, 5);
        $recentWins = 0;
        foreach ($recentFights as $f) {
            if ($f->getWinner() && $f->getWinner()->getFighterId() === $fighterId) {
                $recentWins++;
            }
        }

        $formSummary = $recentWins >= 4 ? 'Hot' : ($recentWins >= 3 ? 'Good' : ($recentWins >= 2 ? 'Fair' : 'Cold'));

        return [
            'win_rate' => $winRate,
            'form_summary' => $formSummary
        ];
    }
}
