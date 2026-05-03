<?php

namespace App\Controller\Admin;

use App\Repository\EventBookingRepository;
use App\Repository\EventRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/admin/finance', name: 'admin_finance_')]
class FinanceAdminController extends AbstractController
{
    #[Route('', name: 'index', methods: ['GET'])]
    public function index(
        EventBookingRepository $bookingRepository,
        EventRepository $eventRepository
    ): Response {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        // General Stats
        $globalStats = $bookingRepository->getAdminStats();
        
        // Revenue per Event (for Bilan Financier)
        $revenuePerEvent = $eventRepository->createQueryBuilder('e')
            ->select('e.eventName, COALESCE(SUM(b.totalPrice), 0) as revenue, COUNT(b.id) as bookings')
            ->leftJoin('App\Entity\EventBooking', 'b', 'WITH', 'b.event = e.eventId AND b.bookingStatus = :status')
            ->setParameter('status', \App\Entity\EventBooking::STATUS_CONFIRMED)
            ->groupBy('e.eventId')
            ->orderBy('revenue', 'DESC')
            ->getQuery()
            ->getResult();

        // Revenue over time (Last 30 days for charts)
        $conn = $bookingRepository->getEntityManager()->getConnection();
        $dailyRevenue = $conn->fetchAllAssociative('
            SELECT booking_date as date, SUM(total_price) as total
            FROM event_booking
            WHERE booking_status = "CONFIRMED"
            AND booking_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
            GROUP BY booking_date
            ORDER BY booking_date ASC
        ');

        // Revenue by Ticket Type
        $revenueByType = $conn->fetchAllAssociative('
            SELECT ticket_type, SUM(total_price) as total, COUNT(*) as count
            FROM event_booking
            WHERE booking_status = "CONFIRMED"
            GROUP BY ticket_type
        ');

        return $this->render('admin/finance/index.html.twig', [
            'stats' => $globalStats,
            'revenuePerEvent' => $revenuePerEvent,
            'dailyRevenue' => $dailyRevenue,
            'revenueByType' => $revenueByType,
            'active_sidebar' => 'finance',
        ]);
    }
}
