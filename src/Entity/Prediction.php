<?php
namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;
use App\Repository\PredictionRepository;

#[ORM\Entity(repositoryClass: PredictionRepository::class)]
#[ORM\Table(name: 'predictions')]
class Prediction
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'predictionId')]
    private ?int $predictionId = null;

    #[ORM\ManyToOne(targetEntity: User::class)]
    #[ORM\JoinColumn(name: 'userId', referencedColumnName: 'userId', nullable: false)]
    private ?User $user = null;

    #[ORM\ManyToOne(targetEntity: FightResult::class)]
    #[ORM\JoinColumn(name: 'fightId', referencedColumnName: 'resultId', nullable: false)]
    private ?FightResult $fight = null;

    #[ORM\ManyToOne(targetEntity: Fighter::class)]
    #[ORM\JoinColumn(name: 'predictedWinnerId', referencedColumnName: 'fighterId', nullable: true)]
    private ?Fighter $predictedWinner = null; // null if Draw

    #[ORM\Column(length: 50)]
    private ?string $predictedMethod = null;

    #[ORM\Column(type: 'integer', nullable: true)]
    private ?int $predictedRound = null;

    #[ORM\Column(type: 'boolean', options: ['default' => false])]
    private bool $isProcessed = false;

    #[ORM\Column(type: 'integer', options: ['default' => 0])]
    private int $pointsAwarded = 0;

    #[ORM\Column(type: 'datetime')]
    private ?\DateTimeInterface $createdAt = null;

    public function __construct()
    {
        $this->createdAt = new \DateTime();
    }

    public function getPredictionId(): ?int { return $this->predictionId; }
    public function getUser(): ?User { return $this->user; }
    public function setUser(?User $user): self { $this->user = $user; return $this; }
    public function getFight(): ?FightResult { return $this->fight; }
    public function setFight(?FightResult $fight): self { $this->fight = $fight; return $this; }
    public function getPredictedWinner(): ?Fighter { return $this->predictedWinner; }
    public function setPredictedWinner(?Fighter $f): self { $this->predictedWinner = $f; return $this; }
    public function getPredictedMethod(): ?string { return $this->predictedMethod; }
    public function setPredictedMethod(?string $m): self { $this->predictedMethod = $m; return $this; }
    public function getPredictedRound(): ?int { return $this->predictedRound; }
    public function setPredictedRound(?int $r): self { $this->predictedRound = $r; return $this; }
    public function isProcessed(): bool { return $this->isProcessed; }
    public function setIsProcessed(bool $v): self { $this->isProcessed = $v; return $this; }
    public function getPointsAwarded(): int { return $this->pointsAwarded; }
    public function setPointsAwarded(int $v): self { $this->pointsAwarded = $v; return $this; }
    public function getCreatedAt(): ?\DateTimeInterface { return $this->createdAt; }
}
