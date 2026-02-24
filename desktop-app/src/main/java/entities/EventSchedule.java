package entities;

import java.time.LocalDateTime;

public class EventSchedule {
    private int id;
    private int eventId;
    private String title;
    private LocalDateTime scheduledTime;
    private int durationMin;
    private String notes;

    public EventSchedule() {
    }

    public EventSchedule(int eventId, String title, LocalDateTime scheduledTime,
            int durationMin, String notes) {
        this.eventId = eventId;
        this.title = title;
        this.scheduledTime = scheduledTime;
        this.durationMin = durationMin;
        this.notes = notes;
    }

    public EventSchedule(int id, int eventId, String title, LocalDateTime scheduledTime,
            int durationMin, String notes) {
        this.id = id;
        this.eventId = eventId;
        this.title = title;
        this.scheduledTime = scheduledTime;
        this.durationMin = durationMin;
        this.notes = notes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public int getDurationMin() {
        return durationMin;
    }

    public void setDurationMin(int durationMin) {
        this.durationMin = durationMin;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "EventSchedule{id=" + id + ", eventId=" + eventId + ", title='" + title +
                "', scheduledTime=" + scheduledTime + ", durationMin=" + durationMin + "}";
    }
}
