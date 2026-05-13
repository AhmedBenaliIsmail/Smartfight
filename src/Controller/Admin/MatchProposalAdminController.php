<?php

namespace App\Controller\Admin;

use App\Entity\MatchProposal;
use App\Repository\FighterRepository;
use App\Repository\MatchProposalRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/admin/proposals', name: 'admin_proposal_')]
class MatchProposalAdminController extends AbstractController
{
    #[Route('', name: 'index')]
    public function index(MatchProposalRepository $proposalRepository): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        $proposals = $proposalRepository->findBy([], ['voteCount' => 'DESC']);

        return $this->render('admin/match_proposal/index.html.twig', [
            'proposals' => $proposals,
            'active_sidebar' => 'match_proposals',
        ]);
    }

    #[Route('/create', name: 'create', methods: ['GET', 'POST'])]
    public function create(
        Request $request,
        FighterRepository $fighterRepository,
        EntityManagerInterface $em
    ): Response {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        $weightClasses = ['Flyweight', 'Bantamweight', 'Featherweight', 'Lightweight', 'Welterweight', 'Middleweight', 'Light Heavyweight', 'Heavyweight'];
        $selectedWeightClass = $request->query->get('weight_class');

        $fighters = [];
        if ($selectedWeightClass) {
            $fighters = $fighterRepository->findBy(['weightClass' => $selectedWeightClass], ['eloRating' => 'DESC']);
        }

        if ($request->isMethod('POST')) {
            $fighter1Id = $request->request->getInt('fighter1');
            $fighter2Id = $request->request->getInt('fighter2');
            
            if ($fighter1Id === $fighter2Id) {
                $this->addFlash('error', 'Fighters must be different.');
            } else {
                $f1 = $fighterRepository->find($fighter1Id);
                $f2 = $fighterRepository->find($fighter2Id);

                if ($f1 && $f2) {
                    $proposal = new MatchProposal();
                    $proposal->setFighter1($f1);
                    $proposal->setFighter2($f2);
                    $proposal->setProposedAt(new \DateTime());
                    $proposal->setStatus('PENDING');

                    // Calculate Compatibility (Simple ELO difference percentage)
                    $eloDiff = abs($f1->getEloRating() - $f2->getEloRating());
                    $compat = max(0, 100 - ($eloDiff / 10)); // 100 ELO diff = 90% compat
                    $proposal->setCompatibility(number_format($compat, 2));

                    $em->persist($proposal);
                    $em->flush();

                    $this->addFlash('success', 'Match proposal created. Fans can now vote on it!');
                    return $this->redirectToRoute('admin_proposal_index');
                }
            }
        }

        return $this->render('admin/match_proposal/create.html.twig', [
            'weightClasses' => $weightClasses,
            'selectedWeightClass' => $selectedWeightClass,
            'fighters' => $fighters,
            'active_sidebar' => 'match_proposals',
        ]);
    }

    #[Route('/{id}/approve', name: 'approve', methods: ['POST'])]
    public function approve(MatchProposal $proposal, EntityManagerInterface $em): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        $proposal->setStatus('APPROVED');
        $em->flush();

        $this->addFlash('success', 'Proposal approved. It is now marked for scheduling.');
        return $this->redirectToRoute('admin_proposal_index');
    }

    #[Route('/{id}/reject', name: 'reject', methods: ['POST'])]
    public function reject(MatchProposal $proposal, EntityManagerInterface $em): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        $proposal->setStatus('REJECTED');
        $em->flush();

        $this->addFlash('success', 'Proposal rejected.');
        return $this->redirectToRoute('admin_proposal_index');
    }
}
