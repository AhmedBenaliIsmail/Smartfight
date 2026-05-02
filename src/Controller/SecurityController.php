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

use Symfony\Component\Mailer\MailerInterface;
use Symfony\Bridge\Twig\Mime\TemplatedEmail;
use Symfony\Component\Mime\Address;

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
    public function register(Request $request, UserPasswordHasherInterface $hasher, EntityManagerInterface $em, MailerInterface $mailer): Response
    {
        if ($this->getUser()) {
            return $this->redirectToRoute('app_dashboard');
        }

        $user = new User();
        $form = $this->createForm(\App\Form\RegistrationType::class, $user);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            // Set initial verification state
            $token = bin2hex(random_bytes(32));
            $user->setVerificationToken($token);
            $user->setIsVerified(false);

            // Encode the plain password
            $user->setPassword(
                $hasher->hashPassword($user, $user->getPassword())
            );

            // Assign USER role
            $role = $em->getRepository(\App\Entity\Role::class)->findOneBy(['roleName' => 'USER']);
            if ($role) $user->addRole($role);

            $em->persist($user);
            $em->flush();

            // Send real verification email
            try {
                $verifyUrl = $this->generateUrl('app_verify_email', ['token' => $token], \Symfony\Component\Routing\Generator\UrlGeneratorInterface::ABSOLUTE_URL);
                $email = (new TemplatedEmail())
                    ->from(new Address('mahdidaly24@gmail.com', 'SmartFight Security'))
                    ->to($user->getEmail())
                    ->subject('Verify your SmartFight Account')
                    ->htmlTemplate('emails/verification.html.twig')
                    ->context([
                        'user' => $user,
                        'verifyUrl' => $verifyUrl,
                    ]);

                $mailer->send($email);
                $this->addFlash('success', "Email sent.");
            } catch (\Exception $e) {
                $this->addFlash('warning', "Registration successful, but we couldn't send the verification email. Please contact support.");
            }
            
            return $this->redirectToRoute('app_login');
        }

        return $this->render('security/register.html.twig', [
            'registrationForm' => $form->createView(),
        ]);
    }

    #[Route('/forgot-password', name: 'app_forgot_password', methods: ['GET', 'POST'])]
    public function forgotPassword(Request $request, UserRepository $userRepo, EntityManagerInterface $em, MailerInterface $mailer): Response
    {
        if ($request->isMethod('POST')) {
            $identifier = trim($request->request->get('email', ''));

            if (empty($identifier)) {
                $this->addFlash('error', 'Please enter your username or email');
                return $this->render('security/forgot_password.html.twig');
            }

            // Search by email OR username
            $user = $userRepo->createQueryBuilder('u')
                ->where('u.email = :id')
                ->orWhere('u.username = :id')
                ->setParameter('id', $identifier)
                ->getQuery()
                ->getOneOrNullResult();

            if ($user) {
                $token = bin2hex(random_bytes(32));
                $user->setResetToken($token);
                $user->setResetTokenExpiresAt(new \DateTime('+1 hour'));
                $em->persist($user);
                $em->flush();

                // Send real recovery email
                try {
                    $resetUrl = $this->generateUrl('app_reset_password', ['token' => $token], \Symfony\Component\Routing\Generator\UrlGeneratorInterface::ABSOLUTE_URL);
                    $emailObj = (new TemplatedEmail())
                        ->from(new Address('mahdidaly24@gmail.com', 'SmartFight Security'))
                        ->to($user->getEmail())
                        ->subject('Reset your SmartFight Password')
                        ->htmlTemplate('emails/password_reset.html.twig')
                        ->context([
                            'user' => $user,
                            'resetUrl' => $resetUrl,
                        ]);

                    $mailer->send($emailObj);
                    $this->addFlash('success', "Recovery link sent to " . $user->getEmail());
                } catch (\Exception $e) {
                    $this->addFlash('error', "Failed to send reset email. Please try again later.");
                }
            } else {
                $this->addFlash('error', 'You don\'t have an account.');
            }
        }
        return $this->render('security/forgot_password.html.twig');
    }

    #[Route('/reset-password/{token}', name: 'app_reset_password', methods: ['GET', 'POST'])]
    public function resetPassword(string $token, Request $request, UserRepository $userRepo, UserPasswordHasherInterface $hasher, EntityManagerInterface $em): Response
    {
        $user = $userRepo->findOneBy(['resetToken' => $token]);

        if (!$user || $user->getResetTokenExpiresAt() < new \DateTime()) {
            $this->addFlash('error', 'Invalid or expired token.');
            return $this->redirectToRoute('app_forgot_password');
        }

        if ($request->isMethod('POST')) {
            $password = $request->request->get('password');
            if (strlen($password) < 6) {
                $this->addFlash('error', 'Password must be at least 6 characters.');
                return $this->render('security/reset_password.html.twig', ['token' => $token]);
            }

            $user->setPassword($hasher->hashPassword($user, $password));
            $user->setResetToken(null);
            $user->setResetTokenExpiresAt(null);
            $em->flush();

            $this->addFlash('success', 'Password reset successfully! You can now log in.');
            return $this->redirectToRoute('app_login');
        }

        return $this->render('security/reset_password.html.twig', ['token' => $token]);
    }

    #[Route('/verify-email/{token}', name: 'app_verify_email')]
    public function verifyEmail(string $token, UserRepository $userRepo, EntityManagerInterface $em): Response
    {
        $user = $userRepo->findOneBy(['verificationToken' => $token]);

        if (!$user) {
            $this->addFlash('error', 'Invalid verification link.');
            return $this->redirectToRoute('app_register');
        }

        $user->setIsVerified(true);
        $user->setVerificationToken(null);
        $em->flush();

        $this->addFlash('success', 'Email verified! Your account is now active and you can log in.');
        return $this->redirectToRoute('app_login');
    }

    #[Route('/logout', name: 'app_logout')]
    public function logout(): void
    {
        // Symfony handles this
    }
}
