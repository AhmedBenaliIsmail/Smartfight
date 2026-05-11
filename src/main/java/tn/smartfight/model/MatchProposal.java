package tn.smartfight.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MatchProposal {
    private int id;
    private BigDecimal compatibility;
    private String status;
    private LocalDateTime proposedAt;
    private String notes;
    private Integer eventId;
    private int fighter1Id;
    private int fighter2Id;
    private int voteCount;
    private Integer weightDivisionId;

    // denormalized for display
    private String fighter1Name;
    private String fighter2Name;
    private String divisionName;

    public MatchProposal() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public BigDecimal getCompatibility() { return compatibility; }
    public void setCompatibility(BigDecimal compatibility) { this.compatibility = compatibility; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getProposedAt() { return proposedAt; }
    public void setProposedAt(LocalDateTime proposedAt) { this.proposedAt = proposedAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Integer getEventId() { return eventId; }
    public void setEventId(Integer eventId) { this.eventId = eventId; }

    public int getFighter1Id() { return fighter1Id; }
    public void setFighter1Id(int fighter1Id) { this.fighter1Id = fighter1Id; }

    public int getFighter2Id() { return fighter2Id; }
    public void setFighter2Id(int fighter2Id) { this.fighter2Id = fighter2Id; }

    public int getVoteCount() { return voteCount; }
    public void setVoteCount(int voteCount) { this.voteCount = voteCount; }

    public Integer getWeightDivisionId() { return weightDivisionId; }
    public void setWeightDivisionId(Integer weightDivisionId) { this.weightDivisionId = weightDivisionId; }

    public String getFighter1Name() { return fighter1Name; }
    public void setFighter1Name(String fighter1Name) { this.fighter1Name = fighter1Name; }

    public String getFighter2Name() { return fighter2Name; }
    public void setFighter2Name(String fighter2Name) { this.fighter2Name = fighter2Name; }

    public String getDivisionName() { return divisionName; }
    public void setDivisionName(String divisionName) { this.divisionName = divisionName; }

    public String getMatchup() {
        return (fighter1Name != null ? fighter1Name : "F" + fighter1Id)
                + " vs " + (fighter2Name != null ? fighter2Name : "F" + fighter2Id);
    }
}
