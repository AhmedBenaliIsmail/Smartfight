<?php

namespace App\Entity;

use App\Repository\CombatRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: CombatRepository::class)]
class Combat
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\ManyToOne]
    #[ORM\JoinColumn(nullable: false)]
    private ?Combattant $combattant1 = null;

    #[ORM\ManyToOne]
    #[ORM\JoinColumn(nullable: false)]
    private ?Combattant $combattant2 = null;

    #[ORM\Column]
    private ?float $scoreIA = null;

    #[ORM\Column(length: 50, nullable: true)]
    private ?string $resultat = null;

    #[ORM\Column(nullable: true)]
    private ?\DateTime $dateCombat = null;

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getCombattant1(): ?Combattant
    {
        return $this->combattant1;
    }

    public function setCombattant1(?Combattant $combattant1): static
    {
        $this->combattant1 = $combattant1;

        return $this;
    }

    public function getCombattant2(): ?Combattant
    {
        return $this->combattant2;
    }

    public function setCombattant2(?Combattant $combattant2): static
    {
        $this->combattant2 = $combattant2;

        return $this;
    }

    public function getScoreIA(): ?float
    {
        return $this->scoreIA;
    }

    public function setScoreIA(float $scoreIA): static
    {
        $this->scoreIA = $scoreIA;

        return $this;
    }

    public function getResultat(): ?string
    {
        return $this->resultat;
    }

    public function setResultat(?string $resultat): static
    {
        $this->resultat = $resultat;

        return $this;
    }

    public function getDateCombat(): ?\DateTime
    {
        return $this->dateCombat;
    }

    public function setDateCombat(?\DateTime $dateCombat): static
    {
        $this->dateCombat = $dateCombat;

        return $this;
    }
}
