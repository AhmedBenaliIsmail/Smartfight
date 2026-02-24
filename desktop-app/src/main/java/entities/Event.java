package entities;

import java.time.LocalDate;

public class Event {
    private int id;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String visibility;
    private int capacity;
    private int venueId;
    private int disciplineId;
    private int organizerId;

    public Event() {
    }

    public Event(String name, String description, LocalDate startDate, LocalDate endDate,
            String status, String visibility, int capacity,
            int venueId, int disciplineId, int organizerId) {
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.visibility = visibility;
        this.capacity = capacity;
        this.venueId = venueId;
        this.disciplineId = disciplineId;
        this.organizerId = organizerId;
    }

    public Event(int id, String name, String description, LocalDate startDate, LocalDate endDate,
            String status, String visibility, int capacity,
            int venueId, int disciplineId, int organizerId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.visibility = visibility;
        this.capacity = capacity;
        this.venueId = venueId;
        this.disciplineId = disciplineId;
        this.organizerId = organizerId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getVenueId() {
        return venueId;
    }

    public void setVenueId(int venueId) {
        this.venueId = venueId;
    }

    public int getDisciplineId() {
        return disciplineId;
    }

    public void setDisciplineId(int disciplineId) {
        this.disciplineId = disciplineId;
    }

    public int getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(int organizerId) {
        this.organizerId = organizerId;
    }

    @Override
    public String toString() {
        return "Event{id=" + id + ", name='" + name + "', startDate=" + startDate +
                ", endDate=" + endDate + ", status='" + status + "', visibility='" + visibility +
                "', capacity=" + capacity + ", venueId=" + venueId +
                ", disciplineId=" + disciplineId + ", organizerId=" + organizerId + "}";
    }
}
