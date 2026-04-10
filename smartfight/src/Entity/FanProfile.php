<?php

namespace App\Entity;

use App\Repository\FanProfileRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: FanProfileRepository::class)]
#[ORM\Table(name: 'fan_profile')]
class FanProfile
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: 'integer')]
    private ?int $id = null;

    #[ORM\OneToOne(targetEntity: User::class)]
    #[ORM\JoinColumn(name: 'user_id', nullable: false)]
    private User $user;

    #[ORM\Column(name: 'favorite_sport', type: 'string', length: 100, nullable: true)]
    private ?string $favoriteSport = null;

    #[ORM\Column(type: 'string', length: 100, nullable: true)]
    private ?string $country = null;

    #[ORM\Column(type: 'text', nullable: true)]
    private ?string $bio = null;

    public function getId(): ?int { return $this->id; }
    public function getUser(): User { return $this->user; }
    public function setUser(User $user): static { $this->user = $user; return $this; }
    public function getFavoriteSport(): ?string { return $this->favoriteSport; }
    public function setFavoriteSport(?string $s): static { $this->favoriteSport = $s; return $this; }
    public function getCountry(): ?string { return $this->country; }
    public function setCountry(?string $c): static { $this->country = $c; return $this; }
    public function getBio(): ?string { return $this->bio; }
    public function setBio(?string $bio): static { $this->bio = $bio; return $this; }
}
