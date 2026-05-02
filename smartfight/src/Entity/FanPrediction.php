<?php

namespace App\Entity;

use App\Repository\FanPredictionRepository;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity(repositoryClass: FanPredictionRepository::class)]
#[ORM\Table(name: 'fan_prediction')]
#[ORM\UniqueConstraint(name: 'uq_fan_prediction', columns: ['match_proposal_id', 'fan_id'])]
class FanPrediction
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: 'integer')]
    private ?int $id = null;

    #[ORM\ManyToOne(targetEntity: MatchProposal::class)]
    #[ORM\JoinColumn(name: 'match_proposal_id', nullable: false)]
    #[Assert\NotBlank]
    private ?MatchProposal $matchProposal = null;

    #[ORM\ManyToOne(targetEntity: User::class)]
    #[ORM\JoinColumn(name: 'fan_id', nullable: false)]
    private ?User $fan = null;

    #[ORM\ManyToOne(targetEntity: Fighter::class)]
    #[ORM\JoinColumn(name: 'predicted_winner_id', nullable: false)]
    #[Assert\NotBlank]
    private ?Fighter $predictedWinner = null;

    #[ORM\Column(name: 'predicted_method', type: 'string', length: 20)]
    #[Assert\NotBlank]
    #[Assert\Choice(choices: ['KO', 'TKO', 'SUBMISSION', 'DECISION', 'DRAW'])]
    private string $predictedMethod = '';

    #[ORM\Column(name: 'submitted_at', type: 'datetime')]
    private \DateTimeInterface $submittedAt;

    #[ORM\Column(name: 'is_locked', type: 'boolean', options: ['default' => true])]
    private bool $isLocked = true;

    #[ORM\Column(name: 'points_earned', type: 'integer', nullable: true)]
    private ?int $pointsEarned = null;

    #[ORM\Column(name: 'is_scored', type: 'boolean', options: ['default' => false])]
    private bool $isScored = false;

    #[ORM\Column(type: 'string', length: 10)]
    private string $season = '2026';

    public function __construct()
    {
        $this->submittedAt = new \DateTime();
    }

    public function getId(): ?int { return $this->id; }
    public function getMatchProposal(): ?MatchProposal { return $this->matchProposal; }
    public function setMatchProposal(?MatchProposal $matchProposal): static { $this->matchProposal = $matchProposal; return $this; }
    public function getFan(): ?User { return $this->fan; }
    public function setFan(?User $fan): static { $this->fan = $fan; return $this; }
    public function getPredictedWinner(): ?Fighter { return $this->predictedWinner; }
    public function setPredictedWinner(?Fighter $predictedWinner): static { $this->predictedWinner = $predictedWinner; return $this; }
    public function getPredictedMethod(): string { return $this->predictedMethod; }
    public function setPredictedMethod(string $predictedMethod): static { $this->predictedMethod = $predictedMethod; return $this; }
    public function getSubmittedAt(): \DateTimeInterface { return $this->submittedAt; }
    public function isLocked(): bool { return $this->isLocked; }
    public function setIsLocked(bool $isLocked): static { $this->isLocked = $isLocked; return $this; }
    public function getPointsEarned(): ?int { return $this->pointsEarned; }
    public function setPointsEarned(?int $pointsEarned): static { $this->pointsEarned = $pointsEarned; return $this; }
    public function isScored(): bool { return $this->isScored; }
    public function setIsScored(bool $isScored): static { $this->isScored = $isScored; return $this; }
    public function getSeason(): string { return $this->season; }
    public function setSeason(string $season): static { $this->season = $season; return $this; }
}
