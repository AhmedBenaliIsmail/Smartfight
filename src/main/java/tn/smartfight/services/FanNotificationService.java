package tn.smartfight.services;

import tn.smartfight.database.DBConnection;
import tn.smartfight.models.*;

import java.sql.*;
import java.util.*;

public class FanNotificationService {

    private final Connection conn = DBConnection.getInstance().getConnection();
    private final FanPreferenceService prefService = new FanPreferenceService();

    // ---------------------------------------------------------------
    // getNotificationsForFan
    // ---------------------------------------------------------------
    public List<FanNotification> getNotificationsForFan(int fanId) {
        List<FanNotification> list = new ArrayList<>();
        String sql = "SELECT * FROM fan_notification WHERE fan_id=? ORDER BY created_at DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fanId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapNotification(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[FanNotificationService] getNotificationsForFan: " + e.getMessage());
        }
        return list;
    }

    // ---------------------------------------------------------------
    // getUnreadCount
    // ---------------------------------------------------------------
    public int getUnreadCount(int fanId) {
        String sql = "SELECT COUNT(*) FROM fan_notification WHERE fan_id=? AND is_read=0";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fanId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[FanNotificationService] getUnreadCount: " + e.getMessage());
        }
        return 0;
    }

    // ---------------------------------------------------------------
    // markAsRead
    // ---------------------------------------------------------------
    public void markAsRead(int notificationId) {
        String sql = "UPDATE fan_notification SET is_read=1 WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[FanNotificationService] markAsRead: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // markAllAsRead
    // ---------------------------------------------------------------
    public void markAllAsRead(int fanId) {
        String sql = "UPDATE fan_notification SET is_read=1 WHERE fan_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fanId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[FanNotificationService] markAllAsRead: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // createNotification
    // ---------------------------------------------------------------
    public void createNotification(int fanId, String type, String title, String message,
                                   Integer relatedEventId, Integer relatedFightId) {
        String sql = "INSERT INTO fan_notification " +
                     "(fan_id, type, title, message, related_event_id, related_fight_id) " +
                     "VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fanId);
            ps.setString(2, type);
            ps.setString(3, title);
            ps.setString(4, message);
            ps.setObject(5, relatedEventId);
            ps.setObject(6, relatedFightId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[FanNotificationService] createNotification: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // broadcastToAll
    // ---------------------------------------------------------------
    public int broadcastToAll(String title, String message) {
        List<Integer> fanIds = prefService.getAllFanIds();
        int count = 0;
        for (int fanId : fanIds) {
            createNotification(fanId, "ADMIN_BROADCAST", title, message, null, null);
            count++;
        }
        return count;
    }

    // ---------------------------------------------------------------
    // broadcastByDiscipline
    // ---------------------------------------------------------------
    public int broadcastByDiscipline(int disciplineId, String title, String message) {
        List<Integer> fanIds = prefService.getFanIdsByDiscipline(disciplineId);
        int count = 0;
        for (int fanId : fanIds) {
            createNotification(fanId, "ADMIN_BROADCAST", title, message, null, null);
            count++;
        }
        return count;
    }

    // ---------------------------------------------------------------
    // triggerNewEventNotification
    // ---------------------------------------------------------------
    public void triggerNewEventNotification(int eventId, String eventName, int disciplineId) {
        List<Integer> fanIds = prefService.getFanIdsByDiscipline(disciplineId);
        String title = "New event: " + eventName;
        String msg   = eventName + " has been announced. Book your tickets!";
        for (int fanId : fanIds) {
            createNotification(fanId, "NEW_EVENT", title, msg, eventId, null);
        }
    }

    // ---------------------------------------------------------------
    // triggerPredictionScoredNotification
    // ---------------------------------------------------------------
    public void triggerPredictionScoredNotification(int fanId, String winnerName, int points) {
        createNotification(
            fanId,
            "PREDICTION_SCORED",
            "Prediction result",
            "Your prediction for " + winnerName + " earned you " + points + " point(s)!",
            null,
            null
        );
    }

    // ---------------------------------------------------------------
    // getBroadcastHistory
    // ---------------------------------------------------------------
    public List<Map<String, Object>> getBroadcastHistory() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql =
            "SELECT fn.title, fn.type, MIN(fn.created_at) AS created_at, COUNT(*) AS recipients " +
            "FROM fan_notification fn " +
            "WHERE fn.type='ADMIN_BROADCAST' " +
            "GROUP BY fn.title, fn.type " +
            "ORDER BY MIN(fn.created_at) DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("title", rs.getString("title"));
                row.put("type", rs.getString("type"));
                java.sql.Timestamp createdAt = rs.getTimestamp("created_at");
                row.put("created_at", createdAt != null ? createdAt.toLocalDateTime() : null);
                row.put("recipients", rs.getInt("recipients"));
                list.add(row);
            }
        } catch (SQLException e) {
            System.err.println("[FanNotificationService] getBroadcastHistory: " + e.getMessage());
        }
        return list;
    }

    // ---------------------------------------------------------------
    // Private helper
    // ---------------------------------------------------------------
    private FanNotification mapNotification(ResultSet rs) throws SQLException {
        FanNotification n = new FanNotification();
        n.setId(rs.getInt("id"));
        n.setFanId(rs.getInt("fan_id"));
        n.setType(rs.getString("type"));
        n.setTitle(rs.getString("title"));
        n.setMessage(rs.getString("message"));
        n.setRead(rs.getBoolean("is_read"));
        n.setRelatedEventId(rs.getObject("related_event_id", Integer.class));
        n.setRelatedFightId(rs.getObject("related_fight_id", Integer.class));
        java.sql.Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) n.setCreatedAt(createdAt.toString());
        return n;
    }
}
