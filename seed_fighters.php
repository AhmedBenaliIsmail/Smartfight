<?php
require __DIR__.'/vendor/autoload.php';

use App\Kernel;
use App\Entity\Fighter;
use App\Repository\WeightDivisionRepository;
use Symfony\Component\Dotenv\Dotenv;

(new Dotenv())->bootEnv(__DIR__.'/.env');
$kernel = new Kernel('dev', true);
$kernel->boot();
$container = $kernel->getContainer();
$em = $container->get('doctrine')->getManager();
$wdRepo = $em->getRepository(\App\Entity\WeightDivision::class);

$divs = $wdRepo->findAll();
if (empty($divs)) { die("Run seed_weight_divisions.php first!\n"); }

$fighters = [
    ['Mohamed','Ali','MW','TN',32,5,1,20,8,4,183,185],
    ['Carlos','Rodriguez','WW','MX',28,3,2,15,9,4,175,178],
    ['James','Thompson','HW','US',25,2,0,22,3,0,195,200],
    ['Ivan','Petrov','LHW','RU',30,4,1,18,10,2,185,188],
    ['Yuki','Tanaka','FW','JP',22,1,0,12,8,2,163,165],
    ['Andre','Dupont','MW','FR',27,3,2,14,9,4,180,182],
    ['Marcus','Williams','HW','US',29,4,1,20,7,2,193,198],
    ['Khalid','Hassan','WW','MA',26,2,1,16,8,2,176,179],
];

$divMap = [];
foreach ($divs as $d) {
    $divMap[$d->getSlug()] = $d;
}
$slugMap = [
    'MW' => 'middleweight', 'WW' => 'welterweight', 'HW' => 'heavyweight',
    'LHW' => 'light-heavyweight', 'FW' => 'featherweight',
];

foreach ($fighters as $data) {
    [$fn,$ln,$divCode,$nat,$w,$l,$d,$ko,$tech,$dec,$height,$reach] = $data;
    $existing = $em->getRepository(Fighter::class)->findOneBy(['firstName' => $fn, 'lastName' => $ln]);
    if ($existing) continue;

    $f = new Fighter();
    $f->setFirstName($fn)->setLastName($ln)->setNationality($nat);
    $f->setWins($w)->setLosses($l)->setDraws($d);
    $f->setKoWins($ko)->setTechnicalWins($tech)->setDecisionWins($dec);
    $f->setHeight($height)->setReach($reach);
    $slug = $slugMap[$divCode] ?? null;
    if ($slug && isset($divMap[$slug])) $f->setWeightDivision($divMap[$slug]);
    $em->persist($f);
    echo "Added: $fn $ln\n";
}

$em->flush();
echo "Done!\n";
