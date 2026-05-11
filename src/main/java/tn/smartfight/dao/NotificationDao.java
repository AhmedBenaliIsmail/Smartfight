package tn.smartfight.dao;

import tn.smartfight.config.DBConnection;
import tn.smartfight.model.Notification;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NotificationDao {
    private static final Logger LOG = Logger.getLogger(NotificationDao.class.getName());
    private final DataSource ds;

    public NotificationDao() { this(DBConnection.getDataSource()); }
    public NotificationDao(DataSource ds) { this.ds = ds; }

    public List<Notification> findRecentByUser(int userId, int limit) {
        String sql = "SELECT notificationId, message, created_at, is_read, type, userId " +
                "FROM notifications WHERE userId=? ORDER BY created_at DESC LIMIT ?";
        List<Notification> list = new ArrayList<>();
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "findRecentByUser failed", e);
        }
        return list;
    }

    public int countUnread(int userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE userId=? AND is_read=0";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "countUnread failed", e);
        }
        return 0;
    }

    public void markAsRead(int notificationId) {
        String sql = "UPDATE notifications SET is_read=1 WHERE notificationId=?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            ps.executeUpdate();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "markAsRead failed", e);
        }
    }

    public void markAllReadForUser(int userId) {
        String sql = "UPDATE notifications SET is_read=1 WHERE userId=?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "markAllReadForUser failed", e);
        }
    }

    public void create(Notification n) {
        String sql = "INSERT INTO notifications (message, created_at, is_read, type, userId) VALUES (?,?,0,?,?)";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, n.getMessage());
            ps.setTimestamp(2, Timestamp.valueOf(n.getCreatedAt() != null ? n.getCreatedAt() : LocalDateTime.now()));
            ps.setString(3, n.getType());
            ps.setInt(4, n.getUserId());
            ps.executeUpdate();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "create notification failed", e);
        }
    }

    public List<Integer> findAllUserIds() {
        String sql = "SELECT userId FROM users";
        List<Integer> ids = new ArrayList<>();
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) ids.add(rs.getInt(1));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "findAllUserIds failed", e);
        }
        return ids;
    }

    private Notification map(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setNotificationId(rs.getInt("notificationId"));
        n.setMessage(rs.getString("message"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) n.setCreatedAt(ts.toLocalDateTime());
        n.setRead(rs.getBoolean("is_read"));
        n.setType(rs.getString("type"));
        n.setUserId(rs.getInt("userId"));
        return n;
    }
}
