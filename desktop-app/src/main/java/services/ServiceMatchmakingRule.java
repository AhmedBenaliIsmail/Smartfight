package services;

import entities.MatchmakingRule;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceMatchmakingRule implements IService<MatchmakingRule> {

    private Connection connection;

    public ServiceMatchmakingRule() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(MatchmakingRule rule) throws SQLException {
        String req = "INSERT INTO matchmaking_rule (name, description, weight, is_active) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, rule.getName());
        ps.setString(2, rule.getDescription());
        ps.setDouble(3, rule.getWeight());
        ps.setBoolean(4, rule.isActive());
        ps.executeUpdate();
        System.out.println("MatchmakingRule added: " + rule.getName());
    }

    @Override
    public void modifier(MatchmakingRule rule) throws SQLException {
        String req = "UPDATE matchmaking_rule SET name=?, description=?, weight=?, is_active=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, rule.getName());
        ps.setString(2, rule.getDescription());
        ps.setDouble(3, rule.getWeight());
        ps.setBoolean(4, rule.isActive());
        ps.setInt(5, rule.getId());
        ps.executeUpdate();
        System.out.println("MatchmakingRule updated: id=" + rule.getId());
    }

    @Override
    public void supprimer(MatchmakingRule rule) throws SQLException {
        String req = "DELETE FROM matchmaking_rule WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, rule.getId());
        ps.executeUpdate();
        System.out.println("MatchmakingRule deleted: id=" + rule.getId());
    }

    @Override
    public List<MatchmakingRule> recuperer() throws SQLException {
        List<MatchmakingRule> list = new ArrayList<>();
        String req = "SELECT * FROM matchmaking_rule";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            list.add(new MatchmakingRule(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDouble("weight"),
                    rs.getBoolean("is_active")));
        }
        return list;
    }
}
