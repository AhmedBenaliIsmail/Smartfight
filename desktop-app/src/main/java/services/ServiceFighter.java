package services;

import entities.Fighter;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceFighter implements IService<Fighter> {

    private Connection connection;

    public ServiceFighter() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Fighter fighter) throws SQLException {
        String req = "INSERT INTO fighter (user_id, nickname, date_of_birth, nationality, weight_class_id, wins, losses, draws, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, fighter.getUserId());
        ps.setString(2, fighter.getNickname());
        if (fighter.getDateOfBirth() != null)
            ps.setDate(3, Date.valueOf(fighter.getDateOfBirth()));
        else
            ps.setNull(3, Types.DATE);
        ps.setString(4, fighter.getNationality());
        if (fighter.getWeightClassId() > 0)
            ps.setInt(5, fighter.getWeightClassId());
        else
            ps.setNull(5, Types.INTEGER);
        ps.setInt(6, fighter.getWins());
        ps.setInt(7, fighter.getLosses());
        ps.setInt(8, fighter.getDraws());
        ps.setString(9, fighter.getStatus());
        ps.executeUpdate();
        System.out.println("Fighter added: " + fighter.getNickname());
    }

    @Override
    public void modifier(Fighter fighter) throws SQLException {
        String req = "UPDATE fighter SET user_id=?, nickname=?, date_of_birth=?, nationality=?, weight_class_id=?, wins=?, losses=?, draws=?, status=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, fighter.getUserId());
        ps.setString(2, fighter.getNickname());
        if (fighter.getDateOfBirth() != null)
            ps.setDate(3, Date.valueOf(fighter.getDateOfBirth()));
        else
            ps.setNull(3, Types.DATE);
        ps.setString(4, fighter.getNationality());
        if (fighter.getWeightClassId() > 0)
            ps.setInt(5, fighter.getWeightClassId());
        else
            ps.setNull(5, Types.INTEGER);
        ps.setInt(6, fighter.getWins());
        ps.setInt(7, fighter.getLosses());
        ps.setInt(8, fighter.getDraws());
        ps.setString(9, fighter.getStatus());
        ps.setInt(10, fighter.getId());
        ps.executeUpdate();
        System.out.println("Fighter updated: id=" + fighter.getId());
    }

    @Override
    public void supprimer(Fighter fighter) throws SQLException {
        String req = "DELETE FROM fighter WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, fighter.getId());
        ps.executeUpdate();
        System.out.println("Fighter deleted: id=" + fighter.getId());
    }

    @Override
    public List<Fighter> recuperer() throws SQLException {
        List<Fighter> list = new ArrayList<>();
        String req = "SELECT * FROM fighter";
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
            list.add(f);
        }
        return list;
    }
}
