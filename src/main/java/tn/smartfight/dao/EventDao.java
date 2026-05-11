package tn.smartfight.dao;

import tn.smartfight.config.DBConnection;
import tn.smartfight.model.Event;
import tn.smartfight.model.EventDetails;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EventDao {
    private static final Logger LOG = Logger.getLogger(EventDao.class.getName());

    private final DataSource dataSource;

    public EventDao() { this(DBConnection.getDataSource()); }
    public EventDao(DataSource dataSource) { this.dataSource = dataSource; }

    public List<Event> findAll() {
        String sql = "SELECT eventId, eventName, eventDate, organization, venue, city, country, " +
                "seat_capacity, poster_filename, status, visibility, is_champions_event " +
                "FROM events ORDER BY eventDate DESC";
        List<Event> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(mapEvent(rs));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "EventDao.findAll failed", e);
        }
        return list;
    }

    public Event getById(int id) {
        String sql = "SELECT eventId, eventName, eventDate, organization, venue, city, country, " +
                "seat_capacity, poster_filename, status, visibility, is_champions_event " +
                "FROM events WHERE eventId = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapEvent(rs);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "EventDao.getById failed id=" + id, e);
        }
        return null;
    }

    public void create(EventDetails e) {
        String sql = "INSERT INTO events (eventName, eventDate, organization, venue, city, country, " +
                "seat_capacity, poster_filename, status, visibility, is_champions_event) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParams(stmt, e);
            stmt.executeUpdate();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "EventDao.create failed", ex);
        }
    }

    public void update(EventDetails e) {
        String sql = "UPDATE events SET eventName=?, eventDate=?, organization=?, venue=?, city=?, country=?, " +
                "seat_capacity=?, poster_filename=?, status=?, visibility=?, is_champions_event=? " +
                "WHERE eventId=?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParams(stmt, e);
            stmt.setInt(12, e.getId());
            stmt.executeUpdate();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "EventDao.update failed id=" + e.getId(), ex);
        }
    }

    public void deleteById(int id) {
        String sql = "DELETE FROM events WHERE eventId = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "EventDao.deleteById failed id=" + id, e);
        }
    }

    private void setParams(PreparedStatement stmt, EventDetails e) throws SQLException {
        stmt.setString(1, e.getEventName());
        stmt.setObject(2, e.getEventDate());
        stmt.setString(3, e.getOrganization());
        stmt.setString(4, e.getVenue());
        stmt.setString(5, e.getCity());
        stmt.setString(6, e.getCountry());
        if (e.getSeatCapacity() != null) stmt.setInt(7, e.getSeatCapacity()); else stmt.setNull(7, Types.INTEGER);
        stmt.setString(8, e.getPosterFilename());
        stmt.setString(9, e.getStatus());
        stmt.setString(10, e.getVisibility());
        stmt.setInt(11, e.isChampionsEvent() ? 1 : 0);
    }

    private Event mapEvent(ResultSet rs) throws SQLException {
        Event ev = new Event();
        ev.setEventId(rs.getInt("eventId"));
        ev.setEventName(rs.getString("eventName"));
        Date d = rs.getDate("eventDate");
        if (d != null) ev.setEventDate(d.toLocalDate());
        ev.setOrganization(rs.getString("organization"));
        ev.setVenue(rs.getString("venue"));
        ev.setCity(rs.getString("city"));
        ev.setCountry(rs.getString("country"));
        int cap = rs.getInt("seat_capacity"); if (!rs.wasNull()) ev.setSeatCapacity(cap);
        ev.setPosterFilename(rs.getString("poster_filename"));
        ev.setStatus(rs.getString("status"));
        ev.setVisibility(rs.getString("visibility"));
        ev.setChampionsEvent(rs.getInt("is_champions_event") == 1);
        return ev;
    }
}
