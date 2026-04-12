<?php
namespace App\Controller;

use App\Repository\CombattantRepository;
use App\Service\MatchmakingService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/combat', name: 'app_combat_')]
class CombatController extends AbstractController
{
    public function __construct(
        private MatchmakingService $matchmaking,
        private CombattantRepository $combattantRepo
    ) {}

    #[Route('', name: 'index', methods: ['GET', 'POST'])]
    public function index(Request $request): Response
    {
        $matches = [];
        $bestOpponent = null;
        $selectedCombattant = null;
        $max = 10;

        // Générer les matchs
        if ($request->isMethod('POST')) {
            $max = $request->request->getInt('max', 10);
            $matches = $this->matchmaking->generateMatches($max);

            // Trouver meilleur adversaire
            $combattantId = $request->request->get('combattant_id');
            if ($combattantId) {
                $selectedCombattant = $this->combattantRepo->find($combattantId);
                if ($selectedCombattant) {
                    $bestOpponent = $this->matchmaking->findBestOpponent($selectedCombattant);
                }
            }
        }

        return $this->render('combat/index.html.twig', [
            'combattants'       => $this->combattantRepo->findAll(),
            'matches'           => $matches,
            'bestOpponent'      => $bestOpponent,
            'selectedCombattant'=> $selectedCombattant,
            'max'               => $max,
        ]);
    }

    #[Route('/find-opponent', name: 'find_opponent', methods: ['POST'])]
    public function findOpponent(Request $request): JsonResponse
    {
        $id = $request->request->get('combattant_id');
        $combattant = $this->combattantRepo->find($id);

        if (!$combattant) {
            return $this->json(['error' => 'Combattant non trouvé'], 404);
        }

        $best = $this->matchmaking->findBestOpponent($combattant);

        if (!$best) {
            return $this->json(['error' => 'Aucun adversaire trouvé']);
        }

        return $this->json([
            'nickname' => $best['combattant']->getNickname(),
            'record'   => $best['combattant']->getWins().'-'.$best['combattant']->getLosses().'-'.$best['combattant']->getDraws(),
            'score'    => $best['score'],
            'qualite'  => $best['qualite'],
            'analyse'  => $best['analyse'],
        ]);
    }
}