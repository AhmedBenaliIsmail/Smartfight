<?php

namespace App\Command;

use App\Entity\User;
use App\Entity\UserRole;
use App\Repository\UserRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Component\Console\Attribute\AsCommand;
use Symfony\Component\Console\Command\Command;
use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Input\InputOption;
use Symfony\Component\Console\Output\OutputInterface;
use Symfony\Component\Console\Style\SymfonyStyle;
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;

#[AsCommand(
    name: 'app:create-admin',
    description: 'Create or promote an admin user account interactively',
)]
class CreateAdminCommand extends Command
{
    public function __construct(
        private readonly EntityManagerInterface $entityManager,
        private readonly UserRepository $userRepository,
        private readonly UserPasswordHasherInterface $passwordHasher,
    ) {
        parent::__construct();
    }

    protected function configure(): void
    {
        $this->addOption('force', 'f', InputOption::VALUE_NONE, 'Skip confirmation and overwrite if user already exists');
    }

    protected function execute(InputInterface $input, OutputInterface $output): int
    {
        $io = new SymfonyStyle($input, $output);
        $force = (bool) $input->getOption('force');

        $io->title('SmartFight — Create Admin User');

        $email = $io->ask('Admin email', null, static function (?string $value): string {
            if (empty($value) || !filter_var($value, FILTER_VALIDATE_EMAIL)) {
                throw new \RuntimeException('A valid email address is required.');
            }
            return $value;
        });

        $username = $io->ask('Username (3–50 characters)', null, function (?string $value) use ($email): string {
            $value = trim((string) $value);
            if (strlen($value) < 3 || strlen($value) > 50) {
                throw new \RuntimeException('Username must be between 3 and 50 characters.');
            }
            $existing = $this->userRepository->findOneBy(['username' => $value]);
            if ($existing !== null && $existing->getEmail() !== $email) {
                throw new \RuntimeException(sprintf('Username "%s" is already taken.', $value));
            }
            return $value;
        });

        $firstName = $io->ask('First name', null, static function (?string $value): string {
            if (empty(trim((string) $value))) {
                throw new \RuntimeException('First name is required.');
            }
            return trim($value);
        });

        $lastName = $io->ask('Last name', null, static function (?string $value): string {
            if (empty(trim((string) $value))) {
                throw new \RuntimeException('Last name is required.');
            }
            return trim($value);
        });

        $plainPassword = $io->askHidden('Password (min 8 characters)', static function (?string $value): string {
            if (empty($value) || strlen($value) < 8) {
                throw new \RuntimeException('Password must be at least 8 characters.');
            }
            return $value;
        });

        $confirm = $io->askHidden('Confirm password', static function (?string $value): string {
            return (string) $value;
        });

        if ($plainPassword !== $confirm) {
            $io->error('Passwords do not match. Aborted.');
            return Command::FAILURE;
        }

        $existingUser = $this->userRepository->findOneBy(['email' => $email]);

        if ($existingUser !== null && !$force) {
            $overwrite = $io->confirm(
                sprintf('A user with email "%s" already exists. Promote to admin?', $email),
                false
            );

            if (!$overwrite) {
                $io->warning('Aborted. No changes made.');
                return Command::SUCCESS;
            }
        }

        if (!$force) {
            $io->table(
                ['Field', 'Value'],
                [
                    ['Email', $email],
                    ['Username', $username],
                    ['First name', $firstName],
                    ['Last name', $lastName],
                    ['Role', 'ADMIN'],
                ]
            );

            if (!$io->confirm('Create this admin user?', true)) {
                $io->warning('Aborted. No changes made.');
                return Command::SUCCESS;
            }
        }

        $adminRole = $this->entityManager->getRepository(UserRole::class)->findOneBy(['name' => 'ADMIN']);

        if ($adminRole === null) {
            $adminRole = (new UserRole())
                ->setName('ADMIN')
                ->setDescription('Full administrative access');
            $this->entityManager->persist($adminRole);
            $this->entityManager->flush();
            $io->note('Created new ADMIN role.');
        }

        $user = $existingUser ?? new User();
        $user
            ->setEmail($email)
            ->setUsername($username)
            ->setFirstName($firstName)
            ->setLastName($lastName)
            ->setRole($adminRole)
            ->setIsActive(true);

        $user->setPassword($this->passwordHasher->hashPassword($user, $plainPassword));

        $this->entityManager->persist($user);
        $this->entityManager->flush();

        $io->success(sprintf(
            '%s admin account for "%s" (ID %d).',
            $existingUser !== null ? 'Updated' : 'Created',
            $email,
            $user->getId()
        ));

        return Command::SUCCESS;
    }
}
