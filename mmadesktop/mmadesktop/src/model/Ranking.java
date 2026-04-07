package model;

import java.time.LocalDate;

public class Ranking {
    private int id;
    private int fighterId;
    private int weightClassId;
    private int rankPosition;
    private double points;
    private String season;
    private LocalDate lastUpdated;
    private String fighterName;
    private String weightClassName;

    public Ranking() {
        this.lastUpdated = LocalDate.now();
    }

    // Constructor used by AnalyticsEngine
    public Ranking(int fighterId, int rankPosition, double points, String season) {
        this();
        this.fighterId = fighterId;
        this.rankPosition = rankPosition;
        this.points = points;
        this.season = season;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getFighterId() { return fighterId; }
    public void setFighterId(int fighterId) { this.fighterId = fighterId; }
    public int getWeightClassId() { return weightClassId; }
    public void setWeightClassId(int weightClassId) { this.weightClassId = weightClassId; }
    public int getRankPosition() { return rankPosition; }
    public void setRankPosition(int rankPosition) { this.rankPosition = rankPosition; }
    public double getPoints() { return points; }
    public void setPoints(double points) { this.points = points; }
    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }
    public LocalDate getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDate lastUpdated) { this.lastUpdated = lastUpdated; }
    public String getFighterName() { return fighterName; }
    public void setFighterName(String fighterName) { this.fighterName = fighterName; }
    public String getWeightClassName() { return weightClassName; }
    public void setWeightClassName(String weightClassName) { this.weightClassName = weightClassName; }

    // Backward-compatible aliases
    public int getRank() { return getRankPosition(); }
    public void setRank(int rank) { setRankPosition(rank); }
    public double getScore() { return getPoints(); }
    public void setScore(double score) { setPoints(score); }
}