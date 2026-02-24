package services;

import entities.FanProfile;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceFanProfile implements IService<FanProfile> {

    private Connection connection;

    public ServiceFanProfile() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(FanProfile fp) throws SQLException {
        String req = "INSERT INTO fan_profile (user_id, favorite_sport, country, bio) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, fp.getUserId());
        ps.setString(2, fp.getFavoriteSport());
        ps.setString(3, fp.getCountry());
        ps.setString(4, fp.getBio());
        ps.executeUpdate();
        System.out.println("FanProfile added for userId=" + fp.getUserId());
    }

    @Override
    public void modifier(FanProfile fp) throws SQLException {
        String req = "UPDATE fan_profile SET user_id=?, favorite_sport=?, country=?, bio=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, fp.getUserId());
        ps.setString(2, fp.getFavoriteSport());
        ps.setString(3, fp.getCountry());
        ps.setString(4, fp.getBio());
        ps.setInt(5, fp.getId());
        ps.executeUpdate();
        System.out.println("FanProfile updated: id=" + fp.getId());
    }

    @Override
    public void supprimer(FanProfile fp) throws SQLException {
        String req = "DELETE FROM fan_profile WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, fp.getId());
        ps.executeUpdate();
        System.out.println("FanProfile deleted: id=" + fp.getId());
    }

    @Override
    public List<FanProfile> recuperer() throws SQLException {
        List<FanProfile> list = new ArrayList<>();
        String req = "SELECT * FROM fan_profile";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            list.add(new FanProfile(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("favorite_sport"),
                    rs.getString("country"),
                    rs.getString("bio")));
        }
        return list;
    }
}
