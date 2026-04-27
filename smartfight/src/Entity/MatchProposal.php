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

    #[ORM\Column(type: 'decimal', precision: 5, scale: 2, nullable: true)]
    private ?string $compatibility = null;

    #[ORM\Column(type: 'string', length: 20)]
    private string $status = 'PENDING';

    #[ORM\Column(name: 'proposed_at', type: 'datetime')]
    private \DateTimeInterface $proposedAt;

    #[ORM\Column(type: 'text', nullable: true)]
    private ?string $notes = null;

    public function getId(): ?int { return $this->id; }
    public function getEvent(): ?Event { return $this->event; }
    public function getFighter1(): Fighter { return $this->fighter1; }
    public function getFighter2(): Fighter { return $this->fighter2; }
    public function getCompatibility(): ?string { return $this->compatibility; }
    public function getStatus(): string { return $this->status; }
    public function getProposedAt(): \DateTimeInterface { return $this->proposedAt; }
    public function getNotes(): ?string { return $this->notes; }

    public function getFightLabel(): string
    {
        return $this->fighter1->getDisplayName() . ' vs ' . $this->fighter2->getDisplayName();
    }

    public function __toString(): string { return $this->getFightLabel(); }
}
