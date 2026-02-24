package entities;

public class Ranking {
    private int id;
    private int fighterId;
    private int disciplineId;
    private int rankPosition;
    private double points;
    private String season;

    public Ranking() {
    }

    public Ranking(int fighterId, int disciplineId, int rankPosition, double points, String season) {
        this.fighterId = fighterId;
        this.disciplineId = disciplineId;
        this.rankPosition = rankPosition;
        this.points = points;
        this.season = season;
    }

    public Ranking(int id, int fighterId, int disciplineId, int rankPosition, double points, String season) {
        this.id = id;
        this.fighterId = fighterId;
        this.disciplineId = disciplineId;
        this.rankPosition = rankPosition;
        this.points = points;
        this.season = season;
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

    public int getDisciplineId() {
        return disciplineId;
    }

    public void setDisciplineId(int disciplineId) {
        this.disciplineId = disciplineId;
    }

    public int getRankPosition() {
        return rankPosition;
    }

    public void setRankPosition(int rankPosition) {
        this.rankPosition = rankPosition;
    }

    public double getPoints() {
        return points;
    }

    public void setPoints(double points) {
        this.points = points;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    @Override
    public String toString() {
        return "Ranking{id=" + id + ", fighterId=" + fighterId + ", rankPosition=" + rankPosition + ", points=" + points
                + ", season='" + season + "'}";
    }
}
