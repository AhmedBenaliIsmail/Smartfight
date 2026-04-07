package model;

public class Fighter {
    private int fighterId;
    private String firstName;
    private String lastName;
    private String nickname;
    private String weightClass;
    private String country;
    private int wins;
    private int losses;
    private int draws;
    private int koWins;
    private int submissionWins;
    private int decisionWins;

    // ========== NEW FIELDS FOR ADVANCED RANKING ==========
    private double eloRating;           // dynamic ELO score (default 1500)
    private double performanceScore;    // computed from fight statistics
    private int winStreak;              // current consecutive wins
    private double strengthOfSchedule;  // average ELO of last 5 opponents

    // ====================================================

    public Fighter() {
        this.eloRating = 1500.0;        // default starting ELO
        this.performanceScore = 0.0;
        this.winStreak = 0;
        this.strengthOfSchedule = 1500.0;
    }

    public Fighter(String firstName, String lastName, String nickname, String weightClass, String country) {
        this();
        this.firstName = firstName;
        this.lastName = lastName;
        this.nickname = nickname;
        this.weightClass = weightClass;
        this.country = country;
    }

    // --- Getters and Setters (existing) ---
    public int getFighterId() { return fighterId; }
    public void setFighterId(int fighterId) { this.fighterId = fighterId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getWeightClass() { return weightClass; }
    public void setWeightClass(String weightClass) { this.weightClass = weightClass; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }

    public int getLosses() { return losses; }
    public void setLosses(int losses) { this.losses = losses; }

    public int getDraws() { return draws; }
    public void setDraws(int draws) { this.draws = draws; }

    public int getKoWins() { return koWins; }
    public void setKoWins(int koWins) { this.koWins = koWins; }

    public int getSubmissionWins() { return submissionWins; }
    public void setSubmissionWins(int submissionWins) { this.submissionWins = submissionWins; }

    public int getDecisionWins() { return decisionWins; }
    public void setDecisionWins(int decisionWins) { this.decisionWins = decisionWins; }

    // --- NEW Getters and Setters for advanced ranking ---
    public double getEloRating() { return eloRating; }
    public void setEloRating(double eloRating) { this.eloRating = eloRating; }

    public double getPerformanceScore() { return performanceScore; }
    public void setPerformanceScore(double performanceScore) { this.performanceScore = performanceScore; }

    public int getWinStreak() { return winStreak; }
    public void setWinStreak(int winStreak) { this.winStreak = winStreak; }

    public double getStrengthOfSchedule() { return strengthOfSchedule; }
    public void setStrengthOfSchedule(double strengthOfSchedule) { this.strengthOfSchedule = strengthOfSchedule; }

    // --- Utility Methods ---
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public int getTotalFights() {
        return wins + losses + draws;
    }

    @Override
    public String toString() {
        return getFullName() + " (" + weightClass + ") | ELO: " + (int) eloRating;
    }
}