<?php

namespace App\Controller;

use App\Entity\FanProfile;
use App\Entity\User;
use App\Form\RegistrationFormType;
use App\Repository\UserRoleRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;
use Symfony\Component\Routing\Annotation\Route;

class RegistrationController extends AbstractController
{
    #[Route('/register', name: 'app_register', methods: ['GET', 'POST'])]
    public function register(
        Request $request,
        EntityManagerInterface $entityManager,
        UserPasswordHasherInterface $passwordHasher,
        UserRoleRepository $userRoleRepository,
    ): Response {
        $user = new User();
        $form = $this->createForm(RegistrationFormType::class, $user);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $plainPassword = (string) $form->get('plainPassword')->getData();
            $user->setPassword($passwordHasher->hashPassword($user, $plainPassword));

            $role = $userRoleRepository->findOneBy(['name' => 'ROLE_FAN'])
                ?? $userRoleRepository->findOneBy(['name' => 'FAN']);

            if ($role === null) {
                $this->addFlash('error', 'Fan role not found. Please contact support.');
                return $this->redirectToRoute('app_register');
            }

            $user->setRole($role);

            $fanProfile = (new FanProfile())->setUser($user);

            $entityManager->persist($user);
            $entityManager->persist($fanProfile);
            $entityManager->flush();

            $this->addFlash('success', 'Registration complete. Please sign in.');
            return $this->redirectToRoute('app_login');
        }

        return $this->render('registration/register.html.twig', [
            'registrationForm' => $form->createView(),
        ]);
    }
}
