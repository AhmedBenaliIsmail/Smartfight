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
    description: 'Seeds the database with professional boxing data, weight divisions, real venues, and SmartFight statistics.',
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

    protected function configure(): void
    {
        $this->addOption('force', 'f', \Symfony\Component\Console\Input\InputOption::VALUE_NONE, 'Force seeding even if data already exists');
    }

    protected function execute(InputInterface $input, OutputInterface $output): int
    {
        $io = new SymfonyStyle($input, $output);
        $io->title('Boxing System Data Seeding — Pro Edition');

        // Check if database is already seeded
        $force = $input->getOption('force');
        $existingFighters = $this->em->getRepository(\App\Entity\Fighter::class)->count([]);
        if ($existingFighters > 0 && !$force) {
            $io->warning('The database is already seeded! To protect your manually uploaded fighter photos and user accounts, the seeder will NOT run again. If you want to wipe the database, run with: php bin/console app:seed --force');
            return Command::SUCCESS;
        }

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

        // 5. Seed Fights with Per-Round SmartFight Stats
        $io->section('Populating fight history with round-by-round SmartFight statistics...');
        $fights = $this->createFights($fighters, $events);

        // 6. Seed Users & Predictions
        $io->section('Seeding users and generating fan predictions...');
        $this->createUsersAndPredictions($fights);

        // 7. Final Recalculation
        $io->section('Recalculating Global Rankings & Boxing Points...');
        $this->rankingService->recomputeAllRankings();
        $this->analyticsEngine->recalculateAll();

        $io->success('Database seeded successfully with boxing events, weight divisions, and SmartFight stats!');

        return Command::SUCCESS;
    }

    private function truncateTables(array $tables): void
    {
        $conn = $this->em->getConnection();
        $conn->executeStatement('SET FOREIGN_KEY_CHECKS = 0');
        foreach ($tables as $table) {
            $conn->executeStatement("TRUNCATE TABLE $table");
        }
        // Truncate all "Fan Experience" and auxiliary tables to prevent orphaned links
        $auxTables = [
            'fan_reaction', 'event_booking', 'fan_vote', 
            'notifications', 'blog_article', 'blog_category',
            'predictions', 'user_roles', 'users', 'match_proposal'
        ];
        foreach ($auxTables as $at) {
            try {
                $conn->executeStatement("TRUNCATE TABLE $at");
            } catch (\Exception $e) {
                // Skip if table doesn't exist yet
            }
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
        // [First, Last, Nick, WC, Nat, W, L, D, KO, Ht, Reach, Thrown, Landed]
        $data = [
            // Heavyweights (Real & Active)
            ['Oleksandr', 'Usyk', 'The Cat', 'Heavyweight', 'UA', 22, 0, 0, 14, 191, 198, 4800, 2300],
            ['Tyson', 'Fury', 'The Gypsy King', 'Heavyweight', 'GB', 34, 1, 1, 24, 206, 216, 5000, 2100],
            ['Anthony', 'Joshua', 'AJ', 'Heavyweight', 'GB', 28, 3, 0, 25, 198, 208, 4200, 1900],
            ['Daniel', 'Dubois', 'Dynamite', 'Heavyweight', 'GB', 21, 2, 0, 20, 196, 198, 3200, 1400],
            ['Zhilei', 'Zhang', 'Big Bang', 'Heavyweight', 'CN', 27, 2, 1, 22, 198, 198, 3800, 1600],
            ['Joseph', 'Parker', 'Joe', 'Heavyweight', 'NZ', 35, 3, 0, 23, 193, 193, 4100, 1800],
            ['Martin', 'Bakole', 'Bakole', 'Heavyweight', 'CD', 21, 1, 0, 16, 198, 203, 3100, 1300],
            ['Agit', 'Kabayel', 'Kabayel', 'Heavyweight', 'DE', 25, 0, 0, 17, 191, 191, 3500, 1500],

            // Light Heavyweights (Real & Active)
            ['Artur', 'Beterbiev', 'Artur', 'Light Heavyweight', 'RU', 20, 0, 0, 20, 182, 185, 3100, 1400],
            ['Dmitry', 'Bivol', 'Bivol', 'Light Heavyweight', 'RU', 23, 0, 0, 12, 183, 183, 4200, 1900],
            ['David', 'Benavidez', 'The Mexican Monster', 'Light Heavyweight', 'US', 29, 0, 0, 24, 188, 189, 4500, 2100],

            // Super Middleweights (Real & Active)
            ['Canelo', 'Alvarez', 'Canelo', 'Super Middleweight', 'MX', 61, 2, 2, 39, 173, 179, 7000, 3200],
            ['Caleb', 'Plant', 'Sweethands', 'Super Middleweight', 'US', 22, 2, 0, 13, 185, 188, 4100, 1700],
            ['Christian', 'Mbilli', 'Solid', 'Super Middleweight', 'FR', 27, 0, 0, 23, 174, 178, 3200, 1300],

            // Middleweights (Real & Active)
            ['Janibek', 'Alimkhanuly', 'Qazaq Style', 'Middleweight', 'KZ', 15, 0, 0, 10, 182, 182, 2800, 1100],
            ['Carlos', 'Adames', 'The Caballo', 'Middleweight', 'DO', 23, 1, 0, 18, 180, 180, 3100, 1300],

            // Welterweights & Super Welterweights (Real & Active)
            ['Terence', 'Crawford', 'Bud', 'Super Welterweight', 'US', 41, 0, 0, 31, 173, 188, 5200, 2400],
            ['Sebastian', 'Fundora', 'The Towering Inferno', 'Super Welterweight', 'US', 21, 1, 1, 13, 197, 203, 3500, 1500],
            ['Tim', 'Tszyu', 'The Soul Taker', 'Super Welterweight', 'AU', 24, 1, 0, 17, 174, 179, 4200, 1800],
            ['Jaron', 'Ennis', 'Boots', 'Welterweight', 'US', 32, 0, 0, 29, 178, 188, 3900, 1800],
            ['Errol', 'Spence Jr.', 'The Truth', 'Welterweight', 'US', 28, 1, 0, 22, 177, 183, 4500, 1900],

            // Lightweights & Super Lightweights (Real & Active)
            ['Gervonta', 'Davis', 'Tank', 'Lightweight', 'US', 30, 0, 0, 28, 166, 171, 3800, 1600],
            ['Shakur', 'Stevenson', 'Sugar', 'Lightweight', 'US', 22, 0, 0, 10, 173, 173, 4200, 1800],
            ['Vasiliy', 'Lomachenko', 'The Matrix', 'Lightweight', 'UA', 18, 3, 0, 12, 170, 166, 5500, 2600],
            ['Teofimo', 'Lopez', 'The Takeover', 'Super Lightweight', 'US', 21, 1, 0, 13, 173, 174, 3800, 1500],
            ['Devin', 'Haney', 'The Dream', 'Super Lightweight', 'US', 31, 0, 0, 15, 173, 180, 4500, 1900],
            ['Ryan', 'Garcia', 'KingRy', 'Super Lightweight', 'US', 24, 1, 0, 20, 174, 178, 3800, 1500],
            ['Isaac', 'Cruz', 'Pitbull', 'Super Lightweight', 'MX', 26, 2, 1, 18, 163, 160, 3200, 1400],

            // Lower Weight Classes (Real & Active Superstars)
            ['Naoya', 'Inoue', 'The Monster', 'Super Bantamweight', 'JP', 27, 0, 0, 24, 165, 171, 3500, 1700],
            ['Jesse', 'Rodriguez', 'Bam', 'Super Flyweight', 'US', 20, 0, 0, 13, 163, 160, 3200, 1400],
            ['Junto', 'Nakatani', 'Junto', 'Bantamweight', 'JP', 28, 0, 0, 21, 172, 170, 3500, 1600],
            ['Emanuel', 'Navarrete', 'El Vaquero', 'Super Featherweight', 'MX', 38, 2, 1, 31, 170, 183, 5500, 2200],
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
            $entity->setEloRating(500 + ($f[5] * 15) - ($f[6] * 25));
            $entity->setLastFightDate(new \DateTime('-' . rand(1, 6) . ' months'));
            $this->em->persist($entity);
        }
        $this->em->flush();
        return $fighters;
    }

    private function createEvents(): array
    {
        $events = [];
        $venues = [
            ['Kingdom Arena', 'Riyadh', 'SA', 22000],
            ['T-Mobile Arena', 'Las Vegas', 'US', 20000],
            ['BMO Stadium', 'Los Angeles', 'US', 22000],
            ['Wembley Stadium', 'London', 'GB', 90000],
            ['Tokyo Dome', 'Tokyo', 'JP', 55000],
            ['MGM Grand', 'Las Vegas', 'US', 16000],
            ['RAC Arena', 'Perth', 'AU', 15000],
            ['Madison Square Garden', 'New York', 'US', 20000],
            ['Barclays Center', 'Brooklyn', 'US', 19000],
            ['Etihad Arena', 'Abu Dhabi', 'AE', 18000],
        ];

        $orgs = ['WBC', 'WBA', 'IBF', 'WBO'];

        // Seed 30 events with realistic frequency
        for ($i = 0; $i < 30; $i++) {
            $v = $venues[array_rand($venues)];
            $e = new Event();
            
            // Randomly decide if this is a major "Champion Event" (10% chance)
            $isChampionEvent = (rand(1, 100) <= 10);
            
            if ($isChampionEvent) {
                $e->setEventName('World Championship #' . rand(1000, 9999));
                $e->setOrganization($orgs[array_rand($orgs)]);
            } else {
                $e->setEventName('Boxing Night: ' . $v[1] . ' Series');
                $e->setOrganization('INDEPENDENT');
            }
            
            // Random date in the last year or next 6 months
            $days = rand(-365, 180);
            $date = new \DateTime();
            $date->modify($days . ' days');
            $e->setEventDate($date);
            
            $e->setVenue($v[0]);
            $e->setCity($v[1]);
            $e->setCountry($v[2]);
            $e->setSeatCapacity($v[3]);
            $e->setStatus($days < 0 ? 'COMPLETED' : 'SCHEDULED');
            
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
            // Original logic: 3 fights per completed event
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
                    // Pick scheduled rounds: 10 or 12
                    $scheduledRounds = (rand(0, 1) === 0) ? 10 : 12;
                    // For KO fights, end early. For decision fights, go the distance.
                    $endRound = ($method === FightResult::METHOD_DECISION) ? $scheduledRounds : rand(3, $scheduledRounds);
                    $decisionType = ($method === FightResult::METHOD_DECISION) ? ['UD', 'SD', 'MD'][rand(0, 2)] : null;

                    $fr = $this->addResolvedFight(
                        $e, $i, $f1, $f2, $winner, $method, $endRound,
                        $decisionType, null, $scheduledRounds,
                        true, $org
                    );

                    // Generate per-round SmartFight stats for each fighter
                    $this->generateRoundByRoundStats($fr, $f1, $f2, $endRound);
                } else {
                    $fr = $this->addScheduledFight($e, $i, $f1, $f2);
                }
                $allFights[] = $fr;
            }
        }
        return $allFights;
    }

    /**
     * Generates realistic per-round SmartFight statistics for both fighters.
     * 
     * Enforces:
     *   - jabsThrown >= jabsLanded
     *   - powerPunchesThrown >= powerPunchesLanded
     *   - punchesThrown = jabsThrown + powerPunchesThrown
     *   - punchesLanded = jabsLanded + powerPunchesLanded
     *   - bodyShotsLanded = bodyJabsLanded + bodyPowerLanded
     *   - bodyJabsLanded <= jabsLanded
     *   - bodyPowerLanded <= powerPunchesLanded
     */
    private function generateRoundByRoundStats(FightResult $fr, Fighter $f1, Fighter $f2, int $endRound): void
    {
        // Determine fighter profiles — higher-volume vs counter-puncher
        $f1Profile = $this->getRandomFighterProfile();
        $f2Profile = $this->getRandomFighterProfile();

        for ($r = 1; $r <= $endRound; $r++) {
            $this->createRoundStat($fr, $f1, $r, $f1Profile);
            $this->createRoundStat($fr, $f2, $r, $f2Profile);
        }
        $this->em->flush();
    }

    /**
     * Returns a random fighter archetype for stat generation.
     * Each profile defines base ranges for punches per round.
     */
    private function getRandomFighterProfile(): array
    {
        $profiles = [
            // High-volume brawler (like Baumgardner)
            [
                'jabs_thrown' => [25, 45],
                'jabs_accuracy' => [0.20, 0.40],
                'power_thrown' => [28, 50],
                'power_accuracy' => [0.30, 0.60],
                'body_jab_ratio' => [0.00, 0.08],
                'body_power_ratio' => [0.05, 0.20],
            ],
            // Counter-puncher / defensive (like Shin)
            [
                'jabs_thrown' => [12, 25],
                'jabs_accuracy' => [0.08, 0.25],
                'power_thrown' => [30, 55],
                'power_accuracy' => [0.15, 0.50],
                'body_jab_ratio' => [0.05, 0.20],
                'body_power_ratio' => [0.10, 0.35],
            ],
            // Balanced boxer
            [
                'jabs_thrown' => [20, 38],
                'jabs_accuracy' => [0.25, 0.38],
                'power_thrown' => [25, 42],
                'power_accuracy' => [0.30, 0.50],
                'body_jab_ratio' => [0.00, 0.10],
                'body_power_ratio' => [0.05, 0.25],
            ],
            // Pressure fighter
            [
                'jabs_thrown' => [30, 50],
                'jabs_accuracy' => [0.22, 0.35],
                'power_thrown' => [35, 55],
                'power_accuracy' => [0.25, 0.45],
                'body_jab_ratio' => [0.02, 0.12],
                'body_power_ratio' => [0.08, 0.30],
            ],
        ];
        return $profiles[array_rand($profiles)];
    }

    private function createRoundStat(FightResult $fr, Fighter $fighter, int $round, array $profile): void
    {
        // Generate jabs: thrown then landed (landed <= thrown, guaranteed by floor)
        $jabsThrown = rand($profile['jabs_thrown'][0], $profile['jabs_thrown'][1]);
        $jabAccuracy = $this->randomFloat($profile['jabs_accuracy'][0], $profile['jabs_accuracy'][1]);
        $jabsLanded = (int)floor($jabsThrown * $jabAccuracy);

        // Generate power punches: thrown then landed (landed <= thrown)
        $powerThrown = rand($profile['power_thrown'][0], $profile['power_thrown'][1]);
        $powerAccuracy = $this->randomFloat($profile['power_accuracy'][0], $profile['power_accuracy'][1]);
        $powerLanded = (int)floor($powerThrown * $powerAccuracy);

        // Total = Jabs + Power (by construction)
        $totalThrown = $jabsThrown + $powerThrown;
        $totalLanded = $jabsLanded + $powerLanded;

        // Body shots: subset of landed punches
        $bodyJabRatio = $this->randomFloat($profile['body_jab_ratio'][0], $profile['body_jab_ratio'][1]);
        $bodyPowerRatio = $this->randomFloat($profile['body_power_ratio'][0], $profile['body_power_ratio'][1]);
        $bodyJabsLanded = (int)floor($jabsLanded * $bodyJabRatio);
        $bodyPowerLanded = (int)floor($powerLanded * $bodyPowerRatio);
        $bodyShotsLanded = $bodyJabsLanded + $bodyPowerLanded;

        // Uppercuts: subset of power punches
        $uppercutsThrown = (int)floor($powerThrown * $this->randomFloat(0.1, 0.3));
        $uppercutsLanded = min($uppercutsThrown, (int)floor($powerLanded * $this->randomFloat(0.1, 0.4)));

        // Left vs Right Hand logic
        // Strict requirement: Left + Right == Total
        $leftHandThrown = (int)floor($totalThrown * $this->randomFloat(0.5, 0.6)); // Jabs usually push this > 50%
        $rightHandThrown = $totalThrown - $leftHandThrown;

        $leftHandLanded = min($leftHandThrown, (int)floor($totalLanded * 0.5));
        $remainingLanded = $totalLanded - $leftHandLanded;

        if ($remainingLanded > $rightHandThrown) {
            $rightHandLanded = $rightHandThrown;
            $leftHandLanded += ($remainingLanded - $rightHandThrown); // Safe because totalLanded <= totalThrown
        } else {
            $rightHandLanded = $remainingLanded;
        }

        // Knockdowns: very rare
        $knockdowns = (rand(1, 100) <= 3) ? 1 : 0;

        $s = new FightStatistic();
        $s->setFightResult($fr);
        $s->setFighter($fighter);
        $s->setRound($round);
        $s->setPunchesThrown($totalThrown);
        $s->setPunchesLanded($totalLanded);
        $s->setJabsThrown($jabsThrown);
        $s->setJabsLanded($jabsLanded);
        $s->setPowerPunchesThrown($powerThrown);
        $s->setPowerPunchesLanded($powerLanded);
        $s->setBodyShotsLanded($bodyShotsLanded);
        $s->setBodyJabsLanded($bodyJabsLanded);
        $s->setBodyPowerLanded($bodyPowerLanded);
        
        $s->setUppercutsThrown($uppercutsThrown);
        $s->setUppercutsLanded($uppercutsLanded);
        $s->setLeftHandThrown($leftHandThrown);
        $s->setLeftHandLanded($leftHandLanded);
        $s->setRightHandThrown($rightHandThrown);
        $s->setRightHandLanded($rightHandLanded);
        
        $s->setKnockdowns($knockdowns);

        $this->em->persist($s);
    }

    /**
     * Returns a random float between min and max.
     */
    private function randomFloat(float $min, float $max): float
    {
        return $min + mt_rand() / mt_getrandmax() * ($max - $min);
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
        bool $isBeltFight, ?string $beltOrg
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
        $this->em->flush();
        return $fr;
    }
}
