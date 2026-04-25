<?php

namespace App\Command;

use App\Service\EventStatusService;
use Symfony\Component\Console\Attribute\AsCommand;
use Symfony\Component\Console\Command\Command;
use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Output\OutputInterface;
use Symfony\Component\Console\Style\SymfonyStyle;

#[AsCommand(
    name: 'app:events:refresh-status',
    description: 'Refresh all event statuses based on current date/time (bypasses throttle, suitable for cron)',
)]
class RefreshEventStatusCommand extends Command
{
    public function __construct(private readonly EventStatusService $eventStatusService)
    {
        parent::__construct();
    }

    protected function execute(InputInterface $input, OutputInterface $output): int
    {
        $io = new SymfonyStyle($input, $output);

        $updated = $this->eventStatusService->refreshAndUpdateMeta();

        if ($updated === 0) {
            $io->info('All event statuses are already up to date.');
        } else {
            $io->success(sprintf('Updated %d event %s.', $updated, $updated === 1 ? 'status' : 'statuses'));
        }

        return Command::SUCCESS;
    }
}
