package tn.smartfight.model;

public class FightStatistic {
    private int id;
    private int fightId;
    private int fighterId;
    private Integer roundNumber; // null = aggregate row
    private int punchesThrown, punchesLanded;
    private int rightHandThrown, rightHandLanded;
    private int leftHandThrown, leftHandLanded;
    private int powerPunchesThrown, powerPunchesLanded;
    private int jabsThrown, jabsLanded;
    private int uppercutsThrown, uppercutsLanded;
    private int bodyShotsLanded;
    private int knockdowns;
    private String commentary;
    private String fighterName;

    public FightStatistic() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getFightId() { return fightId; }
    public void setFightId(int fightId) { this.fightId = fightId; }

    public int getFighterId() { return fighterId; }
    public void setFighterId(int fighterId) { this.fighterId = fighterId; }

    public Integer getRoundNumber() { return roundNumber; }
    public void setRoundNumber(Integer roundNumber) { this.roundNumber = roundNumber; }

    public int getPunchesThrown() { return punchesThrown; }
    public void setPunchesThrown(int v) { this.punchesThrown = v; }

    public int getPunchesLanded() { return punchesLanded; }
    public void setPunchesLanded(int v) { this.punchesLanded = v; }

    public int getRightHandThrown() { return rightHandThrown; }
    public void setRightHandThrown(int v) { this.rightHandThrown = v; }

    public int getRightHandLanded() { return rightHandLanded; }
    public void setRightHandLanded(int v) { this.rightHandLanded = v; }

    public int getLeftHandThrown() { return leftHandThrown; }
    public void setLeftHandThrown(int v) { this.leftHandThrown = v; }

    public int getLeftHandLanded() { return leftHandLanded; }
    public void setLeftHandLanded(int v) { this.leftHandLanded = v; }

    public int getPowerPunchesThrown() { return powerPunchesThrown; }
    public void setPowerPunchesThrown(int v) { this.powerPunchesThrown = v; }

    public int getPowerPunchesLanded() { return powerPunchesLanded; }
    public void setPowerPunchesLanded(int v) { this.powerPunchesLanded = v; }

    public int getJabsThrown() { return jabsThrown; }
    public void setJabsThrown(int v) { this.jabsThrown = v; }

    public int getJabsLanded() { return jabsLanded; }
    public void setJabsLanded(int v) { this.jabsLanded = v; }

    public int getUppercutsThrown() { return uppercutsThrown; }
    public void setUppercutsThrown(int v) { this.uppercutsThrown = v; }

    public int getUppercutsLanded() { return uppercutsLanded; }
    public void setUppercutsLanded(int v) { this.uppercutsLanded = v; }

    public int getBodyShotsLanded() { return bodyShotsLanded; }
    public void setBodyShotsLanded(int v) { this.bodyShotsLanded = v; }

    public int getKnockdowns() { return knockdowns; }
    public void setKnockdowns(int v) { this.knockdowns = v; }

    public String getCommentary() { return commentary; }
    public void setCommentary(String commentary) { this.commentary = commentary; }

    public String getFighterName() { return fighterName; }
    public void setFighterName(String fighterName) { this.fighterName = fighterName; }

    public double getPunchAccuracy() {
        return punchesThrown > 0 ? punchesLanded * 100.0 / punchesThrown : 0;
    }

    public double getPowerAccuracy() {
        return powerPunchesThrown > 0 ? powerPunchesLanded * 100.0 / powerPunchesThrown : 0;
    }

    public double getJabAccuracy() {
        return jabsThrown > 0 ? jabsLanded * 100.0 / jabsThrown : 0;
    }

    public double getUppercutAccuracy() {
        return uppercutsThrown > 0 ? uppercutsLanded * 100.0 / uppercutsThrown : 0;
    }
}
