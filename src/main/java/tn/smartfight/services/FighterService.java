package tn.smartfight.services;

import tn.smartfight.database.DBConnection;
import tn.smartfight.models.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FighterService {

    private final Connection conn = DBConnection.getInstance().getConnection();

    private static final String BASE_SELECT =
        "SELECT f.id, u.first_name, u.last_name, f.nickname, " +
        "       wc.name AS weight_class, f.wins, f.losses, f.draws, " +
        "       f.status, f.nationality, f.date_of_birth, f.weight_class_id, f.user_id " +
        "FROM fighter f " +
        "JOIN user u ON f.user_id = u.id " +
        "LEFT JOIN weight_class wc ON f.weight_class_id = wc.id ";

    // ── READ ─────────────────────────────────────────────────────────────────

    public List<Fighter> getAllActiveFighters() {
        return query(BASE_SELECT + "WHERE f.status = 'ACTIVE' ORDER BY u.last_name, u.first_name");
    }

    public List<Fighter> getAllFighters() {
        return query(BASE_SELECT + "ORDER BY u.last_name, u.first_name");
    }

    public Fighter getFighterById(int id) {
        String sql = BASE_SELECT + "WHERE f.id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapFighter(rs);
            }
        } catch (SQLException e) {
            System.err.println("[FighterService] getFighterById: " + e.getMessage());
        }
        return null;
    }

    public Fighter getFighterByUserId(int userId) {
        String sql = BASE_SELECT + "WHERE f.user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapFighter(rs);
            }
        } catch (SQLException e) {
            System.err.println("[FighterService] getFighterByUserId: " + e.getMessage());
        }
        return null;
    }

    // ── CREATE ───────────────────────────────────────────────────────────────

    public boolean createFighter(Fighter f) {
        String sql = "INSERT INTO fighter (user_id, nickname, date_of_birth, nationality, " +
                     "weight_class_id, wins, losses, draws, status) VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, f.getUserId());
            ps.setString(2, f.getNickname());
            ps.setString(3, f.getDateOfBirth());
            ps.setString(4, f.getNationality());
            if (f.getWeightClassId() > 0) ps.setInt(5, f.getWeightClassId());
            else ps.setNull(5, Types.INTEGER);
            ps.setInt(6, f.getWins());
            ps.setInt(7, f.getLosses());
            ps.setInt(8, f.getDraws());
            ps.setString(9, f.getStatus() != null ? f.getStatus() : "ACTIVE");
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) f.setId(keys.getInt(1));
            }
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[FighterService] createFighter: " + e.getMessage());
            return false;
        }
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────

    public boolean updateFighter(Fighter f) {
        String sql = "UPDATE fighter SET nickname=?, date_of_birth=?, nationality=?, " +
                     "weight_class_id=?, wins=?, losses=?, draws=?, status=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, f.getNickname());
            ps.setString(2, f.getDateOfBirth());
            ps.setString(3, f.getNationality());
            if (f.getWeightClassId() > 0) ps.setInt(4, f.getWeightClassId());
            else ps.setNull(4, Types.INTEGER);
            ps.setInt(5, f.getWins());
            ps.setInt(6, f.getLosses());
            ps.setInt(7, f.getDraws());
            ps.setString(8, f.getStatus() != null ? f.getStatus() : "ACTIVE");
            ps.setInt(9, f.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[FighterService] updateFighter: " + e.getMessage());
            return false;
        }
    }

    // ── DELETE ───────────────────────────────────────────────────────────────

    public boolean deleteFighter(int id) {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM fighter WHERE id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[FighterService] deleteFighter: " + e.getMessage());
            return false;
        }
    }

    // ── HELPERS ──────────────────────────────────────────────────────────────

    private List<Fighter> query(String sql) {
        List<Fighter> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapFighter(rs));
        } catch (SQLException e) {
            System.err.println("[FighterService] query: " + e.getMessage());
        }
        return list;
    }

    private Fighter mapFighter(ResultSet rs) throws SQLException {
        Fighter f = new Fighter();
        f.setId(rs.getInt("id"));
        f.setUserId(rs.getInt("user_id"));
        f.setFirstName(rs.getString("first_name"));
        f.setLastName(rs.getString("last_name"));
        f.setNickname(rs.getString("nickname"));
        f.setWeightClass(rs.getString("weight_class"));
        f.setWins(rs.getInt("wins"));
        f.setLosses(rs.getInt("losses"));
        f.setDraws(rs.getInt("draws"));
        f.setStatus(rs.getString("status"));
        f.setNationality(rs.getString("nationality"));
        f.setDateOfBirth(rs.getString("date_of_birth"));
        f.setWeightClassId(rs.getInt("weight_class_id"));
        return f;
    }
}
