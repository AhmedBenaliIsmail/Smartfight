package services;

import entities.EventSchedule;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceEventSchedule implements IService<EventSchedule> {

    private Connection connection;

    public ServiceEventSchedule() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(EventSchedule schedule) throws SQLException {
        String req = "INSERT INTO event_schedule (event_id, title, scheduled_time, duration_min, notes) " +
                "VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, schedule.getEventId());
        ps.setString(2, schedule.getTitle());
        ps.setTimestamp(3, Timestamp.valueOf(schedule.getScheduledTime()));
        ps.setInt(4, schedule.getDurationMin());
        ps.setString(5, schedule.getNotes());
        ps.executeUpdate();
        System.out.println("EventSchedule added: " + schedule.getTitle());
    }

    @Override
    public void modifier(EventSchedule schedule) throws SQLException {
        String req = "UPDATE event_schedule SET event_id=?, title=?, scheduled_time=?, duration_min=?, notes=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, schedule.getEventId());
        ps.setString(2, schedule.getTitle());
        ps.setTimestamp(3, Timestamp.valueOf(schedule.getScheduledTime()));
        ps.setInt(4, schedule.getDurationMin());
        ps.setString(5, schedule.getNotes());
        ps.setInt(6, schedule.getId());
        ps.executeUpdate();
        System.out.println("EventSchedule updated: id=" + schedule.getId());
    }

    @Override
    public void supprimer(EventSchedule schedule) throws SQLException {
        String req = "DELETE FROM event_schedule WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, schedule.getId());
        ps.executeUpdate();
        System.out.println("EventSchedule deleted: id=" + schedule.getId());
    }

    @Override
    public List<EventSchedule> recuperer() throws SQLException {
        List<EventSchedule> schedules = new ArrayList<>();
        String req = "SELECT * FROM event_schedule";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            EventSchedule s = new EventSchedule(
                    rs.getInt("id"),
                    rs.getInt("event_id"),
                    rs.getString("title"),
                    rs.getTimestamp("scheduled_time").toLocalDateTime(),
                    rs.getInt("duration_min"),
                    rs.getString("notes"));
            schedules.add(s);
        }
        return schedules;
    }

    public List<EventSchedule> recupererByEvent(int eventId) throws SQLException {
        List<EventSchedule> schedules = new ArrayList<>();
        String req = "SELECT * FROM event_schedule WHERE event_id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, eventId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            EventSchedule s = new EventSchedule(
                    rs.getInt("id"),
                    rs.getInt("event_id"),
                    rs.getString("title"),
                    rs.getTimestamp("scheduled_time").toLocalDateTime(),
                    rs.getInt("duration_min"),
                    rs.getString("notes"));
            schedules.add(s);
        }
        return schedules;
    }
}
