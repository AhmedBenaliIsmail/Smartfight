package tn.smartfight.service;

import tn.smartfight.config.DBConnection;
import tn.smartfight.model.FightResult;
import tn.smartfight.model.Prediction;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PredictionService {
    private static final Logger LOG = Logger.getLogger(PredictionService.class.getName());

    public record WinProbability(double f1Prob, double f2Prob, double drawProb,
                                  String f1Name, String f2Name) {}

    private static class FighterStats {
        int id; double elo; int wins, losses, koWins, winStreak;
        double performanceScore; int height, reach, strikesThrown, strikesLanded;
    }

    private final DataSource dataSource;

    public PredictionService() { this(DBConnection.getDataSource()); }
    public PredictionService(DataSource ds) { this.dataSource = ds; }

    public WinProbability calculateWinProbability(int fight1Id, int fight2Id) {
        FighterStats f1 = loadStats(fight1Id);
        FighterStats f2 = loadStats(fight2Id);
        if (f1 == null || f2 == null) return null;
        return computeProbability(f1, f2);
    }

    public WinProbability calculateWinProbabilityForFight(int resultId) {
        String sql = "SELECT fighter1Id, fighter2Id FROM fight_results WHERE resultId=?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, resultId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return calculateWinProbability(rs.getInt("fighter1Id"), rs.getInt("fighter2Id"));
                }
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "calculateWinProbabilityForFight failed", e);
        }
        return null;
    }

    public int calculatePoints(Prediction prediction, FightResult result) {
        boolean isDraw = result.getWinnerId() == null;
        boolean predictedDraw = prediction.getPredictedWinnerId() == null;
        boolean correctWinner = isDraw ? predictedDraw
                : !predictedDraw && prediction.getPredictedWinnerId() == result.getWinnerId();
        if (!correctWinner) return 0;

        int points = 10;
        String method = result.getMethodOfVictory();
        String predMethod = prediction.getPredictedMethod();
        boolean correctMethod = method != null && predMethod != null
                && method.equalsIgnoreCase(predMethod);
        if (correctMethod) points += 10;

        boolean correctRound = "KO".equalsIgnoreCase(method)
                && prediction.getPredictedRound() != null
                && prediction.getPredictedRound() == result.getRoundNumber();
        if (correctRound) points += 20;

        if (correctMethod && ("DECISION".equalsIgnoreCase(method) || correctRound)) points += 10;

        return Math.min(points, 50);
    }

    private WinProbability computeProbability(FighterStats f1, FighterStats f2) {
        int tf1 = Math.max(f1.wins + f1.losses, 1);
        int tf2 = Math.max(f2.wins + f2.losses, 1);

        double elo1 = clamp(f1.elo, 800, 2500);
        double elo2 = clamp(f2.elo, 800, 2500);
        double eloNorm1 = (elo1 - 800) / 1700;
        double eloNorm2 = (elo2 - 800) / 1700;

        double winRate1 = (double) f1.wins / tf1;
        double winRate2 = (double) f2.wins / tf2;
        double koRate1  = (double) f1.koWins / tf1;
        double koRate2  = (double) f2.koWins / tf2;

        double sa1 = f1.strikesThrown > 0 ? Math.min((double) f1.strikesLanded / f1.strikesThrown, 1.0) : 0.5;
        double sa2 = f2.strikesThrown > 0 ? Math.min((double) f2.strikesLanded / f2.strikesThrown, 1.0) : 0.5;

        double form1 = Math.min(f1.winStreak, 10) / 10.0;
        double form2 = Math.min(f2.winStreak, 10) / 10.0;

        double perf1 = clamp(f1.performanceScore, 0, 100) / 100.0;
        double perf2 = clamp(f2.performanceScore, 0, 100) / 100.0;

        int h1 = f1.height > 0 ? f1.height : 175;
        int h2 = f2.height > 0 ? f2.height : 175;
        int r1 = f1.reach  > 0 ? f1.reach  : 175;
        int r2 = f2.reach  > 0 ? f2.reach  : 175;
        double physDiff = clamp((h1 - h2) + (r1 - r2), -60, 60);
        double phys1 = 0.5 + physDiff / 120.0;
        double phys2 = 1.0 - phys1;

        double score1 = 0.30 * eloNorm1 + 0.20 * winRate1 + 0.10 * koRate1
                      + 0.10 * sa1 + 0.10 * form1 + 0.10 * perf1 + 0.10 * phys1;
        double score2 = 0.30 * eloNorm2 + 0.20 * winRate2 + 0.10 * koRate2
                      + 0.10 * sa2 + 0.10 * form2 + 0.10 * perf2 + 0.10 * phys2;

        double eloDiff = Math.abs(elo1 - elo2);
        double drawProb = 4.0 + (eloDiff < 50 ? 2.0 : 0.0);
        double total = Math.max(score1 + score2, 0.001);
        double f1Prob = round1((score1 / total) * (100 - drawProb));
        double f2Prob = round1(100 - drawProb - f1Prob);

        return new WinProbability(f1Prob, f2Prob, drawProb, f1.toString(), f2.toString());
    }

    private FighterStats loadStats(int fighterId) {
        String sql = "SELECT fighterId, eloRating, wins, losses, koWins, winStreak, " +
                "performanceScore, height, reach, strikes_thrown, strikes_landed " +
                "FROM fighters WHERE fighterId=?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fighterId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    FighterStats s = new FighterStats();
                    s.id              = rs.getInt("fighterId");
                    s.elo             = rs.getDouble("eloRating");
                    s.wins            = rs.getInt("wins");
                    s.losses          = rs.getInt("losses");
                    s.koWins          = rs.getInt("koWins");
                    s.winStreak       = rs.getInt("winStreak");
                    s.performanceScore= rs.getDouble("performanceScore");
                    s.height          = rs.getInt("height");
                    s.reach           = rs.getInt("reach");
                    s.strikesThrown   = rs.getInt("strikes_thrown");
                    s.strikesLanded   = rs.getInt("strikes_landed");
                    return s;
                }
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "loadStats failed fighterId=" + fighterId, e);
        }
        return null;
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    private double round1(double v) {
        return BigDecimal.valueOf(v).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
