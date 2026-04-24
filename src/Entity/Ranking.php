<?php
namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;
use App\Repository\RankingRepository;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity(repositoryClass: RankingRepository::class)]
#[ORM\Table(name: 'ranking')]
class Ranking
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\ManyToOne(targetEntity: Fighter::class)]
    #[ORM\JoinColumn(name: "fighter_id", referencedColumnName: "fighterId", nullable: false)]
    private ?Fighter $fighter = null;

    #[ORM\Column(name: 'rank_position', type: 'integer', options: ['default' => 999])]
    private int $rankPosition = 999;

    #[ORM\Column(type: 'float', options: ['default' => 0])]
    private float $points = 0;

    #[ORM\ManyToOne(targetEntity: WeightDivision::class)]
    #[ORM\JoinColumn(name: 'weight_division_id', referencedColumnName: 'id', nullable: true)]
    #[Assert\NotNull]
    private ?WeightDivision $weightDivision = null;

    #[ORM\Column(length: 20, options: ['default' => 'MEDIA'])]
    #[Assert\Choice(choices: ['WBC', 'WBA', 'IBF', 'WBO', 'MEDIA'])]
    private string $organization = 'MEDIA';

    #[ORM\Column(name: 'is_champion', type: 'boolean', options: ['default' => false])]
    private bool $isChampion = false;

    #[ORM\Column(name: 'last_fight_date', type: 'date', nullable: true)]
    private ?\DateTimeInterface $lastFightDate = null;

    #[ORM\Column(name: 'updated_at', type: 'datetime', nullable: true)]
    private ?\DateTimeInterface $updatedAt = null;

    public function getId(): ?int { return $this->id; }
    public function getFighter(): ?Fighter { return $this->fighter; }
    public function setFighter(Fighter $v): self { $this->fighter = $v; return $this; }
    public function getRankPosition(): int { return $this->rankPosition; }
    public function setRankPosition(int $v): self { $this->rankPosition = $v; return $this; }
    public function getPoints(): float { return $this->points; }
    public function setPoints(float $v): self { $this->points = $v; return $this; }
    public function getWeightDivision(): ?WeightDivision { return $this->weightDivision; }
    public function setWeightDivision(?WeightDivision $v): self { $this->weightDivision = $v; return $this; }
    public function getOrganization(): string { return $this->organization; }
    public function setOrganization(string $v): self { $this->organization = $v; return $this; }
    public function isChampion(): bool { return $this->isChampion; }
    public function setIsChampion(bool $v): self { $this->isChampion = $v; return $this; }
    public function getLastFightDate(): ?\DateTimeInterface { return $this->lastFightDate; }
    public function setLastFightDate(?\DateTimeInterface $v): self { $this->lastFightDate = $v; return $this; }
    public function getUpdatedAt(): ?\DateTimeInterface { return $this->updatedAt; }
    public function setUpdatedAt(?\DateTimeInterface $v): self { $this->updatedAt = $v; return $this; }
}
