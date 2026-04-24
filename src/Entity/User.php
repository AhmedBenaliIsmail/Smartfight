<?php
namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;
use App\Repository\UserRepository;
use Symfony\Component\Security\Core\User\UserInterface;
use Symfony\Component\Security\Core\User\PasswordAuthenticatedUserInterface;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;

#[ORM\Entity(repositoryClass: UserRepository::class)]
#[ORM\Table(name: 'users')]
class User implements UserInterface, PasswordAuthenticatedUserInterface
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'userId')]
    private ?int $userId = null;

    #[ORM\Column(length: 100, unique: true)]
    private ?string $username = null;

    #[ORM\Column(length: 255)]
    private ?string $password = null;

    #[ORM\Column(length: 200, nullable: true)]
    private ?string $email = null;

    #[ORM\Column(name: 'createdDate', type: 'datetime', nullable: true)]
    private ?\DateTimeInterface $createdDate = null;

    #[ORM\Column(name: 'predictionPoints', type: 'integer', options: ['default' => 0])]
    private int $predictionPoints = 0;

    #[ORM\ManyToMany(targetEntity: Role::class)]
    #[ORM\JoinTable(
        name: 'user_roles',
        joinColumns: [new ORM\JoinColumn(name: 'userId', referencedColumnName: 'userId')],
        inverseJoinColumns: [new ORM\JoinColumn(name: 'roleId', referencedColumnName: 'roleId')]
    )]
    private Collection $roles;

    public function __construct()
    {
        $this->createdDate = new \DateTime();
        $this->roles = new ArrayCollection();
    }

    public function getUserId(): ?int { return $this->userId; }
    public function getUsername(): ?string { return $this->username; }
    public function setUsername(string $v): self { $this->username = $v; return $this; }
    public function getPassword(): ?string { return $this->password; }
    public function setPassword(string $v): self { $this->password = $v; return $this; }
    public function getEmail(): ?string { return $this->email; }
    public function setEmail(?string $v): self { $this->email = $v; return $this; }
    public function getCreatedDate(): ?\DateTimeInterface { return $this->createdDate; }
    public function setCreatedDate(?\DateTimeInterface $v): self { $this->createdDate = $v; return $this; }
    public function getPredictionPoints(): int { return $this->predictionPoints; }
    public function setPredictionPoints(int $v): self { $this->predictionPoints = $v; return $this; }

    public function getEntityRoles(): Collection { return $this->roles; }

    public function addRole(Role $role): self
    {
        if (!$this->roles->contains($role)) {
            $this->roles->add($role);
        }
        return $this;
    }

    public function removeRole(Role $role): self
    {
        $this->roles->removeElement($role);
        return $this;
    }

    public function hasRole(string $roleName): bool
    {
        foreach ($this->roles as $role) {
            if (strtoupper($role->getRoleName()) === strtoupper($roleName)) return true;
        }
        return false;
    }

    public function getRolesAsString(): string
    {
        if ($this->roles->isEmpty()) return 'No Roles';
        return implode(', ', $this->roles->map(fn(Role $r) => $r->getRoleName())->toArray());
    }

    // Symfony Security Interface
    public function getRoles(): array
    {
        $symRoles = ['ROLE_USER'];
        foreach ($this->roles as $role) {
            $symRoles[] = 'ROLE_' . strtoupper($role->getRoleName());
        }
        return array_unique($symRoles);
    }

    public function eraseCredentials(): void {}

    public function getUserIdentifier(): string
    {
        return $this->username ?? '';
    }

    public function getFullName(): string
    {
        return $this->username ?? 'User';
    }
}
