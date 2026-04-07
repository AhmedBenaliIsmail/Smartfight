package tn.smartfight.models;

import java.time.LocalDate;

public class Event {

    private int       id;
    private String    name;
    private String    status;
    private LocalDate startDate;
    private LocalDate endDate;
    private int       capacity;
    private String    venueName;
    private String    venueCity;
    private int       disciplineId;
    private String    visibility;
    private int       organizerId;

    public Event() {}

    public Event(int id, String name, String status, LocalDate startDate, LocalDate endDate,
                 int capacity, String venueName, String venueCity, int disciplineId) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.capacity = capacity;
        this.venueName = venueName;
        this.venueCity = venueCity;
        this.disciplineId = disciplineId;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    public boolean isScheduled() { return "SCHEDULED".equals(status); }

    public String getDateRange() { return startDate + " → " + endDate; }

    @Override
    public String toString() { return name; }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public int getId()              { return id; }
    public void setId(int id)       { this.id = id; }

    public String getName()                { return name; }
    public void setName(String name)       { this.name = name; }

    public String getStatus()              { return status; }
    public void setStatus(String status)   { this.status = status; }

    public LocalDate getStartDate()                    { return startDate; }
    public void setStartDate(LocalDate startDate)      { this.startDate = startDate; }

    public LocalDate getEndDate()                      { return endDate; }
    public void setEndDate(LocalDate endDate)          { this.endDate = endDate; }

    public int getCapacity()               { return capacity; }
    public void setCapacity(int capacity)  { this.capacity = capacity; }

    public String getVenueName()                   { return venueName; }
    public void setVenueName(String venueName)     { this.venueName = venueName; }

    public String getVenueCity()                   { return venueCity; }
    public void setVenueCity(String venueCity)     { this.venueCity = venueCity; }

    public int getDisciplineId()                       { return disciplineId; }
    public void setDisciplineId(int disciplineId)      { this.disciplineId = disciplineId; }

    public String getVisibility()                  { return visibility; }
    public void setVisibility(String visibility)   { this.visibility = visibility; }

    public int getOrganizerId()                    { return organizerId; }
    public void setOrganizerId(int organizerId)    { this.organizerId = organizerId; }
}
