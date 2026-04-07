package service;

import dao.FighterDao;
import model.Fighter;
import java.util.List;

public class Fighterservice {
    private FighterDao fighterDao;

    public Fighterservice() {
        this.fighterDao = new FighterDao();
    }

    public boolean addFighter(Fighter fighter) {
        if (fighter.getFirstName() == null || fighter.getFirstName().isEmpty()) {
            return false;
        }
        return fighterDao.addFighter(fighter);
    }

    public Fighter getFighterById(int fighterId) {
        return fighterDao.getFighterById(fighterId);
    }

    public List<Fighter> getAllFighters() {
        return fighterDao.getAllFighters();
    }

    public boolean updateFighter(Fighter fighter) {
        if (fighter.getFighterId() <= 0) {
            return false;
        }
        return fighterDao.updateFighter(fighter);
    }

    public boolean deleteFighter(int fighterId) {
        if (fighterId <= 0) {
            return false;
        }
        return fighterDao.deleteFighter(fighterId);
    }

    public double getWinPercentage(Fighter fighter) {
        int totalFights = fighter.getTotalFights();
        if (totalFights == 0) return 0;
        return (double) fighter.getWins() / totalFights * 100;
    }
}