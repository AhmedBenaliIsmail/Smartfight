package entities;

public class Discipline {
    private int id;
    private String name;
    private String description;
    private String weightClass;
    private int roundDuration;
    private int maxRounds;

    public Discipline() {
    }

    public Discipline(String name, String description, String weightClass,
            int roundDuration, int maxRounds) {
        this.name = name;
        this.description = description;
        this.weightClass = weightClass;
        this.roundDuration = roundDuration;
        this.maxRounds = maxRounds;
    }

    public Discipline(int id, String name, String description, String weightClass,
            int roundDuration, int maxRounds) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.weightClass = weightClass;
        this.roundDuration = roundDuration;
        this.maxRounds = maxRounds;
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

    public String getWeightClass() {
        return weightClass;
    }

    public void setWeightClass(String weightClass) {
        this.weightClass = weightClass;
    }

    public int getRoundDuration() {
        return roundDuration;
    }

    public void setRoundDuration(int roundDuration) {
        this.roundDuration = roundDuration;
    }

    public int getMaxRounds() {
        return maxRounds;
    }

    public void setMaxRounds(int maxRounds) {
        this.maxRounds = maxRounds;
    }

    @Override
    public String toString() {
        return "Discipline{id=" + id + ", name='" + name + "', weightClass='" + weightClass + "', roundDuration="
                + roundDuration + ", maxRounds=" + maxRounds + "}";
    }
}
