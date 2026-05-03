<?php
namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;
use App\Repository\FightStatisticRepository;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity(repositoryClass: FightStatisticRepository::class)]
#[ORM\Table(name: 'fight_statistic')]
class FightStatistic
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\ManyToOne(targetEntity: FightResult::class)]
    #[ORM\JoinColumn(name: "fight_result_id", referencedColumnName: "resultId", nullable: false)]
    private ?FightResult $fightResult = null;

    #[ORM\ManyToOne(targetEntity: Fighter::class)]
    #[ORM\JoinColumn(name: "fighter_id", referencedColumnName: "fighterId", nullable: false)]
    private ?Fighter $fighter = null;

    #[ORM\Column(name: 'round', type: 'integer', nullable: true)]
    #[Assert\Range(min: 1, max: 12)]
    private ?int $round = null;

    #[ORM\Column(name: 'punches_landed', type: 'integer', options: ['default' => 0])]
    #[Assert\PositiveOrZero]
    private int $punchesLanded = 0;

    #[ORM\Column(name: 'punches_thrown', type: 'integer', options: ['default' => 0])]
    #[Assert\PositiveOrZero]
    private int $punchesThrown = 0;

    #[ORM\Column(name: 'body_shots_landed', type: 'integer', options: ['default' => 0])]
    #[Assert\PositiveOrZero]
    private int $bodyShotsLanded = 0;

    #[ORM\Column(name: 'body_jabs_landed', type: 'integer', options: ['default' => 0])]
    #[Assert\PositiveOrZero]
    private int $bodyJabsLanded = 0;

    #[ORM\Column(name: 'body_power_landed', type: 'integer', options: ['default' => 0])]
    #[Assert\PositiveOrZero]
    private int $bodyPowerLanded = 0;

    #[ORM\Column(name: 'jabs_landed', type: 'integer', options: ['default' => 0])]
    #[Assert\PositiveOrZero]
    private int $jabsLanded = 0;

    #[ORM\Column(name: 'jabs_thrown', type: 'integer', options: ['default' => 0])]
    #[Assert\PositiveOrZero]
    private int $jabsThrown = 0;

    #[ORM\Column(name: 'power_punches_landed', type: 'integer', options: ['default' => 0])]
    #[Assert\PositiveOrZero]
    private int $powerPunchesLanded = 0;

    #[ORM\Column(name: 'power_punches_thrown', type: 'integer', options: ['default' => 0])]
    #[Assert\PositiveOrZero]
    private int $powerPunchesThrown = 0;

    #[ORM\Column(name: 'uppercuts_landed', type: 'integer', options: ['default' => 0])]
    #[Assert\PositiveOrZero]
    private int $uppercutsLanded = 0;

    #[ORM\Column(name: 'uppercuts_thrown', type: 'integer', options: ['default' => 0])]
    #[Assert\PositiveOrZero]
    private int $uppercutsThrown = 0;

    #[ORM\Column(name: 'right_hand_landed', type: 'integer', options: ['default' => 0])]
    #[Assert\PositiveOrZero]
    private int $rightHandLanded = 0;

    #[ORM\Column(name: 'right_hand_thrown', type: 'integer', options: ['default' => 0])]
    #[Assert\PositiveOrZero]
    private int $rightHandThrown = 0;

    #[ORM\Column(name: 'left_hand_landed', type: 'integer', options: ['default' => 0])]
    #[Assert\PositiveOrZero]
    private int $leftHandLanded = 0;

    #[ORM\Column(name: 'left_hand_thrown', type: 'integer', options: ['default' => 0])]
    #[Assert\PositiveOrZero]
    private int $leftHandThrown = 0;

    #[ORM\Column(type: 'integer', options: ['default' => 0])]
    #[Assert\PositiveOrZero]
    private int $knockdowns = 0;

    #[ORM\Column(type: 'text', nullable: true)]
    private ?string $commentary = null;

    public function getId(): ?int { return $this->id; }

    public function getFightResult(): ?FightResult { return $this->fightResult; }
    public function setFightResult(?FightResult $v): self { $this->fightResult = $v; return $this; }

    public function getFighter(): ?Fighter { return $this->fighter; }
    public function setFighter(?Fighter $v): self { $this->fighter = $v; return $this; }

    public function getRound(): ?int { return $this->round; }
    public function setRound(?int $v): self { $this->round = $v; return $this; }

    public function getPunchesLanded(): int { return $this->punchesLanded; }
    public function setPunchesLanded(int $v): self { $this->punchesLanded = $v; return $this; }

    public function getPunchesThrown(): int { return $this->punchesThrown; }
    public function setPunchesThrown(int $v): self { $this->punchesThrown = $v; return $this; }

    public function getBodyShotsLanded(): int { return $this->bodyShotsLanded; }
    public function setBodyShotsLanded(int $v): self { $this->bodyShotsLanded = $v; return $this; }

    public function getBodyJabsLanded(): int { return $this->bodyJabsLanded; }
    public function setBodyJabsLanded(int $v): self { $this->bodyJabsLanded = $v; return $this; }

    public function getBodyPowerLanded(): int { return $this->bodyPowerLanded; }
    public function setBodyPowerLanded(int $v): self { $this->bodyPowerLanded = $v; return $this; }

    public function getJabsLanded(): int { return $this->jabsLanded; }
    public function setJabsLanded(int $v): self { $this->jabsLanded = $v; return $this; }

    public function getJabsThrown(): int { return $this->jabsThrown; }
    public function setJabsThrown(int $v): self { $this->jabsThrown = $v; return $this; }

    public function getPowerPunchesLanded(): int { return $this->powerPunchesLanded; }
    public function setPowerPunchesLanded(int $v): self { $this->powerPunchesLanded = $v; return $this; }

    public function getPowerPunchesThrown(): int { return $this->powerPunchesThrown; }
    public function setPowerPunchesThrown(int $v): self { $this->powerPunchesThrown = $v; return $this; }

    public function getUppercutsLanded(): int { return $this->uppercutsLanded; }
    public function setUppercutsLanded(int $v): self { $this->uppercutsLanded = $v; return $this; }

    public function getUppercutsThrown(): int { return $this->uppercutsThrown; }
    public function setUppercutsThrown(int $v): self { $this->uppercutsThrown = $v; return $this; }

    public function getRightHandLanded(): int { return $this->rightHandLanded; }
    public function setRightHandLanded(int $v): self { $this->rightHandLanded = $v; return $this; }

    public function getRightHandThrown(): int { return $this->rightHandThrown; }
    public function setRightHandThrown(int $v): self { $this->rightHandThrown = $v; return $this; }

    public function getLeftHandLanded(): int { return $this->leftHandLanded; }
    public function setLeftHandLanded(int $v): self { $this->leftHandLanded = $v; return $this; }

    public function getLeftHandThrown(): int { return $this->leftHandThrown; }
    public function setLeftHandThrown(int $v): self { $this->leftHandThrown = $v; return $this; }

    public function getKnockdowns(): int { return $this->knockdowns; }
    public function setKnockdowns(int $v): self { $this->knockdowns = $v; return $this; }

    public function getCommentary(): ?string { return $this->commentary; }
    public function setCommentary(?string $v): self { $this->commentary = $v; return $this; }

    public function getPunchAccuracy(): float
    {
        if ($this->punchesThrown == 0) return 0.0;
        return ($this->punchesLanded * 100.0) / $this->punchesThrown;
    }

    public function getJabAccuracy(): float
    {
        if ($this->jabsThrown == 0) return 0.0;
        return ($this->jabsLanded * 100.0) / $this->jabsThrown;
    }

    public function getPowerAccuracy(): float
    {
        if ($this->powerPunchesThrown == 0) return 0.0;
        return ($this->powerPunchesLanded * 100.0) / $this->powerPunchesThrown;
    }

    public function getUppercutAccuracy(): float
    {
        if ($this->uppercutsThrown == 0) return 0.0;
        return ($this->uppercutsLanded * 100.0) / $this->uppercutsThrown;
    }

    public function getRightHandAccuracy(): float
    {
        if ($this->rightHandThrown == 0) return 0.0;
        return ($this->rightHandLanded * 100.0) / $this->rightHandThrown;
    }

    public function getLeftHandAccuracy(): float
    {
        if ($this->leftHandThrown == 0) return 0.0;
        return ($this->leftHandLanded * 100.0) / $this->leftHandThrown;
    }

    #[Assert\IsTrue(message: 'Landed punches cannot exceed thrown punches')]
    public function isLandedValid(): bool
    {
        return $this->punchesLanded <= $this->punchesThrown
            && $this->jabsLanded <= $this->jabsThrown
            && $this->powerPunchesLanded <= $this->powerPunchesThrown
            && $this->uppercutsLanded <= $this->uppercutsThrown
            && $this->rightHandLanded <= $this->rightHandThrown
            && $this->leftHandLanded <= $this->leftHandThrown;
    }

    #[Assert\IsTrue(message: 'Total punches thrown must equal right hand thrown + left hand thrown')]
    public function isHandThrownLogicValid(): bool
    {
        return $this->punchesThrown === ($this->rightHandThrown + $this->leftHandThrown);
    }

    #[Assert\IsTrue(message: 'Total punches landed must equal right hand landed + left hand landed')]
    public function isHandLandedLogicValid(): bool
    {
        return $this->punchesLanded === ($this->rightHandLanded + $this->leftHandLanded);
    }

    #[Assert\IsTrue(message: 'Uppercuts thrown cannot exceed total punches thrown')]
    public function isUppercutLogicValid(): bool
    {
        return $this->uppercutsThrown <= $this->punchesThrown && $this->uppercutsLanded <= $this->punchesLanded;
    }
}

