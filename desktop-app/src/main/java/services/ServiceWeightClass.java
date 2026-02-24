package services;

import entities.WeightClass;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceWeightClass implements IService<WeightClass> {

    private Connection connection;

    public ServiceWeightClass() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(WeightClass wc) throws SQLException {
        String req = "INSERT INTO weight_class (name, min_weight, max_weight) VALUES (?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, wc.getName());
        ps.setDouble(2, wc.getMinWeight());
        ps.setDouble(3, wc.getMaxWeight());
        ps.executeUpdate();
        System.out.println("WeightClass added: " + wc.getName());
    }

    @Override
    public void modifier(WeightClass wc) throws SQLException {
        String req = "UPDATE weight_class SET name=?, min_weight=?, max_weight=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, wc.getName());
        ps.setDouble(2, wc.getMinWeight());
        ps.setDouble(3, wc.getMaxWeight());
        ps.setInt(4, wc.getId());
        ps.executeUpdate();
        System.out.println("WeightClass updated: id=" + wc.getId());
    }

    @Override
    public void supprimer(WeightClass wc) throws SQLException {
        String req = "DELETE FROM weight_class WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, wc.getId());
        ps.executeUpdate();
        System.out.println("WeightClass deleted: id=" + wc.getId());
    }

    @Override
    public List<WeightClass> recuperer() throws SQLException {
        List<WeightClass> list = new ArrayList<>();
        String req = "SELECT * FROM weight_class";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            list.add(new WeightClass(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("min_weight"),
                    rs.getDouble("max_weight")));
        }
        return list;
    }
}
