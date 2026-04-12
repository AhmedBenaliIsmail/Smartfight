<?php

namespace App\Controller\Admin;

use App\Entity\FightResult;
use App\Form\FightResultType;
use App\Repository\FightResultRepository;
use App\Service\AnalyticsEngine;
use App\Service\RankingService;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpFoundation\StreamedResponse;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/admin/results', name: 'admin_result_')]
class ResultController extends AbstractController
{
    #[Route('', name: 'index', methods: ['GET'])]
    public function index(Request $request, FightResultRepository $fightResultRepository): Response
    {
        $status = strtoupper((string) $request->query->get('status', 'ALL'));
        $results = $status === 'ALL'
            ? $fightResultRepository->findBy([], ['fightDate' => 'DESC'])
            : $fightResultRepository->findByStatus($status);

        return $this->render('admin/result/index.html.twig', [
            'results' => $results,
            'status' => $status,
            'active_sidebar' => 'results',
        ]);
    }

    #[Route('/new', name: 'new', methods: ['GET', 'POST'])]
    public function new(
        Request $request,
        EntityManagerInterface $entityManager,
        RankingService $rankingService,
        AnalyticsEngine $analyticsEngine,
    ): Response {
        $result = new FightResult();
        $result->setFightDate(new \DateTime());
        $form = $this->createForm(FightResultType::class, $result);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            if ($result->getFighterRed()->getId() === $result->getFighterBlue()->getId()) {
                $this->addFlash('error', 'Red and blue fighters must be different.');
            } elseif (
                $result->getWinner() !== null
                && $result->getWinner()->getId() !== $result->getFighterRed()->getId()
                && $result->getWinner()->getId() !== $result->getFighterBlue()->getId()
            ) {
                $this->addFlash('error', 'Winner must be one of the two fighters.');
            } else {
                $entityManager->persist($result);
                $entityManager->flush();

                if ($result->getStatus() === 'COMPLETED') {
                    $rankingService->recomputeRankings();
                    $analyticsEngine->recomputeFighter($result->getFighterRed());
                    $analyticsEngine->recomputeFighter($result->getFighterBlue());
                }

                $this->addFlash('success', 'Fight result created. Continue to the statistics wizard if needed.');

                return $this->redirectToRoute('admin_statistic_wizard', ['fightResult' => $result->getId()]);
            }
        }

        return $this->render('admin/result/form.html.twig', [
            'form' => $form->createView(),
            'result' => $result,
            'is_edit' => false,
            'is_wizard' => true,
            'active_sidebar' => 'results',
        ]);
    }

    #[Route('/{id}', name: 'show', methods: ['GET'])]
    public function show(FightResult $result): Response
    {
        return $this->render('admin/result/show.html.twig', [
            'result' => $result,
            'active_sidebar' => 'results',
        ]);
    }

    #[Route('/{id}/edit', name: 'edit', methods: ['GET', 'POST'])]
    public function edit(
        Request $request,
        FightResult $result,
        EntityManagerInterface $entityManager,
        RankingService $rankingService,
        AnalyticsEngine $analyticsEngine,
    ): Response {
        $form = $this->createForm(FightResultType::class, $result);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            if ($result->getFighterRed()->getId() === $result->getFighterBlue()->getId()) {
                $this->addFlash('error', 'Red and blue fighters must be different.');
            } elseif (
                $result->getWinner() !== null
                && $result->getWinner()->getId() !== $result->getFighterRed()->getId()
                && $result->getWinner()->getId() !== $result->getFighterBlue()->getId()
            ) {
                $this->addFlash('error', 'Winner must be one of the two fighters.');
            } else {
                $entityManager->flush();

                if ($result->getStatus() === 'COMPLETED') {
                    $rankingService->recomputeRankings();
                    $analyticsEngine->recomputeFighter($result->getFighterRed());
                    $analyticsEngine->recomputeFighter($result->getFighterBlue());
                }

                $this->addFlash('success', 'Fight result updated.');

                return $this->redirectToRoute('admin_result_index');
            }
        }

        return $this->render('admin/result/form.html.twig', [
            'form' => $form->createView(),
            'result' => $result,
            'is_edit' => true,
            'is_wizard' => false,
            'active_sidebar' => 'results',
        ]);
    }

    #[Route('/{id}/delete', name: 'delete', methods: ['POST'])]
    public function delete(Request $request, FightResult $result, EntityManagerInterface $entityManager): Response
    {
        if ($this->isCsrfTokenValid('delete_result_' . $result->getId(), (string) $request->request->get('_token'))) {
            $entityManager->remove($result);
            $entityManager->flush();
            $this->addFlash('success', 'Fight result deleted.');
        }

        return $this->redirectToRoute('admin_result_index');
    }

    #[Route('/export.csv', name: 'export_csv', methods: ['GET'])]
    public function exportCsv(FightResultRepository $fightResultRepository): StreamedResponse
    {
        $response = new StreamedResponse(function () use ($fightResultRepository): void {
            $handle = fopen('php://output', 'w');
            fputcsv($handle, ['id', 'event', 'fight_label', 'status', 'method', 'winner_id', 'fight_date']);

            foreach ($fightResultRepository->findBy([], ['fightDate' => 'DESC']) as $result) {
                fputcsv($handle, [
                    $result->getId(),
                    $result->getEvent()->getName(),
                    $result->getFightLabel(),
                    $result->getStatus(),
                    $result->getMethod(),
                    $result->getWinner()?->getId(),
                    $result->getFightDate()->format('Y-m-d'),
                ]);
            }

            fclose($handle);
        });

        $response->headers->set('Content-Type', 'text/csv; charset=UTF-8');
        $response->headers->set('Content-Disposition', 'attachment; filename="fight_results.csv"');

        return $response;
    }
}
