<?php

namespace App\Repository;

use App\Entity\SystemMeta;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

class SystemMetaRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, SystemMeta::class);
    }

    public function getValue(string $key): ?string
    {
        $meta = $this->findOneBy(['key' => $key]);
        return $meta?->getValue();
    }

    public function setValue(string $key, string $value): void
    {
        $meta = $this->findOneBy(['key' => $key]) ?? (new SystemMeta())->setKey($key);
        $meta->setValue($value)->setUpdatedAt(new \DateTime());
        $this->getEntityManager()->persist($meta);
        $this->getEntityManager()->flush();
    }
}
