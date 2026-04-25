<?php

namespace App\Entity;

use App\Repository\SystemMetaRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: SystemMetaRepository::class)]
#[ORM\Table(name: 'system_meta')]
class SystemMeta
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: 'integer')]
    private ?int $id = null;

    #[ORM\Column(name: 'meta_key', type: 'string', length: 100, unique: true)]
    private string $key;

    #[ORM\Column(type: 'string', length: 255)]
    private string $value;

    #[ORM\Column(name: 'updatedAt', type: 'datetime')]
    private \DateTimeInterface $updatedAt;

    public function getId(): ?int { return $this->id; }

    public function getKey(): string { return $this->key; }
    public function setKey(string $key): static { $this->key = $key; return $this; }

    public function getValue(): string { return $this->value; }
    public function setValue(string $value): static { $this->value = $value; return $this; }

    public function getUpdatedAt(): \DateTimeInterface { return $this->updatedAt; }
    public function setUpdatedAt(\DateTimeInterface $updatedAt): static { $this->updatedAt = $updatedAt; return $this; }
}
