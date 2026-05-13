<?php
namespace App\Command;

use App\Entity\BlogArticle;
use App\Entity\BlogCategory;
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

        // 7. Seed Blog content (after users so admin author exists)
        $io->section('Seeding blog categories and fight articles...');
        $this->createBlogContent($events);

        // 8. Final Recalculation
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

        // 9 poster events — all SCHEDULED, all upcoming (future dates, no past)
        $posterEvents = [
            [
                'name'   => 'Joshua vs. Ngannou: Riyadh Season',
                'org'    => 'WBO',
                'venue'  => 'Kingdom Arena',          'city' => 'Riyadh',    'country' => 'SA', 'cap' => 22000,
                'date'   => '+15 days',
                'poster' => 'anthony-joshua-vs-francis-ngannou-landscape.webp',
            ],
            [
                'name'   => 'Fury vs. Wilder III: The Trilogy',
                'org'    => 'WBC',
                'venue'  => 'T-Mobile Arena',         'city' => 'Las Vegas', 'country' => 'US', 'cap' => 20000,
                'date'   => '+30 days',
                'poster' => 'fury wilder poster.jpg',
            ],
            [
                'name'   => 'Davis vs. Garcia: Lightweight Unification',
                'org'    => 'WBA',
                'venue'  => 'T-Mobile Arena',         'city' => 'Las Vegas', 'country' => 'US', 'cap' => 20000,
                'date'   => '+45 days',
                'poster' => 'gervonta-kingry-davis-garcia-tmobile.jpeg',
            ],
            [
                'name'   => 'Zhang vs. Wilder: Riyadh Knockout Night',
                'org'    => 'WBO',
                'venue'  => 'Kingdom Arena',          'city' => 'Riyadh',    'country' => 'SA', 'cap' => 22000,
                'date'   => '+60 days',
                'poster' => 'poster1.jfif',
            ],
            [
                'name'   => 'Beterbiev vs. Bivol: Light Heavyweight Summit',
                'org'    => 'IBF',
                'venue'  => 'Kingdom Arena',          'city' => 'Riyadh',    'country' => 'SA', 'cap' => 22000,
                'date'   => '+75 days',
                'poster' => 'poster2.avif',
            ],
            [
                'name'   => 'GGG vs. Canelo II: The Rematch',
                'org'    => 'WBC',
                'venue'  => 'T-Mobile Arena',         'city' => 'Las Vegas', 'country' => 'US', 'cap' => 20000,
                'date'   => '+90 days',
                'poster' => 'poster3.jpg',
            ],
            [
                'name'   => 'Lomachenko vs. Martinez: WBO Lightweight Title',
                'org'    => 'WBO',
                'venue'  => 'Madison Square Garden',  'city' => 'New York',  'country' => 'US', 'cap' => 20000,
                'date'   => '+105 days',
                'poster' => 'poster4.jpg',
            ],
            [
                'name'   => 'Dubois vs. Wardley: British Heavyweight Showdown',
                'org'    => 'IBF',
                'venue'  => 'Tottenham Hotspur Stadium', 'city' => 'London', 'country' => 'GB', 'cap' => 32000,
                'date'   => '+120 days',
                'poster' => 'poster5.jpg',
            ],
            [
                'name'   => 'SmartFight Championship Night: Las Vegas',
                'org'    => 'WBC',
                'venue'  => 'MGM Grand Garden Arena', 'city' => 'Las Vegas', 'country' => 'US', 'cap' => 16800,
                'date'   => '+150 days',
                'poster' => 'poster6.jpg',
            ],
        ];

        foreach ($posterEvents as $data) {
            $e = new Event();
            $e->setEventName($data['name']);
            $e->setOrganization($data['org']);
            $date = new \DateTime();
            $date->modify($data['date']);
            $e->setEventDate($date);
            $e->setVenue($data['venue']);
            $e->setCity($data['city']);
            $e->setCountry($data['country']);
            $e->setSeatCapacity($data['cap']);
            $e->setStatus('SCHEDULED');
            $e->setPosterFilename($data['poster']);
            $this->em->persist($e);
            $events[] = $e;
        }

        // 6 historical COMPLETED events (no poster) — exist solely to supply ranking/stats data
        $historicalEvents = [
            ['Kingdom Arena Championship Night',    'WBO', 'Kingdom Arena',           'Riyadh',    'SA', 22000, '-3 months'],
            ['Madison Square Garden Boxing Series', 'WBC', 'Madison Square Garden',   'New York',  'US', 20000, '-5 months'],
            ['T-Mobile Arena Grand Prix',           'WBA', 'T-Mobile Arena',          'Las Vegas', 'US', 20000, '-7 months'],
            ['Wembley Boxing Spectacular',          'IBF', 'Wembley Stadium',         'London',    'GB', 90000, '-9 months'],
            ['Tokyo Dome Fight Night',              'WBO', 'Tokyo Dome',              'Tokyo',     'JP', 55000, '-11 months'],
            ['Barclays Center Showdown',            'WBC', 'Barclays Center',         'Brooklyn',  'US', 19000, '-13 months'],
        ];

        foreach ($historicalEvents as [$name, $org, $venue, $city, $country, $cap, $dateStr]) {
            $e = new Event();
            $e->setEventName($name);
            $e->setOrganization($org);
            $date = new \DateTime();
            $date->modify($dateStr);
            $e->setEventDate($date);
            $e->setVenue($venue);
            $e->setCity($city);
            $e->setCountry($country);
            $e->setSeatCapacity($cap);
            $e->setStatus('COMPLETED');
            $this->em->persist($e);
            $events[] = $e;
        }

        $this->em->flush();
        return $events;
    }

    private function createBlogContent(array $events): void
    {
        // --- Categories ---
        $catData = [
            ['Fight Highlights', 'fight-highlights', 'Full fight highlight videos and round-by-round breakdowns from the biggest bouts in boxing.'],
            ['Event Coverage', 'event-coverage', 'Official coverage, press conferences, weigh-ins, and post-fight recaps from SmartFight events.'],
            ['News & Analysis', 'news-analysis', 'In-depth analysis, rankings commentary, and breaking news from the world of professional boxing.'],
        ];
        $categories = [];
        foreach ($catData as $cd) {
            $cat = new BlogCategory();
            $cat->setName($cd[0]);
            $cat->setSlug($cd[1]);
            $cat->setDescription($cd[2]);
            $this->em->persist($cat);
            $categories[$cd[1]] = $cat;
        }
        $this->em->flush();

        // Grab the seeded admin user as author
        $adminUser = $this->em->getRepository(User::class)->findOneBy(['username' => 'admin']);

        // --- Articles: one per video, paired with matching image ---
        $articles = [
            [
                'title'   => 'Beterbiev vs. Bivol Highlights: A Masterclass in Boxing Warfare',
                'summary' => 'Artur Beterbiev and Dmitry Bivol delivered an all-time classic at Kingdom Arena, unifying all four major light heavyweight titles in a bout for the ages.',
                'content' => '<p>On a historic night in Riyadh, Artur Beterbiev and Dmitry Bivol produced what many are already calling the fight of the decade at light heavyweight. Beterbiev, the relentless pressure fighter, met Bivol, the master technician, in a 12-round battle that showcased everything great about professional boxing.</p><p><strong>Rounds 1-4:</strong> Bivol used his superior footwork to keep Beterbiev at range, landing sharp right hands and slick combinations. Beterbiev absorbed the early punishment but kept pressing forward, landing body shots that would accumulate damage over the later rounds.</p><p><strong>Rounds 5-8:</strong> The fight began to turn as Beterbiev\'s body attack slowed Bivol\'s movement. A powerful left hook in the seventh round wobbled Bivol for the first time in his professional career, sending the crowd into a frenzy.</p><p><strong>Rounds 9-12:</strong> Bivol showed exceptional heart, rallying in the championship rounds and landing several clean combinations. The final scorecards read 115-113, 115-113, 114-114 — a split draw, meaning Beterbiev retained his IBF, WBC, and WBO titles while Bivol kept his WBA strap. A rematch is inevitable.</p><p><em>SmartFight Rating: 9.8/10 — Instant Classic</em></p>',
                'category' => 'fight-highlights',
                'image'    => 'poster2.avif',
                'video'    => 'Artur Beterbiev vs Dmitry Bivol Highlights HD.mp4',
                'status'   => 'PUBLISHED',
                'daysAgo'  => 120,
            ],
            [
                'title'   => 'Zhang Drops Wilder Twice: Brutal KO at Riyadh Season',
                'summary' => 'Zhilei Zhang delivered one of the most devastating knockouts in recent heavyweight history, dropping Deontay Wilder twice before ending the fight in the sixth round.',
                'content' => '<p>In a shocking upset that reverberated across the boxing world, Zhilei Zhang demolished Deontay Wilder at Kingdom Arena in Riyadh, ending the contest in brutal fashion in round six of their heavyweight showdown.</p><p>Wilder, known as one of the hardest punchers in boxing history, landed his trademark right hand early but Zhang absorbed it and kept advancing. The Chinese heavyweight\'s relentless pressure proved to be Wilder\'s undoing.</p><p><strong>The First Knockdown (Round 3):</strong> Zhang caught Wilder clean with a left hook as he was setting up his right hand. Wilder hit the canvas hard but beat the count on unsteady legs.</p><p><strong>The Finishing Sequence (Round 6):</strong> Zhang landed a right hand that sent Wilder down again, and the referee immediately waved it off without a count, correctly determining Wilder was done. The crowd inside Kingdom Arena erupted as Zhang screamed his name to the rafters.</p><p>This victory puts Zhang firmly in contention for the WBO and WBC heavyweight titles, and promotional powerhouses Queensberry and Matchroom have already begun talks for what comes next.</p><p><em>SmartFight KO of the Year candidate. Power rating: 98/100</em></p>',
                'category' => 'fight-highlights',
                'image'    => 'anthony-joshua-vs-francis-ngannou-landscape.webp',
                'video'    => 'BRUTAL KO _ Zhilei Zhang vs. Deontay Wilder Highlights (Queensberry vs. Matchroom - Riyadh Season).mp4',
                'status'   => 'PUBLISHED',
                'daysAgo'  => 180,
            ],
            [
                'title'   => 'Davis vs. Garcia Full Highlights: Tank Delivers Walk-Off KO',
                'summary' => 'Gervonta "Tank" Davis put on a clinical performance before landing the shot of his career to stop Ryan Garcia in the seventh round at T-Mobile Arena.',
                'content' => '<p>The PBC on Showtime PPV event lived up to every bit of its hype as Gervonta Davis and Ryan Garcia collided in a lightweight superfight that had boxing fans on the edge of their seats from the first bell.</p><p>Garcia came out aggressive, using his 6-inch reach advantage and sharp jab to keep Davis on the outside in the opening rounds. By round three, Davis had taken a flash knockdown from a sharp left hook — a stunning moment that reminded everyone what Garcia brings to the table.</p><p><strong>The Turning Point (Rounds 5-6):</strong> Davis began timing Garcia\'s jab perfectly, slipping inside and landing short, compact hooks to the body. Garcia\'s movement began to slow noticeably as the body shots accumulated.</p><p><strong>The Finish (Round 7):</strong> Davis dropped down and landed a left hook to the body that dropped Garcia, who was unable to beat the ten count. The victory marked yet another statement performance from "Tank," cementing his status as pound-for-pound elite.</p><p>With Davis undefeated and Garcia still young at 24, the rematch clause ensures fans haven\'t seen the last of this rivalry. SmartFight\'s pre-fight AI model had called Davis winning by KO rounds 7-9 — a pinpoint prediction.</p><p><em>SmartFight Body Shot Efficiency: Davis 34% | Garcia 19%</em></p>',
                'category' => 'fight-highlights',
                'image'    => 'gervonta-kingry-davis-garcia-tmobile.jpeg',
                'video'    => 'Davis vs Garcia HIGHLIGHTS_ April 22, 2023 _ PBC on Showtime PPV.mp4',
                'status'   => 'PUBLISHED',
                'daysAgo'  => 760,
            ],
            [
                'title'   => 'GGG vs. Canelo 2: The Rematch That Divided Boxing',
                'summary' => 'Gennady Golovkin and Canelo Alvarez met for the second time in a middleweight title unification that ended in a majority decision — and continues to be debated to this day.',
                'content' => '<p>Few fights in modern boxing history have generated as much debate as the Golovkin–Canelo rematch at T-Mobile Arena in Las Vegas. One year after their controversial split draw, the two best middleweights in the world returned to settle the score — and left as many questions as answers.</p><p>The fight was closer than their first meeting, with Canelo controlling more rounds with his body work and lateral movement. Golovkin, meanwhile, pressed forward relentlessly, landing his trademark jab and right hand combinations throughout the middle rounds.</p><p><strong>Rounds 1-4:</strong> Canelo came out sharp, landing to the body early and moving well. Golovkin established his jab and began to build his rhythm by round three.</p><p><strong>Rounds 7-10 — The Heart of the Fight:</strong> Golovkin had his best stretch, landing flush right hands and rocking Canelo in round nine. The crowd noise inside T-Mobile Arena was deafening.</p><p><strong>Championship Rounds:</strong> Canelo came on strong down the stretch, landing his trademark left hook to the body and clean uppercuts in the clinch. All three judges awarded the fight to Canelo — scores of 115-113, 115-113, and 114-114 — though Golovkin\'s camp vehemently disagreed.</p><p>The legacy debate continues. SmartFight\'s scoring algorithm had GGG winning 7 rounds to Canelo\'s 5, with the final analysis noting a clear divergence from the official cards.</p><p><em>SmartFight AI Punch Stats: GGG landed 232 | Canelo landed 219 (CompuBox)</em></p>',
                'category' => 'news-analysis',
                'image'    => 'poster3.jpg',
                'video'    => 'GENNADY GOLOVKIN VS CANELO ALVAREZ 2 (FULL HIGHLIGHTS).mp4',
                'status'   => 'PUBLISHED',
                'daysAgo'  => 2040,
            ],
            [
                'title'   => 'Lomachenko vs. Martinez: The Matrix Displays Artistry in WBO Title Defense',
                'summary' => 'Vasiliy Lomachenko gave a boxing clinic against Rocky Martinez in his WBO super featherweight title defense, showcasing the technical virtuosity that makes him unique in the sport.',
                'content' => '<p>When Vasiliy Lomachenko entered the ring at Madison Square Garden to defend his WBO title against Román "Rocky" Martínez, the boxing world was watching to see if the Ukrainian master could replicate his 2016 stoppage. He did not disappoint.</p><p>From the opening bell, Lomachenko was a ghost — appearing and disappearing, landing punches from angles that seemed geometrically impossible, and making Martínez look like he was fighting a different breed of fighter entirely. The nickname "The Matrix" had never felt more appropriate.</p><p><strong>Technical Brilliance on Display:</strong> Lomachenko\'s pivot game was the story of the early rounds. He consistently circled to Martínez\'s southpaw right side, creating openings for his left hand that the Puerto Rican champion simply had no answer for.</p><p><strong>The Stoppage (Round 5):</strong> After a sustained combination drive along the ropes, Lomachenko landed a short right hook that sent Martínez staggering. The referee stepped in and waved it off as Martínez had no ability to defend himself.</p><p>The performance was widely cited as one of the most technically complete displays of the 2010s. SmartFight\'s analytics later ranked Lomachenko\'s footwork efficiency at 97th percentile among all fighters in the database.</p><p><em>SmartFight Technical Score: 9.6/10</em></p>',
                'category' => 'fight-highlights',
                'image'    => 'poster4.jpg',
                'video'    => 'Vasyl Lomachenko Vs Roman  Rocky  Martinez Highlights.mp4',
                'status'   => 'PUBLISHED',
                'daysAgo'  => 2880,
            ],
            [
                'title'   => 'Dubois vs. Wardley: British Heavyweight Derby Ends in Spectacular Finish',
                'summary' => 'Daniel Dubois and Fabio Wardley clashed in one of British boxing\'s most anticipated heavyweight matchups, delivering a memorable finish that had Tottenham Hotspur Stadium on its feet.',
                'content' => '<p>British boxing delivered another memorable chapter as Daniel "Dynamite" Dubois and Fabio Wardley stepped inside the ropes at a sold-out Tottenham Hotspur Stadium for what proved to be a firefight worthy of the occasion.</p><p>Both men came into the fight with knockout percentages above 90%, and that explosive potential was clear from the very first exchanges. Neither fighter showed any interest in long-range boxing — this was always going to be settled up close and in brutal fashion.</p><p><strong>Early Drama (Rounds 1-3):</strong> Wardley landed a short right hand in round two that briefly stunned Dubois, bringing the crowd to its feet. Dubois responded by pinning Wardley to the ropes and unleashing a combination that opened a cut above Wardley\'s left eye.</p><p><strong>The Finish:</strong> In round four, Dubois landed the right hand he\'d been setting up all night — a searing straight right that sent Wardley crashing to the canvas. Wardley, to his enormous credit, attempted to beat the count but was unable to do so, and the referee correctly halted the contest.</p><p>The victory keeps Dubois firmly in the conversation for a world heavyweight title shot. SmartFight\'s matchmaking AI rates his ELO at 1,847 — putting him in the top tier of available contenders for the WBA and IBF titles.</p><p><em>Punch power index: Dubois 94 | Wardley 89 (SmartFight scale)</em></p>',
                'category' => 'event-coverage',
                'image'    => 'poster5.jpg',
                'video'    => 'WHAT A FINISH!! Daniel Dubois vs. Fabio Wardley _ Fight Highlights.mp4',
                'status'   => 'PUBLISHED',
                'daysAgo'  => 540,
            ],
            [
                'title'   => 'Joshua vs. Ngannou: The Crossover Fight That Shook Riyadh',
                'summary' => 'Anthony Joshua faced MMA crossover star Francis Ngannou in a heavyweight spectacle at Kingdom Arena, delivering a night of drama that kept 22,000 fans on the edge of their seats.',
                'content' => '<p>When promoters first announced Anthony Joshua versus Francis Ngannou, the boxing purist in many observers was skeptical. By the end of fight night at Kingdom Arena in Riyadh, those same observers had to admit they\'d witnessed something extraordinary — even if the arguments about what it proved continue.</p><p>Ngannou, the former UFC Heavyweight Champion making just his second professional boxing appearance, arrived with a game plan centered entirely on landing his nuclear right hand. Joshua, with his superior boxing skills and professional experience, was tasked with not walking onto that punch.</p><p><strong>The First Half:</strong> Joshua boxed beautifully for five rounds, using his jab to keep Ngannou at distance, landing sharp combinations and picking his spots with clinical precision. The crowd inside Kingdom Arena appreciated the technical display but grew restless for the fireworks both men promised.</p><p><strong>The Ngannou Knockdown (Round 5):</strong> The moment that rewrote the narrative. Ngannou landed an overhand right that sent Joshua crashing to the canvas in what was arguably the single most shocking moment in boxing in 2023. The entire arena fell silent, then erupted.</p><p><strong>The Finish:</strong> Joshua, to his enormous credit, rose from the knockdown and systematically took Ngannou apart over the final rounds, landing combination after combination until the referee intervened in round six.</p><p>The fight raised Ngannou\'s stock significantly while reminding the boxing world of Joshua\'s considerable capabilities when fully focused. SmartFight\'s AI model gave Joshua a pre-fight win probability of 76% — the knockdown notwithstanding, the model was directionally correct.</p><p><em>SmartFight Event Rating: 8.7/10</em></p>',
                'category' => 'event-coverage',
                'image'    => 'anthony-joshua-vs-francis-ngannou-landscape.webp',
                'video'    => null,
                'status'   => 'PUBLISHED',
                'daysAgo'  => 240,
            ],
        ];

        foreach ($articles as $data) {
            $a = new BlogArticle();
            $a->setTitle($data['title']);
            $a->setSummary($data['summary']);
            $a->setContent($data['content']);
            $a->setStatus($data['status']);
            $a->setCategory($categories[$data['category']]);
            $a->setAuthor($adminUser);
            $a->setImagePath($data['image']);
            if ($data['video']) {
                $a->setVideoPath($data['video']);
            }
            // Manually set timestamps since PrePersist runs on flush
            $createdAt = new \DateTime('-' . $data['daysAgo'] . ' days');
            $a->setCreatedAt($createdAt);
            $a->setUpdatedAt($createdAt);
            $this->em->persist($a);
        }

        $this->em->flush();
    }

    private function createFights(array $f, array $events): array
    {
        $fighters = array_values($f);
        $allFights = [];
        
        foreach ($events as $e) {
            $org = $e->getOrganization();
            // Original logic: 3 fights per completed event
            $numFights = 3;
            
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
