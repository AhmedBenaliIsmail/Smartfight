package tn.smartfight.dao;

import tn.smartfight.config.DBConnection;
import tn.smartfight.model.FightResult;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FightResultDao {
    private static final Logger LOG = Logger.getLogger(FightResultDao.class.getName());

    private final DataSource dataSource;

    public FightResultDao() {
        this(DBConnection.getDataSource());
    }

    public FightResultDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<FightResult> findAll() {
        String sql = "SELECT fr.resultId, fr.fightNumber, fr.methodOfVictory, fr.status, " +
                "fr.roundNumber, fr.eventId, fr.fighter1Id, fr.fighter2Id, fr.winnerId, " +
                "e.eventName, " +
                "CONCAT(f1.firstName,' ',f1.lastName) AS fighter1Name, " +
                "CONCAT(f2.firstName,' ',f2.lastName) AS fighter2Name, " +
                "CONCAT(w.firstName,' ',w.lastName) AS winnerName " +
                "FROM fight_results fr " +
                "LEFT JOIN events e ON e.eventId = fr.eventId " +
                "LEFT JOIN fighters f1 ON f1.fighterId = fr.fighter1Id " +
                "LEFT JOIN fighters f2 ON f2.fighterId = fr.fighter2Id " +
                "LEFT JOIN fighters w ON w.fighterId = fr.winnerId " +
                "ORDER BY fr.resultId DESC";
        List<FightResult> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                FightResult r = new FightResult();
                r.setResultId(rs.getInt("resultId"));
                r.setFightNumber(rs.getInt("fightNumber"));
                r.setMethodOfVictory(rs.getString("methodOfVictory"));
                r.setStatus(rs.getString("status"));
                r.setRoundNumber(rs.getInt("roundNumber"));
                r.setEventId(rs.getInt("eventId"));
                r.setFighter1Id(rs.getInt("fighter1Id"));
                r.setFighter2Id(rs.getInt("fighter2Id"));
                int wid = rs.getInt("winnerId");
                r.setWinnerId(rs.wasNull() ? null : wid);
                r.setEventName(rs.getString("eventName"));
                r.setFighter1Name(rs.getString("fighter1Name"));
                r.setFighter2Name(rs.getString("fighter2Name"));
                r.setWinnerName(rs.getString("winnerName"));
                list.add(r);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "FightResultDao.findAll failed", e);
        }
        return list;
    }

    public FightResult getById(int id) {
        String sql = "SELECT resultId, fightNumber, methodOfVictory, decision_type, knockdown_round, " +
                "roundNumber, scheduled_rounds, is_belt_fight, belt_organization, " +
                "fighter1_odds, fighter2_odds, fightDate, status, eventId, " +
                "fighter1Id, fighter2Id, winnerId, inside_the_numbers, " +
                "highlight_video_url, video_path, updated_at " +
                "FROM fight_results WHERE resultId = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    FightResult r = new FightResult();
                    r.setResultId(rs.getInt("resultId"));
                    r.setFightNumber(rs.getInt("fightNumber"));
                    r.setMethodOfVictory(rs.getString("methodOfVictory"));
                    r.setDecisionType(rs.getString("decision_type"));
                    r.setKnockdownRound(rs.getInt("knockdown_round"));
                    r.setRoundNumber(rs.getInt("roundNumber"));
                    r.setScheduledRounds(rs.getInt("scheduled_rounds"));
                    r.setBeltFight(rs.getInt("is_belt_fight") == 1);
                    r.setBeltOrganization(rs.getString("belt_organization"));
                    r.setFighter1Odds(rs.getDouble("fighter1_odds"));
                    r.setFighter2Odds(rs.getDouble("fighter2_odds"));
                    r.setFightDate(rs.getObject("fightDate", LocalDateTime.class));
                    r.setStatus(rs.getString("status"));
                    r.setEventId(rs.getInt("eventId"));
                    r.setFighter1Id(rs.getInt("fighter1Id"));
                    r.setFighter2Id(rs.getInt("fighter2Id"));
                    int wid = rs.getInt("winnerId");
                    r.setWinnerId(rs.wasNull() ? null : wid);
                    r.setInsideTheNumbers(rs.getString("inside_the_numbers"));
                    r.setHighlightVideoUrl(rs.getString("highlight_video_url"));
                    r.setVideoPath(rs.getString("video_path"));
                    r.setUpdatedAt(rs.getObject("updated_at", LocalDateTime.class));
                    return r;
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "FightResultDao.getById failed id=" + id, e);
        }
        return null;
    }

    public int create(FightResult r) {
        String sql = "INSERT INTO fight_results " +
                "(fightNumber, methodOfVictory, decision_type, knockdown_round, roundNumber, " +
                "scheduled_rounds, is_belt_fight, belt_organization, fighter1_odds, fighter2_odds, " +
                "fightDate, status, eventId, fighter1Id, fighter2Id, winnerId, " +
                "inside_the_numbers, highlight_video_url, video_path, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, r.getFightNumber());
            stmt.setString(2, r.getMethodOfVictory());
            stmt.setString(3, r.getDecisionType());
            stmt.setInt(4, r.getKnockdownRound());
            stmt.setInt(5, r.getRoundNumber());
            stmt.setInt(6, r.getScheduledRounds());
            stmt.setInt(7, r.isBeltFight() ? 1 : 0);
            stmt.setString(8, r.getBeltOrganization());
            stmt.setDouble(9, r.getFighter1Odds());
            stmt.setDouble(10, r.getFighter2Odds());
            stmt.setObject(11, r.getFightDate());
            stmt.setString(12, r.getStatus() != null ? r.getStatus() : "SCHEDULED");
            stmt.setInt(13, r.getEventId());
            stmt.setInt(14, r.getFighter1Id());
            stmt.setInt(15, r.getFighter2Id());
            if (r.getWinnerId() != null) stmt.setInt(16, r.getWinnerId());
            else stmt.setNull(16, java.sql.Types.INTEGER);
            stmt.setString(17, r.getInsideTheNumbers());
            stmt.setString(18, r.getHighlightVideoUrl());
            stmt.setString(19, r.getVideoPath());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "FightResultDao.create failed", e);
        }
        return -1;
    }

    public void update(FightResult r) {
        String sql = "UPDATE fight_results SET " +
                "fightNumber=?, methodOfVictory=?, decision_type=?, knockdown_round=?, roundNumber=?, " +
                "scheduled_rounds=?, is_belt_fight=?, belt_organization=?, fighter1_odds=?, fighter2_odds=?, " +
                "fightDate=?, status=?, eventId=?, fighter1Id=?, fighter2Id=?, winnerId=?, " +
                "inside_the_numbers=?, highlight_video_url=?, video_path=?, updated_at=NOW() " +
                "WHERE resultId=?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, r.getFightNumber());
            stmt.setString(2, r.getMethodOfVictory());
            stmt.setString(3, r.getDecisionType());
            stmt.setInt(4, r.getKnockdownRound());
            stmt.setInt(5, r.getRoundNumber());
            stmt.setInt(6, r.getScheduledRounds());
            stmt.setInt(7, r.isBeltFight() ? 1 : 0);
            stmt.setString(8, r.getBeltOrganization());
            stmt.setDouble(9, r.getFighter1Odds());
            stmt.setDouble(10, r.getFighter2Odds());
            stmt.setObject(11, r.getFightDate());
            stmt.setString(12, r.getStatus());
            stmt.setInt(13, r.getEventId());
            stmt.setInt(14, r.getFighter1Id());
            stmt.setInt(15, r.getFighter2Id());
            if (r.getWinnerId() != null) stmt.setInt(16, r.getWinnerId());
            else stmt.setNull(16, java.sql.Types.INTEGER);
            stmt.setString(17, r.getInsideTheNumbers());
            stmt.setString(18, r.getHighlightVideoUrl());
            stmt.setString(19, r.getVideoPath());
            stmt.setInt(20, r.getResultId());
            stmt.executeUpdate();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "FightResultDao.update failed id=" + r.getResultId(), e);
        }
    }

    public List<FightResult> findByEventId(int eventId) {
        String sql = "SELECT fr.resultId, fr.fightNumber, fr.methodOfVictory, fr.status, " +
                "fr.roundNumber, fr.scheduled_rounds, fr.is_belt_fight, " +
                "fr.eventId, fr.fighter1Id, fr.fighter2Id, fr.winnerId, " +
                "CONCAT(f1.firstName,' ',f1.lastName) AS fighter1Name, " +
                "CONCAT(f2.firstName,' ',f2.lastName) AS fighter2Name, " +
                "CONCAT(w.firstName,' ',w.lastName) AS winnerName " +
                "FROM fight_results fr " +
                "LEFT JOIN fighters f1 ON f1.fighterId = fr.fighter1Id " +
                "LEFT JOIN fighters f2 ON f2.fighterId = fr.fighter2Id " +
                "LEFT JOIN fighters w  ON w.fighterId  = fr.winnerId " +
                "WHERE fr.eventId = ? ORDER BY fr.fightNumber";
        List<FightResult> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, eventId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    FightResult r = new FightResult();
                    r.setResultId(rs.getInt("resultId"));
                    r.setFightNumber(rs.getInt("fightNumber"));
                    r.setMethodOfVictory(rs.getString("methodOfVictory"));
                    r.setStatus(rs.getString("status"));
                    r.setRoundNumber(rs.getInt("roundNumber"));
                    r.setScheduledRounds(rs.getInt("scheduled_rounds"));
                    r.setBeltFight(rs.getInt("is_belt_fight") == 1);
                    r.setEventId(rs.getInt("eventId"));
                    r.setFighter1Id(rs.getInt("fighter1Id"));
                    r.setFighter2Id(rs.getInt("fighter2Id"));
                    int wid = rs.getInt("winnerId");
                    r.setWinnerId(rs.wasNull() ? null : wid);
                    r.setFighter1Name(rs.getString("fighter1Name"));
                    r.setFighter2Name(rs.getString("fighter2Name"));
                    r.setWinnerName(rs.getString("winnerName"));
                    list.add(r);
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "FightResultDao.findByEventId failed eventId=" + eventId, e);
        }
        return list;
    }

    public void deleteById(int id) {
        String sql = "DELETE FROM fight_results WHERE resultId = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "FightResultDao.deleteById failed id=" + id, e);
        }
    }
}
