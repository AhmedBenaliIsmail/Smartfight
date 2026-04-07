package service;

import dao.*;
import model.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AnalyticsEngine — auto-calculates performance scores & rankings
 * whenever a fight result is submitted. Never needs manual input.
 *
 * ── RANKING POINTS FORMULA ────────────────────────────────────────
 *  Base points per win:
 *    KO/TKO       = 100 pts  (bonus: +20 if in round 1)
 *    SUBMISSION   =  90 pts  (bonus: +10 if early round)
 *    DECISION     =  70 pts
 *    DRAW         =  20 pts each
 *    LOSS         =   0 pts  (but experience still accumulates)
 *  Recency decay:  fight_N_ago * (0.9 ^ N)  so recent wins worth more
 *
 * ── PERFORMANCE SCORE FORMULA ─────────────────────────────────────
 *  Uses weighted components (all 0-10 scale, then * 10 = 0-100):
 *   Aggression  (25%): strike accuracy + KO rate
 *   Defense     (25%): takedown defense + avg rounds survived
 *   Technique   (25%): submission rate + takedown success + accuracy
 *   Experience  (25%): total fights + win rate + finish rate
 *
 * ── AUTO-TRIGGER ──────────────────────────────────────────────────
 *  Call recalculateAll(fighterId) after any fight result is saved.
 *  It updates performance_score AND ranking tables automatically.
 */
public class AnalyticsEngine {

    private final FighterDao         fighterDao   = new FighterDao();
    private final FightResultDao     resultDao    = new FightResultDao();
    private final FightStatisticDao  statDao      = new FightStatisticDao();
    private final PerformanceScoreDao scoreDao    = new PerformanceScoreDao();
    private final RankingDao         rankingDao   = new RankingDao();

    private static final String SEASON = "2026";

    // ════════════════════════════════════════════
    // PUBLIC ENTRY POINT — call this after every
    // fight result submission
    // ════════════════════════════════════════════
    public void recalculateAll(int fighterId) {
        recalculatePerformanceScore(fighterId);
        recalculateRanking(fighterId);
    }

    public void recalculateBothFighters(int fighter1Id, int fighter2Id) {
        recalculateAll(fighter1Id);
        recalculateAll(fighter2Id);
    }

    // ════════════════════════════════════════════
    // PERFORMANCE SCORE
    // ════════════════════════════════════════════
    public void recalculatePerformanceScore(int fighterId) {
        Fighter fighter = fighterDao.getFighterById(fighterId);
        if (fighter == null) return;

        List<FightStatistic> stats = statDao.getStatisticsByFighter(fighterId);
        List<FightResult>    results = getCompletedResultsForFighter(fighterId);

        // ── Aggression (0–10) ─────────────────────────────────────
        // strike accuracy (0-1) * 5  +  KO rate * 5
        double strikeAccuracy = 0;
        double totalLanded = 0, totalThrown = 0;
        int knockdowns = 0;
        for (FightStatistic s : stats) {
            totalLanded += s.getStrikesLanded();
            totalThrown += s.getStrikesThrown();
            knockdowns  += s.getKnockdowns();
        }
        if (totalThrown > 0) strikeAccuracy = totalLanded / totalThrown;
        int totalFights = results.size();
        long koWins = results.stream()
            .filter(r -> r.getWinnerId() != null && r.getWinnerId() == fighterId)
            .filter(r -> r.getMethodOfVictory() != null &&
                         (r.getMethodOfVictory().equalsIgnoreCase("KO") ||
                          r.getMethodOfVictory().equalsIgnoreCase("TKO")))
            .count();
        double koRate = totalFights > 0 ? (double) koWins / totalFights : 0;
        double aggression = clamp((strikeAccuracy * 5.0) + (koRate * 5.0), 0, 10);

        // ── Defense (0–10) ────────────────────────────────────────
        // avg rounds survived + takedown defense proxy
        double avgRounds = 0;
        if (!results.isEmpty()) {
            double sumRounds = results.stream().mapToInt(FightResult::getRoundNumber).sum();
            avgRounds = sumRounds / results.size();
        }
        int totalTakedowns = stats.stream().mapToInt(FightStatistic::getTakedowns).sum();
        // takedown defense: 1 - (opponent takedowns / own fights), proxy using own takedowns as grappling activity
        double grappleRate = totalFights > 0 ? Math.min(totalTakedowns / (totalFights * 3.0), 1.0) : 0;
        double roundsScore = Math.min(avgRounds / 3.0, 1.0); // 3 rounds = full score
        double defense = clamp((roundsScore * 5.0) + (grappleRate * 5.0), 0, 10);

        // ── Technique (0–10) ──────────────────────────────────────
        // submission rate + takedown success per fight
        int submissions = stats.stream().mapToInt(FightStatistic::getSubmissions).sum();
        long subWins = results.stream()
            .filter(r -> r.getWinnerId() != null && r.getWinnerId() == fighterId)
            .filter(r -> r.getMethodOfVictory() != null &&
                         r.getMethodOfVictory().equalsIgnoreCase("SUBMISSION"))
            .count();
        double subRate = totalFights > 0 ? (double) subWins / totalFights : 0;
        double tdPerFight = totalFights > 0 ? Math.min(totalTakedowns / (double) totalFights, 5.0) / 5.0 : 0;
        double technique = clamp((strikeAccuracy * 3.0) + (subRate * 4.0) + (tdPerFight * 3.0), 0, 10);

        // ── Experience (0–10) ─────────────────────────────────────
        // total fights contribution + win rate
        long wins = results.stream()
            .filter(r -> r.getWinnerId() != null && r.getWinnerId() == fighterId)
            .count();
        double winRate = totalFights > 0 ? (double) wins / totalFights : 0;
        double fightsScore = Math.min(totalFights / 10.0, 1.0); // 10 fights = full experience score
        double experience = clamp((fightsScore * 5.0) + (winRate * 5.0), 0, 10);

        // ── Overall score (0–100) ─────────────────────────────────
        // Weighted: aggression 25%, defense 25%, technique 25%, experience 25%
        double overall = (aggression * 0.25 + defense * 0.25 + technique * 0.25 + experience * 0.25) * 10.0;
        overall = clamp(overall, 0, 100);

        // Save to DB
        PerformanceScore ps = new PerformanceScore();
        ps.setFighterId(fighterId);
        ps.setScore(round2(overall));
        ps.setAggression(round2(aggression * 10));   // store as 0-100
        ps.setDefense(round2(defense * 10));
        ps.setTechnique(round2(technique * 10));
        ps.setExperience(round2(experience * 10));
        ps.setCalculatedAt(LocalDateTime.now());

        PerformanceScore existing = scoreDao.getLatestScoreByFighter(fighterId);
        if (existing != null) {
            ps.setId(existing.getId());
            scoreDao.updatePerformanceScore(ps);
        } else {
            scoreDao.addPerformanceScore(ps);
        }
    }

    // ════════════════════════════════════════════
    // RANKING POINTS
    // ════════════════════════════════════════════
    public void recalculateRanking(int fighterId) {
        List<FightResult> results = getCompletedResultsForFighter(fighterId);
        // Sort by resultId ascending (oldest first) so we can apply recency decay
        results.sort(Comparator.comparingInt(FightResult::getResultId));

        double totalPoints = 0;
        int n = results.size();
        for (int i = 0; i < n; i++) {
            FightResult r = results.get(i);
            double recencyWeight = Math.pow(0.9, (n - 1 - i)); // newest fight = 0.9^0 = 1.0
            double pts = basePoints(r, fighterId);
            totalPoints += pts * recencyWeight;
        }
        totalPoints = round2(totalPoints);

        // Upsert ranking
        Ranking existing = rankingDao.getRankingByFighterAndSeason(fighterId, SEASON);
        if (existing != null) {
            existing.setPoints(totalPoints);
            rankingDao.updateRanking(existing);
        } else {
            Ranking newRank = new Ranking(fighterId, 999, totalPoints, SEASON);
            rankingDao.addRanking(newRank);
        }

        // Recompute rank positions for ALL fighters in this season
        recomputePositions();
    }

    /** Recompute rank_position for all fighters sorted by points desc */
    private void recomputePositions() {
        List<Ranking> all = rankingDao.getAllRankings().stream()
            .filter(r -> SEASON.equals(r.getSeason()))
            .sorted(Comparator.comparingDouble(Ranking::getPoints).reversed())
            .collect(Collectors.toList());

        for (int i = 0; i < all.size(); i++) {
            Ranking r = all.get(i);
            r.setRankPosition(i + 1);
            rankingDao.updateRanking(r);
        }
    }

    /** Calculate base ranking points for one fight result */
    private double basePoints(FightResult r, int fighterId) {
        if (r.getWinnerId() == null) {
            // draw
            return 20.0;
        }
        boolean won = r.getWinnerId() == fighterId;
        if (!won) return 0.0;

        String method = r.getMethodOfVictory() != null
            ? r.getMethodOfVictory().toUpperCase() : "";
        double base;
        switch (method) {
            case "KO":
            case "TKO":
                base = 100.0;
                if (r.getRoundNumber() == 1) base += 20; // early finish bonus
                break;
            case "SUBMISSION":
                base = 90.0;
                if (r.getRoundNumber() <= 2) base += 10;
                break;
            case "DECISION":
                base = 70.0;
                break;
            default:
                base = 60.0;
        }
        return base;
    }

    // ════════════════════════════════════════════
    // GETTERS for UI
    // ════════════════════════════════════════════
    public PerformanceScore getPerformanceScore(int fighterId) {
        return scoreDao.getLatestScoreByFighter(fighterId);
    }

    public List<PerformanceScore> getAllPerformanceScores() {
        return scoreDao.getAllScores();
    }

    public List<Ranking> getAllRankings() {
        return rankingDao.getAllRankings().stream()
            .filter(r -> SEASON.equals(r.getSeason()))
            .sorted(Comparator.comparingInt(Ranking::getRankPosition))
            .collect(Collectors.toList());
    }

    public Ranking getRankingForFighter(int fighterId) {
        return rankingDao.getRankingByFighterAndSeason(fighterId, SEASON);
    }

    // ════════════════════════════════════════════
    // HELPER
    // ════════════════════════════════════════════
    private List<FightResult> getCompletedResultsForFighter(int fighterId) {
        return resultDao.getAllFightResults().stream()
            .filter(r -> "COMPLETED".equalsIgnoreCase(r.getStatus()))
            .filter(r -> r.getFighter1Id() == fighterId || r.getFighter2Id() == fighterId)
            .collect(Collectors.toList());
    }

    private double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }

    private double round2(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
