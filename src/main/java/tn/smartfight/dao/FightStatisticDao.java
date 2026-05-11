package tn.smartfight.dao;

import tn.smartfight.config.DBConnection;
import tn.smartfight.model.FightStatistic;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FightStatisticDao {
    private static final Logger LOG = Logger.getLogger(FightStatisticDao.class.getName());
    private final DataSource dataSource;

    public FightStatisticDao() { this(DBConnection.getDataSource()); }
    public FightStatisticDao(DataSource ds) { this.dataSource = ds; }

    public List<FightStatistic> findByFightId(int fightId) {
        String sql = "SELECT fs.id, fs.fight_result_id, fs.fighter_id, fs.round, " +
                "fs.punches_thrown, fs.punches_landed, fs.right_hand_thrown, fs.right_hand_landed, " +
                "fs.left_hand_thrown, fs.left_hand_landed, fs.power_punches_thrown, fs.power_punches_landed, " +
                "fs.jabs_thrown, fs.jabs_landed, fs.uppercuts_thrown, fs.uppercuts_landed, " +
                "fs.body_shots_landed, fs.knockdowns, fs.commentary, " +
                "CONCAT(f.firstName,' ',f.lastName) AS fighterName " +
                "FROM fight_statistic fs JOIN fighters f ON f.fighterId = fs.fighter_id " +
                "WHERE fs.fight_result_id=? ORDER BY fs.fighter_id, fs.round";
        List<FightStatistic> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fightId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "findByFightId failed fightId=" + fightId, e);
        }
        return list;
    }

    public FightStatistic findByFightFighterRound(int fightId, int fighterId, Integer roundNumber) {
        String sql = "SELECT fs.id, fs.fight_result_id, fs.fighter_id, fs.round, " +
                "fs.punches_thrown, fs.punches_landed, fs.right_hand_thrown, fs.right_hand_landed, " +
                "fs.left_hand_thrown, fs.left_hand_landed, fs.power_punches_thrown, fs.power_punches_landed, " +
                "fs.jabs_thrown, fs.jabs_landed, fs.uppercuts_thrown, fs.uppercuts_landed, " +
                "fs.body_shots_landed, fs.knockdowns, fs.commentary, " +
                "CONCAT(f.firstName,' ',f.lastName) AS fighterName " +
                "FROM fight_statistic fs JOIN fighters f ON f.fighterId = fs.fighter_id " +
                "WHERE fs.fight_result_id=? AND fs.fighter_id=? AND " +
                (roundNumber == null ? "fs.round IS NULL" : "fs.round=?");
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fightId);
            ps.setInt(2, fighterId);
            if (roundNumber != null) ps.setInt(3, roundNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "findByFightFighterRound failed", e);
        }
        return null;
    }

    public List<FightStatistic> findSummedByFighter(int fighterId) {
        String sql = "SELECT fs.fight_result_id, fs.fighter_id, " +
                "SUM(fs.punches_thrown) AS punches_thrown, SUM(fs.punches_landed) AS punches_landed, " +
                "SUM(fs.power_punches_thrown) AS power_punches_thrown, SUM(fs.power_punches_landed) AS power_punches_landed, " +
                "SUM(fs.knockdowns) AS knockdowns, " +
                "fr.winnerId, fr.methodOfVictory, fr.is_belt_fight " +
                "FROM fight_statistic fs " +
                "JOIN fight_results fr ON fr.resultId = fs.fight_result_id " +
                "WHERE fs.fighter_id=? AND fr.status='COMPLETED' AND fs.round IS NOT NULL " +
                "GROUP BY fs.fight_result_id, fs.fighter_id, fr.winnerId, fr.methodOfVictory, fr.is_belt_fight";
        List<FightStatistic> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fighterId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    FightStatistic s = new FightStatistic();
                    s.setFightId(rs.getInt("fight_result_id"));
                    s.setFighterId(rs.getInt("fighter_id"));
                    s.setPunchesThrown(rs.getInt("punches_thrown"));
                    s.setPunchesLanded(rs.getInt("punches_landed"));
                    s.setPowerPunchesThrown(rs.getInt("power_punches_thrown"));
                    s.setPowerPunchesLanded(rs.getInt("power_punches_landed"));
                    s.setKnockdowns(rs.getInt("knockdowns"));
                    // store extra data in commentary field (temp) for engine
                    int winnerId = rs.getInt("winnerId");
                    boolean isWinner = !rs.wasNull() && winnerId == fighterId;
                    String method = rs.getString("methodOfVictory");
                    boolean isBelt = rs.getInt("is_belt_fight") == 1;
                    s.setCommentary(isWinner + "|" + method + "|" + isBelt);
                    list.add(s);
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "findSummedByFighter failed fighterId=" + fighterId, e);
        }
        return list;
    }

    /** Returns all fights that have at least one stat row, with aggregated totals per fighter. */
    public List<FightStatistic> findAggregatedByFight() {
        String sql = "SELECT fs.fight_result_id, fs.fighter_id, " +
                "SUM(fs.punches_thrown) AS punches_thrown, SUM(fs.punches_landed) AS punches_landed, " +
                "SUM(fs.power_punches_thrown) AS power_punches_thrown, SUM(fs.power_punches_landed) AS power_punches_landed, " +
                "SUM(fs.jabs_thrown) AS jabs_thrown, SUM(fs.jabs_landed) AS jabs_landed, " +
                "SUM(fs.body_shots_landed) AS body_shots_landed, SUM(fs.knockdowns) AS knockdowns, " +
                "CONCAT(f.firstName,' ',f.lastName) AS fighterName " +
                "FROM fight_statistic fs JOIN fighters f ON f.fighterId = fs.fighter_id " +
                "WHERE fs.round IS NOT NULL " +
                "GROUP BY fs.fight_result_id, fs.fighter_id ORDER BY fs.fight_result_id DESC";
        List<FightStatistic> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                FightStatistic s = new FightStatistic();
                s.setFightId(rs.getInt("fight_result_id"));
                s.setFighterId(rs.getInt("fighter_id"));
                s.setPunchesThrown(rs.getInt("punches_thrown"));
                s.setPunchesLanded(rs.getInt("punches_landed"));
                s.setPowerPunchesThrown(rs.getInt("power_punches_thrown"));
                s.setPowerPunchesLanded(rs.getInt("power_punches_landed"));
                s.setJabsThrown(rs.getInt("jabs_thrown"));
                s.setJabsLanded(rs.getInt("jabs_landed"));
                s.setBodyShotsLanded(rs.getInt("body_shots_landed"));
                s.setKnockdowns(rs.getInt("knockdowns"));
                s.setFighterName(rs.getString("fighterName"));
                list.add(s);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "findAggregatedByFight failed", e);
        }
        return list;
    }

    public void deleteByFightId(int fightId) {
        String sql = "DELETE FROM fight_statistic WHERE fight_result_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fightId);
            ps.executeUpdate();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "deleteByFightId failed fightId=" + fightId, e);
        }
    }

    public void save(FightStatistic s) {
        FightStatistic existing = findByFightFighterRound(s.getFightId(), s.getFighterId(), s.getRoundNumber());
        if (existing == null) insert(s);
        else { s.setId(existing.getId()); update(s); }
    }

    private void insert(FightStatistic s) {
        String sql = "INSERT INTO fight_statistic " +
                "(fight_result_id, fighter_id, round, punches_thrown, punches_landed, " +
                "right_hand_thrown, right_hand_landed, left_hand_thrown, left_hand_landed, " +
                "power_punches_thrown, power_punches_landed, jabs_thrown, jabs_landed, " +
                "uppercuts_thrown, uppercuts_landed, body_shots_landed, knockdowns, commentary) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, s);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) s.setId(keys.getInt(1));
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "insert fight_statistic failed", e);
        }
    }

    private void update(FightStatistic s) {
        String sql = "UPDATE fight_statistic SET " +
                "punches_thrown=?, punches_landed=?, right_hand_thrown=?, right_hand_landed=?, " +
                "left_hand_thrown=?, left_hand_landed=?, power_punches_thrown=?, power_punches_landed=?, " +
                "jabs_thrown=?, jabs_landed=?, uppercuts_thrown=?, uppercuts_landed=?, " +
                "body_shots_landed=?, knockdowns=?, commentary=? WHERE id=?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1,  s.getPunchesThrown());
            ps.setInt(2,  s.getPunchesLanded());
            ps.setInt(3,  s.getRightHandThrown());
            ps.setInt(4,  s.getRightHandLanded());
            ps.setInt(5,  s.getLeftHandThrown());
            ps.setInt(6,  s.getLeftHandLanded());
            ps.setInt(7,  s.getPowerPunchesThrown());
            ps.setInt(8,  s.getPowerPunchesLanded());
            ps.setInt(9,  s.getJabsThrown());
            ps.setInt(10, s.getJabsLanded());
            ps.setInt(11, s.getUppercutsThrown());
            ps.setInt(12, s.getUppercutsLanded());
            ps.setInt(13, s.getBodyShotsLanded());
            ps.setInt(14, s.getKnockdowns());
            ps.setString(15, s.getCommentary());
            ps.setInt(16, s.getId());
            ps.executeUpdate();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "update fight_statistic failed id=" + s.getId(), e);
        }
    }

    private void bind(PreparedStatement ps, FightStatistic s) throws SQLException {
        ps.setInt(1,  s.getFightId());
        ps.setInt(2,  s.getFighterId());
        if (s.getRoundNumber() != null) ps.setInt(3, s.getRoundNumber());
        else ps.setNull(3, Types.INTEGER);
        ps.setInt(4,  s.getPunchesThrown());
        ps.setInt(5,  s.getPunchesLanded());
        ps.setInt(6,  s.getRightHandThrown());
        ps.setInt(7,  s.getRightHandLanded());
        ps.setInt(8,  s.getLeftHandThrown());
        ps.setInt(9,  s.getLeftHandLanded());
        ps.setInt(10, s.getPowerPunchesThrown());
        ps.setInt(11, s.getPowerPunchesLanded());
        ps.setInt(12, s.getJabsThrown());
        ps.setInt(13, s.getJabsLanded());
        ps.setInt(14, s.getUppercutsThrown());
        ps.setInt(15, s.getUppercutsLanded());
        ps.setInt(16, s.getBodyShotsLanded());
        ps.setInt(17, s.getKnockdowns());
        ps.setString(18, s.getCommentary());
    }

    private FightStatistic map(ResultSet rs) throws SQLException {
        FightStatistic s = new FightStatistic();
        s.setId(rs.getInt("id"));
        s.setFightId(rs.getInt("fight_result_id"));
        s.setFighterId(rs.getInt("fighter_id"));
        int rn = rs.getInt("round");
        s.setRoundNumber(rs.wasNull() ? null : rn);
        s.setPunchesThrown(rs.getInt("punches_thrown"));
        s.setPunchesLanded(rs.getInt("punches_landed"));
        s.setRightHandThrown(rs.getInt("right_hand_thrown"));
        s.setRightHandLanded(rs.getInt("right_hand_landed"));
        s.setLeftHandThrown(rs.getInt("left_hand_thrown"));
        s.setLeftHandLanded(rs.getInt("left_hand_landed"));
        s.setPowerPunchesThrown(rs.getInt("power_punches_thrown"));
        s.setPowerPunchesLanded(rs.getInt("power_punches_landed"));
        s.setJabsThrown(rs.getInt("jabs_thrown"));
        s.setJabsLanded(rs.getInt("jabs_landed"));
        s.setUppercutsThrown(rs.getInt("uppercuts_thrown"));
        s.setUppercutsLanded(rs.getInt("uppercuts_landed"));
        s.setBodyShotsLanded(rs.getInt("body_shots_landed"));
        s.setKnockdowns(rs.getInt("knockdowns"));
        s.setCommentary(rs.getString("commentary"));
        try { s.setFighterName(rs.getString("fighterName")); } catch (Exception ignored) {}
        return s;
    }
}
