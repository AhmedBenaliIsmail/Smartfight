<?php
namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;
use App\Repository\EventRepository;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity(repositoryClass: EventRepository::class)]
#[ORM\Table(name: 'events')]
class Event
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'eventId')]
    private ?int $eventId = null;

    #[ORM\Column(name: 'eventName', length: 200)]
    #[Assert\NotBlank(message: 'Event name is required')]
    private ?string $eventName = null;

    #[ORM\Column(name: 'eventDate', type: 'date', nullable: true)]
    #[Assert\NotBlank(message: 'Event date is required')]
    private ?\DateTimeInterface $eventDate = null;

    #[ORM\Column(length: 20, options: ['default' => 'INDEPENDENT'])]
    #[Assert\Choice(choices: ['WBC', 'WBA', 'IBF', 'WBO', 'INDEPENDENT'], message: 'Invalid organization')]
    private string $organization = 'INDEPENDENT';

    #[ORM\Column(length: 200, nullable: true)]
    #[Assert\NotBlank(message: 'Venue is required')]
    #[Assert\Length(min: 3, max: 200)]
    private ?string $venue = null;

    #[ORM\Column(length: 100, nullable: true)]
    #[Assert\NotBlank(message: 'City is required')]
    private ?string $city = null;

    #[ORM\Column(length: 2, nullable: true)]
    // #[Assert\Country(message: 'Invalid country code')]
    private ?string $country = null;

    #[ORM\Column(name: 'seat_capacity', type: 'integer', nullable: true)]
    #[Assert\Positive(message: 'Seat capacity must be positive')]
    private ?int $seatCapacity = null;

    #[ORM\Column(name: 'poster_filename', length: 255, nullable: true)]
    private ?string $posterFilename = null;

    #[ORM\Column(length: 20, options: ['default' => 'SCHEDULED'])]
    #[Assert\Choice(choices: ['SCHEDULED', 'LIVE', 'COMPLETED', 'CANCELLED'])]
    private string $status = 'SCHEDULED';

    #[ORM\Column(length: 20, options: ['default' => 'PUBLIC'])]
    #[Assert\Choice(choices: ['PUBLIC', 'PRIVATE', 'DRAFT'])]
    private string $visibility = 'PUBLIC';

    #[ORM\Column(name: 'is_champions_event', type: 'boolean', options: ['default' => false])]
    private bool $isChampionsEvent = false;

    public function getEventId(): ?int { return $this->eventId; }
    public function getEventName(): ?string { return $this->eventName; }
    public function setEventName(string $v): self { $this->eventName = $v; return $this; }
    public function getEventDate(): ?\DateTimeInterface { return $this->eventDate; }
    public function setEventDate(?\DateTimeInterface $v): self { $this->eventDate = $v; return $this; }

    public function getOrganization(): string { return $this->organization; }
    public function setOrganization(string $v): self { $this->organization = $v; return $this; }

    public function getVenue(): ?string { return $this->venue; }
    public function setVenue(?string $v): self { $this->venue = $v; return $this; }

    public function getCity(): ?string { return $this->city; }
    public function setCity(?string $v): self { $this->city = $v; return $this; }

    public function getCountry(): ?string { return $this->country; }
    public function setCountry(?string $v): self { $this->country = $v; return $this; }

    public function getSeatCapacity(): ?int { return $this->seatCapacity; }
    public function setSeatCapacity(?int $v): self { $this->seatCapacity = $v; return $this; }

    public function getPosterFilename(): ?string { return $this->posterFilename; }
    public function setPosterFilename(?string $v): self { $this->posterFilename = $v; return $this; }

    public function getStatus(): string { return $this->status; }
    public function setStatus(string $v): self { $this->status = $v; return $this; }

    public function getVisibility(): string { return $this->visibility; }
    public function setVisibility(string $v): self { $this->visibility = $v; return $this; }

    public function getIsChampionsEvent(): bool { return $this->isChampionsEvent; }
    public function setIsChampionsEvent(bool $v): self { $this->isChampionsEvent = $v; return $this; }

    public function getId(): ?int { return $this->eventId; }
    public function getCapacity(): ?int { return $this->seatCapacity; }
    public function getName(): ?string { return $this->eventName; }
    public function getStartDate(): ?\DateTimeInterface { return $this->eventDate; }

    public function isUpcoming(): bool
    {
        return $this->eventDate !== null && $this->eventDate >= new \DateTime('today');
    }

    public function __toString(): string
    {
        return $this->eventName . ' (' . ($this->eventDate ? $this->eventDate->format('Y-m-d') : '') . ')';
    }
}
