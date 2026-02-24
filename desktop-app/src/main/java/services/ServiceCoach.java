package services;

import entities.Coach;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceCoach implements IService<Coach> {

    private Connection connection;

    public ServiceCoach() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Coach coach) throws SQLException {
        String req = "INSERT INTO coach (user_id, speciality, experience_years, certification, status) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, coach.getUserId());
        ps.setString(2, coach.getSpeciality());
        ps.setInt(3, coach.getExperienceYears());
        ps.setString(4, coach.getCertification());
        ps.setString(5, coach.getStatus());
        ps.executeUpdate();
        System.out.println("Coach added for userId=" + coach.getUserId());
    }

    @Override
    public void modifier(Coach coach) throws SQLException {
        String req = "UPDATE coach SET user_id=?, speciality=?, experience_years=?, certification=?, status=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, coach.getUserId());
        ps.setString(2, coach.getSpeciality());
        ps.setInt(3, coach.getExperienceYears());
        ps.setString(4, coach.getCertification());
        ps.setString(5, coach.getStatus());
        ps.setInt(6, coach.getId());
        ps.executeUpdate();
        System.out.println("Coach updated: id=" + coach.getId());
    }

    @Override
    public void supprimer(Coach coach) throws SQLException {
        String req = "DELETE FROM coach WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, coach.getId());
        ps.executeUpdate();
        System.out.println("Coach deleted: id=" + coach.getId());
    }

    @Override
    public List<Coach> recuperer() throws SQLException {
        List<Coach> list = new ArrayList<>();
        String req = "SELECT * FROM coach";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            list.add(new Coach(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("speciality"),
                    rs.getInt("experience_years"),
                    rs.getString("certification"),
                    rs.getString("status")));
        }
        return list;
    }
}
