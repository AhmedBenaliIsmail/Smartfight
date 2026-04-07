package service;

import java.util.List;

import dao.FightStatisticDao;
import dao.FightResultDao;
import dao.FighterDao;
import model.FightStatistic;
import model.FightResult;

public class FightStatisticService {
    private FightStatisticDao statisticDao;
    private FightResultDao fightResultDao;
    private FighterDao fighterDao;
    private PerformanceScoreService performanceScoreService;

    public FightStatisticService() {
        this.statisticDao = new FightStatisticDao();
        this.fightResultDao = new FightResultDao();
        this.fighterDao = new FighterDao();
        this.performanceScoreService = new PerformanceScoreService();
    }

    /**
     * Add statistics for a fighter in a specific fight.
     */
    public boolean addFightStatistic(FightStatistic stat) {
        // Validate the statistic
        if (stat.getFightResultId() <= 0 || stat.getFighterId() <= 0) {
            throw new IllegalArgumentException("Fight result ID and fighter ID must be positive.");
        }
        if (stat.getStrikesLanded() < 0 || stat.getStrikesThrown() < 0 ||
            stat.getTakedowns() < 0 || stat.getSubmissions() < 0 || stat.getKnockdowns() < 0) {
            throw new IllegalArgumentException("Statistics cannot be negative.");
        }
        if (stat.getStrikesLanded() > stat.getStrikesThrown()) {
            throw new IllegalArgumentException("Strikes landed cannot exceed strikes thrown.");
        }

        // Check that the fight result exists and is completed
        FightResult fight = fightResultDao.getFightResultById(stat.getFightResultId());
        if (fight == null) {
            throw new IllegalArgumentException("Fight result not found.");
        }
        if (!"COMPLETED".equals(fight.getStatus())) {
            throw new IllegalStateException("Cannot add statistics for a fight that is not completed.");
        }

        // Check that the fighter belongs to this fight
        if (stat.getFighterId() != fight.getFighter1Id() && stat.getFighterId() != fight.getFighter2Id()) {
            throw new IllegalArgumentException("Fighter is not part of this fight.");
        }

        // Check if statistics already exist for this fighter in this fight
        var existing = statisticDao.getStatisticsByFightResult(stat.getFightResultId());
        boolean alreadyExists = existing.stream().anyMatch(s -> s.getFighterId() == stat.getFighterId());
        if (alreadyExists) {
            throw new IllegalStateException("Statistics for this fighter already exist for this fight.");
        }

        boolean success = statisticDao.addFightStatistic(stat);
        if (success) {
            // After adding statistics, recalculate performance score for this fighter
            performanceScoreService.recalculatePerformanceScore(stat.getFighterId());
        }
        return success;
    }

    /**
     * Get statistics for a fight result.
     */
    public java.util.List<FightStatistic> getStatisticsByFightResult(int fightResultId) {
        if (fightResultId <= 0) {
            throw new IllegalArgumentException("Invalid fight result ID.");
        }
        return statisticDao.getStatisticsByFightResult(fightResultId);
    }

    /**
     * Get all statistics for a fighter.
     */
    public java.util.List<FightStatistic> getStatisticsByFighter(int fighterId) {
        if (fighterId <= 0) {
            throw new IllegalArgumentException("Invalid fighter ID.");
        }
        return statisticDao.getStatisticsByFighter(fighterId);
    }

    /**
     * Update an existing statistic record.
     */
    public boolean updateFightStatistic(FightStatistic stat) {
        if (stat.getId() <= 0) {
            throw new IllegalArgumentException("Statistic ID must be positive for update.");
        }
        // Validate as in add
        if (stat.getStrikesLanded() > stat.getStrikesThrown()) {
            throw new IllegalArgumentException("Strikes landed cannot exceed strikes thrown.");
        }
        // Ensure the statistic exists
        FightStatistic existing = statisticDao.getFightStatisticById(stat.getId());
        if (existing == null) {
            throw new IllegalArgumentException("Statistic record not found.");
        }
        // If fighter or fight result changed, need to revalidate consistency, but for simplicity we keep it.
        boolean success = statisticDao.updateFightStatistic(stat);
        if (success) {
            // Recalculate performance score for the fighter
            performanceScoreService.recalculatePerformanceScore(stat.getFighterId());
        }
        return success;
    }

    /**
     * Delete a statistic record.
     */
    public boolean deleteFightStatistic(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid statistic ID.");
        }
        FightStatistic stat = statisticDao.getFightStatisticById(id);
        if (stat == null) {
            throw new IllegalArgumentException("Statistic record not found.");
        }
        boolean success = statisticDao.deleteFightStatistic(id);
        if (success) {
            // Recalculate performance score for the fighter
            performanceScoreService.recalculatePerformanceScore(stat.getFighterId());
        }
        return success;
    }

    public List<FightStatistic> getAllStatistics() {
        return statisticDao.getAllStatistics();
    }
}