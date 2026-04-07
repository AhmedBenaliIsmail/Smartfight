package service;

import dao.PerformanceScoreDao;
import dao.FightStatisticDao;
import dao.FighterDao;
import model.PerformanceScore;
import model.FightStatistic;
import java.time.LocalDateTime;
import java.util.List;

public class PerformanceScoreService {
    private PerformanceScoreDao scoreDao;
    private FightStatisticDao statisticDao;
    private FighterDao fighterDao;

    public PerformanceScoreService() {
        this.scoreDao = new PerformanceScoreDao();
        this.statisticDao = new FightStatisticDao();
        this.fighterDao = new FighterDao();
    }

    /**
     * Recalculate performance score for a fighter based on their last 5 fights.
     * This method should be called after a fight result or statistics change.
     */
    public boolean recalculatePerformanceScore(int fighterId) {
        if (fighterId <= 0) {
            throw new IllegalArgumentException("Invalid fighter ID.");
        }

        // Get all statistics for the fighter, ordered by most recent (assuming latest fight = highest id)
        List<FightStatistic> allStats = statisticDao.getStatisticsByFighter(fighterId);
        if (allStats.isEmpty()) {
            // No statistics yet, we may still create a default score
            return false;
        }

        // Take up to 5 most recent
        int limit = Math.min(allStats.size(), 5);
        List<FightStatistic> recent = allStats.subList(0, limit);

        double totalScore = 0.0;
        double totalAggression = 0.0;
        double totalDefense = 0.0;
        double totalTechnique = 0.0;
        double totalExperience = 0.0;
        int count = recent.size();

        for (FightStatistic stat : recent) {
            // Aggression: knockdowns * 10 + (strike accuracy) * 100 (normalized between 0-100)
            double strikeAccuracy = stat.getStrikesThrown() == 0 ? 0 :
                    (stat.getStrikesLanded() * 100.0) / stat.getStrikesThrown();
            double aggression = (stat.getKnockdowns() * 10.0) + strikeAccuracy;
            aggression = Math.min(100.0, aggression); // cap at 100

            // Defense: (1 - opponent's strike accuracy) but we don't have opponent stats here.
            // For simplicity, we use a placeholder – could be computed from opponent's stats.
            // In a real system, you might need to query the opponent's stats for the same fight.
            double defense = 50.0; // placeholder

            // Technique: submissions * 10 + takedowns * 5, capped at 100
            double technique = (stat.getSubmissions() * 10.0) + (stat.getTakedowns() * 5.0);
            technique = Math.min(100.0, technique);

            // Experience: could be based on total fights, but we'll use a simple ratio (wins+losses+draws)
            // For now, placeholder: 50 + (totalFights/10) maybe.
            int totalFights = fighterDao.getFighterById(fighterId).getTotalFights();
            double experience = 50.0 + (totalFights / 10.0);
            experience = Math.min(100.0, experience);

            // Overall score is average of components (you can weight differently)
            double fightScore = (aggression + defense + technique + experience) / 4.0;
            totalScore += fightScore;
            totalAggression += aggression;
            totalDefense += defense;
            totalTechnique += technique;
            totalExperience += experience;
        }

        double avgScore = totalScore / count;
        double avgAggression = totalAggression / count;
        double avgDefense = totalDefense / count;
        double avgTechnique = totalTechnique / count;
        double avgExperience = totalExperience / count;

        PerformanceScore newScore = new PerformanceScore();
        newScore.setFighterId(fighterId);
        newScore.setScore(avgScore);
        newScore.setAggression(avgAggression);
        newScore.setDefense(avgDefense);
        newScore.setTechnique(avgTechnique);
        newScore.setExperience(avgExperience);
        newScore.setCalculatedAt(LocalDateTime.now());

        // Check if there is already a latest score; if yes, update it; else insert new.
        PerformanceScore existing = scoreDao.getLatestScoreByFighter(fighterId);
        if (existing != null) {
            newScore.setId(existing.getId());
            return scoreDao.updatePerformanceScore(newScore);
        } else {
            return scoreDao.addPerformanceScore(newScore);
        }
    }

    /**
     * Get the most recent performance score for a fighter.
     */
    public PerformanceScore getLatestScore(int fighterId) {
        if (fighterId <= 0) {
            throw new IllegalArgumentException("Invalid fighter ID.");
        }
        return scoreDao.getLatestScoreByFighter(fighterId);
    }

    /**
     * Get all historical performance scores for a fighter.
     */
    public List<PerformanceScore> getScoreHistory(int fighterId) {
        if (fighterId <= 0) {
            throw new IllegalArgumentException("Invalid fighter ID.");
        }
        return scoreDao.getScoresByFighter(fighterId);
    }

    /**
     * Manually add a performance score (useful for seeding or corrections).
     */
    public boolean addPerformanceScore(PerformanceScore score) {
        if (score.getFighterId() <= 0) {
            throw new IllegalArgumentException("Fighter ID must be positive.");
        }
        // Optional: check that fighter exists
        if (fighterDao.getFighterById(score.getFighterId()) == null) {
            throw new IllegalArgumentException("Fighter does not exist.");
        }
        if (score.getScore() < 0 || score.getScore() > 100) {
            throw new IllegalArgumentException("Score must be between 0 and 100.");
        }
        // Allow nulls for component scores
        return scoreDao.addPerformanceScore(score);
    }

    /**
     * Update an existing performance score.
     */
    public boolean updatePerformanceScore(PerformanceScore score) {
        if (score.getId() <= 0) {
            throw new IllegalArgumentException("Score ID must be positive for update.");
        }
        // Validate score range
        if (score.getScore() < 0 || score.getScore() > 100) {
            throw new IllegalArgumentException("Score must be between 0 and 100.");
        }
        // Ensure it exists
        PerformanceScore existing = scoreDao.getPerformanceScoreById(score.getId());
        if (existing == null) {
            throw new IllegalArgumentException("Performance score not found.");
        }
        return scoreDao.updatePerformanceScore(score);
    }

    /**
     * Delete a performance score.
     */
    public boolean deletePerformanceScore(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid score ID.");
        }
        return scoreDao.deletePerformanceScore(id);
    }public List<PerformanceScore> getAllScores() throws Exception {
        return scoreDao.getAllScores();
    }
}