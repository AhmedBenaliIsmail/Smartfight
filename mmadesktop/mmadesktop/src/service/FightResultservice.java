package service;

import dao.FightResultDao;
import dao.FighterDao;
import model.FightResult;
import model.Fighter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class FightResultservice {
    private FightResultDao fightResultDao;
    private FighterDao fighterDao;

    public FightResultservice() {
        this.fightResultDao = new FightResultDao();
        this.fighterDao = new FighterDao();
    }

    // Add a completed fight (with result) – original method
    public boolean addFightResult(FightResult result) {
        // Basic validation
        if (result.getFighter1Id() == result.getFighter2Id()) {
            return false;
        }
        // Ensure fight number is within 1-3
        if (result.getFightNumber() < 1 || result.getFightNumber() > 3) {
            throw new IllegalArgumentException("Fight number must be between 1 and 3.");
        }
        // Check max fights per event
        List<FightResult> eventFights = fightResultDao.getFightResultsByEvent(result.getEventId());
        if (eventFights.size() >= 3) {
            throw new IllegalStateException("Event already has 3 fights. Cannot add more.");
        }
        // Check if fight number already used
        boolean numberTaken = eventFights.stream().anyMatch(f -> f.getFightNumber() == result.getFightNumber());
        if (numberTaken) {
            throw new IllegalArgumentException("Fight number " + result.getFightNumber() + " already exists for this event.");
        }
        // Check if fighters are already used in this event
        if (isFighterUsedInEvent(result.getEventId(), result.getFighter1Id()) ||
            isFighterUsedInEvent(result.getEventId(), result.getFighter2Id())) {
            throw new IllegalArgumentException("One of the fighters is already scheduled in another fight of this event.");
        }

        if (!fightResultDao.addFightResult(result)) {
            return false;
        }
        return updateFighterStats(result);
    }

    // Add a scheduled fight (without result)
    public boolean addScheduledFight(int eventId, int fightNumber, int fighter1Id, int fighter2Id) {
        // Validate fighters are different
        if (fighter1Id == fighter2Id) {
            throw new IllegalArgumentException("Fighter 1 and Fighter 2 must be different.");
        }
        // Validate fight number
        if (fightNumber < 1 || fightNumber > 3) {
            throw new IllegalArgumentException("Fight number must be between 1 and 3.");
        }
        // Check max fights per event
        List<FightResult> eventFights = fightResultDao.getFightResultsByEvent(eventId);
        if (eventFights.size() >= 3) {
            throw new IllegalStateException("Event already has 3 fights. Cannot add more.");
        }
        // Check if fight number already taken
        boolean numberTaken = eventFights.stream().anyMatch(f -> f.getFightNumber() == fightNumber);
        if (numberTaken) {
            throw new IllegalArgumentException("Fight number " + fightNumber + " already exists for this event.");
        }
        // Check if fighters already used in this event
        if (isFighterUsedInEvent(eventId, fighter1Id) || isFighterUsedInEvent(eventId, fighter2Id)) {
            throw new IllegalArgumentException("One of the fighters is already scheduled in another fight of this event.");
        }

        FightResult fr = new FightResult();
        fr.setEventId(eventId);
        fr.setFightNumber(fightNumber);
        fr.setFighter1Id(fighter1Id);
        fr.setFighter2Id(fighter2Id);
        fr.setStatus("SCHEDULED");
        // Result fields remain null/default (winnerId null, method null, roundNumber 0, fightDate null)
        return fightResultDao.addFightResult(fr);
    }

    // Enter result for a scheduled fight
    public boolean enterResult(int resultId, Integer winnerId, String method, int round, LocalDateTime fightDate) {
        FightResult fr = fightResultDao.getFightResultById(resultId);
        if (fr == null) {
            throw new IllegalArgumentException("Fight result not found.");
        }
        if (!"SCHEDULED".equals(fr.getStatus())) {
            throw new IllegalStateException("Fight is not in SCHEDULED status.");
        }
        fr.setWinnerId(winnerId);
        fr.setMethodOfVictory(method);
        fr.setRoundNumber(round);
        fr.setFightDate(fightDate);
        fr.setStatus("COMPLETED");

        boolean updated = fightResultDao.updateFightResult(fr);
        if (updated) {
            // Update fighter stats using the completed result
            return updateFighterStats(fr);
        }
        return false;
    }

    // NEW METHOD: Cancel a scheduled fight
    public boolean cancelFight(int resultId) {
        FightResult fr = fightResultDao.getFightResultById(resultId);
        if (fr == null) {
            return false;
        }
        // Only allow cancellation if the fight is still scheduled
        if (!"SCHEDULED".equals(fr.getStatus())) {
            return false;
        }
        fr.setStatus("CANCELLED");
        return fightResultDao.updateFightResult(fr);
    }

    // Check if a fighter is already used in any fight of a given event
    public boolean isFighterUsedInEvent(int eventId, int fighterId) {
        List<FightResult> eventFights = fightResultDao.getFightResultsByEvent(eventId);
        return eventFights.stream()
                .anyMatch(f -> f.getFighter1Id() == fighterId || f.getFighter2Id() == fighterId);
    }

    // Get available fight numbers for an event (1-3 not taken)
    public List<Integer> getAvailableFightNumbers(int eventId) {
        List<Integer> taken = fightResultDao.getFightResultsByEvent(eventId).stream()
                .map(FightResult::getFightNumber)
                .collect(Collectors.toList());
        List<Integer> available = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            if (!taken.contains(i)) available.add(i);
        }
        return available;
    }

    // Original methods (keep as is)
    public FightResult getFightResultById(int resultId) {
        return fightResultDao.getFightResultById(resultId);
    }

    public List<FightResult> getAllFightResults() {
        return fightResultDao.getAllFightResults();
    }

    public List<FightResult> getFightResultsByEvent(int eventId) {
        return fightResultDao.getFightResultsByEvent(eventId);
    }

    public boolean updateFightResult(FightResult result) {
        if (result.getResultId() <= 0) {
            return false;
        }
        // If updating a result, we should not automatically update stats again?
        // For simplicity, we assume this is only for minor edits; stats update should be handled separately.
        return fightResultDao.updateFightResult(result);
    }

    public boolean deleteFightResult(int resultId) {
        if (resultId <= 0) {
            return false;
        }
        return fightResultDao.deleteFightResult(resultId);
    }

    // Internal method to update fighter stats based on a completed fight
    private boolean updateFighterStats(FightResult result) {
        Fighter fighter1 = fighterDao.getFighterById(result.getFighter1Id());
        Fighter fighter2 = fighterDao.getFighterById(result.getFighter2Id());
        if (fighter1 == null || fighter2 == null) return false;

        String method = result.getMethodOfVictory().toLowerCase();

        // Draw
        if (method.equals("draw")) {
            fighter1.setDraws(fighter1.getDraws() + 1);
            fighter2.setDraws(fighter2.getDraws() + 1);
            fighterDao.updateFighter(fighter1);
            fighterDao.updateFighter(fighter2);
            return true;
        }

        Integer winnerId = result.getWinnerId();
        if (winnerId == null) return false;
        if (!winnerId.equals(fighter1.getFighterId()) && !winnerId.equals(fighter2.getFighterId())) {
            return false;
        }

        Fighter winner = winnerId.equals(fighter1.getFighterId()) ? fighter1 : fighter2;
        Fighter loser = winner == fighter1 ? fighter2 : fighter1;

        winner.setWins(winner.getWins() + 1);
        loser.setLosses(loser.getLosses() + 1);

        switch (method) {
            case "ko":
            case "tko":
                winner.setKoWins(winner.getKoWins() + 1);
                break;
            case "submission":
                winner.setSubmissionWins(winner.getSubmissionWins() + 1);
                break;
            case "decision":
                winner.setDecisionWins(winner.getDecisionWins() + 1);
                break;
            // For draw, we already handled above; no method stats.
        }

        fighterDao.updateFighter(winner);
        fighterDao.updateFighter(loser);
        return true;
    }
}