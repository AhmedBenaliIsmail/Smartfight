package tn.smartfight.model;

import java.time.LocalDate;

public class Fighter {
    private int fighterId;
    private String firstName;
    private String lastName;
    private String nickname;
    private String nationality;
    private String photoFilename;
    private int wins;
    private int losses;
    private int draws;
    private int koWins;
    private int technicalWins;
    private int decisionWins;
    private int koLosses;
    private double eloRating;
    private double performanceScore;
    private int winStreak;
    private double strengthOfSchedule;
    private LocalDate lastFightDate;
    private int titleDefenses;
    private Integer height;
    private Integer reach;
    private Integer weight;
    private Integer age;
    private Integer weightDivisionId;
    private Integer managerId;
    private int strikesThrown;
    private int strikesLanded;
    private String aiStyleTag;
    private String aiDescription;
    private String strength;
    private String weakness;

    public Fighter() {}

    public int getId() { return fighterId; }
    public int getFighterId() { return fighterId; }
    public void setFighterId(int fighterId) { this.fighterId = fighterId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    public String getPhotoFilename() { return photoFilename; }
    public void setPhotoFilename(String photoFilename) { this.photoFilename = photoFilename; }

    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }

    public int getLosses() { return losses; }
    public void setLosses(int losses) { this.losses = losses; }

    public int getDraws() { return draws; }
    public void setDraws(int draws) { this.draws = draws; }

    public int getKoWins() { return koWins; }
    public void setKoWins(int koWins) { this.koWins = koWins; }

    public int getTechnicalWins() { return technicalWins; }
    public void setTechnicalWins(int technicalWins) { this.technicalWins = technicalWins; }

    public int getDecisionWins() { return decisionWins; }
    public void setDecisionWins(int decisionWins) { this.decisionWins = decisionWins; }

    public int getKoLosses() { return koLosses; }
    public void setKoLosses(int koLosses) { this.koLosses = koLosses; }

    public double getEloRating() { return eloRating; }
    public void setEloRating(double eloRating) { this.eloRating = eloRating; }

    public double getPerformanceScore() { return performanceScore; }
    public void setPerformanceScore(double performanceScore) { this.performanceScore = performanceScore; }

    public int getWinStreak() { return winStreak; }
    public void setWinStreak(int winStreak) { this.winStreak = winStreak; }

    public double getStrengthOfSchedule() { return strengthOfSchedule; }
    public void setStrengthOfSchedule(double strengthOfSchedule) { this.strengthOfSchedule = strengthOfSchedule; }

    public LocalDate getLastFightDate() { return lastFightDate; }
    public void setLastFightDate(LocalDate lastFightDate) { this.lastFightDate = lastFightDate; }

    public int getTitleDefenses() { return titleDefenses; }
    public void setTitleDefenses(int titleDefenses) { this.titleDefenses = titleDefenses; }

    public Integer getHeight() { return height; }
    public void setHeight(Integer height) { this.height = height; }

    public Integer getReach() { return reach; }
    public void setReach(Integer reach) { this.reach = reach; }

    public Integer getWeight() { return weight; }
    public void setWeight(Integer weight) { this.weight = weight; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public Integer getWeightDivisionId() { return weightDivisionId; }
    public void setWeightDivisionId(Integer weightDivisionId) { this.weightDivisionId = weightDivisionId; }

    public Integer getManagerId() { return managerId; }
    public void setManagerId(Integer managerId) { this.managerId = managerId; }

    public int getStrikesThrown() { return strikesThrown; }
    public void setStrikesThrown(int strikesThrown) { this.strikesThrown = strikesThrown; }

    public int getStrikesLanded() { return strikesLanded; }
    public void setStrikesLanded(int strikesLanded) { this.strikesLanded = strikesLanded; }

    public String getAiStyleTag() { return aiStyleTag; }
    public void setAiStyleTag(String aiStyleTag) { this.aiStyleTag = aiStyleTag; }

    public String getAiDescription() { return aiDescription; }
    public void setAiDescription(String aiDescription) { this.aiDescription = aiDescription; }

    public String getStrength() { return strength; }
    public void setStrength(String strength) { this.strength = strength; }

    public String getWeakness() { return weakness; }
    public void setWeakness(String weakness) { this.weakness = weakness; }

    public String getRecord() {
        return wins + "-" + losses + (draws > 0 ? "-" + draws : "");
    }
}
