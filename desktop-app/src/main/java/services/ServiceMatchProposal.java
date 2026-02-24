package services;

import entities.MatchProposal;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceMatchProposal implements IService<MatchProposal> {

    private Connection connection;

    public ServiceMatchProposal() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(MatchProposal mp) throws SQLException {
        String req = "INSERT INTO match_proposal (event_id, fighter1_id, fighter2_id, compatibility, status, notes) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, mp.getEventId());
        ps.setInt(2, mp.getFighter1Id());
        ps.setInt(3, mp.getFighter2Id());
        ps.setDouble(4, mp.getCompatibility());
        ps.setString(5, mp.getStatus());
        ps.setString(6, mp.getNotes());
        ps.executeUpdate();
        System.out.println("MatchProposal added for event=" + mp.getEventId());
    }

    @Override
    public void modifier(MatchProposal mp) throws SQLException {
        String req = "UPDATE match_proposal SET event_id=?, fighter1_id=?, fighter2_id=?, compatibility=?, status=?, notes=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, mp.getEventId());
        ps.setInt(2, mp.getFighter1Id());
        ps.setInt(3, mp.getFighter2Id());
        ps.setDouble(4, mp.getCompatibility());
        ps.setString(5, mp.getStatus());
        ps.setString(6, mp.getNotes());
        ps.setInt(7, mp.getId());
        ps.executeUpdate();
        System.out.println("MatchProposal updated: id=" + mp.getId());
    }

    @Override
    public void supprimer(MatchProposal mp) throws SQLException {
        String req = "DELETE FROM match_proposal WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, mp.getId());
        ps.executeUpdate();
        System.out.println("MatchProposal deleted: id=" + mp.getId());
    }

    @Override
    public List<MatchProposal> recuperer() throws SQLException {
        List<MatchProposal> list = new ArrayList<>();
        String req = "SELECT * FROM match_proposal";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            list.add(new MatchProposal(
                    rs.getInt("id"),
                    rs.getInt("event_id"),
                    rs.getInt("fighter1_id"),
                    rs.getInt("fighter2_id"),
                    rs.getDouble("compatibility"),
                    rs.getString("status"),
                    rs.getString("notes")));
        }
        return list;
    }

    public List<MatchProposal> recupererByEvent(int eventId) throws SQLException {
        List<MatchProposal> list = new ArrayList<>();
        String req = "SELECT * FROM match_proposal WHERE event_id=" + eventId;
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            list.add(new MatchProposal(
                    rs.getInt("id"),
                    rs.getInt("event_id"),
                    rs.getInt("fighter1_id"),
                    rs.getInt("fighter2_id"),
                    rs.getDouble("compatibility"),
                    rs.getString("status"),
                    rs.getString("notes")));
        }
        return list;
    }
}
