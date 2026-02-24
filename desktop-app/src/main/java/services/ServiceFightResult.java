package services;

import entities.FightResult;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceFightResult implements IService<FightResult> {

    private Connection connection;

    public ServiceFightResult() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(FightResult fr) throws SQLException {
        String req = "INSERT INTO fight_result (event_id, match_id, fighter_red_id, fighter_blue_id, winner_id, method, round_ended, fight_date, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(req);
        if (fr.getEventId() > 0)
            ps.setInt(1, fr.getEventId());
        else
            ps.setNull(1, Types.INTEGER);
        if (fr.getMatchId() > 0)
            ps.setInt(2, fr.getMatchId());
        else
            ps.setNull(2, Types.INTEGER);
        ps.setInt(3, fr.getFighterRedId());
        ps.setInt(4, fr.getFighterBlueId());
        if (fr.getWinnerId() > 0)
            ps.setInt(5, fr.getWinnerId());
        else
            ps.setNull(5, Types.INTEGER);
        ps.setString(6, fr.getMethod());
        ps.setInt(7, fr.getRoundEnded());
        if (fr.getFightDate() != null)
            ps.setDate(8, Date.valueOf(fr.getFightDate()));
        else
            ps.setNull(8, Types.DATE);
        ps.setString(9, fr.getNotes());
        ps.executeUpdate();
        System.out.println("FightResult added for event=" + fr.getEventId());
    }

    @Override
    public void modifier(FightResult fr) throws SQLException {
        String req = "UPDATE fight_result SET event_id=?, match_id=?, fighter_red_id=?, fighter_blue_id=?, winner_id=?, method=?, round_ended=?, fight_date=?, notes=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        if (fr.getEventId() > 0)
            ps.setInt(1, fr.getEventId());
        else
            ps.setNull(1, Types.INTEGER);
        if (fr.getMatchId() > 0)
            ps.setInt(2, fr.getMatchId());
        else
            ps.setNull(2, Types.INTEGER);
        ps.setInt(3, fr.getFighterRedId());
        ps.setInt(4, fr.getFighterBlueId());
        if (fr.getWinnerId() > 0)
            ps.setInt(5, fr.getWinnerId());
        else
            ps.setNull(5, Types.INTEGER);
        ps.setString(6, fr.getMethod());
        ps.setInt(7, fr.getRoundEnded());
        if (fr.getFightDate() != null)
            ps.setDate(8, Date.valueOf(fr.getFightDate()));
        else
            ps.setNull(8, Types.DATE);
        ps.setString(9, fr.getNotes());
        ps.setInt(10, fr.getId());
        ps.executeUpdate();
        System.out.println("FightResult updated: id=" + fr.getId());
    }

    @Override
    public void supprimer(FightResult fr) throws SQLException {
        String req = "DELETE FROM fight_result WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, fr.getId());
        ps.executeUpdate();
        System.out.println("FightResult deleted: id=" + fr.getId());
    }

    @Override
    public List<FightResult> recuperer() throws SQLException {
        List<FightResult> list = new ArrayList<>();
        String req = "SELECT * FROM fight_result";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            list.add(new FightResult(
                    rs.getInt("id"),
                    rs.getInt("event_id"),
                    rs.getInt("match_id"),
                    rs.getInt("fighter_red_id"),
                    rs.getInt("fighter_blue_id"),
                    rs.getInt("winner_id"),
                    rs.getString("method"),
                    rs.getInt("round_ended"),
                    rs.getDate("fight_date") != null ? rs.getDate("fight_date").toLocalDate() : null,
                    rs.getString("notes")));
        }
        return list;
    }

    public List<FightResult> recupererByEvent(int eventId) throws SQLException {
        List<FightResult> list = new ArrayList<>();
        String req = "SELECT * FROM fight_result WHERE event_id=" + eventId;
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            list.add(new FightResult(
                    rs.getInt("id"),
                    rs.getInt("event_id"),
                    rs.getInt("match_id"),
                    rs.getInt("fighter_red_id"),
                    rs.getInt("fighter_blue_id"),
                    rs.getInt("winner_id"),
                    rs.getString("method"),
                    rs.getInt("round_ended"),
                    rs.getDate("fight_date") != null ? rs.getDate("fight_date").toLocalDate() : null,
                    rs.getString("notes")));
        }
        return list;
    }
}
