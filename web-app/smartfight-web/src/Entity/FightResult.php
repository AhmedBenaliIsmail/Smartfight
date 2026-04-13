<?php

namespace App\Entity;

use App\Repository\FightResultRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: FightResultRepository::class)]
#[ORM\Table(name: 'fight_result')]
class FightResult
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'id', type: 'integer')]
    private ?int $id = null;

    #[ORM\Column(name: 'event_id', type: 'integer', nullable: true)]
    private ?int $eventId = null;

    #[ORM\Column(name: 'fighter_red_id', type: 'integer', nullable: true)]
    private ?int $fighterRedId = null;

    #[ORM\Column(name: 'fighter_blue_id', type: 'integer', nullable: true)]
    private ?int $fighterBlueId = null;

    #[ORM\Column(name: 'winner_id', type: 'integer', nullable: true)]
    private ?int $winnerId = null;

    #[ORM\Column(name: 'method', type: 'string', length: 100, nullable: true)]
    private ?string $method = null;

    #[ORM\Column(name: 'fight_date', type: 'date', nullable: true)]
    private ?\DateTimeInterface $fightDate = null;

    public function getId(): ?int { return $this->id; }
    public function getEventId(): ?int { return $this->eventId; }
    public function setEventId(?int $eventId): static { $this->eventId = $eventId; return $this; }
    public function getFighterRedId(): ?int { return $this->fighterRedId; }
    public function setFighterRedId(?int $fighterRedId): static { $this->fighterRedId = $fighterRedId; return $this; }
    public function getFighterBlueId(): ?int { return $this->fighterBlueId; }
    public function setFighterBlueId(?int $fighterBlueId): static { $this->fighterBlueId = $fighterBlueId; return $this; }
    public function getWinnerId(): ?int { return $this->winnerId; }
    public function setWinnerId(?int $winnerId): static { $this->winnerId = $winnerId; return $this; }
    public function getMethod(): ?string { return $this->method; }
    public function setMethod(?string $method): static { $this->method = $method; return $this; }
    public function getFightDate(): ?\DateTimeInterface { return $this->fightDate; }
    public function setFightDate(?\DateTimeInterface $fightDate): static { $this->fightDate = $fightDate; return $this; }

    public function __toString(): string
    {
        return 'Result #' . $this->id;
    }
}
