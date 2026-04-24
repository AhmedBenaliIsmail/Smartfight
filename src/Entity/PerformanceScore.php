<?php
namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;
use App\Repository\PerformanceScoreRepository;

#[ORM\Entity(repositoryClass: PerformanceScoreRepository::class)]
#[ORM\Table(name: 'performance_score')]
class PerformanceScore
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\ManyToOne(targetEntity: Fighter::class)]
    #[ORM\JoinColumn(name: "fighter_id", referencedColumnName: "fighterId", nullable: false)]
    private ?Fighter $fighter = null;

    #[ORM\Column(type: 'float', options: ['default' => 0])]
    private float $score = 0;

    #[ORM\Column(type: 'float', nullable: true)]
    private ?float $aggression = null;

    #[ORM\Column(type: 'float', nullable: true)]
    private ?float $defense = null;

    #[ORM\Column(type: 'float', nullable: true)]
    private ?float $technique = null;

    #[ORM\Column(type: 'float', nullable: true)]
    private ?float $experience = null;

    #[ORM\Column(name: 'calculated_at', type: 'datetime', nullable: true)]
    private ?\DateTimeInterface $calculatedAt = null;

    public function getId(): ?int { return $this->id; }
    public function setId(int $v): self { $this->id = $v; return $this; }
    public function getFighter(): ?Fighter { return $this->fighter; }
    public function setFighter(Fighter $v): self { $this->fighter = $v; return $this; }
    public function getScore(): float { return $this->score; }
    public function setScore(float $v): self { $this->score = $v; return $this; }
    public function getAggression(): ?float { return $this->aggression; }
    public function setAggression(?float $v): self { $this->aggression = $v; return $this; }
    public function getDefense(): ?float { return $this->defense; }
    public function setDefense(?float $v): self { $this->defense = $v; return $this; }
    public function getTechnique(): ?float { return $this->technique; }
    public function setTechnique(?float $v): self { $this->technique = $v; return $this; }
    public function getExperience(): ?float { return $this->experience; }
    public function setExperience(?float $v): self { $this->experience = $v; return $this; }
    public function getCalculatedAt(): ?\DateTimeInterface { return $this->calculatedAt; }
    public function setCalculatedAt(?\DateTimeInterface $v): self { $this->calculatedAt = $v; return $this; }
}
