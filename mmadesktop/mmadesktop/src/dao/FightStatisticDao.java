package dao;

import app.util.DBCNX;
import model.FightStatistic;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FightStatisticDao {

    // Insert a new fight statistic record (now includes takedownAttempts)
    public boolean addFightStatistic(FightStatistic stat) {
        String sql = "INSERT INTO fight_statistic (fight_result_id, fighter_id, strikes_landed, strikes_thrown, " +
                     "takedowns, takedownAttempts, submissions, knockdowns) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, stat.getFightResultId());
            pstmt.setInt(2, stat.getFighterId());
            pstmt.setInt(3, stat.getStrikesLanded());
            pstmt.setInt(4, stat.getStrikesThrown());
            pstmt.setInt(5, stat.getTakedowns());
            pstmt.setInt(6, stat.getTakedownAttempts());  // NEW
            pstmt.setInt(7, stat.getSubmissions());
            pstmt.setInt(8, stat.getKnockdowns());

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        stat.setId(keys.getInt(1));
                    }
                }
            }
            return affected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Retrieve a statistic by its ID
    public FightStatistic getFightStatisticById(int id) {
        String sql = "SELECT * FROM fight_statistic WHERE id = ?";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapFightStatistic(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Get all statistics for a specific fight result
    public List<FightStatistic> getStatisticsByFightResult(int fightResultId) {
        List<FightStatistic> list = new ArrayList<>();
        String sql = "SELECT * FROM fight_statistic WHERE fight_result_id = ? ORDER BY fighter_id";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, fightResultId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapFightStatistic(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Get all statistics for a specific fighter
    public List<FightStatistic> getStatisticsByFighter(int fighterId) {
        List<FightStatistic> list = new ArrayList<>();
        String sql = "SELECT * FROM fight_statistic WHERE fighter_id = ? ORDER BY id DESC";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, fighterId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapFightStatistic(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Update an existing statistic (includes takedownAttempts)
    public boolean updateFightStatistic(FightStatistic stat) {
        String sql = "UPDATE fight_statistic SET fight_result_id = ?, fighter_id = ?, strikes_landed = ?, strikes_thrown = ?, " +
                     "takedowns = ?, takedownAttempts = ?, submissions = ?, knockdowns = ? WHERE id = ?";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, stat.getFightResultId());
            pstmt.setInt(2, stat.getFighterId());
            pstmt.setInt(3, stat.getStrikesLanded());
            pstmt.setInt(4, stat.getStrikesThrown());
            pstmt.setInt(5, stat.getTakedowns());
            pstmt.setInt(6, stat.getTakedownAttempts());  // NEW
            pstmt.setInt(7, stat.getSubmissions());
            pstmt.setInt(8, stat.getKnockdowns());
            pstmt.setInt(9, stat.getId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete a statistic record by ID
    public boolean deleteFightStatistic(int id) {
        String sql = "DELETE FROM fight_statistic WHERE id = ?";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get all statistics (for admin use)
    public List<FightStatistic> getAllStatistics() {
        List<FightStatistic> list = new ArrayList<>();
        String sql = "SELECT * FROM fight_statistic ORDER BY id DESC";
        try (Connection conn = DBCNX.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapFightStatistic(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Helper method to map ResultSet to FightStatistic object (now includes takedownAttempts)
    private FightStatistic mapFightStatistic(ResultSet rs) throws SQLException {
        FightStatistic stat = new FightStatistic();
        stat.setId(rs.getInt("id"));
        stat.setFightResultId(rs.getInt("fight_result_id"));
        stat.setFighterId(rs.getInt("fighter_id"));
        stat.setStrikesLanded(rs.getInt("strikes_landed"));
        stat.setStrikesThrown(rs.getInt("strikes_thrown"));
        stat.setTakedowns(rs.getInt("takedowns"));
        stat.setTakedownAttempts(rs.getInt("takedownAttempts"));  // NEW
        stat.setSubmissions(rs.getInt("submissions"));
        stat.setKnockdowns(rs.getInt("knockdowns"));
        return stat;
    }
}