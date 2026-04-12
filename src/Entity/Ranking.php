<?php
namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;
use App\Repository\RankingRepository;

#[ORM\Entity(repositoryClass: RankingRepository::class)]
#[ORM\Table(name: 'ranking')]
class Ranking
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column(name: 'fighter_id')]
    private ?int $fighterId = null;

    #[ORM\Column(name: 'rank_position', type: 'integer', options: ['default' => 999])]
    private int $rankPosition = 999;

    #[ORM\Column(type: 'float', options: ['default' => 0])]
    private float $points = 0;

    #[ORM\Column(name: 'weightClass', length: 50, nullable: true)]
    private ?string $weightClass = null;

    #[ORM\Column(length: 20, nullable: true)]
    private ?string $season = null;

    #[ORM\Column(name: 'updated_at', type: 'datetime', nullable: true)]
    private ?\DateTimeInterface $updatedAt = null;

    public function getId(): ?int { return $this->id; }
    public function getFighterId(): ?int { return $this->fighterId; }
    public function setFighterId(int $v): self { $this->fighterId = $v; return $this; }
    public function getRankPosition(): int { return $this->rankPosition; }
    public function setRankPosition(int $v): self { $this->rankPosition = $v; return $this; }
    public function getPoints(): float { return $this->points; }
    public function setPoints(float $v): self { $this->points = $v; return $this; }
    public function getWeightClass(): ?string { return $this->weightClass; }
    public function setWeightClass(?string $v): self { $this->weightClass = $v; return $this; }
    public function getSeason(): ?string { return $this->season; }
    public function setSeason(?string $v): self { $this->season = $v; return $this; }
    public function getUpdatedAt(): ?\DateTimeInterface { return $this->updatedAt; }
    public function setUpdatedAt(?\DateTimeInterface $v): self { $this->updatedAt = $v; return $this; }
}
