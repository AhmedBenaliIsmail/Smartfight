<?php
namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;
use App\Repository\FighterRepository;

#[ORM\Entity(repositoryClass: FighterRepository::class)]
#[ORM\Table(name: 'fighters')]
class Fighter
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'fighterId')]
    private ?int $fighterId = null;

    #[ORM\Column(name: 'firstName', length: 100)]
    private ?string $firstName = null;

    #[ORM\Column(name: 'lastName', length: 100)]
    private ?string $lastName = null;

    #[ORM\Column(length: 100, nullable: true)]
    private ?string $nickname = null;

    #[ORM\Column(name: 'weightClass', length: 50, nullable: true)]
    private ?string $weightClass = null;

    #[ORM\Column(length: 50, nullable: true)]
    private ?string $country = null;

    #[ORM\Column(type: 'integer', options: ['default' => 0])]
    private int $wins = 0;

    #[ORM\Column(type: 'integer', options: ['default' => 0])]
    private int $losses = 0;

    #[ORM\Column(type: 'integer', options: ['default' => 0])]
    private int $draws = 0;

    #[ORM\Column(name: 'koWins', type: 'integer', options: ['default' => 0])]
    private int $koWins = 0;

    #[ORM\Column(name: 'submissionWins', type: 'integer', options: ['default' => 0])]
    private int $submissionWins = 0;

    #[ORM\Column(name: 'decisionWins', type: 'integer', options: ['default' => 0])]
    private int $decisionWins = 0;

    #[ORM\Column(name: 'eloRating', type: 'float', options: ['default' => 1500.0])]
    private float $eloRating = 1500.0;

    #[ORM\Column(name: 'performanceScore', type: 'float', options: ['default' => 0.0])]
    private float $performanceScore = 0.0;

    #[ORM\Column(name: 'winStreak', type: 'integer', options: ['default' => 0])]
    private int $winStreak = 0;

    #[ORM\Column(name: 'strengthOfSchedule', type: 'float', options: ['default' => 1500.0])]
    private float $strengthOfSchedule = 1500.0;

    #[ORM\Column(name: 'championsEventWinStreak', type: 'integer', options: ['default' => 0])]
    private int $championsEventWinStreak = 0;

    #[ORM\Column(name: 'titleDefenses', type: 'integer', options: ['default' => 0])]
    private int $titleDefenses = 0;

    #[ORM\Column(type: 'integer', nullable: true)]
    private ?int $height = null;

    #[ORM\Column(type: 'integer', nullable: true)]
    private ?int $reach = null;

    // Getters and Setters
    public function getFighterId(): ?int { return $this->fighterId; }
    public function setFighterId(int $id): self { $this->fighterId = $id; return $this; }

    public function getFirstName(): ?string { return $this->firstName; }
    public function setFirstName(string $v): self { $this->firstName = $v; return $this; }

    public function getLastName(): ?string { return $this->lastName; }
    public function setLastName(string $v): self { $this->lastName = $v; return $this; }

    public function getNickname(): ?string { return $this->nickname; }
    public function setNickname(?string $v): self { $this->nickname = $v; return $this; }

    public function getWeightClass(): ?string { return $this->weightClass; }
    public function setWeightClass(?string $v): self { $this->weightClass = $v; return $this; }

    public function getCountry(): ?string { return $this->country; }
    public function setCountry(?string $v): self { $this->country = $v; return $this; }

    public function getWins(): int { return $this->wins; }
    public function setWins(int $v): self { $this->wins = $v; return $this; }

    public function getLosses(): int { return $this->losses; }
    public function setLosses(int $v): self { $this->losses = $v; return $this; }

    public function getDraws(): int { return $this->draws; }
    public function setDraws(int $v): self { $this->draws = $v; return $this; }

    public function getKoWins(): int { return $this->koWins; }
    public function setKoWins(int $v): self { $this->koWins = $v; return $this; }

    public function getSubmissionWins(): int { return $this->submissionWins; }
    public function setSubmissionWins(int $v): self { $this->submissionWins = $v; return $this; }

    public function getDecisionWins(): int { return $this->decisionWins; }
    public function setDecisionWins(int $v): self { $this->decisionWins = $v; return $this; }

    public function getEloRating(): float { return $this->eloRating; }
    public function setEloRating(float $v): self { $this->eloRating = $v; return $this; }

    public function getPerformanceScore(): float { return $this->performanceScore; }
    public function setPerformanceScore(float $v): self { $this->performanceScore = $v; return $this; }

    public function getWinStreak(): int { return $this->winStreak; }
    public function setWinStreak(int $v): self { $this->winStreak = $v; return $this; }

    public function getStrengthOfSchedule(): float { return $this->strengthOfSchedule; }
    public function setStrengthOfSchedule(float $v): self { $this->strengthOfSchedule = $v; return $this; }

    public function getChampionsEventWinStreak(): int { return $this->championsEventWinStreak; }
    public function setChampionsEventWinStreak(int $v): self { $this->championsEventWinStreak = $v; return $this; }

    public function getTitleDefenses(): int { return $this->titleDefenses; }
    public function setTitleDefenses(int $v): self { $this->titleDefenses = $v; return $this; }

    public function getHeight(): ?int { return $this->height; }
    public function setHeight(?int $v): self { $this->height = $v; return $this; }

    public function getReach(): ?int { return $this->reach; }
    public function setReach(?int $v): self { $this->reach = $v; return $this; }

    public function getFullName(): string { return $this->firstName . ' ' . $this->lastName; }
    public function getTotalFights(): int { return $this->wins + $this->losses + $this->draws; }

    public function __toString(): string
    {
        return $this->getFullName() . ' (' . $this->weightClass . ') | ELO: ' . (int)$this->eloRating;
    }
}
