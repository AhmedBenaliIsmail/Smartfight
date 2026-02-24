package services;

import entities.Discipline;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceDiscipline implements IService<Discipline> {

    private Connection connection;

    public ServiceDiscipline() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Discipline discipline) throws SQLException {
        String req = "INSERT INTO discipline (name, description, weight_class, round_duration, max_rounds) " +
                "VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, discipline.getName());
        ps.setString(2, discipline.getDescription());
        ps.setString(3, discipline.getWeightClass());
        ps.setInt(4, discipline.getRoundDuration());
        ps.setInt(5, discipline.getMaxRounds());
        ps.executeUpdate();
        System.out.println("Discipline added: " + discipline.getName());
    }

    @Override
    public void modifier(Discipline discipline) throws SQLException {
        String req = "UPDATE discipline SET name=?, description=?, weight_class=?, round_duration=?, max_rounds=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, discipline.getName());
        ps.setString(2, discipline.getDescription());
        ps.setString(3, discipline.getWeightClass());
        ps.setInt(4, discipline.getRoundDuration());
        ps.setInt(5, discipline.getMaxRounds());
        ps.setInt(6, discipline.getId());
        ps.executeUpdate();
        System.out.println("Discipline updated: id=" + discipline.getId());
    }

    @Override
    public void supprimer(Discipline discipline) throws SQLException {
        String req = "DELETE FROM discipline WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, discipline.getId());
        ps.executeUpdate();
        System.out.println("Discipline deleted: id=" + discipline.getId());
    }

    @Override
    public List<Discipline> recuperer() throws SQLException {
        List<Discipline> disciplines = new ArrayList<>();
        String req = "SELECT * FROM discipline";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            Discipline d = new Discipline(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("weight_class"),
                    rs.getInt("round_duration"),
                    rs.getInt("max_rounds"));
            disciplines.add(d);
        }
        return disciplines;
    }
}
