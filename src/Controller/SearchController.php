<?php
namespace App\Controller;

use App\Repository\FighterRepository;
use App\Repository\EventRepository;
use App\Repository\BlogArticleRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

class SearchController extends AbstractController
{
    #[Route('/api/global-search', name: 'api_global_search', methods: ['GET'])]
    public function search(
        Request $request,
        FighterRepository $fighterRepo,
        EventRepository $eventRepo,
        BlogArticleRepository $articleRepo
    ): Response {
        $query = $request->query->get('q', '');
        if (strlen($query) < 2) {
            return $this->json(['results' => []]);
        }

        $results = [];

        // 1. Search Boxers
        $boxers = $fighterRepo->createQueryBuilder('f')
            ->where('f.firstName LIKE :q OR f.lastName LIKE :q OR f.nickname LIKE :q')
            ->setParameter('q', '%' . $query . '%')
            ->setMaxResults(5)
            ->getQuery()
            ->getResult();

        foreach ($boxers as $f) {
            $results[] = [
                'type' => 'BOXER',
                'title' => $f->getFullName(),
                'subtitle' => $f->getWeightDivision() ? $f->getWeightDivision()->getName() : 'Unclassified',
                'url' => $this->generateUrl('app_performance_show', ['id' => $f->getFighterId()]),
                'icon' => 'fa-mitten'
            ];
        }

        // 2. Search Events
        $events = $eventRepo->createQueryBuilder('e')
            ->where('e.eventName LIKE :q OR e.venue LIKE :q OR e.city LIKE :q')
            ->setParameter('q', '%' . $query . '%')
            ->setMaxResults(3)
            ->getQuery()
            ->getResult();

        foreach ($events as $e) {
            $results[] = [
                'type' => 'EVENT',
                'title' => $e->getEventName(),
                'subtitle' => $e->getEventDate() ? $e->getEventDate()->format('M d, Y') : 'TBA',
                'url' => $this->generateUrl('app_event_fights', ['id' => $e->getId()]),
                'icon' => 'fa-calendar-days'
            ];
        }

        // 3. Search Blog
        $articles = $articleRepo->createQueryBuilder('a')
            ->where('a.title LIKE :q OR a.summary LIKE :q')
            ->setParameter('q', '%' . $query . '%')
            ->setMaxResults(3)
            ->getQuery()
            ->getResult();

        foreach ($articles as $a) {
            $results[] = [
                'type' => 'ARTICLE',
                'title' => $a->getTitle(),
                'subtitle' => 'News / Blog',
                'url' => $this->isGranted('ROLE_ADMIN') 
                    ? $this->generateUrl('admin_blog_show', ['id' => $a->getId()]) 
                    : $this->generateUrl('front_blog_show', ['id' => $a->getId()]),
                'icon' => 'fa-newspaper'
            ];
        }

        return $this->json(['results' => $results]);
    }
}
