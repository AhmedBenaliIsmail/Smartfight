package tn.smartfight.service;

import tn.smartfight.config.AppConfig;
import tn.smartfight.config.DBConnection;
import tn.smartfight.dao.BookingDao;
import tn.smartfight.integration.GmailMailer;
import tn.smartfight.integration.PebbleRenderer;
import tn.smartfight.model.Booking;
import tn.smartfight.model.Event;
import tn.smartfight.model.User;
import tn.smartfight.util.QrCodeGenerator;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BookingService {
    private static final Logger LOG = Logger.getLogger(BookingService.class.getName());
    private static final SecureRandom RANDOM = new SecureRandom();

    private static final Map<String, BigDecimal> BASE_PRICES = Map.of(
            "VIP_RINGSIDE",    BigDecimal.valueOf(200),
            "PREMIUM_LOWER",   BigDecimal.valueOf(120),
            "REGULAR_SEATING", BigDecimal.valueOf(75),
            "BALCONY",         BigDecimal.valueOf(50),
            "STANDING_ROOM",   BigDecimal.valueOf(30)
    );

    private final DataSource dataSource;

    public BookingService() { this(DBConnection.getDataSource()); }
    public BookingService(DataSource ds) { this.dataSource = ds; }

    public BigDecimal calculateUnitPrice(Event event, String ticketType, int userId) {
        BigDecimal base = BASE_PRICES.getOrDefault(ticketType, BigDecimal.valueOf(75));
        double eloMult = fetchAvgEloMultiplier(event.getEventId());
        double champMult = event.getOrganization() != null
                && !"INDEPENDENT".equalsIgnoreCase(event.getOrganization()) ? 1.5 : 1.0;
        double fanMult = 1.0; // requires MatchProposal (Phase 10)
        double loyaltyMult = fetchLoyaltyMultiplier(userId);
        return base.multiply(BigDecimal.valueOf(eloMult * champMult * fanMult * loyaltyMult))
                   .setScale(2, RoundingMode.HALF_UP);
    }

    public Booking createBooking(Event event, String ticketType, int quantity, User user) throws Exception {
        if (quantity < 1 || quantity > 4) throw new IllegalArgumentException("Quantity must be 1–4");
        BigDecimal unitPrice = calculateUnitPrice(event, ticketType, user.getUserId());
        BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(quantity));
        String ref = "SF-" + generateHexRef();

        Booking b = new Booking();
        b.setEventId(event.getEventId());
        b.setUserId(user.getUserId());
        b.setTicketType(ticketType);
        b.setTicketQuantity(quantity);
        b.setTotalPrice(total);
        b.setBookingDate(LocalDate.now());
        b.setBookingReference(ref);
        b.setBookingStatus("CONFIRMED");

        new BookingDao(dataSource).create(b);
        trySendConfirmationEmail(b, event, user);
        return b;
    }

    private void trySendConfirmationEmail(Booking b, Event event, User user) {
        try {
            AppConfig cfg = AppConfig.get();
            if (cfg.mailSmtpHost.isBlank() || user.getEmail() == null || user.getEmail().isBlank()) return;

            String qrText = buildQrText(b, event, user);
            String qrBase64 = QrCodeGenerator.toPngBase64(qrText);

            String dateStr = event.getEventDate() != null
                    ? event.getEventDate().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"))
                    : "TBD";

            Map<String, Object> bookingMap = new HashMap<>();
            bookingMap.put("user", Map.of("fullName", user.getUsername()));
            bookingMap.put("event", Map.of(
                    "name",      event.getEventName() != null ? event.getEventName() : "",
                    "eventDate", dateStr,
                    "city",      event.getCity() != null ? event.getCity() : ""
            ));
            bookingMap.put("ticketType",        b.getTicketType().replace("_", " "));
            bookingMap.put("ticketQuantity",    b.getTicketQuantity());
            bookingMap.put("totalPrice",        String.format("%.2f", b.getTotalPrice()));
            bookingMap.put("bookingReference",  b.getBookingReference());

            Map<String, Object> ctx = new HashMap<>();
            ctx.put("booking", bookingMap);
            ctx.put("qrDataUri", qrBase64);
            ctx.put("year", LocalDate.now().getYear());

            String html = PebbleRenderer.render("templates/emails/booking_confirmation.html.twig", ctx);
            GmailMailer.send(user.getEmail(),
                    "SmartFight Booking Confirmed – " + b.getBookingReference(), html);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Booking confirmation email failed for ref=" + b.getBookingReference(), e);
        }
    }

    private String buildQrText(Booking b, Event event, User user) {
        String dateStr = event.getEventDate() != null
                ? event.getEventDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")) : "TBD";
        String venuePart = event.getVenue() != null ? event.getVenue() : "";
        String cityPart  = event.getCity()  != null ? event.getCity()  : "";
        String venue = venuePart.isBlank() ? cityPart : cityPart.isBlank() ? venuePart : venuePart + ", " + cityPart;
        return "SMARTFIGHT FAN TICKET\n" +
               "-------------------\n" +
               "Event: " + (event.getEventName() != null ? event.getEventName() : "") + "\n" +
               "Date: " + dateStr + "\n" +
               "Venue: " + venue + "\n\n" +
               "Ticket Details\n" +
               "-------------------\n" +
               "Fan: " + user.getUsername() + "\n" +
               "Type: " + b.getTicketType().replace("_", " ") + "\n" +
               "Qty: " + b.getTicketQuantity() + "\n" +
               "Ref: " + b.getBookingReference() + "\n" +
               "Status: " + b.getBookingStatus();
    }

    private double fetchAvgEloMultiplier(int eventId) {
        String sql = "SELECT AVG(elo) FROM (" +
                "SELECT fd.eloRating AS elo FROM fight_results fr " +
                "  JOIN fighter_details fd ON fd.fighterId = fr.fighter1Id WHERE fr.eventId = ? " +
                "UNION ALL " +
                "SELECT fd.eloRating FROM fight_results fr " +
                "  JOIN fighter_details fd ON fd.fighterId = fr.fighter2Id WHERE fr.eventId = ?" +
                ") t";
        try (var conn = dataSource.getConnection();
             var ps   = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.setInt(2, eventId);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    double avg = rs.getDouble(1);
                    return rs.wasNull() ? 1.0 : avg / 1500.0;
                }
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "fetchAvgEloMultiplier failed eventId=" + eventId, e);
        }
        return 1.0;
    }

    private double fetchLoyaltyMultiplier(int userId) {
        String sql = "SELECT COUNT(*) FROM event_booking WHERE user_id = ? AND booking_status = 'CONFIRMED'";
        try (var conn = dataSource.getConnection();
             var ps   = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return (count > 0 && (count + 1) % 4 == 0) ? 0.9 : 1.0;
                }
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "fetchLoyaltyMultiplier failed userId=" + userId, e);
        }
        return 1.0;
    }

    private String generateHexRef() {
        byte[] bytes = new byte[4];
        RANDOM.nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02X", b));
        return sb.toString();
    }
}
