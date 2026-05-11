package tn.smartfight.model;

import java.time.LocalDateTime;

public class FightResult {
    private int resultId;
    private int fightNumber;
    private String methodOfVictory;
    private String decisionType;
    private int knockdownRound;
    private int roundNumber;
    private int scheduledRounds = 12;
    private boolean beltFight;
    private String beltOrganization;
    private double fighter1Odds;
    private double fighter2Odds;
    private LocalDateTime fightDate;
    private String status = "SCHEDULED";
    private int eventId;
    private int fighter1Id;
    private int fighter2Id;
    private Integer winnerId;
    private String insideTheNumbers;
    private String highlightVideoUrl;
    private String videoPath;
    private LocalDateTime updatedAt;

    // display-only (joined from related tables)
    private String eventName;
    private String fighter1Name;
    private String fighter2Name;
    private String winnerName;

    public FightResult() {}

    public int getResultId() { return resultId; }
    public void setResultId(int resultId) { this.resultId = resultId; }

    public int getFightNumber() { return fightNumber; }
    public void setFightNumber(int fightNumber) { this.fightNumber = fightNumber; }

    public String getMethodOfVictory() { return methodOfVictory; }
    public void setMethodOfVictory(String methodOfVictory) { this.methodOfVictory = methodOfVictory; }

    public String getDecisionType() { return decisionType; }
    public void setDecisionType(String decisionType) { this.decisionType = decisionType; }

    public int getKnockdownRound() { return knockdownRound; }
    public void setKnockdownRound(int knockdownRound) { this.knockdownRound = knockdownRound; }

    public int getRoundNumber() { return roundNumber; }
    public void setRoundNumber(int roundNumber) { this.roundNumber = roundNumber; }

    public int getScheduledRounds() { return scheduledRounds; }
    public void setScheduledRounds(int scheduledRounds) { this.scheduledRounds = scheduledRounds; }

    public boolean isBeltFight() { return beltFight; }
    public void setBeltFight(boolean beltFight) { this.beltFight = beltFight; }

    public String getBeltOrganization() { return beltOrganization; }
    public void setBeltOrganization(String beltOrganization) { this.beltOrganization = beltOrganization; }

    public double getFighter1Odds() { return fighter1Odds; }
    public void setFighter1Odds(double fighter1Odds) { this.fighter1Odds = fighter1Odds; }

    public double getFighter2Odds() { return fighter2Odds; }
    public void setFighter2Odds(double fighter2Odds) { this.fighter2Odds = fighter2Odds; }

    public LocalDateTime getFightDate() { return fightDate; }
    public void setFightDate(LocalDateTime fightDate) { this.fightDate = fightDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }

    public int getFighter1Id() { return fighter1Id; }
    public void setFighter1Id(int fighter1Id) { this.fighter1Id = fighter1Id; }

    public int getFighter2Id() { return fighter2Id; }
    public void setFighter2Id(int fighter2Id) { this.fighter2Id = fighter2Id; }

    public Integer getWinnerId() { return winnerId; }
    public void setWinnerId(Integer winnerId) { this.winnerId = winnerId; }

    public String getInsideTheNumbers() { return insideTheNumbers; }
    public void setInsideTheNumbers(String insideTheNumbers) { this.insideTheNumbers = insideTheNumbers; }

    public String getHighlightVideoUrl() { return highlightVideoUrl; }
    public void setHighlightVideoUrl(String highlightVideoUrl) { this.highlightVideoUrl = highlightVideoUrl; }

    public String getVideoPath() { return videoPath; }
    public void setVideoPath(String videoPath) { this.videoPath = videoPath; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getFighter1Name() { return fighter1Name; }
    public void setFighter1Name(String fighter1Name) { this.fighter1Name = fighter1Name; }

    public String getFighter2Name() { return fighter2Name; }
    public void setFighter2Name(String fighter2Name) { this.fighter2Name = fighter2Name; }

    public String getWinnerName() { return winnerName; }
    public void setWinnerName(String winnerName) { this.winnerName = winnerName; }
}
