package tn.smartfight.models;

public class FightResult {

    private int     id;
    private int     eventId;
    private String  eventName;
    private int     matchId;
    private int     fighterRedId;
    private String  fighterRedName;
    private int     fighterBlueId;
    private String  fighterBlueName;
    private int     winnerId;
    private String  winnerName;
    private String  method;
    private Integer roundEnded;
    private String  fightDate;
    private String  notes;

    public FightResult() {}

    // ── Helpers ──────────────────────────────────────────────────────────────

    public String getSummary() {
        return fighterRedName + " vs " + fighterBlueName + " — " + winnerName + " by " + method;
    }

    @Override
    public String toString() { return getSummary(); }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public int getId()              { return id; }
    public void setId(int id)       { this.id = id; }

    public int getEventId()                { return eventId; }
    public void setEventId(int eventId)    { this.eventId = eventId; }

    public String getEventName()                   { return eventName; }
    public void setEventName(String eventName)     { this.eventName = eventName; }

    public int getMatchId()                { return matchId; }
    public void setMatchId(int matchId)    { this.matchId = matchId; }

    public int getFighterRedId()                   { return fighterRedId; }
    public void setFighterRedId(int id)            { this.fighterRedId = id; }

    public String getFighterRedName()                  { return fighterRedName; }
    public void setFighterRedName(String name)         { this.fighterRedName = name; }

    public int getFighterBlueId()                  { return fighterBlueId; }
    public void setFighterBlueId(int id)           { this.fighterBlueId = id; }

    public String getFighterBlueName()                 { return fighterBlueName; }
    public void setFighterBlueName(String name)        { this.fighterBlueName = name; }

    public int getWinnerId()               { return winnerId; }
    public void setWinnerId(int winnerId)  { this.winnerId = winnerId; }

    public String getWinnerName()                  { return winnerName; }
    public void setWinnerName(String winnerName)   { this.winnerName = winnerName; }

    public String getMethod()              { return method; }
    public void setMethod(String method)   { this.method = method; }

    public Integer getRoundEnded()                     { return roundEnded; }
    public void setRoundEnded(Integer roundEnded)      { this.roundEnded = roundEnded; }

    public String getFightDate()                   { return fightDate; }
    public void setFightDate(String fightDate)     { this.fightDate = fightDate; }

    public String getNotes()               { return notes; }
    public void setNotes(String notes)     { this.notes = notes; }
}
