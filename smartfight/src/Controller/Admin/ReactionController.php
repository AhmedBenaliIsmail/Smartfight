<?php

namespace App\Controller\Admin;

use App\Entity\FanReaction;
use App\Repository\FanReactionRepository;
use App\Repository\FightResultRepository;
use Doctrine\ORM\EntityManagerInterface;
use Knp\Component\Pager\PaginatorInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/admin/reactions', name: 'admin_reaction_')]
class ReactionController extends AbstractController
{
    #[Route('', name: 'index', methods: ['GET'])]
    public function index(
        Request $request,
        FanReactionRepository $repo,
        FightResultRepository $resultRepo,
        PaginatorInterface $paginator,
    ): Response {
        $qb = $repo->findFilteredForAdmin(
            $request->query->getInt('fight_result') ?: null,
            $request->query->get('type'),
            $request->query->get('status'),
        );

        $pagination = $paginator->paginate($qb, $request->query->getInt('page', 1), 12);

        return $this->render('admin/reaction/index.html.twig', [
            'pagination' => $pagination,
            'fightResults' => $resultRepo->findAll(),
            'filters' => $request->query->all(),
            'active_sidebar' => 'reactions',
        ]);
    }

    #[Route('/{id}/pin', name: 'pin', methods: ['POST'])]
    public function togglePin(FanReaction $reaction, EntityManagerInterface $em): Response
    {
        $reaction->setIsPinned(!$reaction->isPinned());
        $em->flush();

        $this->addFlash('success', $reaction->isPinned() ? 'Reaction pinned.' : 'Reaction unpinned.');
        return $this->redirectToRoute('admin_reaction_index');
    }

    #[Route('/{id}/delete', name: 'delete', methods: ['POST'])]
    public function delete(Request $request, FanReaction $reaction, EntityManagerInterface $em): Response
    {
        if ($this->isCsrfTokenValid('delete' . $reaction->getId(), $request->request->get('_token'))) {
            $reaction->setIsDeleted(true);
            $em->flush();
            $this->addFlash('success', 'Reaction deleted.');
        }

        return $this->redirectToRoute('admin_reaction_index');
    }
}
