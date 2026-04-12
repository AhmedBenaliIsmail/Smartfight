<?php
namespace App\Controller;

use App\Entity\User;
use App\Repository\UserRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\Security\Http\Authentication\AuthenticationUtils;
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;

class SecurityController extends AbstractController
{
    #[Route('/login', name: 'app_login')]
    public function login(AuthenticationUtils $authUtils): Response
    {
        if ($this->getUser()) {
            return $this->redirectToRoute('app_dashboard');
        }
        return $this->render('security/login.html.twig', [
            'last_username' => $authUtils->getLastUsername(),
            'error' => $authUtils->getLastAuthenticationError(),
        ]);
    }

    #[Route('/register', name: 'app_register', methods: ['GET', 'POST'])]
    public function register(Request $request, UserPasswordHasherInterface $hasher, EntityManagerInterface $em, UserRepository $userRepo): Response
    {
        if ($request->isMethod('POST')) {
            $username = trim($request->request->get('username', ''));
            $password = $request->request->get('password', '');
            $email = trim($request->request->get('email', ''));

            if (empty($username) || empty($password)) {
                $this->addFlash('error', 'Username and password are required.');
                return $this->redirectToRoute('app_register');
            }
            if ($userRepo->usernameExists($username)) {
                $this->addFlash('error', 'Username already taken.');
                return $this->redirectToRoute('app_register');
            }

            $user = new User();
            $user->setUsername($username);
            $user->setPassword($hasher->hashPassword($user, $password));
            $user->setEmail($email ?: null);

            // Assign USER role
            $role = $em->getRepository(\App\Entity\Role::class)->findOneBy(['roleName' => 'USER']);
            if ($role) $user->addRole($role);

            $em->persist($user);
            $em->flush();

            $this->addFlash('success', 'Account created! You can now log in.');
            return $this->redirectToRoute('app_login');
        }

        return $this->render('security/register.html.twig');
    }

    #[Route('/logout', name: 'app_logout')]
    public function logout(): void
    {
        // Symfony handles this
    }
}
