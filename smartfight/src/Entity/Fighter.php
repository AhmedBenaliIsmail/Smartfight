<?php

namespace App\Entity;

use App\Repository\FighterRepository;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\HttpFoundation\File\File;
use Vich\UploaderBundle\Mapping\Annotation as Vich;

#[Vich\Uploadable]
#[ORM\Entity(repositoryClass: FighterRepository::class)]
#[ORM\Table(name: 'fighter')]
#[ORM\HasLifecycleCallbacks]
class Fighter
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: 'integer')]
    private ?int $id = null;

    #[ORM\ManyToOne(targetEntity: User::class)]
    #[ORM\JoinColumn(name: 'user_id', nullable: false)]
    private User $user;

    #[ORM\Column(type: 'string', length: 100, nullable: true)]
    private ?string $nickname = null;

    #[ORM\Column(name: 'date_of_birth', type: 'date', nullable: true)]
    private ?\DateTimeInterface $dateOfBirth = null;

    #[ORM\Column(type: 'string', length: 100, nullable: true)]
    private ?string $nationality = null;

    #[ORM\Column(name: 'photo_url', type: 'string', length: 255, nullable: true)]
    private ?string $photoUrl = null;

    #[Vich\UploadableField(mapping: 'fighter_photo', fileNameProperty: 'photoUrl')]
    private ?File $photoFile = null;

    #[ORM\Column(name: 'weight_class_id', type: 'integer', nullable: true)]
    private ?int $weightClassId = null;

    #[ORM\Column(type: 'integer', options: ['default' => 0])]
    private int $wins = 0;

    #[ORM\Column(type: 'integer', options: ['default' => 0])]
    private int $losses = 0;

    #[ORM\Column(type: 'integer', options: ['default' => 0])]
    private int $draws = 0;

    #[ORM\Column(name: 'elo_rating', type: 'float', options: ['default' => 1500.0])]
    private float $eloRating = 1500.0;

    #[ORM\Column(name: 'performance_score', type: 'float', options: ['default' => 0.0])]
    private float $performanceScore = 0.0;

    #[ORM\Column(name: 'win_streak', type: 'integer', options: ['default' => 0])]
    private int $winStreak = 0;

    #[ORM\Column(name: 'strength_of_schedule', type: 'float', options: ['default' => 1500.0])]
    private float $strengthOfSchedule = 1500.0;

    #[ORM\Column(name: 'ko_wins', type: 'integer', options: ['default' => 0])]
    private int $koWins = 0;

    #[ORM\Column(name: 'submission_wins', type: 'integer', options: ['default' => 0])]
    private int $submissionWins = 0;

    #[ORM\Column(name: 'decision_wins', type: 'integer', options: ['default' => 0])]
    private int $decisionWins = 0;

    #[ORM\Column(name: 'champions_event_win_streak', type: 'integer', options: ['default' => 0])]
    private int $championsEventWinStreak = 0;

    #[ORM\Column(name: 'title_defenses', type: 'integer', options: ['default' => 0])]
    private int $titleDefenses = 0;

    #[ORM\Column(type: 'integer', nullable: true)]
    private ?int $height = null;

    #[ORM\Column(type: 'integer', nullable: true)]
    private ?int $reach = null;

    #[ORM\Column(name: 'weight_class', type: 'string', length: 50, nullable: true)]
    private ?string $weightClass = null;

    #[ORM\Column(type: 'string', length: 20)]
    private string $status = 'ACTIVE';

    #[ORM\Column(name: 'created_at', type: 'datetime')]
    private \DateTimeInterface $createdAt;

    #[ORM\Column(name: 'updated_at', type: 'datetime')]
    private \DateTimeInterface $updatedAt;

    #[ORM\PrePersist]
    public function onPrePersist(): void
    {
        $this->createdAt = new \DateTime();
        $this->updatedAt = new \DateTime();
    }

    #[ORM\PreUpdate]
    public function onPreUpdate(): void
    {
        $this->updatedAt = new \DateTime();
    }

    public function getId(): ?int { return $this->id; }
    public function getUser(): User { return $this->user; }
    public function setUser(User $user): static { $this->user = $user; return $this; }
    public function getNickname(): ?string { return $this->nickname; }
    public function setNickname(?string $nickname): static { $this->nickname = $nickname; return $this; }
    public function getDateOfBirth(): ?\DateTimeInterface { return $this->dateOfBirth; }
    public function setDateOfBirth(?\DateTimeInterface $dateOfBirth): static { $this->dateOfBirth = $dateOfBirth; return $this; }
    public function getNationality(): ?string { return $this->nationality; }
    public function setNationality(?string $nationality): static { $this->nationality = $nationality; return $this; }
    public function getPhotoUrl(): ?string { return $this->photoUrl; }
    public function setPhotoUrl(?string $photoUrl): static { $this->photoUrl = $photoUrl; return $this; }
    public function getWeightClassId(): ?int { return $this->weightClassId; }
    public function setWeightClassId(?int $weightClassId): static { $this->weightClassId = $weightClassId; return $this; }
    public function getWins(): int { return $this->wins; }
    public function setWins(int $wins): static { $this->wins = $wins; return $this; }
    public function getLosses(): int { return $this->losses; }
    public function setLosses(int $losses): static { $this->losses = $losses; return $this; }
    public function getDraws(): int { return $this->draws; }
    public function setDraws(int $draws): static { $this->draws = $draws; return $this; }
    public function getEloRating(): float { return $this->eloRating; }
    public function setEloRating(float $eloRating): static { $this->eloRating = $eloRating; return $this; }
    public function getPerformanceScore(): float { return $this->performanceScore; }
    public function setPerformanceScore(float $performanceScore): static { $this->performanceScore = $performanceScore; return $this; }
    public function getWinStreak(): int { return $this->winStreak; }
    public function setWinStreak(int $winStreak): static { $this->winStreak = $winStreak; return $this; }
    public function getStrengthOfSchedule(): float { return $this->strengthOfSchedule; }
    public function setStrengthOfSchedule(float $strengthOfSchedule): static { $this->strengthOfSchedule = $strengthOfSchedule; return $this; }
    public function getKoWins(): int { return $this->koWins; }
    public function setKoWins(int $koWins): static { $this->koWins = $koWins; return $this; }
    public function getSubmissionWins(): int { return $this->submissionWins; }
    public function setSubmissionWins(int $submissionWins): static { $this->submissionWins = $submissionWins; return $this; }
    public function getDecisionWins(): int { return $this->decisionWins; }
    public function setDecisionWins(int $decisionWins): static { $this->decisionWins = $decisionWins; return $this; }
    public function getChampionsEventWinStreak(): int { return $this->championsEventWinStreak; }
    public function setChampionsEventWinStreak(int $championsEventWinStreak): static { $this->championsEventWinStreak = $championsEventWinStreak; return $this; }
    public function getTitleDefenses(): int { return $this->titleDefenses; }
    public function setTitleDefenses(int $titleDefenses): static { $this->titleDefenses = $titleDefenses; return $this; }
    public function getHeight(): ?int { return $this->height; }
    public function setHeight(?int $height): static { $this->height = $height; return $this; }
    public function getReach(): ?int { return $this->reach; }
    public function setReach(?int $reach): static { $this->reach = $reach; return $this; }
    public function getWeightClass(): ?string { return $this->weightClass; }
    public function setWeightClass(?string $weightClass): static { $this->weightClass = $weightClass; return $this; }
    public function getStatus(): string { return $this->status; }
    public function setStatus(string $status): static { $this->status = $status; return $this; }
    public function getCreatedAt(): \DateTimeInterface { return $this->createdAt; }
    public function setCreatedAt(\DateTimeInterface $createdAt): static { $this->createdAt = $createdAt; return $this; }
    public function getUpdatedAt(): \DateTimeInterface { return $this->updatedAt; }
    public function setUpdatedAt(\DateTimeInterface $updatedAt): static { $this->updatedAt = $updatedAt; return $this; }

    public function setPhotoFile(?File $photoFile = null): static
    {
        $this->photoFile = $photoFile;
        if ($photoFile !== null) { $this->updatedAt = new \DateTime(); }
        return $this;
    }
    public function getPhotoFile(): ?File { return $this->photoFile; }

    public function getRecord(): string { return $this->wins . '-' . $this->losses . '-' . $this->draws; }

    public function getTotalFights(): int
    {
        return $this->wins + $this->losses + $this->draws;
    }

    public function getDisplayName(): string
    {
        return $this->nickname
            ? $this->user->getFirstName() . ' "' . $this->nickname . '" ' . $this->user->getLastName()
            : $this->user->getFullName();
    }

    public function __toString(): string { return $this->getDisplayName(); }
}
