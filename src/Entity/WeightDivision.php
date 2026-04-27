<?php
namespace App\Entity;

use App\Repository\WeightDivisionRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: WeightDivisionRepository::class)]
#[ORM\Table(name: 'weight_division')]
class WeightDivision
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column(length: 100)]
    private ?string $name = null;

    #[ORM\Column(name: 'max_weight_lbs')]
    private ?int $maxWeightLbs = null;

    #[ORM\Column(length: 100, unique: true)]
    private ?string $slug = null;

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getName(): ?string
    {
        return $this->name;
    }

    public function setName(string $name): self
    {
        $this->name = $name;
        return $this;
    }

    public function getMaxWeightLbs(): ?int
    {
        return $this->maxWeightLbs;
    }

    public function setMaxWeightLbs(int $maxWeightLbs): self
    {
        $this->maxWeightLbs = $maxWeightLbs;
        return $this;
    }

    public function getSlug(): ?string
    {
        return $this->slug;
    }

    public function setSlug(string $slug): self
    {
        $this->slug = $slug;
        return $this;
    }

    public function __toString(): string
    {
        return $this->name ?? '';
    }
}
