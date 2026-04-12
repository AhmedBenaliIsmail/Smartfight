<?php
namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;
use App\Repository\FightResultRepository;

#[ORM\Entity(repositoryClass: FightResultRepository::class)]
#[ORM\Table(name: 'fight_results')]
class FightResult
{
    public const METHOD_KO_TKO = 'KO/TKO';
    public const METHOD_SUBMISSION = 'SUBMISSION';
    public const METHOD_DECISION = 'DECISION';
    public const METHOD_DQ = 'DISQUALIFICATION';
    public const METHOD_DRAW = 'DRAW';

    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'resultId')]
    private ?int $resultId = null;

    #[ORM\Column(name: 'eventId')]
    private ?int $eventId = null;

    #[ORM\Column(name: 'fightNumber')]
    private int $fightNumber = 1;

    #[ORM\Column(name: 'fighter1Id')]
    private ?int $fighter1Id = null;

    #[ORM\Column(name: 'fighter2Id')]
    private ?int $fighter2Id = null;

    #[ORM\Column(name: 'winnerId', nullable: true)]
    private ?int $winnerId = null;

    #[ORM\Column(name: 'methodOfVictory', length: 50, nullable: true)]
    private ?string $methodOfVictory = null;

    #[ORM\Column(name: 'roundNumber', nullable: true)]
    private ?int $roundNumber = null;

    #[ORM\Column(name: 'fightDate', type: 'datetime', nullable: true)]
    private ?\DateTimeInterface $fightDate = null;

    #[ORM\Column(length: 20, options: ['default' => 'SCHEDULED'])]
    private string $status = 'SCHEDULED';

    public function getResultId(): ?int { return $this->resultId; }
    public function getEventId(): ?int { return $this->eventId; }
    public function setEventId(int $v): self { $this->eventId = $v; return $this; }
    public function getFightNumber(): int { return $this->fightNumber; }
    public function setFightNumber(int $v): self { $this->fightNumber = $v; return $this; }
    public function getFighter1Id(): ?int { return $this->fighter1Id; }
    public function setFighter1Id(int $v): self { $this->fighter1Id = $v; return $this; }
    public function getFighter2Id(): ?int { return $this->fighter2Id; }
    public function setFighter2Id(int $v): self { $this->fighter2Id = $v; return $this; }
    public function getWinnerId(): ?int { return $this->winnerId; }
    public function setWinnerId(?int $v): self { $this->winnerId = $v; return $this; }
    public function getMethodOfVictory(): ?string { return $this->methodOfVictory; }
    public function setMethodOfVictory(?string $v): self { $this->methodOfVictory = $v; return $this; }
    public function getRoundNumber(): ?int { return $this->roundNumber; }
    public function setRoundNumber(?int $v): self { $this->roundNumber = $v; return $this; }
    public function getFightDate(): ?\DateTimeInterface { return $this->fightDate; }
    public function setFightDate(?\DateTimeInterface $v): self { $this->fightDate = $v; return $this; }
    public function getStatus(): string { return $this->status; }
    public function setStatus(string $v): self { $this->status = $v; return $this; }

    public function getLoserId(): ?int
    {
        if ($this->winnerId === null || $this->status !== 'COMPLETED') return null;
        return ($this->winnerId === $this->fighter1Id) ? $this->fighter2Id : $this->fighter1Id;
    }

    public function getMethodBonus(): int
    {
        if ($this->methodOfVictory === null) return 0;
        return match (strtoupper($this->methodOfVictory)) {
            'KO/TKO', 'KO', 'TKO' => 10,
            'SUBMISSION' => 8,
            'DECISION' => 5,
            'DISQUALIFICATION' => 3,
            default => 0,
        };
    }

    public function isDraw(): bool
    {
        return $this->winnerId === null && $this->status === 'COMPLETED';
    }
}
