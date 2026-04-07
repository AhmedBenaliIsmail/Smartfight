package dao;

import app.util.DBCNX;
import model.Role;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoleDao {

    public Optional<Role> findById(int roleId) {
        String sql = "SELECT * FROM roles WHERE roleId = ?";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, roleId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRole(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public Optional<Role> findByName(String roleName) {
        String sql = "SELECT * FROM roles WHERE roleName = ?";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, roleName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRole(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public List<Role> findAll() {
        List<Role> roles = new ArrayList<>();
        String sql = "SELECT * FROM roles ORDER BY roleName";
        try (Connection conn = DBCNX.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                roles.add(mapRole(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roles;
    }

    private Role mapRole(ResultSet rs) throws SQLException {
        Role role = new Role();
        role.setRoleId(rs.getInt("roleId"));
        role.setRoleName(rs.getString("roleName"));
        return role;
    }
}