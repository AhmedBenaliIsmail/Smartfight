<?php
require __DIR__.'/vendor/autoload.php';

use App\Kernel;
use App\Entity\User;
use App\Entity\Role;
use Symfony\Component\Dotenv\Dotenv;

(new Dotenv())->bootEnv(__DIR__.'/.env');

$kernel = new Kernel('dev', true);
$kernel->boot();

$container = $kernel->getContainer();
$em = $container->get('doctrine')->getManager();


// Create Admin Role
$role = $em->getRepository(Role::class)->findOneBy(['roleName' => 'ADMIN']);
if (!$role) {
    $role = new Role();
    $role->setRoleName('ADMIN');
    $em->persist($role);
}

// Create User Role
$userRole = $em->getRepository(Role::class)->findOneBy(['roleName' => 'USER']);
if (!$userRole) {
    $userRole = new Role();
    $userRole->setRoleName('USER');
    $em->persist($userRole);
}

// Create standard admin user
$admin = new User();
$admin->setUsername('admin');
$admin->setPassword(password_hash('admin', PASSWORD_DEFAULT));
$admin->setEmail('admin@smartfight.com');
$admin->addRole($role);
$admin->addRole($userRole);

$em->persist($admin);
$em->flush();

echo "Admin user created successfully with password 'admin'!\n";
