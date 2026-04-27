<?php

namespace App\Controller\Admin;

use App\Entity\FighterContract;
use App\Repository\EventRepository;
use App\Repository\FighterContractRepository;
use App\Repository\FighterRepository;
use App\Repository\FightResultRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/admin/contracts')]
class AdminContractController extends AbstractController
{
    #[Route('', name: 'admin_contract_index')]
    public function index(FighterContractRepository $contractRepo, EventRepository $eventRepo, FighterRepository $fighterRepo, FightResultRepository $fightRepo): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        $contracts = $contractRepo->findBy([], ['id' => 'DESC']);
        $events = $eventRepo->findAllOrderedByDate();
        $fighters = $fighterRepo->findAllOrderedByName();
        
        // Build event-to-fighters map
        $allFights = $fightRepo->findAll();
        $eventFighters = [];
        foreach ($allFights as $f) {
            $eId = $f->getEvent()->getEventId();
            if (!isset($eventFighters[$eId])) $eventFighters[$eId] = [];
            
            $f1 = $f->getFighter1();
            $f2 = $f->getFighter2();
            
            if ($f1) $eventFighters[$eId][$f1->getFighterId()] = $f1->getFullName();
            if ($f2) $eventFighters[$eId][$f2->getFighterId()] = $f2->getFullName();
        }

        // Calculate totals
        $totalCommitted = 0;
        $totalPaidOut = 0;
        foreach ($contracts as $c) {
            $expectedMax = $c->getBasePay() + $c->getWinBonus();
            $totalCommitted += $expectedMax;
            if ($c->isPaid() && $c->getCalculatedPayout() !== null) {
                $totalPaidOut += $c->getCalculatedPayout();
            }
        }

        return $this->render('admin/contract/index.html.twig', [
            'contracts' => $contracts,
            'events' => $events,
            'fighters' => $fighters,
            'totalCommitted' => $totalCommitted,
            'totalPaidOut' => $totalPaidOut,
            'eventFightersJson' => json_encode($eventFighters),
        ]);
    }

    #[Route('/create', name: 'admin_contract_create', methods: ['POST'])]
    public function create(Request $request, EntityManagerInterface $em, EventRepository $eventRepo, FighterRepository $fighterRepo, FighterContractRepository $contractRepo): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        $eventId = $request->request->get('event_id');
        $fighterId = $request->request->get('fighter_id');
        $basePay = (float) $request->request->get('base_pay', 0);
        $winBonus = (float) $request->request->get('win_bonus', 0);

        if (!$eventId || !$fighterId) {
            $this->addFlash('error', 'Event and Fighter are required.');
            return $this->redirectToRoute('admin_contract_index');
        }

        $event = $eventRepo->find($eventId);
        $fighter = $fighterRepo->find($fighterId);

        if (!$event || !$fighter) {
            $this->addFlash('error', 'Invalid Event or Fighter.');
            return $this->redirectToRoute('admin_contract_index');
        }

        // Check if contract already exists
        $existing = $contractRepo->findOneBy(['event' => $event, 'fighter' => $fighter]);
        if ($existing) {
            $this->addFlash('error', 'A contract already exists for this fighter in this event.');
            return $this->redirectToRoute('admin_contract_index');
        }

        $contract = new FighterContract();
        $contract->setEvent($event);
        $contract->setFighter($fighter);
        $contract->setBasePay($basePay);
        $contract->setWinBonus($winBonus);

        $em->persist($contract);
        $em->flush();

        $this->addFlash('success', 'Fighter contract created successfully.');
        return $this->redirectToRoute('admin_contract_index');
    }

    #[Route('/{id}/pay', name: 'admin_contract_pay', methods: ['POST'])]
    public function pay(int $id, FighterContractRepository $contractRepo, EntityManagerInterface $em): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        $contract = $contractRepo->find($id);
        if (!$contract) {
            throw $this->createNotFoundException('Contract not found');
        }

        if ($contract->getCalculatedPayout() === null) {
            $this->addFlash('error', 'Cannot pay contract yet. The fight result has not been processed.');
            return $this->redirectToRoute('admin_contract_index');
        }

        $contract->setIsPaid(true);
        $em->flush();

        $this->addFlash('success', 'Contract marked as PAID.');
        return $this->redirectToRoute('admin_contract_index');
    }

    #[Route('/{id}/delete', name: 'admin_contract_delete', methods: ['POST'])]
    public function delete(int $id, FighterContractRepository $contractRepo, EntityManagerInterface $em): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        $contract = $contractRepo->find($id);
        if ($contract) {
            $em->remove($contract);
            $em->flush();
            $this->addFlash('success', 'Contract deleted.');
        }

        return $this->redirectToRoute('admin_contract_index');
    }
}
