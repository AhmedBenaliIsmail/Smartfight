<?php

namespace App\Entity;

use App\Repository\WeightClassRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: WeightClassRepository::class)]
#[ORM\Table(name: 'weight_class')]
class WeightClass
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: 'integer')]
    private ?int $id = null;

    #[ORM\Column(type: 'string', length: 50)]
    private string $name;

    #[ORM\Column(name: 'min_weight', type: 'decimal', precision: 5, scale: 2, nullable: true)]
    private ?string $minWeight = null;

    #[ORM\Column(name: 'max_weight', type: 'decimal', precision: 5, scale: 2, nullable: true)]
    private ?string $maxWeight = null;

    #[ORM\ManyToOne(targetEntity: Discipline::class)]
    #[ORM\JoinColumn(name: 'discipline_id', nullable: false)]
    private Discipline $discipline;

    #[ORM\ManyToOne(targetEntity: Fighter::class)]
    #[ORM\JoinColumn(name: 'champion_id', nullable: true, onDelete: 'SET NULL')]
    private ?Fighter $champion = null;

    #[ORM\Column(name: 'displayOrder', type: 'integer', options: ['default' => 0])]
    private int $displayOrder = 0;

    #[ORM\Column(type: 'string', length: 100, unique: true)]
    private string $slug;

    public function getId(): ?int { return $this->id; }

    public function getName(): string { return $this->name; }
    public function setName(string $name): static { $this->name = $name; return $this; }

    public function getMinWeight(): ?string { return $this->minWeight; }
    public function setMinWeight(?string $minWeight): static { $this->minWeight = $minWeight; return $this; }

    public function getMaxWeight(): ?string { return $this->maxWeight; }
    public function setMaxWeight(?string $maxWeight): static { $this->maxWeight = $maxWeight; return $this; }

    public function getDiscipline(): Discipline { return $this->discipline; }
    public function setDiscipline(Discipline $discipline): static { $this->discipline = $discipline; return $this; }

    public function getChampion(): ?Fighter { return $this->champion; }
    public function setChampion(?Fighter $champion): static { $this->champion = $champion; return $this; }

    public function getDisplayOrder(): int { return $this->displayOrder; }
    public function setDisplayOrder(int $displayOrder): static { $this->displayOrder = $displayOrder; return $this; }

    public function getSlug(): string { return $this->slug; }
    public function setSlug(string $slug): static { $this->slug = $slug; return $this; }

    public function __toString(): string { return $this->name; }
}
