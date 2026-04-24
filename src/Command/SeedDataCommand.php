<?php
namespace App\Command;

use App\Entity\Event;
use App\Entity\Fighter;
use App\Entity\FightResult;
use App\Entity\FightStatistic;
use App\Entity\Prediction;
use App\Entity\Role;
use App\Entity\User;
use App\Entity\WeightDivision;
use App\Repository\EventRepository;
use App\Repository\FighterRepository;
use App\Repository\FightResultRepository;
use App\Service\RankingService;
use App\Service\AnalyticsEngine;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;
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
        private AnalyticsEngine $analyticsEngine,
        private UserPasswordHasherInterface $hasher
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
        $fights = $this->createFights($fighters, $events);

        // 6. Seed Users & Predictions
        $io->section('Seeding users and generating fan predictions...');
        $this->createUsersAndPredictions($fights);

        // 7. Final Recalculation
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
        // Also truncate users and predictions for a clean test
        $conn->executeStatement("TRUNCATE TABLE predictions");
        $conn->executeStatement("TRUNCATE TABLE users");
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
        // [First, Last, Nick, WC, Nat, W, L, D, KO, Ht, Reach, Thrown, Landed]
        $data = [
            // Heavyweights
            ['Tyson', 'Fury', 'The Gypsy King', 'Heavyweight', 'GB', 34, 1, 1, 24, 206, 216, 5000, 2100],
            ['Oleksandr', 'Usyk', 'The Cat', 'Heavyweight', 'UA', 22, 0, 0, 14, 191, 198, 4800, 2300],
            ['Anthony', 'Joshua', 'AJ', 'Heavyweight', 'GB', 28, 3, 0, 25, 198, 208, 4200, 1900],
            ['Deontay', 'Wilder', 'The Bronze Bomber', 'Heavyweight', 'US', 43, 3, 1, 42, 201, 211, 3500, 1100],
            ['Zhilei', 'Zhang', 'Big Bang', 'Heavyweight', 'CN', 27, 2, 1, 22, 198, 198, 3800, 1600],
            ['Joseph', 'Parker', 'Joe', 'Heavyweight', 'NZ', 35, 3, 0, 23, 193, 193, 4100, 1800],

            // Middleweights
            ['Canelo', 'Alvarez', 'Canelo', 'Middleweight', 'MX', 61, 2, 2, 39, 173, 179, 7000, 3200],
            ['Gennadiy', 'Golovkin', 'GGG', 'Middleweight', 'KZ', 42, 2, 1, 37, 179, 178, 6500, 3000],
            ['Jermall', 'Charlo', 'Hitman', 'Middleweight', 'US', 33, 0, 0, 22, 183, 185, 4200, 1700],
            ['Janibek', 'Alimkhanuly', 'Qazaq Style', 'Middleweight', 'KZ', 15, 0, 0, 10, 182, 182, 2800, 1100],
            ['Carlos', 'Adames', 'The Caballo', 'Middleweight', 'DO', 23, 1, 0, 18, 180, 180, 3100, 1300],

            // Lightweights
            ['Gervonta', 'Davis', 'Tank', 'Lightweight', 'US', 30, 0, 0, 28, 166, 171, 3800, 1600],
            ['Vasiliy', 'Lomachenko', 'The Matrix', 'Lightweight', 'UA', 18, 3, 0, 12, 170, 166, 5500, 2600],
            ['Shakur', 'Stevenson', 'Sugar', 'Lightweight', 'US', 21, 0, 0, 10, 173, 173, 4200, 1800],
            ['William', 'Zepeda', 'Camaron', 'Lightweight', 'MX', 30, 0, 0, 26, 175, 175, 4500, 1900],
            ['Isaac', 'Cruz', 'Pitbull', 'Lightweight', 'MX', 26, 2, 1, 18, 163, 160, 3200, 1400],
            ['Ryan', 'Garcia', 'KingRy', 'Lightweight', 'US', 24, 1, 0, 20, 174, 178, 3800, 1500],

            // Welterweights
            ['Terence', 'Crawford', 'Bud', 'Welterweight', 'US', 40, 0, 0, 31, 173, 188, 5200, 2400],
            ['Errol', 'Spence Jr.', 'The Truth', 'Welterweight', 'US', 28, 1, 0, 22, 177, 183, 4500, 1900],
            ['Jaron', 'Ennis', 'Boots', 'Welterweight', 'US', 31, 0, 0, 28, 178, 188, 3900, 1800],
            ['Eimantas', 'Stanionis', 'Stanionis', 'Welterweight', 'LT', 15, 0, 0, 9, 173, 173, 2500, 1100],

            // Featherweights
            ['Naoya', 'Inoue', 'The Monster', 'Super Bantamweight', 'JP', 27, 0, 0, 24, 165, 171, 3500, 1700],
            ['Luis', 'Nery', 'Pantera', 'Super Bantamweight', 'MX', 35, 2, 0, 27, 165, 169, 3200, 1500],
            ['Stephen', 'Fulton', 'Cool Boy Steph', 'Super Bantamweight', 'US', 21, 1, 0, 8, 169, 179, 2900, 1200],
            ['Murodjon', 'Akhmadaliev', 'MJ', 'Super Bantamweight', 'UZ', 12, 1, 0, 9, 166, 173, 2100, 900],
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
            $entity->setKoWins($f[8]);
            $entity->setHeight($f[9]);
            $entity->setReach($f[10]);
            $entity->setStrikesThrown($f[11]);
            $entity->setStrikesLanded($f[12]);
            $entity->setEloRating(1500 + ($f[5] * 20) - ($f[6] * 30));
            $entity->setLastFightDate(new \DateTime('-' . rand(1, 6) . ' months'));
            $this->em->persist($entity);
            $fighters[$f[0] . ' ' . $f[1]] = $entity;
        }
        $this->em->flush();
        return $fighters;
    }

    private function createEvents(): array
    {
        $events = [];
        // Naming pattern: SmartFight #XXXX
        // Date, Venue, City, Country, Seats, Status, Organization
        $data = [
            ['-1 month', 'Kingdom Arena', 'Riyadh', 'SA', 22000, 'COMPLETED', 'WBC'],
            ['-20 days', 'T-Mobile Arena', 'Las Vegas', 'US', 20000, 'COMPLETED', 'WBA'],
            ['+2 months', 'BMO Stadium', 'Los Angeles', 'US', 22000, 'SCHEDULED', 'IBF'],
            ['+4 months', 'Wembley Stadium', 'London', 'GB', 90000, 'SCHEDULED', 'WBO'],
            ['-10 days', 'Tokyo Dome', 'Tokyo', 'JP', 55000, 'COMPLETED', 'WBC'],
            ['+1 month', 'MGM Grand', 'Las Vegas', 'US', 16000, 'SCHEDULED', 'INDEPENDENT'],
            ['-15 days', 'RAC Arena', 'Perth', 'AU', 15000, 'COMPLETED', 'IBF'],
            ['-1 year', 'T-Mobile Arena', 'Las Vegas', 'US', 20000, 'COMPLETED', 'WBA'],
        ];

        foreach ($data as $i => $d) {
            $e = new Event();
            $e->setEventName('Event #' . rand(1000, 9999));
            $e->setEventDate(new \DateTime($d[0]));
            $e->setVenue($d[1]);
            $e->setCity($d[2]);
            $e->setCountry($d[3]);
            $e->setSeatCapacity($d[4]);
            $e->setStatus($d[5]);
            $e->setOrganization($d[6]);
            $this->em->persist($e);
            $events[] = $e;
        }

        $this->em->flush();
        return $events;
    }

    private function createFights(array $f, array $events): array
    {
        $fighters = array_values($f);
        $allFights = [];
        
        foreach ($events as $e) {
            $org = $e->getOrganization();
            $numFights = ($e->getStatus() === 'COMPLETED') ? 3 : 1;
            
            $usedInEvent = [];
            for ($i = 1; $i <= $numFights; $i++) {
                shuffle($fighters);
                $f1 = null; $f2 = null;
                foreach ($fighters as $fighter) {
                    if (!in_array($fighter->getFighterId(), $usedInEvent)) {
                        if (!$f1) $f1 = $fighter;
                        elseif (!$f2) $f2 = $fighter;
                    }
                    if ($f1 && $f2) break;
                }

                if (!$f1 || !$f2) continue;
                $usedInEvent[] = $f1->getFighterId();
                $usedInEvent[] = $f2->getFighterId();

                if ($e->getStatus() === 'COMPLETED') {
                    $winner = (rand(0, 1) === 0) ? $f1 : $f2;
                    $method = (rand(0, 5) > 1) ? FightResult::METHOD_KO : FightResult::METHOD_DECISION;
                    $fr = $this->addResolvedFight($e, $i, $f1, $f2, $winner, $method, rand(1, 12), ($method === FightResult::METHOD_DECISION ? 'UD' : null), null, 12, true, $org,
                        ['f1' => ['pl' => rand(50, 150), 'pt' => rand(200, 500), 'bpl' => rand(10, 40), 'jl' => rand(10, 50), 'jt' => rand(50, 150), 'ppl' => rand(40, 100), 'ppt' => rand(150, 350), 'kd' => rand(0, 2)],
                         'f2' => ['pl' => rand(50, 150), 'pt' => rand(200, 500), 'bpl' => rand(10, 40), 'jl' => rand(10, 50), 'jt' => rand(50, 150), 'ppl' => rand(40, 100), 'ppt' => rand(150, 350), 'kd' => rand(0, 2)]]);
                } else {
                    $fr = $this->addScheduledFight($e, $i, $f1, $f2);
                }
                $allFights[] = $fr;
            }
        }
        return $allFights;
    }

    private function createUsersAndPredictions(array $fights): void
    {
        // 1. Create Roles
        $adminRole = new Role();
        $adminRole->setRoleName('ADMIN');
        $this->em->persist($adminRole);

        $userRole = new Role();
        $userRole->setRoleName('USER');
        $this->em->persist($userRole);
        $this->em->flush();

        $fanData = [
            ['john_doe', 'john@example.com', 'john123'],
            ['jane_smith', 'jane@example.com', 'jane123'],
            ['mike_tyson', 'mike@example.com', 'mike123'],
            ['ali_fan', 'ali@example.com', 'ali123'],
            ['boxerfan1', 'fan1@smartfight.com', 'fan123'],
            ['boxerfan2', 'fan2@smartfight.com', 'fan123'],
            ['mahdi', 'mahdi@smartfight.com', 'mahdi'],
        ];

        // Add Admin
        $admin = new User();
        $admin->setUsername('admin');
        $admin->setEmail('admin@smartfight.com');
        $admin->addRole($adminRole);
        $admin->setPassword($this->hasher->hashPassword($admin, 'admin123'));
        $this->em->persist($admin);

        $fans = [];
        foreach ($fanData as $data) {
            $user = new User();
            $user->setUsername($data[0]);
            $user->setEmail($data[1]);
            
            // Special case: make mahdi an admin too
            if ($data[0] === 'mahdi') {
                $user->addRole($adminRole);
            } else {
                $user->addRole($userRole);
            }
            
            $user->setPassword($this->hasher->hashPassword($user, $data[2]));
            $this->em->persist($user);
            $fans[] = $user;
        }
        $this->em->flush();

        // Generate Random Predictions
        foreach ($fans as $fan) {
            // Predict on 50% of fights
            $sampledFights = array_filter($fights, fn() => rand(0, 1) === 1);
            foreach ($sampledFights as $fight) {
                $p = new Prediction();
                $p->setUser($fan);
                $p->setFight($fight);
                $p->setPredictedWinner(rand(0, 1) === 0 ? $fight->getFighter1() : $fight->getFighter2());
                $p->setPredictedMethod(rand(0, 1) === 0 ? 'KO' : 'DECISION');
                $p->setPredictedRound(rand(1, 12));
                
                if ($fight->getStatus() === 'COMPLETED') {
                    $p->setIsProcessed(true);
                    $p->setPointsAwarded(rand(0, 10) * 10);
                }
                
                $this->em->persist($p);
            }
        }
        $this->em->flush();
    }

    private function addScheduledFight(Event $event, int $num, Fighter $f1, Fighter $f2): FightResult
    {
        $fr = new FightResult();
        $fr->setEvent($event);
        $fr->setFightNumber($num);
        $fr->setFighter1($f1);
        $fr->setFighter2($f2);
        $fr->setFightDate($event->getEventDate());
        $fr->setStatus('SCHEDULED');
        $fr->setScheduledRounds(12);
        $this->em->persist($fr);
        $this->em->flush();
        return $fr;
    }

    private function addResolvedFight(
        Event $event, int $num, Fighter $f1, Fighter $f2, ?Fighter $winner, 
        string $method, int $round, ?string $decisionType, ?int $kdRound, int $scheduledRounds, 
        bool $isBeltFight, ?string $beltOrg, array $stats
    ): FightResult {
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
        return $fr;
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

