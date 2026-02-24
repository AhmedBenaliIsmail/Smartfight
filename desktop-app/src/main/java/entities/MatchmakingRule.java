package entities;

public class MatchmakingRule {
    private int id;
    private String name;
    private String description;
    private double weight;
    private boolean isActive;

    public MatchmakingRule() {
    }

    public MatchmakingRule(String name, String description, double weight, boolean isActive) {
        this.name = name;
        this.description = description;
        this.weight = weight;
        this.isActive = isActive;
    }

    public MatchmakingRule(int id, String name, String description, double weight, boolean isActive) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.weight = weight;
        this.isActive = isActive;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return "MatchmakingRule{id=" + id + ", name='" + name + "', weight=" + weight + ", isActive=" + isActive + "}";
    }
}
