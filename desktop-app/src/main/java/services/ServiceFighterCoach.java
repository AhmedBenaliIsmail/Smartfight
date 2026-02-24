package services;

import entities.FighterCoach;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceFighterCoach implements IService<FighterCoach> {

    private Connection connection;

    public ServiceFighterCoach() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(FighterCoach fc) throws SQLException {
        String req = "INSERT INTO fighter_coach (fighter_id, coach_id, start_date, end_date, is_active) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, fc.getFighterId());
        ps.setInt(2, fc.getCoachId());
        ps.setDate(3, Date.valueOf(fc.getStartDate()));
        if (fc.getEndDate() != null)
            ps.setDate(4, Date.valueOf(fc.getEndDate()));
        else
            ps.setNull(4, Types.DATE);
        ps.setBoolean(5, fc.isActive());
        ps.executeUpdate();
        System.out.println("FighterCoach relation added: fighter=" + fc.getFighterId() + " coach=" + fc.getCoachId());
    }

    @Override
    public void modifier(FighterCoach fc) throws SQLException {
        String req = "UPDATE fighter_coach SET fighter_id=?, coach_id=?, start_date=?, end_date=?, is_active=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, fc.getFighterId());
        ps.setInt(2, fc.getCoachId());
        ps.setDate(3, Date.valueOf(fc.getStartDate()));
        if (fc.getEndDate() != null)
            ps.setDate(4, Date.valueOf(fc.getEndDate()));
        else
            ps.setNull(4, Types.DATE);
        ps.setBoolean(5, fc.isActive());
        ps.setInt(6, fc.getId());
        ps.executeUpdate();
        System.out.println("FighterCoach updated: id=" + fc.getId());
    }

    @Override
    public void supprimer(FighterCoach fc) throws SQLException {
        String req = "DELETE FROM fighter_coach WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, fc.getId());
        ps.executeUpdate();
        System.out.println("FighterCoach deleted: id=" + fc.getId());
    }

    @Override
    public List<FighterCoach> recuperer() throws SQLException {
        List<FighterCoach> list = new ArrayList<>();
        String req = "SELECT * FROM fighter_coach";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            list.add(new FighterCoach(
                    rs.getInt("id"),
                    rs.getInt("fighter_id"),
                    rs.getInt("coach_id"),
                    rs.getDate("start_date").toLocalDate(),
                    rs.getDate("end_date") != null ? rs.getDate("end_date").toLocalDate() : null,
                    rs.getBoolean("is_active")));
        }
        return list;
    }
}
