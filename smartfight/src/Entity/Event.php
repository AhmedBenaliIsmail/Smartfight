<?php

namespace App\Entity;

use App\Enum\EventStatus;
use App\Repository\EventRepository;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\HttpFoundation\File\File;
use Vich\UploaderBundle\Mapping\Annotation as Vich;

#[Vich\Uploadable]
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

    #[ORM\Column(name: 'starts_at', type: 'datetime', nullable: true)]
    private ?\DateTimeInterface $startsAt = null;

    #[ORM\Column(name: 'ends_at', type: 'datetime', nullable: true)]
    private ?\DateTimeInterface $endsAt = null;

    #[ORM\Column(type: 'string', length: 20)]
    private string $status = 'SCHEDULED';

    #[ORM\Column(type: 'string', length: 10)]
    private string $visibility = 'PUBLIC';

    #[ORM\Column(type: 'integer', options: ['default' => 0])]
    private int $capacity = 0;

    #[ORM\ManyToOne(targetEntity: Discipline::class)]
    #[ORM\JoinColumn(name: 'discipline_id', nullable: true)]
    private ?Discipline $discipline = null;

    #[ORM\Column(name: 'venue_name', type: 'string', length: 150, nullable: true)]
    private ?string $venueName = null;

    #[ORM\Column(type: 'string', length: 100, nullable: true)]
    private ?string $city = null;

    #[ORM\Column(type: 'string', length: 2, nullable: true)]
    private ?string $country = null;

    #[ORM\Column(name: 'poster_url', type: 'string', length: 255, nullable: true)]
    private ?string $posterUrl = null;

    #[Vich\UploadableField(mapping: 'event_poster', fileNameProperty: 'posterUrl')]
    private ?File $posterFile = null;

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

    public function getStartsAt(): ?\DateTimeInterface { return $this->startsAt; }
    public function setStartsAt(?\DateTimeInterface $startsAt): static { $this->startsAt = $startsAt; return $this; }

    public function getEndsAt(): ?\DateTimeInterface { return $this->endsAt; }
    public function setEndsAt(?\DateTimeInterface $endsAt): static { $this->endsAt = $endsAt; return $this; }

    public function getStatus(): string { return $this->status; }
    public function setStatus(string $status): static { $this->status = $status; return $this; }

    public function getStatusLabel(): string
    {
        $case = EventStatus::tryFrom($this->status);
        return $case !== null ? $case->label() : $this->status;
    }

    public function getVisibility(): string { return $this->visibility; }
    public function setVisibility(string $visibility): static { $this->visibility = $visibility; return $this; }

    public function getCapacity(): int { return $this->capacity; }
    public function setCapacity(int $capacity): static { $this->capacity = $capacity; return $this; }

    public function getDiscipline(): ?Discipline { return $this->discipline; }
    public function setDiscipline(?Discipline $discipline): static { $this->discipline = $discipline; return $this; }

    public function getVenueName(): ?string { return $this->venueName; }
    public function setVenueName(?string $venueName): static { $this->venueName = $venueName; return $this; }

    public function getCity(): ?string { return $this->city; }
    public function setCity(?string $city): static { $this->city = $city; return $this; }

    public function getCountry(): ?string { return $this->country; }
    public function setCountry(?string $country): static { $this->country = $country; return $this; }

    public function getPosterUrl(): ?string { return $this->posterUrl; }
    public function setPosterUrl(?string $posterUrl): static { $this->posterUrl = $posterUrl; return $this; }

    public function getPosterFile(): ?File { return $this->posterFile; }
    public function setPosterFile(?File $posterFile = null): static
    {
        $this->posterFile = $posterFile;
        if ($posterFile !== null) { $this->updatedAt = new \DateTime(); }
        return $this;
    }

    public function getCreatedAt(): \DateTimeInterface { return $this->createdAt; }
    public function setCreatedAt(\DateTimeInterface $createdAt): static { $this->createdAt = $createdAt; return $this; }

    public function getUpdatedAt(): \DateTimeInterface { return $this->updatedAt; }
    public function setUpdatedAt(\DateTimeInterface $updatedAt): static { $this->updatedAt = $updatedAt; return $this; }

    public function __toString(): string { return $this->name; }
}
