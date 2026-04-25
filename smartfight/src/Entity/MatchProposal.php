<?php

namespace App\Entity;

use App\Enum\CardType;
use App\Enum\MatchStatus;
use App\Repository\MatchProposalRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: MatchProposalRepository::class)]
#[ORM\Table(name: 'match_proposal')]
class MatchProposal
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: 'integer')]
    private ?int $id = null;

    #[ORM\ManyToOne(targetEntity: Event::class)]
    #[ORM\JoinColumn(name: 'event_id', nullable: true)]
    private ?Event $event = null;

    #[ORM\ManyToOne(targetEntity: Fighter::class)]
    #[ORM\JoinColumn(name: 'fighter1_id', nullable: false)]
    private Fighter $fighter1;

    #[ORM\ManyToOne(targetEntity: Fighter::class)]
    #[ORM\JoinColumn(name: 'fighter2_id', nullable: false)]
    private Fighter $fighter2;

    #[ORM\ManyToOne(targetEntity: WeightClass::class)]
    #[ORM\JoinColumn(name: 'weight_class_id', nullable: true)]
    private ?WeightClass $weightClass = null;

    #[ORM\Column(type: 'decimal', precision: 5, scale: 2, nullable: true)]
    private ?string $compatibility = null;

    #[ORM\Column(type: 'string', length: 20)]
    private string $status = 'SCHEDULED';

    #[ORM\Column(name: 'proposed_at', type: 'datetime')]
    private \DateTimeInterface $proposedAt;

    #[ORM\Column(type: 'text', nullable: true)]
    private ?string $notes = null;

    #[ORM\Column(name: 'scheduled_rounds', type: 'integer', nullable: true)]
    private ?int $scheduledRounds = null;

    #[ORM\Column(name: 'is_title_fight', type: 'boolean', options: ['default' => false])]
    private bool $isTitleFight = false;

    #[ORM\Column(name: 'odds_fighter1', type: 'decimal', precision: 5, scale: 2, nullable: true)]
    private ?string $oddsFighter1 = null;

    #[ORM\Column(name: 'odds_fighter2', type: 'decimal', precision: 5, scale: 2, nullable: true)]
    private ?string $oddsFighter2 = null;

    #[ORM\Column(name: 'card_position', type: 'integer', nullable: true)]
    private ?int $cardPosition = null;

    #[ORM\Column(name: 'card_type', type: 'string', length: 20, nullable: true)]
    private ?string $cardType = null;

    public function getId(): ?int { return $this->id; }

    public function getEvent(): ?Event { return $this->event; }
    public function setEvent(?Event $event): static { $this->event = $event; return $this; }

    public function getFighter1(): Fighter { return $this->fighter1; }
    public function setFighter1(Fighter $fighter1): static { $this->fighter1 = $fighter1; return $this; }

    public function getFighter2(): Fighter { return $this->fighter2; }
    public function setFighter2(Fighter $fighter2): static { $this->fighter2 = $fighter2; return $this; }

    public function getWeightClass(): ?WeightClass { return $this->weightClass; }
    public function setWeightClass(?WeightClass $weightClass): static { $this->weightClass = $weightClass; return $this; }

    public function getCompatibility(): ?string { return $this->compatibility; }
    public function setCompatibility(?string $compatibility): static { $this->compatibility = $compatibility; return $this; }

    public function getStatus(): string { return $this->status; }
    public function setStatus(string $status): static { $this->status = $status; return $this; }

    public function getProposedAt(): \DateTimeInterface { return $this->proposedAt; }
    public function setProposedAt(\DateTimeInterface $proposedAt): static { $this->proposedAt = $proposedAt; return $this; }

    public function getNotes(): ?string { return $this->notes; }
    public function setNotes(?string $notes): static { $this->notes = $notes; return $this; }

    public function getScheduledRounds(): ?int { return $this->scheduledRounds; }
    public function setScheduledRounds(?int $scheduledRounds): static { $this->scheduledRounds = $scheduledRounds; return $this; }

    public function isTitleFight(): bool { return $this->isTitleFight; }
    public function setIsTitleFight(bool $isTitleFight): static { $this->isTitleFight = $isTitleFight; return $this; }

    public function getOddsFighter1(): ?string { return $this->oddsFighter1; }
    public function setOddsFighter1(?string $oddsFighter1): static { $this->oddsFighter1 = $oddsFighter1; return $this; }

    public function getOddsFighter2(): ?string { return $this->oddsFighter2; }
    public function setOddsFighter2(?string $oddsFighter2): static { $this->oddsFighter2 = $oddsFighter2; return $this; }

    public function getCardPosition(): ?int { return $this->cardPosition; }
    public function setCardPosition(?int $cardPosition): static { $this->cardPosition = $cardPosition; return $this; }

    public function getCardType(): ?string { return $this->cardType; }
    public function setCardType(?string $cardType): static { $this->cardType = $cardType; return $this; }

    public function getCardTypeLabel(): string
    {
        $case = $this->cardType !== null ? CardType::tryFrom($this->cardType) : null;
        return $case !== null ? $case->label() : ($this->cardType ?? '—');
    }

    public function getStatusLabel(): string
    {
        $case = MatchStatus::tryFrom($this->status);
        return $case !== null ? $case->label() : ucfirst(strtolower(str_replace('_', ' ', $this->status)));
    }

    public function getFightLabel(): string
    {
        return $this->fighter1->getDisplayName() . ' vs ' . $this->fighter2->getDisplayName();
    }

    public function __toString(): string { return $this->getFightLabel(); }
}
