<?php
namespace App\Controller;

use App\Repository\FighterRepository;
use App\Repository\EventRepository;
use App\Repository\FightResultRepository;
use App\Repository\UserRepository;
use App\Repository\BlogArticleRepository;
use App\Repository\EventBookingRepository;
use App\Repository\FighterContractRepository;
use App\Repository\MatchProposalRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

class DashboardController extends AbstractController
{
    #[Route('/', name: 'app_dashboard')]
    public function index(
        FighterRepository $fighterRepo, 
        EventRepository $eventRepo, 
        FightResultRepository $resultRepo,
        UserRepository $userRepo,
        BlogArticleRepository $articleRepo,
        EventBookingRepository $bookingRepo,
        FighterContractRepository $contractRepo,
        MatchProposalRepository $proposalRepo
    ): Response
    {
        if (!$this->isGranted('ROLE_ADMIN')) {
            return $this->redirectToRoute('app_fan_dashboard');
        }

        // Main Stats
        $fighters = $fighterRepo->findAll();
        $events = $eventRepo->findAll();
        $results = $resultRepo->findAll();
        $users = $userRepo->findAll();
        $articles = $articleRepo->findAll();

        // 1. Real Revenue Data for last 7 days
        $rawRevenue = $bookingRepo->getDailyRevenueLast7Days();
        $revenueData = $this->prepareChartData($rawRevenue, 7);

        // 2. Real User Growth Data for last 7 days
        $rawUsers = $userRepo->getDailyRegistrationsLast7Days();
        $userGrowth = $this->prepareChartData($rawUsers, 7);

        // 3. Generate Labels for the last 7 days
        $chartLabels = [];
        for ($i = 6; $i >= 0; $i--) {
            $chartLabels[] = (new \DateTime())->modify("-$i days")->format('D');
        }

        return $this->render('dashboard/index.html.twig', [
            'totalFighters' => count($fighters),
            'totalEvents' => count($events),
            'totalFights' => count($results),
            'totalUsers' => count($users),
            'totalArticles' => count($articles),
            'revenueData' => json_encode($revenueData),
            'userGrowth' => json_encode($userGrowth),
            'chartLabels' => json_encode($chartLabels),
            'recentResults' => array_slice($results, 0, 5),
            'upcomingEvents' => array_slice($eventRepo->findUpcoming(), 0, 5),
            'financials' => [
                'revenue' => $bookingRepo->getAdminStats()['total_revenue'],
                'payouts' => $contractRepo->getFinancialStats()['total_calculated_payouts'],
                'pending' => $contractRepo->getFinancialStats()['total_pending'],
                'committed' => $contractRepo->getFinancialStats()['total_committed'],
            ],
            'topVoted' => $proposalRepo->findTopVoted(3)
        ]);
    }

    private function prepareChartData(array $rawData, int $days): array
    {
        $data = [];
        $lookup = [];
        foreach ($rawData as $row) {
            $lookup[$row['date']] = (float)$row['total'];
        }

        for ($i = $days - 1; $i >= 0; $i--) {
            $date = (new \DateTime())->modify("-$i days")->format('Y-m-d');
            $data[] = $lookup[$date] ?? 0;
        }

        return $data;
    }
}
