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
    #[Route('/hub', name: 'app_stats_hub')]
    public function hub(): Response
    {
        return $this->render('statistic/hub.html.twig');
    }

    #[Route('', name: 'app_stats')]
    public function index(Request $request, FightStatisticRepository $statRepo, FighterRepository $fighterRepo, FightResultRepository $resultRepo, EventRepository $eventRepo, \App\Service\BoutAnalysisService $analysisService, EntityManagerInterface $em): Response
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

        // Generate AI analysis and Aggregate totals per fighter for each group
        foreach ($groupedStats as $rid => &$group) {
            $group['analysis'] = $analysisService->analyzeBout($group['result'], $group['stats']);
            
            $totals = [];
            foreach ($group['stats'] as $s) {
                $fid = $s->getFighter()->getFighterId();
                if (!isset($totals[$fid])) {
                    // Use $group['result'] not stale outer $fight variable
                    $contract = $em->getRepository(\App\Entity\FighterContract::class)->findOneBy([
                        'fighter' => $s->getFighter(),
                        'event'   => $group['result']->getEvent()
                    ]);
                    
                    $totals[$fid] = [
                        'fighter' => $s->getFighter(),
                        'punchesLanded' => 0, 'punchesThrown' => 0,
                        'jabsLanded' => 0, 'jabsThrown' => 0,
                        'powerPunchesLanded' => 0, 'powerPunchesThrown' => 0,
                        'bodyShotsLanded' => 0,
                        'payout' => $contract ? $contract->getCalculatedPayout() : 0
                    ];
                }
                $totals[$fid]['punchesLanded'] += $s->getPunchesLanded();
                $totals[$fid]['punchesThrown'] += $s->getPunchesThrown();
                $totals[$fid]['jabsLanded'] += $s->getJabsLanded();
                $totals[$fid]['jabsThrown'] += $s->getJabsThrown();
                $totals[$fid]['powerPunchesLanded'] += $s->getPowerPunchesLanded();
                $totals[$fid]['powerPunchesThrown'] += $s->getPowerPunchesThrown();
                $totals[$fid]['bodyShotsLanded'] += $s->getBodyShotsLanded();
            }
            
            // Calculate accuracies for aggregated totals
            foreach ($totals as &$t) {
                $t['punchAccuracy'] = $t['punchesThrown'] > 0 ? ($t['punchesLanded'] / $t['punchesThrown'] * 100) : 0;
                $t['jabAccuracy'] = $t['jabsThrown'] > 0 ? ($t['jabsLanded'] / $t['jabsThrown'] * 100) : 0;
                $t['powerAccuracy'] = $t['powerPunchesThrown'] > 0 ? ($t['powerPunchesLanded'] / $t['powerPunchesThrown'] * 100) : 0;
            }
            
            $group['totalStats'] = array_values($totals);
            $group['totalPayout'] = array_sum(array_column($group['totalStats'], 'payout'));
        }

        // Apply search if needed
        $q = strtolower(trim($request->query->get('q', '')));
        if ($q) {
            $groupedStats = array_filter($groupedStats, function($group) use ($q) {
                if ($group['event'] && str_contains(strtolower($group['event']->getEventName()), $q)) return true;
                foreach ($group['totalStats'] as $t) {
                    if (str_contains(strtolower($t['fighter']->getFullName()), $q)) return true;
                }
                return false;
            });
        }

        return $this->render('statistic/index.html.twig', [
            'groupedStats' => $groupedStats,
            'q' => $q,
        ]);
    }

    #[Route('/select', name: 'app_stat_selection')]
    public function selection(Request $request, EventRepository $eventRepo, FightResultRepository $resultRepo): Response
    {
        $this->denyAccessUnlessGranted('ROLE_USER');
        
        $eventId = $request->query->get('eventId');
        
        if (!$eventId) {
            return $this->render('statistic/form_select_event.html.twig', [
                'events' => $eventRepo->findFinishedEvents()
            ]);
        }

        $event = $eventRepo->find($eventId);
        if (!$event) throw $this->createNotFoundException();
        
        return $this->render('statistic/form_select_fight.html.twig', [
            'event' => $event,
            'fights' => $resultRepo->findCompletedFightsByEvent($eventId),
            'targetRoute' => $this->isGranted('ROLE_ADMIN') ? 'app_stat_new' : 'app_stat_show'
        ]);
    }

    #[Route('/show/{fightId}', name: 'app_stat_show')]
    public function show(int $fightId, FightStatisticRepository $statRepo, FightResultRepository $resultRepo, \App\Service\BoutAnalysisService $analysisService, EntityManagerInterface $em): Response
    {
        $this->denyAccessUnlessGranted('ROLE_USER');
        
        $fight = $resultRepo->find($fightId);
        if (!$fight) throw $this->createNotFoundException();
        
        $stats = $statRepo->findByFightResult($fightId);
        
        // Similar aggregation logic as index...
        $analysis = $analysisService->analyzeBout($fight, $stats);
        
        $totals = [];
        foreach ($stats as $s) {
            $fid = $s->getFighter()->getFighterId();
            if (!isset($totals[$fid])) {
                $contract = $em->getRepository(\App\Entity\FighterContract::class)->findOneBy([
                    'fighter' => $s->getFighter(),
                    'event' => $fight->getEvent()
                ]);
                $totals[$fid] = [
                    'fighter' => $s->getFighter(),
                    'punchesLanded' => 0, 'punchesThrown' => 0,
                    'jabsLanded' => 0, 'jabsThrown' => 0,
                    'powerPunchesLanded' => 0, 'powerPunchesThrown' => 0,
                    'bodyShotsLanded' => 0,
                    'payout' => $contract ? $contract->getCalculatedPayout() : 0
                ];
            }
            $totals[$fid]['punchesLanded'] += $s->getPunchesLanded();
            $totals[$fid]['punchesThrown'] += $s->getPunchesThrown();
            $totals[$fid]['jabsLanded'] += $s->getJabsLanded();
            $totals[$fid]['jabsThrown'] += $s->getJabsThrown();
            $totals[$fid]['powerPunchesLanded'] += $s->getPowerPunchesLanded();
            $totals[$fid]['powerPunchesThrown'] += $s->getPowerPunchesThrown();
            $totals[$fid]['bodyShotsLanded'] += $s->getBodyShotsLanded();
        }
        
        foreach ($totals as &$t) {
            $t['punchAccuracy'] = $t['punchesThrown'] > 0 ? ($t['punchesLanded'] / $t['punchesThrown'] * 100) : 0;
            $t['jabAccuracy'] = $t['jabsThrown'] > 0 ? ($t['jabsLanded'] / $t['jabsThrown'] * 100) : 0;
            $t['powerAccuracy'] = $t['powerPunchesThrown'] > 0 ? ($t['powerPunchesLanded'] / $t['powerPunchesThrown'] * 100) : 0;
        }

        return $this->render('statistic/show.html.twig', [
            'fight' => $fight,
            'stats' => $stats,
            'analysis' => $analysis,
            'totalStats' => array_values($totals),
        ]);
    }

    #[Route('/new', name: 'app_stat_new', methods: ['GET', 'POST'])]
    public function new(Request $request, FightStatisticService $service, FighterRepository $fighterRepo, FightResultRepository $resultRepo, EventRepository $eventRepo, FightStatisticRepository $statRepo, EntityManagerInterface $em): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        $fightId = $request->query->get('fightId');
        $round = (int)$request->query->get('round', 1);

        if (!$fightId) {
            return $this->redirectToRoute('app_stat_selection');
        }

        // Phase 3: SmartFight Statistics Entry (Per Round)
        $fight = $resultRepo->find($fightId);
        if (!$fight) throw $this->createNotFoundException();

        $boxer1 = $fight->getFighter1();
        $boxer2 = $fight->getFighter2();
        
        // Find existing stats for this specific round or create new ones
        $stat1 = $statRepo->findOneBy([
            'fightResult' => $fight,
            'fighter' => $boxer1,
            'round' => $round
        ]) ?: new FightStatistic();
        
        $stat2 = $statRepo->findOneBy([
            'fightResult' => $fight,
            'fighter' => $boxer2,
            'round' => $round
        ]) ?: new FightStatistic();

        // VALIDATION: Prevent entry for rounds beyond the actual fight end
        $maxRound = ($fight->getStatus() === 'COMPLETED' && $fight->getRoundNumber()) ? $fight->getRoundNumber() : ($fight->getScheduledRounds() ?: 12);
        if ($round > $maxRound) {
            $this->addFlash('warning', "This fight ended in Round $maxRound. You cannot enter statistics for Round $round.");
            return $this->redirectToRoute('app_stat_new', ['fightId' => $fightId, 'round' => $maxRound]);
        }

        if ($request->isMethod('POST')) {
            try {
                $this->bindPairedStats($stat1, $stat2, $fight, $request);
                $stat1->setRound($round);
                $stat2->setRound($round);
                
                $em->persist($stat1);
                $em->persist($stat2);
                $em->flush();

                // Trigger AI recalculations
                $service->recalculateForFighter($boxer1->getFighterId());
                $service->recalculateForFighter($boxer2->getFighterId());

                $this->addFlash('success', "Statistics for Round $round saved successfully.");
                
                // If there are more rounds, maybe stay on page for next round?
                if ($round < ($fight->getRoundNumber() ?: 12)) {
                    return $this->redirectToRoute('app_stat_new', [
                        'fightId' => $fightId,
                        'round' => $round + 1
                    ]);
                }

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
            'currentRound' => $round,
            'totalRounds' => $maxRound,
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
        $this->addFlash('success', 'All SmartFight records for this bout removed.');
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
        $s1->setUppercutsThrown((int)$r->request->get('s1_uppercutsThrown', 0));
        $s1->setUppercutsLanded((int)$r->request->get('s1_uppercutsLanded', 0));
        $s1->setRightHandThrown((int)$r->request->get('s1_rightThrown', 0));
        $s1->setRightHandLanded((int)$r->request->get('s1_rightLanded', 0));
        $s1->setLeftHandThrown((int)$r->request->get('s1_leftThrown', 0));
        $s1->setLeftHandLanded((int)$r->request->get('s1_leftLanded', 0));
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
        $s2->setUppercutsThrown((int)$r->request->get('s2_uppercutsThrown', 0));
        $s2->setUppercutsLanded((int)$r->request->get('s2_uppercutsLanded', 0));
        $s2->setRightHandThrown((int)$r->request->get('s2_rightThrown', 0));
        $s2->setRightHandLanded((int)$r->request->get('s2_rightLanded', 0));
        $s2->setLeftHandThrown((int)$r->request->get('s2_leftThrown', 0));
        $s2->setLeftHandLanded((int)$r->request->get('s2_leftLanded', 0));
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
            if ($s->getUppercutsLanded() > $s->getUppercutsThrown()) {
                throw new \RuntimeException("$label: Landed uppercuts cannot exceed thrown.");
            }
            if ($s->getRightHandThrown() + $s->getLeftHandThrown() !== $s->getPunchesThrown()) {
                throw new \RuntimeException("$label: Right + Left thrown must equal Total punches thrown.");
            }
            if ($s->getRightHandLanded() + $s->getLeftHandLanded() !== $s->getPunchesLanded()) {
                throw new \RuntimeException("$label: Right + Left landed must equal Total punches landed.");
            }
            if ($s->getPunchesThrown() < 0) {
                throw new \RuntimeException("$label: Values cannot be negative.");
            }
        };

        $validate($s1, 'Boxer 1');
        $validate($s2, 'Boxer 2');
    }
}

