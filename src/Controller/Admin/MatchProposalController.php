<?php
namespace App\Controller\Admin;

use App\Entity\MatchProposal;
use App\Repository\MatchProposalRepository;
use App\Repository\FighterRepository;
use App\Repository\WeightDivisionRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/admin/match-proposals')]
class MatchProposalController extends AbstractController
{
    #[Route('', name: 'app_match_proposal_index')]
    public function index(MatchProposalRepository $repo, FighterRepository $fighterRepo, WeightDivisionRepository $wdRepo): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        return $this->render('admin/match_proposal/index.html.twig', [
            'proposals' => $repo->findBy([], ['voteCount' => 'DESC']),
            'fighters' => $fighterRepo->findAll(),
            'weightDivisions' => $wdRepo->findAll()
        ]);
    }

    #[Route('/new', name: 'app_match_proposal_new', methods: ['POST'])]
    public function new(Request $request, FighterRepository $fighterRepo, WeightDivisionRepository $wdRepo, EntityManagerInterface $em): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        
        $fighter1 = $fighterRepo->find($request->request->get('fighter1Id'));
        $fighter2 = $fighterRepo->find($request->request->get('fighter2Id'));
        $wd = $wdRepo->find($request->request->get('weightDivisionId'));

        if (!$fighter1 || !$fighter2 || !$wd) {
            $this->addFlash('error', 'Invalid fighter or division selection.');
            return $this->redirectToRoute('app_match_proposal_index');
        }

        if ($fighter1->getFighterId() === $fighter2->getFighterId()) {
            $this->addFlash('error', 'A fighter cannot fight themselves.');
            return $this->redirectToRoute('app_match_proposal_index');
        }

        $proposal = new MatchProposal();
        $proposal->setFighter1($fighter1);
        $proposal->setFighter2($fighter2);
        $proposal->setWeightDivision($wd);
        $proposal->setVoteCount(0);
        $proposal->setStatus('PENDING');
        $proposal->setProposedAt(new \DateTime());

        $em->persist($proposal);
        $em->flush();

        $this->addFlash('success', 'Matchup added to the fan voting pool!');
        return $this->redirectToRoute('app_match_proposal_index');
    }

    #[Route('/{id}/delete', name: 'app_match_proposal_delete', methods: ['POST'])]
    public function delete(int $id, MatchProposalRepository $repo, EntityManagerInterface $em): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        $proposal = $repo->find($id);
        if ($proposal) {
            $em->remove($proposal);
            $em->flush();
            $this->addFlash('success', 'Matchup removed from the pool.');
        }
        return $this->redirectToRoute('app_match_proposal_index');
    }
}
