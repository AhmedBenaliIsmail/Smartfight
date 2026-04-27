<?php
namespace App\Repository;

use App\Entity\User;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;
use Symfony\Component\Security\Core\User\PasswordUpgraderInterface;
use Symfony\Component\Security\Core\User\PasswordAuthenticatedUserInterface;

class UserRepository extends ServiceEntityRepository implements PasswordUpgraderInterface
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, User::class);
    }

    public function findByUsername(string $username): ?User
    {
        return $this->findOneBy(['username' => $username]);
    }

    public function usernameExists(string $username): bool
    {
        return $this->findOneBy(['username' => $username]) !== null;
    }

    public function emailExists(string $email): bool
    {
        return $this->findOneBy(['email' => $email]) !== null;
    }

    public function upgradePassword(PasswordAuthenticatedUserInterface $user, string $newHashedPassword): void
    {
        if (!$user instanceof User) return;
        $user->setPassword($newHashedPassword);
        $this->getEntityManager()->persist($user);
        $this->getEntityManager()->flush();
    }
    public function findFansOrderedByPoints(int $limit = 50): array
    {
        $qb = $this->createQueryBuilder('u');
        $adminSubquery = $this->getEntityManager()->createQueryBuilder()
            ->select('u2.userId')
            ->from('App\Entity\User', 'u2')
            ->join('u2.roles', 'r2')
            ->where('r2.roleName = :adminRole');

        return $qb->where($qb->expr()->notIn('u.userId', $adminSubquery->getDQL()))
            ->setParameter('adminRole', 'ADMIN')
            ->orderBy('u.predictionPoints', 'DESC')
            ->setMaxResults($limit)
            ->getQuery()
            ->getResult();
    }

    public function getDailyRegistrationsLast7Days(): array
    {
        $conn = $this->getEntityManager()->getConnection();
        $sql = '
            SELECT DATE(createdDate) as date, COUNT(userId) as total
            FROM users
            WHERE createdDate >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)
            GROUP BY DATE(createdDate)
            ORDER BY date ASC
        ';
        return $conn->fetchAllAssociative($sql);
    }
}
