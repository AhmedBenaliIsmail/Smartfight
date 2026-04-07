package model;

import java.time.LocalDate;
import javafx.beans.property.*;

/**
 * EVENT MODEL
 * Optimized for JavaFX TableView compatibility and data binding.
 */
public class Event {
    private final IntegerProperty eventId = new SimpleIntegerProperty();
    private final StringProperty eventName = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> eventDate = new SimpleObjectProperty<>();
    private final StringProperty location = new SimpleStringProperty();
    
    // Helper property to store the number of fights (0-3) 
    // calculated during the initial loadData() call.
    private final IntegerProperty fightCount = new SimpleIntegerProperty(0);

    // Constructors
    public Event() {}

    public Event(String eventName, LocalDate eventDate, String location) {
        setEventName(eventName);
        setEventDate(eventDate);
        setLocation(location);
    }

    public Event(int eventId, String eventName, LocalDate eventDate, String location) {
        setEventId(eventId);
        setEventName(eventName);
        setEventDate(eventDate);
        setLocation(location);
    }

    // --- Property Getters (For TableView setCellValueFactory) ---

    public IntegerProperty eventIdProperty() { return eventId; }
    public StringProperty eventNameProperty() { return eventName; }
    public ObjectProperty<LocalDate> eventDateProperty() { return eventDate; }
    public StringProperty locationProperty() { return location; }
    public IntegerProperty fightCountProperty() { return fightCount; }

    // --- Standard Getters & Setters ---

    public int getEventId() { return eventId.get(); }
    public void setEventId(int id) { this.eventId.set(id); }

    public String getEventName() { return eventName.get(); }
    public void setEventName(String name) { this.eventName.set(name); }

    public LocalDate getEventDate() { return eventDate.get(); }
    public void setEventDate(LocalDate date) { this.eventDate.set(date); }

    public String getLocation() { return location.get(); }
    public void setLocation(String loc) { this.location.set(loc); }

    public int getFightCount() { return fightCount.get(); }
    public void setFightCount(int count) { this.fightCount.set(count); }

    /**
     * Determine if the event is upcoming based on current system date.
     */
    public boolean isUpcoming() {
        LocalDate date = getEventDate();
        return date != null && !date.isBefore(LocalDate.now());
    }

    @Override
    public String toString() {
        return getEventName() + " (" + getEventDate() + ")";
    }
}