<?php
require 'vendor/autoload.php';

use Symfony\Component\Dotenv\Dotenv;

$dotenv = new Dotenv();
$dotenv->load(__DIR__ . '/../.env');

$kernel = new \App\Kernel('dev', true);
$kernel->boot();
$container = $kernel->getContainer();
$repo = $container->get('doctrine')->getRepository(\App\Entity\User::class);
$user = $repo->findOneBy(['username' => 'mahdi']);
if ($user) {
    echo 'Roles for mahdi: ' . implode(', ', $user->getRoles()) . PHP_EOL;
} else {
    echo 'User mahdi not found' . PHP_EOL;
}
