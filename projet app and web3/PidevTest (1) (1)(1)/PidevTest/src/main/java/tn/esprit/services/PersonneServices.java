package tn.esprit.services;

import tn.esprit.config.DBConnection;
import tn.esprit.entities.Personne;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersonneServices {
    private Connection cnx;
    public PersonneServices(){

        cnx = DBConnection.getInstance().getCnx();
    }
    public void ajouter(Personne p) throws SQLException {

//        String sql ="insert into personne (nom,prenom,age)" +
//                "values('"+p.getNom()+"','"+p.getPrenom()+"',"+p.getAge()+")";
//        Statement st = cnx.createStatement();
//        st.executeUpdate(sql);
            String sql="insert into personne(nom,prenom,age) values(?,?,?)";
            PreparedStatement st = cnx.prepareStatement(sql);
            st.setString(1, p.getNom());
            st.setString(2, p.getPrenom());
            st.setInt(3,p.getAge());
            st.executeUpdate();
        }


    // ------------------------
    // Modifier une personne
    // ------------------------
    public void modifier(Personne p) throws SQLException {
        String sql = "UPDATE personne SET nom=?, prenom=? WHERE id=?";

        PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, p.getNom());
            ps.setString(2, p.getPrenom());
            ps.setLong(3, p.getId());
            ps.executeUpdate();
        }

    // ------------------------
    // Supprimer une personne
    // ------------------------
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM personne WHERE id=?";

             PreparedStatement ps = cnx.prepareStatement(sql);

            ps.setLong(1, id);
            ps.executeUpdate();
        }


    // ------------------------
    // Afficher toutes les personnes
    // ------------------------
    public List<Personne> afficher() throws SQLException {
        List<Personne> personnes = new ArrayList<>();
        String sql = "SELECT * FROM personne";


             Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                Personne p = new Personne(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom")
                );
                personnes.add(p);
            }
        return personnes;
    }
}
