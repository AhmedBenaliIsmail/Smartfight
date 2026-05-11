package tn.smartfight.service;

import tn.smartfight.config.DBConnection;
import tn.smartfight.model.FightResult;
import tn.smartfight.model.Ranking;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RankingService {
    private static final Logger LOG = Logger.getLogger(RankingService.class.getName());
    private static final String[] ORGS = {"WBC", "WBA", "IBF", "WBO", "MEDIA"};
    private static final int TOP_PER_ORG = 16;

    private final DataSource dataSource;
    private final NotificationService notificationService;

    public RankingService() { this(DBConnection.getDataSource(), new NotificationService()); }
    public RankingService(DataSource ds) { this(ds, new NotificationService()); }
    public RankingService(DataSource ds, NotificationService ns) {
        this.dataSource = ds;
        this.notificationService = ns;
    }

    // ── Called from ResultFormController after a result is saved as COMPLETED ──

    public void processCompletedFight(int resultId) {
        FightResult fr;
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                fr = loadResult(conn, resultId);
                if (fr == null || !"COMPLETED".equals(fr.getStatus())) {
                    conn.rollback();
                    return;
                }
                processInternal(conn, fr);
                conn.commit();
                LOG.info("processCompletedFight complete for resultId=" + resultId);
            } catch (Exception e) {
                conn.rollback();
                LOG.log(Level.SEVERE, "processCompletedFight rolled back, resultId=" + resultId, e);
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "processCompletedFight connection error", e);
            return;
        }
        // Notify after transaction commits (separate connections — safe)
        try {
            String desc = buildFightDescription(resultId);
            notificationService.notifyRankingUpdate(desc);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Notification failed after processCompletedFight", e);
        }
    }

    private String buildFightDescription(int resultId) {
        String sql = "SELECT CONCAT(f1.firstName,' ',f1.lastName,' vs ',f2.firstName,' ',f2.lastName) AS desc " +
                "FROM fight_results fr " +
                "JOIN fighters f1 ON f1.fighterId=fr.fighter1Id " +
                "JOIN fighters f2 ON f2.fighterId=fr.fighter2Id " +
                "WHERE fr.resultId=?";
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, resultId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("desc");
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "buildFightDescription failed", e);
        }
        return "Fight #" + resultId;
    }

    // ── Full reset + replay ────────────────────────────────────────────────────

    public void recomputeAllRankings() {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Reset all fighter stats — Java fix: also zero wins/losses/draws/koWins
                try (PreparedStatement s = conn.prepareStatement(
                        "UPDATE fighters SET eloRating=800, performanceScore=0, winStreak=0, " +
                        "strengthOfSchedule=1000, decisionWins=0, technical_wins=0, titleDefenses=0, " +
                        "wins=0, losses=0, draws=0, koWins=0")) {
                    s.executeUpdate();
                }

                // Load all completed fights ordered chronologically
                List<FightResult> fights = new ArrayList<>();
                String sql = "SELECT resultId, fighter1Id, fighter2Id, winnerId, methodOfVictory, " +
                        "decision_type, roundNumber, is_belt_fight, status " +
                        "FROM fight_results WHERE status='COMPLETED' " +
                        "ORDER BY COALESCE(fightDate, updated_at) ASC, resultId ASC";
                try (PreparedStatement s = conn.prepareStatement(sql);
                     ResultSet rs = s.executeQuery()) {
                    while (rs.next()) {
                        FightResult fr = new FightResult();
                        fr.setResultId(rs.getInt("resultId"));
                        fr.setFighter1Id(rs.getInt("fighter1Id"));
                        fr.setFighter2Id(rs.getInt("fighter2Id"));
                        int wid = rs.getInt("winnerId");
                        fr.setWinnerId(rs.wasNull() ? null : wid);
                        fr.setMethodOfVictory(rs.getString("methodOfVictory"));
                        fr.setDecisionType(rs.getString("decision_type"));
                        fr.setRoundNumber(rs.getInt("roundNumber"));
                        fr.setBeltFight(rs.getInt("is_belt_fight") == 1);
                        fr.setStatus(rs.getString("status"));
                        fights.add(fr);
                    }
                }

                for (FightResult fr : fights) {
                    replayFight(conn, fr);
                }

                updateGlobalRankings(conn);
                conn.commit();
                LOG.info("recomputeAllRankings complete: replayed " + fights.size() + " fights");
            } catch (Exception e) {
                conn.rollback();
                LOG.log(Level.SEVERE, "recomputeAllRankings rolled back", e);
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "recomputeAllRankings connection error", e);
        }
    }

    // ── Read rankings for display ──────────────────────────────────────────────

    public List<Ranking> findAll() {
        String sql = "SELECT r.id, r.rank_position, r.organization, r.weight_division_id, " +
                "f.fighterId, CONCAT(f.firstName,' ',f.lastName) AS fighter_name " +
                "FROM ranking r " +
                "JOIN fighters f ON f.fighterId = r.fighter_id " +
                "ORDER BY r.organization, r.rank_position";
        List<Ranking> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement s = conn.prepareStatement(sql);
             ResultSet rs = s.executeQuery()) {
            while (rs.next()) {
                Ranking rank = new Ranking();
                rank.setId(rs.getInt("id"));
                rank.setRankPosition(rs.getInt("rank_position"));
                rank.setOrganization(rs.getString("organization"));
                int divId = rs.getInt("weight_division_id");
                rank.setWeightDivisionId(rs.wasNull() ? null : divId);
                rank.setFighterId(rs.getInt("fighterId"));
                rank.setFighterName(rs.getString("fighter_name"));
                list.add(rank);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "RankingService.findAll failed", e);
        }
        return list;
    }

    // ── Internal: process one fight incrementally ──────────────────────────────

    private void processInternal(Connection conn, FightResult fr) throws SQLException {
        int f1Id = fr.getFighter1Id();
        int f2Id = fr.getFighter2Id();
        Integer winnerId = fr.getWinnerId();
        boolean isDraw = winnerId == null || "DRAW".equalsIgnoreCase(fr.getMethodOfVictory());

        updateLastFightDate(conn, f1Id, LocalDate.now());
        updateLastFightDate(conn, f2Id, LocalDate.now());

        if (!isDraw) {
            int loserId = winnerId.equals(f1Id) ? f2Id : f1Id;
            incrementWin(conn, winnerId, fr.getMethodOfVictory(), fr.isBeltFight());
            incrementLoss(conn, loserId);

            double[] elos = calculateElo(conn, winnerId, loserId,
                    fr.getMethodOfVictory(), fr.getDecisionType(),
                    fr.getRoundNumber(), fr.isBeltFight());
            updateElo(conn, winnerId, elos[0]);
            updateElo(conn, loserId, Math.max(elos[1], 100.0));

            incrementWinStreak(conn, winnerId);
            resetWinStreak(conn, loserId);
        } else {
            incrementDraw(conn, f1Id);
            incrementDraw(conn, f2Id);
            resetWinStreak(conn, f1Id);
            resetWinStreak(conn, f2Id);
        }

        updateSOS(conn, f1Id, calculateSOS(conn, f1Id));
        updateSOS(conn, f2Id, calculateSOS(conn, f2Id));
        updateGlobalRankings(conn);
    }

    private void replayFight(Connection conn, FightResult fr) throws SQLException {
        int f1Id = fr.getFighter1Id();
        int f2Id = fr.getFighter2Id();
        Integer winnerId = fr.getWinnerId();
        boolean isDraw = winnerId == null || "DRAW".equalsIgnoreCase(fr.getMethodOfVictory());

        if (!isDraw) {
            int loserId = winnerId.equals(f1Id) ? f2Id : f1Id;
            incrementWin(conn, winnerId, fr.getMethodOfVictory(), fr.isBeltFight());
            incrementLoss(conn, loserId);
            double[] elos = calculateElo(conn, winnerId, loserId,
                    fr.getMethodOfVictory(), fr.getDecisionType(),
                    fr.getRoundNumber(), fr.isBeltFight());
            updateElo(conn, winnerId, elos[0]);
            updateElo(conn, loserId, Math.max(elos[1], 100.0));
            incrementWinStreak(conn, winnerId);
            resetWinStreak(conn, loserId);
        } else {
            incrementDraw(conn, f1Id);
            incrementDraw(conn, f2Id);
            resetWinStreak(conn, f1Id);
            resetWinStreak(conn, f2Id);
        }
    }

    // ── ELO formula (§5.10) ──────────────────────────────────────────────────

    private double[] calculateElo(Connection conn, int winnerId, int loserId,
                                   String method, String decisionType,
                                   int roundNumber, boolean beltFight) throws SQLException {
        FighterStats w = loadStats(conn, winnerId);
        FighterStats l = loadStats(conn, loserId);

        int kW = (w.wins + w.losses + w.draws) < 12 ? 60 : 32;
        int kL = (l.wins + l.losses + l.draws) < 12 ? 60 : 32;

        double expected = 1.0 / (1.0 + Math.pow(10.0, (l.elo - w.elo) / 400.0));

        double domBonus;
        String m = method == null ? "" : method.toUpperCase();
        if (m.equals("KO") || m.equals("TKO")) {
            domBonus = 1.5 + Math.max(0, 12 - roundNumber) * 0.05;
        } else if ("UD".equalsIgnoreCase(decisionType)) {
            domBonus = 1.2;
        } else {
            domBonus = 1.0;
        }

        double wChange = kW * (1.0 - expected) * domBonus;
        double lChange = kL * (0.0 - (1.0 - expected)) * (1.0 / domBonus);

        if (beltFight) {
            wChange *= 1.25;
            lChange *= 0.75;
        }

        return new double[]{round2(w.elo + wChange), round2(l.elo + lChange)};
    }

    // ── SOS formula (§5.10) ──────────────────────────────────────────────────

    private double calculateSOS(Connection conn, int fighterId) throws SQLException {
        String sql =
                "SELECT fr.winnerId, " +
                "CASE WHEN fr.fighter1Id=? THEN fr.fighter2Id ELSE fr.fighter1Id END AS oppId, " +
                "f.eloRating " +
                "FROM fight_results fr " +
                "JOIN fighters f ON f.fighterId = CASE WHEN fr.fighter1Id=? THEN fr.fighter2Id ELSE fr.fighter1Id END " +
                "WHERE (fr.fighter1Id=? OR fr.fighter2Id=?) AND fr.status='COMPLETED'";
        double total = 0;
        int count = 0;
        try (PreparedStatement s = conn.prepareStatement(sql)) {
            s.setInt(1, fighterId); s.setInt(2, fighterId);
            s.setInt(3, fighterId); s.setInt(4, fighterId);
            try (ResultSet rs = s.executeQuery()) {
                while (rs.next()) {
                    double oppElo = rs.getDouble("eloRating");
                    int wid = rs.getInt("winnerId");
                    boolean won = !rs.wasNull() && wid == fighterId;
                    total += won ? oppElo * 1.3 * (oppElo / 1200.0) : oppElo * 0.85;
                    count++;
                }
            }
        }
        return count > 0 ? round2(total / count) : 1000.0;
    }

    // ── Ranking points formula (§5.10) ───────────────────────────────────────

    public double getBoxingRankingPoints(FighterStats f) {
        double decay;
        if (f.lastFightDate == null) {
            decay = 0.5;
        } else {
            long days = ChronoUnit.DAYS.between(f.lastFightDate, LocalDate.now());
            if (days > 730) return 0.0;
            decay = days > 180 ? 1.0 - (days - 180.0) / 550.0 : 1.0;
        }
        double R = f.elo / 15.0 + f.performanceScore * 1.5;
        double S = (f.sos / 1200.0) * 80.0;
        double L = f.titleDefenses * 20.0 + f.winStreak * 2.0;
        return round2((R * 0.45 + S * 0.35 + L * 0.20) * Math.max(0.1, decay));
    }

    // ── Rebuild ranking table ────────────────────────────────────────────────

    public void updateGlobalRankings(Connection conn) throws SQLException {
        try (PreparedStatement del = conn.prepareStatement("DELETE FROM ranking")) {
            del.executeUpdate();
        }

        String qFighters = "SELECT fighterId, weight_division_id, eloRating, performanceScore, " +
                "winStreak, strengthOfSchedule, titleDefenses, last_fight_date " +
                "FROM fighters";
        Map<Integer, List<FighterStats>> byDiv = new LinkedHashMap<>();
        try (PreparedStatement s = conn.prepareStatement(qFighters);
             ResultSet rs = s.executeQuery()) {
            while (rs.next()) {
                FighterStats fs = new FighterStats();
                fs.id = rs.getInt("fighterId");
                int d = rs.getInt("weight_division_id");
                fs.weightDivisionId = rs.wasNull() ? null : d;
                fs.elo = rs.getDouble("eloRating");
                fs.performanceScore = rs.getDouble("performanceScore");
                fs.winStreak = rs.getInt("winStreak");
                fs.sos = rs.getDouble("strengthOfSchedule");
                fs.titleDefenses = rs.getInt("titleDefenses");
                java.sql.Date ld = rs.getDate("last_fight_date");
                fs.lastFightDate = ld != null ? ld.toLocalDate() : null;
                int key = fs.weightDivisionId == null ? 0 : fs.weightDivisionId;
                byDiv.computeIfAbsent(key, k -> new ArrayList<>()).add(fs);
            }
        }

        String ins = "INSERT INTO ranking (fighter_id, rank_position, organization, weight_division_id) " +
                "VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(ins)) {
            for (Map.Entry<Integer, List<FighterStats>> entry : byDiv.entrySet()) {
                Integer divId = entry.getKey() == 0 ? null : entry.getKey();
                List<FighterStats> fighters = entry.getValue();
                fighters.sort((a, b) -> Double.compare(getBoxingRankingPoints(b), getBoxingRankingPoints(a)));

                for (String org : ORGS) {
                    int pos = 1;
                    for (FighterStats fs : fighters) {
                        if (pos > TOP_PER_ORG) break;
                        if (getBoxingRankingPoints(fs) <= 0) break;
                        stmt.setInt(1, fs.id);
                        stmt.setInt(2, pos);
                        stmt.setString(3, org);
                        if (divId != null) stmt.setInt(4, divId);
                        else stmt.setNull(4, Types.INTEGER);
                        stmt.addBatch();
                        pos++;
                    }
                }
            }
            stmt.executeBatch();
        }
    }

    // ── DB helpers ────────────────────────────────────────────────────────────

    private FightResult loadResult(Connection conn, int id) throws SQLException {
        String sql = "SELECT resultId, fighter1Id, fighter2Id, winnerId, methodOfVictory, " +
                "decision_type, roundNumber, is_belt_fight, status " +
                "FROM fight_results WHERE resultId=?";
        try (PreparedStatement s = conn.prepareStatement(sql)) {
            s.setInt(1, id);
            try (ResultSet rs = s.executeQuery()) {
                if (rs.next()) {
                    FightResult fr = new FightResult();
                    fr.setResultId(rs.getInt("resultId"));
                    fr.setFighter1Id(rs.getInt("fighter1Id"));
                    fr.setFighter2Id(rs.getInt("fighter2Id"));
                    int wid = rs.getInt("winnerId");
                    fr.setWinnerId(rs.wasNull() ? null : wid);
                    fr.setMethodOfVictory(rs.getString("methodOfVictory"));
                    fr.setDecisionType(rs.getString("decision_type"));
                    fr.setRoundNumber(rs.getInt("roundNumber"));
                    fr.setBeltFight(rs.getInt("is_belt_fight") == 1);
                    fr.setStatus(rs.getString("status"));
                    return fr;
                }
            }
        }
        return null;
    }

    private FighterStats loadStats(Connection conn, int fighterId) throws SQLException {
        String sql = "SELECT eloRating, wins, losses, draws, winStreak, performanceScore, " +
                "strengthOfSchedule, titleDefenses, last_fight_date " +
                "FROM fighters WHERE fighterId=?";
        try (PreparedStatement s = conn.prepareStatement(sql)) {
            s.setInt(1, fighterId);
            try (ResultSet rs = s.executeQuery()) {
                if (rs.next()) {
                    FighterStats fs = new FighterStats();
                    fs.id = fighterId;
                    fs.elo = rs.getDouble("eloRating");
                    fs.wins = rs.getInt("wins");
                    fs.losses = rs.getInt("losses");
                    fs.draws = rs.getInt("draws");
                    fs.winStreak = rs.getInt("winStreak");
                    fs.performanceScore = rs.getDouble("performanceScore");
                    fs.sos = rs.getDouble("strengthOfSchedule");
                    fs.titleDefenses = rs.getInt("titleDefenses");
                    java.sql.Date ld = rs.getDate("last_fight_date");
                    fs.lastFightDate = ld != null ? ld.toLocalDate() : null;
                    return fs;
                }
            }
        }
        return new FighterStats();
    }

    private void updateLastFightDate(Connection conn, int id, LocalDate d) throws SQLException {
        try (PreparedStatement s = conn.prepareStatement(
                "UPDATE fighters SET last_fight_date=? WHERE fighterId=?")) {
            s.setObject(1, d); s.setInt(2, id); s.executeUpdate();
        }
    }

    private void incrementWin(Connection conn, int id, String method, boolean belt) throws SQLException {
        String m = method == null ? "" : method.toUpperCase();
        boolean ko = m.equals("KO") || m.equals("TKO");
        boolean dec = m.equals("DECISION") || m.equals("UD") || m.equals("SD") || m.equals("MD");
        StringBuilder sb = new StringBuilder("UPDATE fighters SET wins=wins+1");
        if (ko) sb.append(", koWins=koWins+1");
        if (dec) sb.append(", decisionWins=decisionWins+1");
        if (belt) sb.append(", titleDefenses=titleDefenses+1");
        sb.append(" WHERE fighterId=?");
        try (PreparedStatement s = conn.prepareStatement(sb.toString())) {
            s.setInt(1, id); s.executeUpdate();
        }
    }

    private void incrementLoss(Connection conn, int id) throws SQLException {
        try (PreparedStatement s = conn.prepareStatement(
                "UPDATE fighters SET losses=losses+1 WHERE fighterId=?")) {
            s.setInt(1, id); s.executeUpdate();
        }
    }

    private void incrementDraw(Connection conn, int id) throws SQLException {
        try (PreparedStatement s = conn.prepareStatement(
                "UPDATE fighters SET draws=draws+1 WHERE fighterId=?")) {
            s.setInt(1, id); s.executeUpdate();
        }
    }

    private void updateElo(Connection conn, int id, double elo) throws SQLException {
        try (PreparedStatement s = conn.prepareStatement(
                "UPDATE fighters SET eloRating=? WHERE fighterId=?")) {
            s.setDouble(1, round2(elo)); s.setInt(2, id); s.executeUpdate();
        }
    }

    private void incrementWinStreak(Connection conn, int id) throws SQLException {
        try (PreparedStatement s = conn.prepareStatement(
                "UPDATE fighters SET winStreak=winStreak+1 WHERE fighterId=?")) {
            s.setInt(1, id); s.executeUpdate();
        }
    }

    private void resetWinStreak(Connection conn, int id) throws SQLException {
        try (PreparedStatement s = conn.prepareStatement(
                "UPDATE fighters SET winStreak=0 WHERE fighterId=?")) {
            s.setInt(1, id); s.executeUpdate();
        }
    }

    private void updateSOS(Connection conn, int id, double sos) throws SQLException {
        try (PreparedStatement s = conn.prepareStatement(
                "UPDATE fighters SET strengthOfSchedule=? WHERE fighterId=?")) {
            s.setDouble(1, sos); s.setInt(2, id); s.executeUpdate();
        }
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    // ── Inner DTO ─────────────────────────────────────────────────────────────

    public static class FighterStats {
        public int id;
        public double elo = 1500;
        public int wins, losses, draws;
        public int winStreak;
        public double performanceScore;
        public double sos = 1500;
        public int titleDefenses;
        public Integer weightDivisionId;
        public LocalDate lastFightDate;
    }
}
