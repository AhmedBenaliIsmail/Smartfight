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
        $eventDetails = [];

        foreach ($events as $event) {
            $confirmed = $bookingRepository->getTotalConfirmedQty((int) $event->getId());
            $remaining = max($event->getCapacity() - $confirmed, 0);
            $pricingInfo = $bookingService->getDynamicPricingForEvent($event, $user);
            
            $eventDetails[(int) $event->getId()] = [
                'remaining' => $remaining,
                'prices' => $pricingInfo['prices'],
                'messages' => $pricingInfo['messages'],
            ];
        }

        if ($request->isMethod('POST')) {
            $eventId = $request->request->getInt('event_id');
            $ticketType = (string) $request->request->get('ticket_type', EventBooking::TYPE_REGULAR_SEATING);
            $quantity = $request->request->getInt('quantity', 1);

            $event = $eventRepository->find($eventId);
            if (!$event) {
                $this->addFlash('error', 'Selected event not found.');
                return $this->redirectToRoute('front_booking_index');
            }

            try {
                $bookingService->createBooking($user, $event, $ticketType, $quantity);
                $this->addFlash('success', '🏆 Booking confirmed! Your ticket has been sent. Please check your email and "My Bookings" page.');
                
                return $this->redirectToRoute('front_booking_my');
            } catch (\Throwable $e) {
                $this->addFlash('error', $e->getMessage());
            }
        }

        $bookedEventIds = $bookingRepository->findConfirmedEventIdsByUser((int) $user->getUserId());

        return $this->render('front/booking/book.html.twig', [
            'events' => $events,
            'eventDetails' => json_encode($eventDetails),
            'bookedEventIds' => $bookedEventIds,
            'ticketTypes' => [
                EventBooking::TYPE_VIP_RINGSIDE => 'VIP Ringside',
                EventBooking::TYPE_PREMIUM_LOWER => 'Premium Lower',
                EventBooking::TYPE_REGULAR_SEATING => 'Regular Seating',
                EventBooking::TYPE_BALCONY => 'Balcony',
                EventBooking::TYPE_STANDING_ROOM => 'Standing Room',
            ],
        ]);
    }

    #[Route('/booking/my', name: 'front_booking_my', methods: ['GET'])]
    public function myBookings(EventBookingRepository $bookingRepository, QrCodeService $qrCodeService): Response
    {
        $this->denyAccessUnlessGranted('ROLE_USER');
        $user = $this->getUser();

        $bookings = $bookingRepository->findByUserWithEvent((int) $user->getUserId());
        $bookingsWithQr = [];
        
        foreach ($bookings as $booking) {
            $qrBase64 = $qrCodeService->generateQrBase64($qrCodeService->buildFanQrData($booking));
            $bookingsWithQr[] = [
                'data' => $booking,
                'qrBase64' => $qrBase64
            ];
        }

        return $this->render('front/booking/my_bookings.html.twig', [
            'bookings' => $bookingsWithQr,
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

        if ($booking->getUser()->getUserId() !== $user->getUserId()) {
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

        if ($booking->getUser()->getUserId() !== $user->getUserId()) {
            throw $this->createAccessDeniedException('You cannot access this ticket QR code.');
        }

        return $qrCodeService->generateQrResponse($qrCodeService->buildFanQrData($booking), 250);
    }
}
