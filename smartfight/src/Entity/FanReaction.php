<?php

namespace App\Entity;

use App\Repository\FanReactionRepository;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity(repositoryClass: FanReactionRepository::class)]
#[ORM\Table(name: 'fan_reaction')]
class FanReaction
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: 'integer')]
    private ?int $id = null;

    #[ORM\ManyToOne(targetEntity: FightResult::class)]
    #[ORM\JoinColumn(name: 'fight_result_id', nullable: false)]
    #[Assert\NotBlank]
    private ?FightResult $fightResult = null;

    #[ORM\ManyToOne(targetEntity: User::class)]
    #[ORM\JoinColumn(name: 'fan_id', nullable: false)]
    private ?User $fan = null;

    #[ORM\Column(name: 'reaction_type', type: 'string', length: 20)]
    #[Assert\NotBlank]
    #[Assert\Choice(choices: ['FIRE', 'SHOCK', 'RESPECT', 'DOMINANT', 'CONTROVERSIAL'])]
    private string $reactionType = '';

    #[ORM\Column(type: 'string', length: 140, nullable: true)]
    #[Assert\Length(max: 140)]
    private ?string $comment = null;

    #[ORM\Column(name: 'reacted_at', type: 'datetime')]
    private \DateTimeInterface $reactedAt;

    #[ORM\Column(name: 'is_pinned', type: 'boolean', options: ['default' => false])]
    private bool $isPinned = false;

    #[ORM\Column(name: 'is_deleted', type: 'boolean', options: ['default' => false])]
    private bool $isDeleted = false;

    public function __construct()
    {
        $this->reactedAt = new \DateTime();
    }

    public function getId(): ?int { return $this->id; }
    public function getFightResult(): ?FightResult { return $this->fightResult; }
    public function setFightResult(?FightResult $fightResult): static { $this->fightResult = $fightResult; return $this; }
    public function getFan(): ?User { return $this->fan; }
    public function setFan(?User $fan): static { $this->fan = $fan; return $this; }
    public function getReactionType(): string { return $this->reactionType; }
    public function setReactionType(string $reactionType): static { $this->reactionType = $reactionType; return $this; }
    public function getComment(): ?string { return $this->comment; }
    public function setComment(?string $comment): static { $this->comment = $comment; return $this; }
    public function getReactedAt(): \DateTimeInterface { return $this->reactedAt; }
    public function isPinned(): bool { return $this->isPinned; }
    public function setIsPinned(bool $isPinned): static { $this->isPinned = $isPinned; return $this; }
    public function isDeleted(): bool { return $this->isDeleted; }
    public function setIsDeleted(bool $isDeleted): static { $this->isDeleted = $isDeleted; return $this; }

    public function getReactionEmoji(): string
    {
        return match ($this->reactionType) {
            'FIRE' => '🔥',
            'SHOCK' => '😱',
            'RESPECT' => '👏',
            'DOMINANT' => '🏆',
            'CONTROVERSIAL' => '💔',
            default => '❓',
        };
    }
}
