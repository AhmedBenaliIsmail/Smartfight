package entities;

public class JudgeScore {
    private int id;
    private int fightResultId;
    private String judgeName;
    private int scoreRed;
    private int scoreBlue;

    public JudgeScore() {
    }

    public JudgeScore(int fightResultId, String judgeName, int scoreRed, int scoreBlue) {
        this.fightResultId = fightResultId;
        this.judgeName = judgeName;
        this.scoreRed = scoreRed;
        this.scoreBlue = scoreBlue;
    }

    public JudgeScore(int id, int fightResultId, String judgeName, int scoreRed, int scoreBlue) {
        this.id = id;
        this.fightResultId = fightResultId;
        this.judgeName = judgeName;
        this.scoreRed = scoreRed;
        this.scoreBlue = scoreBlue;
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

    public String getJudgeName() {
        return judgeName;
    }

    public void setJudgeName(String judgeName) {
        this.judgeName = judgeName;
    }

    public int getScoreRed() {
        return scoreRed;
    }

    public void setScoreRed(int scoreRed) {
        this.scoreRed = scoreRed;
    }

    public int getScoreBlue() {
        return scoreBlue;
    }

    public void setScoreBlue(int scoreBlue) {
        this.scoreBlue = scoreBlue;
    }

    @Override
    public String toString() {
        return "JudgeScore{id=" + id + ", fightResultId=" + fightResultId + ", judge='" + judgeName + "', red="
                + scoreRed + ", blue=" + scoreBlue + "}";
    }
}
