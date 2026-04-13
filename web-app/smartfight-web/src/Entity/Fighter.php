<?php

namespace App\Entity;

use App\Repository\FighterRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: FighterRepository::class)]
#[ORM\Table(name: 'fighter')]
class Fighter
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'id', type: 'integer')]
    private ?int $id = null;

    #[ORM\Column(name: 'user_id', type: 'integer', nullable: true)]
    private ?int $userId = null;

    #[ORM\Column(name: 'nickname', type: 'string', length: 100, nullable: true)]
    private ?string $nickname = null;

    #[ORM\Column(name: 'date_of_birth', type: 'date', nullable: true)]
    private ?\DateTimeInterface $dateOfBirth = null;

    #[ORM\Column(name: 'nationality', type: 'string', length: 100, nullable: true)]
    private ?string $nationality = null;

    #[ORM\Column(name: 'weight_class_id', type: 'integer', nullable: true)]
    private ?int $weightClassId = null;

    #[ORM\Column(name: 'wins', type: 'integer', nullable: true, options: ['default' => 0])]
    private ?int $wins = 0;

    #[ORM\Column(name: 'losses', type: 'integer', nullable: true, options: ['default' => 0])]
    private ?int $losses = 0;

    #[ORM\Column(name: 'draws', type: 'integer', nullable: true, options: ['default' => 0])]
    private ?int $draws = 0;

    #[ORM\Column(name: 'status', type: 'string', length: 50, nullable: true)]
    private ?string $status = null;

    public function getId(): ?int { return $this->id; }
    public function getUserId(): ?int { return $this->userId; }
    public function setUserId(?int $userId): static { $this->userId = $userId; return $this; }
    public function getNickname(): ?string { return $this->nickname; }
    public function setNickname(?string $nickname): static { $this->nickname = $nickname; return $this; }
    public function getDateOfBirth(): ?\DateTimeInterface { return $this->dateOfBirth; }
    public function setDateOfBirth(?\DateTimeInterface $dateOfBirth): static { $this->dateOfBirth = $dateOfBirth; return $this; }
    public function getNationality(): ?string { return $this->nationality; }
    public function setNationality(?string $nationality): static { $this->nationality = $nationality; return $this; }
    public function getWeightClassId(): ?int { return $this->weightClassId; }
    public function setWeightClassId(?int $weightClassId): static { $this->weightClassId = $weightClassId; return $this; }
    public function getWins(): ?int { return $this->wins; }
    public function setWins(?int $wins): static { $this->wins = $wins; return $this; }
    public function getLosses(): ?int { return $this->losses; }
    public function setLosses(?int $losses): static { $this->losses = $losses; return $this; }
    public function getDraws(): ?int { return $this->draws; }
    public function setDraws(?int $draws): static { $this->draws = $draws; return $this; }
    public function getStatus(): ?string { return $this->status; }
    public function setStatus(?string $status): static { $this->status = $status; return $this; }

    public function __toString(): string
    {
        return $this->nickname ?? 'Fighter #' . $this->id;
    }
}
