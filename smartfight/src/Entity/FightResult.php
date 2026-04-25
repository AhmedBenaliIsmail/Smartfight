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
    #[ORM\Column(type: 'integer')]
    private ?int $id = null;

    #[ORM\ManyToOne(targetEntity: Event::class)]
    #[ORM\JoinColumn(name: 'event_id', nullable: false)]
    private Event $event;

    #[ORM\ManyToOne(targetEntity: MatchProposal::class)]
    #[ORM\JoinColumn(name: 'match_id', nullable: true)]
    private ?MatchProposal $match = null;

    #[ORM\ManyToOne(targetEntity: Fighter::class)]
    #[ORM\JoinColumn(name: 'fighter_red_id', nullable: false)]
    private Fighter $fighterRed;

    #[ORM\ManyToOne(targetEntity: Fighter::class)]
    #[ORM\JoinColumn(name: 'fighter_blue_id', nullable: false)]
    private Fighter $fighterBlue;

    #[ORM\ManyToOne(targetEntity: Fighter::class)]
    #[ORM\JoinColumn(name: 'winner_id', nullable: true)]
    private ?Fighter $winner = null;

    #[ORM\Column(type: 'string', length: 20)]
    private string $method;

    #[ORM\Column(name: 'round_ended', type: 'integer', nullable: true)]
    private ?int $roundEnded = null;

    #[ORM\Column(name: 'time_ended', type: 'time', nullable: true)]
    private ?\DateTimeInterface $timeEnded = null;

    #[ORM\Column(type: 'text', nullable: true)]
    private ?string $notes = null;

    #[ORM\Column(name: 'fight_date', type: 'date')]
    private \DateTimeInterface $fightDate;

    #[ORM\Column(name: 'created_at', type: 'datetime')]
    private \DateTimeInterface $createdAt;

    public function getId(): ?int { return $this->id; }
    public function getEvent(): Event { return $this->event; }
    public function getMatch(): ?MatchProposal { return $this->match; }
    public function getFighterRed(): Fighter { return $this->fighterRed; }
    public function getFighterBlue(): Fighter { return $this->fighterBlue; }
    public function getWinner(): ?Fighter { return $this->winner; }
    public function getMethod(): string { return $this->method; }
    public function getRoundEnded(): ?int { return $this->roundEnded; }
    public function getTimeEnded(): ?\DateTimeInterface { return $this->timeEnded; }
    public function getNotes(): ?string { return $this->notes; }
    public function getFightDate(): \DateTimeInterface { return $this->fightDate; }

    public function getFightLabel(): string
    {
        return $this->fighterRed->getDisplayName() . ' vs ' . $this->fighterBlue->getDisplayName();
    }

    public function __toString(): string { return $this->getFightLabel(); }
}
