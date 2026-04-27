<?php

namespace App\Entity;

use App\Repository\FanPreferenceRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: FanPreferenceRepository::class)]
#[ORM\Table(name: 'fan_preference')]
#[ORM\UniqueConstraint(name: 'uq_fan_preference', columns: ['fan_id'])]
class FanPreference
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: 'integer')]
    private ?int $id = null;

    #[ORM\ManyToOne(targetEntity: User::class)]
    #[ORM\JoinColumn(name: 'fan_id', nullable: false)]
    private User $fan;

    #[ORM\ManyToOne(targetEntity: Discipline::class)]
    #[ORM\JoinColumn(name: 'favorite_discipline_id', nullable: true)]
    private ?Discipline $favoriteDiscipline = null;

    #[ORM\ManyToOne(targetEntity: Fighter::class)]
    #[ORM\JoinColumn(name: 'favorite_fighter_id', nullable: true)]
    private ?Fighter $favoriteFighter = null;

    #[ORM\Column(name: 'created_at', type: 'datetime')]
    private \DateTimeInterface $createdAt;

    #[ORM\Column(name: 'updated_at', type: 'datetime')]
    private \DateTimeInterface $updatedAt;

    public function getId(): ?int { return $this->id; }
    public function getFan(): User { return $this->fan; }
    public function setFan(User $fan): static { $this->fan = $fan; return $this; }
    public function getFavoriteDiscipline(): ?Discipline { return $this->favoriteDiscipline; }
    public function setFavoriteDiscipline(?Discipline $d): static { $this->favoriteDiscipline = $d; return $this; }
    public function getFavoriteFighter(): ?Fighter { return $this->favoriteFighter; }
    public function setFavoriteFighter(?Fighter $f): static { $this->favoriteFighter = $f; return $this; }
}
