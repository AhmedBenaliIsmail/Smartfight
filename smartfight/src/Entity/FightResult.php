<?php

namespace App\Entity;

use App\Enum\FightMethod;
use App\Repository\FightResultRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: FightResultRepository::class)]
#[ORM\Table(name: 'fight_result')]
#[ORM\HasLifecycleCallbacks]
class FightResult
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: 'integer')]
    private ?int $id = null;

    #[ORM\ManyToOne(targetEntity: Event::class)]
    #[ORM\JoinColumn(name: 'event_id', nullable: false)]
    private Event $event;

    #[ORM\ManyToOne(targetEntity: MatchProposal::class)]
    #[ORM\JoinColumn(name: 'match_id', nullable: true)]
    private ?MatchProposal $match = null;

    #[ORM\ManyToOne(targetEntity: Fighter::class)]
    #[ORM\JoinColumn(name: 'fighter_red_id', nullable: false)]
    private Fighter $fighterRed;

    #[ORM\ManyToOne(targetEntity: Fighter::class)]
    #[ORM\JoinColumn(name: 'fighter_blue_id', nullable: false)]
    private Fighter $fighterBlue;

    #[ORM\ManyToOne(targetEntity: Fighter::class)]
    #[ORM\JoinColumn(name: 'winner_id', nullable: true)]
    private ?Fighter $winner = null;

    #[ORM\Column(type: 'string', length: 20)]
    private string $method;

    #[ORM\Column(name: 'fight_number', type: 'integer', options: ['default' => 1])]
    private int $fightNumber = 1;

    #[ORM\Column(type: 'string', length: 20, options: ['default' => 'COMPLETED'])]
    private string $status = 'COMPLETED';

    #[ORM\Column(name: 'round_ended', type: 'integer', nullable: true)]
    private ?int $roundEnded = null;

    #[ORM\Column(name: 'time_ended', type: 'time', nullable: true)]
    private ?\DateTimeInterface $timeEnded = null;

    #[ORM\Column(type: 'text', nullable: true)]
    private ?string $notes = null;

    #[ORM\Column(name: 'fight_date', type: 'date')]
    private \DateTimeInterface $fightDate;

    #[ORM\Column(name: 'knockdowns_fighter_red', type: 'integer', options: ['default' => 0])]
    private int $knockdownsFighterRed = 0;

    #[ORM\Column(name: 'knockdowns_fighter_blue', type: 'integer', options: ['default' => 0])]
    private int $knockdownsFighterBlue = 0;

    #[ORM\Column(name: 'created_at', type: 'datetime')]
    private \DateTimeInterface $createdAt;

    #[ORM\PrePersist]
    public function onPrePersist(): void
    {
        $this->createdAt = new \DateTime();
    }

    public function getId(): ?int { return $this->id; }

    public function getEvent(): Event { return $this->event; }
    public function setEvent(Event $event): static { $this->event = $event; return $this; }

    public function getMatch(): ?MatchProposal { return $this->match; }
    public function setMatch(?MatchProposal $match): static { $this->match = $match; return $this; }

    public function getFighterRed(): Fighter { return $this->fighterRed; }
    public function setFighterRed(Fighter $fighterRed): static { $this->fighterRed = $fighterRed; return $this; }

    public function getFighterBlue(): Fighter { return $this->fighterBlue; }
    public function setFighterBlue(Fighter $fighterBlue): static { $this->fighterBlue = $fighterBlue; return $this; }

    public function getWinner(): ?Fighter { return $this->winner; }
    public function setWinner(?Fighter $winner): static { $this->winner = $winner; return $this; }

    public function getMethod(): string { return $this->method; }
    public function setMethod(string $method): static { $this->method = $method; return $this; }
    public function getMethodOfVictory(): string { return $this->getMethod(); }

    public function getMethodLabel(): string
    {
        $case = FightMethod::tryFrom($this->method);
        return $case !== null ? $case->label() : $this->method;
    }

    public function getFightNumber(): int { return $this->fightNumber; }
    public function setFightNumber(int $fightNumber): static { $this->fightNumber = $fightNumber; return $this; }

    public function getStatus(): string { return $this->status; }
    public function setStatus(string $status): static { $this->status = $status; return $this; }

    public function getRoundEnded(): ?int { return $this->roundEnded; }
    public function setRoundEnded(?int $roundEnded): static { $this->roundEnded = $roundEnded; return $this; }

    public function getTimeEnded(): ?\DateTimeInterface { return $this->timeEnded; }
    public function setTimeEnded(?\DateTimeInterface $timeEnded): static { $this->timeEnded = $timeEnded; return $this; }

    public function getNotes(): ?string { return $this->notes; }
    public function setNotes(?string $notes): static { $this->notes = $notes; return $this; }

    public function getFightDate(): \DateTimeInterface { return $this->fightDate; }
    public function setFightDate(\DateTimeInterface $fightDate): static { $this->fightDate = $fightDate; return $this; }

    public function getKnockdownsFighterRed(): int { return $this->knockdownsFighterRed; }
    public function setKnockdownsFighterRed(int $knockdownsFighterRed): static { $this->knockdownsFighterRed = $knockdownsFighterRed; return $this; }

    public function getKnockdownsFighterBlue(): int { return $this->knockdownsFighterBlue; }
    public function setKnockdownsFighterBlue(int $knockdownsFighterBlue): static { $this->knockdownsFighterBlue = $knockdownsFighterBlue; return $this; }

    public function getCreatedAt(): \DateTimeInterface { return $this->createdAt; }
    public function setCreatedAt(\DateTimeInterface $createdAt): static { $this->createdAt = $createdAt; return $this; }

    public function isDraw(): bool
    {
        return strtoupper($this->method) === 'DRAW';
    }

    public function getFightLabel(): string
    {
        return $this->fighterRed->getDisplayName() . ' vs ' . $this->fighterBlue->getDisplayName();
    }

    public function __toString(): string { return $this->getFightLabel(); }
}
