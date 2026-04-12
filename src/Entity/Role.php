<?php
namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity]
#[ORM\Table(name: 'roles')]
class Role
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'roleId')]
    private ?int $roleId = null;

    #[ORM\Column(name: 'roleName', length: 50)]
    private ?string $roleName = null;

    public function getRoleId(): ?int { return $this->roleId; }
    public function getRoleName(): ?string { return $this->roleName; }
    public function setRoleName(string $v): self { $this->roleName = $v; return $this; }

    public function __toString(): string { return $this->roleName ?? ''; }
}
