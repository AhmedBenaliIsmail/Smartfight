<?php
require 'vendor/autoload.php';

use App\Kernel;
use App\Entity\User;
use App\Entity\Role;
use Symfony\Component\Dotenv\Dotenv;

(new Dotenv())->bootEnv('.env');

$kernel = new Kernel($_SERVER['APP_ENV'], (bool) $_SERVER['APP_DEBUG']);
$kernel->boot();
$container = $kernel->getContainer();
$em = $container->get('doctrine.orm.entity_manager');
$hasher = $container->get('security.user_password_hasher');

$username = 'admin';
$password = 'admin';

$userRepo = $em->getRepository(User::class);
$user = $userRepo->findOneBy(['username' => $username]);

if (!$user) {
    echo "Creating admin user...\n";
    $user = new User();
    $user->setUsername($username);
    $user->setPassword($hasher->hashPassword($user, $password));
} else {
    echo "Updating admin password...\n";
    $user->setPassword($hasher->hashPassword($user, $password));
}

$roleRepo = $em->getRepository(Role::class);
$adminRole = $roleRepo->findOneBy(['roleName' => 'ADMIN']);
if (!$adminRole) {
    $adminRole = new Role();
    $adminRole->setRoleName('ADMIN');
    $em->persist($adminRole);
}

if (!$user->hasRole('ADMIN')) {
    $user->addRole($adminRole);
}

$em->persist($user);
$em->flush();

echo "Admin user ready.\n";
