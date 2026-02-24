package entities;

public class PerformanceScore {
    private int id;
    private int fighterId;
    private double score;
    private double aggression;
    private double defense;
    private double technique;
    private double experience;

    public PerformanceScore() {
    }

    public PerformanceScore(int fighterId, double score, double aggression, double defense, double technique,
            double experience) {
        this.fighterId = fighterId;
        this.score = score;
        this.aggression = aggression;
        this.defense = defense;
        this.technique = technique;
        this.experience = experience;
    }

    public PerformanceScore(int id, int fighterId, double score, double aggression, double defense, double technique,
            double experience) {
        this.id = id;
        this.fighterId = fighterId;
        this.score = score;
        this.aggression = aggression;
        this.defense = defense;
        this.technique = technique;
        this.experience = experience;
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

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getAggression() {
        return aggression;
    }

    public void setAggression(double aggression) {
        this.aggression = aggression;
    }

    public double getDefense() {
        return defense;
    }

    public void setDefense(double defense) {
        this.defense = defense;
    }

    public double getTechnique() {
        return technique;
    }

    public void setTechnique(double technique) {
        this.technique = technique;
    }

    public double getExperience() {
        return experience;
    }

    public void setExperience(double experience) {
        this.experience = experience;
    }

    @Override
    public String toString() {
        return "PerformanceScore{id=" + id + ", fighterId=" + fighterId + ", score=" + score + "}";
    }
}
