<?php
namespace App\Controller;

use App\Repository\FighterRepository;
use App\Repository\EventRepository;
use App\Repository\FightResultRepository;
use App\Entity\FightStatistic;
use App\Service\FightResultService;
use App\Service\FightStatisticService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpFoundation\StreamedResponse;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/results')]
class ResultController extends AbstractController
{
    #[Route('', name: 'app_results')]
    public function index(Request $request, FightResultRepository $resultRepo, EventRepository $eventRepo): Response
    {
        $events = $eventRepo->findAllOrderedByDate();
        $eventStats = [];
        $eventFighters = [];

        foreach ($events as $e) {
            $fights = $resultRepo->findByEvent($e->getEventId());
            $total = count($fights);
            $completed = 0;
            $names = [];
            foreach ($fights as $f) {
                if ($f->getStatus() === 'COMPLETED') $completed++;
                if ($f->getFighter1()) $names[] = $f->getFighter1()->getFullName();
                if ($f->getFighter2()) $names[] = $f->getFighter2()->getFullName();
            }
            $eventStats[$e->getEventId()] = [
                'total' => $total,
                'completed' => $completed,
                'isFullyCompleted' => ($total > 0 && $total === $completed)
            ];
            $eventFighters[$e->getEventId()] = implode(', ', array_unique($names));
        }

        return $this->render('result/index.html.twig', [
            'events' => $events,
            'eventStats' => $eventStats,
            'eventFighters' => $eventFighters,
            'q' => $request->query->get('q', ''),
            'title' => 'Fight Results'
        ]);
    }

    #[Route('/champions', name: 'app_results_champions')]
    public function championsIndex(Request $request, FightResultRepository $resultRepo, EventRepository $eventRepo): Response
    {
        $events = $eventRepo->findChampionsEvents();
        $eventStats = [];
        $eventFighters = [];

        foreach ($events as $e) {
            $fights = $resultRepo->findByEvent($e->getEventId());
            $total = count($fights);
            $completed = 0;
            $names = [];
            foreach ($fights as $f) {
                if ($f->getStatus() === 'COMPLETED') $completed++;
                if ($f->getFighter1()) $names[] = $f->getFighter1()->getFullName();
                if ($f->getFighter2()) $names[] = $f->getFighter2()->getFullName();
            }
            $eventStats[$e->getEventId()] = [
                'total' => $total,
                'completed' => $completed,
                'isFullyCompleted' => ($total > 0 && $total === $completed)
            ];
            $eventFighters[$e->getEventId()] = implode(', ', array_unique($names));
        }

        return $this->render('result/index.html.twig', [
            'events' => $events,
            'eventStats' => $eventStats,
            'eventFighters' => $eventFighters,
            'q' => $request->query->get('q', ''),
            'title' => 'Champions Results',
            'isChampionsOnly' => true
        ]);
    }

    #[Route('/schedule', name: 'app_result_schedule', methods: ['GET', 'POST'])]
    public function schedule(Request $request, FightResultService $service, EventRepository $eventRepo, FighterRepository $fighterRepo): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        if ($request->isMethod('POST')) {
            try {
                $service->addScheduledFight(
                    (int)$request->request->get('eventId'),
                    (int)$request->request->get('fightNumber', 1),
                    (int)$request->request->get('fighter1Id'),
                    (int)$request->request->get('fighter2Id')
                );
                $this->addFlash('success', 'Fight scheduled!');
                return $this->redirectToRoute('app_results');
            } catch (\Exception $e) {
                $this->addFlash('error', $e->getMessage());
            }
        }
        return $this->render('result/schedule.html.twig', [
            'events' => $eventRepo->findAllOrderedByDate(),
            'fighters' => $fighterRepo->findAllOrderedByName(),
        ]);
    }

    #[Route('/{id}/enter', name: 'app_result_enter', methods: ['GET', 'POST'])]
    public function enter(int $id, Request $request, FightResultService $service, FightResultRepository $resultRepo, FighterRepository $fighterRepo): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        $fr = $resultRepo->find($id);
        if (!$fr) throw $this->createNotFoundException();

        $fighter1 = $fr->getFighter1();
        $fighter2 = $fr->getFighter2();

        if ($request->isMethod('POST')) {
            $winnerChoice = $request->request->get('winner');
            $winnerId = null;
            if ($winnerChoice === 'fighter1') $winnerId = $fighter1->getFighterId();
            elseif ($winnerChoice === 'fighter2') $winnerId = $fighter2->getFighterId();

            $dateStr = $request->request->get('fightDate');
            $fightDate = $dateStr ? new \DateTime($dateStr) : new \DateTime();

            $decisionType = $request->request->get('decisionType');
            if ($request->request->get('method', 'DECISION') !== 'DECISION') {
                $decisionType = null;
            }

            $ok = $service->enterResult(
                $id, $winnerId,
                $request->request->get('method', 'DECISION'),
                (int)$request->request->get('round', 1),
                $decisionType,
                null,
                $fightDate,
                $request->request->get('highlightVideoUrl'),
                $request->files->get('videoFile')
            );

            if ($ok) {
                $this->addFlash('success', 'Result finalized! You can now add detailed stats.');
                return $this->redirectToRoute('app_result_manage_card', ['id' => $fr->getEvent()->getEventId()]);
            }

            $this->addFlash('error', 'Could not save result.');
        }

        return $this->render('result/enter.html.twig', [
            'result' => $fr,
            'fighter1' => $fighter1,
            'fighter2' => $fighter2,
        ]);
    }

    #[Route('/{id}/stats', name: 'app_result_stats', methods: ['GET', 'POST'])]
    public function stats(int $id, Request $request, FightResultRepository $resultRepo, FightStatisticService $statService): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        $fr = $resultRepo->find($id);
        if (!$fr) throw $this->createNotFoundException();

        if ($request->isMethod('POST')) {
            $statsData = $request->request->all('stats');
            $fr->setInsideTheNumbers($request->request->get('insideTheNumbers'));
            
            // Clear existing stats for this fight to avoid duplicates
            $statService->clearStatsForFight($fr);
            $endRound = $fr->getRoundNumber() ?: 12;

            foreach ($statsData as $roundNum => $data) {
                if ($roundNum > $endRound) continue; // Safety check: No stats after the fight ended

                // Fighter 1 Round Stat
                $s1 = new FightStatistic();
                $s1->setFightResult($fr);
                $s1->setFighter($fr->getFighter1());
                $s1->setRound((int)$roundNum);
                $s1->setPunchesThrown((int)($data['f1']['thrown'] ?? 0));
                $s1->setPunchesLanded((int)($data['f1']['landed'] ?? 0));
                $s1->setRightHandThrown((int)($data['f1']['right_thrown'] ?? 0));
                $s1->setRightHandLanded((int)($data['f1']['right_landed'] ?? 0));
                $s1->setLeftHandThrown((int)($data['f1']['left_thrown'] ?? 0));
                $s1->setLeftHandLanded((int)($data['f1']['left_landed'] ?? 0));
                $s1->setPowerPunchesThrown((int)($data['f1']['power_thrown'] ?? 0));
                $s1->setPowerPunchesLanded((int)($data['f1']['power_landed'] ?? 0));
                $s1->setJabsLanded((int)($data['f1']['jabs_landed'] ?? 0));
                $s1->setBodyShotsLanded((int)($data['f1']['body_shots'] ?? 0));
                $s1->setKnockdowns((int)($data['f1']['kds'] ?? 0));
                $statService->addFightStatistic($s1);

                // Fighter 2 Round Stat
                $s2 = new FightStatistic();
                $s2->setFightResult($fr);
                $s2->setFighter($fr->getFighter2());
                $s2->setRound((int)$roundNum);
                $s2->setPunchesThrown((int)($data['f2']['thrown'] ?? 0));
                $s2->setPunchesLanded((int)($data['f2']['landed'] ?? 0));
                $s2->setRightHandThrown((int)($data['f2']['right_thrown'] ?? 0));
                $s2->setRightHandLanded((int)($data['f2']['right_landed'] ?? 0));
                $s2->setLeftHandThrown((int)($data['f2']['left_thrown'] ?? 0));
                $s2->setLeftHandLanded((int)($data['f2']['left_landed'] ?? 0));
                $s2->setPowerPunchesThrown((int)($data['f2']['power_thrown'] ?? 0));
                $s2->setPowerPunchesLanded((int)($data['f2']['power_landed'] ?? 0));
                $s2->setJabsLanded((int)($data['f2']['jabs_landed'] ?? 0));
                $s2->setBodyShotsLanded((int)($data['f2']['body_shots'] ?? 0));
                $s2->setKnockdowns((int)($data['f2']['kds'] ?? 0));
                $statService->addFightStatistic($s2);
            }

            $this->addFlash('success', 'Fight statistics updated successfully.');
            return $this->redirectToRoute('app_result_manage_card', ['id' => $fr->getEvent()->getEventId()]);
        }

        return $this->render('result/stats.html.twig', [
            'result' => $fr,
            'fighter1' => $fr->getFighter1(),
            'fighter2' => $fr->getFighter2(),
        ]);
    }

    #[Route('/{id}/cancel', name: 'app_result_cancel', methods: ['POST'])]
    public function cancel(int $id, FightResultService $service): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        $service->cancelFight($id);
        $this->addFlash('success', 'Fight cancelled.');
        return $this->redirectToRoute('app_results');
    }

    #[Route('/{id}/delete', name: 'app_result_delete', methods: ['POST'])]
    public function delete(int $id, FightResultService $service): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        $service->deleteFightResult($id);
        $this->addFlash('success', 'Result deleted.');
        return $this->redirectToRoute('app_results');
    }

    #[Route('/{id}', name: 'app_result_show', methods: ['GET'])]
    public function show(int $id, FightResultRepository $resultRepo, \App\Repository\FightStatisticRepository $statRepo): Response
    {
        $this->denyAccessUnlessGranted('ROLE_USER');
        $fr = $resultRepo->find($id);
        if (!$fr) throw $this->createNotFoundException();

        $stats = $statRepo->findBy(['fightResult' => $fr]);
        
        // Group stats by fighter and round
        $fighterStats = [];
        foreach ($stats as $s) {
            $fid = $s->getFighter()->getFighterId();
            $fighterStats[$fid][$s->getRound()] = $s;
        }

        return $this->render('result/show.html.twig', [
            'result' => $fr,
            'fighterStats' => $fighterStats,
            'rounds' => $fr->getRoundNumber() ?: 12
        ]);
    }

    #[Route('/export', name: 'app_result_export')]
    public function export(FightResultRepository $resultRepo, FighterRepository $fighterRepo, EventRepository $eventRepo): StreamedResponse
    {
        $results = $resultRepo->findAllOrdered();
        $fighterMap = []; foreach ($fighterRepo->findAll() as $f) $fighterMap[$f->getFighterId()] = $f->getFullName();
        $eventMap = []; foreach ($eventRepo->findAll() as $e) $eventMap[$e->getEventId()] = $e->getEventName();

        $response = new StreamedResponse(function() use ($results, $fighterMap, $eventMap) {
            $out = fopen('php://output', 'w');
            fputcsv($out, ['ID','Event','Fight#','Fighter1','Fighter2','Winner','Method','Round','Date','Status']);
            foreach ($results as $r) {
                fputcsv($out, [
                    $r->getResultId(),
                    $r->getEvent()->getEventName() ?? '',
                    $r->getFightNumber(),
                    $r->getFighter1()->getFullName() ?? '',
                    $r->getFighter2()->getFullName() ?? '',
                    $r->getWinner() ? $r->getWinner()->getFullName() : '',
                    $r->getMethodOfVictory() ?? '',
                    $r->getRoundNumber() ?? '',
                    $r->getFightDate() ? $r->getFightDate()->format('d/m/Y H:i') : '',
                    $r->getStatus(),
                ]);
            }
            fclose($out);
        });
        $response->headers->set('Content-Type', 'text/csv');
        $response->headers->set('Content-Disposition', 'attachment; filename="fight_results.csv"');
        return $response;
    }

    #[Route('/{id}/manage-card', name: 'app_result_manage_card')]
    public function manageEventResults(int $id, EventRepository $eventRepo, FightResultRepository $resultRepo, FighterRepository $fighterRepo): Response
    {
        $this->denyAccessUnlessGranted('ROLE_USER');
        $event = $eventRepo->find($id);
        if (!$event) throw $this->createNotFoundException();
        
        $fights = $resultRepo->findByEvent($id);
        $fighterMap = [];
        foreach ($fighterRepo->findAll() as $f) {
            $fighterMap[$f->getFighterId()] = $f;
        }

        return $this->render('result/event_center.html.twig', [
            'event' => $event,
            'fights' => $fights,
            'fighterMap' => $fighterMap
        ]);
    }

    #[Route('/{id}/generate-ai-stats', name: 'app_result_generate_ai_stats', methods: ['POST'])]
    public function generateAIStats(int $id, FightResultRepository $resultRepo, \App\Service\AIService $aiService, Request $request): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        $fr = $resultRepo->find($id);
        if (!$fr) throw $this->createNotFoundException();

        $data = json_decode($request->getContent(), true);
        $totalRounds = $data['totalRounds'] ?? 12;

        $rounds = [];
        for ($i = 1; $i <= $totalRounds; $i++) {
            $generateStats = function() use ($i) {
                // Base numbers per round (smaller than total)
                $thrown = rand(40, 80);
                $landed = rand( (int)($thrown * 0.15), (int)($thrown * 0.40) );
                
                $jabsThrown = (int)($thrown * rand(30, 50) / 100);
                $powerThrown = $thrown - $jabsThrown;
                
                $jabsLanded = (int)($jabsThrown * rand(10, 25) / 100);
                $powerLanded = $landed - $jabsLanded;

                $leftThrown = (int)($jabsThrown * 0.8) + (int)($powerThrown * 0.4);
                $rightThrown = $thrown - $leftThrown;
                
                $leftLanded = (int)($jabsLanded * 0.8) + (int)($powerLanded * 0.4);
                $rightLanded = $landed - $leftLanded;

                return [
                    'round' => $i,
                    'punches_thrown' => $thrown,
                    'punches_landed' => $landed,
                    'jabs_thrown' => $jabsThrown,
                    'jabs_landed' => $jabsLanded,
                    'power_thrown' => $powerThrown,
                    'power_landed' => $powerLanded,
                    'left_thrown' => $leftThrown,
                    'left_landed' => $leftLanded,
                    'right_thrown' => $rightThrown,
                    'right_landed' => $rightLanded,
                    'body_shots' => (int)($landed * rand(10, 25) / 100),
                    'kds' => ($i > 5 && rand(0, 100) > 95) ? 1 : 0,
                ];
            };

            $rounds[$i] = [
                'f1' => $generateStats(),
                'f2' => $generateStats(),
            ];
        }

        // Generate AI Insight based on the generated stats
        $prompt = "Analyze these round-by-round boxing stats (Rounds 1-{$totalRounds}) and provide a 3-paragraph professional broadcast-style 'Inside the Numbers' breakdown. " .
                  "Fighter 1 ({$fr->getFighter1()->getFullName()}) vs Fighter 2 ({$fr->getFighter2()->getFullName()}). " .
                  "Summarize the tactical shifts and who dominated specific phases. Stats provided in round chunks.";
        
        $insideTheNumbers = "Data-driven analysis pending...";
        try {
            $aiResponse = $aiService->generateText($prompt);
            if ($aiResponse) $insideTheNumbers = $aiResponse;
        } catch (\Exception $e) {}

        return $this->json([
            'success' => true,
            'rounds' => $rounds,
            'insideTheNumbers' => trim($insideTheNumbers)
        ]);
    }
}
