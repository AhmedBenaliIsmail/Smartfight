<?php

namespace App\Command;

use App\Entity\FightStatistic;
use App\Repository\FightResultRepository;
use App\Service\AnalyticsEngine;
use App\Service\RankingService;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Component\Console\Attribute\AsCommand;
use Symfony\Component\Console\Command\Command;
use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Output\OutputInterface;
use Symfony\Component\Console\Style\SymfonyStyle;

#[AsCommand(
    name: 'app:analytics:backfill',
    description: 'Backfills ELO analytics tables from existing fighters and fight results',
)]
class BackfillEloAnalyticsCommand extends Command
{
    public function __construct(
        private readonly AnalyticsEngine $analyticsEngine,
        private readonly RankingService $rankingService,
        private readonly FightResultRepository $fightResultRepository,
        private readonly EntityManagerInterface $entityManager,
    ) {
        parent::__construct();
    }

    protected function execute(InputInterface $input, OutputInterface $output): int
    {
        $io = new SymfonyStyle($input, $output);

        $this->analyticsEngine->recomputeAll('CURRENT');
        $this->rankingService->recomputeRankings('CURRENT');

        $insertedStats = 0;
        $completed = $this->fightResultRepository->findByStatus('COMPLETED');

        foreach ($completed as $result) {
            $pairs = [
                $result->getFighterRed()->getId(),
                $result->getFighterBlue()->getId(),
            ];

            foreach ($pairs as $fighterId) {
                $existing = $this->entityManager->getRepository(FightStatistic::class)->findOneBy([
                    'fightResultId' => $result->getId(),
                    'fighterId' => $fighterId,
                ]);

                if ($existing !== null) {
                    continue;
                }

                $stat = (new FightStatistic())
                    ->setFightResultId((int) $result->getId())
                    ->setFighterId((int) $fighterId)
                    ->setStrikesLanded(0)
                    ->setStrikesAttempted(0)
                    ->setTakedownsLanded(0)
                    ->setTakedownsAttempted(0)
                    ->setSubmissionAttempts(0)
                    ->setKnockdowns(0)
                    ->setControlTimeSeconds(0);

                $this->entityManager->persist($stat);
                ++$insertedStats;
            }
        }

        $this->entityManager->flush();

        $io->success(sprintf(
            'Backfill completed. rankings/performance recomputed, %d fight_statistic rows inserted.',
            $insertedStats
        ));

        return Command::SUCCESS;
    }
}
