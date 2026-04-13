<?php

namespace App\Entity;

use App\Repository\RankingRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: RankingRepository::class)]
#[ORM\Table(name: 'ranking')]
class Ranking
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'id', type: 'integer')]
    private ?int $id = null;

    #[ORM\Column(name: 'fighter_id', type: 'integer', nullable: true)]
    private ?int $fighterId = null;

    #[ORM\Column(name: 'discipline_id', type: 'integer', nullable: true)]
    private ?int $disciplineId = null;

    #[ORM\Column(name: 'rank_position', type: 'integer', nullable: true)]
    private ?int $rankPosition = null;

    #[ORM\Column(name: 'points', type: 'integer', nullable: true)]
    private ?int $points = null;

    #[ORM\Column(name: 'season', type: 'string', length: 20, nullable: true)]
    private ?string $season = null;

    public function getId(): ?int { return $this->id; }
    public function getFighterId(): ?int { return $this->fighterId; }
    public function setFighterId(?int $fighterId): static { $this->fighterId = $fighterId; return $this; }
    public function getDisciplineId(): ?int { return $this->disciplineId; }
    public function setDisciplineId(?int $disciplineId): static { $this->disciplineId = $disciplineId; return $this; }
    public function getRankPosition(): ?int { return $this->rankPosition; }
    public function setRankPosition(?int $rankPosition): static { $this->rankPosition = $rankPosition; return $this; }
    public function getPoints(): ?int { return $this->points; }
    public function setPoints(?int $points): static { $this->points = $points; return $this; }
    public function getSeason(): ?string { return $this->season; }
    public function setSeason(?string $season): static { $this->season = $season; return $this; }

    public function __toString(): string
    {
        return 'Ranking #' . $this->id;
    }
}
