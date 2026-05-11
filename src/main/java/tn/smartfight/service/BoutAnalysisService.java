package tn.smartfight.service;

import tn.smartfight.model.FightStatistic;

import java.util.*;

/**
 * Java port of PHP BoutAnalysisService.
 * Provides statistical modeling: dominance score, efficiency index, tactical insights.
 */
public class BoutAnalysisService {

    public static class BoutAnalysis {
        public String summary = "";
        public double dominanceScore = 0;
        public int totalPunchesLanded = 0;
        public int knockdowns = 0;
        public double overallAccuracy = 0;
        public List<String> insights = new ArrayList<>();
    }

    /**
     * @param fightId       the resultId of the fight
     * @param winner        name of winner, null for draw
     * @param method        methodOfVictory string
     * @param roundEnded    round number the fight ended
     * @param decisionType  decision_type string
     * @param agg1          aggregated stats for fighter 1
     * @param agg2          aggregated stats for fighter 2
     * @param allRoundStats all per-round stats (both fighters)
     */
    public BoutAnalysis analyzeBout(
            int fightId,
            String winner,
            String method,
            int roundEnded,
            String decisionType,
            FightStatistic agg1,
            FightStatistic agg2,
            List<FightStatistic> allRoundStats
    ) {
        if (agg1 == null || agg2 == null) return empty();

        int totalLanded = agg1.getPunchesLanded() + agg2.getPunchesLanded();
        int totalThrown = agg1.getPunchesThrown() + agg2.getPunchesThrown();
        int totalKds    = agg1.getKnockdowns() + agg2.getKnockdowns();
        double overallAcc = totalThrown > 0 ? (totalLanded * 100.0 / totalThrown) : 0;

        double domScore = 5.0;
        FightStatistic winnerStat = null;
        FightStatistic loserStat  = null;

        boolean isDraw = (winner == null || winner.isBlank());

        if (!isDraw) {
            boolean isF1Winner = agg1.getFighterName() != null &&
                    agg1.getFighterName().toLowerCase().contains(extractLastName(winner).toLowerCase());
            winnerStat = isF1Winner ? agg1 : agg2;
            loserStat  = isF1Winner ? agg2 : agg1;

            double efficiencyIndex = loserStat.getPunchesLanded() > 0
                    ? (double) winnerStat.getPunchesLanded() / loserStat.getPunchesLanded()
                    : 2.5;
            double kdVector  = winnerStat.getKnockdowns() * 2.0;
            double accVector = (winnerStat.getPunchAccuracy() - loserStat.getPunchAccuracy()) * 0.1;
            double volVector = winnerStat.getPunchesThrown() > loserStat.getPunchesThrown() ? 0.5 : -0.5;
            domScore = Math.min(10.0, 5.0 + efficiencyIndex * 1.5 + kdVector + accVector + volVector);
        }

        List<String> analysis = new ArrayList<>();
        List<String> insights = new ArrayList<>();

        if (isDraw) {
            analysis.add("A grueling stalemate defined by extreme statistical parity. " +
                    "Neither combatant could definitively solve the other's defensive structure, " +
                    "resulting in a razor-close deadlock.");
        } else {
            String wName = extractLastName(winner);
            if ("KO".equalsIgnoreCase(method) || "TKO".equalsIgnoreCase(method)) {
                analysis.add("A clinical stoppage victory for " + wName +
                        ", who translated statistical pressure into a terminal finish in Round " + roundEnded + ".");
                insights.add(wName + " demonstrated elite 'Stop-Start' kinetic efficiency, " +
                        "concluding the bout before it reached the judges.");
            } else {
                String dtype = (decisionType != null && !decisionType.isBlank()) ? decisionType : "Decision";
                analysis.add("A strategic masterclass by " + wName +
                        ", utilizing superior ring generalship to secure a " + dtype + " over the distance.");
                insights.add("Tactical discipline and range management were the primary differentiators for the victor.");
            }
        }

        FightStatistic higherVol = agg1.getPunchesThrown() >= agg2.getPunchesThrown() ? agg1 : agg2;
        FightStatistic higherAcc = agg1.getPunchAccuracy() >= agg2.getPunchAccuracy() ? agg1 : agg2;
        String volName = lastName(higherVol);
        String accName = lastName(higherAcc);
        analysis.add(volName + " dictated the tempo with " + higherVol.getPunchesThrown() +
                " attempts, while " + accName + " provided the clinical counter-balance, landing with " +
                String.format("%.1f", higherAcc.getPunchAccuracy()) + "% precision.");

        int r1Wins = 0, r2Wins = 0;
        Map<Integer, List<FightStatistic>> roundMap = new LinkedHashMap<>();
        for (FightStatistic s : allRoundStats) {
            if (s.getRoundNumber() == null) continue;
            roundMap.computeIfAbsent(s.getRoundNumber(), k -> new ArrayList<>()).add(s);
        }

        FightStatistic lateLeader = null;
        int lateCount = 0;

        for (Map.Entry<Integer, List<FightStatistic>> entry : roundMap.entrySet()) {
            List<FightStatistic> rStats = entry.getValue();
            if (rStats.size() < 2) continue;
            FightStatistic r1 = rStats.get(0);
            FightStatistic r2 = rStats.get(1);
            boolean f1WinsRound = r1.getPunchesLanded() >= r2.getPunchesLanded();
            if (f1WinsRound) r1Wins++; else r2Wins++;
            if (entry.getKey() > 6) {
                FightStatistic rWinner = f1WinsRound ? r1 : r2;
                if (lateLeader != null && lateLeader.getFighterId() == rWinner.getFighterId()) {
                    lateCount++;
                } else {
                    lateLeader = rWinner;
                    lateCount = 1;
                }
            }
        }

        generateTacticalInsights(insights, agg1, agg2, r1Wins, r2Wins, lateLeader, lateCount);

        BoutAnalysis result = new BoutAnalysis();
        result.summary           = String.join(" ", analysis);
        result.dominanceScore    = Math.round(domScore * 10.0) / 10.0;
        result.totalPunchesLanded = totalLanded;
        result.knockdowns        = totalKds;
        result.overallAccuracy   = Math.round(overallAcc * 10.0) / 10.0;
        result.insights          = insights;
        return result;
    }

    private void generateTacticalInsights(List<String> insights,
                                          FightStatistic s1, FightStatistic s2,
                                          int r1Wins, int r2Wins,
                                          FightStatistic lateSurgeLeader, int surgeCount) {
        if (Math.abs(r1Wins - r2Wins) > 3) {
            FightStatistic dom = r1Wins > r2Wins ? s1 : s2;
            insights.add(lastName(dom) + " controlled the connective architecture, outlanding the opponent in " +
                    Math.max(r1Wins, r2Wins) + " distinct rounds.");
        }
        if (surgeCount >= 3 && lateSurgeLeader != null) {
            insights.add(lateSurgeLeader.getFighterName() + " demonstrated elite cardiovascular reserves, " +
                    "seizing total kinetic control during the championship rounds.");
        }
        int p1 = s1.getPowerPunchesLanded(), p2 = s2.getPowerPunchesLanded();
        if (Math.abs(p1 - p2) > 12) {
            FightStatistic pDom = p1 > p2 ? s1 : s2;
            insights.add(lastName(pDom) + " dominated the heavy-artillery exchanges, landing " +
                    Math.max(p1, p2) + " significant power shots.");
        }
        double acc1 = s1.getPunchAccuracy(), acc2 = s2.getPunchAccuracy();
        if (Math.abs(acc1 - acc2) > 15) {
            FightStatistic eff = acc1 > acc2 ? s1 : s2;
            insights.add("Precision gap identified: " + lastName(eff) +
                    " operated at a much higher efficiency threshold than the opposition.");
        }
    }

    private BoutAnalysis empty() {
        BoutAnalysis a = new BoutAnalysis();
        a.summary = "Insufficient kinetic data for professional modeling.";
        a.insights.add("Data integrity verification required for full tactical breakdown.");
        return a;
    }

    private String lastName(FightStatistic s) {
        if (s == null || s.getFighterName() == null) return "Fighter";
        String[] parts = s.getFighterName().trim().split("\\s+");
        return parts.length > 1 ? parts[parts.length - 1] : parts[0];
    }

    private String extractLastName(String fullName) {
        if (fullName == null || fullName.isBlank()) return "";
        String[] parts = fullName.trim().split("\\s+");
        return parts.length > 1 ? parts[parts.length - 1] : parts[0];
    }
}
