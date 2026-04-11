<?php
namespace App\Service;

use App\Entity\Classement;
use App\Repository\CombattantRepository;
use App\Repository\ClassementRepository;
use Doctrine\ORM\EntityManagerInterface;

class ClassementService
{
    public function __construct(
        private CombattantRepository $combattantRepo,
        private ClassementRepository $classementRepo,
        private EntityManagerInterface $em
    ) {}

    public function recalculer(?string $discipline = null): void
    {
        // Supprimer anciens classements
        $anciens = $discipline
            ? $this->classementRepo->findBy(['discipline' => $discipline])
            : $this->classementRepo->findAll();

        foreach ($anciens as $c) {
            $this->em->remove($c);
        }
        $this->em->flush();

        // Récupérer combattants
        $combattants = $discipline
            ? $this->combattantRepo->findBy(['discipline' => $discipline])
            : $this->combattantRepo->findAll();

        // Calculer scores
        $scores = [];
        foreach ($combattants as $combattant) {
            $total = $combattant->getWins() + $combattant->getLosses() + $combattant->getDraws();
            $winRate = $total > 0 ? ($combattant->getWins() / $total) : 0;
            $score = ($combattant->getWins() * 100)
                   + ($winRate * 1000)
                   + ($total * 10)
                   - ($combattant->getLosses() * 50);

            $scores[] = [
                'combattant' => $combattant,
                'score'      => round($score, 1),
                'discipline' => $combattant->getDiscipline(),
            ];
        }

        // Trier par score
        usort($scores, fn($a, $b) => $b['score'] <=> $a['score']);

        // Grouper par discipline pour le rang
        $rangs = [];
        foreach ($scores as $data) {
            $disc = $data['discipline'];
            if (!isset($rangs[$disc])) $rangs[$disc] = 1;

            $classement = new Classement();
            $classement->setCombattant($data['combattant']);
            $classement->setScore($data['score']);
            $classement->setDiscipline($disc);
            $classement->setRang($rangs[$disc]++);

            $this->em->persist($classement);
        }

        $this->em->flush();
    }
}