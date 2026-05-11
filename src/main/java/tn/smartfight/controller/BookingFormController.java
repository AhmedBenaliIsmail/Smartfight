package tn.smartfight.controller;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.smartfight.config.Session;
import tn.smartfight.dao.EventDao;
import tn.smartfight.model.Booking;
import tn.smartfight.model.Event;
import tn.smartfight.service.BookingService;

import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BookingFormController {
    private static final Logger LOG = Logger.getLogger(BookingFormController.class.getName());

    @FXML private ComboBox<Event> eventCombo;
    @FXML private ComboBox<String> ticketTypeCombo;
    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private Label totalLabel;
    @FXML private Label statusLabel;

    private final BookingService bookingService = new BookingService();
    private final EventDao eventDao = new EventDao();
    private BigDecimal cachedUnitPrice = BigDecimal.ZERO;

    @FXML
    public void initialize() {
        List<Event> events = eventDao.findAll();
        eventCombo.setItems(FXCollections.observableArrayList(events));
        eventCombo.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Event e, boolean empty) {
                super.updateItem(e, empty);
                setText(empty || e == null ? null : e.getEventName() + " (" + e.getEventDate() + ")");
            }
        });
        eventCombo.setButtonCell(eventCombo.getCellFactory().call(null));

        ticketTypeCombo.setItems(FXCollections.observableArrayList(
                "VIP_RINGSIDE", "PREMIUM_LOWER", "REGULAR_SEATING", "BALCONY", "STANDING_ROOM"));
        ticketTypeCombo.setValue("REGULAR_SEATING");

        quantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 4, 1));

        eventCombo.valueProperty().addListener((obs, o, n) -> requestPriceUpdate());
        ticketTypeCombo.valueProperty().addListener((obs, o, n) -> requestPriceUpdate());
        quantitySpinner.valueProperty().addListener((obs, o, n) -> updateTotal());
    }

    private void requestPriceUpdate() {
        Event ev = eventCombo.getValue();
        String type = ticketTypeCombo.getValue();
        if (ev == null || type == null || Session.getUser() == null) {
            if (totalLabel != null) totalLabel.setText("Total: —");
            return;
        }
        Task<BigDecimal> task = new Task<>() {
            @Override protected BigDecimal call() {
                return bookingService.calculateUnitPrice(ev, type, Session.getUser().getUserId());
            }
        };
        task.setOnSucceeded(e -> {
            cachedUnitPrice = task.getValue();
            updateTotal();
        });
        task.setOnFailed(e -> { if (totalLabel != null) totalLabel.setText("Total: —"); });
        new Thread(task, "price-task").start();
    }

    private void updateTotal() {
        int qty = quantitySpinner.getValue();
        BigDecimal total = cachedUnitPrice.multiply(BigDecimal.valueOf(qty));
        if (totalLabel != null) totalLabel.setText(String.format("Total: $%.2f", total));
    }

    @FXML
    private void onBook() {
        Event event = eventCombo.getValue();
        if (event == null) { statusLabel.setText("Select an event."); return; }
        if (Session.getUser() == null) { statusLabel.setText("Not logged in."); return; }

        String type = ticketTypeCombo.getValue();
        int qty = quantitySpinner.getValue();

        statusLabel.setText("Booking...");
        Task<Booking> task = new Task<>() {
            @Override protected Booking call() throws Exception {
                return bookingService.createBooking(event, type, qty, Session.getUser());
            }
        };
        task.setOnSucceeded(ev -> {
            Booking b = task.getValue();
            statusLabel.setText("Booked! Ref: " + b.getBookingReference());
        });
        task.setOnFailed(ev -> {
            LOG.log(Level.SEVERE, "Booking failed", task.getException());
            statusLabel.setText("Booking failed: " + task.getException().getMessage());
        });
        new Thread(task, "booking-task").start();
    }
}
