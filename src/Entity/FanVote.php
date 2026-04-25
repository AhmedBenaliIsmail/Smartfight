<?php

namespace App\Entity;

use App\Repository\FanVoteRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: FanVoteRepository::class)]
#[ORM\Table(name: 'fan_vote')]
#[ORM\UniqueConstraint(name: 'unique_user_proposal_vote', columns: ['user_id', 'match_proposal_id'])]
class FanVote
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: 'integer')]
    private ?int $id = null;

    #[ORM\ManyToOne(targetEntity: User::class)]
    #[ORM\JoinColumn(name: 'user_id', referencedColumnName: 'userId', nullable: false)]
    private User $user;

    #[ORM\ManyToOne(targetEntity: MatchProposal::class)]
    #[ORM\JoinColumn(name: 'match_proposal_id', referencedColumnName: 'id', nullable: false, onDelete: 'CASCADE')]
    private MatchProposal $matchProposal;

    #[ORM\Column(name: 'voted_at', type: 'datetime')]
    private \DateTimeInterface $votedAt;

    public function __construct()
    {
        $this->votedAt = new \DateTime();
    }

    public function getId(): ?int { return $this->id; }
    public function getUser(): User { return $this->user; }
    public function setUser(User $user): self { $this->user = $user; return $this; }
    public function getMatchProposal(): MatchProposal { return $this->matchProposal; }
    public function setMatchProposal(MatchProposal $proposal): self { $this->matchProposal = $proposal; return $this; }
    public function getVotedAt(): \DateTimeInterface { return $this->votedAt; }
    public function setVotedAt(\DateTimeInterface $v): self { $this->votedAt = $v; return $this; }
}
