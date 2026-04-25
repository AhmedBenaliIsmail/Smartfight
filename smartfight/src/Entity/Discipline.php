<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity]
#[ORM\Table(name: 'discipline')]
class Discipline
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: 'integer')]
    private ?int $id = null;

    #[ORM\Column(type: 'string', length: 100, unique: true)]
    private string $name;

    #[ORM\Column(type: 'text', nullable: true)]
    private ?string $description = null;

    #[ORM\Column(name: 'weight_class', type: 'string', length: 60, nullable: true)]
    private ?string $weightClass = null;

    #[ORM\Column(name: 'round_duration', type: 'integer', options: ['default' => 3])]
    private int $roundDuration = 3;

    #[ORM\Column(name: 'max_rounds', type: 'integer', options: ['default' => 3])]
    private int $maxRounds = 3;

    #[ORM\Column(name: 'created_at', type: 'datetime')]
    private \DateTimeInterface $createdAt;

    #[ORM\Column(name: 'updated_at', type: 'datetime')]
    private \DateTimeInterface $updatedAt;

    public function getId(): ?int { return $this->id; }
    public function getName(): string { return $this->name; }
    public function __toString(): string { return $this->name; }
}
