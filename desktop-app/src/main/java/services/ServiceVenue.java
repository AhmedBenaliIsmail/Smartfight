package services;

import entities.Venue;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceVenue implements IService<Venue> {

    private Connection connection;

    public ServiceVenue() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Venue venue) throws SQLException {
        String req = "INSERT INTO venue (name, address, city, country, capacity, contact_email, contact_phone) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, venue.getName());
        ps.setString(2, venue.getAddress());
        ps.setString(3, venue.getCity());
        ps.setString(4, venue.getCountry());
        ps.setInt(5, venue.getCapacity());
        ps.setString(6, venue.getContactEmail());
        ps.setString(7, venue.getContactPhone());
        ps.executeUpdate();
        System.out.println("Venue added: " + venue.getName());
    }

    @Override
    public void modifier(Venue venue) throws SQLException {
        String req = "UPDATE venue SET name=?, address=?, city=?, country=?, capacity=?, contact_email=?, contact_phone=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, venue.getName());
        ps.setString(2, venue.getAddress());
        ps.setString(3, venue.getCity());
        ps.setString(4, venue.getCountry());
        ps.setInt(5, venue.getCapacity());
        ps.setString(6, venue.getContactEmail());
        ps.setString(7, venue.getContactPhone());
        ps.setInt(8, venue.getId());
        ps.executeUpdate();
        System.out.println("Venue updated: id=" + venue.getId());
    }

    @Override
    public void supprimer(Venue venue) throws SQLException {
        String req = "DELETE FROM venue WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, venue.getId());
        ps.executeUpdate();
        System.out.println("Venue deleted: id=" + venue.getId());
    }

    @Override
    public List<Venue> recuperer() throws SQLException {
        List<Venue> venues = new ArrayList<>();
        String req = "SELECT * FROM venue";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            Venue v = new Venue(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("address"),
                    rs.getString("city"),
                    rs.getString("country"),
                    rs.getInt("capacity"),
                    rs.getString("contact_email"),
                    rs.getString("contact_phone"));
            venues.add(v);
        }
        return venues;
    }
}
