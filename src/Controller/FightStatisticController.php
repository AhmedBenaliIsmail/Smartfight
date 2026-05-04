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

        // Build aggregated per-fighter totals (totalStats[0] = fighter1, totalStats[1] = fighter2)
        // so the template can render fighter names, punch accuracy and landed stats correctly
        foreach ($groupedStats as $rid => &$group) {
            $fight = $group['result'];
            $fighters = array_filter([$fight->getFighter1(), $fight->getFighter2()]);
            $totalStats = [];
            foreach ($fighters as $fighter) {
                $agg = new \App\Entity\FightStatistic();
                $agg->setFighter($fighter);
                $agg->setFightResult($fight);
                $pl = $pt = $ppl = $ppt = $jl = $jt = $kd = $bsl = 0;
                foreach ($group['stats'] as $s) {
                    if ($s->getFighter() && $s->getFighter()->getFighterId() === $fighter->getFighterId()) {
                        $pl  += $s->getPunchesLanded();
                        $pt  += $s->getPunchesThrown();
                        $ppl += $s->getPowerPunchesLanded();
                        $ppt += $s->getPowerPunchesThrown();
                        $jl  += $s->getJabsLanded();
                        $jt  += $s->getJabsThrown();
                        $kd  += $s->getKnockdowns();
                        $bsl += $s->getBodyShotsLanded();
                    }
                }
                $agg->setPunchesLanded($pl);
                $agg->setPunchesThrown($pt);
                $agg->setPowerPunchesLanded($ppl);
                $agg->setPowerPunchesThrown($ppt);
                $agg->setJabsLanded($jl);
                $agg->setJabsThrown($jt);
                $agg->setKnockdowns($kd);
                $agg->setBodyShotsLanded($bsl);
                $totalStats[] = $agg;
            }
            $group['totalStats'] = $totalStats;
        }
        unset($group);

        // Generate AI analysis for each group
        foreach ($groupedStats as $rid => &$group) {
            $group['analysis'] = $analysisService->analyzeBout($group['result'], $group['totalStats'], $group['stats']);
        }
        unset($group);

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
        $round = (int)$request->query->get('round', 1);

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
                
                $commentary = $request->request->get('roundCommentary') ?: null;
                $stat1->setCommentary($commentary);
                $stat2->setCommentary($commentary);

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

    #[Route('/show/{fightId}', name: 'app_stat_show')]
    public function show(int $fightId, FightStatisticRepository $statRepo, FightResultRepository $resultRepo, \App\Service\BoutAnalysisService $analysisService, \App\Service\RoundCommentaryService $commentaryService): Response
    {
        $this->denyAccessUnlessGranted('ROLE_USER');

        $fight = $resultRepo->find($fightId);
        if (!$fight) throw $this->createNotFoundException('Fight not found.');

        $allStats = $statRepo->findBy(['fightResult' => $fight], ['round' => 'ASC']);

        $fighter1 = $fight->getFighter1();
        $fighter2 = $fight->getFighter2();

        // Build one aggregate FightStatistic per fighter so the template can
        // call ->getPunchAccuracy(), ->getJabAccuracy(), ->getPowerAccuracy() etc.
        $aggregates = [];
        foreach ([$fighter1, $fighter2] as $fighter) {
            if (!$fighter) continue;

            $agg = new FightStatistic();
            $agg->setFighter($fighter);
            $agg->setFightResult($fight);

            $pl = $pt = $ppl = $ppt = $jl = $jt = $kd = $bsl = 0;
            foreach ($allStats as $s) {
                if ($s->getFighter() && $s->getFighter()->getFighterId() === $fighter->getFighterId()) {
                    $pl  += $s->getPunchesLanded();
                    $pt  += $s->getPunchesThrown();
                    $ppl += $s->getPowerPunchesLanded();
                    $ppt += $s->getPowerPunchesThrown();
                    $jl  += $s->getJabsLanded();
                    $jt  += $s->getJabsThrown();
                    $kd  += $s->getKnockdowns();
                    $bsl += $s->getBodyShotsLanded();
                }
            }

            $agg->setPunchesLanded($pl);
            $agg->setPunchesThrown($pt);
            $agg->setPowerPunchesLanded($ppl);
            $agg->setPowerPunchesThrown($ppt);
            $agg->setJabsLanded($jl);
            $agg->setJabsThrown($jt);
            $agg->setKnockdowns($kd);
            $agg->setBodyShotsLanded($bsl);

            $aggregates[] = $agg;
        }

        // Group stats by round to generate on-the-fly commentary if missing
        $roundsData = [];
        foreach ($allStats as $s) {
            $r = $s->getRound();
            if (!isset($roundsData[$r])) {
                $roundsData[$r] = [];
            }
            $roundsData[$r][] = $s;
        }

        // Generate dynamic commentary for any round that doesn't have it
        foreach ($roundsData as $r => $roundStats) {
            $hasCommentary = false;
            foreach ($roundStats as $s) {
                if ($s->getCommentary()) {
                    $hasCommentary = true;
                    break;
                }
            }

            if (!$hasCommentary && count($roundStats) == 2) {
                $s1 = $roundStats[0];
                $s2 = $roundStats[1];

                $f1Data = [
                    'landed' => $s1->getPunchesLanded(),
                    'thrown' => $s1->getPunchesThrown(),
                    'kds' => $s1->getKnockdowns(),
                    'power_landed' => $s1->getPowerPunchesLanded(),
                    'power_thrown' => $s1->getPowerPunchesThrown(),
                    'body_shots' => $s1->getBodyShotsLanded(),
                    'jabs_landed' => $s1->getJabsLanded(),
                ];

                $f2Data = [
                    'landed' => $s2->getPunchesLanded(),
                    'thrown' => $s2->getPunchesThrown(),
                    'kds' => $s2->getKnockdowns(),
                    'power_landed' => $s2->getPowerPunchesLanded(),
                    'power_thrown' => $s2->getPowerPunchesThrown(),
                    'body_shots' => $s2->getBodyShotsLanded(),
                    'jabs_landed' => $s2->getJabsLanded(),
                ];

                $generatedCommentary = $commentaryService->generateForRound(
                    $s1->getFighter()->getLastName(), $f1Data,
                    $s2->getFighter()->getLastName(), $f2Data,
                    $r
                );

                // Attach to the first stat of the round for template display
                $s1->setCommentary($generatedCommentary);
            }
        }

        $analysis = $analysisService->analyzeBout($fight, $aggregates, $allStats);

        return $this->render('statistic/show.html.twig', [
            'fight'       => $fight,
            'fighter1'    => $fighter1,
            'fighter2'    => $fighter2,
            'totalStats'  => $aggregates,   // template: {% for t in totalStats %}
            'stats'       => $allStats,      // template: {% for stat in stats %}
            'analysis'    => $analysis,
        ]);
    }


    #[Route('/selection', name: 'app_stat_selection')]
    public function selection(): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        return $this->redirectToRoute('app_stat_new');
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

