package model;

import java.time.LocalDateTime;

public class PerformanceScore {
    private int id;
    private int fighterId;
    private double score;
    private Double aggression;   // can be null
    private Double defense;
    private Double technique;
    private Double experience;
    private LocalDateTime calculatedAt;

    public PerformanceScore() {}

    public PerformanceScore(int fighterId, double score, Double aggression, Double defense,
                            Double technique, Double experience, LocalDateTime calculatedAt) {
        this.fighterId = fighterId;
        this.score = score;
        this.aggression = aggression;
        this.defense = defense;
        this.technique = technique;
        this.experience = experience;
        this.calculatedAt = calculatedAt;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getFighterId() { return fighterId; }
    public void setFighterId(int fighterId) { this.fighterId = fighterId; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public Double getAggression() { return aggression; }
    public void setAggression(Double aggression) { this.aggression = aggression; }

    public Double getDefense() { return defense; }
    public void setDefense(Double defense) { this.defense = defense; }

    public Double getTechnique() { return technique; }
    public void setTechnique(Double technique) { this.technique = technique; }

    public Double getExperience() { return experience; }
    public void setExperience(Double experience) { this.experience = experience; }

    public LocalDateTime getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(LocalDateTime calculatedAt) { this.calculatedAt = calculatedAt; }
}