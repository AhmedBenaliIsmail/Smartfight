package entities;

import java.time.LocalDate;

public class FightResult {
    private int id;
    private int eventId;
    private int matchId;
    private int fighterRedId;
    private int fighterBlueId;
    private int winnerId;
    private String method;
    private int roundEnded;
    private LocalDate fightDate;
    private String notes;

    public FightResult() {
    }

    public FightResult(int eventId, int matchId, int fighterRedId, int fighterBlueId,
            int winnerId, String method, int roundEnded, LocalDate fightDate, String notes) {
        this.eventId = eventId;
        this.matchId = matchId;
        this.fighterRedId = fighterRedId;
        this.fighterBlueId = fighterBlueId;
        this.winnerId = winnerId;
        this.method = method;
        this.roundEnded = roundEnded;
        this.fightDate = fightDate;
        this.notes = notes;
    }

    public FightResult(int id, int eventId, int matchId, int fighterRedId, int fighterBlueId,
            int winnerId, String method, int roundEnded, LocalDate fightDate, String notes) {
        this.id = id;
        this.eventId = eventId;
        this.matchId = matchId;
        this.fighterRedId = fighterRedId;
        this.fighterBlueId = fighterBlueId;
        this.winnerId = winnerId;
        this.method = method;
        this.roundEnded = roundEnded;
        this.fightDate = fightDate;
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

    public int getMatchId() {
        return matchId;
    }

    public void setMatchId(int matchId) {
        this.matchId = matchId;
    }

    public int getFighterRedId() {
        return fighterRedId;
    }

    public void setFighterRedId(int fighterRedId) {
        this.fighterRedId = fighterRedId;
    }

    public int getFighterBlueId() {
        return fighterBlueId;
    }

    public void setFighterBlueId(int fighterBlueId) {
        this.fighterBlueId = fighterBlueId;
    }

    public int getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(int winnerId) {
        this.winnerId = winnerId;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public int getRoundEnded() {
        return roundEnded;
    }

    public void setRoundEnded(int roundEnded) {
        this.roundEnded = roundEnded;
    }

    public LocalDate getFightDate() {
        return fightDate;
    }

    public void setFightDate(LocalDate fightDate) {
        this.fightDate = fightDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "FightResult{id=" + id + ", eventId=" + eventId + ", method='" + method + "', fightDate=" + fightDate
                + "}";
    }
}
