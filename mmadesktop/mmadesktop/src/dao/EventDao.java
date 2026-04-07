package dao;

import app.util.DBCNX;
import model.Event;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EventDao {

    /**
     * Adds a new event and updates the object with the generated ID.
     */
    public boolean addEvent(Event event) {
        String sql = "INSERT INTO events (eventName, eventDate, location) VALUES (?, ?, ?)";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, event.getEventName());
            // Safe conversion of LocalDate to SQL Date
            pstmt.setDate(2, event.getEventDate() != null ? Date.valueOf(event.getEventDate()) : null);
            pstmt.setString(3, event.getLocation());

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        event.setEventId(keys.getInt(1));
                    }
                }
            }
            return affected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding event: " + e.getMessage());
            return false;
        }
    }

    public Event getEventById(int eventId) {
        String sql = "SELECT * FROM events WHERE eventId = ?";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, eventId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapEvent(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Event> getAllEvents() {
        List<Event> events = new ArrayList<>();
        // Ordering by date descending keeps upcoming/recent events at the top
        String sql = "SELECT * FROM events ORDER BY eventDate DESC";
        try (Connection conn = DBCNX.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                events.add(mapEvent(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return events;
    }

    public boolean updateEvent(Event event) {
        String sql = "UPDATE events SET eventName = ?, eventDate = ?, location = ? WHERE eventId = ?";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, event.getEventName());
            pstmt.setDate(2, Date.valueOf(event.getEventDate()));
            pstmt.setString(3, event.getLocation());
            pstmt.setInt(4, event.getEventId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes an event. 
     * NOTE: If your DB doesn't have "ON DELETE CASCADE" set up on the fights table,
     * you should delete associated fights before calling this.
     */
    public boolean deleteEvent(int eventId) {
        String sql = "DELETE FROM events WHERE eventId = ?";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, eventId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting event: " + e.getMessage());
            return false;
        }
    }

    /**
     * Helper to convert a ResultSet row into an Event object.
     * Uses the setters which interact with the JavaFX Properties.
     */
    private Event mapEvent(ResultSet rs) throws SQLException {
        Event event = new Event();
        event.setEventId(rs.getInt("eventId"));
        event.setEventName(rs.getString("eventName"));
        
        // Handle potential null dates from the database safely
        Date dbDate = rs.getDate("eventDate");
        if (dbDate != null) {
            event.setEventDate(dbDate.toLocalDate());
        }
        
        event.setLocation(rs.getString("location"));
        return event;
    }
}