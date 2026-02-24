package services;

import entities.User;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceUser implements IService<User> {

    private Connection connection;

    public ServiceUser() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(User user) throws SQLException {
        String req = "INSERT INTO user (first_name, last_name, email, password, phone, role_id, is_active) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, user.getFirstName());
        ps.setString(2, user.getLastName());
        ps.setString(3, user.getEmail());
        ps.setString(4, user.getPassword());
        ps.setString(5, user.getPhone());
        ps.setInt(6, user.getRoleId());
        ps.setBoolean(7, user.isActive());
        ps.executeUpdate();
        System.out.println("User added: " + user.getEmail());
    }

    @Override
    public void modifier(User user) throws SQLException {
        String req = "UPDATE user SET first_name=?, last_name=?, email=?, password=?, phone=?, role_id=?, is_active=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, user.getFirstName());
        ps.setString(2, user.getLastName());
        ps.setString(3, user.getEmail());
        ps.setString(4, user.getPassword());
        ps.setString(5, user.getPhone());
        ps.setInt(6, user.getRoleId());
        ps.setBoolean(7, user.isActive());
        ps.setInt(8, user.getId());
        ps.executeUpdate();
        System.out.println("User updated: id=" + user.getId());
    }

    @Override
    public void supprimer(User user) throws SQLException {
        String req = "DELETE FROM user WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, user.getId());
        ps.executeUpdate();
        System.out.println("User deleted: id=" + user.getId());
    }

    @Override
    public List<User> recuperer() throws SQLException {
        List<User> list = new ArrayList<>();
        String req = "SELECT * FROM user";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            list.add(new User(
                    rs.getInt("id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getString("phone"),
                    rs.getInt("role_id"),
                    rs.getBoolean("is_active")));
        }
        return list;
    }
}
