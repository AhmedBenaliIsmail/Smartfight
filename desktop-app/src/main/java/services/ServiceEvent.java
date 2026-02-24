package services;

import entities.Event;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceEvent implements IService<Event> {

    private Connection connection;

    public ServiceEvent() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Event event) throws SQLException {
        String req = "INSERT INTO event (name, description, start_date, end_date, status, visibility, capacity, venue_id, discipline_id, organizer_id) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, event.getName());
        ps.setString(2, event.getDescription());
        ps.setDate(3, Date.valueOf(event.getStartDate()));
        ps.setDate(4, Date.valueOf(event.getEndDate()));
        ps.setString(5, event.getStatus());
        ps.setString(6, event.getVisibility());
        ps.setInt(7, event.getCapacity());
        if (event.getVenueId() > 0)
            ps.setInt(8, event.getVenueId());
        else
            ps.setNull(8, Types.INTEGER);
        if (event.getDisciplineId() > 0)
            ps.setInt(9, event.getDisciplineId());
        else
            ps.setNull(9, Types.INTEGER);
        if (event.getOrganizerId() > 0)
            ps.setInt(10, event.getOrganizerId());
        else
            ps.setNull(10, Types.INTEGER);
        ps.executeUpdate();
        System.out.println("Event added: " + event.getName());
    }

    @Override
    public void modifier(Event event) throws SQLException {
        String req = "UPDATE event SET name=?, description=?, start_date=?, end_date=?, status=?, visibility=?, capacity=?, venue_id=?, discipline_id=?, organizer_id=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, event.getName());
        ps.setString(2, event.getDescription());
        ps.setDate(3, Date.valueOf(event.getStartDate()));
        ps.setDate(4, Date.valueOf(event.getEndDate()));
        ps.setString(5, event.getStatus());
        ps.setString(6, event.getVisibility());
        ps.setInt(7, event.getCapacity());
        if (event.getVenueId() > 0)
            ps.setInt(8, event.getVenueId());
        else
            ps.setNull(8, Types.INTEGER);
        if (event.getDisciplineId() > 0)
            ps.setInt(9, event.getDisciplineId());
        else
            ps.setNull(9, Types.INTEGER);
        if (event.getOrganizerId() > 0)
            ps.setInt(10, event.getOrganizerId());
        else
            ps.setNull(10, Types.INTEGER);
        ps.setInt(11, event.getId());
        ps.executeUpdate();
        System.out.println("Event updated: id=" + event.getId());
    }

    @Override
    public void supprimer(Event event) throws SQLException {
        String req = "DELETE FROM event WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, event.getId());
        ps.executeUpdate();
        System.out.println("Event deleted: id=" + event.getId());
    }

    @Override
    public List<Event> recuperer() throws SQLException {
        List<Event> events = new ArrayList<>();
        String req = "SELECT * FROM event";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            Event e = new Event(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDate("start_date") != null ? rs.getDate("start_date").toLocalDate() : null,
                    rs.getDate("end_date") != null ? rs.getDate("end_date").toLocalDate() : null,
                    rs.getString("status"),
                    rs.getString("visibility"),
                    rs.getInt("capacity"),
                    rs.getInt("venue_id"),
                    rs.getInt("discipline_id"),
                    rs.getInt("organizer_id"));
            events.add(e);
        }
        return events;
    }
}
