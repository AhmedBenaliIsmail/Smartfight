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
    #[ORM\Column(type: 'integer')]
    private ?int $id = null;

    #[ORM\ManyToOne(targetEntity: User::class)]
    #[ORM\JoinColumn(name: 'user_id', nullable: false)]
    private User $user;

    #[ORM\Column(type: 'string', length: 100, nullable: true)]
    private ?string $nickname = null;

    #[ORM\Column(name: 'date_of_birth', type: 'date', nullable: true)]
    private ?\DateTimeInterface $dateOfBirth = null;

    #[ORM\Column(type: 'string', length: 100, nullable: true)]
    private ?string $nationality = null;

    #[ORM\Column(name: 'photo_url', type: 'string', length: 255, nullable: true)]
    private ?string $photoUrl = null;

    #[ORM\Column(name: 'weight_class_id', type: 'integer', nullable: true)]
    private ?int $weightClassId = null;

    #[ORM\Column(type: 'integer', options: ['default' => 0])]
    private int $wins = 0;

    #[ORM\Column(type: 'integer', options: ['default' => 0])]
    private int $losses = 0;

    #[ORM\Column(type: 'integer', options: ['default' => 0])]
    private int $draws = 0;

    #[ORM\Column(type: 'string', length: 20)]
    private string $status = 'ACTIVE';

    #[ORM\Column(name: 'created_at', type: 'datetime')]
    private \DateTimeInterface $createdAt;

    #[ORM\Column(name: 'updated_at', type: 'datetime')]
    private \DateTimeInterface $updatedAt;

    public function getId(): ?int { return $this->id; }
    public function getUser(): User { return $this->user; }
    public function getNickname(): ?string { return $this->nickname; }
    public function getDateOfBirth(): ?\DateTimeInterface { return $this->dateOfBirth; }
    public function getNationality(): ?string { return $this->nationality; }
    public function getPhotoUrl(): ?string { return $this->photoUrl; }
    public function getWins(): int { return $this->wins; }
    public function getLosses(): int { return $this->losses; }
    public function getDraws(): int { return $this->draws; }
    public function getStatus(): string { return $this->status; }
    public function getRecord(): string { return $this->wins . '-' . $this->losses . '-' . $this->draws; }

    public function getDisplayName(): string
    {
        return $this->nickname
            ? $this->user->getFirstName() . ' "' . $this->nickname . '" ' . $this->user->getLastName()
            : $this->user->getFullName();
    }

    public function __toString(): string { return $this->getDisplayName(); }
}
