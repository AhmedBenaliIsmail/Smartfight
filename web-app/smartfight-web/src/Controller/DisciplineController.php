<?php

namespace App\Controller;

use App\Entity\Discipline;
use App\Form\DisciplineType;
use App\Repository\DisciplineRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/admin/disciplines', name: 'app_admin_discipline_')]
class DisciplineController extends AbstractController
{
    #[Route('', name: 'index', methods: ['GET'])]
    public function index(DisciplineRepository $disciplineRepository): Response
    {
        return $this->render('admin/discipline/index.html.twig', [
            'disciplines' => $disciplineRepository->findAll(),
        ]);
    }

    #[Route('/new', name: 'new', methods: ['GET', 'POST'])]
    public function new(Request $request, EntityManagerInterface $em): Response
    {
        $discipline = new Discipline();
        $form       = $this->createForm(DisciplineType::class, $discipline);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $em->persist($discipline);
            $em->flush();
            $this->addFlash('success', 'Discipline created successfully!');
            return $this->redirectToRoute('app_admin_discipline_index');
        }

        return $this->render('admin/discipline/new.html.twig', [
            'form' => $form->createView(),
        ]);
    }

    #[Route('/{id}', name: 'show', methods: ['GET'], requirements: ['id' => '\d+'])]
    public function show(Discipline $discipline): Response
    {
        return $this->render('admin/discipline/show.html.twig', [
            'discipline' => $discipline,
        ]);
    }

    #[Route('/{id}/edit', name: 'edit', methods: ['GET', 'POST'], requirements: ['id' => '\d+'])]
    public function edit(Request $request, Discipline $discipline, EntityManagerInterface $em): Response
    {
        $form = $this->createForm(DisciplineType::class, $discipline);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $em->flush();
            $this->addFlash('success', 'Discipline modified successfully!');
            return $this->redirectToRoute('app_admin_discipline_show', ['id' => $discipline->getId()]);
        }

        return $this->render('admin/discipline/edit.html.twig', [
            'form'       => $form->createView(),
            'discipline' => $discipline,
        ]);
    }

    #[Route('/{id}', name: 'delete', methods: ['POST'], requirements: ['id' => '\d+'])]
    public function delete(Request $request, Discipline $discipline, EntityManagerInterface $em): Response
    {
        $token = $request->request->get('_token');
        if ($this->isCsrfTokenValid('delete' . $discipline->getId(), $token)) {
            $em->remove($discipline);
            $em->flush();
            $this->addFlash('success', 'Discipline deleted.');
        }

        return $this->redirectToRoute('app_admin_discipline_index');
    }
}
