package tn.smartfight.dao;

import tn.smartfight.config.DBConnection;
import tn.smartfight.model.Booking;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BookingDao {
    private static final Logger LOG = Logger.getLogger(BookingDao.class.getName());

    private final DataSource dataSource;

    public BookingDao() { this(DBConnection.getDataSource()); }
    public BookingDao(DataSource dataSource) { this.dataSource = dataSource; }

    public List<Booking> findByUserId(int userId) {
        String sql = "SELECT b.id, b.booking_status, b.ticket_quantity, b.total_price, " +
                "b.ticket_type, b.booking_date, b.booking_reference, b.created_at, b.updated_at, " +
                "b.event_id, b.user_id, e.eventName " +
                "FROM event_booking b JOIN events e ON e.eventId = b.event_id " +
                "WHERE b.user_id = ? ORDER BY b.created_at DESC";
        List<Booking> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapBooking(rs));
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "BookingDao.findByUserId failed userId=" + userId, e);
        }
        return list;
    }

    public void create(Booking b) {
        String sql = "INSERT INTO event_booking " +
                "(booking_status, ticket_quantity, total_price, ticket_type, booking_date, " +
                "booking_reference, created_at, updated_at, event_id, user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW(), ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, b.getBookingStatus() != null ? b.getBookingStatus() : "CONFIRMED");
            stmt.setInt(2, b.getTicketQuantity());
            stmt.setBigDecimal(3, b.getTotalPrice());
            stmt.setString(4, b.getTicketType());
            stmt.setObject(5, b.getBookingDate());
            stmt.setString(6, b.getBookingReference());
            stmt.setInt(7, b.getEventId());
            stmt.setInt(8, b.getUserId());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) b.setId(keys.getInt(1));
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "BookingDao.create failed", e);
        }
    }

    public void updateStatus(int bookingId, String status) {
        String sql = "UPDATE event_booking SET booking_status=?, updated_at=NOW() WHERE id=?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, bookingId);
            stmt.executeUpdate();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "BookingDao.updateStatus failed id=" + bookingId, e);
        }
    }

    private Booking mapBooking(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setId(rs.getInt("id"));
        b.setBookingStatus(rs.getString("booking_status"));
        b.setTicketQuantity(rs.getInt("ticket_quantity"));
        b.setTotalPrice(rs.getBigDecimal("total_price"));
        b.setTicketType(rs.getString("ticket_type"));
        Date bd = rs.getDate("booking_date");
        if (bd != null) b.setBookingDate(bd.toLocalDate());
        b.setBookingReference(rs.getString("booking_reference"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) b.setCreatedAt(ca.toLocalDateTime());
        b.setEventId(rs.getInt("event_id"));
        b.setUserId(rs.getInt("user_id"));
        b.setEventName(rs.getString("eventName"));
        return b;
    }
}
