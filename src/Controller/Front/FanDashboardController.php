<?php
namespace App\Controller\Front;

use App\Entity\FanVote;
use App\Repository\BlogArticleRepository;
use App\Repository\EventRepository;
use App\Repository\FanVoteRepository;
use App\Repository\FighterRepository;
use App\Repository\FightResultRepository;
use App\Repository\MatchProposalRepository;
use App\Repository\PerformanceScoreRepository;
use App\Repository\RankingRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

class FanDashboardController extends AbstractController
{
    #[Route('/fan/dashboard', name: 'app_fan_dashboard')]
    public function index(
        FighterRepository $fighterRepo,
        EventRepository $eventRepo,
        BlogArticleRepository $articleRepo,
        FightResultRepository $resultRepo,
        RankingRepository $rankingRepo,
        MatchProposalRepository $proposalRepo,
        FanVoteRepository $fanVoteRepo,
        PerformanceScoreRepository $perfScoreRepo,
        \App\Repository\PredictionRepository $predRepo
    ): Response {
        $user = $this->getUser();
        if (!$user) {
            return $this->redirectToRoute('app_login');
        }

        // Calculate Prediction Accuracy per Event
        $predictions = $predRepo->findBy(['user' => $user->getUserId(), 'isProcessed' => true], ['createdAt' => 'ASC']);
        $eventAccuracy = [];
        $tempData = [];
        
        foreach ($predictions as $p) {
            $eventId = $p->getFight()->getEvent()->getId();
            $eventName = $p->getFight()->getEvent()->getEventName();
            if (!isset($tempData[$eventId])) {
                $tempData[$eventId] = ['correct' => 0, 'total' => 0, 'name' => $eventName];
            }
            $tempData[$eventId]['total']++;
            if ($p->getPointsAwarded() > 0) {
                $tempData[$eventId]['correct']++;
            }
        }

        $labels = [];
        $accuracyData = [];
        $count = 0;
        foreach ($tempData as $data) {
            $labels[] = $data['name'];
            $accuracyData[] = ($data['total'] > 0) ? round(($data['correct'] / $data['total']) * 100) : 0;
            $count++;
            if ($count >= 6) break; // Limit to last 6 events
        }

        // Default if no data
        if (empty($labels)) {
            $labels = ['No Data'];
            $accuracyData = [0];
        }

        // Elite Fighter Showcase: Unique Top 3 by PerformanceScore
        $allPerfScores = $perfScoreRepo->findBy([], ['score' => 'DESC'], 10); // Get more to filter duplicates
        $eliteFighters = [];
        $seenFighterIds = [];
        
        foreach ($allPerfScores as $ps) {
            $fighterId = $ps->getFighter()->getFighterId();
            if (!in_array($fighterId, $seenFighterIds)) {
                $eliteFighters[] = [
                    'fighter' => $ps->getFighter(),
                    'perfScore' => $ps,
                ];
                $seenFighterIds[] = $fighterId;
            }
            if (count($eliteFighters) >= 3) break;
        }

        // Fallback to ELO if no performance scores exist
        if (empty($eliteFighters)) {
            $topByElo = $fighterRepo->findBy([], ['eloRating' => 'DESC'], 3);
            foreach ($topByElo as $f) {
                $eliteFighters[] = ['fighter' => $f, 'perfScore' => null];
            }
        }

        // Fan Matchmaking Proposals
        $matchProposals = $proposalRepo->findBy(['status' => 'PENDING'], ['voteCount' => 'DESC'], 5);

        // User's already-voted proposals
        $userVotedIds = $fanVoteRepo->findVotedProposalIdsByUser($user->getUserId());

        // Real World Rankings from Ranking entity (MEDIA org, grouped by weight class)
        $allRankings = $rankingRepo->findBy(
            ['organization' => 'MEDIA'],
            ['points' => 'DESC']
        );
        
        $worldRankingsByWeight = [];
        foreach ($allRankings as $ranking) {
            $wc = $ranking->getWeightDivision() ? $ranking->getWeightDivision()->getName() : 'Unclassified';
            if (!isset($worldRankingsByWeight[$wc])) {
                $worldRankingsByWeight[$wc] = [];
            }
            if (count($worldRankingsByWeight[$wc]) < 5) {
                $worldRankingsByWeight[$wc][] = $ranking;
            }
        }

        // Upcoming Events (only future events)
        $upcomingEvents = array_slice($eventRepo->findUpcoming(), 0, 6);

        // Latest Results (Only COMPLETED fights)
        $latestResults = $resultRepo->findBy(['status' => 'COMPLETED'], ['resultId' => 'DESC'], 5);

        // Trending Articles
        $trendingArticles = $articleRepo->findBy([], ['createdAt' => 'DESC'], 4);

        // Prepare Calendar Data — all events
        $allEvents = $eventRepo->findAll();
        $calendarEvents = [];
        foreach ($allEvents as $event) {
            $calendarEvents[] = [
                'title' => $event->getEventName(),
                'start' => $event->getEventDate() ? $event->getEventDate()->format('Y-m-d') : null,
                'url' => $this->generateUrl('app_event_fights', ['id' => $event->getId()]),
                'backgroundColor' => '#dc2626',
                'borderColor' => '#dc2626',
            ];
        }

        return $this->render('front/fan_dashboard.html.twig', [
            'eliteFighters' => $eliteFighters,
            'matchProposals' => $matchProposals,
            'userVotedIds' => $userVotedIds,
            'worldRankingsByWeight' => $worldRankingsByWeight,
            'upcomingEvents' => $upcomingEvents,
            'latestResults' => $latestResults,
            'trendingArticles' => $trendingArticles,
            'calendarEvents' => json_encode($calendarEvents),
            'predictionLabels' => json_encode($labels),
            'predictionData' => json_encode($accuracyData),
            'topVoted' => $proposalRepo->findTopVoted(3)
        ]);
    }

    #[Route('/fan/vote/{id}', name: 'app_fan_vote', methods: ['POST'])]
    public function vote(
        int $id,
        MatchProposalRepository $proposalRepo,
        FanVoteRepository $fanVoteRepo,
        EntityManagerInterface $em
    ): Response {
        $user = $this->getUser();
        if (!$user) {
            return $this->redirectToRoute('app_login');
        }

        $proposal = $proposalRepo->find($id);
        if (!$proposal) {
            $this->addFlash('error', 'Matchmaking proposal not found.');
            return $this->redirectToRoute('app_fan_dashboard');
        }

        // Enforce one vote per user per proposal
        if ($fanVoteRepo->hasUserVoted($user->getUserId(), $id)) {
            $this->addFlash('warning', 'You have already voted for this matchup!');
            return $this->redirectToRoute('app_fan_dashboard');
        }

        // Record the vote
        $vote = new FanVote();
        $vote->setUser($user);
        $vote->setMatchProposal($proposal);
        $em->persist($vote);

        // Increment vote count on proposal
        $proposal->incrementVoteCount();
        $em->persist($proposal);

        $em->flush();

        $this->addFlash('success', 'Your vote has been recorded! Thanks for shaping the future of SmartFight.');
        return $this->redirect($this->generateUrl('app_fan_dashboard') . '#matchmaking');
    }
}
