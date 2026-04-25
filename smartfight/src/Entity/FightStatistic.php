<?php

namespace App\Entity;

use App\Repository\FightStatisticRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: FightStatisticRepository::class)]
#[ORM\Table(name: 'fight_statistic')]
#[ORM\HasLifecycleCallbacks]
class FightStatistic
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: 'integer')]
    private ?int $id = null;

    #[ORM\Column(name: 'fight_result_id', type: 'integer')]
    private int $fightResultId;

    #[ORM\Column(name: 'fighter_id', type: 'integer')]
    private int $fighterId;

    #[ORM\Column(name: 'strikes_landed', type: 'integer', options: ['default' => 0])]
    private int $strikesLanded = 0;

    #[ORM\Column(name: 'strikes_attempted', type: 'integer', options: ['default' => 0])]
    private int $strikesAttempted = 0;

    #[ORM\Column(name: 'takedowns_landed', type: 'integer', options: ['default' => 0])]
    private int $takedownsLanded = 0;

    #[ORM\Column(name: 'takedowns_attempted', type: 'integer', options: ['default' => 0])]
    private int $takedownsAttempted = 0;

    #[ORM\Column(name: 'submission_attempts', type: 'integer', options: ['default' => 0])]
    private int $submissionAttempts = 0;

    #[ORM\Column(type: 'integer', options: ['default' => 0])]
    private int $knockdowns = 0;

    #[ORM\Column(name: 'control_time_seconds', type: 'integer', options: ['default' => 0])]
    private int $controlTimeSeconds = 0;

    #[ORM\Column(name: 'punches_thrown', type: 'integer', options: ['default' => 0])]
    private int $punchesThrown = 0;

    #[ORM\Column(name: 'punches_landed', type: 'integer', options: ['default' => 0])]
    private int $punchesLanded = 0;

    #[ORM\Column(name: 'round_number', type: 'integer', nullable: true)]
    private ?int $roundNumber = null;

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

    public function getFightResultId(): int { return $this->fightResultId; }
    public function setFightResultId(int $fightResultId): static { $this->fightResultId = $fightResultId; return $this; }

    public function getFighterId(): int { return $this->fighterId; }
    public function setFighterId(int $fighterId): static { $this->fighterId = $fighterId; return $this; }

    public function getStrikesLanded(): int { return $this->strikesLanded; }
    public function setStrikesLanded(int $strikesLanded): static { $this->strikesLanded = $strikesLanded; return $this; }

    public function getStrikesAttempted(): int { return $this->strikesAttempted; }
    public function setStrikesAttempted(int $strikesAttempted): static { $this->strikesAttempted = $strikesAttempted; return $this; }

    public function getTakedownsLanded(): int { return $this->takedownsLanded; }
    public function setTakedownsLanded(int $takedownsLanded): static { $this->takedownsLanded = $takedownsLanded; return $this; }

    public function getTakedownsAttempted(): int { return $this->takedownsAttempted; }
    public function setTakedownsAttempted(int $takedownsAttempted): static { $this->takedownsAttempted = $takedownsAttempted; return $this; }

    public function getSubmissionAttempts(): int { return $this->submissionAttempts; }
    public function setSubmissionAttempts(int $submissionAttempts): static { $this->submissionAttempts = $submissionAttempts; return $this; }

    public function getKnockdowns(): int { return $this->knockdowns; }
    public function setKnockdowns(int $knockdowns): static { $this->knockdowns = $knockdowns; return $this; }

    public function getControlTimeSeconds(): int { return $this->controlTimeSeconds; }
    public function setControlTimeSeconds(int $controlTimeSeconds): static { $this->controlTimeSeconds = $controlTimeSeconds; return $this; }

    public function getPunchesThrown(): int { return $this->punchesThrown; }
    public function setPunchesThrown(int $punchesThrown): static { $this->punchesThrown = $punchesThrown; return $this; }

    public function getPunchesLanded(): int { return $this->punchesLanded; }
    public function setPunchesLanded(int $punchesLanded): static { $this->punchesLanded = $punchesLanded; return $this; }

    public function getRoundNumber(): ?int { return $this->roundNumber; }
    public function setRoundNumber(?int $roundNumber): static { $this->roundNumber = $roundNumber; return $this; }

    public function getCreatedAt(): \DateTimeInterface { return $this->createdAt; }
    public function setCreatedAt(\DateTimeInterface $createdAt): static { $this->createdAt = $createdAt; return $this; }

    public function getUpdatedAt(): \DateTimeInterface { return $this->updatedAt; }
    public function setUpdatedAt(\DateTimeInterface $updatedAt): static { $this->updatedAt = $updatedAt; return $this; }
}
