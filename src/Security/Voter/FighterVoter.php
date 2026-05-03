<?php

namespace App\Security\Voter;

use App\Entity\Fighter;
use App\Entity\User;
use Symfony\Component\Security\Core\Authentication\Token\TokenInterface;
use Symfony\Component\Security\Core\Authorization\Voter\Voter;
use Symfony\Bundle\SecurityBundle\Security;

class FighterVoter extends Voter
{
    public const MANAGE = 'CAN_MANAGE_FIGHTER';

    private $security;

    public function __construct(Security $security)
    {
        $this->security = $security;
    }

    protected function supports(string $attribute, $subject): bool
    {
        return $attribute === self::MANAGE && $subject instanceof Fighter;
    }

    protected function voteOnAttribute(string $attribute, $subject, TokenInterface $token): bool
    {
        $user = $token->getUser();
        if (!$user instanceof User) {
            return false;
        }

        // Admins can do everything
        if ($this->security->isGranted('ROLE_ADMIN')) {
            return true;
        }

        /** @var Fighter $fighter */
        $fighter = $subject;

        return $this->canManage($fighter, $user);
    }

    private function canManage(Fighter $fighter, User $user): bool
    {
        // Check if the user is the assigned manager
        return $fighter->getManager() && $fighter->getManager()->getUserId() === $user->getUserId();
    }
}
