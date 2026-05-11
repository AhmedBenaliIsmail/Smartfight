package tn.smartfight.service;

import tn.smartfight.dao.FighterDao;
import tn.smartfight.dao.MatchProposalDao;
import tn.smartfight.model.Fighter;
import tn.smartfight.model.MatchProposal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MatchmakingService {
    private static final Logger LOG = Logger.getLogger(MatchmakingService.class.getName());

    private final FighterDao fighterDao;
    private final MatchProposalDao proposalDao;

    public MatchmakingService() { this(new FighterDao(), new MatchProposalDao()); }
    public MatchmakingService(FighterDao fd, MatchProposalDao pd) {
        this.fighterDao = fd;
        this.proposalDao = pd;
    }

    // ── §5.7 8-vector Score IA ─────────────────────────────────────────────────

    public double computeScoreIA(Fighter f1, Fighter f2, Set<Integer> usedDivisionIds) {
        int tf1 = Math.max(f1.getWins() + f1.getLosses(), 1);
        int tf2 = Math.max(f2.getWins() + f2.getLosses(), 1);

        // 1. Weight integrity: 25
        Integer div1 = f1.getWeightDivisionId();
        Integer div2 = f2.getWeightDivisionId();
        double v1 = (div1 != null && div1.equals(div2)) ? 25 : 0;

        // 2. ELO parity: 20
        double eloDiff = Math.abs(f1.getEloRating() - f2.getEloRating());
        double v2 = Math.max(0, 20 * (1 - eloDiff / 400));

        // 3. Record parity: 15
        double wr1 = (double) f1.getWins() / tf1;
        double wr2 = (double) f2.getWins() / tf2;
        double v3 = Math.max(0, 15 * (1 - Math.abs(wr1 - wr2)));

        // 4. Height balance: 8
        double v4;
        if (f1.getHeight() != null && f2.getHeight() != null) {
            v4 = Math.max(0, 8 * (1 - Math.abs(f1.getHeight() - f2.getHeight()) / 30.0));
        } else {
            v4 = 4;
        }

        // 5. Reach balance: 7
        double v5;
        if (f1.getReach() != null && f2.getReach() != null) {
            v5 = Math.max(0, 7 * (1 - Math.abs(f1.getReach() - f2.getReach()) / 30.0));
        } else {
            v5 = 3.5;
        }

        // 6. Lethality (KO ratio): 10
        double ko1 = f1.getWins() > 0 ? (double) f1.getKoWins() / f1.getWins() : 0;
        double ko2 = f2.getWins() > 0 ? (double) f2.getKoWins() / f2.getWins() : 0;
        double v6 = Math.max(0, 10 * (1 - Math.abs(ko1 - ko2)));

        // 7. Precision (accuracy): 10
        double acc1 = f1.getStrikesThrown() > 0 ? (double) f1.getStrikesLanded() / f1.getStrikesThrown() * 100 : 0;
        double acc2 = f2.getStrikesThrown() > 0 ? (double) f2.getStrikesLanded() / f2.getStrikesThrown() * 100 : 0;
        double v7 = Math.max(0, 10 * (1 - Math.abs(acc1 - acc2) / 100.0));

        // 8. Diversity: 5
        double v8 = (div1 == null || !usedDivisionIds.contains(div1)) ? 5 : 0;

        double total = v1 + v2 + v3 + v4 + v5 + v6 + v7 + v8;
        return Math.min(100, Math.round(total * 100.0) / 100.0);
    }

    public String getExcitementLabel(double score) {
        if (score >= 75) return "HIGH";
        if (score >= 50) return "MEDIUM";
        return "LOW";
    }

    // ── Generate proposals (greedy-randomized from §5.7) ─────────────────────

    public List<MatchProposal> generateProposalsLocal(int count) {
        List<Fighter> fighters = fighterDao.findAll();
        List<ScoredPair> candidates = new ArrayList<>();

        for (int i = 0; i < fighters.size(); i++) {
            for (int j = i + 1; j < fighters.size(); j++) {
                Fighter a = fighters.get(i);
                Fighter b = fighters.get(j);
                // Only pair fighters in same division if both have one
                Integer da = a.getWeightDivisionId();
                Integer db = b.getWeightDivisionId();
                if (da != null && db != null && !da.equals(db)) continue;
                double score = computeScoreIA(a, b, Set.of());
                candidates.add(new ScoredPair(a, b, score));
            }
        }

        candidates.sort((x, y) -> Double.compare(y.score, x.score));
        List<ScoredPair> top50 = candidates.stream().limit(50).collect(Collectors.toList());

        Set<Integer> usedFighters = new HashSet<>();
        Set<Integer> usedDivisions = new HashSet<>();
        List<MatchProposal> proposals = new ArrayList<>();

        for (int i = 0; i < count && !top50.isEmpty(); ) {
            List<ScoredPair> eligible = top50.stream()
                    .filter(p -> !usedFighters.contains(p.f1.getFighterId())
                            && !usedFighters.contains(p.f2.getFighterId()))
                    .limit(5)
                    .collect(Collectors.toList());
            if (eligible.isEmpty()) break;

            ScoredPair chosen = eligible.get(ThreadLocalRandom.current().nextInt(eligible.size()));
            top50.remove(chosen);

            double reScore = computeScoreIA(chosen.f1, chosen.f2, usedDivisions);
            MatchProposal mp = buildProposal(chosen.f1, chosen.f2, reScore);
            int newId = proposalDao.create(mp);
            if (newId > 0) {
                mp.setId(newId);
                proposals.add(mp);
                usedFighters.add(chosen.f1.getFighterId());
                usedFighters.add(chosen.f2.getFighterId());
                if (chosen.f1.getWeightDivisionId() != null) usedDivisions.add(chosen.f1.getWeightDivisionId());
                i++;
            }
        }
        return proposals;
    }

    private MatchProposal buildProposal(Fighter f1, Fighter f2, double score) {
        MatchProposal mp = new MatchProposal();
        mp.setFighter1Id(f1.getFighterId());
        mp.setFighter2Id(f2.getFighterId());
        mp.setCompatibility(BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP));
        mp.setStatus("PENDING");
        if (f1.getWeightDivisionId() != null && f1.getWeightDivisionId().equals(f2.getWeightDivisionId())) {
            mp.setWeightDivisionId(f1.getWeightDivisionId());
        }
        String n1 = f1.getFirstName() + " " + f1.getLastName();
        String n2 = f2.getFirstName() + " " + f2.getLastName();
        int tf1 = Math.max(f1.getWins() + f1.getLosses(), 1);
        int tf2 = Math.max(f2.getWins() + f2.getLosses(), 1);
        double wr1 = (double) f1.getWins() / tf1 * 100;
        double wr2 = (double) f2.getWins() / tf2 * 100;
        double acc1 = f1.getStrikesThrown() > 0 ? (double) f1.getStrikesLanded() / f1.getStrikesThrown() * 100 : 0;
        double acc2 = f2.getStrikesThrown() > 0 ? (double) f2.getStrikesLanded() / f2.getStrikesThrown() * 100 : 0;
        mp.setNotes(String.format(
                "%s (%d-%d, ELO %.0f) vs %s (%d-%d, ELO %.0f) — near-identical win rates (%.0f%% vs %.0f%%), " +
                "aligned strike accuracy (%.0f%% vs %.0f%%). Score IA: %.2f/100.",
                n1, f1.getWins(), f1.getLosses(), f1.getEloRating(),
                n2, f2.getWins(), f2.getLosses(), f2.getEloRating(),
                wr1, wr2, acc1, acc2, score));
        mp.setFighter1Name(n1);
        mp.setFighter2Name(n2);
        return mp;
    }

    private record ScoredPair(Fighter f1, Fighter f2, double score) {}
}
