<?php

namespace App\Controller\Admin;

use App\Entity\EventBooking;
use App\Repository\EventBookingRepository;
use App\Repository\EventRepository;
use App\Service\BookingService;
use App\Service\QrCodeService;
use Knp\Component\Pager\PaginatorInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/admin/bookings', name: 'admin_booking_')]
class BookingAdminController extends AbstractController
{
    #[Route('', name: 'index', methods: ['GET'])]
    public function index(
        Request $request,
        EventBookingRepository $bookingRepository,
        EventRepository $eventRepository,
        PaginatorInterface $paginator,
    ): Response {
        if (!$this->isGranted('ROLE_ADMIN')) {
            return $this->redirectToRoute('front_booking_index');
        }
        $eventId = $request->query->getInt('event') ?: null;
        $status = $request->query->get('status') ?: null;

        $qb = $bookingRepository->findForAdmin($eventId, $status);
        $pagination = $paginator->paginate($qb, $request->query->getInt('page', 1), 15);

        return $this->render('admin/booking/index.html.twig', [
            'pagination' => $pagination,
            'events' => $eventRepository->findAll(),
            'stats' => $bookingRepository->getAdminStats(),
            'filters' => $request->query->all(),
            'active_sidebar' => 'bookings',
        ]);
    }

    #[Route('/{id}/cancel', name: 'cancel', methods: ['POST'])]
    public function cancel(
        Request $request,
        EventBooking $booking,
        BookingService $bookingService,
    ): Response {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        if (!$this->isCsrfTokenValid('admin_cancel_booking_' . $booking->getId(), (string) $request->request->get('_token'))) {
            $this->addFlash('error', 'Invalid request token.');
            return $this->redirectToRoute('admin_booking_index');
        }

        $bookingService->cancelBooking($booking);
        $this->addFlash('success', 'Booking cancelled successfully.');

        return $this->redirectToRoute('admin_booking_index');
    }

    #[Route('/create', name: 'create', methods: ['GET', 'POST'])]
    public function create(
        Request $request,
        EventRepository $eventRepository,
        \App\Repository\UserRepository $userRepository,
        BookingService $bookingService
    ): Response {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        $events = $eventRepository->findAll();
        $users = $userRepository->findAll();

        if ($request->isMethod('POST')) {
            $eventId = $request->request->getInt('event_id');
            $userId = $request->request->getInt('user_id');
            $ticketType = $request->request->get('ticket_type');
            $quantity = $request->request->getInt('quantity', 1);

            $event = $eventRepository->find($eventId);
            $user = $userRepository->find($userId);

            if (!$event || !$user) {
                $this->addFlash('error', 'Invalid event or user.');
                return $this->redirectToRoute('admin_booking_create');
            }

            try {
                $bookingService->createBooking($user, $event, $ticketType, $quantity);
                $this->addFlash('success', 'Booking manually created successfully.');
                return $this->redirectToRoute('admin_booking_index');
            } catch (\Throwable $e) {
                $this->addFlash('error', $e->getMessage());
            }
        }

        return $this->render('admin/booking/create.html.twig', [
            'events' => $events,
            'users' => $users,
            'ticketTypes' => [
                EventBooking::TYPE_VIP_RINGSIDE => 'VIP Ringside',
                EventBooking::TYPE_PREMIUM_LOWER => 'Premium Lower',
                EventBooking::TYPE_REGULAR_SEATING => 'Regular Seating',
                EventBooking::TYPE_BALCONY => 'Balcony',
                EventBooking::TYPE_STANDING_ROOM => 'Standing Room',
            ],
            'active_sidebar' => 'bookings',
        ]);
    }

    #[Route('/{id}/qr', name: 'qr', methods: ['GET'])]
    public function qr(EventBooking $booking, QrCodeService $qrCodeService): Response
    {
        return $qrCodeService->generateQrResponse($qrCodeService->buildAdminQrData($booking), 250);
    }
}
