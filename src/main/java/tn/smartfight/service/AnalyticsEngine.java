package tn.smartfight.service;

import tn.smartfight.config.DBConnection;
import tn.smartfight.dao.FightStatisticDao;
import tn.smartfight.model.FightStatistic;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AnalyticsEngine {
    private static final Logger LOG = Logger.getLogger(AnalyticsEngine.class.getName());

    private final DataSource dataSource;
    private final FightStatisticDao statDao;

    public AnalyticsEngine() { this(DBConnection.getDataSource()); }
    public AnalyticsEngine(DataSource ds) {
        this.dataSource = ds;
        this.statDao = new FightStatisticDao(ds);
    }

    public double calculatePerformanceScore(int fighterId) {
        // findSummedByFighter returns one row per completed fight, with aggregated stats
        // commentary field encodes: isWinner|method|isBelt
        List<FightStatistic> fights = statDao.findSummedByFighter(fighterId);
        if (fights.isEmpty()) return 0.0;

        double accumulator = 0.0;
        for (FightStatistic stat : fights) {
            double punchAcc  = stat.getPunchAccuracy();
            double powerAcc  = stat.getPowerAccuracy();
            double kdBonus   = Math.min(stat.getKnockdowns() * 15.0, 45.0);
            double fightScore = punchAcc * 0.5 + powerAcc * 0.3 + kdBonus * 0.2;

            String[] parts = stat.getCommentary() != null ? stat.getCommentary().split("\\|") : new String[0];
            boolean isWinner = parts.length > 0 && "true".equals(parts[0]);
            String method    = parts.length > 1 ? parts[1] : "";
            boolean isBelt   = parts.length > 2 && "true".equals(parts[2]);

            if (!isWinner && "DQ".equals(method)) fightScore -= 50.0;
            if (isBelt) fightScore *= isWinner ? 1.5 : 1.25;

            accumulator += fightScore;
        }

        double avg = accumulator / fights.size();
        return BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public void recalculateAndPersist(int fighterId) {
        double score = calculatePerformanceScore(fighterId);
        String sql = "UPDATE fighters SET performanceScore=? WHERE fighterId=?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, score);
            ps.setInt(2, fighterId);
            ps.executeUpdate();
            LOG.info("Updated performanceScore=" + score + " for fighterId=" + fighterId);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "recalculateAndPersist failed fighterId=" + fighterId, e);
        }
    }
}
