<?php

use App\Entity\Fighter;
use App\Entity\WeightDivision;
use Doctrine\ORM\EntityManagerInterface;

/** @var \Symfony\Component\HttpKernel\KernelInterface $kernel */
$container = $kernel->getContainer();
$em = $container->get('doctrine.orm.entity_manager');

$fightersData = [
    // Heavyweights (ID 1)
    ['Tyson', 'Fury', 'The Gypsy King', 1, 'GB', 33, 0, 1, 23, 206, 216, 3500, 1600],
    ['Oleksandr', 'Usyk', 'The Cat', 1, 'UA', 21, 0, 0, 14, 191, 198, 4200, 2100],
    ['Anthony', 'Joshua', 'AJ', 1, 'GB', 26, 3, 0, 23, 198, 208, 5000, 2400],
    ['Deontay', 'Wilder', 'The Bronze Bomber', 1, 'US', 43, 2, 1, 42, 201, 211, 4000, 1500],
    
    // Middleweights (ID 5)
    ['Canelo', 'Alvarez', 'Canelo', 5, 'MX', 60, 2, 2, 39, 173, 179, 6500, 3800],
    ['Gennadiy', 'Golovkin', 'GGG', 5, 'KZ', 42, 2, 1, 37, 179, 178, 5800, 3200],
    ['Jermall', 'Charlo', 'Hitman', 5, 'US', 32, 0, 0, 22, 183, 185, 4100, 1800],
    ['Demetrius', 'Andrade', 'Boo Boo', 5, 'US', 32, 0, 0, 19, 183, 187, 3900, 1600],

    // Lightweights (ID 9)
    ['Gervonta', 'Davis', 'Tank', 9, 'US', 29, 0, 0, 27, 166, 171, 3500, 1700],
    ['Devin', 'Haney', 'The Dream', 9, 'US', 30, 0, 0, 15, 173, 180, 4800, 2200],
    ['Ryan', 'Garcia', 'KingRy', 9, 'US', 24, 1, 0, 20, 174, 178, 3800, 1500],
    ['Vasiliy', 'Lomachenko', 'The Matrix', 9, 'UA', 17, 3, 0, 11, 170, 166, 5200, 3100],
    ['Shakur', 'Stevenson', 'Sugar', 9, 'US', 20, 0, 0, 10, 173, 173, 4100, 1900],
];

foreach ($fightersData as $data) {
    $f = new Fighter();
    $f->setFirstName($data[0]);
    $f->setLastName($data[1]);
    $f->setNickname($data[2]);
    
    $wd = $em->getRepository(WeightDivision::class)->find($data[3]);
    $f->setWeightDivision($wd);
    
    $f->setNationality($data[4]);
    $f->setWins($data[5]);
    $f->setLosses($data[6]);
    $f->setDraws($data[7]);
    $f->setKoWins($data[8]);
    $f->setHeight($data[9]);
    $f->setReach($data[10]);
    $f->setStrikesThrown($data[11]);
    $f->setStrikesLanded($data[12]);
    $f->setEloRating(1500 + ($data[5] * 10) - ($data[6] * 15));
    $f->setPerformanceScore($data[5] * 5);
    
    $em->persist($f);
}

$em->flush();
echo "Successfully seeded " . count($fightersData) . " professional fighters across divisions.\n";
