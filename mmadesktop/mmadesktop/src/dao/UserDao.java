package dao;

import app.util.DBCNX;
import model.Role;
import model.User;
import java.sql.*;
import java.util.*;

public class UserDao {

    public User createUser(User user) {
        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());

            int affected = pstmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setUserId(keys.getInt(1));
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
            return user;
        } catch (SQLException e) {
            throw new RuntimeException("Error creating user", e);
        }
    }

    public Optional<User> findById(Integer userId) {
        String sql = "SELECT * FROM users WHERE userId = ?";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = mapUser(rs);
                    user.setRoles(new HashSet<>(getUserRoles(userId)));
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by ID", e);
        }
        return Optional.empty();
    }

    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = mapUser(rs);
                    user.setRoles(new HashSet<>(getUserRoles(user.getUserId())));
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by username", e);
        }
        return Optional.empty();
    }

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = mapUser(rs);
                    user.setRoles(new HashSet<>(getUserRoles(user.getUserId())));
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by email", e);
        }
        return Optional.empty();
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection conn = DBCNX.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                User user = mapUser(rs);
                user.setRoles(new HashSet<>(getUserRoles(user.getUserId())));
                users.add(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all users", e);
        }
        return users;
    }

    public boolean updateUser(User user) {
        String sql = "UPDATE users SET username = ?, password = ?, email = ? WHERE userId = ?";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setInt(4, user.getUserId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating user", e);
        }
    }

    public boolean deleteUser(Integer userId) {
        String sql = "DELETE FROM users WHERE userId = ?";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user", e);
        }
    }

    public boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking username existence", e);
        }
        return false;
    }

    public boolean assignRoleToUser(Integer userId, Integer roleId) {
        String sql = "INSERT INTO user_roles (userId, roleId) VALUES (?, ?)";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, roleId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error assigning role to user", e);
        }
    }

    public boolean removeRoleFromUser(Integer userId, Integer roleId) {
        String sql = "DELETE FROM user_roles WHERE userId = ? AND roleId = ?";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, roleId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error removing role from user", e);
        }
    }

    public List<Role> getUserRoles(Integer userId) {
        List<Role> roles = new ArrayList<>();
        String sql = "SELECT r.roleId, r.roleName FROM roles r " +
                     "INNER JOIN user_roles ur ON r.roleId = ur.roleId " +
                     "WHERE ur.userId = ?";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Role role = new Role();
                    role.setRoleId(rs.getInt("roleId"));
                    role.setRoleName(rs.getString("roleName"));
                    roles.add(role);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting user roles", e);
        }
        return roles;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("userId"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));

        Timestamp ts = rs.getTimestamp("createdDate");
        if (ts != null) {
            user.setCreatedDate(ts.toLocalDateTime());
        }
        return user;
    }
}