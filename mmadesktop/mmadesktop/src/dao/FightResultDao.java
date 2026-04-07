package dao;

import app.util.DBCNX;
import model.FightResult;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FightResultDao {

    // ------------------- CREATE -------------------
    public boolean addFightResult(FightResult result) {
        String sql = "INSERT INTO fight_results (eventId, fightNumber, fighter1Id, fighter2Id, winnerId, methodOfVictory, roundNumber, fightDate, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, result.getEventId());
            pstmt.setInt(2, result.getFightNumber());
            pstmt.setInt(3, result.getFighter1Id());
            pstmt.setInt(4, result.getFighter2Id());

            if (result.getWinnerId() == null) {
                pstmt.setNull(5, Types.INTEGER);
            } else {
                pstmt.setInt(5, result.getWinnerId());
            }

            pstmt.setString(6, result.getMethodOfVictory());

            if (result.getRoundNumber() == 0) {
                pstmt.setNull(7, Types.INTEGER);
            } else {
                pstmt.setInt(7, result.getRoundNumber());
            }

            if (result.getFightDate() == null) {
                pstmt.setNull(8, Types.TIMESTAMP);
            } else {
                pstmt.setTimestamp(8, Timestamp.valueOf(result.getFightDate()));
            }

            pstmt.setString(9, result.getStatus());

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        result.setResultId(keys.getInt(1));
                    }
                }
            }
            return affected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ------------------- READ (single) -------------------
    public FightResult getFightResultById(int resultId) {
        String sql = "SELECT * FROM fight_results WHERE resultId = ?";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, resultId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapFightResult(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ------------------- READ (all) -------------------
    public List<FightResult> getAllFightResults() {
        List<FightResult> results = new ArrayList<>();
        String sql = "SELECT * FROM fight_results ORDER BY fightDate DESC";
        try (Connection conn = DBCNX.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                results.add(mapFightResult(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    public List<FightResult> getFightResultsByEvent(int eventId) {
        List<FightResult> results = new ArrayList<>();
        String sql = "SELECT * FROM fight_results WHERE eventId = ? ORDER BY fightNumber";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, eventId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapFightResult(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    // ========== NEW METHODS FOR RANKING SYSTEM ==========

    /**
     * Get last N fights for a specific fighter, ordered by date descending (most recent first).
     * Includes fights where fighter is either fighter1 or fighter2.
     */
    public List<FightResult> getLastFightsByFighter(int fighterId, int limit) {
        List<FightResult> results = new ArrayList<>();
        String sql = "SELECT * FROM fight_results WHERE (fighter1Id = ? OR fighter2Id = ?) " +
                     "AND status = 'COMPLETED' ORDER BY fightDate DESC LIMIT ?";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, fighterId);
            pstmt.setInt(2, fighterId);
            pstmt.setInt(3, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapFightResult(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    /**
     * Get all completed fights for a fighter (no limit) – used for performance score aggregation.
     */
    public List<FightResult> getAllCompletedFightsByFighter(int fighterId) {
        List<FightResult> results = new ArrayList<>();
        String sql = "SELECT * FROM fight_results WHERE (fighter1Id = ? OR fighter2Id = ?) " +
                     "AND status = 'COMPLETED' ORDER BY fightDate DESC";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, fighterId);
            pstmt.setInt(2, fighterId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapFightResult(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    /**
     * Get all completed fights (used for batch ranking updates).
     */
    public List<FightResult> getAllCompletedFights() {
        List<FightResult> results = new ArrayList<>();
        String sql = "SELECT * FROM fight_results WHERE status = 'COMPLETED' ORDER BY fightDate DESC";
        try (Connection conn = DBCNX.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                results.add(mapFightResult(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    /**
     * Check if a fighter has any completed fights.
     */
    public boolean hasCompletedFights(int fighterId) {
        String sql = "SELECT COUNT(*) FROM fight_results WHERE (fighter1Id = ? OR fighter2Id = ?) AND status = 'COMPLETED'";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, fighterId);
            pstmt.setInt(2, fighterId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ------------------- UPDATE -------------------
    public boolean updateFightResult(FightResult result) {
        String sql = "UPDATE fight_results SET eventId = ?, fightNumber = ?, fighter1Id = ?, fighter2Id = ?, " +
                     "winnerId = ?, methodOfVictory = ?, roundNumber = ?, fightDate = ?, status = ? " +
                     "WHERE resultId = ?";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, result.getEventId());
            pstmt.setInt(2, result.getFightNumber());
            pstmt.setInt(3, result.getFighter1Id());
            pstmt.setInt(4, result.getFighter2Id());

            if (result.getWinnerId() == null) {
                pstmt.setNull(5, Types.INTEGER);
            } else {
                pstmt.setInt(5, result.getWinnerId());
            }

            pstmt.setString(6, result.getMethodOfVictory());

            if (result.getRoundNumber() == 0) {
                pstmt.setNull(7, Types.INTEGER);
            } else {
                pstmt.setInt(7, result.getRoundNumber());
            }

            if (result.getFightDate() == null) {
                pstmt.setNull(8, Types.TIMESTAMP);
            } else {
                pstmt.setTimestamp(8, Timestamp.valueOf(result.getFightDate()));
            }

            pstmt.setString(9, result.getStatus());
            pstmt.setInt(10, result.getResultId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ------------------- DELETE -------------------
    public boolean deleteFightResult(int resultId) {
        String sql = "DELETE FROM fight_results WHERE resultId = ?";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, resultId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ------------------- MAPPER -------------------
    private FightResult mapFightResult(ResultSet rs) throws SQLException {
        FightResult r = new FightResult();
        r.setResultId(rs.getInt("resultId"));
        r.setEventId(rs.getInt("eventId"));
        r.setFightNumber(rs.getInt("fightNumber"));
        r.setFighter1Id(rs.getInt("fighter1Id"));
        r.setFighter2Id(rs.getInt("fighter2Id"));

        int winnerId = rs.getInt("winnerId");
        if (rs.wasNull()) {
            r.setWinnerId(null);
        } else {
            r.setWinnerId(winnerId);
        }

        r.setMethodOfVictory(rs.getString("methodOfVictory"));

        int round = rs.getInt("roundNumber");
        if (rs.wasNull()) {
            r.setRoundNumber(0);
        } else {
            r.setRoundNumber(round);
        }

        Timestamp ts = rs.getTimestamp("fightDate");
        r.setFightDate(ts != null ? ts.toLocalDateTime() : null);
        r.setStatus(rs.getString("status"));
        return r;
    }
}