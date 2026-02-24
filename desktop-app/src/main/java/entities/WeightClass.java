package entities;

public class WeightClass {
    private int id;
    private String name;
    private double minWeight;
    private double maxWeight;

    public WeightClass() {
    }

    public WeightClass(String name, double minWeight, double maxWeight) {
        this.name = name;
        this.minWeight = minWeight;
        this.maxWeight = maxWeight;
    }

    public WeightClass(int id, String name, double minWeight, double maxWeight) {
        this.id = id;
        this.name = name;
        this.minWeight = minWeight;
        this.maxWeight = maxWeight;
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

    public double getMinWeight() {
        return minWeight;
    }

    public void setMinWeight(double minWeight) {
        this.minWeight = minWeight;
    }

    public double getMaxWeight() {
        return maxWeight;
    }

    public void setMaxWeight(double maxWeight) {
        this.maxWeight = maxWeight;
    }

    @Override
    public String toString() {
        return "WeightClass{id=" + id + ", name='" + name + "', min=" + minWeight + ", max=" + maxWeight + "}";
    }
}
