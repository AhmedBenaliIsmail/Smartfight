<?php
namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;
use App\Repository\EventRepository;

#[ORM\Entity(repositoryClass: EventRepository::class)]
#[ORM\Table(name: 'events')]
class Event
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'eventId')]
    private ?int $eventId = null;

    #[ORM\Column(name: 'eventName', length: 200)]
    private ?string $eventName = null;

    #[ORM\Column(name: 'eventDate', type: 'date', nullable: true)]
    private ?\DateTimeInterface $eventDate = null;

    #[ORM\Column(length: 200, nullable: true)]
    private ?string $location = null;

    #[ORM\Column(name: 'isChampionsEvent', type: 'boolean', options: ['default' => false])]
    private bool $isChampionsEvent = false;

    public function getEventId(): ?int { return $this->eventId; }
    public function getEventName(): ?string { return $this->eventName; }
    public function setEventName(string $v): self { $this->eventName = $v; return $this; }
    public function getEventDate(): ?\DateTimeInterface { return $this->eventDate; }
    public function setEventDate(?\DateTimeInterface $v): self { $this->eventDate = $v; return $this; }
    public function getLocation(): ?string { return $this->location; }
    public function setLocation(?string $v): self { $this->location = $v; return $this; }

    public function isChampionsEvent(): bool { return $this->isChampionsEvent; }
    public function setIsChampionsEvent(bool $v): self { $this->isChampionsEvent = $v; return $this; }

    public function isUpcoming(): bool
    {
        return $this->eventDate !== null && $this->eventDate >= new \DateTime('today');
    }

    public function __toString(): string
    {
        return $this->eventName . ' (' . ($this->eventDate ? $this->eventDate->format('Y-m-d') : '') . ')';
    }
}
