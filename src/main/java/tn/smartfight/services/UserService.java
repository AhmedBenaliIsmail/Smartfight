package tn.smartfight.services;

import tn.smartfight.database.DBConnection;
import tn.smartfight.models.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    private final Connection conn = DBConnection.getInstance().getConnection();

    private static final String BASE_SELECT =
        "SELECT u.id, u.first_name, u.last_name, u.email, u.phone, u.role_id, u.is_active, " +
        "       ur.name AS role_name " +
        "FROM user u JOIN user_role ur ON u.role_id = ur.id ";

    // ── READ ─────────────────────────────────────────────────────────────────

    public User getUserById(int id) {
        try (PreparedStatement ps = conn.prepareStatement(BASE_SELECT + "WHERE u.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("[UserService] getUserById: " + e.getMessage());
        }
        return null;
    }

    public User getUserByEmail(String email) {
        try (PreparedStatement ps = conn.prepareStatement(BASE_SELECT + "WHERE u.email = ?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("[UserService] getUserByEmail: " + e.getMessage());
        }
        return null;
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(BASE_SELECT + "ORDER BY u.last_name, u.first_name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapUser(rs));
        } catch (SQLException e) {
            System.err.println("[UserService] getAllUsers: " + e.getMessage());
        }
        return list;
    }

    public List<User> getFanUsers() {
        List<User> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(BASE_SELECT + "WHERE u.role_id = 5");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapUser(rs));
        } catch (SQLException e) {
            System.err.println("[UserService] getFanUsers: " + e.getMessage());
        }
        return list;
    }

    // ── CREATE ───────────────────────────────────────────────────────────────

    public boolean createUser(User u, String password) {
        String sql = "INSERT INTO user (first_name, last_name, email, password, phone, role_id, is_active) " +
                     "VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getFirstName());
            ps.setString(2, u.getLastName());
            ps.setString(3, u.getEmail());
            ps.setString(4, password);
            ps.setString(5, u.getPhone());
            ps.setInt(6, u.getRoleId());
            ps.setBoolean(7, u.isActive());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) u.setId(keys.getInt(1));
            }
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[UserService] createUser: " + e.getMessage());
            return false;
        }
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────

    public boolean updateUser(User u) {
        String sql = "UPDATE user SET first_name=?, last_name=?, email=?, phone=?, role_id=?, is_active=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getFirstName());
            ps.setString(2, u.getLastName());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getPhone());
            ps.setInt(5, u.getRoleId());
            ps.setBoolean(6, u.isActive());
            ps.setInt(7, u.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserService] updateUser: " + e.getMessage());
            return false;
        }
    }

    // ── DELETE ───────────────────────────────────────────────────────────────

    public boolean deleteUser(int id) {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM user WHERE id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserService] deleteUser: " + e.getMessage());
            return false;
        }
    }

    // ── HELPERS ──────────────────────────────────────────────────────────────

    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setFirstName(rs.getString("first_name"));
        u.setLastName(rs.getString("last_name"));
        u.setEmail(rs.getString("email"));
        u.setPhone(rs.getString("phone"));
        u.setRoleId(rs.getInt("role_id"));
        u.setActive(rs.getBoolean("is_active"));
        try { u.setRoleName(rs.getString("role_name")); } catch (SQLException ignored) {}
        return u;
    }
}
