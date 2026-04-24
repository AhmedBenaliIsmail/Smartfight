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
    public function index(Request $request, FightStatisticRepository $statRepo, FighterRepository $fighterRepo, FightResultRepository $resultRepo, EventRepository $eventRepo, \App\Service\BoutAnalysisService $analysisService): Response
    {
        $this->denyAccessUnlessGranted('ROLE_USER');
        
        $allStats = $statRepo->findAll();
        
        // Group stats by fight result
        $groupedStats = [];
        foreach ($allStats as $s) {
            $fight = $s->getFightResult();
            if (!$fight) continue;
            
            $rid = $fight->getResultId();
            if (!isset($groupedStats[$rid])) {
                $groupedStats[$rid] = [
                    'result' => $fight,
                    'event' => $fight->getEvent(),
                    'stats' => []
                ];
            }
            $groupedStats[$rid]['stats'][] = $s;
        }

        // Generate AI analysis for each group
        foreach ($groupedStats as $rid => &$group) {
            $group['analysis'] = $analysisService->analyzeBout($group['result'], $group['stats']);
        }

        // Apply search if needed
        $q = strtolower(trim($request->query->get('q', '')));
        if ($q) {
            $groupedStats = array_filter($groupedStats, function($group) use ($q) {
                if ($group['event'] && str_contains(strtolower($group['event']->getEventName()), $q)) return true;
                foreach ($group['stats'] as $s) {
                    if ($s->getFighter() && str_contains(strtolower($s->getFighter()->getFullName()), $q)) return true;
                }
                return false;
            });
        }

        return $this->render('statistic/index.html.twig', [
            'groupedStats' => $groupedStats,
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
                'fights' => $resultRepo->findCompletedFightsByEvent($eventId)
            ]);
        }

        // Phase 3: CompuBox Statistics Entry
        $fight = $resultRepo->find($fightId);
        if (!$fight) throw $this->createNotFoundException();

        $boxer1 = $fight->getFighter1();
        $boxer2 = $fight->getFighter2();
        
        $stat1 = $statRepo->findByFighterAndFightResult($boxer1->getFighterId(), $fight->getResultId()) ?: new FightStatistic();
        $stat2 = $statRepo->findByFighterAndFightResult($boxer2->getFighterId(), $fight->getResultId()) ?: new FightStatistic();

        if ($request->isMethod('POST')) {
            try {
                $this->bindPairedStats($stat1, $stat2, $fight, $request);
                
                $em->persist($stat1);
                $em->persist($stat2);
                $em->flush();

                // Trigger AI recalculations
                $service->recalculateForFighter($boxer1->getFighterId());
                $service->recalculateForFighter($boxer2->getFighterId());

                $this->addFlash('success', 'CompuBox statistics saved and AI performance scores updated.');
                return $this->redirectToRoute('app_stats');
            } catch (\Exception $e) {
                $this->addFlash('error', $e->getMessage());
            }
        }

        return $this->render('statistic/form.html.twig', [
            'fight' => $fight,
            'event' => $fight->getEvent(),
            'stat1' => $stat1,
            'stat2' => $stat2,
            'fighter1' => $boxer1,
            'fighter2' => $boxer2,
        ]);
    }

    #[Route('/{id}/edit', name: 'app_stat_edit')]
    public function edit(int $id, FightStatisticRepository $repo): Response
    {
        $stat = $repo->find($id);
        if (!$stat || !$stat->getFightResult()) throw $this->createNotFoundException();
        return $this->redirectToRoute('app_stat_new', ['fightId' => $stat->getFightResult()->getResultId()]);
    }

    #[Route('/{id}/delete', name: 'app_stat_delete', methods: ['POST'])]
    public function delete(int $id, FightStatisticRepository $repo, EntityManagerInterface $em, FightStatisticService $service): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        $stat = $repo->find($id);
        if ($stat) {
            $fid = $stat->getFighter() ? $stat->getFighter()->getFighterId() : null;
            $em->remove($stat);
            $em->flush();
            if ($fid) $service->recalculateForFighter($fid);
            $this->addFlash('success', 'Statistic record removed.');
        }
        return $this->redirectToRoute('app_stats');
    }

    #[Route('/delete-fight/{fightId}', name: 'app_stat_delete_fight', methods: ['POST'])]
    public function deleteByFight(int $fightId, FightStatisticRepository $repo, EntityManagerInterface $em, FightStatisticService $service): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        $stats = $repo->findByFightResult($fightId);
        foreach ($stats as $s) {
            $fid = $s->getFighter() ? $s->getFighter()->getFighterId() : null;
            $em->remove($s);
            if ($fid) $service->recalculateForFighter($fid);
        }
        $em->flush();
        $this->addFlash('success', 'All CompuBox records for this bout removed.');
        return $this->redirectToRoute('app_stats');
    }

    private function bindPairedStats(FightStatistic $s1, FightStatistic $s2, \App\Entity\FightResult $fight, Request $r): void
    {
        $s1->setFightResult($fight);
        $s1->setFighter($fight->getFighter1());
        $s1->setPunchesThrown((int)$r->request->get('s1_punchesThrown', 0));
        $s1->setPunchesLanded((int)$r->request->get('s1_punchesLanded', 0));
        $s1->setPowerPunchesThrown((int)$r->request->get('s1_powerPunchesThrown', 0));
        $s1->setPowerPunchesLanded((int)$r->request->get('s1_powerPunchesLanded', 0));
        $s1->setJabsThrown((int)$r->request->get('s1_jabsThrown', 0));
        $s1->setJabsLanded((int)$r->request->get('s1_jabsLanded', 0));
        $s1->setBodyShotsLanded((int)$r->request->get('s1_bodyShotsLanded', 0));
        $s1->setKnockdowns((int)$r->request->get('s1_knockdowns', 0));

        $s2->setFightResult($fight);
        $s2->setFighter($fight->getFighter2());
        $s2->setPunchesThrown((int)$r->request->get('s2_punchesThrown', 0));
        $s2->setPunchesLanded((int)$r->request->get('s2_punchesLanded', 0));
        $s2->setPowerPunchesThrown((int)$r->request->get('s2_powerPunchesThrown', 0));
        $s2->setPowerPunchesLanded((int)$r->request->get('s2_powerPunchesLanded', 0));
        $s2->setJabsThrown((int)$r->request->get('s2_jabsThrown', 0));
        $s2->setJabsLanded((int)$r->request->get('s2_jabsLanded', 0));
        $s2->setBodyShotsLanded((int)$r->request->get('s2_bodyShotsLanded', 0));
        $s2->setKnockdowns((int)$r->request->get('s2_knockdowns', 0));

        // Rigid validation
        $validate = function(FightStatistic $s, string $label) {
            if ($s->getPunchesLanded() > $s->getPunchesThrown()) {
                throw new \RuntimeException("$label: Landed punches cannot exceed thrown punches.");
            }
            if ($s->getPowerPunchesLanded() > $s->getPowerPunchesThrown()) {
                throw new \RuntimeException("$label: Landed power punches cannot exceed thrown.");
            }
            if ($s->getJabsLanded() > $s->getJabsThrown()) {
                throw new \RuntimeException("$label: Landed jabs cannot exceed thrown.");
            }
            if ($s->getPunchesThrown() < 0) {
                throw new \RuntimeException("$label: Values cannot be negative.");
            }
        };

        $validate($s1, 'Boxer 1');
        $validate($s2, 'Boxer 2');
    }
}

