<?php
namespace App\Security;

use App\Entity\User;
use App\Entity\Role;
use App\Repository\UserRepository;
use Doctrine\ORM\EntityManagerInterface;
use KnpU\OAuth2ClientBundle\Client\ClientRegistry;
use KnpU\OAuth2ClientBundle\Security\Authenticator\OAuth2Authenticator;
use Symfony\Component\HttpFoundation\RedirectResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\RouterInterface;
use Symfony\Component\Security\Core\Authentication\Token\TokenInterface;
use Symfony\Component\Security\Core\Exception\AuthenticationException;
use Symfony\Component\Security\Http\Authenticator\Passport\Badge\UserBadge;
use Symfony\Component\Security\Http\Authenticator\Passport\Passport;
use Symfony\Component\Security\Http\Authenticator\Passport\SelfValidatingPassport;
use Symfony\Component\Security\Http\EntryPoint\AuthenticationEntryPointInterface;

class GoogleAuthenticator extends OAuth2Authenticator implements AuthenticationEntryPointInterface
{
    public function __construct(
        private ClientRegistry $clientRegistry,
        private EntityManagerInterface $em,
        private RouterInterface $router,
        private UserRepository $userRepository
    ) {}

    public function supports(Request $request): ?bool
    {
        return $request->attributes->get('_route') === 'connect_google_check';
    }

    public function authenticate(Request $request): Passport
    {
        $client = $this->clientRegistry->getClient('google');
        $accessToken = $this->fetchAccessToken($client);

        return new SelfValidatingPassport(
            new UserBadge($accessToken->getToken(), function() use ($accessToken, $client) {
                /** @var \League\OAuth2\Client\Provider\GoogleUser $googleUser */
                $googleUser = $client->fetchUserFromToken($accessToken);

                $email = $googleUser->getEmail();

                // 1) Check if the user already exists
                $existingUser = $this->userRepository->findOneBy(['email' => $email]);

                if ($existingUser) {
                    // Detach to prevent Doctrine from scheduling stale collection updates
                    $this->em->getUnitOfWork()->clear();
                    // Re-fetch fresh from DB
                    return $this->userRepository->findOneBy(['email' => $email]);
                }

                // 2) Check if a user with this username already exists (avoid unique constraint)
                $name = $googleUser->getName() ?: explode('@', $email)[0];
                $existingByName = $this->userRepository->findOneBy(['username' => $name]);
                if ($existingByName) {
                    $name = $name . '_' . substr(md5($email), 0, 4);
                }

                // 3) Create a new User (Auto-Registration)
                $user = new User();
                $user->setEmail($email);
                $user->setUsername($name);
                
                // Set a random secure password (they will use Google to login anyway)
                $user->setPassword(bin2hex(random_bytes(16)));
                $user->setIsVerified(true);
                $user->setCreatedDate(new \DateTime());

                // Assign default FAN role (or USER fallback)
                $fanRole = $this->em->getRepository(Role::class)->findOneBy(['roleName' => 'FAN']);
                if (!$fanRole) {
                    $fanRole = $this->em->getRepository(Role::class)->findOneBy(['roleName' => 'USER']);
                }

                if ($fanRole) {
                    $user->addRole($fanRole);
                }

                try {
                    $this->em->persist($user);
                    $this->em->flush();
                } catch (\Exception $e) {
                    // If duplicate constraint, clear and re-fetch
                    $this->em->clear();
                    $retryUser = $this->userRepository->findOneBy(['email' => $email]);
                    if ($retryUser) {
                        return $retryUser;
                    }
                    throw $e;
                }

                return $user;
            })
        );
    }

    public function onAuthenticationSuccess(Request $request, TokenInterface $token, string $firewallName): ?Response
    {
        return new RedirectResponse($this->router->generate('app_dashboard'));
    }

    public function onAuthenticationFailure(Request $request, AuthenticationException $exception): ?Response
    {
        $message = strtr($exception->getMessageKey(), $exception->getMessageData());
        $request->getSession()->getFlashBag()->add('error', $message);
        return new RedirectResponse($this->router->generate('app_login'));
    }

    public function start(Request $request, AuthenticationException $authException = null): Response
    {
        return new RedirectResponse($this->router->generate('app_login'));
    }
}
