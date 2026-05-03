<?php
require 'vendor/autoload.php';

use Symfony\Component\Dotenv\Dotenv;

$dotenv = new Dotenv();
$dotenv->load(__DIR__ . '/../.env');

$kernel = new \App\Kernel('dev', true);
$kernel->boot();
$container = $kernel->getContainer();
$em = $container->get('doctrine')->getManager();
$userRepo = $em->getRepository(\App\Entity\User::class);
$roleRepo = $em->getRepository(\App\Entity\Role::class);

$user = $userRepo->findOneBy(['username' => 'mahdi']);
if ($user) {
    echo "User mahdi found. Current roles: " . implode(', ', $user->getRoles()) . "\n";
    
    $adminRole = $roleRepo->findOneBy(['roleName' => 'ADMIN']);
    if (!$adminRole) {
        $adminRole = new \App\Entity\Role();
        $adminRole->setRoleName('ADMIN');
        $em->persist($adminRole);
        echo "Created ADMIN role.\n";
    }
    
    if (!$user->hasRole('ADMIN')) {
        $user->addRole($adminRole);
        echo "Added ADMIN role to mahdi.\n";
    }
    
    $em->flush();
    echo "New roles for mahdi: " . implode(', ', $user->getRoles()) . "\n";
} else {
    echo "User mahdi not found.\n";
}
