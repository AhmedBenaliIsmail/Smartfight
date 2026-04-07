package dao;

import app.util.DBCNX;
import model.Fighter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FighterDao {

    // ------------------- CREATE -------------------
    public boolean addFighter(Fighter fighter) {
        String sql = "INSERT INTO fighters (firstName, lastName, nickname, weightClass, country, wins, losses, draws, koWins, submissionWins, decisionWins, eloRating, performanceScore, winStreak, strengthOfSchedule) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, fighter.getFirstName());
            pstmt.setString(2, fighter.getLastName());
            pstmt.setString(3, fighter.getNickname());
            pstmt.setString(4, fighter.getWeightClass());
            pstmt.setString(5, fighter.getCountry());
            pstmt.setInt(6, fighter.getWins());
            pstmt.setInt(7, fighter.getLosses());
            pstmt.setInt(8, fighter.getDraws());
            pstmt.setInt(9, fighter.getKoWins());
            pstmt.setInt(10, fighter.getSubmissionWins());
            pstmt.setInt(11, fighter.getDecisionWins());
            pstmt.setDouble(12, fighter.getEloRating());
            pstmt.setDouble(13, fighter.getPerformanceScore());
            pstmt.setInt(14, fighter.getWinStreak());
            pstmt.setDouble(15, fighter.getStrengthOfSchedule());

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        fighter.setFighterId(keys.getInt(1));
                    }
                }
            }
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ------------------- READ -------------------
    public Fighter getFighterById(int fighterId) {
        String sql = "SELECT * FROM fighters WHERE fighterId = ?";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, fighterId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapFighter(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Fighter> getAllFighters() {
        List<Fighter> fighters = new ArrayList<>();
        String sql = "SELECT * FROM fighters ORDER BY lastName";
        try (Connection conn = DBCNX.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                fighters.add(mapFighter(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return fighters;
    }

    public List<Fighter> getFightersByWeightClass(String weightClass) {
        List<Fighter> fighters = new ArrayList<>();
        String sql = "SELECT * FROM fighters WHERE weightClass = ? ORDER BY eloRating DESC";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, weightClass);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    fighters.add(mapFighter(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return fighters;
    }

    // ------------------- UPDATE (ranking related) -------------------
    public boolean updateEloRating(int fighterId, double newElo) {
        String sql = "UPDATE fighters SET eloRating = ? WHERE fighterId = ?";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newElo);
            pstmt.setInt(2, fighterId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePerformanceScore(int fighterId, double score) {
        String sql = "UPDATE fighters SET performanceScore = ? WHERE fighterId = ?";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, score);
            pstmt.setInt(2, fighterId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateWinStreak(int fighterId, int streak) {
        String sql = "UPDATE fighters SET winStreak = ? WHERE fighterId = ?";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, streak);
            pstmt.setInt(2, fighterId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStrengthOfSchedule(int fighterId, double sos) {
        String sql = "UPDATE fighters SET strengthOfSchedule = ? WHERE fighterId = ?";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, sos);
            pstmt.setInt(2, fighterId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ------------------- UPDATE (fight results) -------------------
    public boolean incrementWins(int fighterId) {
        String sql = "UPDATE fighters SET wins = wins + 1 WHERE fighterId = ?";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, fighterId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean incrementLosses(int fighterId) {
        String sql = "UPDATE fighters SET losses = losses + 1 WHERE fighterId = ?";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, fighterId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean incrementDraws(int fighterId) {
        String sql = "UPDATE fighters SET draws = draws + 1 WHERE fighterId = ?";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, fighterId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ------------------- UPDATE (full fighter) -------------------
    public boolean updateFighter(Fighter fighter) {
        String sql = "UPDATE fighters SET firstName = ?, lastName = ?, nickname = ?, weightClass = ?, country = ?, " +
                     "wins = ?, losses = ?, draws = ?, koWins = ?, submissionWins = ?, decisionWins = ?, " +
                     "eloRating = ?, performanceScore = ?, winStreak = ?, strengthOfSchedule = ? WHERE fighterId = ?";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, fighter.getFirstName());
            pstmt.setString(2, fighter.getLastName());
            pstmt.setString(3, fighter.getNickname());
            pstmt.setString(4, fighter.getWeightClass());
            pstmt.setString(5, fighter.getCountry());
            pstmt.setInt(6, fighter.getWins());
            pstmt.setInt(7, fighter.getLosses());
            pstmt.setInt(8, fighter.getDraws());
            pstmt.setInt(9, fighter.getKoWins());
            pstmt.setInt(10, fighter.getSubmissionWins());
            pstmt.setInt(11, fighter.getDecisionWins());
            pstmt.setDouble(12, fighter.getEloRating());
            pstmt.setDouble(13, fighter.getPerformanceScore());
            pstmt.setInt(14, fighter.getWinStreak());
            pstmt.setDouble(15, fighter.getStrengthOfSchedule());
            pstmt.setInt(16, fighter.getFighterId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ------------------- DELETE -------------------
    public boolean deleteFighter(int fighterId) {
        String sql = "DELETE FROM fighters WHERE fighterId = ?";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, fighterId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ------------------- UTILITY -------------------
    private Fighter mapFighter(ResultSet rs) throws SQLException {
        Fighter f = new Fighter();
        f.setFighterId(rs.getInt("fighterId"));
        f.setFirstName(rs.getString("firstName"));
        f.setLastName(rs.getString("lastName"));
        f.setNickname(rs.getString("nickname"));
        f.setWeightClass(rs.getString("weightClass"));
        f.setCountry(rs.getString("country"));
        f.setWins(rs.getInt("wins"));
        f.setLosses(rs.getInt("losses"));
        f.setDraws(rs.getInt("draws"));
        f.setKoWins(rs.getInt("koWins"));
        f.setSubmissionWins(rs.getInt("submissionWins"));
        f.setDecisionWins(rs.getInt("decisionWins"));
        
        // New fields – if column doesn't exist, default values are used
        try {
            f.setEloRating(rs.getDouble("eloRating"));
        } catch (SQLException e) { f.setEloRating(1500.0); }
        try {
            f.setPerformanceScore(rs.getDouble("performanceScore"));
        } catch (SQLException e) { f.setPerformanceScore(0.0); }
        try {
            f.setWinStreak(rs.getInt("winStreak"));
        } catch (SQLException e) { f.setWinStreak(0); }
        try {
            f.setStrengthOfSchedule(rs.getDouble("strengthOfSchedule"));
        } catch (SQLException e) { f.setStrengthOfSchedule(1500.0); }
        
        return f;
    }
}