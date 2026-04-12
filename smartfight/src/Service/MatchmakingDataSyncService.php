<?php

namespace App\Service;

use App\Entity\Combattant;
use App\Repository\ClassementRepository;
use App\Repository\CombattantRepository;
use App\Repository\FighterRepository;
use Doctrine\ORM\EntityManagerInterface;

class MatchmakingDataSyncService
{
    public function __construct(
        private FighterRepository $fighterRepository,
        private CombattantRepository $combattantRepository,
        private ClassementRepository $classementRepository,
        private ClassementService $classementService,
        private EntityManagerInterface $entityManager,
    ) {}

    public function syncCombattantsFromFighters(): int
    {
        $existingKeys = [];

        foreach ($this->combattantRepository->findAll() as $combattant) {
            $key = $this->makeKey((string) $combattant->getNickname(), (string) $combattant->getWeightClass());
            $existingKeys[$key] = true;
        }

        $created = 0;

        foreach ($this->fighterRepository->findAll() as $fighter) {
            $nickname = trim((string) ($fighter->getNickname() ?: $fighter->getDisplayName()));
            $weightClass = trim((string) ($fighter->getWeightClass() ?: 'Unknown'));
            $key = $this->makeKey($nickname, $weightClass);

            if (isset($existingKeys[$key])) {
                continue;
            }

            $combattant = new Combattant();
            $combattant
                ->setNickname($nickname)
                ->setNationalite((string) ($fighter->getNationality() ?: 'Unknown'))
                ->setWeightClass($weightClass)
                ->setWins((int) $fighter->getWins())
                ->setLosses((int) $fighter->getLosses())
                ->setDraws((int) $fighter->getDraws())
                ->setDiscipline('MMA');

            $this->entityManager->persist($combattant);
            $existingKeys[$key] = true;
            ++$created;
        }

        if ($created > 0) {
            $this->entityManager->flush();
        }

        return $created;
    }

    public function ensureClassementsGenerated(?string $discipline = null): bool
    {
        if ($this->classementRepository->count([]) > 0 || $this->combattantRepository->count([]) === 0) {
            return false;
        }

        $this->classementService->recalculer($discipline);

        return true;
    }

    private function makeKey(string $nickname, string $weightClass): string
    {
        return strtolower(trim($nickname) . '|' . trim($weightClass));
    }
}
