package services;

import entities.Fighter;
import entities.MatchProposal;
import entities.MatchmakingRule;
import entities.PerformanceScore;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * AI Matchmaking Service — rule-based, no ML required.
 * Generates fighter pairing proposals for a given event using matchmaking
 * rules.
 */
public class AIMatchmakingService {

    private Connection connection;
    private ServiceFighter serviceFighter;
    private ServiceMatchmakingRule serviceRule;
    private ServiceMatchProposal serviceProposal;
    private ServicePerformanceScore servicePerformance;

    public AIMatchmakingService() {
        connection = MyDatabase.getInstance().getConnection();
        serviceFighter = new ServiceFighter();
        serviceRule = new ServiceMatchmakingRule();
        serviceProposal = new ServiceMatchProposal();
        servicePerformance = new ServicePerformanceScore();
    }

    /**
     * Step 1: Load all fighters registered for the given event from event_fighter
     * table.
     */
    private List<Fighter> loadFightersForEvent(int eventId) throws SQLException {
        List<Fighter> fighters = new ArrayList<>();
        String req = "SELECT f.* FROM fighter f " +
                "INNER JOIN event_fighter ef ON f.id = ef.fighter_id " +
                "WHERE ef.event_id = " + eventId;
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            Fighter f = new Fighter(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("nickname"),
                    rs.getDate("date_of_birth") != null ? rs.getDate("date_of_birth").toLocalDate() : null,
                    rs.getString("nationality"),
                    rs.getInt("weight_class_id"),
                    rs.getInt("wins"),
                    rs.getInt("losses"),
                    rs.getInt("draws"),
                    rs.getString("status"));
            fighters.add(f);
        }
        return fighters;
    }

    /**
     * Generate match proposals for all fighter pairs in the event.
     * Uses 4 weighted rules to calculate a compatibility score (0–100).
     */
    public List<MatchProposal> generateProposals(int eventId) throws SQLException {
        List<Fighter> fighters = loadFightersForEvent(eventId);
        List<MatchmakingRule> rules = serviceRule.recuperer();
        List<MatchProposal> proposals = new ArrayList<>();

        if (fighters.size() < 2) {
            System.out.println("AIMatchmaking: not enough fighters registered for event " + eventId);
            return proposals;
        }

        // Filter only active rules
        List<MatchmakingRule> activeRules = new ArrayList<>();
        for (MatchmakingRule r : rules) {
            if (r.isActive())
                activeRules.add(r);
        }

        // Generate all unique pairs
        for (int i = 0; i < fighters.size(); i++) {
            for (int j = i + 1; j < fighters.size(); j++) {
                Fighter f1 = fighters.get(i);
                Fighter f2 = fighters.get(j);
                double compatibility = computeCompatibility(f1, f2, activeRules);
                MatchProposal proposal = new MatchProposal(
                        eventId, f1.getId(), f2.getId(), compatibility, "PENDING",
                        "AI-generated: " + f1.getNickname() + " vs " + f2.getNickname());
                serviceProposal.ajouter(proposal);
                proposals.add(proposal);
            }
        }
        System.out.println("AIMatchmaking: generated " + proposals.size() + " proposals for event " + eventId);
        return proposals;
    }

    /**
     * Rule-based compatibility calculation for a fighter pair.
     */
    private double computeCompatibility(Fighter f1, Fighter f2, List<MatchmakingRule> activeRules) throws SQLException {
        double total = 0.0;

        for (MatchmakingRule rule : activeRules) {
            String ruleName = rule.getName().toUpperCase();

            // Rule 1 — Weight class match
            if (ruleName.contains("WEIGHT")) {
                if (f1.getWeightClassId() == f2.getWeightClassId() && f1.getWeightClassId() > 0) {
                    total += rule.getWeight() * 40;
                }
            }
            // Rule 2 — Win rate balance
            else if (ruleName.contains("WIN") || ruleName.contains("RATE")) {
                double total1 = f1.getWins() + f1.getLosses() + f1.getDraws();
                double total2 = f2.getWins() + f2.getLosses() + f2.getDraws();
                double rate1 = (total1 > 0) ? f1.getWins() / total1 : 0.0;
                double rate2 = (total2 > 0) ? f2.getWins() / total2 : 0.0;
                if (Math.abs(rate1 - rate2) < 0.30)
                    total += rule.getWeight() * 30;
            }
            // Rule 3 — Experience (total fights)
            else if (ruleName.contains("EXPERIENCE") || ruleName.contains("EXP")) {
                int fights1 = f1.getWins() + f1.getLosses() + f1.getDraws();
                int fights2 = f2.getWins() + f2.getLosses() + f2.getDraws();
                if (Math.abs(fights1 - fights2) < 10)
                    total += rule.getWeight() * 20;
            }
            // Rule 4 — Performance score
            else if (ruleName.contains("PERFORMANCE") || ruleName.contains("SCORE")) {
                PerformanceScore ps1 = servicePerformance.recupererByFighter(f1.getId());
                PerformanceScore ps2 = servicePerformance.recupererByFighter(f2.getId());
                if (ps1 != null && ps2 != null) {
                    if (Math.abs(ps1.getScore() - ps2.getScore()) < 20)
                        total += rule.getWeight() * 10;
                }
            }
        }
        // Clamp to 0–100
        return Math.min(100.0, Math.max(0.0, total));
    }

    /**
     * Returns all match proposals for a given event from match_proposal table.
     */
    public List<MatchProposal> getProposalsByEvent(int eventId) throws SQLException {
        return serviceProposal.recupererByEvent(eventId);
    }
}
