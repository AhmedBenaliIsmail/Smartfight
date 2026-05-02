<?php
namespace App\Controller;

use App\Entity\MatchProposal;
use App\Repository\FighterRepository;
use App\Repository\FightResultRepository;
use App\Service\AIService;
use App\Service\MatchmakingService;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/api/ai')]
class AIController extends AbstractController
{
    #[Route('/stat-suggestions', name: 'api_ai_stat_suggestions', methods: ['POST'])]
    public function suggestStatistics(
        Request $request,
        AIService $aiService,
        FighterRepository $fighterRepo,
        FightResultRepository $resultRepo
    ): JsonResponse {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        $data = json_decode($request->getContent(), true);
        
        $fighter1Id = $data['fighter1_id'] ?? null;
        $fighter2Id = $data['fighter2_id'] ?? null;
        $round = $data['round'] ?? 1;

        if (!$fighter1Id || !$fighter2Id) {
            return $this->json(['error' => 'Missing fighter IDs'], Response::HTTP_BAD_REQUEST);
        }

        $fighter1 = $fighterRepo->find($fighter1Id);
        $fighter2 = $fighterRepo->find($fighter2Id);

        if (!$fighter1 || !$fighter2) {
            return $this->json(['error' => 'Fighter not found'], Response::HTTP_NOT_FOUND);
        }

        // Prepare fighter data
        $f1Fights = $resultRepo->findCompletedByFighter($fighter1Id, 5);
        $f2Fights = $resultRepo->findCompletedByFighter($fighter2Id, 5);

        $f1Data = [
            'name' => $fighter1->getFullName(),
            'style' => $fighter1->getCalculatedFightingStyle(),
            'win_rate' => $this->calculateWinRate($f1Fights, $fighter1Id),
            'recent_fights_summary' => $this->summarizeRecentFights($f1Fights, $fighter1Id),
            'strength' => $fighter1->getStrength() ?? 'Balanced',
            'weakness' => $fighter1->getWeakness() ?? 'Unknown'
        ];

        $f2Data = [
            'name' => $fighter2->getFullName(),
            'style' => $fighter2->getCalculatedFightingStyle(),
            'win_rate' => $this->calculateWinRate($f2Fights, $fighter2Id),
            'recent_fights_summary' => $this->summarizeRecentFights($f2Fights, $fighter2Id),
            'strength' => $fighter2->getStrength() ?? 'Balanced',
            'weakness' => $fighter2->getWeakness() ?? 'Unknown'
        ];

        $result = $aiService->suggestFightStats($f1Data, $f2Data, $round);

        return $this->json($result);
    }

    #[Route('/matchmaking-suggestions', name: 'api_ai_matchmaking', methods: ['POST'])]
    public function suggestMatches(
        Request $request,
        MatchmakingService $matchmakingService
    ): JsonResponse {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        $data  = json_decode($request->getContent(), true);
        $count = (int) ($data['count'] ?? 5);

        $result = $matchmakingService->suggestMatches($count);

        return $this->json(array_merge($result, ['count_returned' => count($result['matches'] ?? [])]));
    }

    #[Route('/generate-proposals', name: 'api_ai_generate_proposals', methods: ['POST'])]
    public function generateProposals(
        Request $request,
        MatchmakingService $matchmakingService,
        FighterRepository $fighterRepo,
        EntityManagerInterface $em
    ): JsonResponse {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        $data  = json_decode($request->getContent(), true);
        $count = (int) ($data['count'] ?? 5);

        $matches = $matchmakingService->generateProposalsLocal($count);

        if (empty($matches)) {
            return $this->json([
                'success' => false,
                'message' => 'No valid matchups found (insufficient fighters or all fought recently).',
                'count'   => 0,
            ], Response::HTTP_UNPROCESSABLE_ENTITY);
        }

        $created = 0;
        foreach ($matches as $m) {
            $f1 = $fighterRepo->find($m['fighter1_id']);
            $f2 = $fighterRepo->find($m['fighter2_id']);

            if (!$f1 || !$f2) {
                continue;
            }

            $proposal = new MatchProposal();
            $proposal->setFighter1($f1);
            $proposal->setFighter2($f2);
            $proposal->setCompatibility((string) number_format($m['score_ia'], 2, '.', ''));
            $proposal->setNotes($m['reason']);
            $proposal->setProposedAt(new \DateTime());
            $proposal->setStatus('PENDING');
            $proposal->setVoteCount(0);

            // Carry over weight division from the pairing when both fighters share one
            if ($f1->getWeightDivision()
                && $f2->getWeightDivision()
                && $f1->getWeightDivision()->getId() === $f2->getWeightDivision()->getId()) {
                $proposal->setWeightDivision($f1->getWeightDivision());
            }

            $em->persist($proposal);
            $created++;
        }

        $em->flush();

        return $this->json([
            'success' => true,
            'message' => "Generated {$created} proposals via 7-vector Score IA algorithm.",
            'count'   => $created,
            'matches' => $matches,
        ]);
    }

    private function calculateWinRate(array $fights, int $fighterId): float
    {
        if (empty($fights)) return 0.0;

        $wins = 0;
        foreach ($fights as $fight) {
            if ($fight->getWinner() && $fight->getWinner()->getFighterId() === $fighterId) {
                $wins++;
            }
        }

        return round(($wins / count($fights)) * 100, 1);
    }

    private function summarizeRecentFights(array $fights, int $fighterId): string
    {
        if (empty($fights)) return 'No recent fights';

        $results = [];
        foreach ($fights as $fight) {
            if ($fight->getWinner() && $fight->getWinner()->getFighterId() === $fighterId) {
                $results[] = 'W';
            } elseif ($fight->isDraw()) {
                $results[] = 'D';
            } else {
                $results[] = 'L';
            }
        }

        return implode('-', $results);
    }
}
