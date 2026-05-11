package tn.smartfight.service;

import tn.smartfight.model.FighterContract;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ContractService {

    public record PurseBreakdown(
            BigDecimal calculatedPayout,
            BigDecimal netToFighter,
            BigDecimal managerFee) {}

    // §5.5 — always call this, never use basePay directly (fix for Symfony bug)
    public PurseBreakdown calculateFinalPurse(FighterContract contract, boolean isWinner) {
        BigDecimal base = contract.getBasePay();
        if (contract.isMissedWeight()) {
            base = base.multiply(BigDecimal.valueOf(0.8)).setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal winBonus = isWinner ? contract.getWinBonus() : BigDecimal.ZERO;
        BigDecimal total = base.add(winBonus).setScale(2, RoundingMode.HALF_UP);

        double feeRate = contract.getManagerFeePercent();
        BigDecimal managerFee = total.multiply(BigDecimal.valueOf(feeRate)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netToFighter = total.subtract(managerFee).setScale(2, RoundingMode.HALF_UP);

        return new PurseBreakdown(total, netToFighter, managerFee);
    }
}
