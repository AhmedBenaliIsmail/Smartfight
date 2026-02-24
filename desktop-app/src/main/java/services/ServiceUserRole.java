package services;

import entities.UserRole;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceUserRole implements IService<UserRole> {

    private Connection connection;

    public ServiceUserRole() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(UserRole role) throws SQLException {
        String req = "INSERT INTO user_role (name, description) VALUES (?, ?)";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, role.getName());
        ps.setString(2, role.getDescription());
        ps.executeUpdate();
        System.out.println("UserRole added: " + role.getName());
    }

    @Override
    public void modifier(UserRole role) throws SQLException {
        String req = "UPDATE user_role SET name=?, description=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, role.getName());
        ps.setString(2, role.getDescription());
        ps.setInt(3, role.getId());
        ps.executeUpdate();
        System.out.println("UserRole updated: id=" + role.getId());
    }

    @Override
    public void supprimer(UserRole role) throws SQLException {
        String req = "DELETE FROM user_role WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, role.getId());
        ps.executeUpdate();
        System.out.println("UserRole deleted: id=" + role.getId());
    }

    @Override
    public List<UserRole> recuperer() throws SQLException {
        List<UserRole> list = new ArrayList<>();
        String req = "SELECT * FROM user_role";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            list.add(new UserRole(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description")));
        }
        return list;
    }
}
