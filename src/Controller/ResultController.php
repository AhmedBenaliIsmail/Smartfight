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
        foreach ($events as $e) {
            $fights = $resultRepo->findByEvent($e->getEventId());
            $total = count($fights);
            $completed = 0;
            foreach ($fights as $f) {
                if ($f->getStatus() === 'COMPLETED') $completed++;
            }
            $eventStats[$e->getEventId()] = [
                'total' => $total,
                'completed' => $completed,
                'isFullyCompleted' => ($total > 0 && $total === $completed)
            ];
        }

        return $this->render('result/index.html.twig', [
            'events' => $events,
            'eventStats' => $eventStats,
            'q' => $request->query->get('q', ''),
            'title' => 'Fight Results'
        ]);
    }

    #[Route('/champions', name: 'app_results_champions')]
    public function championsIndex(Request $request, FightResultRepository $resultRepo, EventRepository $eventRepo): Response
    {
        $events = $eventRepo->findChampionsEvents();
        $eventStats = [];
        foreach ($events as $e) {
            $fights = $resultRepo->findByEvent($e->getEventId());
            $total = count($fights);
            $completed = 0;
            foreach ($fights as $f) {
                if ($f->getStatus() === 'COMPLETED') $completed++;
            }
            $eventStats[$e->getEventId()] = [
                'total' => $total,
                'completed' => $completed,
                'isFullyCompleted' => ($total > 0 && $total === $completed)
            ];
        }

        return $this->render('result/index.html.twig', [
            'events' => $events,
            'eventStats' => $eventStats,
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
    public function enter(int $id, Request $request, FightResultService $service, FightResultRepository $resultRepo, FighterRepository $fighterRepo, FightStatisticService $statService): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        $fr = $resultRepo->find($id);
        if (!$fr) throw $this->createNotFoundException();

        $fighter1 = $fighterRepo->find($fr->getFighter1Id());
        $fighter2 = $fighterRepo->find($fr->getFighter2Id());

        if ($request->isMethod('POST')) {
            $winnerChoice = $request->request->get('winner');
            $winnerId = null;
            if ($winnerChoice === 'fighter1') $winnerId = $fr->getFighter1Id();
            elseif ($winnerChoice === 'fighter2') $winnerId = $fr->getFighter2Id();

            $dateStr = $request->request->get('fightDate');
            $fightDate = $dateStr ? new \DateTime($dateStr) : new \DateTime();

            // 1. Save Statistics for Fighter 1
            $s1 = new FightStatistic();
            $s1->setFightResultId($id);
            $s1->setFighterId($fr->getFighter1Id());
            $s1->setStrikesThrown((int)$request->request->get('f1_strikes_thrown', 0));
            $s1->setStrikesLanded((int)$request->request->get('f1_strikes_landed', 0));
            $s1->setTakedownAttempts((int)$request->request->get('f1_td_attempts', 0));
            $s1->setTakedowns((int)$request->request->get('f1_td_landed', 0));
            $s1->setSubmissions((int)$request->request->get('f1_subs', 0));
            $s1->setKnockdowns((int)$request->request->get('f1_kds', 0));
            $s1->setControlTimeSeconds((int)$request->request->get('f1_control_time', 0));
            
            // 2. Save Statistics for Fighter 2
            $s2 = new FightStatistic();
            $s2->setFightResultId($id);
            $s2->setFighterId($fr->getFighter2Id());
            $s2->setStrikesThrown((int)$request->request->get('f2_strikes_thrown', 0));
            $s2->setStrikesLanded((int)$request->request->get('f2_strikes_landed', 0));
            $s2->setTakedownAttempts((int)$request->request->get('f2_td_attempts', 0));
            $s2->setTakedowns((int)$request->request->get('f2_td_landed', 0));
            $s2->setSubmissions((int)$request->request->get('f2_subs', 0));
            $s2->setKnockdowns((int)$request->request->get('f2_kds', 0));
            $s2->setControlTimeSeconds((int)$request->request->get('f2_control_time', 0));

            $statService->addFightStatistic($s1);
            $statService->addFightStatistic($s2);

            // 3. Complete the Result (This triggers ELO and ranking updates)
            $ok = $service->enterResult(
                $id, $winnerId,
                $request->request->get('method', 'DECISION'),
                (int)$request->request->get('round', 1),
                $fightDate
            );
            if ($ok) {
                $this->addFlash('success', sprintf(
                    'Result saved! Records for %s and %s have been updated along with ELO and rankings.',
                    $fighter1->getFullName(),
                    $fighter2->getFullName()
                ));
            } else {
                $this->addFlash('error', 'Could not save result.');
            }
            return $this->redirectToRoute('app_results');
        }

        return $this->render('result/enter.html.twig', [
            'result' => $fr,
            'fighter1' => $fighter1,
            'fighter2' => $fighter2,
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
                    $eventMap[$r->getEventId()] ?? '',
                    $r->getFightNumber(),
                    $fighterMap[$r->getFighter1Id()] ?? '',
                    $fighterMap[$r->getFighter2Id()] ?? '',
                    $r->getWinnerId() ? ($fighterMap[$r->getWinnerId()] ?? '') : '',
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
}
