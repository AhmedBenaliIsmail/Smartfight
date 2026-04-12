<?php
namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use App\Repository\CombattantRepository;

class DashboardController extends AbstractController
{
    #[Route('/', name: 'home')]
    public function home(): Response
    {
        return $this->redirectToRoute('app_login');
    }

    #[Route('/admin/dashboard', name: 'dashboard')]
    public function index(CombattantRepository $repo): Response
    {
        $totalCombattants = count($repo->findAll());

        return $this->render('dashboard/index.html.twig', [
            'totalCombattants' => $totalCombattants,
        ]);
    }
}