<?php
namespace App\Command;

use App\Entity\Role;
use App\Entity\User;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Component\Console\Attribute\AsCommand;
use Symfony\Component\Console\Command\Command;
use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Output\OutputInterface;
use Symfony\Component\Console\Style\SymfonyStyle;
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;

#[AsCommand(
    name: 'app:create-admin',
    description: 'Creates a new admin user.',
)]
class CreateAdminCommand extends Command
{
    public function __construct(
        private EntityManagerInterface $em,
        private UserPasswordHasherInterface $passwordHasher
    ) {
        parent::__construct();
    }

    protected function execute(InputInterface $input, OutputInterface $output): int
    {
        $io = new SymfonyStyle($input, $output);

        $username = $io->ask('Username');
        if (empty($username)) {
            $io->error('Username cannot be empty.');
            return Command::FAILURE;
        }

        $email = $io->ask('Email (optional)');
        
        $password = $io->askHidden('Password');
        if (empty($password)) {
            $io->error('Password cannot be empty.');
            return Command::FAILURE;
        }

        $userRepo = $this->em->getRepository(User::class);
        $existingUser = $userRepo->findOneBy(['username' => $username]);

        if ($existingUser) {
            $io->note('User already exists. Updating password and ensuring ADMIN role.');
            $user = $existingUser;
        } else {
            $user = new User();
            $user->setUsername($username);
            $user->setEmail($email);
            $io->note('Creating new user.');
        }

        // Hash password
        $hashedPassword = $this->passwordHasher->hashPassword($user, $password);
        $user->setPassword($hashedPassword);

        // Ensure ADMIN role
        $roleRepo = $this->em->getRepository(Role::class);
        $adminRole = $roleRepo->findOneBy(['roleName' => 'ADMIN']);
        if (!$adminRole) {
            $adminRole = new Role();
            $adminRole->setRoleName('ADMIN');
            $this->em->persist($adminRole);
        }

        if (!$user->hasRole('ADMIN')) {
            $user->addRole($adminRole);
        }

        $this->em->persist($user);
        $this->em->flush();

        $io->success(sprintf('Admin user "%s" was successfully created/updated.', $username));

        return Command::SUCCESS;
    }
}
