package services;

import entities.PerformanceScore;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServicePerformanceScore implements IService<PerformanceScore> {

    private Connection connection;

    public ServicePerformanceScore() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(PerformanceScore ps_entity) throws SQLException {
        String req = "INSERT INTO performance_score (fighter_id, score, aggression, defense, technique, experience) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, ps_entity.getFighterId());
        ps.setDouble(2, ps_entity.getScore());
        ps.setDouble(3, ps_entity.getAggression());
        ps.setDouble(4, ps_entity.getDefense());
        ps.setDouble(5, ps_entity.getTechnique());
        ps.setDouble(6, ps_entity.getExperience());
        ps.executeUpdate();
        System.out.println("PerformanceScore added for fighter=" + ps_entity.getFighterId());
    }

    @Override
    public void modifier(PerformanceScore ps_entity) throws SQLException {
        String req = "UPDATE performance_score SET fighter_id=?, score=?, aggression=?, defense=?, technique=?, experience=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, ps_entity.getFighterId());
        ps.setDouble(2, ps_entity.getScore());
        ps.setDouble(3, ps_entity.getAggression());
        ps.setDouble(4, ps_entity.getDefense());
        ps.setDouble(5, ps_entity.getTechnique());
        ps.setDouble(6, ps_entity.getExperience());
        ps.setInt(7, ps_entity.getId());
        ps.executeUpdate();
        System.out.println("PerformanceScore updated: id=" + ps_entity.getId());
    }

    @Override
    public void supprimer(PerformanceScore ps_entity) throws SQLException {
        String req = "DELETE FROM performance_score WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, ps_entity.getId());
        ps.executeUpdate();
        System.out.println("PerformanceScore deleted: id=" + ps_entity.getId());
    }

    @Override
    public List<PerformanceScore> recuperer() throws SQLException {
        List<PerformanceScore> list = new ArrayList<>();
        String req = "SELECT * FROM performance_score";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            list.add(new PerformanceScore(
                    rs.getInt("id"),
                    rs.getInt("fighter_id"),
                    rs.getDouble("score"),
                    rs.getDouble("aggression"),
                    rs.getDouble("defense"),
                    rs.getDouble("technique"),
                    rs.getDouble("experience")));
        }
        return list;
    }

    public PerformanceScore recupererByFighter(int fighterId) throws SQLException {
        String req = "SELECT * FROM performance_score WHERE fighter_id=" + fighterId + " LIMIT 1";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);
        if (rs.next()) {
            return new PerformanceScore(
                    rs.getInt("id"),
                    rs.getInt("fighter_id"),
                    rs.getDouble("score"),
                    rs.getDouble("aggression"),
                    rs.getDouble("defense"),
                    rs.getDouble("technique"),
                    rs.getDouble("experience"));
        }
        return null;
    }
}
