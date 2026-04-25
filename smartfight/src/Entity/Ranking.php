<?php

namespace App\Entity;

use App\Repository\RankingRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: RankingRepository::class)]
#[ORM\Table(name: 'ranking')]
#[ORM\HasLifecycleCallbacks]
class Ranking
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: 'integer')]
    private ?int $id = null;

    #[ORM\Column(name: 'fighter_id', type: 'integer')]
    private int $fighterId;

    #[ORM\Column(name: 'weight_class', type: 'string', length: 50)]
    private string $weightClass;

    #[ORM\ManyToOne(targetEntity: WeightClass::class)]
    #[ORM\JoinColumn(name: 'weight_class_id', nullable: true, onDelete: 'SET NULL')]
    private ?WeightClass $weightClassEntity = null;

    #[ORM\Column(name: 'rank_position', type: 'integer')]
    private int $rankPosition;

    #[ORM\Column(type: 'float', options: ['default' => '0'])]
    private float $points = 0.0;

    #[ORM\Column(type: 'string', length: 20)]
    private string $season = 'CURRENT';

    #[ORM\Column(name: 'created_at', type: 'datetime')]
    private \DateTimeInterface $createdAt;

    #[ORM\Column(name: 'updated_at', type: 'datetime')]
    private \DateTimeInterface $updatedAt;

    #[ORM\PrePersist]
    public function onPrePersist(): void
    {
        $now = new \DateTime();
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

    public function getWeightClass(): string { return $this->weightClass; }
    public function setWeightClass(string $weightClass): static { $this->weightClass = $weightClass; return $this; }

    public function getWeightClassEntity(): ?WeightClass { return $this->weightClassEntity; }
    public function setWeightClassEntity(?WeightClass $weightClassEntity): static { $this->weightClassEntity = $weightClassEntity; return $this; }

    public function getRankPosition(): int { return $this->rankPosition; }
    public function setRankPosition(int $rankPosition): static { $this->rankPosition = $rankPosition; return $this; }

    public function getPoints(): float { return $this->points; }
    public function setPoints(float $points): static { $this->points = $points; return $this; }

    public function getSeason(): string { return $this->season; }
    public function setSeason(string $season): static { $this->season = $season; return $this; }

    public function getCreatedAt(): \DateTimeInterface { return $this->createdAt; }
    public function setCreatedAt(\DateTimeInterface $createdAt): static { $this->createdAt = $createdAt; return $this; }

    public function getUpdatedAt(): \DateTimeInterface { return $this->updatedAt; }
    public function setUpdatedAt(\DateTimeInterface $updatedAt): static { $this->updatedAt = $updatedAt; return $this; }
}
