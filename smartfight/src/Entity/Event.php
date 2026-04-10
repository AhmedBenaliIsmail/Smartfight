<?php

namespace App\Entity;

use App\Repository\EventRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: EventRepository::class)]
#[ORM\Table(name: 'event')]
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

    #[ORM\Column(name: 'created_at', type: 'datetime')]
    private \DateTimeInterface $createdAt;

    #[ORM\Column(name: 'updated_at', type: 'datetime')]
    private \DateTimeInterface $updatedAt;

    public function getId(): ?int { return $this->id; }
    public function getName(): string { return $this->name; }
    public function getDescription(): ?string { return $this->description; }
    public function getStartDate(): \DateTimeInterface { return $this->startDate; }
    public function getEndDate(): \DateTimeInterface { return $this->endDate; }
    public function getStatus(): string { return $this->status; }
    public function getVisibility(): string { return $this->visibility; }
    public function getCapacity(): int { return $this->capacity; }
    public function getDiscipline(): ?Discipline { return $this->discipline; }
    public function __toString(): string { return $this->name; }
}
