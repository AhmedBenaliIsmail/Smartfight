<?php

namespace App\Controller\Front;

use App\Entity\FanReaction;
use App\Repository\FanReactionRepository;
use App\Repository\FightResultRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/reactions', name: 'front_reaction_')]
class ReactionController extends AbstractController
{
    #[Route('', name: 'index', methods: ['GET', 'POST'])]
    public function index(
        Request $request,
        FanReactionRepository $reactionRepo,
        FightResultRepository $resultRepo,
        EntityManagerInterface $em
    ): Response {
        $recentFights = $resultRepo->findBy(['status' => 'COMPLETED'], ['fightDate' => 'DESC'], 10);
        $reactions = $reactionRepo->findBy(['isDeleted' => false], ['reactedAt' => 'DESC'], 50);

        if ($request->isMethod('POST')) {
            $this->denyAccessUnlessGranted('ROLE_USER');
            
            $fightResultId = $request->request->get('fight_result_id');
            $type = $request->request->get('reaction_type');
            $comment = $request->request->get('comment');

            $fightResult = $resultRepo->find($fightResultId);
            if ($fightResult && in_array($type, ['FIRE', 'SHOCK', 'RESPECT', 'DOMINANT', 'CONTROVERSIAL'])) {
                $reaction = new FanReaction();
                $reaction->setFan($this->getUser());
                $reaction->setFightResult($fightResult);
                $reaction->setReactionType($type);
                if ($comment) {
                    $reaction->setComment(substr($comment, 0, 140));
                }

                $em->persist($reaction);
                $em->flush();

                $this->addFlash('success', 'Your reaction has been posted!');
                return $this->redirectToRoute('front_reaction_index');
            } else {
                $this->addFlash('error', 'Invalid reaction data.');
            }
        }

        return $this->render('front/reaction/index.html.twig', [
            'fights' => $recentFights,
            'reactions' => $reactions,
        ]);
    }
}
