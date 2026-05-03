<?php
require 'vendor/autoload.php';
$kernel = new App\Kernel('dev', true);
$kernel->boot();
$container = $kernel->getContainer();
$em = $container->get('doctrine')->getManager();
$userRepo = $em->getRepository(App\Entity\User::class);
$user = $userRepo->findOneBy(['username' => 'mahdi']);
if ($user) {
    print_r($user->getRoles());
} else {
    echo "User mahdi not found\n";
}
