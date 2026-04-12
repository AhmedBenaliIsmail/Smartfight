<?php

namespace App\Controller\Admin;

use App\Entity\FightStatistic;
use App\Entity\FightResult;
use App\Form\FightStatisticType;
use App\Repository\FightResultRepository;
use App\Repository\FighterRepository;
use App\Repository\FightStatisticRepository;
use App\Service\FightStatisticService;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/admin/stats', name: 'admin_statistic_')]
class FightStatisticController extends AbstractController
{
    #[Route('', name: 'index', methods: ['GET'])]
    public function index(
        Request $request,
        FightStatisticRepository $fightStatisticRepository,
        FighterRepository $fighterRepository,
    ): Response
    {
        $fightResultId = $request->query->getInt('fightResultId');
        $statistics = $fightResultId > 0
            ? $fightStatisticRepository->findByFightResult($fightResultId)
            : $fightStatisticRepository->findBy([], ['createdAt' => 'DESC']);
        $fightersById = [];

        foreach ($fighterRepository->findAll() as $fighter) {
            $fightersById[(int) $fighter->getId()] = $fighter;
        }

        return $this->render('admin/statistic/index.html.twig', [
            'statistics' => $statistics,
            'fightersById' => $fightersById,
            'fightResultId' => $fightResultId,
            'active_sidebar' => 'stats',
        ]);
    }

    #[Route('/new', name: 'new', methods: ['GET', 'POST'])]
    public function new(
        Request $request,
        FightResultRepository $fightResultRepository,
        FighterRepository $fighterRepository,
        EntityManagerInterface $entityManager,
    ): Response {
        $statistic = new FightStatistic();
        $form = $this->createForm(FightStatisticType::class, $statistic, [
            'fight_result_choices' => $this->fightResultChoices($fightResultRepository),
            'fighter_choices' => $this->fighterChoices($fighterRepository),
        ]);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $existing = $fightStatisticRepository->findOneBy([
                'fightResultId' => $statistic->getFightResultId(),
                'fighterId' => $statistic->getFighterId(),
            ]);

            if ($existing !== null) {
                $this->addFlash('error', 'Statistic already exists for this fight and fighter. Edit the existing record instead.');

                return $this->redirectToRoute('admin_statistic_edit', ['id' => $existing->getId()]);
            }

            $entityManager->persist($statistic);
            $entityManager->flush();

            $this->addFlash('success', 'Fight statistic created.');

            return $this->redirectToRoute('admin_statistic_index');
        }

        return $this->render('admin/statistic/form.html.twig', [
            'form' => $form->createView(),
            'statistic' => $statistic,
            'is_edit' => false,
            'active_sidebar' => 'stats',
        ]);
    }

    #[Route('/wizard/{fightResult}', name: 'wizard', methods: ['GET', 'POST'])]
    public function wizard(
        FightResult $fightResult,
        Request $request,
        FightStatisticService $fightStatisticService,
    ): Response {
        if ($request->isMethod('POST')) {
            foreach (['red' => $fightResult->getFighterRed(), 'blue' => $fightResult->getFighterBlue()] as $corner => $fighter) {
                $fightStatisticService->upsertForFighter($fightResult, (int) $fighter->getId(), [
                    'strikesLanded' => $request->request->getInt($corner . '_strikesLanded'),
                    'strikesAttempted' => $request->request->getInt($corner . '_strikesAttempted'),
                    'takedownsLanded' => $request->request->getInt($corner . '_takedownsLanded'),
                    'takedownsAttempted' => $request->request->getInt($corner . '_takedownsAttempted'),
                    'submissionAttempts' => $request->request->getInt($corner . '_submissionAttempts'),
                    'knockdowns' => $request->request->getInt($corner . '_knockdowns'),
                    'controlTimeSeconds' => $request->request->getInt($corner . '_controlTimeSeconds'),
                ]);
            }

            $this->addFlash('success', 'Statistics wizard saved for both fighters.');

            return $this->redirectToRoute('admin_result_show', ['id' => $fightResult->getId()]);
        }

        return $this->render('admin/statistic/wizard.html.twig', [
            'fightResult' => $fightResult,
            'active_sidebar' => 'stats',
        ]);
    }

    #[Route('/{id}', name: 'show', methods: ['GET'])]
    public function show(FightStatistic $statistic, FighterRepository $fighterRepository): Response
    {
        $fighter = $fighterRepository->find($statistic->getFighterId());

        return $this->render('admin/statistic/show.html.twig', [
            'statistic' => $statistic,
            'fighter' => $fighter,
            'active_sidebar' => 'stats',
        ]);
    }

    #[Route('/{id}/edit', name: 'edit', methods: ['GET', 'POST'])]
    public function edit(
        Request $request,
        FightStatistic $statistic,
        FightResultRepository $fightResultRepository,
        FighterRepository $fighterRepository,
        EntityManagerInterface $entityManager,
    ): Response {
        $form = $this->createForm(FightStatisticType::class, $statistic, [
            'fight_result_choices' => $this->fightResultChoices($fightResultRepository),
            'fighter_choices' => $this->fighterChoices($fighterRepository),
        ]);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $entityManager->flush();
            $this->addFlash('success', 'Fight statistic updated.');

            return $this->redirectToRoute('admin_statistic_index');
        }

        return $this->render('admin/statistic/form.html.twig', [
            'form' => $form->createView(),
            'statistic' => $statistic,
            'is_edit' => true,
            'active_sidebar' => 'stats',
        ]);
    }

    #[Route('/{id}/delete', name: 'delete', methods: ['POST'])]
    public function delete(Request $request, FightStatistic $statistic, EntityManagerInterface $entityManager): Response
    {
        if ($this->isCsrfTokenValid('delete_statistic_' . $statistic->getId(), (string) $request->request->get('_token'))) {
            $entityManager->remove($statistic);
            $entityManager->flush();
            $this->addFlash('success', 'Fight statistic deleted.');
        }

        return $this->redirectToRoute('admin_statistic_index');
    }

    #[Route('/upsert/{fightResult}/{fighterId}', name: 'upsert', methods: ['POST'])]
    public function upsert(
        FightResult $fightResult,
        int $fighterId,
        Request $request,
        FightStatisticService $fightStatisticService,
    ): Response {
        $fightStatisticService->upsertForFighter($fightResult, $fighterId, [
            'strikesLanded' => $request->request->getInt('strikesLanded'),
            'strikesAttempted' => $request->request->getInt('strikesAttempted'),
            'takedownsLanded' => $request->request->getInt('takedownsLanded'),
            'takedownsAttempted' => $request->request->getInt('takedownsAttempted'),
            'submissionAttempts' => $request->request->getInt('submissionAttempts'),
            'knockdowns' => $request->request->getInt('knockdowns'),
            'controlTimeSeconds' => $request->request->getInt('controlTimeSeconds'),
        ]);

        $this->addFlash('success', 'Fight statistics saved.');

        return $this->redirectToRoute('admin_statistic_index', ['fightResultId' => $fightResult->getId()]);
    }

    private function fightResultChoices(FightResultRepository $fightResultRepository): array
    {
        $choices = [];
        foreach ($fightResultRepository->findBy([], ['fightDate' => 'DESC']) as $result) {
            $choices[sprintf(
                '#%d %s (%s)',
                $result->getId(),
                $result->getFightLabel(),
                $result->getFightDate()->format('Y-m-d')
            )] = (int) $result->getId();
        }

        return $choices;
    }

    private function fighterChoices(FighterRepository $fighterRepository): array
    {
        $choices = [];
        foreach ($fighterRepository->findBy([], ['id' => 'ASC']) as $fighter) {
            $choices[sprintf('#%d %s', $fighter->getId(), $fighter->getDisplayName())] = (int) $fighter->getId();
        }

        return $choices;
    }
}
