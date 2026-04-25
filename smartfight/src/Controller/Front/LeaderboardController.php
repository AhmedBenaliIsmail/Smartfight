<?php

namespace App\Controller\Front;

use App\Service\LeaderboardService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

class LeaderboardController extends AbstractController
{
    #[Route('/leaderboard', name: 'front_leaderboard_index', methods: ['GET'])]
    public function index(Request $request, LeaderboardService $service): Response
    {
        $season = $request->query->get('season', '2026');

        $rankings = $service->getRankings($season);
        $topThree = array_slice($rankings, 0, 3);

        $user = $this->getUser();
        $myRank = $user ? $service->getFanRank($user->getId(), $season) : null;

        return $this->render('front/leaderboard/index.html.twig', [
            'rankings' => $rankings,
            'topThree' => $topThree,
            'myRank' => $myRank,
            'season' => $season,
        ]);
    }
}
