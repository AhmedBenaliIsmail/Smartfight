<?php
namespace App\Controller;

use App\Entity\User;
use App\Repository\UserRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;

#[Route('/users')]
class UserController extends AbstractController
{
    #[Route('', name: 'app_users')]
    public function index(Request $request, UserRepository $repo): Response
    {
        $this->denyAccessUnlessGranted('ROLE_USER');
        if ($this->isGranted('ROLE_ADMIN')) {
            $q = trim($request->query->get('q', ''));
            $users = $q
                ? $repo->createQueryBuilder('u')
                    ->where('u.username LIKE :q')
                    ->setParameter('q', '%' . $q . '%')
                    ->orderBy('u.userId', 'ASC')
                    ->getQuery()->getResult()
                : $repo->findAll();

            return $this->render('user/index.html.twig', [
                'users' => $users,
                'q'     => $q,
            ]);
        }
        return $this->render('user/profile.html.twig', [
            'user' => $this->getUser(),
        ]);
    }

    #[Route('/new', name: 'app_user_new', methods: ['GET', 'POST'])]
    public function new(Request $request, UserPasswordHasherInterface $hasher, EntityManagerInterface $em, UserRepository $repo): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        if ($request->isMethod('POST')) {
            $username = trim($request->request->get('username', ''));
            $password = $request->request->get('password', '');
            if (empty($username) || empty($password)) {
                $this->addFlash('error', 'Username and password required.');
                return $this->redirectToRoute('app_user_new');
            }
            if ($repo->usernameExists($username)) {
                $this->addFlash('error', 'Username already exists.');
                return $this->redirectToRoute('app_user_new');
            }
            $user = new User();
            $user->setUsername($username);
            $user->setPassword($hasher->hashPassword($user, $password));
            $user->setEmail(trim($request->request->get('email', '')) ?: null);

            $role = $em->getRepository(\App\Entity\Role::class)->findOneBy(['roleName' => 'USER']);
            if ($role) $user->addRole($role);

            $em->persist($user);
            $em->flush();
            $this->addFlash('success', 'User created.');
            return $this->redirectToRoute('app_users');
        }
        return $this->render('user/form.html.twig');
    }

    #[Route('/{id}/update', name: 'app_user_update', methods: ['POST'])]
    public function updateInfo(int $id, Request $request, UserRepository $repo, EntityManagerInterface $em): Response
    {
        $user = $repo->find($id);
        if (!$user) throw $this->createNotFoundException();

        if (!$this->isGranted('ROLE_ADMIN') && $user !== $this->getUser()) {
             throw $this->createAccessDeniedException();
        }

        $newUsername = trim($request->request->get('username', ''));
        $newEmail = trim($request->request->get('email', ''));

        if (!empty($newUsername) && $newUsername !== $user->getUsername()) {
            if ($repo->usernameExists($newUsername)) {
                $this->addFlash('error', 'Username already taken.');
                return $this->redirectToRoute('app_users');
            }
            $user->setUsername($newUsername);
        }
        if ($newEmail !== '') $user->setEmail($newEmail);
        
        $em->flush();
        $this->addFlash('success', 'Profile updated successfully.');
        return $this->redirectToRoute('app_users');
    }

    #[Route('/{id}/password', name: 'app_user_password', methods: ['POST'])]
    public function changePassword(int $id, Request $request, UserRepository $repo, UserPasswordHasherInterface $hasher, EntityManagerInterface $em): Response
    {
        $user = $repo->find($id);
        if (!$user) throw $this->createNotFoundException();

        if (!$this->isGranted('ROLE_ADMIN') && $user !== $this->getUser()) {
             throw $this->createAccessDeniedException();
        }

        $newPass = $request->request->get('newPassword', '');
        if (!empty($newPass)) {
            $user->setPassword($hasher->hashPassword($user, $newPass));
            $em->flush();
            $this->addFlash('success', 'Password updated successfully.');
        }
        return $this->redirectToRoute('app_users');
    }

    #[Route('/{id}/delete', name: 'app_user_delete', methods: ['POST'])]
    public function delete(int $id, UserRepository $repo, EntityManagerInterface $em): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        $user = $repo->find($id);
        if ($user) { $em->remove($user); $em->flush(); $this->addFlash('success', 'User deleted.'); }
        return $this->redirectToRoute('app_users');
    }
}
