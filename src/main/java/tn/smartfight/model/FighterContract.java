package tn.smartfight.model;

import java.math.BigDecimal;

public class FighterContract {
    private int id;
    private BigDecimal basePay;
    private BigDecimal winBonus;
    private BigDecimal calculatedPayout;
    private boolean isPaid;
    private int fighterId;
    private int eventId;
    private boolean missedWeight;
    private double managerFeePercent;

    // denormalized for display
    private String fighterName;
    private String eventName;

    public FighterContract() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public BigDecimal getBasePay() { return basePay; }
    public void setBasePay(BigDecimal basePay) { this.basePay = basePay; }

    public BigDecimal getWinBonus() { return winBonus; }
    public void setWinBonus(BigDecimal winBonus) { this.winBonus = winBonus; }

    public BigDecimal getCalculatedPayout() { return calculatedPayout; }
    public void setCalculatedPayout(BigDecimal calculatedPayout) { this.calculatedPayout = calculatedPayout; }

    public boolean isPaid() { return isPaid; }
    public void setPaid(boolean paid) { isPaid = paid; }

    public int getFighterId() { return fighterId; }
    public void setFighterId(int fighterId) { this.fighterId = fighterId; }

    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }

    public boolean isMissedWeight() { return missedWeight; }
    public void setMissedWeight(boolean missedWeight) { this.missedWeight = missedWeight; }

    public double getManagerFeePercent() { return managerFeePercent; }
    public void setManagerFeePercent(double managerFeePercent) { this.managerFeePercent = managerFeePercent; }

    public String getFighterName() { return fighterName; }
    public void setFighterName(String fighterName) { this.fighterName = fighterName; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
}
