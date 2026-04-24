<?php

namespace App\Controller\Front;

use App\Entity\EventBooking;
use App\Repository\EventBookingRepository;
use App\Repository\EventRepository;
use App\Service\BookingService;
use App\Service\QrCodeService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

class BookingController extends AbstractController
{
    #[Route('/booking', name: 'front_booking_index', methods: ['GET', 'POST'])]
    public function index(
        Request $request,
        EventRepository $eventRepository,
        EventBookingRepository $bookingRepository,
        BookingService $bookingService,
    ): Response {
        $this->denyAccessUnlessGranted('ROLE_USER');
        $user = $this->getUser();

        $events = $eventRepository->findBookableEvents();
        $remainingByEvent = [];
        foreach ($events as $event) {
            $confirmed = $bookingRepository->getTotalConfirmedQty((int) $event->getId());
            $remainingByEvent[(int) $event->getId()] = max($event->getCapacity() - $confirmed, 0);
        }

        if ($request->isMethod('POST')) {
            $eventId = $request->request->getInt('event_id');
            $ticketType = (string) $request->request->get('ticket_type', 'REGULAR');
            $quantity = $request->request->getInt('quantity', 1);

            $event = $eventRepository->find($eventId);
            if (!$event) {
                $this->addFlash('error', 'Selected event not found.');
                return $this->redirectToRoute('front_booking_index');
            }

            try {
                $bookingService->createBooking($user, $event, $ticketType, $quantity);
                $this->addFlash('success', 'Booking confirmed successfully.');
                return $this->redirectToRoute('front_booking_my');
            } catch (\Throwable $e) {
                $this->addFlash('error', $e->getMessage());
            }
        }

        return $this->render('front/booking/book.html.twig', [
            'events' => $events,
            'remainingByEvent' => $remainingByEvent,
            'priceMap' => [
                EventBooking::TYPE_VIP => BookingService::PRICE_VIP,
                EventBooking::TYPE_REGULAR => BookingService::PRICE_REGULAR,
                EventBooking::TYPE_STANDING => BookingService::PRICE_STANDING,
            ],
        ]);
    }

    #[Route('/booking/my', name: 'front_booking_my', methods: ['GET'])]
    public function myBookings(EventBookingRepository $bookingRepository): Response
    {
        $this->denyAccessUnlessGranted('ROLE_USER');
        $user = $this->getUser();

        return $this->render('front/booking/my_bookings.html.twig', [
            'bookings' => $bookingRepository->findByUserWithEvent((int) $user->getId()),
        ]);
    }

    #[Route('/booking/{id}/cancel', name: 'front_booking_cancel', methods: ['POST'])]
    public function cancel(
        Request $request,
        EventBooking $booking,
        BookingService $bookingService,
    ): Response {
        $this->denyAccessUnlessGranted('ROLE_USER');
        $user = $this->getUser();

        if ($booking->getUser()->getId() !== $user->getId()) {
            throw $this->createAccessDeniedException('You cannot cancel this booking.');
        }

        if (!$this->isCsrfTokenValid('cancel_booking_' . $booking->getId(), (string) $request->request->get('_token'))) {
            $this->addFlash('error', 'Invalid request token.');
            return $this->redirectToRoute('front_booking_my');
        }

        $bookingService->cancelBooking($booking);
        $this->addFlash('success', 'Booking cancelled.');

        return $this->redirectToRoute('front_booking_my');
    }

    #[Route('/booking/{id}/qr', name: 'front_booking_qr', methods: ['GET'])]
    public function qr(EventBooking $booking, QrCodeService $qrCodeService): Response
    {
        $this->denyAccessUnlessGranted('ROLE_USER');
        $user = $this->getUser();

        if ($booking->getUser()->getId() !== $user->getId()) {
            throw $this->createAccessDeniedException('You cannot access this ticket QR code.');
        }

        return $qrCodeService->generateQrResponse($qrCodeService->buildFanQrData($booking), 250);
    }
}
