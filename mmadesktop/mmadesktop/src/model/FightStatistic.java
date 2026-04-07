package model;

public class FightStatistic {
    private int id;
    private int fightResultId;
    private int fighterId;
    private int strikesLanded;
    private int strikesThrown;
    private int takedowns;          // takedowns landed
    private int takedownAttempts;   // NEW: needed for accuracy
    private int submissions;
    private int knockdowns;

    // Constructors
    public FightStatistic() {
        this.takedownAttempts = 0;  // default
    }

    public FightStatistic(int fightResultId, int fighterId, int strikesLanded, int strikesThrown,
                          int takedowns, int takedownAttempts, int submissions, int knockdowns) {
        this.fightResultId = fightResultId;
        this.fighterId = fighterId;
        this.strikesLanded = strikesLanded;
        this.strikesThrown = strikesThrown;
        this.takedowns = takedowns;
        this.takedownAttempts = takedownAttempts;
        this.submissions = submissions;
        this.knockdowns = knockdowns;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getFightResultId() { return fightResultId; }
    public void setFightResultId(int fightResultId) { this.fightResultId = fightResultId; }

    public int getFighterId() { return fighterId; }
    public void setFighterId(int fighterId) { this.fighterId = fighterId; }

    public int getStrikesLanded() { return strikesLanded; }
    public void setStrikesLanded(int strikesLanded) { this.strikesLanded = strikesLanded; }

    public int getStrikesThrown() { return strikesThrown; }
    public void setStrikesThrown(int strikesThrown) { this.strikesThrown = strikesThrown; }

    public int getTakedowns() { return takedowns; }
    public void setTakedowns(int takedowns) { this.takedowns = takedowns; }

    public int getTakedownAttempts() { return takedownAttempts; }
    public void setTakedownAttempts(int takedownAttempts) { this.takedownAttempts = takedownAttempts; }

    public int getSubmissions() { return submissions; }
    public void setSubmissions(int submissions) { this.submissions = submissions; }

    public int getKnockdowns() { return knockdowns; }
    public void setKnockdowns(int knockdowns) { this.knockdowns = knockdowns; }

    // ========== HELPER METHODS FOR PERFORMANCE SCORE ==========

    /**
     * Strike accuracy as a percentage (0-100)
     */
    public double getStrikeAccuracy() {
        if (strikesThrown == 0) return 0.0;
        return (strikesLanded * 100.0) / strikesThrown;
    }

    /**
     * Takedown accuracy as a percentage (0-100)
     */
    public double getTakedownAccuracy() {
        if (takedownAttempts == 0) return 0.0;
        return (takedowns * 100.0) / takedownAttempts;
    }

    /**
     * Contribution to performance score for this single fight.
     * Weights: strike accuracy (0.3), takedown accuracy (0.25),
     *          submissions (0.25 per sub, but capped), knockdowns (0.2 per KD)
     * This is used to aggregate over multiple fights.
     */
    public double getFightPerformanceContribution() {
        double strikeScore = getStrikeAccuracy();               // 0-100
        double takedownScore = getTakedownAccuracy();           // 0-100
        double subScore = Math.min(submissions * 10, 30);       // each sub adds 10%, max 30%
        double kdScore = Math.min(knockdowns * 10, 30);         // each KD adds 10%, max 30%

        return (strikeScore * 0.3) + (takedownScore * 0.25) + (subScore * 0.25) + (kdScore * 0.2);
    }
}