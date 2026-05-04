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
use App\Repository\RankingRepository;
use App\Repository\FightStatisticRepository;

#[Route('/api/ai')]
class AIController extends AbstractController
{
    #[Route('/stat-suggestions', name: 'api_ai_stat_suggestions', methods: ['POST'])]
    public function suggestStatistics(
        Request $request,
        AIService $aiService,
        FighterRepository $fighterRepo,
        FightResultRepository $resultRepo,
        \App\Service\RoundCommentaryService $commentaryService
    ): JsonResponse {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        $data = json_decode($request->getContent(), true) ?? [];
        
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

        // SYNC: Use RoundCommentaryService for the text instead of AI text
        if (isset($result['suggestions'])) {
            $sug = $result['suggestions'];
            $sf1 = $sug['fighter1'] ?? [];
            $sf2 = $sug['fighter2'] ?? [];

            // Map suggestions to commentary service format
            $map = function($f) {
                return [
                    'landed' => $f['punches_landed'] ?? 0,
                    'thrown' => $f['punches_thrown'] ?? 0,
                    'kds' => $f['knockdowns'] ?? 0,
                    'power_landed' => $f['power_punches_landed'] ?? 0,
                    'power_thrown' => $f['power_punches_thrown'] ?? 0,
                    'body_shots' => $f['body_shots_landed'] ?? 0,
                    'jabs_landed' => $f['jabs_landed'] ?? 0,
                ];
            };

            $itnText = $commentaryService->generateForRound(
                $fighter1->getLastName(), $map($sf1),
                $fighter2->getLastName(), $map($sf2),
                $round
            );

            $result['suggestions']['inside_the_numbers_text'] = $itnText;
        }

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

    #[Route('/simulate', name: 'api_ai_simulate', methods: ['POST'])]
    public function simulate(
        Request $request,
        AIService $aiService,
        FighterRepository $fighterRepo,
        FightResultRepository $resultRepo,
        RankingRepository $rankingRepo
    ): JsonResponse {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        $data = json_decode($request->getContent(), true);
        $fighter1Id = $data['fighter1_id'] ?? null;
        $fighter2Id = $data['fighter2_id'] ?? null;

        if (!$fighter1Id || !$fighter2Id) {
            return $this->json(['error' => 'Missing fighter IDs'], Response::HTTP_BAD_REQUEST);
        }

        $fighter1 = $fighterRepo->find($fighter1Id);
        $fighter2 = $fighterRepo->find($fighter2Id);

        if (!$fighter1 || !$fighter2) {
            return $this->json(['error' => 'Fighter not found'], Response::HTTP_NOT_FOUND);
        }

        $f1Data = $this->buildFighterPayload($fighter1, $resultRepo, $rankingRepo);
        $f2Data = $this->buildFighterPayload($fighter2, $resultRepo, $rankingRepo);

        $result = $aiService->analyzeFightDynamics($f1Data, $f2Data);

        return $this->json($result);
    }

    #[Route('/scouting-report', name: 'api_ai_scouting_report', methods: ['POST'])]
    public function scoutingReport(
        Request $request,
        AIService $aiService,
        FighterRepository $fighterRepo,
        FightResultRepository $resultRepo,
        RankingRepository $rankingRepo
    ): JsonResponse {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        $data = json_decode($request->getContent(), true);
        $subjectId = $data['subject_id'] ?? null;
        $opponentId = $data['opponent_id'] ?? null;

        if (!$subjectId || !$opponentId) {
            return $this->json(['error' => 'Missing fighter IDs'], Response::HTTP_BAD_REQUEST);
        }

        $subject = $fighterRepo->find($subjectId);
        $opponent = $fighterRepo->find($opponentId);

        if (!$subject || !$opponent) {
            return $this->json(['error' => 'Fighter not found'], Response::HTTP_NOT_FOUND);
        }

        $subjectData = $this->buildFighterPayload($subject, $resultRepo, $rankingRepo);
        $opponentData = $this->buildFighterPayload($opponent, $resultRepo, $rankingRepo);

        $result = $aiService->generateScoutingReport($subjectData, $opponentData);

        return $this->json($result);
    }

    #[Route('/post-fight-recap', name: 'api_ai_post_fight_recap', methods: ['POST'])]
    public function postFightRecap(
        Request $request,
        AIService $aiService,
        FighterRepository $fighterRepo,
        FightResultRepository $resultRepo,
        RankingRepository $rankingRepo,
        FightStatisticRepository $statRepo
    ): JsonResponse {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        $data = json_decode($request->getContent(), true);
        $fightId = $data['fight_id'] ?? null;

        if (!$fightId) {
            return $this->json(['error' => 'Missing fight ID'], Response::HTTP_BAD_REQUEST);
        }

        $fight = $resultRepo->find($fightId);
        if (!$fight) {
            return $this->json(['error' => 'Fight not found'], Response::HTTP_NOT_FOUND);
        }

        $fighter1 = $fight->getFighter1();
        $fighter2 = $fight->getFighter2();

        $f1Data = $this->buildFighterPayload($fighter1, $resultRepo, $rankingRepo);
        $f2Data = $this->buildFighterPayload($fighter2, $resultRepo, $rankingRepo);

        $fightInfo = [
            'event_name' => $fight->getEvent()?->getName() ?? 'Unknown Event',
            'division' => $fight->getWeightDivision()?->getName() ?? 'Catchweight',
            'winner_name' => $fight->getWinner()?->getFullName() ?? 'Draw',
            'result_type' => $fight->getResultType() ?? 'Decision',
            'end_round' => $fight->getEndRound() ?? 3
        ];

        // Aggregate stats
        $stats = $statRepo->findBy(['fightResult' => $fight]);
        $aggregateStats = [];
        // Simple aggregation example
        $aggregateStats['total_rounds_analyzed'] = count($stats);

        $result = $aiService->generatePostFightRecap($fightInfo, $f1Data, $f2Data, $aggregateStats);

        return $this->json($result);
    }

    #[Route('/compare-result', name: 'api_ai_compare_result', methods: ['POST'])]
    public function compareResult(Request $request, FightResultRepository $resultRepo, RankingRepository $rankingRepo, AIService $aiService): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        try {
            $data = json_decode($request->getContent(), true) ?? [];
            $fightId = $data['fight_id'] ?? null;
            
            if (!$fightId) {
                return $this->json(['success' => false, 'error' => 'Missing fight_id'], 400);
            }

            $fight = $resultRepo->find($fightId);
            if (!$fight) {
                return $this->json(['success' => false, 'error' => 'Fight not found'], 404);
            }

            if (!$fight->getFighter1() || !$fight->getFighter2()) {
                return $this->json(['success' => false, 'error' => 'Fight data incomplete (missing fighters)'], 400);
            }

            $f1Data = $this->buildFighterPayload($fight->getFighter1(), $resultRepo, $rankingRepo);
            $f2Data = $this->buildFighterPayload($fight->getFighter2(), $resultRepo, $rankingRepo);

            $realResultData = [
                'winner' => $fight->getWinner() ? $fight->getWinner()->getFullName() : 'DRAW',
                'method' => $fight->getMethodOfVictory() ?? 'N/A',
                'round' => $fight->getRoundNumber() ?? 0
            ];

            $result = $aiService->comparePredictionVsReality($f1Data, $f2Data, $realResultData);
            
            return $this->json($result);
        } catch (\Exception $e) {
            return $this->json([
                'success' => false,
                'error' => 'An unexpected error occurred during analysis: ' . $e->getMessage()
            ], 500);
        }
    }

    #[Route('/predict-injury', name: 'api_ai_predict_injury', methods: ['POST'])]
    public function predictInjury(
        Request $request,
        AIService $aiService,
        FighterRepository $fighterRepo,
        FightResultRepository $resultRepo,
        RankingRepository $rankingRepo
    ): JsonResponse {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        $data = json_decode($request->getContent(), true);
        $fighterId = $data['fighter_id'] ?? null;

        if (!$fighterId) {
            return $this->json(['error' => 'Missing fighter ID'], Response::HTTP_BAD_REQUEST);
        }

        $fighter = $fighterRepo->find($fighterId);
        if (!$fighter) {
            return $this->json(['error' => 'Fighter not found'], Response::HTTP_NOT_FOUND);
        }

        $fighterData = $this->buildFighterPayload($fighter, $resultRepo, $rankingRepo);
        $fighterData['age'] = $fighter->getAge() ?? 28;
        $fighterData['total_fights'] = $fighter->getTotalFights();
        $fighterData['ko_losses'] = $fighter->getKoLosses();

        $result = $aiService->predictInjuryRisk($fighterData);

        return $this->json($result);
    }

    private function buildFighterPayload($fighter, FightResultRepository $resultRepo, RankingRepository $rankingRepo): array
    {
        if (!$fighter) return [];

        $fights = $resultRepo->findCompletedByFighter($fighter->getFighterId(), 5);
        $ranking = $rankingRepo->findOneBy(['fighter' => $fighter]);
        
        $totalFights = $fighter->getWins() + $fighter->getLosses() + $fighter->getDraws();
        $koRate = $fighter->getWins() > 0 ? round(($fighter->getKoWins() / $fighter->getWins()) * 100, 1) : 0;

        return [
            'name' => $fighter->getFullName(),
            'style' => $fighter->getCalculatedFightingStyle(),
            'height' => $fighter->getHeight() ?? 175,
            'reach' => $fighter->getReach() ?? 180,
            'weight' => $fighter->getWeight() ?? 155,
            'wins' => $fighter->getWins(),
            'losses' => $fighter->getLosses(),
            'ko_rate' => $koRate,
            'elo' => $fighter->getEloRating(),
            'form' => $this->summarizeRecentFights($fights, $fighter->getFighterId()),
            'isChampion' => $ranking ? $ranking->isChampion() : false
        ];
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
