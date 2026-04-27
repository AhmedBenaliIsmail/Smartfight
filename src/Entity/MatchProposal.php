<?php

namespace App\Entity;

use App\Repository\MatchProposalRepository;
use App\Entity\WeightDivision;
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
    #[ORM\JoinColumn(name: 'event_id', referencedColumnName: 'eventId', nullable: true)]
    private ?Event $event = null;

    #[ORM\ManyToOne(targetEntity: Fighter::class)]
    #[ORM\JoinColumn(name: 'fighter1_id', referencedColumnName: 'fighterId', nullable: false)]
    private Fighter $fighter1;

    #[ORM\ManyToOne(targetEntity: Fighter::class)]
    #[ORM\JoinColumn(name: 'fighter2_id', referencedColumnName: 'fighterId', nullable: false)]
    private Fighter $fighter2;

    #[ORM\Column(type: 'decimal', precision: 5, scale: 2, nullable: true)]
    private ?string $compatibility = null;

    #[ORM\Column(type: 'string', length: 20)]
    private string $status = 'PENDING';

    #[ORM\Column(name: 'proposed_at', type: 'datetime')]
    private \DateTimeInterface $proposedAt;

    #[ORM\Column(type: 'text', nullable: true)]
    private ?string $notes = null;

    #[ORM\Column(name: 'vote_count', type: 'integer', options: ['default' => 0])]
    private int $voteCount = 0;

    #[ORM\ManyToOne(targetEntity: WeightDivision::class)]
    #[ORM\JoinColumn(name: 'weight_division_id', referencedColumnName: 'id', nullable: true)]
    private ?WeightDivision $weightDivision = null;

    public function getId(): ?int { return $this->id; }

    public function getEvent(): ?Event { return $this->event; }
    public function setEvent(?Event $event): self { $this->event = $event; return $this; }

    public function getFighter1(): Fighter { return $this->fighter1; }
    public function setFighter1(Fighter $fighter): self { $this->fighter1 = $fighter; return $this; }

    public function getFighter2(): Fighter { return $this->fighter2; }
    public function setFighter2(Fighter $fighter): self { $this->fighter2 = $fighter; return $this; }

    public function getCompatibility(): ?string { return $this->compatibility; }
    public function setCompatibility(?string $v): self { $this->compatibility = $v; return $this; }

    public function getStatus(): string { return $this->status; }
    public function setStatus(string $v): self { $this->status = $v; return $this; }

    public function getProposedAt(): \DateTimeInterface { return $this->proposedAt; }
    public function setProposedAt(\DateTimeInterface $v): self { $this->proposedAt = $v; return $this; }

    public function getNotes(): ?string { return $this->notes; }
    public function setNotes(?string $v): self { $this->notes = $v; return $this; }

    public function getVoteCount(): int { return $this->voteCount; }
    public function setVoteCount(int $v): self { $this->voteCount = $v; return $this; }
    public function incrementVoteCount(): self { $this->voteCount++; return $this; }

    public function getWeightDivision(): ?WeightDivision { return $this->weightDivision; }
    public function setWeightDivision(?WeightDivision $v): self { $this->weightDivision = $v; return $this; }

    public function getFightLabel(): string
    {
        return $this->fighter1->getFullName() . ' vs ' . $this->fighter2->getFullName();
    }

    public function __toString(): string { return $this->getFightLabel(); }
}
