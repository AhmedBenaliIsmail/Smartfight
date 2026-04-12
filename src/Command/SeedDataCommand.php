<?php
namespace App\Command;

use App\Entity\Event;
use App\Entity\Fighter;
use App\Entity\FightResult;
use App\Entity\FightStatistic;
use App\Repository\EventRepository;
use App\Repository\FighterRepository;
use App\Repository\FightResultRepository;
use App\Service\RankingService;
use App\Service\AnalyticsEngine;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Component\Console\Attribute\AsCommand;
use Symfony\Component\Console\Command\Command;
use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Output\OutputInterface;
use Symfony\Component\Console\Style\SymfonyStyle;

#[AsCommand(
    name: 'app:seed',
    description: 'Seeds the database with realistic MMA data, physical attributes, unique events, and detailed statistics.',
)]
class SeedDataCommand extends Command
{
    public function __construct(
        private EntityManagerInterface $em,
        private RankingService $rankingService,
        private AnalyticsEngine $analyticsEngine
    ) {
        parent::__construct();
    }

    protected function execute(InputInterface $input, OutputInterface $output): int
    {
        $io = new SymfonyStyle($input, $output);
        $io->title('MMA System Data Seeding — Pro Edition');

        // 1. Truncate Tables
        $io->section('Cleaning existing data...');
        $this->truncateTables(['ranking', 'performance_score', 'fight_statistic', 'fight_results', 'events', 'fighters']);

        // 2. Seed Fighters
        $io->section('Seeding elite fighters with physical attributes...');
        $fighters = $this->createFighters();
        
        // 3. Seed Events
        $io->section('Seeding unique events (Event 1, 2, 3, etc.)...');
        $events = $this->createEvents();

        // 4. Seed Fights
        $io->section('Populating fight history and real-world statistics...');
        $this->createFights($fighters, $events);

        // 5. Final Recalculation
        $io->section('Recalculating Global Rankings & Elo...');
        $this->rankingService->recomputeAllRankings();
        $this->analyticsEngine->recalculateAll();

        $io->success('Database seeded successfully with unique events, physical stats, and a DQ penalty example!');

        return Command::SUCCESS;
    }

    private function truncateTables(array $tables): void
    {
        $conn = $this->em->getConnection();
        $conn->executeStatement('SET FOREIGN_KEY_CHECKS = 0');
        foreach ($tables as $table) {
            $conn->executeStatement("TRUNCATE TABLE $table");
        }
        $conn->executeStatement('SET FOREIGN_KEY_CHECKS = 1');
    }

    private function createFighters(): array
    {
        // [First, Last, Nick, WC, Country, W, L, D, ELO, Ht, Reach]
        $data = [
            ['Jon', 'Jones', 'Bones', 'Heavyweight', 'USA', 27, 1, 0, 1950, 193, 215],
            ['Francis', 'Ngannou', 'The Predator', 'Heavyweight', 'Cameroon', 17, 3, 0, 1880, 193, 211],
            ['Israel', 'Adesanya', 'The Last Stylebender', 'Middleweight', 'Nigeria', 24, 3, 0, 1820, 193, 203],
            ['Alex', 'Pereira', 'Poatan', 'Middleweight', 'Brazil', 10, 2, 0, 1850, 193, 200],
            ['Islam', 'Makhachev', '', 'Lightweight', 'Russia', 25, 1, 0, 1920, 178, 179],
            ['Khabib', 'Nurmagomedov', 'The Eagle', 'Lightweight', 'Russia', 29, 0, 0, 2000, 178, 178],
            ['Leon', 'Edwards', 'Rocky', 'Welterweight', 'UK', 22, 3, 0, 1840, 183, 188],
            ['Kamaru', 'Usman', 'The Nigerian Nightmare', 'Welterweight', 'Nigeria', 20, 4, 0, 1780, 183, 193],
            ['Alexander', 'Volkanovski', 'The Great', 'Featherweight', 'Australia', 26, 4, 0, 1860, 168, 182],
            ['Max', 'Holloway', 'Blessed', 'Featherweight', 'USA', 26, 7, 0, 1790, 180, 175],
            ['Aljamain', 'Sterling', 'Funk Master', 'Bantamweight', 'USA', 23, 4, 0, 1720, 170, 180],
            ['Sean', 'O\'Malley', 'Suga', 'Bantamweight', 'USA', 18, 1, 0, 1750, 180, 183],
        ];

        $fighters = [];
        foreach ($data as $f) {
            $entity = new Fighter();
            $entity->setFirstName($f[0]);
            $entity->setLastName($f[1]);
            $entity->setNickname($f[2]);
            $entity->setWeightClass($f[3]);
            $entity->setCountry($f[4]);
            $entity->setWins($f[5]);
            $entity->setLosses($f[6]);
            $entity->setDraws($f[7]);
            $entity->setEloRating($f[8]);
            $entity->setHeight($f[9]);
            $entity->setReach($f[10]);
            $this->em->persist($entity);
            $fighters[$f[0] . ' ' . $f[1]] = $entity;
        }
        $this->em->flush();
        return $fighters;
    }

    private function createEvents(): array
    {
        $events = [];
        $eventNames = ['Event 1', 'Event 2', 'Event 3', 'Event 100', 'Event 500'];
        $dates = ['-10 months', '-8 months', '-5 months', '-2 months', '-1 month'];
        $locations = ['Las Vegas, NV', 'London, UK', 'Abu Dhabi, UAE', 'Paris, France', 'Rio, Brazil'];

        foreach ($eventNames as $i => $name) {
            $e = new Event();
            $e->setEventName($name);
            $e->setEventDate(new \DateTime($dates[$i]));
            $e->setLocation($locations[$i]);
            // Make Event 100 and 500 Champions Events
            $e->setIsChampionsEvent(in_array($name, ['Event 100', 'Event 500']));
            $this->em->persist($e);
            $events[$name] = $e;
        }

        $this->em->flush();
        return $events;
    }

    private function createFights(array $f, array $e): void
    {
        // Event 1 (Regular)
        $this->addResolvedFight($e['Event 1'], 1, $f['Khabib Nurmagomedov'], $f['Aljamain Sterling'], $f['Khabib Nurmagomedov'], 'SUBMISSION', 2, [
            'f1' => ['sl' => 45, 'st' => 60, 'tdl' => 4, 'tda' => 6, 'ct' => 420],
            'f2' => ['sl' => 22, 'st' => 50, 'tdl' => 0, 'tda' => 1, 'ct' => 30]
        ]);

        // Event 2 (Regular) - DQ Loss Example
        // Aljamain wins by DQ against Petr Yan (proxying here)
        $this->addResolvedFight($e['Event 2'], 1, $f['Aljamain Sterling'], $f['Sean O\'Malley'], $f['Aljamain Sterling'], 'DISQUALIFICATION', 4, [
            'f1' => ['sl' => 35, 'st' => 100, 'tdl' => 1, 'tda' => 8, 'ct' => 120, 'kd' => 0],
            'f2' => ['sl' => 78, 'st' => 120, 'tdl' => 0, 'tda' => 0, 'ct' => 10, 'kd' => 1] // Sean was winning but got DQ'd
        ]);

        // Event 100 (Champions Event)
        $this->addResolvedFight($e['Event 100'], 1, $f['Jon Jones'], $f['Francis Ngannou'], $f['Jon Jones'], 'DECISION', 5, [
            'f1' => ['sl' => 110, 'st' => 200, 'tdl' => 3, 'tda' => 7, 'ct' => 600, 'kd' => 0],
            'f2' => ['sl' => 85, 'st' => 180, 'tdl' => 0, 'tda' => 1, 'ct' => 40, 'kd' => 1]
        ]);

        // Event 500 (Champions Event)
        $this->addResolvedFight($e['Event 500'], 1, $f['Alex Pereira'], $f['Israel Adesanya'], $f['Alex Pereira'], 'KO/TKO', 2, [
            'f1' => ['sl' => 42, 'st' => 90, 'tdl' => 0, 'tda' => 0, 'ct' => 15, 'kd' => 1],
            'f2' => ['sl' => 38, 'st' => 85, 'tdl' => 1, 'tda' => 2, 'ct' => 180, 'kd' => 0]
        ]);
        
        $this->addResolvedFight($e['Event 500'], 2, $f['Islam Makhachev'], $f['Leon Edwards'], $f['Islam Makhachev'], 'DECISION', 5, [
            'f1' => ['sl' => 75, 'st' => 140, 'tdl' => 6, 'tda' => 10, 'ct' => 800],
            'f2' => ['sl' => 92, 'st' => 150, 'tdl' => 0, 'tda' => 1, 'ct' => 60]
        ]);
    }

    private function addResolvedFight(Event $event, int $num, Fighter $f1, Fighter $f2, ?Fighter $winner, string $method, int $round, array $stats): void
    {
        $fr = new FightResult();
        $fr->setEventId($event->getEventId());
        $fr->setFightNumber($num);
        $fr->setFighter1Id($f1->getFighterId());
        $fr->setFighter2Id($f2->getFighterId());
        $fr->setWinnerId($winner ? $winner->getFighterId() : null);
        $fr->setMethodOfVictory($method);
        $fr->setRoundNumber($round);
        $fr->setFightDate($event->getEventDate());
        $fr->setStatus('COMPLETED');
        $this->em->persist($fr);
        $this->em->flush();

        // Stats F1
        $s1 = new FightStatistic();
        $s1->setFightResultId($fr->getResultId());
        $s1->setFighterId($f1->getFighterId());
        $s1->setStrikesLanded($stats['f1']['sl']);
        $s1->setStrikesThrown($stats['f1']['st']);
        $s1->setTakedowns($stats['f1']['tdl']);
        $s1->setTakedownAttempts($stats['f1']['tda']);
        $s1->setKnockdowns($stats['f1']['kd'] ?? 0);
        $s1->setControlTimeSeconds($stats['f1']['ct']);
        $this->em->persist($s1);

        // Stats F2
        $s2 = new FightStatistic();
        $s2->setFightResultId($fr->getResultId());
        $s2->setFighterId($f2->getFighterId());
        $s2->setStrikesLanded($stats['f2']['sl']);
        $s2->setStrikesThrown($stats['f2']['st']);
        $s2->setTakedowns($stats['f2']['tdl']);
        $s2->setTakedownAttempts($stats['f2']['tda']);
        $s2->setKnockdowns($stats['f2']['kd'] ?? 0);
        $s2->setControlTimeSeconds($stats['f2']['ct']);
        $this->em->persist($s2);
        
        $this->em->flush();
    }
}
