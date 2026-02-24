package services;

import entities.FightStatistic;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceFightStatistic implements IService<FightStatistic> {

    private Connection connection;

    public ServiceFightStatistic() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(FightStatistic fs) throws SQLException {
        String req = "INSERT INTO fight_statistic (fight_result_id, fighter_id, strikes_landed, strikes_thrown, takedowns, submissions, knockdowns) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, fs.getFightResultId());
        ps.setInt(2, fs.getFighterId());
        ps.setInt(3, fs.getStrikesLanded());
        ps.setInt(4, fs.getStrikesThrown());
        ps.setInt(5, fs.getTakedowns());
        ps.setInt(6, fs.getSubmissions());
        ps.setInt(7, fs.getKnockdowns());
        ps.executeUpdate();
        System.out.println("FightStatistic added for fightResult=" + fs.getFightResultId());
    }

    @Override
    public void modifier(FightStatistic fs) throws SQLException {
        String req = "UPDATE fight_statistic SET fight_result_id=?, fighter_id=?, strikes_landed=?, strikes_thrown=?, takedowns=?, submissions=?, knockdowns=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, fs.getFightResultId());
        ps.setInt(2, fs.getFighterId());
        ps.setInt(3, fs.getStrikesLanded());
        ps.setInt(4, fs.getStrikesThrown());
        ps.setInt(5, fs.getTakedowns());
        ps.setInt(6, fs.getSubmissions());
        ps.setInt(7, fs.getKnockdowns());
        ps.setInt(8, fs.getId());
        ps.executeUpdate();
        System.out.println("FightStatistic updated: id=" + fs.getId());
    }

    @Override
    public void supprimer(FightStatistic fs) throws SQLException {
        String req = "DELETE FROM fight_statistic WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, fs.getId());
        ps.executeUpdate();
        System.out.println("FightStatistic deleted: id=" + fs.getId());
    }

    @Override
    public List<FightStatistic> recuperer() throws SQLException {
        List<FightStatistic> list = new ArrayList<>();
        String req = "SELECT * FROM fight_statistic";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            list.add(new FightStatistic(
                    rs.getInt("id"),
                    rs.getInt("fight_result_id"),
                    rs.getInt("fighter_id"),
                    rs.getInt("strikes_landed"),
                    rs.getInt("strikes_thrown"),
                    rs.getInt("takedowns"),
                    rs.getInt("submissions"),
                    rs.getInt("knockdowns")));
        }
        return list;
    }

    public List<FightStatistic> recupererByFightResult(int fightResultId) throws SQLException {
        List<FightStatistic> list = new ArrayList<>();
        String req = "SELECT * FROM fight_statistic WHERE fight_result_id=" + fightResultId;
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            list.add(new FightStatistic(
                    rs.getInt("id"),
                    rs.getInt("fight_result_id"),
                    rs.getInt("fighter_id"),
                    rs.getInt("strikes_landed"),
                    rs.getInt("strikes_thrown"),
                    rs.getInt("takedowns"),
                    rs.getInt("submissions"),
                    rs.getInt("knockdowns")));
        }
        return list;
    }
}
