<?php
namespace App\Command;

use App\Entity\Event;
use App\Repository\EventRepository;
use App\Repository\FightResultRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Component\Console\Attribute\AsCommand;
use Symfony\Component\Console\Command\Command;
use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Output\OutputInterface;
use Symfony\Component\Console\Style\SymfonyStyle;

#[AsCommand(
    name: 'app:update-event-status',
    description: 'Updates event statuses (SCHEDULED -> LIVE, LIVE -> COMPLETED).',
)]
class UpdateEventStatusCommand extends Command
{
    public function __construct(
        private EntityManagerInterface $em,
        private EventRepository $eventRepo,
        private FightResultRepository $resultRepo
    ) {
        parent::__construct();
    }

    protected function execute(InputInterface $input, OutputInterface $output): int
    {
        $io = new SymfonyStyle($input, $output);
        $today = new \DateTime('today');
        
        $events = $this->eventRepo->findAll();
        $updatedCount = 0;

        foreach ($events as $event) {
            $status = $event->getStatus();
            if ($status === 'CANCELLED' || $status === 'COMPLETED') {
                continue;
            }

            $eventDate = $event->getEventDate();
            if (!$eventDate) continue;

            $newStatus = $status;

            if ($status === 'SCHEDULED') {
                // If today is the event date or past it, it goes LIVE
                if ($eventDate->format('Y-m-d') <= $today->format('Y-m-d')) {
                    $newStatus = 'LIVE';
                }
            }

            if ($newStatus === 'LIVE') {
                // Check if all fights are COMPLETED or CANCELLED
                $fights = $this->resultRepo->findByEvent($event->getEventId());
                if (!empty($fights)) {
                    $allDone = true;
                    foreach ($fights as $fight) {
                        if ($fight->getStatus() === 'SCHEDULED') {
                            $allDone = false;
                            break;
                        }
                    }
                    if ($allDone) {
                        $newStatus = 'COMPLETED';
                    }
                }
            }

            if ($newStatus !== $status) {
                $event->setStatus($newStatus);
                $this->em->persist($event);
                $updatedCount++;
                $io->text(sprintf('Event "%s" changed from %s to %s', $event->getEventName(), $status, $newStatus));
            }
        }

        if ($updatedCount > 0) {
            $this->em->flush();
            $io->success(sprintf('Successfully updated %d events.', $updatedCount));
        } else {
            $io->success('No events needed status updates.');
        }

        return Command::SUCCESS;
    }
}
