<?php

namespace App\Service;

use App\Repository\FanPredictionRepository;

class LeaderboardService
{
    public function __construct(
        private FanPredictionRepository $predictionRepo,
    ) {}

    public function getRankings(string $season = '2026', int $limit = 50): array
    {
        $rows = $this->predictionRepo->findLeaderboard($season, $limit);

        $rank = 0;
        foreach ($rows as &$row) {
            $rank++;
            $row['rank'] = $rank;
            $row['accuracy'] = $row['total_predictions'] > 0
                ? round(($row['correct_predictions'] / $row['total_predictions']) * 100, 1)
                : 0;
        }

        return $rows;
    }

    public function getTopThree(string $season = '2026'): array
    {
        return array_slice($this->getRankings($season, 3), 0, 3);
    }

    public function getFanRank(int $fanId, string $season = '2026'): ?int
    {
        $rankings = $this->getRankings($season);
        foreach ($rankings as $row) {
            if ((int) $row['fan_id'] === $fanId) {
                return $row['rank'];
            }
        }
        return null;
    }
}
