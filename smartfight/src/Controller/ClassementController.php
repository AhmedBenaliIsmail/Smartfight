<?php
namespace App\Controller;

use App\Repository\ClassementRepository;
use App\Service\ClassementService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/classement', name: 'app_classement_')]
class ClassementController extends AbstractController
{
    public function __construct(
        private ClassementRepository $classementRepo,
        private ClassementService $classementService
    ) {}

    #[Route('', name: 'index', methods: ['GET', 'POST'])]
    public function index(Request $request): Response
    {
        $discipline = $request->query->get('discipline', 'All');
        $search = $request->query->get('search', '');

        // Recalculer
        if ($request->query->get('recalculer')) {
            $this->classementService->recalculer(
                $discipline !== 'All' ? $discipline : null
            );
            return $this->redirectToRoute('app_classement_index', [
                'discipline' => $discipline
            ]);
        }

        // Filtrer
        $classements = $discipline !== 'All'
            ? $this->classementRepo->findBy(['discipline' => $discipline], ['rang' => 'ASC'])
            : $this->classementRepo->findBy([], ['score' => 'DESC']);

        // Recherche par nom
        if ($search) {
            $classements = array_filter($classements, fn($c) =>
                str_contains(strtolower($c->getCombattant()->getNickname()), strtolower($search))
            );
        }

        // Top 10 pour le chart
        $top10 = array_slice(
            $this->classementRepo->findBy([], ['score' => 'DESC']),
            0, 10
        );

        return $this->render('classement/index.html.twig', [
            'classements' => $classements,
            'top10'       => $top10,
            'discipline'  => $discipline,
            'search'      => $search,
            'total'       => count($classements),
        ]);
    }
}