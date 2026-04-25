<?php

namespace App\Entity;

use App\Repository\FighterContractRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: FighterContractRepository::class)]
class FighterContract
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\ManyToOne(targetEntity: Fighter::class)]
    #[ORM\JoinColumn(name: "fighter_id", referencedColumnName: "fighterId", nullable: false)]
    private ?Fighter $fighter = null;

    #[ORM\ManyToOne(targetEntity: Event::class)]
    #[ORM\JoinColumn(name: "event_id", referencedColumnName: "eventId", nullable: false)]
    private ?Event $event = null;

    #[ORM\Column]
    private float $basePay = 0.0;

    #[ORM\Column]
    private float $winBonus = 0.0;

    #[ORM\Column(nullable: true)]
    private ?float $calculatedPayout = null;

    #[ORM\Column(options: ["default" => false])]
    private bool $isPaid = false;

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getFighter(): ?Fighter
    {
        return $this->fighter;
    }

    public function setFighter(?Fighter $fighter): static
    {
        $this->fighter = $fighter;

        return $this;
    }

    public function getEvent(): ?Event
    {
        return $this->event;
    }

    public function setEvent(?Event $event): static
    {
        $this->event = $event;

        return $this;
    }

    public function getBasePay(): float
    {
        return $this->basePay;
    }

    public function setBasePay(float $basePay): static
    {
        $this->basePay = $basePay;

        return $this;
    }

    public function getWinBonus(): float
    {
        return $this->winBonus;
    }

    public function setWinBonus(float $winBonus): static
    {
        $this->winBonus = $winBonus;

        return $this;
    }

    public function getCalculatedPayout(): ?float
    {
        return $this->calculatedPayout;
    }

    public function setCalculatedPayout(?float $calculatedPayout): static
    {
        $this->calculatedPayout = $calculatedPayout;

        return $this;
    }

    public function isPaid(): bool
    {
        return $this->isPaid;
    }

    public function setIsPaid(bool $isPaid): static
    {
        $this->isPaid = $isPaid;

        return $this;
    }
}
