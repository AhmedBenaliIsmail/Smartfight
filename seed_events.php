<?php
require __DIR__.'/vendor/autoload.php';
use App\Kernel;
use App\Entity\Event;
use Symfony\Component\Dotenv\Dotenv;

(new Dotenv())->bootEnv(__DIR__.'/.env');
$kernel = new Kernel('dev', true);
$kernel->boot();
$em = $kernel->getContainer()->get('doctrine')->getManager();

$events = [
    ['Fury vs Usyk II','2026-09-20','WBO','Kingdom Arena','Riyadh, Saudi Arabia','SCHEDULED',80000],
    ['Garcia vs Haney','2026-10-05','WBC','T-Mobile Arena','Las Vegas, USA','SCHEDULED',20000],
    ['Canelo vs Benavidez','2026-11-14','WBC','Barclays Center','Brooklyn, USA','SCHEDULED',18000],
    ['Beterbiev vs Bivol II','2026-08-22','WBA','Crypto.com Arena','Los Angeles, USA','SCHEDULED',22000],
    ['Tank vs Garcia','2026-12-12','IBF','MGM Grand Garden Arena','Las Vegas, USA','SCHEDULED',17000],
    ['Inoue vs Nery','2026-07-18','WBO','Saitama Super Arena','Tokyo, Japan','SCHEDULED',36000],
    ['Crawford vs Spence III','2026-06-28','UNDISPUTED','T-Mobile Arena','Las Vegas, USA','SCHEDULED',21000],
];

foreach ($events as [$name, $date, $org, $venue, $city, $status, $cap]) {
    $existing = $em->getRepository(Event::class)->findOneBy(['eventName' => $name]);
    if ($existing) continue;
    $e = new Event();
    $e->setEventName($name)
      ->setEventDate(new \DateTime($date))
      ->setOrganization($org)
      ->setVenue($venue)
      ->setCity($city)
      ->setStatus($status)
      ->setSeatCapacity($cap);
    $em->persist($e);
    echo "Added: $name\n";
}
$em->flush();
echo "Done.\n";
