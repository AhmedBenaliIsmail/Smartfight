<?php

namespace App\Entity;

use App\Repository\FanNotificationRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: FanNotificationRepository::class)]
#[ORM\Table(name: 'fan_notification')]
class FanNotification
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: 'integer')]
    private ?int $id = null;

    #[ORM\ManyToOne(targetEntity: User::class)]
    #[ORM\JoinColumn(name: 'fan_id', nullable: false)]
    private User $fan;

    #[ORM\Column(type: 'string', length: 30)]
    private string $type;

    #[ORM\Column(type: 'string', length: 200)]
    private string $title;

    #[ORM\Column(type: 'text')]
    private string $message;

    #[ORM\ManyToOne(targetEntity: Event::class)]
    #[ORM\JoinColumn(name: 'related_event_id', nullable: true)]
    private ?Event $relatedEvent = null;

    #[ORM\Column(name: 'related_fight_id', type: 'integer', nullable: true)]
    private ?int $relatedFightId = null;

    #[ORM\Column(name: 'is_read', type: 'boolean', options: ['default' => false])]
    private bool $isRead = false;

    #[ORM\Column(name: 'created_at', type: 'datetime')]
    private \DateTimeInterface $createdAt;

    public function __construct()
    {
        $this->createdAt = new \DateTime();
    }

    public function getId(): ?int { return $this->id; }
    public function getFan(): User { return $this->fan; }
    public function setFan(User $fan): static { $this->fan = $fan; return $this; }
    public function getType(): string { return $this->type; }
    public function setType(string $type): static { $this->type = $type; return $this; }
    public function getTitle(): string { return $this->title; }
    public function setTitle(string $title): static { $this->title = $title; return $this; }
    public function getMessage(): string { return $this->message; }
    public function setMessage(string $message): static { $this->message = $message; return $this; }
    public function getRelatedEvent(): ?Event { return $this->relatedEvent; }
    public function setRelatedEvent(?Event $relatedEvent): static { $this->relatedEvent = $relatedEvent; return $this; }
    public function getRelatedFightId(): ?int { return $this->relatedFightId; }
    public function setRelatedFightId(?int $relatedFightId): static { $this->relatedFightId = $relatedFightId; return $this; }
    public function isRead(): bool { return $this->isRead; }
    public function setIsRead(bool $isRead): static { $this->isRead = $isRead; return $this; }
    public function getCreatedAt(): \DateTimeInterface { return $this->createdAt; }

    public function getTypeIcon(): string
    {
        return match ($this->type) {
            'NEW_EVENT' => '📅',
            'PREDICTION_SCORED' => '🏆',
            'LEADERBOARD_CHANGE' => '📊',
            'ADMIN_BROADCAST' => '📢',
            default => '🔔',
        };
    }
}
