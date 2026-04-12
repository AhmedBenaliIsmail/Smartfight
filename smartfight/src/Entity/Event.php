<?php

namespace App\Entity;

use App\Repository\EventRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: EventRepository::class)]
#[ORM\Table(name: 'event')]
#[ORM\HasLifecycleCallbacks]
class Event
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: 'integer')]
    private ?int $id = null;

    #[ORM\Column(type: 'string', length: 200)]
    private string $name;

    #[ORM\Column(type: 'text', nullable: true)]
    private ?string $description = null;

    #[ORM\Column(name: 'start_date', type: 'date')]
    private \DateTimeInterface $startDate;

    #[ORM\Column(name: 'end_date', type: 'date')]
    private \DateTimeInterface $endDate;

    #[ORM\Column(type: 'string', length: 20)]
    private string $status = 'SCHEDULED';

    #[ORM\Column(type: 'string', length: 10)]
    private string $visibility = 'PUBLIC';

    #[ORM\Column(type: 'integer', options: ['default' => 0])]
    private int $capacity = 0;

    #[ORM\ManyToOne(targetEntity: Discipline::class)]
    #[ORM\JoinColumn(name: 'discipline_id', nullable: true)]
    private ?Discipline $discipline = null;

    #[ORM\Column(name: 'venue_id', type: 'integer', nullable: true)]
    private ?int $venueId = null;

    #[ORM\Column(name: 'organizer_id', type: 'integer', nullable: true)]
    private ?int $organizerId = null;

    #[ORM\Column(name: 'is_champions_event', type: 'boolean', options: ['default' => false])]
    private bool $isChampionsEvent = false;

    #[ORM\Column(type: 'string', length: 255, nullable: true)]
    private ?string $location = null;

    #[ORM\Column(name: 'created_at', type: 'datetime')]
    private \DateTimeInterface $createdAt;

    #[ORM\Column(name: 'updated_at', type: 'datetime')]
    private \DateTimeInterface $updatedAt;

    #[ORM\PrePersist]
    public function onPrePersist(): void
    {
        $now = new \DateTime();
        $this->createdAt = $now;
        $this->updatedAt = $now;
    }

    #[ORM\PreUpdate]
    public function onPreUpdate(): void
    {
        $this->updatedAt = new \DateTime();
    }

    public function getId(): ?int { return $this->id; }
    public function getName(): string { return $this->name; }
    public function setName(string $name): static { $this->name = $name; return $this; }
    public function getDescription(): ?string { return $this->description; }
    public function setDescription(?string $description): static { $this->description = $description; return $this; }
    public function getStartDate(): \DateTimeInterface { return $this->startDate; }
    public function setStartDate(\DateTimeInterface $startDate): static { $this->startDate = $startDate; return $this; }
    public function getEndDate(): \DateTimeInterface { return $this->endDate; }
    public function setEndDate(\DateTimeInterface $endDate): static { $this->endDate = $endDate; return $this; }
    public function getStatus(): string { return $this->status; }
    public function setStatus(string $status): static { $this->status = $status; return $this; }
    public function getVisibility(): string { return $this->visibility; }
    public function setVisibility(string $visibility): static { $this->visibility = $visibility; return $this; }
    public function getCapacity(): int { return $this->capacity; }
    public function setCapacity(int $capacity): static { $this->capacity = $capacity; return $this; }
    public function getDiscipline(): ?Discipline { return $this->discipline; }
    public function setDiscipline(?Discipline $discipline): static { $this->discipline = $discipline; return $this; }
    public function getVenueId(): ?int { return $this->venueId; }
    public function setVenueId(?int $venueId): static { $this->venueId = $venueId; return $this; }
    public function getOrganizerId(): ?int { return $this->organizerId; }
    public function setOrganizerId(?int $organizerId): static { $this->organizerId = $organizerId; return $this; }
    public function isChampionsEvent(): bool { return $this->isChampionsEvent; }
    public function setIsChampionsEvent(bool $isChampionsEvent): static { $this->isChampionsEvent = $isChampionsEvent; return $this; }
    public function getLocation(): ?string { return $this->location; }
    public function setLocation(?string $location): static { $this->location = $location; return $this; }
    public function getCreatedAt(): \DateTimeInterface { return $this->createdAt; }
    public function setCreatedAt(\DateTimeInterface $createdAt): static { $this->createdAt = $createdAt; return $this; }
    public function getUpdatedAt(): \DateTimeInterface { return $this->updatedAt; }
    public function setUpdatedAt(\DateTimeInterface $updatedAt): static { $this->updatedAt = $updatedAt; return $this; }
    public function __toString(): string { return $this->name; }
}
