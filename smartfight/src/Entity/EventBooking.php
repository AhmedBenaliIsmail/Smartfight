<?php

namespace App\Entity;

use App\Repository\EventBookingRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: EventBookingRepository::class)]
#[ORM\Table(name: 'event_booking')]
#[ORM\UniqueConstraint(name: 'uq_eb_event_user', columns: ['event_id', 'user_id'])]
#[ORM\UniqueConstraint(name: 'uq_eb_reference', columns: ['booking_reference'])]
#[ORM\HasLifecycleCallbacks]
class EventBooking
{
    public const STATUS_CONFIRMED = 'CONFIRMED';
    public const STATUS_CANCELLED = 'CANCELLED';

    public const TYPE_VIP = 'VIP';
    public const TYPE_REGULAR = 'REGULAR';
    public const TYPE_STANDING = 'STANDING';

    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: 'integer')]
    private ?int $id = null;

    #[ORM\ManyToOne(targetEntity: Event::class)]
    #[ORM\JoinColumn(name: 'event_id', nullable: false)]
    private Event $event;

    #[ORM\ManyToOne(targetEntity: User::class)]
    #[ORM\JoinColumn(name: 'user_id', nullable: false)]
    private User $user;

    #[ORM\Column(name: 'booking_status', type: 'string', length: 20)]
    private string $bookingStatus = self::STATUS_CONFIRMED;

    #[ORM\Column(name: 'ticket_quantity', type: 'integer', options: ['default' => 1])]
    private int $ticketQuantity = 1;

    #[ORM\Column(name: 'total_price', type: 'decimal', precision: 10, scale: 2, options: ['default' => '0.00'])]
    private string $totalPrice = '0.00';

    #[ORM\Column(name: 'ticket_type', type: 'string', length: 20)]
    private string $ticketType = self::TYPE_REGULAR;

    #[ORM\Column(name: 'booking_date', type: 'date')]
    private \DateTimeInterface $bookingDate;

    #[ORM\Column(name: 'booking_reference', type: 'string', length: 64)]
    private string $bookingReference;

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

        if (!isset($this->bookingDate)) {
            $this->bookingDate = new \DateTime();
        }
    }

    #[ORM\PreUpdate]
    public function onPreUpdate(): void
    {
        $this->updatedAt = new \DateTime();
    }

    public function getId(): ?int { return $this->id; }
    public function getEvent(): Event { return $this->event; }
    public function setEvent(Event $event): static { $this->event = $event; return $this; }
    public function getUser(): User { return $this->user; }
    public function setUser(User $user): static { $this->user = $user; return $this; }
    public function getBookingStatus(): string { return $this->bookingStatus; }
    public function setBookingStatus(string $bookingStatus): static { $this->bookingStatus = $bookingStatus; return $this; }
    public function getTicketQuantity(): int { return $this->ticketQuantity; }
    public function setTicketQuantity(int $ticketQuantity): static { $this->ticketQuantity = $ticketQuantity; return $this; }
    public function getTotalPrice(): string { return $this->totalPrice; }
    public function setTotalPrice(string $totalPrice): static { $this->totalPrice = $totalPrice; return $this; }
    public function getTicketType(): string { return $this->ticketType; }
    public function setTicketType(string $ticketType): static { $this->ticketType = $ticketType; return $this; }
    public function getBookingDate(): \DateTimeInterface { return $this->bookingDate; }
    public function setBookingDate(\DateTimeInterface $bookingDate): static { $this->bookingDate = $bookingDate; return $this; }
    public function getBookingReference(): string { return $this->bookingReference; }
    public function setBookingReference(string $bookingReference): static { $this->bookingReference = $bookingReference; return $this; }
    public function getCreatedAt(): \DateTimeInterface { return $this->createdAt; }
    public function getUpdatedAt(): \DateTimeInterface { return $this->updatedAt; }

    public function isConfirmed(): bool
    {
        return $this->bookingStatus === self::STATUS_CONFIRMED;
    }

    public function isCancelled(): bool
    {
        return $this->bookingStatus === self::STATUS_CANCELLED;
    }
}
