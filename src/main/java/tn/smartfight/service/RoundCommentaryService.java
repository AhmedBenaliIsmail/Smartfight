package tn.smartfight.service;

import tn.smartfight.model.FightStatistic;

import java.util.List;
import java.util.Random;

/**
 * Java port of PHP RoundCommentaryService.
 * Generates broadcast-style CompuBox commentary for a single round.
 */
public class RoundCommentaryService {

    private final Random rng = new Random();

    public String generateForRound(
            String f1Name, FightStatistic f1,
            String f2Name, FightStatistic f2,
            int round
    ) {
        int f1l = f1 != null ? f1.getPunchesLanded() : 0;
        int f2l = f2 != null ? f2.getPunchesLanded() : 0;
        int f1t = f1 != null ? f1.getPunchesThrown() : 0;
        int f2t = f2 != null ? f2.getPunchesThrown() : 0;
        int f1kd = f1 != null ? f1.getKnockdowns() : 0;
        int f2kd = f2 != null ? f2.getKnockdowns() : 0;
        int f1pl = f1 != null ? f1.getPowerPunchesLanded() : 0;
        int f1pt = f1 != null ? f1.getPowerPunchesThrown() : 0;
        int f2pl = f2 != null ? f2.getPowerPunchesLanded() : 0;
        int f2pt = f2 != null ? f2.getPowerPunchesThrown() : 0;
        int f1body = f1 != null ? f1.getBodyShotsLanded() : 0;
        int f2body = f2 != null ? f2.getBodyShotsLanded() : 0;
        int f1jabs = f1 != null ? f1.getJabsLanded() : 0;
        int f2jabs = f2 != null ? f2.getJabsLanded() : 0;

        String winner = f1l >= f2l ? f1Name : f2Name;
        String loser  = f1l >= f2l ? f2Name : f1Name;

        StringBuilder sb = new StringBuilder();

        // Sentence 1 — who won the round
        if (f1l == f2l) {
            String[] v = {
                "Round " + round + " was dead even — both fighters landed " + f1l + " punches apiece.",
                "CompuBox shows an even split in Round " + round + ": " + f1l + " connects for " + f1Name + ", " + f2l + " for " + f2Name + ".",
                "A perfectly balanced round on the CompuBox numbers — " + f1l + " landed for each man.",
                "Too close to call on the CompuBox numbers. Both men connected on " + f1l + " — this one goes to the judges.",
            };
            sb.append(pick(v));
        } else if (f1l > f2l) {
            String[] openers = {"edged", "controlled", "outworked", "dominated", "swept", "outlanded"};
            String op = pick(openers);
            String[] v = {
                f1Name + " " + op + " Round " + round + ", landing " + f1l + " of " + f1t + " total punches compared to " + f2Name + "'s " + f2l + ".",
                "CompuBox gives Round " + round + " to " + f1Name + ", who connected on " + f1l + " to " + f2Name + "'s " + f2l + " out of " + f2t + " thrown.",
                f1Name + " held the connect advantage in Round " + round + ", landing " + f1l + " versus " + f2l + " for " + f2Name + ".",
                "The CompuBox numbers tell the story of Round " + round + ": " + f1Name + " outlanded " + f2Name + " " + f1l + " to " + f2l + ".",
            };
            sb.append(pick(v));
        } else {
            String[] openers = {"edged", "controlled", "outworked", "dominated", "swept", "outlanded"};
            String op = pick(openers);
            String[] v = {
                f2Name + " " + op + " Round " + round + ", connecting on " + f2l + " of " + f2t + " punches while " + f1Name + " landed " + f1l + ".",
                "CompuBox credits Round " + round + " to " + f2Name + ", who outlanded " + f1Name + " " + f2l + " to " + f1l + ".",
                f2Name + " held the volume edge in Round " + round + ", landing " + f2l + " punches compared to " + f1l + " for " + f1Name + ".",
                "The CompuBox numbers tell the story of Round " + round + ": " + f2Name + " outlanded " + f1Name + " " + f2l + " to " + f1l + ".",
            };
            sb.append(pick(v));
        }

        sb.append(" ");

        // Sentence 2 — round-phase context
        if (round <= 3) {
            String[] v = {
                "It's still early in the conversation, but the numbers don't lie.",
                "The feeling-out process is over — the stats show who's dictating terms.",
                "Early exchanges, and already a pattern is beginning to form.",
            };
            sb.append(pick(v));
        } else if (round <= 7) {
            String[] v = {
                "As the fight settles into a rhythm, " + winner + " is beginning to impose their will.",
                "The middle rounds are where fights are won, and " + winner + " is building their case.",
                "At the halfway mark, the CompuBox numbers are telling a story " + loser + " cannot ignore.",
            };
            sb.append(pick(v));
        } else {
            String[] v = {
                "In the championship rounds, every landed punch counts double.",
                "This is championship territory, and " + winner + " is rising to the occasion.",
                "You cannot gift away rounds at this stage. " + winner + " knows it and is cashing in.",
                "The scorecards are being written right now. " + winner + " is writing a strong chapter.",
            };
            sb.append(pick(v));
        }

        // Sentence 3 — knockdowns
        if (f1kd > 0) {
            sb.append(" ");
            String kdLabel = f1kd == 1 ? "a knockdown" : f1kd + " knockdowns";
            String[] v = {
                f1Name + " scored " + kdLabel + ", sending " + f2Name + " to the canvas in a pivotal moment.",
                "The round was defined by " + f1Name + "'s " + kdLabel + " — dropping " + f2Name + " and shifting momentum completely.",
                "A massive moment. " + f1Name + " puts " + f2Name + " down. That's a 10-8 round on every card.",
            };
            sb.append(pick(v));
        } else if (f2kd > 0) {
            sb.append(" ");
            String kdLabel = f2kd == 1 ? "a knockdown" : f2kd + " knockdowns";
            String[] v = {
                f2Name + " scored " + kdLabel + ", putting " + f1Name + " down and threatening a stoppage.",
                "The story of Round " + round + " was " + f2Name + "'s " + kdLabel + ", sending " + f1Name + " to the canvas.",
                "A massive moment. " + f2Name + " puts " + f1Name + " down. That's a 10-8 round on every card.",
            };
            sb.append(pick(v));
        }

        // Sentence 4 — signature stat
        double f1PowerAcc = f1pt > 0 ? (f1pl * 100.0 / f1pt) : 0;
        double f2PowerAcc = f2pt > 0 ? (f2pl * 100.0 / f2pt) : 0;
        int jabDiff = Math.abs(f1jabs - f2jabs);

        String[] connectors = {"Meanwhile, ", "On the other side, ", "That said, ", ""};
        String conn = pick(connectors);

        sb.append(" ");
        if (f1PowerAcc >= 50 || f2PowerAcc >= 50) {
            boolean f1dom = f1PowerAcc >= f2PowerAcc;
            String name  = f1dom ? f1Name : f2Name;
            String opp   = f1dom ? f2Name : f1Name;
            int pl = f1dom ? f1pl : f2pl;
            int pt = f1dom ? f1pt : f2pt;
            int acc = (int) Math.round(f1dom ? f1PowerAcc : f2PowerAcc);
            String[] v = {
                conn + name + " was devastating with the power shots, connecting on " + pl + " of " + pt + " (" + acc + "% accuracy).",
                conn + "In the power game, " + name + " stood out — landing " + pl + " of " + pt + " big shots at a " + acc + "% clip.",
                conn + name + " was surgical with the power punches — " + pl + " of " + pt + " at " + acc + "% accuracy.",
            };
            sb.append(pick(v));
        } else if (f1body >= 4 || f2body >= 4) {
            boolean f1dom = f1body >= f2body && f1body >= 4;
            String name = f1dom ? f1Name : f2Name;
            String opp  = f1dom ? f2Name : f1Name;
            int bs = f1dom ? f1body : f2body;
            String[] v = {
                conn + name + " made the body a priority, racking up " + bs + " body shots to wear down " + opp + ".",
                conn + "Note the body work from " + name + " — " + bs + " shots downstairs that will pay dividends later.",
                conn + bs + " body shots landed for " + name + ". Every one has a cumulative cost on " + opp + "'s legs.",
            };
            sb.append(pick(v));
        } else if (jabDiff >= 5) {
            String jabLeader = f1jabs > f2jabs ? f1Name : f2Name;
            int jabCount = Math.max(f1jabs, f2jabs);
            String[] v = {
                conn + jabLeader + " controlled the range with " + jabCount + " jabs landed, dictating distance throughout the round.",
                conn + "The jab was key — " + jabLeader + " used it to great effect, connecting on " + jabCount + " to control the pocket.",
                conn + jabLeader + "'s jab was the difference maker: " + jabCount + " landed as both rangefinder and scoring weapon.",
            };
            sb.append(pick(v));
        } else {
            int wt = f1l >= f2l ? f1t : f2t;
            int wl = Math.max(f1l, f2l);
            if (wt > 0) {
                String[] v = {
                    conn + winner + " brought the activity this round — " + wt + " punches thrown, keeping pressure on " + loser + " throughout.",
                    conn + "Volume was the story for " + winner + ": " + wt + " thrown, " + wl + " connected. That's relentless output.",
                    conn + winner + " put up " + wt + " punches in Round " + round + ". Championship-level work rate.",
                };
                sb.append(pick(v));
            }
        }

        return sb.toString().trim();
    }

    private String pick(String[] arr) {
        return arr[rng.nextInt(arr.length)];
    }
}
