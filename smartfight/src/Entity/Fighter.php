<?php

namespace App\Entity;

use App\Repository\FighterRepository;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\HttpFoundation\File\File;
use Vich\UploaderBundle\Mapping\Annotation as Vich;

#[Vich\Uploadable]
#[ORM\Entity(repositoryClass: FighterRepository::class)]
#[ORM\Table(name: 'fighter')]
#[ORM\HasLifecycleCallbacks]
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

    #[Vich\UploadableField(mapping: 'fighter_photo', fileNameProperty: 'photoUrl')]
    private ?File $photoFile = null;

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

    #[ORM\PrePersist]
    public function onPrePersist(): void
    {
        $this->createdAt = new \DateTime();
        $this->updatedAt = new \DateTime();
    }

    #[ORM\PreUpdate]
    public function onPreUpdate(): void
    {
        $this->updatedAt = new \DateTime();
    }

    public function getId(): ?int { return $this->id; }
    public function getUser(): User { return $this->user; }
    public function setUser(User $user): static { $this->user = $user; return $this; }
    public function getNickname(): ?string { return $this->nickname; }
    public function setNickname(?string $nickname): static { $this->nickname = $nickname; return $this; }
    public function getDateOfBirth(): ?\DateTimeInterface { return $this->dateOfBirth; }
    public function setDateOfBirth(?\DateTimeInterface $dateOfBirth): static { $this->dateOfBirth = $dateOfBirth; return $this; }
    public function getNationality(): ?string { return $this->nationality; }
    public function setNationality(?string $nationality): static { $this->nationality = $nationality; return $this; }
    public function getPhotoUrl(): ?string { return $this->photoUrl; }
    public function setPhotoUrl(?string $photoUrl): static { $this->photoUrl = $photoUrl; return $this; }
    public function getWeightClassId(): ?int { return $this->weightClassId; }
    public function setWeightClassId(?int $weightClassId): static { $this->weightClassId = $weightClassId; return $this; }
    public function getWins(): int { return $this->wins; }
    public function setWins(int $wins): static { $this->wins = $wins; return $this; }
    public function getLosses(): int { return $this->losses; }
    public function setLosses(int $losses): static { $this->losses = $losses; return $this; }
    public function getDraws(): int { return $this->draws; }
    public function setDraws(int $draws): static { $this->draws = $draws; return $this; }
    public function getStatus(): string { return $this->status; }
    public function setStatus(string $status): static { $this->status = $status; return $this; }
    public function getCreatedAt(): \DateTimeInterface { return $this->createdAt; }
    public function setCreatedAt(\DateTimeInterface $createdAt): static { $this->createdAt = $createdAt; return $this; }
    public function getUpdatedAt(): \DateTimeInterface { return $this->updatedAt; }
    public function setUpdatedAt(\DateTimeInterface $updatedAt): static { $this->updatedAt = $updatedAt; return $this; }

    public function setPhotoFile(?File $photoFile = null): static
    {
        $this->photoFile = $photoFile;
        if ($photoFile !== null) { $this->updatedAt = new \DateTime(); }
        return $this;
    }
    public function getPhotoFile(): ?File { return $this->photoFile; }

    public function getRecord(): string { return $this->wins . '-' . $this->losses . '-' . $this->draws; }

    public function getDisplayName(): string
    {
        return $this->nickname
            ? $this->user->getFirstName() . ' "' . $this->nickname . '" ' . $this->user->getLastName()
            : $this->user->getFullName();
    }

    public function __toString(): string { return $this->getDisplayName(); }
}
