package entities;

public class FightStatistic {
    private int id;
    private int fightResultId;
    private int fighterId;
    private int strikesLanded;
    private int strikesThrown;
    private int takedowns;
    private int submissions;
    private int knockdowns;

    public FightStatistic() {
    }

    public FightStatistic(int fightResultId, int fighterId, int strikesLanded, int strikesThrown,
            int takedowns, int submissions, int knockdowns) {
        this.fightResultId = fightResultId;
        this.fighterId = fighterId;
        this.strikesLanded = strikesLanded;
        this.strikesThrown = strikesThrown;
        this.takedowns = takedowns;
        this.submissions = submissions;
        this.knockdowns = knockdowns;
    }

    public FightStatistic(int id, int fightResultId, int fighterId, int strikesLanded, int strikesThrown,
            int takedowns, int submissions, int knockdowns) {
        this.id = id;
        this.fightResultId = fightResultId;
        this.fighterId = fighterId;
        this.strikesLanded = strikesLanded;
        this.strikesThrown = strikesThrown;
        this.takedowns = takedowns;
        this.submissions = submissions;
        this.knockdowns = knockdowns;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFightResultId() {
        return fightResultId;
    }

    public void setFightResultId(int fightResultId) {
        this.fightResultId = fightResultId;
    }

    public int getFighterId() {
        return fighterId;
    }

    public void setFighterId(int fighterId) {
        this.fighterId = fighterId;
    }

    public int getStrikesLanded() {
        return strikesLanded;
    }

    public void setStrikesLanded(int strikesLanded) {
        this.strikesLanded = strikesLanded;
    }

    public int getStrikesThrown() {
        return strikesThrown;
    }

    public void setStrikesThrown(int strikesThrown) {
        this.strikesThrown = strikesThrown;
    }

    public int getTakedowns() {
        return takedowns;
    }

    public void setTakedowns(int takedowns) {
        this.takedowns = takedowns;
    }

    public int getSubmissions() {
        return submissions;
    }

    public void setSubmissions(int submissions) {
        this.submissions = submissions;
    }

    public int getKnockdowns() {
        return knockdowns;
    }

    public void setKnockdowns(int knockdowns) {
        this.knockdowns = knockdowns;
    }

    @Override
    public String toString() {
        return "FightStatistic{id=" + id + ", fightResultId=" + fightResultId + ", fighterId=" + fighterId + "}";
    }
}
