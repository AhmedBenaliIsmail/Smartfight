package tn.smartfight.model;

import java.time.LocalDate;

public class EventDetails {
    private int id;
    private String eventName;
    private LocalDate eventDate;
    private String organization;
    private String venue;
    private String city;
    private String country;
    private Integer seatCapacity;
    private String posterFilename;
    private String status;
    private String visibility;
    private boolean championsEvent;

    public EventDetails() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public Integer getSeatCapacity() { return seatCapacity; }
    public void setSeatCapacity(Integer seatCapacity) { this.seatCapacity = seatCapacity; }

    public String getPosterFilename() { return posterFilename; }
    public void setPosterFilename(String posterFilename) { this.posterFilename = posterFilename; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }

    public boolean isChampionsEvent() { return championsEvent; }
    public void setChampionsEvent(boolean championsEvent) { this.championsEvent = championsEvent; }
}
