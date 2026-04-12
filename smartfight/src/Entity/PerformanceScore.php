<?php

namespace App\Entity;

use App\Repository\PerformanceScoreRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: PerformanceScoreRepository::class)]
#[ORM\Table(name: 'performance_score')]
#[ORM\HasLifecycleCallbacks]
class PerformanceScore
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: 'integer')]
    private ?int $id = null;

    #[ORM\Column(name: 'fighter_id', type: 'integer')]
    private int $fighterId;

    #[ORM\Column(type: 'float', options: ['default' => 0.0])]
    private float $score = 0.0;

    #[ORM\Column(type: 'float', options: ['default' => 0.0])]
    private float $aggression = 0.0;

    #[ORM\Column(type: 'float', options: ['default' => 0.0])]
    private float $defense = 0.0;

    #[ORM\Column(type: 'float', options: ['default' => 0.0])]
    private float $technique = 0.0;

    #[ORM\Column(type: 'float', options: ['default' => 0.0])]
    private float $experience = 0.0;

    #[ORM\Column(type: 'string', length: 20)]
    private string $season = 'CURRENT';

    #[ORM\Column(name: 'computed_at', type: 'datetime')]
    private \DateTimeInterface $computedAt;

    #[ORM\Column(name: 'created_at', type: 'datetime')]
    private \DateTimeInterface $createdAt;

    #[ORM\Column(name: 'updated_at', type: 'datetime')]
    private \DateTimeInterface $updatedAt;

    #[ORM\PrePersist]
    public function onPrePersist(): void
    {
        $now = new \DateTime();
        $this->computedAt = $now;
        $this->createdAt = $now;
        $this->updatedAt = $now;
    }

    #[ORM\PreUpdate]
    public function onPreUpdate(): void
    {
        $this->updatedAt = new \DateTime();
    }

    public function getId(): ?int { return $this->id; }
    public function getFighterId(): int { return $this->fighterId; }
    public function setFighterId(int $fighterId): static { $this->fighterId = $fighterId; return $this; }
    public function getScore(): float { return $this->score; }
    public function setScore(float $score): static { $this->score = $score; return $this; }
    public function getAggression(): float { return $this->aggression; }
    public function setAggression(float $aggression): static { $this->aggression = $aggression; return $this; }
    public function getDefense(): float { return $this->defense; }
    public function setDefense(float $defense): static { $this->defense = $defense; return $this; }
    public function getTechnique(): float { return $this->technique; }
    public function setTechnique(float $technique): static { $this->technique = $technique; return $this; }
    public function getExperience(): float { return $this->experience; }
    public function setExperience(float $experience): static { $this->experience = $experience; return $this; }
    public function getSeason(): string { return $this->season; }
    public function setSeason(string $season): static { $this->season = $season; return $this; }
    public function getComputedAt(): \DateTimeInterface { return $this->computedAt; }
    public function setComputedAt(\DateTimeInterface $computedAt): static { $this->computedAt = $computedAt; return $this; }
    public function getCreatedAt(): \DateTimeInterface { return $this->createdAt; }
    public function setCreatedAt(\DateTimeInterface $createdAt): static { $this->createdAt = $createdAt; return $this; }
    public function getUpdatedAt(): \DateTimeInterface { return $this->updatedAt; }
    public function setUpdatedAt(\DateTimeInterface $updatedAt): static { $this->updatedAt = $updatedAt; return $this; }
}
