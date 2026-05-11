package tn.smartfight.dao;

import tn.smartfight.config.DBConnection;
import tn.smartfight.model.FanReaction;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FanReactionDao {
    private static final Logger LOG = Logger.getLogger(FanReactionDao.class.getName());

    private final DataSource dataSource;

    public FanReactionDao() { this(DBConnection.getDataSource()); }
    public FanReactionDao(DataSource ds) { this.dataSource = ds; }

    public List<FanReaction> findRecent(int limit) {
        String sql =
            "SELECT fr.id, fr.reaction_type, fr.comment, fr.reacted_at, " +
            "fr.is_pinned, fr.is_deleted, fr.fight_result_id, fr.fan_id, " +
            "u.username AS fanUsername, " +
            "e.eventName, " +
            "CONCAT(f1.firstName,' ',f1.lastName) AS fighter1Name, " +
            "CONCAT(f2.firstName,' ',f2.lastName) AS fighter2Name " +
            "FROM fan_reactions fr " +
            "JOIN users u        ON u.userId        = fr.fan_id " +
            "JOIN fight_results r ON r.resultId     = fr.fight_result_id " +
            "JOIN events e       ON e.eventId       = r.eventId " +
            "JOIN fighters f1    ON f1.fighterId    = r.fighter1Id " +
            "JOIN fighters f2    ON f2.fighterId    = r.fighter2Id " +
            "WHERE fr.is_deleted = 0 " +
            "ORDER BY fr.is_pinned DESC, fr.reacted_at DESC " +
            "LIMIT ?";
        List<FanReaction> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "FanReactionDao.findRecent failed", e);
        }
        return list;
    }

    public void create(FanReaction r) {
        String sql =
            "INSERT INTO fan_reactions (reaction_type, comment, reacted_at, " +
            "is_pinned, is_deleted, fight_result_id, fan_id) " +
            "VALUES (?, ?, NOW(), 0, 0, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getReactionType());
            ps.setString(2, r.getComment());
            ps.setInt(3, r.getFightResultId());
            ps.setInt(4, r.getFanId());
            ps.executeUpdate();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "FanReactionDao.create failed", e);
        }
    }

    /** Returns rows as [resultId (String), "EventName: F1 vs F2"] for completed fights. */
    public List<String[]> findCompletedFights() {
        String sql =
            "SELECT r.resultId, e.eventName, " +
            "CONCAT(f1.firstName,' ',f1.lastName) AS f1, " +
            "CONCAT(f2.firstName,' ',f2.lastName) AS f2 " +
            "FROM fight_results r " +
            "JOIN events   e  ON e.eventId    = r.eventId " +
            "JOIN fighters f1 ON f1.fighterId = r.fighter1Id " +
            "JOIN fighters f2 ON f2.fighterId = r.fighter2Id " +
            "WHERE e.status = 'COMPLETED' " +
            "ORDER BY e.eventDate DESC LIMIT 50";
        List<String[]> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String label = rs.getString("eventName") + ": " +
                        rs.getString("f1") + " vs " + rs.getString("f2");
                list.add(new String[]{ rs.getString("resultId"), label });
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "FanReactionDao.findCompletedFights failed", e);
        }
        return list;
    }

    private FanReaction map(ResultSet rs) throws SQLException {
        FanReaction r = new FanReaction();
        r.setId(rs.getInt("id"));
        r.setReactionType(rs.getString("reaction_type"));
        r.setComment(rs.getString("comment"));
        Timestamp ra = rs.getTimestamp("reacted_at");
        if (ra != null) r.setReactedAt(ra.toLocalDateTime());
        r.setPinned(rs.getBoolean("is_pinned"));
        r.setDeleted(rs.getBoolean("is_deleted"));
        r.setFightResultId(rs.getInt("fight_result_id"));
        r.setFanId(rs.getInt("fan_id"));
        r.setFanUsername(rs.getString("fanUsername"));
        r.setEventName(rs.getString("eventName"));
        r.setFighter1Name(rs.getString("fighter1Name"));
        r.setFighter2Name(rs.getString("fighter2Name"));
        return r;
    }
}
