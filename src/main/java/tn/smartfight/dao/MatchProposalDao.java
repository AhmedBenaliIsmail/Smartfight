package tn.smartfight.dao;

import tn.smartfight.config.DBConnection;
import tn.smartfight.model.MatchProposal;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MatchProposalDao {
    private static final Logger LOG = Logger.getLogger(MatchProposalDao.class.getName());
    private final DataSource ds;

    public MatchProposalDao() { this(DBConnection.getDataSource()); }
    public MatchProposalDao(DataSource ds) { this.ds = ds; }

    private static final String SELECT_BASE =
            "SELECT mp.id, mp.compatibility, mp.status, mp.proposed_at, mp.notes, " +
            "mp.event_id, mp.fighter1_id, mp.fighter2_id, mp.vote_count, mp.weight_division_id, " +
            "CONCAT(f1.firstName,' ',f1.lastName) AS f1Name, " +
            "CONCAT(f2.firstName,' ',f2.lastName) AS f2Name, " +
            "wd.name AS divisionName " +
            "FROM match_proposal mp " +
            "LEFT JOIN fighters f1 ON f1.fighterId=mp.fighter1_id " +
            "LEFT JOIN fighters f2 ON f2.fighterId=mp.fighter2_id " +
            "LEFT JOIN weight_division wd ON wd.id=mp.weight_division_id";

    public List<MatchProposal> findAll() {
        List<MatchProposal> list = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_BASE + " ORDER BY mp.vote_count DESC, mp.proposed_at DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "findAll failed", e);
        }
        return list;
    }

    public List<MatchProposal> findByStatus(String status) {
        List<MatchProposal> list = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_BASE + " WHERE mp.status=? ORDER BY mp.vote_count DESC")) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "findByStatus failed", e);
        }
        return list;
    }

    public int create(MatchProposal mp) {
        String sql = "INSERT INTO match_proposal (compatibility, status, proposed_at, notes, event_id, fighter1_id, fighter2_id, vote_count, weight_division_id) " +
                "VALUES (?,?,NOW(),?,?,?,?,0,?)";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setObject(1, mp.getCompatibility());
            ps.setString(2, mp.getStatus() != null ? mp.getStatus() : "PENDING");
            ps.setString(3, mp.getNotes());
            ps.setObject(4, mp.getEventId());
            ps.setInt(5, mp.getFighter1Id());
            ps.setInt(6, mp.getFighter2Id());
            ps.setObject(7, mp.getWeightDivisionId());
            ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) return gk.getInt(1);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "create failed", e);
        }
        return -1;
    }

    public void updateStatus(int id, String status) {
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE match_proposal SET status=? WHERE id=?")) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "updateStatus failed id=" + id, e);
        }
    }

    public void deleteById(int id) {
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM match_proposal WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "deleteById failed id=" + id, e);
        }
    }

    private MatchProposal map(ResultSet rs) throws SQLException {
        MatchProposal mp = new MatchProposal();
        mp.setId(rs.getInt("id"));
        BigDecimal compat = rs.getBigDecimal("compatibility");
        mp.setCompatibility(compat);
        mp.setStatus(rs.getString("status"));
        Timestamp ts = rs.getTimestamp("proposed_at");
        if (ts != null) mp.setProposedAt(ts.toLocalDateTime());
        mp.setNotes(rs.getString("notes"));
        int eid = rs.getInt("event_id"); if (!rs.wasNull()) mp.setEventId(eid);
        mp.setFighter1Id(rs.getInt("fighter1_id"));
        mp.setFighter2Id(rs.getInt("fighter2_id"));
        mp.setVoteCount(rs.getInt("vote_count"));
        int wdid = rs.getInt("weight_division_id"); if (!rs.wasNull()) mp.setWeightDivisionId(wdid);
        mp.setFighter1Name(rs.getString("f1Name"));
        mp.setFighter2Name(rs.getString("f2Name"));
        mp.setDivisionName(rs.getString("divisionName"));
        return mp;
    }
}
