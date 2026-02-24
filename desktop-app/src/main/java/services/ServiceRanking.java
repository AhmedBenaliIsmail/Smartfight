package services;

import entities.Ranking;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceRanking implements IService<Ranking> {

    private Connection connection;

    public ServiceRanking() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Ranking ranking) throws SQLException {
        String req = "INSERT INTO ranking (fighter_id, discipline_id, rank_position, points, season) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, ranking.getFighterId());
        if (ranking.getDisciplineId() > 0)
            ps.setInt(2, ranking.getDisciplineId());
        else
            ps.setNull(2, Types.INTEGER);
        ps.setInt(3, ranking.getRankPosition());
        ps.setDouble(4, ranking.getPoints());
        ps.setString(5, ranking.getSeason());
        ps.executeUpdate();
        System.out.println("Ranking added: fighter=" + ranking.getFighterId());
    }

    @Override
    public void modifier(Ranking ranking) throws SQLException {
        String req = "UPDATE ranking SET fighter_id=?, discipline_id=?, rank_position=?, points=?, season=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, ranking.getFighterId());
        if (ranking.getDisciplineId() > 0)
            ps.setInt(2, ranking.getDisciplineId());
        else
            ps.setNull(2, Types.INTEGER);
        ps.setInt(3, ranking.getRankPosition());
        ps.setDouble(4, ranking.getPoints());
        ps.setString(5, ranking.getSeason());
        ps.setInt(6, ranking.getId());
        ps.executeUpdate();
        System.out.println("Ranking updated: id=" + ranking.getId());
    }

    @Override
    public void supprimer(Ranking ranking) throws SQLException {
        String req = "DELETE FROM ranking WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, ranking.getId());
        ps.executeUpdate();
        System.out.println("Ranking deleted: id=" + ranking.getId());
    }

    @Override
    public List<Ranking> recuperer() throws SQLException {
        List<Ranking> list = new ArrayList<>();
        String req = "SELECT * FROM ranking ORDER BY rank_position ASC";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            list.add(new Ranking(
                    rs.getInt("id"),
                    rs.getInt("fighter_id"),
                    rs.getInt("discipline_id"),
                    rs.getInt("rank_position"),
                    rs.getDouble("points"),
                    rs.getString("season")));
        }
        return list;
    }
}
