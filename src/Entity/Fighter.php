<?php
namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;
use App\Repository\FighterRepository;
use Symfony\Component\Validator\Constraints as Assert;

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

    #[ORM\Column(length: 100, nullable: true)]
    private ?string $aiStyleTag = null;

    #[ORM\Column(type: 'text', nullable: true)]
    private ?string $aiDescription = null;

    #[ORM\ManyToOne(targetEntity: WeightDivision::class)]
    #[ORM\JoinColumn(name: 'weight_division_id', referencedColumnName: 'id', nullable: true)]
    #[Assert\NotNull(message: 'Weight division is required.')]
    private ?WeightDivision $weightDivision = null;

    #[ORM\Column(length: 2, nullable: true)]
    #[Assert\Country(message: 'Invalid country code.')]
    private ?string $nationality = null;

    #[ORM\Column(name: 'photo_filename', length: 255, nullable: true)]
    private ?string $photoFilename = null;

    #[ORM\Column(type: 'integer', options: ['default' => 0])]
    private int $wins = 0;

    #[ORM\Column(type: 'integer', options: ['default' => 0])]
    private int $losses = 0;

    #[ORM\Column(type: 'integer', options: ['default' => 0])]
    private int $draws = 0;

    #[ORM\Column(name: 'koWins', type: 'integer', options: ['default' => 0])]
    private int $koWins = 0;

    #[ORM\Column(name: 'technical_wins', type: 'integer', options: ['default' => 0])]
    #[Assert\PositiveOrZero]
    private int $technicalWins = 0;

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

    #[ORM\Column(name: 'last_fight_date', type: 'date', nullable: true)]
    #[Assert\LessThanOrEqual('today', message: 'Last fight date cannot be in the future.')]
    private ?\DateTimeInterface $lastFightDate = null;

    #[ORM\Column(name: 'titleDefenses', type: 'integer', options: ['default' => 0])]
    private int $titleDefenses = 0;

    #[ORM\Column(type: 'integer', nullable: true)]
    private ?int $height = null;

    #[ORM\Column(type: 'integer', nullable: true)]
    private ?int $reach = null;

    #[ORM\Column(name: 'strikes_thrown', type: 'integer', options: ['default' => 0])]
    private int $strikesThrown = 0;

    #[ORM\Column(name: 'strikes_landed', type: 'integer', options: ['default' => 0])]
    private int $strikesLanded = 0;

    #[ORM\Column(length: 255, nullable: true)]
    private ?string $strength = null;

    #[ORM\Column(length: 255, nullable: true)]
    private ?string $weakness = null;

    #[ORM\Column(type: 'integer', nullable: true)]
    private ?int $weight = null;

    #[ORM\Column(type: 'integer', nullable: true)]
    private ?int $age = null;

    #[ORM\Column(name: 'ko_losses', type: 'integer', options: ['default' => 0])]
    private int $koLosses = 0;

    #[ORM\ManyToOne(targetEntity: User::class)]
    #[ORM\JoinColumn(name: "manager_id", referencedColumnName: "userId", nullable: true)]
    private ?User $manager = null;

    // Getters and Setters
    public function getFighterId(): ?int { return $this->fighterId; }
    public function setFighterId(int $id): self { $this->fighterId = $id; return $this; }

    public function getFirstName(): ?string { return $this->firstName; }
    public function setFirstName(string $v): self { $this->firstName = $v; return $this; }

    public function getLastName(): ?string { return $this->lastName; }
    public function setLastName(string $v): self { $this->lastName = $v; return $this; }

    public function getNickname(): ?string { return $this->nickname; }
    public function setNickname(?string $v): self { $this->nickname = $v; return $this; }

    public function getAiStyleTag(): ?string
    {
        return $this->aiStyleTag;
    }

    public function setAiStyleTag(?string $aiStyleTag): static
    {
        $this->aiStyleTag = $aiStyleTag;

        return $this;
    }

    public function getAiDescription(): ?string
    {
        return $this->aiDescription;
    }

    public function setAiDescription(?string $aiDescription): static
    {
        $this->aiDescription = $aiDescription;

        return $this;
    }

    public function getWeightDivision(): ?WeightDivision { return $this->weightDivision; }
    public function setWeightDivision(?WeightDivision $v): self { $this->weightDivision = $v; return $this; }

    public function getNationality(): ?string { return $this->nationality; }
    public function setNationality(?string $v): self { $this->nationality = $v; return $this; }

    public function getPhotoFilename(): ?string { return $this->photoFilename; }
    public function setPhotoFilename(?string $v): self { $this->photoFilename = $v; return $this; }

    public function getWins(): int { return $this->wins; }
    public function setWins(int $v): self { $this->wins = $v; return $this; }

    public function getLosses(): int { return $this->losses; }
    public function setLosses(int $v): self { $this->losses = $v; return $this; }

    public function getDraws(): int { return $this->draws; }
    public function setDraws(int $v): self { $this->draws = $v; return $this; }

    public function getKoWins(): int { return $this->koWins; }
    public function setKoWins(int $v): self { $this->koWins = $v; return $this; }

    public function getTechnicalWins(): int { return $this->technicalWins; }
    public function setTechnicalWins(int $v): self { $this->technicalWins = $v; return $this; }

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

    public function getLastFightDate(): ?\DateTimeInterface { return $this->lastFightDate; }
    public function setLastFightDate(?\DateTimeInterface $v): self { $this->lastFightDate = $v; return $this; }

    public function getTitleDefenses(): int { return $this->titleDefenses; }
    public function setTitleDefenses(int $v): self { $this->titleDefenses = $v; return $this; }

    public function getHeight(): ?int { return $this->height; }
    public function setHeight(?int $v): self { $this->height = $v; return $this; }

    public function getReach(): ?int { return $this->reach; }
    public function setReach(?int $v): self { $this->reach = $v; return $this; }

    public function getStrikesThrown(): int { return $this->strikesThrown; }
    public function setStrikesThrown(int $v): self { $this->strikesThrown = $v; return $this; }

    public function getStrikesLanded(): int { return $this->strikesLanded; }
    public function setStrikesLanded(int $v): self { $this->strikesLanded = $v; return $this; }

    public function getStrength(): ?string { return $this->strength; }
    public function setStrength(?string $v): self { $this->strength = $v; return $this; }

    public function getWeakness(): ?string { return $this->weakness; }
    public function setWeakness(?string $v): self { $this->weakness = $v; return $this; }

    public function getWeight(): ?int { return $this->weight; }
    public function setWeight(?int $v): self { $this->weight = $v; return $this; }

    public function getAge(): ?int { return $this->age; }
    public function setAge(?int $v): self { $this->age = $v; return $this; }

    public function getKoLosses(): int { return $this->koLosses; }
    public function setKoLosses(int $v): self { $this->koLosses = $v; return $this; }

    public function getManager(): ?User { return $this->manager; }
    public function setManager(?User $manager): self { $this->manager = $manager; return $this; }

    public function getStrikeAccuracy(): float
    {
        if ($this->strikesThrown <= 0) return 0.0;
        return ($this->strikesLanded / $this->strikesThrown) * 100;
    }

    public function getCalculatedFightingStyle(): string
    {
        if ($this->aiStyleTag) {
            return $this->aiStyleTag;
        }

        $totalFights = $this->getTotalFights();
        if ($totalFights < 3) {
            return 'PROSPECT';
        }

        $koRate = $this->wins > 0 ? ($this->koWins / $this->wins) * 100 : 0;
        $accuracy = $this->getStrikeAccuracy();
        $reachAdvantage = ($this->reach ?? 180) > ($this->height ?? 175);

        // 1. ELITE FINISHERS
        if ($koRate >= 75) {
            return 'SLUGGER';
        }

        // 2. TACTICAL HYBRIDS
        if ($koRate >= 45 && $koRate < 75) {
            if ($accuracy > 42) return 'TACTICIAN';
            return 'BOXER-PUNCHER';
        }

        // 3. SPECIALIZED STYLES
        if ($accuracy > 45) {
            return 'SHARPSHOOTER';
        }

        if ($reachAdvantage && $accuracy > 35) {
            return 'OUT-BOXER';
        }

        if ($this->strikesThrown > 2000 && $totalFights > 0 && ($this->strikesThrown / $totalFights) > 500) {
            return 'PRESSURE FIGHTER';
        }

        if ($this->losses >= $this->wins && $totalFights > 10) {
            return 'GATEKEEPER';
        }

        return 'BALANCED';
    }

    public function getFullName(): string { return $this->firstName . ' ' . $this->lastName; }
    public function getTotalFights(): int { return $this->wins + $this->losses + $this->draws; }

    public function __toString(): string
    {
        return $this->getFullName() . ' (' . ($this->weightDivision ? $this->weightDivision->getName() : 'Unclassified') . ') | ELO: ' . (int)$this->eloRating;
    }
}
