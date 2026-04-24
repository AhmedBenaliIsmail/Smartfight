<?php
namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;
use App\Repository\FightResultRepository;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity(repositoryClass: FightResultRepository::class)]
#[ORM\Table(name: 'fight_results')]
class FightResult
{
    public const METHOD_KO = 'KO';
    public const METHOD_DQ = 'DQ';
    public const METHOD_DRAW = 'DRAW';
    public const METHOD_DECISION = 'DECISION';

    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'resultId')]
    private ?int $resultId = null;

    #[ORM\ManyToOne(targetEntity: Event::class)]
    #[ORM\JoinColumn(name: 'eventId', referencedColumnName: 'eventId', nullable: false)]
    private ?Event $event = null;

    #[ORM\Column(name: 'fightNumber')]
    private int $fightNumber = 1;

    #[ORM\ManyToOne(targetEntity: Fighter::class)]
    #[ORM\JoinColumn(name: 'fighter1Id', referencedColumnName: 'fighterId', nullable: false)]
    private ?Fighter $fighter1 = null;

    #[ORM\ManyToOne(targetEntity: Fighter::class)]
    #[ORM\JoinColumn(name: 'fighter2Id', referencedColumnName: 'fighterId', nullable: false)]
    private ?Fighter $fighter2 = null;

    #[ORM\ManyToOne(targetEntity: Fighter::class)]
    #[ORM\JoinColumn(name: 'winnerId', referencedColumnName: 'fighterId', nullable: true)]
    private ?Fighter $winner = null;

    #[ORM\Column(name: 'methodOfVictory', length: 50, nullable: true)]
    #[Assert\Choice(choices: [self::METHOD_KO, self::METHOD_DQ, self::METHOD_DRAW, self::METHOD_DECISION])]
    private ?string $methodOfVictory = null;

    #[ORM\Column(name: 'decision_type', length: 10, nullable: true)]
    #[Assert\Choice(choices: ['UD', 'SD', 'MD'])]
    private ?string $decisionType = null;

    #[ORM\Column(name: 'knockdown_round', type: 'integer', nullable: true)]
    #[Assert\Positive]
    #[Assert\LessThanOrEqual(12)]
    private ?int $knockdownRound = null;

    #[ORM\Column(name: 'roundNumber', nullable: true)]
    #[Assert\Positive]
    #[Assert\LessThanOrEqual(12)]
    private ?int $roundNumber = null;

    #[ORM\Column(name: 'scheduled_rounds', type: 'integer', options: ['default' => 12])]
    #[Assert\Choice(choices: [4, 6, 8, 10, 12])]
    private int $scheduledRounds = 12;

    #[ORM\Column(name: 'is_belt_fight', type: 'boolean', options: ['default' => false])]
    private bool $isBeltFight = false;

    #[ORM\Column(name: 'belt_organization', length: 20, nullable: true)]
    #[Assert\Choice(choices: ['WBC', 'WBA', 'IBF', 'WBO', 'UNDISPUTED'])]
    private ?string $beltOrganization = null;

    #[ORM\Column(name: 'fighter1_odds', type: 'float', nullable: true)]
    #[Assert\Positive]
    private ?float $fighter1Odds = null;

    #[ORM\Column(name: 'fighter2_odds', type: 'float', nullable: true)]
    #[Assert\Positive]
    private ?float $fighter2Odds = null;

    #[ORM\Column(name: 'fightDate', type: 'datetime', nullable: true)]
    private ?\DateTimeInterface $fightDate = null;

    #[ORM\Column(length: 20, options: ['default' => 'SCHEDULED'])]
    private string $status = 'SCHEDULED';

    public function getResultId(): ?int { return $this->resultId; }
    public function getEvent(): ?Event { return $this->event; }
    public function setEvent(Event $v): self { $this->event = $v; return $this; }
    public function getFightNumber(): int { return $this->fightNumber; }
    public function setFightNumber(int $v): self { $this->fightNumber = $v; return $this; }
    public function getFighter1(): ?Fighter { return $this->fighter1; }
    public function setFighter1(Fighter $v): self { $this->fighter1 = $v; return $this; }
    public function getFighter2(): ?Fighter { return $this->fighter2; }
    public function setFighter2(Fighter $v): self { $this->fighter2 = $v; return $this; }
    public function getWinner(): ?Fighter { return $this->winner; }
    public function setWinner(?Fighter $v): self { $this->winner = $v; return $this; }
    public function getMethodOfVictory(): ?string { return $this->methodOfVictory; }
    public function setMethodOfVictory(?string $v): self { $this->methodOfVictory = $v; return $this; }
    
    public function getDecisionType(): ?string { return $this->decisionType; }
    public function setDecisionType(?string $v): self { $this->decisionType = $v; return $this; }

    public function getKnockdownRound(): ?int { return $this->knockdownRound; }
    public function setKnockdownRound(?int $v): self { $this->knockdownRound = $v; return $this; }

    public function getRoundNumber(): ?int { return $this->roundNumber; }
    public function setRoundNumber(?int $v): self { $this->roundNumber = $v; return $this; }
    
    public function getScheduledRounds(): int { return $this->scheduledRounds; }
    public function setScheduledRounds(int $v): self { $this->scheduledRounds = $v; return $this; }

    public function isBeltFight(): bool { return $this->isBeltFight; }
    public function setIsBeltFight(bool $v): self { $this->isBeltFight = $v; return $this; }

    public function getBeltOrganization(): ?string { return $this->beltOrganization; }
    public function setBeltOrganization(?string $v): self { $this->beltOrganization = $v; return $this; }

    public function getFighter1Odds(): ?float { return $this->fighter1Odds; }
    public function setFighter1Odds(?float $v): self { $this->fighter1Odds = $v; return $this; }

    public function getFighter2Odds(): ?float { return $this->fighter2Odds; }
    public function setFighter2Odds(?float $v): self { $this->fighter2Odds = $v; return $this; }
    public function getFightDate(): ?\DateTimeInterface { return $this->fightDate; }
    public function setFightDate(?\DateTimeInterface $v): self { $this->fightDate = $v; return $this; }
    public function getStatus(): string { return $this->status; }
    public function setStatus(string $v): self { $this->status = $v; return $this; }

    public function getLoser(): ?Fighter
    {
        if ($this->winner === null || $this->status !== 'COMPLETED') return null;
        return ($this->winner === $this->fighter1) ? $this->fighter2 : $this->fighter1;
    }

    public function getMethodBonus(): int
    {
        if ($this->methodOfVictory === null) return 0;
        return match (strtoupper($this->methodOfVictory)) {
            self::METHOD_KO => 10,
            self::METHOD_DECISION => 5,
            self::METHOD_DQ => 2,
            self::METHOD_DRAW => 1,
            default => 0,
        };
    }

    public function isDraw(): bool
    {
        return $this->winner === null && $this->status === 'COMPLETED' && $this->methodOfVictory === self::METHOD_DRAW;
    }
}
