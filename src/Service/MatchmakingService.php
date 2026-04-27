<?php
namespace App\Service;

use App\Entity\Fighter;
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
     * AI-assisted matchmaking for the /matchmaking-suggestions endpoint.
     * Falls back to the local algorithm when DeepSeek is unavailable.
     */
    public function suggestMatches(int $count = 5): array
    {
        $fighters = $this->fighterRepo->findAll();

        if (count($fighters) < 2) {
            return ['success' => false, 'error' => 'Not enough fighters available', 'matches' => []];
        }

        $fighterData = [];
        foreach ($fighters as $f) {
            $record        = $this->getRecentFightRecord($f->getFighterId());
            $fighterData[] = [
                'id'       => $f->getFighterId(),
                'name'     => $f->getFullName(),
                'weight'   => $f->getWeight(),
                'division' => $f->getWeightDivision()?->getName() ?? 'N/A',
                'wins'     => $f->getWins(),
                'losses'   => $f->getLosses(),
                'draws'    => $f->getDraws(),
                'win_rate' => $record['win_rate'],
                'elo'      => $f->getEloRating(),
                'style'    => $f->getCalculatedFightingStyle(),
                'form'     => $record['form_summary'],
                'ko_rate'  => $f->getWins() > 0 ? round($f->getKoWins() / $f->getWins() * 100, 1) : 0.0,
                'accuracy' => round($f->getStrikeAccuracy(), 1),
                'height'   => $f->getHeight(),
                'reach'    => $f->getReach(),
            ];
        }

        $aiResult = $this->aiService->suggestMatches($fighterData);

        if (!$aiResult['success']) {
            return [
                'success' => false,
                'error'   => $aiResult['error'],
                'matches' => $this->getFallbackMatches($fighters, $count),
            ];
        }

        return [
            'success' => true,
            'matches' => array_slice($aiResult['matches'], 0, $count),
        ];
    }

    /**
     * Pure local 7-vector matchmaking for /generate-proposals (no AI dependency).
     *
     * Algorithm:
     *  1. Score every valid fighter pair with computeScoreIA() (no diversity context yet).
     *  2. Keep the top 50 candidates for efficiency.
     *  3. Greedy selection loop: re-score remaining pairs with the diversity context built
     *     so far, pick randomly from the Top-5 highest scorers, mark fighters as used.
     *  4. Return $count proposals with Score IA and a human-readable reason.
     */
    public function generateProposalsLocal(int $count = 5): array
    {
        $fighters = $this->fighterRepo->findAll();
        $n        = count($fighters);

        if ($n < 2) {
            return [];
        }

        // Build candidate pairs (skip recent rematches)
        $candidates = [];
        for ($i = 0; $i < $n; $i++) {
            for ($j = $i + 1; $j < $n; $j++) {
                $f1 = $fighters[$i];
                $f2 = $fighters[$j];

                if ($this->resultRepo->findRecentMatch($f1->getFighterId(), $f2->getFighterId())) {
                    continue;
                }

                $candidates[] = [
                    'f1'        => $f1,
                    'f2'        => $f2,
                    'raw_score' => $this->computeScoreIA($f1, $f2, []),
                    'score'     => 0.0,
                ];
            }
        }

        if (empty($candidates)) {
            return [];
        }

        // Pre-sort and keep top 50
        usort($candidates, fn($a, $b) => $b['raw_score'] <=> $a['raw_score']);
        $candidates = array_slice($candidates, 0, 50);

        // Greedy selection with diversity-aware scoring + Top-5 random sampling
        $selected     = [];
        $usedFighters = [];
        $usedDivIds   = [];

        while (count($selected) < $count && !empty($candidates)) {
            // Re-score with current diversity context
            foreach ($candidates as &$c) {
                $c['score'] = $this->computeScoreIA($c['f1'], $c['f2'], $usedDivIds);
            }
            unset($c);

            usort($candidates, fn($a, $b) => $b['score'] <=> $a['score']);

            // Random pick from Top 5
            $pool = array_slice($candidates, 0, min(5, count($candidates)));
            $pick = $pool[array_rand($pool)];
            $f1Id = $pick['f1']->getFighterId();
            $f2Id = $pick['f2']->getFighterId();

            if (!isset($usedFighters[$f1Id]) && !isset($usedFighters[$f2Id])) {
                $selected[]          = $pick;
                $usedFighters[$f1Id] = true;
                $usedFighters[$f2Id] = true;
                if ($pick['f1']->getWeightDivision()) {
                    $usedDivIds[] = $pick['f1']->getWeightDivision()->getId();
                }
            }

            // Remove selected pair and any pairs that share a now-used fighter
            $candidates = array_values(array_filter(
                $candidates,
                fn($c) => !(
                    ($c['f1']->getFighterId() === $f1Id && $c['f2']->getFighterId() === $f2Id)
                    || isset($usedFighters[$c['f1']->getFighterId()])
                    || isset($usedFighters[$c['f2']->getFighterId()])
                )
            ));
        }

        $matches = [];
        foreach ($selected as $s) {
            $score     = round($s['score'], 2);
            $matches[] = [
                'fighter1_id'    => $s['f1']->getFighterId(),
                'fighter2_id'    => $s['f2']->getFighterId(),
                'score_ia'       => $score,
                'reason'         => $this->buildMatchReason($s['f1'], $s['f2'], $score),
                'excitement_level' => $score >= 75 ? 'high' : ($score >= 50 ? 'medium' : 'low'),
            ];
        }

        return $matches;
    }

    /**
     * Find the best single opponent for a fighter using the 8-vector score.
     */
    public function findBalancedOpponent(Fighter $fighter, int $eloTolerance = 100): ?Fighter
    {
        $candidates = [];

        foreach ($this->fighterRepo->findAll() as $opponent) {
            if ($opponent->getFighterId() === $fighter->getFighterId()) {
                continue;
            }
            if (abs($opponent->getEloRating() - $fighter->getEloRating()) > $eloTolerance) {
                continue;
            }
            if ($this->resultRepo->findRecentMatch($fighter->getFighterId(), $opponent->getFighterId())) {
                continue;
            }

            $candidates[] = [
                'opponent' => $opponent,
                'score'    => $this->computeScoreIA($fighter, $opponent, []),
            ];
        }

        if (empty($candidates)) {
            return null;
        }

        usort($candidates, fn($a, $b) => $b['score'] <=> $a['score']);

        return $candidates[0]['opponent'];
    }

    /**
     * Compute the 8-vector Score IA (0–100) for a fighter pairing.
     *
     * Vector breakdown (max points):
     *   1. Weight Integrity  — 25 pts  same official division
     *   2. ELO Parity        — 20 pts  linear decay over 400-pt gap
     *   3. Record Parity     — 15 pts  win-rate proximity
     *   4. Height Balance    —  8 pts  diff capped at 30 cm; neutral 4 when absent
     *   5. Reach Balance     —  7 pts  diff capped at 30 cm; neutral 3.5 when absent
     *   6. Lethality         — 10 pts  similar KO/finish ratios
     *   7. Precision         — 10 pts  strike accuracy alignment
     *   8. Diversity         —  5 pts  bonus when division not already in batch
     *                          ———
     *                         100 pts
     *
     * @param int[] $usedDivisionIds  Division IDs already committed to in this proposal batch.
     */
    public function computeScoreIA(Fighter $f1, Fighter $f2, array $usedDivisionIds): float
    {
        $score = 0.0;

        // 1. Weight Integrity
        $sameDiv = $f1->getWeightDivision() !== null
            && $f2->getWeightDivision() !== null
            && $f1->getWeightDivision()->getId() === $f2->getWeightDivision()->getId();
        $score += $sameDiv ? 25.0 : 0.0;

        // 2. ELO Parity
        $eloDiff = abs($f1->getEloRating() - $f2->getEloRating());
        $score  += max(0.0, 20.0 * (1.0 - $eloDiff / 400.0));

        // 3. Record Parity
        $wr1    = ($f1->getWins() + $f1->getLosses()) > 0
            ? $f1->getWins() / ($f1->getWins() + $f1->getLosses())
            : 0.5;
        $wr2    = ($f2->getWins() + $f2->getLosses()) > 0
            ? $f2->getWins() / ($f2->getWins() + $f2->getLosses())
            : 0.5;
        $score += max(0.0, 15.0 * (1.0 - abs($wr1 - $wr2)));

        // 4. Height Balance (neutral when data absent)
        if ($f1->getHeight() !== null && $f2->getHeight() !== null) {
            $score += max(0.0, 8.0 * (1.0 - abs($f1->getHeight() - $f2->getHeight()) / 30.0));
        } else {
            $score += 4.0;
        }

        // 5. Reach Balance (neutral when data absent)
        if ($f1->getReach() !== null && $f2->getReach() !== null) {
            $score += max(0.0, 7.0 * (1.0 - abs($f1->getReach() - $f2->getReach()) / 30.0));
        } else {
            $score += 3.5;
        }

        // 6. Lethality — KO finishing ratio proximity (0–1 scale each)
        $ko1    = $f1->getWins() > 0 ? $f1->getKoWins() / $f1->getWins() : 0.0;
        $ko2    = $f2->getWins() > 0 ? $f2->getKoWins() / $f2->getWins() : 0.0;
        $score += max(0.0, 10.0 * (1.0 - abs($ko1 - $ko2)));

        // 7. Precision — strike accuracy alignment (0–100 scale)
        $score += max(0.0, 10.0 * (1.0 - abs($f1->getStrikeAccuracy() - $f2->getStrikeAccuracy()) / 100.0));

        // 8. Diversity — reward adding a new weight class to the batch
        $divId  = $f1->getWeightDivision()?->getId();
        $score += (!$divId || !in_array($divId, $usedDivisionIds, true)) ? 5.0 : 0.0;

        return max(0.0, min(100.0, $score));
    }

    // ─── Private helpers ────────────────────────────────────────────────────

    private function getFallbackMatches(array $fighters, int $count = 5): array
    {
        $matches = [];
        $used    = [];

        foreach ($fighters as $f1) {
            if (count($matches) >= $count) break;
            if (isset($used[$f1->getFighterId()])) continue;

            $opponent = $this->findBalancedOpponent($f1, 150);
            if ($opponent && !isset($used[$opponent->getFighterId()])) {
                $score     = $this->computeScoreIA($f1, $opponent, []);
                $matches[] = [
                    'fighter1_id'    => $f1->getFighterId(),
                    'fighter2_id'    => $opponent->getFighterId(),
                    'score_ia'       => round($score, 2),
                    'reason'         => 'Algorithmically balanced matchup',
                    'excitement_level' => $score >= 75 ? 'high' : ($score >= 50 ? 'medium' : 'low'),
                ];
                $used[$f1->getFighterId()]       = true;
                $used[$opponent->getFighterId()] = true;
            }
        }

        return $matches;
    }

    private function buildMatchReason(Fighter $f1, Fighter $f2, float $score): string
    {
        $parts = [];

        if ($f1->getWeightDivision() && $f2->getWeightDivision()
            && $f1->getWeightDivision()->getId() === $f2->getWeightDivision()->getId()) {
            $parts[] = "same {$f1->getWeightDivision()->getName()} division";
        }

        $t1 = $f1->getWins() + $f1->getLosses();
        $t2 = $f2->getWins() + $f2->getLosses();
        if ($t1 > 0 && $t2 > 0) {
            $wr1 = round($f1->getWins() / $t1 * 100);
            $wr2 = round($f2->getWins() / $t2 * 100);
            if (abs($wr1 - $wr2) <= 10) {
                $parts[] = "near-identical win rates ({$wr1}% vs {$wr2}%)";
            }
        }

        $ko1 = $f1->getWins() > 0 ? round($f1->getKoWins() / $f1->getWins() * 100) : 0;
        $ko2 = $f2->getWins() > 0 ? round($f2->getKoWins() / $f2->getWins() * 100) : 0;
        if (abs($ko1 - $ko2) <= 15) {
            $parts[] = "matched finishing instincts (KO: {$ko1}% vs {$ko2}%)";
        }

        $acc1 = round($f1->getStrikeAccuracy());
        $acc2 = round($f2->getStrikeAccuracy());
        if (abs($acc1 - $acc2) <= 5 && ($acc1 > 0 || $acc2 > 0)) {
            $parts[] = "aligned strike accuracy ({$acc1}% vs {$acc2}%)";
        }

        $base = empty($parts)
            ? 'Competitive pairing by 7-vector algorithm'
            : ucfirst(implode(', ', $parts));

        return "{$base}. Score IA: {$score}/100.";
    }

    private function getRecentFightRecord(int $fighterId): array
    {
        $fights = $this->resultRepo->findCompletedByFighter($fighterId, 10);

        $wins = $losses = $draws = 0;
        foreach ($fights as $fight) {
            if ($fight->isDraw()) {
                $draws++;
            } elseif ($fight->getWinner()?->getFighterId() === $fighterId) {
                $wins++;
            } else {
                $losses++;
            }
        }

        $total   = $wins + $losses + $draws;
        $winRate = $total > 0 ? round(($wins / $total) * 100, 1) : 0;

        $recentWins = 0;
        foreach (array_slice($fights, 0, 5) as $f) {
            if ($f->getWinner()?->getFighterId() === $fighterId) {
                $recentWins++;
            }
        }

        return [
            'win_rate'     => $winRate,
            'form_summary' => match(true) {
                $recentWins >= 4 => 'Hot',
                $recentWins >= 3 => 'Good',
                $recentWins >= 2 => 'Fair',
                default          => 'Cold',
            },
        ];
    }
}
