<?php

namespace App\Service;

use App\Entity\Fighter;
use App\Entity\FightResult;
use App\Entity\Ranking;
use App\Repository\FighterRepository;
use App\Repository\FightResultRepository;
use App\Repository\RankingRepository;
use Doctrine\ORM\EntityManagerInterface;

class RankingService
{
    private const DEFAULT_K_FACTOR = 32.0;

    public function __construct(
        private readonly EntityManagerInterface $entityManager,
        private readonly FighterRepository $fighterRepository,
        private readonly FightResultRepository $fightResultRepository,
        private readonly RankingRepository $rankingRepository,
    ) {
    }

    public function processCompletedResult(FightResult $result, float $kFactor = self::DEFAULT_K_FACTOR): void
    {
        if ($result->getStatus() !== 'COMPLETED') {
            return;
        }

        $red = $result->getFighterRed();
        $blue = $result->getFighterBlue();

        [$redScore, $blueScore] = $this->resolveActualScores($result);
        [$expectedRed, $expectedBlue] = $this->expectedScores($red->getEloRating(), $blue->getEloRating());

        $red->setEloRating($red->getEloRating() + $kFactor * ($redScore - $expectedRed));
        $blue->setEloRating($blue->getEloRating() + $kFactor * ($blueScore - $expectedBlue));

        $this->updateStreaks($red, $blue, $result);
        $this->updateStrengthOfSchedule($red, $blue);

        $this->entityManager->persist($red);
        $this->entityManager->persist($blue);
        $this->entityManager->flush();

        $this->recomputeRankings();
    }

    public function recomputeRankings(string $season = 'CURRENT'): void
    {
        $fighters = $this->fighterRepository->findBy([], ['eloRating' => 'DESC']);
        $grouped = [];

        foreach ($fighters as $fighter) {
            $weightClass = $fighter->getWeightClass() ?? 'UNSPECIFIED';
            $grouped[$weightClass][] = $fighter;
        }

        foreach ($grouped as $weightClass => $list) {
            foreach ($list as $index => $fighter) {
                $ranking = $this->rankingRepository->findOneBy([
                    'fighterId' => $fighter->getId(),
                    'weightClass' => $weightClass,
                    'season' => $season,
                ]) ?? new Ranking();

                $ranking
                    ->setFighterId((int) $fighter->getId())
                    ->setWeightClass($weightClass)
                    ->setSeason($season)
                    ->setRankPosition($index + 1)
                    ->setPoints($fighter->getEloRating());

                $this->entityManager->persist($ranking);
            }
        }

        $this->entityManager->flush();
    }

    private function resolveActualScores(FightResult $result): array
    {
        if ($result->isDraw() || $result->getWinner() === null) {
            return [0.5, 0.5];
        }

        if ($result->getWinner()->getId() === $result->getFighterRed()->getId()) {
            return [1.0, 0.0];
        }

        return [0.0, 1.0];
    }

    private function expectedScores(float $ratingA, float $ratingB): array
    {
        $expectedA = 1 / (1 + pow(10, ($ratingB - $ratingA) / 400));
        $expectedB = 1 / (1 + pow(10, ($ratingA - $ratingB) / 400));

        return [$expectedA, $expectedB];
    }

    private function updateStreaks(Fighter $red, Fighter $blue, FightResult $result): void
    {
        if ($result->isDraw() || $result->getWinner() === null) {
            $red->setWinStreak(0);
            $blue->setWinStreak(0);
            return;
        }

        $redWon = $result->getWinner()->getId() === $red->getId();
        $winner = $redWon ? $red : $blue;
        $loser = $redWon ? $blue : $red;

        $winner->setWinStreak($winner->getWinStreak() + 1);
        $loser->setWinStreak(0);

        if ($result->getEvent()->isChampionsEvent()) {
            $winner->setChampionsEventWinStreak($winner->getChampionsEventWinStreak() + 1);
        }
    }

    private function updateStrengthOfSchedule(Fighter $red, Fighter $blue): void
    {
        $red->setStrengthOfSchedule(($red->getStrengthOfSchedule() + $blue->getEloRating()) / 2);
        $blue->setStrengthOfSchedule(($blue->getStrengthOfSchedule() + $red->getEloRating()) / 2);
    }
}
