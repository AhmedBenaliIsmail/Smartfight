package tn.smartfight.model;

public class Ranking {
    private int id;
    private int fighterId;
    private int rankPosition;
    private String organization;
    private Integer weightDivisionId;
    private double points;

    // display (joined)
    private String fighterName;
    private String divisionName;

    public Ranking() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getFighterId() { return fighterId; }
    public void setFighterId(int fighterId) { this.fighterId = fighterId; }

    public int getRankPosition() { return rankPosition; }
    public void setRankPosition(int rankPosition) { this.rankPosition = rankPosition; }

    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }

    public Integer getWeightDivisionId() { return weightDivisionId; }
    public void setWeightDivisionId(Integer weightDivisionId) { this.weightDivisionId = weightDivisionId; }

    public double getPoints() { return points; }
    public void setPoints(double points) { this.points = points; }

    public String getFighterName() { return fighterName; }
    public void setFighterName(String fighterName) { this.fighterName = fighterName; }

    public String getDivisionName() { return divisionName; }
    public void setDivisionName(String divisionName) { this.divisionName = divisionName; }
}
