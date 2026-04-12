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
        if (!$this->isCsrfTokenValid('admin_cancel_booking_' . $booking->getId(), (string) $request->request->get('_token'))) {
            $this->addFlash('error', 'Invalid request token.');
            return $this->redirectToRoute('admin_booking_index');
        }

        $bookingService->cancelBooking($booking);
        $this->addFlash('success', 'Booking cancelled successfully.');

        return $this->redirectToRoute('admin_booking_index');
    }

    #[Route('/{id}/qr', name: 'qr', methods: ['GET'])]
    public function qr(EventBooking $booking, QrCodeService $qrCodeService): Response
    {
        return $qrCodeService->generateQrResponse($qrCodeService->buildAdminQrData($booking), 250);
    }
}
