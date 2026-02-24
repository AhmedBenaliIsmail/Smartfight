package entities;

import java.time.LocalDate;

public class FighterCoach {
    private int id;
    private int fighterId;
    private int coachId;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isActive;

    public FighterCoach() {
    }

    public FighterCoach(int fighterId, int coachId, LocalDate startDate, LocalDate endDate, boolean isActive) {
        this.fighterId = fighterId;
        this.coachId = coachId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = isActive;
    }

    public FighterCoach(int id, int fighterId, int coachId, LocalDate startDate, LocalDate endDate, boolean isActive) {
        this.id = id;
        this.fighterId = fighterId;
        this.coachId = coachId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = isActive;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFighterId() {
        return fighterId;
    }

    public void setFighterId(int fighterId) {
        this.fighterId = fighterId;
    }

    public int getCoachId() {
        return coachId;
    }

    public void setCoachId(int coachId) {
        this.coachId = coachId;
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

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return "FighterCoach{id=" + id + ", fighterId=" + fighterId + ", coachId=" + coachId +
                ", startDate=" + startDate + ", isActive=" + isActive + "}";
    }
}
