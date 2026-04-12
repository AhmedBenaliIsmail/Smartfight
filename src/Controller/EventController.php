<?php
namespace App\Controller;

use App\Entity\Event;
use App\Repository\EventRepository;
use App\Repository\FighterRepository;
use App\Repository\FightResultRepository;
use App\Repository\RankingRepository;
use App\Service\FightResultService;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/events')]
class EventController extends AbstractController
{
    #[Route('', name: 'app_events')]
    public function index(EventRepository $eventRepo, FightResultRepository $resultRepo): Response
    {
        $events = $eventRepo->findAllOrderedByDate();
        $fightCounts = [];
        foreach ($events as $e) {
            $fightCounts[$e->getEventId()] = $resultRepo->countByEvent($e->getEventId());
        }
        return $this->render('event/index.html.twig', [
            'events' => $events,
            'fightCounts' => $fightCounts,
            'title' => 'Events'
        ]);
    }

    #[Route('/champions', name: 'app_events_champions')]
    public function championsIndex(EventRepository $eventRepo, FightResultRepository $resultRepo): Response
    {
        $events = $eventRepo->findChampionsEvents();
        $fightCounts = [];
        foreach ($events as $e) {
            $fightCounts[$e->getEventId()] = $resultRepo->countByEvent($e->getEventId());
        }
        return $this->render('event/index.html.twig', [
            'events' => $events,
            'fightCounts' => $fightCounts,
            'title' => 'Champions Events',
            'isChampionsOnly' => true
        ]);
    }

    #[Route('/new', name: 'app_event_new', methods: ['GET', 'POST'])]
    public function new(Request $request, EntityManagerInterface $em): Response
    {
        if ($request->isMethod('POST')) {
            $e = new Event();
            $e->setEventName(trim($request->request->get('eventName', '')));
            $e->setEventDate(new \DateTime($request->request->get('eventDate', 'now')));
            $e->setLocation(trim($request->request->get('location', '')));
            $e->setIsChampionsEvent(false);
            $em->persist($e);
            $em->flush();
            $this->addFlash('success', 'Event created.');
            return $this->redirectToRoute('app_events');
        }
        return $this->render('event/form.html.twig', ['event' => null]);
    }

    #[Route('/champions-event/new', name: 'app_event_champions_new', methods: ['GET', 'POST'])]
    public function newChampionsEvent(Request $request, EntityManagerInterface $em, FighterRepository $fighterRepo, RankingRepository $rankingRepo, FightResultService $fightService): Response
    {
        // Use official Weight Class Distribution
        $weightClasses = ['Flyweight', 'Bantamweight', 'Featherweight', 'Lightweight', 'Welterweight', 'Middleweight', 'Light Heavyweight', 'Heavyweight'];

        if ($request->isMethod('POST')) {
            $wc1 = $request->request->get('weightClass1');
            $wc2 = $request->request->get('weightClass2');
            $wc3 = $request->request->get('weightClass3');

            if (!$wc1 || !$wc2 || !$wc3 || count(array_unique([$wc1, $wc2, $wc3])) !== 3) {
                $this->addFlash('error', 'You must select exactly 3 distinct weight classes.');
                return $this->render('event/champions_form.html.twig', ['weightClasses' => $weightClasses]);
            }

            // Verify we have #1 and #2 in those classes
            $classes = [$wc1, $wc2, $wc3];
            $fightsToAdd = [];
            foreach ($classes as $wc) {
                $rankings = $rankingRepo->findBy(['weightClass' => $wc], ['rankPosition' => 'ASC'], 2);
                if (count($rankings) < 2) {
                    $this->addFlash('error', "Not enough ranked fighters in $wc. Need at least 2.");
                    return $this->render('event/champions_form.html.twig', ['weightClasses' => $weightClasses]);
                }
                $fightsToAdd[] = [$rankings[0]->getFighterId(), $rankings[1]->getFighterId()];
            }

            $e = new Event();
            $e->setEventName(trim($request->request->get('eventName', 'Champions Event Night')));
            $e->setEventDate(new \DateTime($request->request->get('eventDate', 'now')));
            $e->setLocation(trim($request->request->get('location', 'Global Arena')));
            $e->setIsChampionsEvent(true);
            $em->persist($e);
            $em->flush();

            // Auto-schedule fights
            try {
                $fightService->addScheduledFight($e->getEventId(), 1, $fightsToAdd[0][0], $fightsToAdd[0][1]);
                $fightService->addScheduledFight($e->getEventId(), 2, $fightsToAdd[1][0], $fightsToAdd[1][1]);
                $fightService->addScheduledFight($e->getEventId(), 3, $fightsToAdd[2][0], $fightsToAdd[2][1]);
                $this->addFlash('success', 'Champions Event created and main card automatically scheduled!');
            } catch (\Exception $ex) {
                $this->addFlash('error', 'Event created but scheduling failed: ' . $ex->getMessage());
            }

            return $this->redirectToRoute('app_event_fights', ['id' => $e->getEventId()]);
        }

        return $this->render('event/champions_form.html.twig', ['weightClasses' => $weightClasses]);
    }

    #[Route('/{id}/edit', name: 'app_event_edit', methods: ['GET', 'POST'])]
    public function edit(int $id, Request $request, EventRepository $repo, EntityManagerInterface $em): Response
    {
        $e = $repo->find($id);
        if (!$e) throw $this->createNotFoundException();
        if ($request->isMethod('POST')) {
            $e->setEventName(trim($request->request->get('eventName', '')));
            $e->setEventDate(new \DateTime($request->request->get('eventDate', 'now')));
            $e->setLocation(trim($request->request->get('location', '')));
            $em->flush();
            $this->addFlash('success', 'Event updated.');
            return $this->redirectToRoute('app_events');
        }
        return $this->render('event/form.html.twig', ['event' => $e]);
    }

    #[Route('/{id}/delete', name: 'app_event_delete', methods: ['POST'])]
    public function delete(int $id, EventRepository $repo, EntityManagerInterface $em): Response
    {
        $e = $repo->find($id);
        if ($e) { $em->remove($e); $em->flush(); $this->addFlash('success', 'Event deleted.'); }
        return $this->redirectToRoute('app_events');
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
        $fighterMap = [];
        foreach ($fighterRepo->findAll() as $f) $fighterMap[$f->getFighterId()] = $f->getFullName();

        return $this->render('event/fights.html.twig', [
            'event' => $event,
            'fights' => $fights,
            'availableFighters' => array_values($availableFighters),
            'availableNumbers' => $availableNumbers,
            'fighterMap' => $fighterMap,
        ]);
    }

    #[Route('/{id}/fights/add', name: 'app_event_fight_add', methods: ['POST'])]
    public function addFight(int $id, Request $request, FightResultService $service): Response
    {
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
        $eventId = (int)$request->request->get('eventId');
        $service->deleteFightResult($fightId);
        $this->addFlash('success', 'Fight removed.');
        return $this->redirectToRoute('app_event_fights', ['id' => $eventId]);
    }
}
