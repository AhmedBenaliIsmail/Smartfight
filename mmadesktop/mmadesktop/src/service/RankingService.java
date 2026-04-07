package service;

import dao.*;
import model.*;
import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * Advanced Ranking Service
 * - ELO rating calculation (like chess)
 * - Performance score from fight statistics
 * - Win streak bonus
 * - Strength of schedule (SOS)
 * - Automatic ranking update after fight completion
 */
public class RankingService {

    private final FighterDao fighterDao;
    private final FightResultDao fightResultDao;
    private final FightStatisticDao fightStatisticDao;

    // ELO constants
    private static final int K_FACTOR_NEW = 32;      // for fighters with < 5 fights
    private static final int K_FACTOR_ESTABLISHED = 16; // for experienced fighters
    private static final int BASE_RATING = 1500;

    public RankingService() {
        this.fighterDao = new FighterDao();
        this.fightResultDao = new FightResultDao();
        this.fightStatisticDao = new FightStatisticDao();
    }

    // ==================== PUBLIC API ====================

    /**
     * Complete a fight: update records, recalculate ELO, performance scores, win streaks, SOS,
     * and finally reorder the ranking table.
     * Call this when a fight status changes to "COMPLETED".
     */
    public void processCompletedFight(FightResult fight) {
        if (!"COMPLETED".equals(fight.getStatus())) return;

        int winnerId = fight.getWinnerId();
        int loserId = fight.getLoserId();

        // 1. Update win/loss/draw counts
        if (winnerId > 0) {
            fighterDao.incrementWins(winnerId);
            fighterDao.incrementLosses(loserId);
            updateWinStreak(winnerId, true);
            updateWinStreak(loserId, false);
        } else if (fight.isDraw()) {
            fighterDao.incrementDraws(fight.getFighter1Id());
            fighterDao.incrementDraws(fight.getFighter2Id());
            // Win streak resets for both
            updateWinStreak(fight.getFighter1Id(), false);
            updateWinStreak(fight.getFighter2Id(), false);
        }

        // 2. Update ELO ratings (only for non-draw)
        if (winnerId > 0) {
            updateEloRatings(fight);
        }

        // 3. Update performance scores for both fighters (based on fight statistics)
        updatePerformanceScore(winnerId > 0 ? winnerId : fight.getFighter1Id());
        updatePerformanceScore(winnerId > 0 ? loserId : fight.getFighter2Id());

        // 4. Update strength of schedule for both fighters
        updateStrengthOfSchedule(fight.getFighter1Id());
        updateStrengthOfSchedule(fight.getFighter2Id());

        // 5. Recompute global ranking order (persisted to ranking table if needed)
        recomputeRankingTable();
    }

    /**
     * Recompute the entire ranking for all fighters (e.g., after bulk changes).
     */
    public void recomputeAllRankings() {
        List<Fighter> fighters = fighterDao.getAllFighters();
        for (Fighter f : fighters) {
            updatePerformanceScore(f.getFighterId());
            updateWinStreak(f.getFighterId(), false); // recalc from history
            updateStrengthOfSchedule(f.getFighterId());
        }
        recomputeEloFromHistory(); // recalc ELO based on all past fights
        recomputeRankingTable();
    }

    /**
     * Get fighters sorted by ELO rating (primary) and performance score (secondary).
     */
    public List<Fighter> getRankedFighters() {
        List<Fighter> fighters = fighterDao.getAllFighters();
        fighters.sort((a, b) -> {
            int cmp = Double.compare(b.getEloRating(), a.getEloRating());
            if (cmp == 0) cmp = Double.compare(b.getPerformanceScore(), a.getPerformanceScore());
            return cmp;
        });
        return fighters;
    }

    /**
     * Get fighters ranked within a specific weight class.
     */
    public List<Fighter> getRankedFightersByWeightClass(String weightClass) {
        List<Fighter> fighters = fighterDao.getFightersByWeightClass(weightClass);
        fighters.sort((a, b) -> Double.compare(b.getEloRating(), a.getEloRating()));
        return fighters;
    }

    // ==================== PRIVATE CORE LOGIC ====================

    /**
     * Update ELO ratings for winner and loser based on the fight result and method bonus.
     */
    private void updateEloRatings(FightResult fight) {
        int winnerId = fight.getWinnerId();
        int loserId = fight.getLoserId();
        Fighter winner = fighterDao.getFighterById(winnerId);
        Fighter loser = fighterDao.getFighterById(loserId);
        if (winner == null || loser == null) return;

        double winnerRating = winner.getEloRating();
        double loserRating = loser.getEloRating();

        // Expected scores
        double expectedWinner = 1 / (1 + Math.pow(10, (loserRating - winnerRating) / 400));
        double expectedLoser = 1 - expectedWinner;

        // K factor based on experience
        int winnerTotalFights = winner.getTotalFights();
        int loserTotalFights = loser.getTotalFights();
        int kWinner = (winnerTotalFights < 5) ? K_FACTOR_NEW : K_FACTOR_ESTABLISHED;
        int kLoser = (loserTotalFights < 5) ? K_FACTOR_NEW : K_FACTOR_ESTABLISHED;

        // Actual scores: 1 for win, 0 for loss
        double actualWinner = 1.0;
        double actualLoser = 0.0;

        // Calculate new ratings
        double newWinnerRating = winnerRating + kWinner * (actualWinner - expectedWinner);
        double newLoserRating = loserRating + kLoser * (actualLoser - expectedLoser);

        // Add method bonus (winner only)
        int bonus = fight.getMethodBonus();
        newWinnerRating += bonus;

        // Cap ratings between 0 and 3000
        newWinnerRating = Math.max(0, Math.min(3000, newWinnerRating));
        newLoserRating = Math.max(0, Math.min(3000, newLoserRating));

        // Persist
        fighterDao.updateEloRating(winnerId, newWinnerRating);
        fighterDao.updateEloRating(loserId, newLoserRating);
    }

    /**
     * Recalculate ELO ratings from scratch using all historical fights (for consistency).
     */
    private void recomputeEloFromHistory() {
        // Reset all fighters to base rating
        List<Fighter> fighters = fighterDao.getAllFighters();
        for (Fighter f : fighters) {
            fighterDao.updateEloRating(f.getFighterId(), BASE_RATING);
        }

        // Get all completed fights in chronological order
        List<FightResult> fights = fightResultDao.getAllCompletedFights();
        fights.sort(Comparator.comparing(FightResult::getFightDate));

        // Process each fight in order
        for (FightResult fight : fights) {
            if (fight.getWinnerId() != null && fight.getWinnerId() > 0) {
                updateEloRatings(fight);
            }
        }
    }

    /**
     * Update win streak for a fighter based on their last fights.
     * @param fighterId the fighter
     * @param isWin true if we just recorded a win, false for loss/draw (recalc from scratch)
     */
    private void updateWinStreak(int fighterId, boolean isWin) {
        if (!isWin) {
            // Recalculate from scratch
            List<FightResult> lastFights = fightResultDao.getLastFightsByFighter(fighterId, 10);
            int streak = 0;
            for (FightResult fight : lastFights) {
                if (fight.getWinnerId() != null && fight.getWinnerId() == fighterId) {
                    streak++;
                } else {
                    break;
                }
            }
            fighterDao.updateWinStreak(fighterId, streak);
        } else {
            // Increment streak (efficient after a win)
            Fighter f = fighterDao.getFighterById(fighterId);
            if (f != null) {
                fighterDao.updateWinStreak(fighterId, f.getWinStreak() + 1);
            }
        }
    }

    /**
     * Update performance score for a fighter based on all his fight statistics.
     * Score = average of (strike accuracy * 0.3 + takedown accuracy * 0.25 + subs*10*0.25 + kd*10*0.2)
     */
    private void updatePerformanceScore(int fighterId) {
        Fighter fighter = fighterDao.getFighterById(fighterId);
        if (fighter == null) {
            return;
        }

        List<FightStatistic> stats = fightStatisticDao.getStatisticsByFighter(fighterId);
        if (stats.isEmpty()) {
            fighterDao.updatePerformanceScore(fighterId, computeRecordBasedPerformance(fighter));
            return;
        }

        double totalScore = 0.0;
        int validFights = 0;
        for (FightStatistic stat : stats) {
            // Skip if no strikes thrown (avoid division by zero)
            if (stat.getStrikesThrown() == 0) continue;
            totalScore += stat.getFightPerformanceContribution();
            validFights++;
        }
        double avgScore = validFights > 0 ? totalScore / validFights : computeRecordBasedPerformance(fighter);
        avgScore = Math.max(0.0, Math.min(100.0, avgScore));
        fighterDao.updatePerformanceScore(fighterId, avgScore);
    }

    /**
     * Fallback score when detailed fight statistics are missing.
     * Keeps output meaningful instead of showing all fighters as zero.
     */
    private double computeRecordBasedPerformance(Fighter fighter) {
        int total = fighter.getTotalFights();
        if (total <= 0) {
            return 25.0; // baseline for fighters with no recorded fights yet
        }

        double winRate = (fighter.getWins() * 100.0) / total; // 0..100
        double finishRate = ((fighter.getKoWins() + fighter.getSubmissionWins()) * 100.0) / Math.max(1, fighter.getWins()); // 0..100+
        double streakScore = Math.min(30.0, fighter.getWinStreak() * 6.0); // 0..30
        double sosScore = Math.min(100.0, Math.max(0.0, (fighter.getStrengthOfSchedule() - 1200.0) / 8.0)); // roughly 0..100

        double score = (winRate * 0.45) + (Math.min(100.0, finishRate) * 0.25) + (streakScore * 0.15) + (sosScore * 0.15);
        return Math.max(0.0, Math.min(100.0, score));
    }

    /**
     * Update strength of schedule = average ELO rating of last 5 opponents.
     */
    private void updateStrengthOfSchedule(int fighterId) {
        List<FightResult> lastFights = fightResultDao.getLastFightsByFighter(fighterId, 5);
        if (lastFights.isEmpty()) {
            fighterDao.updateStrengthOfSchedule(fighterId, BASE_RATING);
            return;
        }

        double totalOpponentRating = 0.0;
        int count = 0;
        for (FightResult fight : lastFights) {
            int opponentId = (fight.getFighter1Id() == fighterId) ? fight.getFighter2Id() : fight.getFighter1Id();
            Fighter opp = fighterDao.getFighterById(opponentId);
            if (opp != null) {
                totalOpponentRating += opp.getEloRating();
                count++;
            }
        }
        double sos = count > 0 ? totalOpponentRating / count : BASE_RATING;
        fighterDao.updateStrengthOfSchedule(fighterId, sos);
    }

    /**
     * Persist the current ranking order into the `ranking` table.
     * (Assuming you have a ranking table with fighter_id, rank_position, points, season)
     */
    private void recomputeRankingTable() {
        List<Fighter> ranked = getRankedFighters();
        int position = 1;
        String currentSeason = "2026"; // or derive from date

        // First, clear existing rankings for the season (or update)
        try (Connection conn = app.util.DBCNX.getConnection();
             PreparedStatement delStmt = conn.prepareStatement("DELETE FROM ranking WHERE season = ?")) {
            delStmt.setString(1, currentSeason);
            delStmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert new rankings
        String insertSql = "INSERT INTO ranking (fighter_id, rank_position, points, season, updated_at) VALUES (?, ?, ?, ?, NOW())";
        try (Connection conn = app.util.DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
            for (Fighter f : ranked) {
                pstmt.setInt(1, f.getFighterId());
                pstmt.setInt(2, position++);
                // Points = ELO rating + performance score * 10 (example)
                double points = f.getEloRating() + (f.getPerformanceScore() * 10);
                pstmt.setDouble(3, points);
                pstmt.setString(4, currentSeason);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}