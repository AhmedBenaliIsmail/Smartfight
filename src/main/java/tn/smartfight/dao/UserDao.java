package tn.smartfight.dao;

import tn.smartfight.config.DBConnection;
import tn.smartfight.model.User;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDao {
    private static final Logger LOG = Logger.getLogger(UserDao.class.getName());

    private final DataSource dataSource;

    public UserDao() { this(DBConnection.getDataSource()); }
    public UserDao(DataSource dataSource) { this.dataSource = dataSource; }

    public User findByUsername(String username) {
        String sql = "SELECT userId, username, password, email, createdDate, predictionPoints, " +
                "is_verified, verification_token FROM users WHERE username = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User u = mapUser(rs);
                    u.setRoles(loadRoles(conn, u.getUserId()));
                    return u;
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "UserDao.findByUsername failed: " + username, e);
        }
        return null;
    }

    public User findByEmail(String email) {
        String sql = "SELECT userId, username, password, email, createdDate, predictionPoints, " +
                "is_verified, verification_token FROM users WHERE email = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User u = mapUser(rs);
                    u.setRoles(loadRoles(conn, u.getUserId()));
                    return u;
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "UserDao.findByEmail failed: " + email, e);
        }
        return null;
    }

    public User findByVerificationToken(String token) {
        String sql = "SELECT userId, username, password, email, createdDate, predictionPoints, " +
                "is_verified, verification_token FROM users WHERE verification_token = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapUser(rs);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "UserDao.findByVerificationToken failed", e);
        }
        return null;
    }

    public int create(User u) {
        String sql = "INSERT INTO users (username, password, email, createdDate, predictionPoints, " +
                "is_verified, verification_token) VALUES (?, ?, ?, NOW(), 0, 0, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, u.getUsername());
            stmt.setString(2, u.getPassword());
            stmt.setString(3, u.getEmail());
            stmt.setString(4, u.getVerificationToken());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    u.setUserId(id);
                    return id;
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "UserDao.create failed for: " + u.getUsername(), e);
        }
        return -1;
    }

    public int findRoleId(String roleName) {
        String sql = "SELECT roleId FROM roles WHERE roleName = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roleName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "UserDao.findRoleId failed: " + roleName, e);
        }
        return -1;
    }

    public void assignRole(int userId, int roleId) {
        String sql = "INSERT IGNORE INTO user_roles (userId, roleId) VALUES (?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, roleId);
            stmt.executeUpdate();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "UserDao.assignRole failed userId=" + userId, e);
        }
    }

    public void setVerified(int userId) {
        String sql = "UPDATE users SET is_verified=1, verification_token=NULL WHERE userId=?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "UserDao.setVerified failed userId=" + userId, e);
        }
    }

    public User findFirstWithFacePhoto() {
        String sql = "SELECT userId, username, password, email, createdDate, predictionPoints, " +
                "is_verified, verification_token FROM users WHERE face_photo IS NOT NULL LIMIT 1";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                User u = mapUser(rs);
                u.setRoles(loadRoles(conn, u.getUserId()));
                return u;
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "UserDao.findFirstWithFacePhoto failed", e);
        }
        return null;
    }

    public void saveFacePhoto(int userId, String base64) {
        String sql = "UPDATE users SET face_photo=? WHERE userId=?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, base64);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "UserDao.saveFacePhoto failed userId=" + userId, e);
        }
    }

    public void setVerificationToken(int userId, String token) {
        String sql = "UPDATE users SET verification_token=? WHERE userId=?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "UserDao.setVerificationToken failed userId=" + userId, e);
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("userId"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setEmail(rs.getString("email"));
        u.setVerified(rs.getInt("is_verified") == 1);
        u.setVerificationToken(rs.getString("verification_token"));
        Timestamp ts = rs.getTimestamp("createdDate");
        if (ts != null) u.setCreatedDate(ts.toLocalDateTime());
        return u;
    }

    private List<String> loadRoles(Connection conn, int userId) throws SQLException {
        List<String> roles = new ArrayList<>();
        String sql = "SELECT r.roleName FROM roles r JOIN user_roles ur ON ur.roleId=r.roleId WHERE ur.userId=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) roles.add(rs.getString(1));
            }
        }
        return roles;
    }
}
