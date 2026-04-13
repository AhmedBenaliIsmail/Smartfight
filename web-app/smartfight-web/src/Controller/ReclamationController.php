<?php

namespace App\Controller;

use App\Entity\Reclamation;
use App\Form\ReclamationAdminType;
use App\Form\ReclamationType;
use App\Repository\ReclamationRepository;
use App\Repository\UserRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

class ReclamationController extends AbstractController
{
    // ── FRONT ROUTES ─────────────────────────────────────────────────────

    #[Route('/reclamations', name: 'app_reclamation_index', methods: ['GET'])]
    public function index(ReclamationRepository $reclamationRepository): Response
    {
        $reclamations = $reclamationRepository->findBy([], ['createdAt' => 'DESC']);

        return $this->render('front/reclamation/index.html.twig', [
            'reclamations' => $reclamations,
        ]);
    }

    #[Route('/reclamations/new', name: 'app_reclamation_new', methods: ['GET', 'POST'])]
    public function new(Request $request, EntityManagerInterface $em, UserRepository $userRepository): Response
    {
        $reclamation = new Reclamation();
        $form        = $this->createForm(ReclamationType::class, $reclamation);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $reclamation->setStatus('PENDING');
            $reclamation->setCreatedAt(new \DateTime());
            $reclamation->setUpdatedAt(new \DateTime());

            // Use first user in DB as placeholder (session auth will replace this)
            $user = $userRepository->find(1);
            if ($user) {
                $reclamation->setUser($user);
            }

            $em->persist($reclamation);
            $em->flush();

            $this->addFlash('success', 'Reclamation submitted successfully! We will get back to you soon.');
            return $this->redirectToRoute('app_reclamation_index');
        }

        return $this->render('front/reclamation/new.html.twig', [
            'form' => $form->createView(),
        ]);
    }

    // ── ADMIN ROUTES ──────────────────────────────────────────────────────

    #[Route('/admin/reclamations', name: 'app_admin_reclamation_index', methods: ['GET'])]
    public function adminIndex(Request $request, ReclamationRepository $reclamationRepository): Response
    {
        $status = $request->query->get('status');

        if ($status) {
            $reclamations = $reclamationRepository->findByStatus($status);
        } else {
            $reclamations = $reclamationRepository->findBy([], ['createdAt' => 'DESC']);
        }

        return $this->render('admin/reclamation/index.html.twig', [
            'reclamations' => $reclamations,
            'statusFilter' => $status,
        ]);
    }

    #[Route('/admin/reclamations/{id}/edit', name: 'app_admin_reclamation_edit', methods: ['GET', 'POST'])]
    public function adminEdit(Request $request, Reclamation $reclamation, EntityManagerInterface $em): Response
    {
        $form = $this->createForm(ReclamationAdminType::class, $reclamation);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $reclamation->setUpdatedAt(new \DateTime());
            $em->flush();

            $this->addFlash('success', 'Reclamation updated.');
            return $this->redirectToRoute('app_admin_reclamation_index');
        }

        return $this->render('admin/reclamation/edit.html.twig', [
            'form'        => $form->createView(),
            'reclamation' => $reclamation,
        ]);
    }

    #[Route('/admin/reclamations/{id}', name: 'app_admin_reclamation_delete', methods: ['POST'])]
    public function adminDelete(Request $request, Reclamation $reclamation, EntityManagerInterface $em): Response
    {
        $token = $request->request->get('_token');
        if ($this->isCsrfTokenValid('delete' . $reclamation->getId(), $token)) {
            $em->remove($reclamation);
            $em->flush();
            $this->addFlash('success', 'Reclamation deleted.');
        }

        return $this->redirectToRoute('app_admin_reclamation_index');
    }
}
