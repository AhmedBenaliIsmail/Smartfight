package tn.smartfight.services;

import tn.smartfight.database.DBConnection;
import tn.smartfight.models.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventService {

    private final Connection conn = DBConnection.getInstance().getConnection();

    private static final String BASE_SELECT =
        "SELECT e.id, e.name, e.status, e.start_date, e.end_date, e.capacity, " +
        "       v.name AS venue_name, v.city AS venue_city, e.discipline_id, " +
        "       e.visibility, e.organizer_id " +
        "FROM event e LEFT JOIN venue v ON e.venue_id = v.id ";

    // ── READ ─────────────────────────────────────────────────────────────────

    public List<Event> getScheduledPublicEvents() {
        return query(BASE_SELECT +
            "WHERE e.visibility = 'PUBLIC' AND e.status = 'SCHEDULED' ORDER BY e.start_date ASC");
    }

    public List<Event> getAllEvents() {
        return query(BASE_SELECT + "ORDER BY e.start_date DESC");
    }

    public Event getEventById(int id) {
        String sql = BASE_SELECT + "WHERE e.id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapEvent(rs);
            }
        } catch (SQLException e) {
            System.err.println("[EventService] getEventById: " + e.getMessage());
        }
        return null;
    }

    public List<Event> getEventsByOrganizer(int organizerId) {
        return query(BASE_SELECT + "WHERE e.organizer_id = ? ORDER BY e.start_date DESC",
                     organizerId);
    }

    // ── CREATE ───────────────────────────────────────────────────────────────

    public boolean createEvent(Event e) {
        String sql = "INSERT INTO event (name, status, start_date, end_date, capacity, " +
                     "venue_id, discipline_id, visibility, organizer_id) VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getName());
            ps.setString(2, e.getStatus() != null ? e.getStatus() : "SCHEDULED");
            ps.setDate(3, e.getStartDate() != null ? Date.valueOf(e.getStartDate()) : null);
            ps.setDate(4, e.getEndDate()   != null ? Date.valueOf(e.getEndDate())   : null);
            ps.setInt(5, e.getCapacity());
            ps.setNull(6, Types.INTEGER); // venue_id optional
            if (e.getDisciplineId() > 0) ps.setInt(7, e.getDisciplineId());
            else ps.setNull(7, Types.INTEGER);
            ps.setString(8, e.getVisibility() != null ? e.getVisibility() : "PUBLIC");
            if (e.getOrganizerId() > 0) ps.setInt(9, e.getOrganizerId());
            else ps.setNull(9, Types.INTEGER);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) e.setId(keys.getInt(1));
            }
            return rows > 0;
        } catch (SQLException ex) {
            System.err.println("[EventService] createEvent: " + ex.getMessage());
            return false;
        }
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────

    public boolean updateEvent(Event e) {
        String sql = "UPDATE event SET name=?, status=?, start_date=?, end_date=?, " +
                     "capacity=?, discipline_id=?, visibility=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getName());
            ps.setString(2, e.getStatus());
            ps.setDate(3, e.getStartDate() != null ? Date.valueOf(e.getStartDate()) : null);
            ps.setDate(4, e.getEndDate()   != null ? Date.valueOf(e.getEndDate())   : null);
            ps.setInt(5, e.getCapacity());
            if (e.getDisciplineId() > 0) ps.setInt(6, e.getDisciplineId());
            else ps.setNull(6, Types.INTEGER);
            ps.setString(7, e.getVisibility() != null ? e.getVisibility() : "PUBLIC");
            ps.setInt(8, e.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("[EventService] updateEvent: " + ex.getMessage());
            return false;
        }
    }

    // ── DELETE ───────────────────────────────────────────────────────────────

    public boolean deleteEvent(int id) {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM event WHERE id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[EventService] deleteEvent: " + e.getMessage());
            return false;
        }
    }

    // ── HELPERS ──────────────────────────────────────────────────────────────

    private List<Event> query(String sql, Object... params) {
        List<Event> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                if (params[i] instanceof Integer) ps.setInt(i + 1, (Integer) params[i]);
                else ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapEvent(rs));
            }
        } catch (SQLException e) {
            System.err.println("[EventService] query: " + e.getMessage());
        }
        return list;
    }

    private Event mapEvent(ResultSet rs) throws SQLException {
        Event e = new Event();
        e.setId(rs.getInt("id"));
        e.setName(rs.getString("name"));
        e.setStatus(rs.getString("status"));
        java.sql.Date startDate = rs.getDate("start_date");
        if (startDate != null) e.setStartDate(startDate.toLocalDate());
        java.sql.Date endDate = rs.getDate("end_date");
        if (endDate != null) e.setEndDate(endDate.toLocalDate());
        e.setCapacity(rs.getInt("capacity"));
        e.setVenueName(rs.getString("venue_name"));
        e.setVenueCity(rs.getString("venue_city"));
        e.setDisciplineId(rs.getInt("discipline_id"));
        try { e.setVisibility(rs.getString("visibility")); } catch (SQLException ignored) {}
        try { e.setOrganizerId(rs.getInt("organizer_id")); } catch (SQLException ignored) {}
        return e;
    }
}
