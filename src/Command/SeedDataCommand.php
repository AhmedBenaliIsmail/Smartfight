<?php
namespace App\Command;

use App\Entity\Event;
use App\Entity\Fighter;
use App\Entity\FightResult;
use App\Entity\FightStatistic;
use App\Entity\WeightDivision;
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
    description: 'Seeds the database with professional boxing data, weight divisions, real venues, and CompuBox statistics.',
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
        $io->title('Boxing System Data Seeding — Pro Edition');

        // 1. Truncate Tables
        $io->section('Cleaning existing data...');
        $this->truncateTables(['ranking', 'performance_score', 'fight_statistic', 'fight_results', 'events', 'fighters', 'weight_division']);

        // 2. Seed Weight Divisions
        $io->section('Creating standard 17 boxing weight divisions...');
        $divisions = $this->createWeightDivisions();

        // 3. Seed Fighters
        $io->section('Seeding elite boxers...');
        $fighters = $this->createFighters($divisions);
        
        // 4. Seed Events
        $io->section('Seeding real-world boxing events and venues...');
        $events = $this->createEvents();

        // 5. Seed Fights
        $io->section('Populating fight history and CompuBox statistics...');
        $this->createFights($fighters, $events);

        // 6. Final Recalculation
        $io->section('Recalculating Global Rankings & Boxing Points...');
        $this->rankingService->recomputeAllRankings();
        $this->analyticsEngine->recalculateAll();

        $io->success('Database seeded successfully with boxing events, weight divisions, and CompuBox stats!');

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

    private function createWeightDivisions(): array
    {
        $divs = [
            ['Heavyweight', 999, 'heavyweight'],
            ['Cruiserweight', 200, 'cruiserweight'],
            ['Light Heavyweight', 175, 'light-heavyweight'],
            ['Super Middleweight', 168, 'super-middleweight'],
            ['Middleweight', 160, 'middleweight'],
            ['Super Welterweight', 154, 'super-welterweight'],
            ['Welterweight', 147, 'welterweight'],
            ['Super Lightweight', 140, 'super-lightweight'],
            ['Lightweight', 135, 'lightweight'],
            ['Super Featherweight', 130, 'super-featherweight'],
            ['Featherweight', 126, 'featherweight'],
            ['Super Bantamweight', 122, 'super-bantamweight'],
            ['Bantamweight', 118, 'bantamweight'],
            ['Super Flyweight', 115, 'super-flyweight'],
            ['Flyweight', 112, 'flyweight'],
            ['Light Flyweight', 108, 'light-flyweight'],
            ['Minimumweight', 105, 'minimumweight'],
        ];

        $divisions = [];
        foreach ($divs as $d) {
            $wd = new WeightDivision();
            $wd->setName($d[0]);
            $wd->setMaxWeightLbs($d[1]);
            $wd->setSlug($d[2]);
            $this->em->persist($wd);
            $divisions[$d[0]] = $wd;
        }
        $this->em->flush();
        return $divisions;
    }

    private function createFighters(array $divisions): array
    {
        // [First, Last, Nick, WC, Nat, W, L, D, ELO, Ht, Reach]
        $data = [
            ['Tyson', 'Fury', 'The Gypsy King', 'Heavyweight', 'GB', 34, 1, 1, 1950, 206, 216],
            ['Oleksandr', 'Usyk', 'The Cat', 'Heavyweight', 'UA', 22, 0, 0, 2000, 191, 198],
            ['Anthony', 'Joshua', 'AJ', 'Heavyweight', 'GB', 28, 3, 0, 1880, 198, 208],
            ['Canelo', 'Alvarez', 'Canelo', 'Super Middleweight', 'MX', 60, 2, 2, 1980, 173, 179],
            ['David', 'Benavidez', 'The Mexican Monster', 'Super Middleweight', 'US', 28, 0, 0, 1850, 188, 189],
            ['Terence', 'Crawford', 'Bud', 'Welterweight', 'US', 40, 0, 0, 2050, 173, 188],
            ['Errol', 'Spence Jr.', 'The Truth', 'Welterweight', 'US', 28, 1, 0, 1860, 177, 183],
            ['Gervonta', 'Davis', 'Tank', 'Lightweight', 'US', 29, 0, 0, 1900, 166, 171],
            ['Devin', 'Haney', 'The Dream', 'Super Lightweight', 'US', 31, 0, 0, 1850, 173, 180],
            ['Naoya', 'Inoue', 'The Monster', 'Super Bantamweight', 'JP', 26, 0, 0, 2020, 165, 171],
        ];

        $fighters = [];
        foreach ($data as $f) {
            $entity = new Fighter();
            $entity->setFirstName($f[0]);
            $entity->setLastName($f[1]);
            $entity->setNickname($f[2]);
            $entity->setWeightDivision($divisions[$f[3]]);
            $entity->setNationality($f[4]);
            $entity->setWins($f[5]);
            $entity->setLosses($f[6]);
            $entity->setDraws($f[7]);
            $entity->setEloRating($f[8]);
            $entity->setHeight($f[9]);
            $entity->setReach($f[10]);
            $entity->setLastFightDate(new \DateTime('-2 months'));
            $this->em->persist($entity);
            $fighters[$f[0] . ' ' . $f[1]] = $entity;
        }
        $this->em->flush();
        return $fighters;
    }

    private function createEvents(): array
    {
        $events = [];
        // Name, Org, Date, Venue, City, Country, Seats
        $data = [
            ['Fury vs Usyk', 'UNDISPUTED', '-1 month', 'Kingdom Arena', 'Riyadh', 'SA', 20000],
            ['Canelo vs Benavidez', 'WBC', '-3 months', 'T-Mobile Arena', 'Las Vegas', 'US', 20000],
            ['Crawford vs Spence', 'UNDISPUTED', '-8 months', 'T-Mobile Arena', 'Las Vegas', 'US', 20000],
            ['Joshua vs Ngannou', 'INDEPENDENT', '-2 months', 'Kingdom Arena', 'Riyadh', 'SA', 20000],
            ['Inoue vs Nery', 'UNDISPUTED', '-15 days', 'Tokyo Dome', 'Tokyo', 'JP', 55000],
        ];

        foreach ($data as $i => $d) {
            $e = new Event();
            $e->setEventName($d[0]);
            $e->setOrganization($d[1]);
            $e->setEventDate(new \DateTime($d[2]));
            $e->setVenue($d[3]);
            $e->setCity($d[4]);
            $e->setCountry($d[5]);
            $e->setSeatCapacity($d[6]);
            $e->setStatus('COMPLETED');
            $this->em->persist($e);
            $events[$d[0]] = $e;
        }

        $this->em->flush();
        return $events;
    }

    private function createFights(array $f, array $e): void
    {
        // Usyk beats Fury by Split Decision
        $this->addResolvedFight(
            $e['Fury vs Usyk'], 1, $f['Tyson Fury'], $f['Oleksandr Usyk'], $f['Oleksandr Usyk'], 
            FightResult::METHOD_DECISION, 12, 'SD', 9, 12, true, 'UNDISPUTED',
            [
                'f1' => ['pl' => 157, 'pt' => 496, 'bpl' => 30, 'jl' => 45, 'jt' => 200, 'ppl' => 112, 'ppt' => 296, 'kd' => 0],
                'f2' => ['pl' => 170, 'pt' => 407, 'bpl' => 42, 'jl' => 48, 'jt' => 210, 'ppl' => 122, 'ppt' => 197, 'kd' => 1]
            ]
        );

        // Crawford beats Spence by TKO
        $this->addResolvedFight(
            $e['Crawford vs Spence'], 1, $f['Terence Crawford'], $f['Errol Spence Jr.'], $f['Terence Crawford'], 
            FightResult::METHOD_KO, 9, null, 9, 12, true, 'UNDISPUTED',
            [
                'f1' => ['pl' => 185, 'pt' => 369, 'bpl' => 20, 'jl' => 87, 'jt' => 200, 'ppl' => 98, 'ppt' => 169, 'kd' => 3],
                'f2' => ['pl' => 96, 'pt' => 480, 'bpl' => 25, 'jl' => 33, 'jt' => 200, 'ppl' => 63, 'ppt' => 280, 'kd' => 0]
            ]
        );
        
        // Canelo beats Benavidez by UD
        $this->addResolvedFight(
            $e['Canelo vs Benavidez'], 1, $f['Canelo Alvarez'], $f['David Benavidez'], $f['Canelo Alvarez'], 
            FightResult::METHOD_DECISION, 12, 'UD', null, 12, true, 'WBC',
            [
                'f1' => ['pl' => 234, 'pt' => 500, 'bpl' => 60, 'jl' => 50, 'jt' => 150, 'ppl' => 184, 'ppt' => 350, 'kd' => 0],
                'f2' => ['pl' => 190, 'pt' => 600, 'bpl' => 40, 'jl' => 80, 'jt' => 300, 'ppl' => 110, 'ppt' => 300, 'kd' => 0]
            ]
        );
    }

    private function addResolvedFight(
        Event $event, int $num, Fighter $f1, Fighter $f2, ?Fighter $winner, 
        string $method, int $round, ?string $decisionType, ?int $kdRound, int $scheduledRounds, 
        bool $isBeltFight, ?string $beltOrg, array $stats
    ): void {
        $fr = new FightResult();
        $fr->setEvent($event);
        $fr->setFightNumber($num);
        $fr->setFighter1($f1);
        $fr->setFighter2($f2);
        $fr->setWinner($winner);
        $fr->setMethodOfVictory($method);
        $fr->setRoundNumber($round);
        $fr->setDecisionType($decisionType);
        $fr->setKnockdownRound($kdRound);
        $fr->setScheduledRounds($scheduledRounds);
        $fr->setIsBeltFight($isBeltFight);
        $fr->setBeltOrganization($beltOrg);
        $fr->setFightDate($event->getEventDate());
        $fr->setStatus('COMPLETED');
        $this->em->persist($fr);

        // Stats F1
        $this->createTotalStats($fr, $f1, $stats['f1']);
        
        // Stats F2
        $this->createTotalStats($fr, $f2, $stats['f2']);
        
        $this->em->flush();
    }

    private function createTotalStats(FightResult $fr, Fighter $fighter, array $s): void
    {
        $stat = new FightStatistic();
        $stat->setFightResult($fr);
        $stat->setFighter($fighter);
        $stat->setRound(null); // NULL implies total fight stats

        $stat->setPunchesLanded($s['pl']);
        $stat->setPunchesThrown($s['pt']);
        $stat->setBodyShotsLanded($s['bpl']);
        
        $stat->setJabsLanded($s['jl']);
        $stat->setJabsThrown($s['jt']);

        $stat->setPowerPunchesLanded($s['ppl']);
        $stat->setPowerPunchesThrown($s['ppt']);

        $stat->setKnockdowns($s['kd']);

        $this->em->persist($stat);
    }

}

