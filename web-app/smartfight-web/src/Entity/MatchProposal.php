<?php

namespace App\Entity;

use App\Repository\MatchProposalRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: MatchProposalRepository::class)]
#[ORM\Table(name: 'match_proposal')]
class MatchProposal
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'id', type: 'integer')]
    private ?int $id = null;

    #[ORM\Column(name: 'event_id', type: 'integer', nullable: true)]
    private ?int $eventId = null;

    #[ORM\Column(name: 'fighter1_id', type: 'integer', nullable: true)]
    private ?int $fighter1Id = null;

    #[ORM\Column(name: 'fighter2_id', type: 'integer', nullable: true)]
    private ?int $fighter2Id = null;

    #[ORM\Column(name: 'compatibility_score', type: 'float', nullable: true)]
    private ?float $compatibilityScore = null;

    #[ORM\Column(name: 'status', type: 'string', length: 50, nullable: true)]
    private ?string $status = null;

    public function getId(): ?int { return $this->id; }
    public function getEventId(): ?int { return $this->eventId; }
    public function setEventId(?int $eventId): static { $this->eventId = $eventId; return $this; }
    public function getFighter1Id(): ?int { return $this->fighter1Id; }
    public function setFighter1Id(?int $fighter1Id): static { $this->fighter1Id = $fighter1Id; return $this; }
    public function getFighter2Id(): ?int { return $this->fighter2Id; }
    public function setFighter2Id(?int $fighter2Id): static { $this->fighter2Id = $fighter2Id; return $this; }
    public function getCompatibilityScore(): ?float { return $this->compatibilityScore; }
    public function setCompatibilityScore(?float $compatibilityScore): static { $this->compatibilityScore = $compatibilityScore; return $this; }
    public function getStatus(): ?string { return $this->status; }
    public function setStatus(?string $status): static { $this->status = $status; return $this; }

    public function __toString(): string
    {
        return 'Proposal #' . $this->id;
    }
}
