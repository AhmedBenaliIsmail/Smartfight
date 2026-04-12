<?php

namespace App\Repository;

use App\Entity\EventBooking;
use App\Entity\FanPrediction;
use App\Entity\User;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\ORM\QueryBuilder;
use Doctrine\Persistence\ManagerRegistry;

class UserRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, User::class);
    }

    public function findFans(): array
    {
        return $this->createQueryBuilder('u')
            ->leftJoin('u.role', 'r')
            ->where('r.name = :role')
            ->setParameter('role', 'FAN')
            ->getQuery()
            ->getResult();
    }

    public function findFansByAudience(string $audience): array
    {
        $audience = strtolower(trim($audience));
        $qb = $this->createFanBaseQueryBuilder();

        if ($audience === 'vip') {
            $qb
                ->innerJoin(EventBooking::class, 'eb', 'WITH', 'eb.user = u')
                ->andWhere('eb.ticketType = :vipType')
                ->andWhere('eb.bookingStatus = :confirmedStatus')
                ->setParameter('vipType', EventBooking::TYPE_VIP)
                ->setParameter('confirmedStatus', EventBooking::STATUS_CONFIRMED)
                ->groupBy('u.id');
        } elseif ($audience === 'predictors') {
            $qb
                ->innerJoin(FanPrediction::class, 'fp', 'WITH', 'fp.fan = u')
                ->groupBy('u.id');
        } elseif ($audience === 'active') {
            $monthStart = new \DateTimeImmutable('first day of this month 00:00:00');
            $monthEnd = $monthStart->modify('first day of next month 00:00:00');

            $qb
                ->leftJoin(EventBooking::class, 'eb', 'WITH', 'eb.user = u AND eb.bookingDate >= :monthStart AND eb.bookingDate < :monthEnd')
                ->leftJoin(FanPrediction::class, 'fp', 'WITH', 'fp.fan = u AND fp.submittedAt >= :monthStart AND fp.submittedAt < :monthEnd')
                ->andWhere('(eb.id IS NOT NULL OR fp.id IS NOT NULL)')
                ->setParameter('monthStart', $monthStart)
                ->setParameter('monthEnd', $monthEnd)
                ->groupBy('u.id');
        }

        return $qb->getQuery()->getResult();
    }

    private function createFanBaseQueryBuilder(): QueryBuilder
    {
        return $this->createQueryBuilder('u')
            ->leftJoin('u.role', 'r')
            ->where('r.name = :role')
            ->setParameter('role', 'FAN');
    }
}
