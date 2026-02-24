package entities;

public class MatchProposal {
    private int id;
    private int eventId;
    private int fighter1Id;
    private int fighter2Id;
    private double compatibility;
    private String status;
    private String notes;

    public MatchProposal() {
    }

    public MatchProposal(int eventId, int fighter1Id, int fighter2Id, double compatibility, String status,
            String notes) {
        this.eventId = eventId;
        this.fighter1Id = fighter1Id;
        this.fighter2Id = fighter2Id;
        this.compatibility = compatibility;
        this.status = status;
        this.notes = notes;
    }

    public MatchProposal(int id, int eventId, int fighter1Id, int fighter2Id, double compatibility, String status,
            String notes) {
        this.id = id;
        this.eventId = eventId;
        this.fighter1Id = fighter1Id;
        this.fighter2Id = fighter2Id;
        this.compatibility = compatibility;
        this.status = status;
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

    public int getFighter1Id() {
        return fighter1Id;
    }

    public void setFighter1Id(int fighter1Id) {
        this.fighter1Id = fighter1Id;
    }

    public int getFighter2Id() {
        return fighter2Id;
    }

    public void setFighter2Id(int fighter2Id) {
        this.fighter2Id = fighter2Id;
    }

    public double getCompatibility() {
        return compatibility;
    }

    public void setCompatibility(double compatibility) {
        this.compatibility = compatibility;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "MatchProposal{id=" + id + ", eventId=" + eventId + ", fighter1=" + fighter1Id +
                ", fighter2=" + fighter2Id + ", compatibility=" + compatibility + ", status='" + status + "'}";
    }
}
