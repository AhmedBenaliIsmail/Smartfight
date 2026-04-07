package dao;

import app.util.DBCNX;
import model.PerformanceScore;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PerformanceScoreDao {

    // Insert a new performance score
    public boolean addPerformanceScore(PerformanceScore score) {
        String sql = "INSERT INTO performance_score (fighter_id, score, aggression, defense, technique, experience, calculated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, score.getFighterId());
            pstmt.setDouble(2, score.getScore());

            // Handle nullable Double fields
            setDoubleOrNull(pstmt, 3, score.getAggression());
            setDoubleOrNull(pstmt, 4, score.getDefense());
            setDoubleOrNull(pstmt, 5, score.getTechnique());
            setDoubleOrNull(pstmt, 6, score.getExperience());

            // calculated_at: if not set, use current time
            LocalDateTime calcAt = score.getCalculatedAt();
            if (calcAt == null) {
                calcAt = LocalDateTime.now();
            }
            pstmt.setTimestamp(7, Timestamp.valueOf(calcAt));

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        score.setId(keys.getInt(1));
                    }
                }
            }
            return affected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Retrieve performance score by ID
    public PerformanceScore getPerformanceScoreById(int id) {
        String sql = "SELECT * FROM performance_score WHERE id = ?";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapPerformanceScore(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Get all scores for a fighter (historical)
    public List<PerformanceScore> getScoresByFighter(int fighterId) {
        List<PerformanceScore> list = new ArrayList<>();
        String sql = "SELECT * FROM performance_score WHERE fighter_id = ? ORDER BY calculated_at DESC";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, fighterId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapPerformanceScore(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Get the most recent performance score for a fighter
    public PerformanceScore getLatestScoreByFighter(int fighterId) {
        String sql = "SELECT * FROM performance_score WHERE fighter_id = ? ORDER BY calculated_at DESC LIMIT 1";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, fighterId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapPerformanceScore(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Update an existing performance score
    public boolean updatePerformanceScore(PerformanceScore score) {
        String sql = "UPDATE performance_score SET fighter_id = ?, score = ?, aggression = ?, defense = ?, " +
                     "technique = ?, experience = ?, calculated_at = ? WHERE id = ?";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, score.getFighterId());
            pstmt.setDouble(2, score.getScore());
            setDoubleOrNull(pstmt, 3, score.getAggression());
            setDoubleOrNull(pstmt, 4, score.getDefense());
            setDoubleOrNull(pstmt, 5, score.getTechnique());
            setDoubleOrNull(pstmt, 6, score.getExperience());

            LocalDateTime calcAt = score.getCalculatedAt();
            if (calcAt == null) {
                calcAt = LocalDateTime.now();
            }
            pstmt.setTimestamp(7, Timestamp.valueOf(calcAt));
            pstmt.setInt(8, score.getId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete a performance score by ID
    public boolean deletePerformanceScore(int id) {
        String sql = "DELETE FROM performance_score WHERE id = ?";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Helper to set nullable Double
    private void setDoubleOrNull(PreparedStatement pstmt, int index, Double value) throws SQLException {
        if (value == null) {
            pstmt.setNull(index, Types.DOUBLE);
        } else {
            pstmt.setDouble(index, value);
        }
    }

    // Helper to map ResultSet to PerformanceScore
    private PerformanceScore mapPerformanceScore(ResultSet rs) throws SQLException {
        PerformanceScore score = new PerformanceScore();
        score.setId(rs.getInt("id"));
        score.setFighterId(rs.getInt("fighter_id"));
        score.setScore(rs.getDouble("score"));

        double aggression = rs.getDouble("aggression");
        score.setAggression(rs.wasNull() ? null : aggression);
        double defense = rs.getDouble("defense");
        score.setDefense(rs.wasNull() ? null : defense);
        double technique = rs.getDouble("technique");
        score.setTechnique(rs.wasNull() ? null : technique);
        double experience = rs.getDouble("experience");
        score.setExperience(rs.wasNull() ? null : experience);

        Timestamp ts = rs.getTimestamp("calculated_at");
        score.setCalculatedAt(ts != null ? ts.toLocalDateTime() : null);
        return score;
    }

public List<PerformanceScore> getAllScores() {
    List<PerformanceScore> list = new ArrayList<>();
    String sql = "SELECT * FROM performance_score ORDER BY calculated_at DESC";
    try (Connection conn = DBCNX.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        while (rs.next()) {
            list.add(mapPerformanceScore(rs));
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return list;
}
}