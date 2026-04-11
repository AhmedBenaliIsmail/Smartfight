<?php
namespace App\Entity;

use App\Repository\ClassementRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: ClassementRepository::class)]
class Classement
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\ManyToOne(targetEntity: Combattant::class)]
    #[ORM\JoinColumn(nullable: false)]
    private ?Combattant $combattant = null;

    #[ORM\Column]
    private ?float $score = null;

    #[ORM\Column]
    private ?int $rang = null;

    #[ORM\Column(length: 50)]
    private ?string $discipline = null;

    public function getId(): ?int { return $this->id; }

    public function getCombattant(): ?Combattant { return $this->combattant; }

    public function setCombattant(?Combattant $combattant): static
    {
        $this->combattant = $combattant;
        return $this;
    }

    public function getScore(): ?float { return $this->score; }

    public function setScore(float $score): static
    {
        $this->score = $score;
        return $this;
    }

    public function getRang(): ?int { return $this->rang; }

    public function setRang(int $rang): static
    {
        $this->rang = $rang;
        return $this;
    }

    public function getDiscipline(): ?string { return $this->discipline; }

    public function setDiscipline(string $discipline): static
    {
        $this->discipline = $discipline;
        return $this;
    }
}