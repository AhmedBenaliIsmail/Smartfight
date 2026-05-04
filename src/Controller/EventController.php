<?php
namespace App\Controller;

use App\Entity\Event;
use App\Entity\WeightDivision;
use App\Repository\EventRepository;
use App\Repository\FighterRepository;
use App\Repository\FightResultRepository;
use App\Repository\WeightDivisionRepository;
use App\Repository\RankingRepository;
use App\Repository\MatchProposalRepository;
use App\Service\FightResultService;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use Knp\Component\Pager\PaginatorInterface;

#[Route('/events')]
class EventController extends AbstractController
{
    #[Route('/search-suggestions', name: 'app_event_search_suggestions', methods: ['GET'])]
    public function searchSuggestions(Request $request, EventRepository $repo): Response
    {
        $q = $request->query->get('q', '');
        if (empty($q)) {
            return $this->json([]);
        }

        $qb = $repo->createQueryBuilder('e')
            ->where('e.eventName LIKE :query')
            ->setParameter('query', '%' . $q . '%')
            ->setMaxResults(10);
            
        $events = $qb->getQuery()->getResult();
        $results = [];
        foreach ($events as $event) {
            $results[] = [
                'id' => $event->getEventId(),
                'name' => $event->getEventName()
            ];
        }

        return $this->json($results);
    }

    #[Route('/calendar', name: 'app_event_calendar', methods: ['GET'])]
    public function calendar(): Response
    {
        return $this->render('event/calendar.html.twig', [
            'title' => 'Event Calendar'
        ]);
    }

    #[Route('/api/calendar-events', name: 'app_event_api_calendar', methods: ['GET'])]
    public function calendarEvents(EventRepository $repo): Response
    {
        $events = $repo->findAll();
        $data = [];

        foreach ($events as $event) {
            if ($event->getEventDate()) {
                $status = $event->getStatus();
                $bgColor = match($status) {
                    'COMPLETED' => '#22c55e',
                    'LIVE'      => '#eab308',
                    'CANCELLED' => '#6b7280',
                    default     => '#dc2626',
                };
                $data[] = [
                    'id'              => $event->getEventId(),
                    'title'           => $event->getEventName(),
                    'start'           => $event->getEventDate()->format('Y-m-d'),
                    'url'             => $this->generateUrl('app_event_fights', ['id' => $event->getEventId()]),
                    'backgroundColor' => $bgColor,
                    'borderColor'     => '#09090b',
                    'textColor'       => '#ffffff',
                    'extendedProps'   => [
                        'venue'        => $event->getVenue(),
                        'city'         => $event->getCity(),
                        'organization' => $event->getOrganization(),
                        'status'       => $status,
                        'gcalDate'     => $event->getEventDate()->format('Ymd'),
                    ]
                ];
            }
        }

        return $this->json($data);
    }

    #[Route('/api/map-events', name: 'app_event_api_map', methods: ['GET'])]
    public function mapEvents(EventRepository $repo): Response
    {
        $events = $repo->findAll();
        $data = [];

        foreach ($events as $event) {
            if ($event->getCity() && $event->getVenue()) {
                $data[] = [
                    'id'              => $event->getEventId(),
                    'title'           => $event->getEventName(),
                    'venue'           => $event->getVenue(),
                    'city'            => $event->getCity(),
                    'date'            => $event->getEventDate() ? $event->getEventDate()->format('Y-m-d') : 'TBD',
                    'status'          => $event->getStatus(),
                    'organization'    => $event->getOrganization(),
                    'url'             => $this->generateUrl('app_event_fights', ['id' => $event->getEventId()]),
                ];
            }
        }

        return $this->json($data);
    }

    #[Route('/map', name: 'app_event_map', methods: ['GET'])]
    public function map(): Response
    {
        return $this->render('event/map.html.twig', [
            'title' => 'Event Map Locations'
        ]);
    }

    #[Route('', name: 'app_events')]
    public function index(EventRepository $eventRepo, FightResultRepository $resultRepo, PaginatorInterface $paginator, Request $request): Response
    {
        $q = $request->query->get('q');
        $org = $request->query->get('organization');
        $status = $request->query->get('status');
        $sortBy = $request->query->get('sortBy');

        $queryBuilder = $eventRepo->findFilteredAndSortedEvents($q, $org, $status, $sortBy);
        
        $pagination = $paginator->paginate(
            $queryBuilder,
            $request->query->getInt('page', 1),
            5 // limit per page
        );

        $fightCounts = [];
        foreach ($pagination->getItems() as $e) {
            $fightCounts[$e->getEventId()] = $resultRepo->countByEvent($e->getEventId());
        }
        return $this->render('event/index.html.twig', [
            'pagination' => $pagination,
            'fightCounts' => $fightCounts,
            'title' => 'Events',
            'current_q' => $q,
            'current_org' => $org,
            'current_status' => $status,
            'current_sortBy' => $sortBy,
        ]);
    }

    #[Route('/champions', name: 'app_events_champions')]
    public function championsIndex(EventRepository $eventRepo, FightResultRepository $resultRepo, PaginatorInterface $paginator, Request $request): Response
    {
        $queryBuilder = $eventRepo->createQueryBuilder('e')
            ->where('e.isChampionsEvent = :isChamp')
            ->setParameter('isChamp', true)
            ->orderBy('e.eventDate', 'DESC');
            
        $pagination = $paginator->paginate(
            $queryBuilder,
            $request->query->getInt('page', 1),
            5 // limit per page
        );

        $fightCounts = [];
        foreach ($pagination->getItems() as $e) {
            $fightCounts[$e->getEventId()] = $resultRepo->countByEvent($e->getEventId());
        }
        return $this->render('event/index.html.twig', [
            'pagination' => $pagination,
            'fightCounts' => $fightCounts,
            'title' => 'Major Sanctioned Events',
            'isChampionsOnly' => true
        ]);
    }

    #[Route('/new', name: 'app_event_new', methods: ['GET', 'POST'])]
    public function new(Request $request, EntityManagerInterface $em): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        if ($request->isMethod('POST')) {
            $e = new Event();
            $e->setEventName(trim($request->request->get('eventName', '')));
            $e->setEventDate(new \DateTime($request->request->get('eventDate', 'now')));
            $e->setVenue(trim($request->request->get('venue', '')));
            $e->setCity(trim($request->request->get('city', '')));
            $e->setOrganization(trim($request->request->get('organization', 'INDEPENDENT')));
            $e->setStatus($request->request->get('status', 'SCHEDULED'));
            $cap = $request->request->get('seatCapacity', '');
            if ($cap !== '') $e->setSeatCapacity((int)$cap);
            $country = trim($request->request->get('country', ''));
            if ($country) $e->setCountry(strtoupper(substr($country, 0, 2)));
            $em->persist($e);
            $em->flush();
            $this->addFlash('success', 'Boxing event card created.');
            return $this->redirectToRoute('app_events');
        }
        return $this->render('event/form.html.twig', ['event' => null]);
    }

    #[Route('/champions-event/new', name: 'app_event_champions_new', methods: ['GET', 'POST'])]
    public function newChampionsEvent(Request $request, EntityManagerInterface $em, WeightDivisionRepository $wdRepo, RankingRepository $rankingRepo, FightResultService $fightService): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        $weightDivisions = $wdRepo->findAll();

        if ($request->isMethod('POST')) {
            $wd1Id = $request->request->get('wd1');
            $wd2Id = $request->request->get('wd2');
            $wd3Id = $request->request->get('wd3');

            if (!$wd1Id || !$wd2Id || !$wd3Id || count(array_unique([$wd1Id, $wd2Id, $wd3Id])) !== 3) {
                $this->addFlash('error', 'You must select exactly 3 distinct weight divisions.');
                return $this->render('event/champions_form.html.twig', ['weightDivisions' => $weightDivisions]);
            }

            $divIds = [$wd1Id, $wd2Id, $wd3Id];
            $fightsToAdd = [];
            foreach ($divIds as $id) {
                $wd = $wdRepo->find($id);
                $rankings = $rankingRepo->findBy(['weightDivision' => $wd], ['rankPosition' => 'ASC'], 2);
                if (count($rankings) < 2) {
                    $this->addFlash('error', "Not enough ranked boxers in " . $wd->getName() . ". Need at least 2.");
                    return $this->render('event/champions_form.html.twig', ['weightDivisions' => $weightDivisions]);
                }
                $fightsToAdd[] = [$rankings[0]->getFighter(), $rankings[1]->getFighter()];
            }

            $e = new Event();
            $e->setEventName(trim($request->request->get('eventName', 'World Title Night')));
            $e->setEventDate(new \DateTime($request->request->get('eventDate', 'now')));
            $e->setVenue(trim($request->request->get('venue', 'Global Arena')));
            $e->setCity(trim($request->request->get('city', 'Las Vegas')));
            $e->setOrganization(trim($request->request->get('organization', 'UNDISPUTED')));
            $em->persist($e);
            $em->flush();

            // Auto-schedule fights
            try {
                $fightService->addScheduledFight($e->getEventId(), 1, $fightsToAdd[0][0]->getFighterId(), $fightsToAdd[0][1]->getFighterId());
                $fightService->addScheduledFight($e->getEventId(), 2, $fightsToAdd[1][0]->getFighterId(), $fightsToAdd[1][1]->getFighterId());
                $fightService->addScheduledFight($e->getEventId(), 3, $fightsToAdd[2][0]->getFighterId(), $fightsToAdd[2][1]->getFighterId());
                $this->addFlash('success', 'World Title Event created and headliners automatically scheduled!');
            } catch (\Exception $ex) {
                $this->addFlash('error', 'Event created but scheduling failed: ' . $ex->getMessage());
            }

            return $this->redirectToRoute('app_event_fights', ['id' => $e->getEventId()]);
        }

        return $this->render('event/champions_form.html.twig', ['weightDivisions' => $weightDivisions]);
    }

    #[Route('/{id}/edit', name: 'app_event_edit', methods: ['GET', 'POST'])]
    public function edit(int $id, Request $request, EventRepository $repo, EntityManagerInterface $em): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        $e = $repo->find($id);
        if (!$e) throw $this->createNotFoundException();
        if ($request->isMethod('POST')) {
            $e->setEventName(trim($request->request->get('eventName', '')));
            $e->setEventDate(new \DateTime($request->request->get('eventDate', 'now')));
            $e->setVenue(trim($request->request->get('venue', '')));
            $e->setCity(trim($request->request->get('city', '')));
            $e->setOrganization(trim($request->request->get('organization', 'INDEPENDENT')));
            $e->setStatus($request->request->get('status', $e->getStatus()));
            $cap = $request->request->get('seatCapacity', '');
            if ($cap !== '') $e->setSeatCapacity((int)$cap);
            $country = trim($request->request->get('country', ''));
            if ($country) $e->setCountry(strtoupper(substr($country, 0, 2)));
            $em->flush();
            $this->addFlash('success', 'Event updated.');
            return $this->redirectToRoute('app_events');
        }
        return $this->render('event/form.html.twig', ['event' => $e]);
    }

    #[Route('/{id}/delete', name: 'app_event_delete', methods: ['POST'])]
    public function delete(int $id, EventRepository $repo, EntityManagerInterface $em): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        $e = $repo->find($id);
        if ($e) { $em->remove($e); $em->flush(); $this->addFlash('success', 'Event deleted.'); }
        return $this->redirectToRoute('app_events');
    }

    #[Route('/{id}/flyer', name: 'app_event_flyer', methods: ['GET'])]
    public function generateFlyer(int $id, EventRepository $repo): Response
    {
        $event = $repo->find($id);
        if (!$event) throw $this->createNotFoundException();

        return $this->render('event/flyer.html.twig', [
            'event' => $event
        ]);
    }

    #[Route('/{id}/fights', name: 'app_event_fights')]
    public function fights(int $id, EventRepository $eventRepo, FightResultRepository $resultRepo, FighterRepository $fighterRepo): Response
    {
        $event = $eventRepo->find($id);
        if (!$event) throw $this->createNotFoundException();
        $fights = $resultRepo->findByEvent($id);
        $usedIds = $resultRepo->getFighterIdsInEvent($id);
        $availableFighters = array_filter($fighterRepo->findAll(), fn($f) => !in_array($f->getFighterId(), $usedIds));
        $availableNumbers = $resultRepo->getAvailableFightNumbers($id);

        return $this->render('event/fights.html.twig', [
            'event' => $event,
            'fights' => $fights,
            'availableFighters' => array_values($availableFighters),
            'availableNumbers' => $availableNumbers,
        ]);
    }

    #[Route('/{id}/fights/add', name: 'app_event_fight_add', methods: ['POST'])]
    public function addFight(int $id, Request $request, FightResultService $service): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        try {
            $service->addScheduledFight(
                $id,
                (int)$request->request->get('fightNumber'),
                (int)$request->request->get('fighter1Id'),
                (int)$request->request->get('fighter2Id')
            );
            $this->addFlash('success', 'Fight added!');
        } catch (\Exception $e) {
            $this->addFlash('error', $e->getMessage());
        }
        return $this->redirectToRoute('app_event_fights', ['id' => $id]);
    }

    #[Route('/fights/{fightId}/delete', name: 'app_event_fight_delete', methods: ['POST'])]
    public function deleteFight(int $fightId, Request $request, FightResultService $service): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        $eventId = (int)$request->request->get('eventId');
        $service->deleteFightResult($fightId);
        $this->addFlash('success', 'Fight removed.');
        return $this->redirectToRoute('app_event_fights', ['id' => $eventId]);
    }
    #[Route('/{id}/fights/ai-matchmake', name: 'app_event_fight_ai', methods: ['GET'])]
    public function aiMatchmake(int $id, EventRepository $eventRepo, FightResultRepository $resultRepo, FighterRepository $fighterRepo): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        $event = $eventRepo->find($id);
        if (!$event) return $this->json(['error' => 'Event not found'], 404);

        $usedIds = $resultRepo->getFighterIdsInEvent($id);
        $available = array_filter($fighterRepo->findAll(), fn($f) => !in_array($f->getFighterId(), $usedIds));
        
        if (count($available) < 2) {
            return $this->json(['error' => 'Not enough available fighters'], 400);
        }

        // Group by weight division
        $byWeight = [];
        foreach ($available as $f) {
            $wdId = $f->getWeightDivision() ? $f->getWeightDivision()->getId() : 0;
            if ($wdId > 0) $byWeight[$wdId][] = $f;
        }

        // Identify weight classes already present in this event card
        $existingFights = $resultRepo->findByEvent($id);
        $usedWeightIds = [];
        foreach ($existingFights as $ef) {
            if ($ef->getFighter1() && $ef->getFighter1()->getWeightDivision()) {
                $usedWeightIds[] = $ef->getFighter1()->getWeightDivision()->getId();
            }
        }

        // Shuffle the weight divisions to provide variety each time
        $wdIds = array_keys($byWeight);
        shuffle($wdIds);

        $allMatches = [];

        foreach ($wdIds as $wdId) {
            $fighters = $byWeight[$wdId];
            if (count($fighters) < 2) continue;

            // Give a "bonus" to weight classes not yet used in this event card
            $isNewWeightClass = !in_array($wdId, $usedWeightIds);

            for ($i = 0; $i < count($fighters); $i++) {
                for ($j = $i + 1; $j < count($fighters); $j++) {
                    $f1 = $fighters[$i];
                    $f2 = $fighters[$j];

                    // Heuristic scoring: Smaller difference is better match
                    $diffWins = abs($f1->getWins() - $f2->getWins());
                    $diffLosses = abs($f1->getLosses() - $f2->getLosses());
                    $diffHeight = abs(($f1->getHeight() ?? 175) - ($f2->getHeight() ?? 175));
                    $diffReach = abs(($f1->getReach() ?? 180) - ($f2->getReach() ?? 180));
                    
                    $ko1 = $f1->getWins() > 0 ? $f1->getKoWins() / $f1->getWins() : 0;
                    $ko2 = $f2->getWins() > 0 ? $f2->getKoWins() / $f2->getWins() : 0;
                    $diffKO = abs($ko1 - $ko2) * 10; 

                    $acc1 = $f1->getStrikeAccuracy();
                    $acc2 = $f2->getStrikeAccuracy();
                    $diffAcc = abs($acc1 - $acc2) / 10;

                    $score = ($diffWins * 1.0) + ($diffLosses * 1.0) + ($diffHeight * 0.5) + ($diffReach * 0.5) + ($diffKO * 2.0) + ($diffAcc * 1.5);

                    // --- STYLISTIC CLASH VECTOR ---
                    $style1 = $f1->getCalculatedFightingStyle();
                    $style2 = $f2->getCalculatedFightingStyle();
                    
                    if ($style1 !== $style2) {
                        $score -= 3.0; // Bonus for varied styles (Bull vs Matador logic)
                    } else if ($style1 === 'SLUGGER') {
                        $score -= 1.0; // Two sluggers is always fun
                    }

                    // Strongly prioritize weight classes NOT already on the card
                    if (!$isNewWeightClass) {
                        $score += 15.0; // Significant penalty for duplicate weight classes
                    }

                    $allMatches[] = [
                        'pair' => [$f1, $f2],
                        'score' => $score
                    ];
                }
            }
        }

        if (empty($allMatches)) {
            return $this->json(['error' => 'No appropriate matches found in same weight classes'], 400);
        }

        // Sort by score (best matches first)
        usort($allMatches, fn($a, $b) => $a['score'] <=> $b['score']);

        // Pick a random match from the top 5 (to provide variety)
        $poolSize = min(5, count($allMatches));
        $selectedIndex = rand(0, $poolSize - 1);
        $bestPair = $allMatches[$selectedIndex]['pair'];
        $bestScore = $allMatches[$selectedIndex]['score'];

        return $this->json([
            'fighter1' => [
                'id' => $bestPair[0]->getFighterId(),
                'name' => $bestPair[0]->getFullName(),
                'style' => $bestPair[0]->getCalculatedFightingStyle(),
                'weight' => $bestPair[0]->getWeightDivision()->getName(),
                'stats' => [
                    'record' => sprintf('%d-%d', $bestPair[0]->getWins(), $bestPair[0]->getLosses()),
                    'koWins' => $bestPair[0]->getKoWins(),
                    'height' => ($bestPair[0]->getHeight() ?? 'N/A') . 'cm',
                    'reach' => ($bestPair[0]->getReach() ?? 'N/A') . 'cm',
                    'accuracy' => number_format($bestPair[0]->getStrikeAccuracy(), 1) . '%'
                ]
            ],
            'fighter2' => [
                'id' => $bestPair[1]->getFighterId(),
                'name' => $bestPair[1]->getFullName(),
                'style' => $bestPair[1]->getCalculatedFightingStyle(),
                'weight' => $bestPair[1]->getWeightDivision()->getName(),
                'stats' => [
                    'record' => sprintf('%d-%d', $bestPair[1]->getWins(), $bestPair[1]->getLosses()),
                    'koWins' => $bestPair[1]->getKoWins(),
                    'height' => ($bestPair[1]->getHeight() ?? 'N/A') . 'cm',
                    'reach' => ($bestPair[1]->getReach() ?? 'N/A') . 'cm',
                    'accuracy' => number_format($bestPair[1]->getStrikeAccuracy(), 1) . '%'
                ]
            ],
            'score' => $bestScore,
            'matchQuality' => $bestScore < 3 ? 'EXCELLENT' : ($bestScore < 7 ? 'GOOD' : 'FAIR')
        ]);
    }

    #[Route('/fan-favorite/create', name: 'app_event_create_fan_favorite', methods: ['POST'])]
    public function createFanFavoriteEvent(
        MatchProposalRepository $proposalRepo,
        FightResultService $fightService,
        EntityManagerInterface $em
    ): Response {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        // Fetch top 3 most-voted pending proposals
        $topProposals = $proposalRepo->createQueryBuilder('p')
            ->where('p.status = :status')
            ->setParameter('status', 'PENDING')
            ->orderBy('p.voteCount', 'DESC')
            ->setMaxResults(3)
            ->getQuery()
            ->getResult();

        if (count($topProposals) < 1) {
            $this->addFlash('error', 'No voted proposals available to build a Fan Event.');
            return $this->redirectToRoute('app_dashboard');
        }

        // Create the Fan Favorite event
        $event = new Event();
        $event->setEventName('Fan Favorite Night — ' . (new \DateTime())->format('M j, Y'));
        $event->setEventDate(new \DateTime('+14 days'));
        $event->setVenue('Community Arena');
        $event->setCity('TBD');
        $event->setOrganization('FAN CHOICE');
        $em->persist($event);
        $em->flush();

        // Schedule each top proposal as a fight
        $fightNumber = 1;
        foreach ($topProposals as $proposal) {
            try {
                $fightService->addScheduledFight(
                    $event->getEventId(),
                    $fightNumber,
                    $proposal->getFighter1()->getFighterId(),
                    $proposal->getFighter2()->getFighterId()
                );
                // Mark proposal as approved
                $proposal->setStatus('APPROVED');
                $proposal->setEvent($event);
                $fightNumber++;
            } catch (\Exception $ex) {
                $this->addFlash('warning', 'Could not schedule "' . $proposal->getFightLabel() . '": ' . $ex->getMessage());
            }
        }

        $em->flush();

        $this->addFlash('success', sprintf(
            'Fan Favorite Event "%s" created with %d fight(s)!',
            $event->getEventName(),
            $fightNumber - 1
        ));

        return $this->redirectToRoute('app_event_fights', ['id' => $event->getEventId()]);
    }
}

