<?php
require __DIR__.'/vendor/autoload.php';

use App\Kernel;
use App\Entity\WeightDivision;
use Symfony\Component\Dotenv\Dotenv;

(new Dotenv())->bootEnv(__DIR__.'/.env');

$kernel = new Kernel('dev', true);
$kernel->boot();

$container = $kernel->getContainer();
$em = $container->get('doctrine')->getManager();

$divisions = [
    ['name' => 'Heavyweight', 'min' => 200, 'max' => 300],
    ['name' => 'Cruiserweight', 'min' => 175, 'max' => 200],
    ['name' => 'Light Heavyweight', 'min' => 168, 'max' => 175],
    ['name' => 'Middleweight', 'min' => 154, 'max' => 160],
    ['name' => 'Welterweight', 'min' => 140, 'max' => 147],
    ['name' => 'Lightweight', 'min' => 130, 'max' => 135],
    ['name' => 'Featherweight', 'min' => 122, 'max' => 126],
    ['name' => 'Bantamweight', 'min' => 115, 'max' => 118]
];

foreach ($divisions as $d) {
    $wd = new WeightDivision();
    $wd->setName($d['name']);
    $wd->setSlug(strtolower(str_replace(' ', '-', $d['name'])));
    $wd->setMaxWeightLbs($d['max']);
    $em->persist($wd);
}

$em->flush();

echo "Weight Divisions seeded successfully!\n";
