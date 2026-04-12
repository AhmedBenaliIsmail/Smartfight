<?php

namespace App\Controller;

use App\Entity\Combattant;
use App\Form\CombattantType;
use App\Repository\CombattantRepository;
use App\Service\MatchmakingDataSyncService;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/combattant')]
final class CombattantController extends AbstractController
{
    #[Route(name: 'app_combattant_index', methods: ['GET'])]
    public function index(
        CombattantRepository $combattantRepository,
        MatchmakingDataSyncService $dataSyncService,
    ): Response
    {
        $created = $dataSyncService->syncCombattantsFromFighters();
        if ($created > 0) {
            $this->addFlash('success', sprintf('%d combattants were imported from fighters data.', $created));
        }

        return $this->render('combattant/index.html.twig', [
            'combattants' => $combattantRepository->findAll(),
            'active_sidebar' => 'combattants',
        ]);
    }

   #[Route('/new', name: 'app_combattant_new', methods: ['GET', 'POST'])]
public function new(Request $request, EntityManagerInterface $entityManager): Response
{
    $combattant = new Combattant();
    $form = $this->createForm(CombattantType::class, $combattant);
    $form->handleRequest($request);

    if ($form->isSubmitted() && $form->isValid()) {
        $entityManager->persist($combattant);
        $entityManager->flush();
        return $this->redirectToRoute('app_combattant_index');
    }

    return $this->render('combattant/new.html.twig', [
        'combattant' => $combattant,
        'form' => $form,
    ]);
}

    #[Route('/{id}', name: 'app_combattant_show', methods: ['GET'])]
    public function show(Combattant $combattant): Response
    {
        return $this->render('combattant/show.html.twig', [
            'combattant' => $combattant,
        ]);
    }

    #[Route('/{id}/edit', name: 'app_combattant_edit', methods: ['GET', 'POST'])]
    public function edit(Request $request, Combattant $combattant, EntityManagerInterface $entityManager): Response
    {
        $form = $this->createForm(CombattantType::class, $combattant);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $entityManager->flush();

            return $this->redirectToRoute('app_combattant_index', [], Response::HTTP_SEE_OTHER);
        }

        return $this->render('combattant/edit.html.twig', [
            'combattant' => $combattant,
            'form' => $form,
        ]);
    }

    #[Route('/{id}', name: 'app_combattant_delete', methods: ['POST'])]
    public function delete(Request $request, Combattant $combattant, EntityManagerInterface $entityManager): Response
    {
        if ($this->isCsrfTokenValid('delete'.$combattant->getId(), $request->getPayload()->getString('_token'))) {
            $entityManager->remove($combattant);
            $entityManager->flush();
        }

        return $this->redirectToRoute('app_combattant_index', [], Response::HTTP_SEE_OTHER);
    }
}
