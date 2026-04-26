<?php
namespace App\Controller;

use App\Entity\Fighter;
use App\Entity\WeightDivision;
use App\Repository\FighterRepository;
use App\Repository\WeightDivisionRepository;
use App\Service\RankingService;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/fighters')]
class FighterController extends AbstractController
{
    #[Route('', name: 'app_fighters')]
    public function index(Request $request, FighterRepository $repo): Response
    {
        $q = $request->query->get('q', '');
        $fighters = $q ? $repo->search($q) : $repo->findAllOrderedByName();
        return $this->render('fighter/index.html.twig', [
            'fighters' => $fighters,
            'q' => $q,
        ]);
    }

    #[Route('/new', name: 'app_fighter_new', methods: ['GET', 'POST'])]
    public function new(Request $request, EntityManagerInterface $em, WeightDivisionRepository $wdRepo): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        if ($request->isMethod('POST')) {
            $f = new Fighter();
            $this->bindFighter($f, $request, $em);
            $em->persist($f);
            $em->flush();
            $this->addFlash('success', 'Fighter added.');
            return $this->redirectToRoute('app_fighters');
        }
        return $this->render('fighter/form.html.twig', [
            'fighter' => null,
            'weightDivisions' => $wdRepo->findAll()
        ]);
    }

    #[Route('/{id}/edit', name: 'app_fighter_edit', methods: ['GET', 'POST'])]
    public function edit(int $id, Request $request, FighterRepository $repo, EntityManagerInterface $em, WeightDivisionRepository $wdRepo): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        $f = $repo->find($id);
        if (!$f) throw $this->createNotFoundException();
        if ($request->isMethod('POST')) {
            $this->bindFighter($f, $request, $em);
            $em->flush();
            $this->addFlash('success', 'Fighter updated.');
            return $this->redirectToRoute('app_fighters');
        }
        return $this->render('fighter/form.html.twig', [
            'fighter' => $f,
            'weightDivisions' => $wdRepo->findAll()
        ]);
    }

    #[Route('/{id}/delete', name: 'app_fighter_delete', methods: ['POST'])]
    public function delete(int $id, FighterRepository $repo, EntityManagerInterface $em): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        $f = $repo->find($id);
        if ($f) { $em->remove($f); $em->flush(); $this->addFlash('success', 'Fighter deleted.'); }
        return $this->redirectToRoute('app_fighters');
    }

    #[Route('/recalc-rankings', name: 'app_fighter_recalc', methods: ['POST'])]
    public function recalcRankings(RankingService $rankingService): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        $rankingService->recomputeAllRankings();
        $this->addFlash('success', 'Rankings recalculated from all fight data.');
        return $this->redirectToRoute('app_fighters');
    }

    #[Route('/{id}/generate-ai-profile', name: 'app_fighter_generate_ai_profile', methods: ['POST'])]
    public function generateAiProfile(int $id, FighterRepository $repo, \App\Service\AIService $aiService, EntityManagerInterface $em): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        $f = $repo->find($id);
        if (!$f) {
            throw $this->createNotFoundException();
        }

        $fighterData = [
            'name' => $f->getFullName(),
            'wins' => $f->getWins(),
            'losses' => $f->getLosses(),
            'draws' => $f->getDraws(),
            'ko_wins' => $f->getKoWins(),
            'height' => $f->getHeight() ?? 0,
            'reach' => $f->getReach() ?? 0,
        ];

        $response = $aiService->generateFighterProfile($fighterData);

        if ($response['success'] && isset($response['profile'])) {
            $f->setAiStyleTag($response['profile']['aiStyleTag'] ?? null);
            $f->setAiDescription($response['profile']['aiDescription'] ?? null);
            $em->flush();
            $this->addFlash('success', 'AI Profile generated successfully.');
        } else {
            $this->addFlash('error', 'Failed to generate AI profile: ' . ($response['error'] ?? 'Unknown error'));
        }

        return $this->redirectToRoute('app_fighter_edit', ['id' => $f->getFighterId()]);
    }

    private function bindFighter(Fighter $f, Request $r, EntityManagerInterface $em): void
    {
        $f->setFirstName(trim($r->request->get('firstName', '')));
        $f->setLastName(trim($r->request->get('lastName', '')));
        $f->setNickname(trim($r->request->get('nickname', '')) ?: null);
        
        $wdId = (int)$r->request->get('weightDivision');
        if ($wdId) {
            $wd = $em->getRepository(WeightDivision::class)->find($wdId);
            $f->setWeightDivision($wd);
        }

        $f->setNationality(trim($r->request->get('nationality', '')) ?: null);
        $f->setWins((int)$r->request->get('wins', 0));
        $f->setLosses((int)$r->request->get('losses', 0));
        $f->setDraws((int)$r->request->get('draws', 0));
        $f->setKoWins((int)$r->request->get('koWins', 0));
        $f->setTechnicalWins((int)$r->request->get('technicalWins', 0));
        $f->setDecisionWins((int)$r->request->get('decisionWins', 0));
        $f->setHeight((int)$r->request->get('height', 0) ?: null);
        $f->setReach((int)$r->request->get('reach', 0) ?: null);

        // Photo Upload Handling
        $photoFile = $r->files->get('photo');
        if ($photoFile) {
            $destination = $this->getParameter('kernel.project_dir') . '/public/uploads/boxers';
            $newFilename = uniqid() . '.' . $photoFile->guessExtension();
            $photoFile->move($destination, $newFilename);
            $f->setPhotoFilename($newFilename);
        }
    }

}

