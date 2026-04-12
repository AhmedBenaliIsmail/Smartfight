<?php
namespace App\Controller;

use App\Entity\FightStatistic;
use App\Repository\FightStatisticRepository;
use App\Repository\FighterRepository;
use App\Repository\FightResultRepository;
use App\Repository\EventRepository;
use App\Service\FightStatisticService;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/stats')]
class FightStatisticController extends AbstractController
{
    #[Route('', name: 'app_stats')]
    public function index(Request $request, FightStatisticRepository $statRepo, FighterRepository $fighterRepo, FightResultRepository $resultRepo, EventRepository $eventRepo): Response
    {
        $this->denyAccessUnlessGranted('ROLE_USER');
        
        $allStats = $statRepo->findAll();
        $fighterMap = []; foreach ($fighterRepo->findAll() as $f) $fighterMap[$f->getFighterId()] = $f->getFullName();
        $eventMap = []; foreach ($eventRepo->findAll() as $e) $eventMap[$e->getEventId()] = $e;
        $resultMap = []; foreach ($resultRepo->findAll() as $r) $resultMap[$r->getResultId()] = $r;

        // Group stats by fight result
        $groupedStats = [];
        foreach ($allStats as $s) {
            $rid = $s->getFightResultId();
            if (!isset($groupedStats[$rid])) {
                $groupedStats[$rid] = [
                    'result' => $resultMap[$rid] ?? null,
                    'event' => $resultMap[$rid] ? ($eventMap[$resultMap[$rid]->getEventId()] ?? null) : null,
                    'stats' => []
                ];
            }
            $groupedStats[$rid]['stats'][] = $s;
        }

        // Apply search if needed
        $q = strtolower(trim($request->query->get('q', '')));
        if ($q) {
            $groupedStats = array_filter($groupedStats, function($group) use ($q, $fighterMap) {
                if ($group['event'] && str_contains(strtolower($group['event']->getEventName()), $q)) return true;
                foreach ($group['stats'] as $s) {
                    if (str_contains(strtolower($fighterMap[$s->getFighterId()] ?? ''), $q)) return true;
                }
                return false;
            });
        }

        return $this->render('statistic/index.html.twig', [
            'groupedStats' => $groupedStats,
            'fighterMap' => $fighterMap,
            'q' => $q,
        ]);
    }

    #[Route('/new', name: 'app_stat_new', methods: ['GET', 'POST'])]
    public function new(Request $request, FightStatisticService $service, FighterRepository $fighterRepo, FightResultRepository $resultRepo, EventRepository $eventRepo, FightStatisticRepository $statRepo, EntityManagerInterface $em): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        $eventId = $request->query->get('eventId');
        $fightId = $request->query->get('fightId');

        // Phase 1: Event Selection
        if (!$eventId && !$fightId) {
            return $this->render('statistic/form_select_event.html.twig', [
                'events' => $eventRepo->findFinishedEvents()
            ]);
        }

        // Phase 2: Fight Selection
        if ($eventId && !$fightId) {
            $event = $eventRepo->find($eventId);
            if (!$event) throw $this->createNotFoundException();
            return $this->render('statistic/form_select_fight.html.twig', [
                'event' => $event,
                'fights' => $resultRepo->findCompletedFightsByEvent($eventId),
                'fighterMap' => $this->getFighterMap($fighterRepo)
            ]);
        }

        // Phase 3: Bulk Statistics Entry
        $fight = $resultRepo->find($fightId);
        if (!$fight) throw $this->createNotFoundException();

        $fighter1Id = $fight->getFighter1Id();
        $fighter2Id = $fight->getFighter2Id();
        
        $stat1 = $statRepo->findByFighterAndFightResult($fighter1Id, $fightId) ?: new FightStatistic();
        $stat2 = $statRepo->findByFighterAndFightResult($fighter2Id, $fightId) ?: new FightStatistic();

        if ($request->isMethod('POST')) {
            try {
                $this->bindPairedStats($stat1, $stat2, $fight, $request);
                
                $em->persist($stat1);
                $em->persist($stat2);
                $em->flush();

                // Trigger recalculations
                $service->recalculateForFighter($fighter1Id);
                $service->recalculateForFighter($fighter2Id);

                $this->addFlash('success', 'Statistics saved and performance scores updated for both fighters.');
                return $this->redirectToRoute('app_stats');
            } catch (\Exception $e) {
                $this->addFlash('error', $e->getMessage());
            }
        }

        $event = $eventRepo->find($fight->getEventId());
        if (!$event) {
            $this->addFlash('error', 'The event for this fight could not be found.');
            return $this->redirectToRoute('app_stats');
        }

        return $this->render('statistic/form.html.twig', [
            'fight' => $fight,
            'event' => $event,
            'stat1' => $stat1,
            'stat2' => $stat2,
            'fighter1' => $fighterRepo->find($fighter1Id),
            'fighter2' => $fighterRepo->find($fighter2Id),
        ]);
    }

    #[Route('/{id}/edit', name: 'app_stat_edit')]
    public function edit(int $id, FightStatisticRepository $repo): Response
    {
        // We redirect to the new dual-entry mode using the fight ID
        $stat = $repo->find($id);
        if (!$stat) throw $this->createNotFoundException();
        return $this->redirectToRoute('app_stat_new', ['fightId' => $stat->getFightResultId()]);
    }

    #[Route('/{id}/delete', name: 'app_stat_delete', methods: ['POST'])]
    public function delete(int $id, FightStatisticRepository $repo, EntityManagerInterface $em, FightStatisticService $service): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        $stat = $repo->find($id);
        if ($stat) {
            $fid = $stat->getFighterId();
            $em->remove($stat);
            $em->flush();
            $service->recalculateForFighter($fid);
            $this->addFlash('success', 'Statistic removed.');
        }
        return $this->redirectToRoute('app_stats');
    }

    #[Route('/delete-fight/{fightId}', name: 'app_stat_delete_fight', methods: ['POST'])]
    public function deleteByFight(int $fightId, FightStatisticRepository $repo, EntityManagerInterface $em, FightStatisticService $service): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        $stats = $repo->findByFightResult($fightId);
        foreach ($stats as $s) {
            $fid = $s->getFighterId();
            $em->remove($s);
            $service->recalculateForFighter($fid);
        }
        $em->flush();
        $this->addFlash('success', 'All statistics for this fight have been removed.');
        return $this->redirectToRoute('app_stats');
    }

    private function getFighterMap(FighterRepository $repo): array
    {
        $map = []; foreach ($repo->findAll() as $f) $map[$f->getFighterId()] = $f->getFullName();
        return $map;
    }

    private function bindPairedStats(FightStatistic $s1, FightStatistic $s2, \App\Entity\FightResult $fight, Request $r): void
    {
        $s1->setFightResultId($fight->getResultId());
        $s1->setFighterId($fight->getFighter1Id());
        $s1->setStrikesThrown((int)$r->request->get('s1_strikesThrown', 0));
        $s1->setStrikesLanded((int)$r->request->get('s1_strikesLanded', 0));
        $s1->setTakedownAttempts((int)$r->request->get('s1_takedownAttempts', 0));
        $s1->setTakedowns((int)$r->request->get('s1_takedowns', 0));
        $s1->setSubmissions((int)$r->request->get('s1_submissions', 0));
        $s1->setKnockdowns((int)$r->request->get('s1_knockdowns', 0));
        $s1->setControlTimeSeconds((int)$r->request->get('s1_controlTime', 0));

        $s2->setFightResultId($fight->getResultId());
        $s2->setFighterId($fight->getFighter2Id());
        $s2->setStrikesThrown((int)$r->request->get('s2_strikesThrown', 0));
        $s2->setStrikesLanded((int)$r->request->get('s2_strikesLanded', 0));
        $s2->setTakedownAttempts((int)$r->request->get('s2_takedownAttempts', 0));
        $s2->setTakedowns((int)$r->request->get('s2_takedowns', 0));
        $s2->setSubmissions((int)$r->request->get('s2_submissions', 0));
        $s2->setKnockdowns((int)$r->request->get('s2_knockdowns', 0));
        $s2->setControlTimeSeconds((int)$r->request->get('s2_controlTime', 0));

        // Rigid validation
        $validate = function(FightStatistic $s, string $label) {
            if ($s->getStrikesLanded() > $s->getStrikesThrown()) {
                throw new \RuntimeException("$label: Landed strikes cannot exceed thrown strikes.");
            }
            if ($s->getTakedowns() > $s->getTakedownAttempts()) {
                throw new \RuntimeException("$label: Successful takedowns cannot exceed attempts.");
            }
            if ($s->getStrikesThrown() < 0 || $s->getTakedownAttempts() < 0 || $s->getControlTimeSeconds() < 0) {
                throw new \RuntimeException("$label: Values cannot be negative.");
            }
        };

        $validate($s1, $fight->getFighter1Id() == $fight->getFighter1Id() ? 'Fighter 1' : 'Fighter 1');
        $validate($s2, 'Fighter 2');
    }
}
