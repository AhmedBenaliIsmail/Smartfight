package model;

import java.time.LocalDateTime;

public class FightResult {
    // Constants for method of victory
    public static final String METHOD_KO_TKO = "KO/TKO";
    public static final String METHOD_SUBMISSION = "SUBMISSION";
    public static final String METHOD_DECISION = "DECISION";
    public static final String METHOD_DQ = "DISQUALIFICATION";
    public static final String METHOD_DRAW = "DRAW";

    private int resultId;
    private int eventId;
    private int fightNumber;
    private int fighter1Id;
    private int fighter2Id;
    private Integer winnerId;
    private String methodOfVictory;
    private int roundNumber;
    private LocalDateTime fightDate;
    private String status;

    public FightResult() {
        this.status = "SCHEDULED";
    }

    // --- Getters and Setters (existing) ---
    public int getResultId() { return resultId; }
    public void setResultId(int resultId) { this.resultId = resultId; }

    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }

    public int getFightNumber() { return fightNumber; }
    public void setFightNumber(int fightNumber) { this.fightNumber = fightNumber; }

    public int getFighter1Id() { return fighter1Id; }
    public void setFighter1Id(int fighter1Id) { this.fighter1Id = fighter1Id; }

    public int getFighter2Id() { return fighter2Id; }
    public void setFighter2Id(int fighter2Id) { this.fighter2Id = fighter2Id; }

    public Integer getWinnerId() { return winnerId; }
    public void setWinnerId(Integer winnerId) { this.winnerId = winnerId; }

    public String getMethodOfVictory() { return methodOfVictory; }
    public void setMethodOfVictory(String methodOfVictory) { this.methodOfVictory = methodOfVictory; }

    public int getRoundNumber() { return roundNumber; }
    public void setRoundNumber(int roundNumber) { this.roundNumber = roundNumber; }

    public LocalDateTime getFightDate() { return fightDate; }
    public void setFightDate(LocalDateTime fightDate) { this.fightDate = fightDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // ========== HELPER METHODS FOR RANKING ==========

    /**
     * Returns the ID of the loser (the fighter who is not the winner).
     * If the fight is a draw or not completed, returns null.
     */
    public Integer getLoserId() {
        if (winnerId == null || status.equals("DRAW") || !status.equals("COMPLETED")) {
            return null;
        }
        return (winnerId == fighter1Id) ? fighter2Id : fighter1Id;
    }

    /**
     * Returns bonus points based on method of victory.
     * Used in ELO calculation to reward spectacular finishes.
     */
    public int getMethodBonus() {
        if (methodOfVictory == null) return 0;
        switch (methodOfVictory.toUpperCase()) {
            case METHOD_KO_TKO:
                return 10;
            case METHOD_SUBMISSION:
                return 8;
            case METHOD_DECISION:
                return 5;
            case METHOD_DQ:
                return 3;
            default:
                return 0;
        }
    }

    /**
     * Checks if the fight is a draw (no winner).
     */
    public boolean isDraw() {
        return winnerId == null && "COMPLETED".equals(status);
    }
}