package tn.smartfight.dao;

import tn.smartfight.config.DBConnection;
import tn.smartfight.model.Prediction;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PredictionDao {
    private static final Logger LOG = Logger.getLogger(PredictionDao.class.getName());
    private final DataSource dataSource;

    public PredictionDao() { this(DBConnection.getDataSource()); }
    public PredictionDao(DataSource ds) { this.dataSource = ds; }

    public List<Prediction> findByUserId(int userId) {
        String sql = "SELECT p.predictionId, p.userId, p.fightId, p.predictedWinnerId, " +
                "p.predicted_method, p.predicted_round, p.created_at, " +
                "CONCAT(f.firstName,' ',f.lastName) AS predictedWinnerName " +
                "FROM predictions p " +
                "LEFT JOIN fighters f ON f.fighterId = p.predictedWinnerId " +
                "WHERE p.userId=? ORDER BY p.created_at DESC";
        return query(sql, userId);
    }

    public Prediction findByFightAndUser(int fightId, int userId) {
        String sql = "SELECT p.predictionId, p.userId, p.fightId, p.predictedWinnerId, " +
                "p.predicted_method, p.predicted_round, p.created_at, " +
                "CONCAT(f.firstName,' ',f.lastName) AS predictedWinnerName " +
                "FROM predictions p " +
                "LEFT JOIN fighters f ON f.fighterId = p.predictedWinnerId " +
                "WHERE p.fightId=? AND p.userId=?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fightId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "findByFightAndUser failed", e);
        }
        return null;
    }

    public List<Prediction> findByFightId(int fightId) {
        String sql = "SELECT p.predictionId, p.userId, p.fightId, p.predictedWinnerId, " +
                "p.predicted_method, p.predicted_round, p.created_at, " +
                "CONCAT(f.firstName,' ',f.lastName) AS predictedWinnerName " +
                "FROM predictions p " +
                "LEFT JOIN fighters f ON f.fighterId = p.predictedWinnerId " +
                "WHERE p.fightId=?";
        return query(sql, fightId);
    }

    public void save(Prediction p) {
        Prediction existing = findByFightAndUser(p.getFightId(), p.getUserId());
        if (existing == null) insert(p);
        else { p.setPredictionId(existing.getPredictionId()); update(p); }
    }

    public void addPoints(int userId, int delta) {
        String sql = "UPDATE users SET predictionPoints = predictionPoints + ? WHERE userId=?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "addPoints failed userId=" + userId, e);
        }
    }

    public List<Object[]> findLeaderboard(int limit) {
        String sql = "SELECT userId, username, predictionPoints FROM users " +
                "ORDER BY predictionPoints DESC LIMIT ?";
        List<Object[]> rows = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                int rank = 1;
                while (rs.next()) {
                    rows.add(new Object[]{
                        rank++,
                        rs.getString("username"),
                        rs.getInt("predictionPoints")
                    });
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "findLeaderboard failed", e);
        }
        return rows;
    }

    private void insert(Prediction p) {
        String sql = "INSERT INTO predictions (userId, fightId, predictedWinnerId, predicted_method, predicted_round, created_at) " +
                "VALUES (?,?,?,?,?,NOW())";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, p);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) p.setPredictionId(keys.getInt(1));
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "insert prediction failed", e);
        }
    }

    private void update(Prediction p) {
        String sql = "UPDATE predictions SET predictedWinnerId=?, predicted_method=?, predicted_round=? " +
                "WHERE predictionId=?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (p.getPredictedWinnerId() != null) ps.setInt(1, p.getPredictedWinnerId());
            else ps.setNull(1, Types.INTEGER);
            ps.setString(2, p.getPredictedMethod());
            if (p.getPredictedRound() != null) ps.setInt(3, p.getPredictedRound());
            else ps.setNull(3, Types.INTEGER);
            ps.setInt(4, p.getPredictionId());
            ps.executeUpdate();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "update prediction failed id=" + p.getPredictionId(), e);
        }
    }

    private void bind(PreparedStatement ps, Prediction p) throws SQLException {
        ps.setInt(1, p.getUserId());
        ps.setInt(2, p.getFightId());
        if (p.getPredictedWinnerId() != null) ps.setInt(3, p.getPredictedWinnerId());
        else ps.setNull(3, Types.INTEGER);
        ps.setString(4, p.getPredictedMethod());
        if (p.getPredictedRound() != null) ps.setInt(5, p.getPredictedRound());
        else ps.setNull(5, Types.INTEGER);
    }

    private List<Prediction> query(String sql, int id) {
        List<Prediction> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "query predictions failed", e);
        }
        return list;
    }

    private Prediction map(ResultSet rs) throws SQLException {
        Prediction p = new Prediction();
        p.setPredictionId(rs.getInt("predictionId"));
        p.setUserId(rs.getInt("userId"));
        p.setFightId(rs.getInt("fightId"));
        int wid = rs.getInt("predictedWinnerId");
        p.setPredictedWinnerId(rs.wasNull() ? null : wid);
        p.setPredictedMethod(rs.getString("predicted_method"));
        int pr = rs.getInt("predicted_round");
        p.setPredictedRound(rs.wasNull() ? null : pr);
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) p.setCreatedAt(ca.toLocalDateTime());
        try { p.setPredictedWinnerName(rs.getString("predictedWinnerName")); } catch (Exception ignored) {}
        return p;
    }
}
