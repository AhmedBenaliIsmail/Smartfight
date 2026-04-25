<?php

namespace App\Security;

use App\Entity\FanProfile;
use App\Entity\User;
use App\Repository\UserRepository;
use App\Repository\UserRoleRepository;
use Doctrine\ORM\EntityManagerInterface;
use KnpU\OAuth2ClientBundle\Client\ClientRegistry;
use KnpU\OAuth2ClientBundle\Security\Authenticator\OAuth2Authenticator;
use Symfony\Component\HttpFoundation\RedirectResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;
use Symfony\Component\Routing\RouterInterface;
use Symfony\Component\Security\Core\Authentication\Token\TokenInterface;
use Symfony\Component\Security\Core\Exception\AuthenticationException;
use Symfony\Component\Security\Http\Authenticator\Passport\Badge\UserBadge;
use Symfony\Component\Security\Http\Authenticator\Passport\SelfValidatingPassport;
use Symfony\Component\Security\Http\Authenticator\Passport\Passport;
use Symfony\Component\Security\Http\Util\TargetPathTrait;

class GoogleAuthenticator extends OAuth2Authenticator
{
    use TargetPathTrait;

    public function __construct(
        private readonly ClientRegistry $clientRegistry,
        private readonly EntityManagerInterface $entityManager,
        private readonly UserRepository $userRepository,
        private readonly UserRoleRepository $userRoleRepository,
        private readonly UserPasswordHasherInterface $passwordHasher,
        private readonly RouterInterface $router,
    ) {
    }

    public function supports(Request $request): bool
    {
        return $request->attributes->get('_route') === 'connect_google_check';
    }

    public function authenticate(Request $request): Passport
    {
        $client = $this->clientRegistry->getClient('google');
        $accessToken = $this->fetchAccessToken($client);
        $googleUser = $client->fetchUserFromToken($accessToken);
        $email = $googleUser->getEmail();

        if (!$email) {
            throw new AuthenticationException('Google account does not provide an email address.');
        }

        return new SelfValidatingPassport(new UserBadge($email, function () use ($googleUser, $email) {
            $user = $this->userRepository->findOneBy(['email' => $email]);
            if ($user instanceof User) {
                $this->ensureFanProfile($user);
                return $user;
            }

            $firstName = $googleUser->getFirstName() ?: 'Fan';
            $lastName = $googleUser->getLastName() ?: 'User';

            $user = (new User())
                ->setFirstName($firstName)
                ->setLastName($lastName)
                ->setEmail($email);

            $user->setPassword($this->passwordHasher->hashPassword($user, bin2hex(random_bytes(16))));

            $role = $this->userRoleRepository->findOneBy(['name' => 'ROLE_FAN'])
                ?? $this->userRoleRepository->findOneBy(['name' => 'FAN']);

            if ($role !== null) {
                $user->setRole($role);
            }

            $this->entityManager->persist($user);
            $this->ensureFanProfile($user);
            $this->entityManager->flush();

            return $user;
        }));
    }

    public function onAuthenticationSuccess(Request $request, TokenInterface $token, string $firewallName): ?RedirectResponse
    {
        $targetPath = $this->getTargetPath($request->getSession(), $firewallName);
        if ($targetPath) {
            return new RedirectResponse($targetPath);
        }

        return new RedirectResponse($this->router->generate('front_blog_index'));
    }

    public function onAuthenticationFailure(Request $request, AuthenticationException $exception): ?RedirectResponse
    {
        $request->getSession()->getFlashBag()->add('error', 'Google sign-in failed.');
        return new RedirectResponse($this->router->generate('app_login'));
    }

    private function ensureFanProfile(User $user): void
    {
        $profile = $this->entityManager->getRepository(FanProfile::class)
            ->findOneBy(['user' => $user]);

        if ($profile instanceof FanProfile) {
            return;
        }

        $this->entityManager->persist((new FanProfile())->setUser($user));
    }
}
