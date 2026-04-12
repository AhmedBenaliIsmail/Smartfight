<?php
namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;
use App\Repository\FightStatisticRepository;

#[ORM\Entity(repositoryClass: FightStatisticRepository::class)]
#[ORM\Table(name: 'fight_statistic')]
class FightStatistic
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column(name: 'fight_result_id')]
    private ?int $fightResultId = null;

    #[ORM\Column(name: 'fighter_id')]
    private ?int $fighterId = null;

    #[ORM\Column(name: 'strikes_landed', type: 'integer', options: ['default' => 0])]
    private int $strikesLanded = 0;

    #[ORM\Column(name: 'strikes_thrown', type: 'integer', options: ['default' => 0])]
    private int $strikesThrown = 0;

    #[ORM\Column(type: 'integer', options: ['default' => 0])]
    private int $takedowns = 0;

    #[ORM\Column(name: 'takedownAttempts', type: 'integer', options: ['default' => 0])]
    private int $takedownAttempts = 0;

    #[ORM\Column(type: 'integer', options: ['default' => 0])]
    private int $submissions = 0;

    #[ORM\Column(type: 'integer', options: ['default' => 0])]
    private int $knockdowns = 0;

    #[ORM\Column(name: 'controlTimeSeconds', type: 'integer', options: ['default' => 0])]
    private int $controlTimeSeconds = 0;

    public function getId(): ?int { return $this->id; }
    public function getFightResultId(): ?int { return $this->fightResultId; }
    public function setFightResultId(int $v): self { $this->fightResultId = $v; return $this; }
    public function getFighterId(): ?int { return $this->fighterId; }
    public function setFighterId(int $v): self { $this->fighterId = $v; return $this; }
    public function getStrikesLanded(): int { return $this->strikesLanded; }
    public function setStrikesLanded(int $v): self { $this->strikesLanded = $v; return $this; }
    public function getStrikesThrown(): int { return $this->strikesThrown; }
    public function setStrikesThrown(int $v): self { $this->strikesThrown = $v; return $this; }
    public function getTakedowns(): int { return $this->takedowns; }
    public function setTakedowns(int $v): self { $this->takedowns = $v; return $this; }
    public function getTakedownAttempts(): int { return $this->takedownAttempts; }
    public function setTakedownAttempts(int $v): self { $this->takedownAttempts = $v; return $this; }
    public function getSubmissions(): int { return $this->submissions; }
    public function setSubmissions(int $v): self { $this->submissions = $v; return $this; }
    public function getKnockdowns(): int { return $this->knockdowns; }
    public function setKnockdowns(int $v): self { $this->knockdowns = $v; return $this; }
    public function getControlTimeSeconds(): int { return $this->controlTimeSeconds; }
    public function setControlTimeSeconds(int $v): self { $this->controlTimeSeconds = $v; return $this; }

    public function getStrikeAccuracy(): float
    {
        if ($this->strikesThrown == 0) return 0.0;
        return ($this->strikesLanded * 100.0) / $this->strikesThrown;
    }

    public function getTakedownAccuracy(): float
    {
        if ($this->takedownAttempts == 0) return 0.0;
        return ($this->takedowns * 100.0) / $this->takedownAttempts;
    }

    public function getFightPerformanceContribution(): float
    {
        $strikeScore = $this->getStrikeAccuracy();
        $takedownScore = $this->getTakedownAccuracy();
        $subScore = min($this->submissions * 10, 30);
        $kdScore = min($this->knockdowns * 10, 30);
        return ($strikeScore * 0.3) + ($takedownScore * 0.25) + ($subScore * 0.25) + ($kdScore * 0.2);
    }
}
