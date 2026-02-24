package services;

import entities.Admin;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceAdmin implements IService<Admin> {

    private Connection connection;

    public ServiceAdmin() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Admin admin) throws SQLException {
        String req = "INSERT INTO admin (user_id, access_level, department) VALUES (?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, admin.getUserId());
        ps.setString(2, admin.getAccessLevel());
        ps.setString(3, admin.getDepartment());
        ps.executeUpdate();
        System.out.println("Admin added for userId=" + admin.getUserId());
    }

    @Override
    public void modifier(Admin admin) throws SQLException {
        String req = "UPDATE admin SET user_id=?, access_level=?, department=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, admin.getUserId());
        ps.setString(2, admin.getAccessLevel());
        ps.setString(3, admin.getDepartment());
        ps.setInt(4, admin.getId());
        ps.executeUpdate();
        System.out.println("Admin updated: id=" + admin.getId());
    }

    @Override
    public void supprimer(Admin admin) throws SQLException {
        String req = "DELETE FROM admin WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, admin.getId());
        ps.executeUpdate();
        System.out.println("Admin deleted: id=" + admin.getId());
    }

    @Override
    public List<Admin> recuperer() throws SQLException {
        List<Admin> list = new ArrayList<>();
        String req = "SELECT * FROM admin";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            list.add(new Admin(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("access_level"),
                    rs.getString("department")));
        }
        return list;
    }
}
