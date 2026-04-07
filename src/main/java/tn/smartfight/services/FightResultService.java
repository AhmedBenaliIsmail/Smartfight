package tn.smartfight.services;

import tn.smartfight.database.DBConnection;
import tn.smartfight.models.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FightResultService {

    private final Connection conn = DBConnection.getInstance().getConnection();

    private static final String BASE_SELECT =
        "SELECT fr.id, fr.event_id, e.name AS event_name, " +
        "       fr.fighter_red_id, " +
        "       CONCAT(ur.first_name,' ',ur.last_name) AS red_name, " +
        "       fr.fighter_blue_id, " +
        "       CONCAT(ub.first_name,' ',ub.last_name) AS blue_name, " +
        "       fr.winner_id, " +
        "       CONCAT(uw.first_name,' ',uw.last_name) AS winner_name, " +
        "       fr.method, fr.round_ended, fr.fight_date " +
        "FROM fight_result fr " +
        "JOIN event e ON fr.event_id = e.id " +
        "JOIN fighter fr2 ON fr.fighter_red_id = fr2.id " +
        "JOIN user ur ON fr2.user_id = ur.id " +
        "JOIN fighter fb ON fr.fighter_blue_id = fb.id " +
        "JOIN user ub ON fb.user_id = ub.id " +
        "LEFT JOIN fighter fw ON fr.winner_id = fw.id " +
        "LEFT JOIN user uw ON fw.user_id = uw.id ";

    // ── READ ─────────────────────────────────────────────────────────────────

    public List<FightResult> getCompletedFightResults() {
        List<FightResult> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(BASE_SELECT + "ORDER BY fr.fight_date DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapFightResult(rs));
        } catch (SQLException e) {
            System.err.println("[FightResultService] getCompletedFightResults: " + e.getMessage());
        }
        return list;
    }

    public FightResult getFightResultById(int id) {
        try (PreparedStatement ps = conn.prepareStatement(BASE_SELECT + "WHERE fr.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapFightResult(rs);
            }
        } catch (SQLException e) {
            System.err.println("[FightResultService] getFightResultById: " + e.getMessage());
        }
        return null;
    }

    public FightResult getFightResultByMatchId(int matchId) {
        String sql = "SELECT * FROM fight_result WHERE match_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, matchId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    FightResult fr = new FightResult();
                    fr.setId(rs.getInt("id"));
                    fr.setEventId(rs.getInt("event_id"));
                    fr.setFighterRedId(rs.getInt("fighter_red_id"));
                    fr.setFighterBlueId(rs.getInt("fighter_blue_id"));
                    fr.setWinnerId(rs.getInt("winner_id"));
                    fr.setMethod(rs.getString("method"));
                    fr.setRoundEnded(rs.getObject("round_ended", Integer.class));
                    java.sql.Date fd = rs.getDate("fight_date");
                    if (fd != null) fr.setFightDate(fd.toString());
                    return fr;
                }
            }
        } catch (SQLException e) {
            System.err.println("[FightResultService] getFightResultByMatchId: " + e.getMessage());
        }
        return null;
    }

    public List<FightResult> getFightResultsByFighter(int fighterId) {
        List<FightResult> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                BASE_SELECT + "WHERE fr.fighter_red_id = ? OR fr.fighter_blue_id = ? " +
                "ORDER BY fr.fight_date DESC")) {
            ps.setInt(1, fighterId);
            ps.setInt(2, fighterId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapFightResult(rs));
            }
        } catch (SQLException e) {
            System.err.println("[FightResultService] getFightResultsByFighter: " + e.getMessage());
        }
        return list;
    }

    // ── CREATE ───────────────────────────────────────────────────────────────

    public boolean createFightResult(FightResult fr) {
        String sql = "INSERT INTO fight_result (event_id, match_id, fighter_red_id, fighter_blue_id, " +
                     "winner_id, method, round_ended, fight_date, notes) VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, fr.getEventId());
            if (fr.getMatchId() > 0) ps.setInt(2, fr.getMatchId()); else ps.setNull(2, Types.INTEGER);
            ps.setInt(3, fr.getFighterRedId());
            ps.setInt(4, fr.getFighterBlueId());
            if (fr.getWinnerId() > 0) ps.setInt(5, fr.getWinnerId()); else ps.setNull(5, Types.INTEGER);
            ps.setString(6, fr.getMethod());
            if (fr.getRoundEnded() != null) ps.setInt(7, fr.getRoundEnded()); else ps.setNull(7, Types.INTEGER);
            ps.setString(8, fr.getFightDate());
            ps.setString(9, fr.getNotes());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) fr.setId(keys.getInt(1));
            }
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[FightResultService] createFightResult: " + e.getMessage());
            return false;
        }
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────

    public boolean updateFightResult(FightResult fr) {
        String sql = "UPDATE fight_result SET winner_id=?, method=?, round_ended=?, notes=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (fr.getWinnerId() > 0) ps.setInt(1, fr.getWinnerId()); else ps.setNull(1, Types.INTEGER);
            ps.setString(2, fr.getMethod());
            if (fr.getRoundEnded() != null) ps.setInt(3, fr.getRoundEnded()); else ps.setNull(3, Types.INTEGER);
            ps.setString(4, fr.getNotes());
            ps.setInt(5, fr.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[FightResultService] updateFightResult: " + e.getMessage());
            return false;
        }
    }

    // ── DELETE ───────────────────────────────────────────────────────────────

    public boolean deleteFightResult(int id) {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM fight_result WHERE id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[FightResultService] deleteFightResult: " + e.getMessage());
            return false;
        }
    }

    // ── HELPERS ──────────────────────────────────────────────────────────────

    private FightResult mapFightResult(ResultSet rs) throws SQLException {
        FightResult fr = new FightResult();
        fr.setId(rs.getInt("id"));
        fr.setEventId(rs.getInt("event_id"));
        fr.setEventName(rs.getString("event_name"));
        fr.setFighterRedId(rs.getInt("fighter_red_id"));
        fr.setFighterRedName(rs.getString("red_name"));
        fr.setFighterBlueId(rs.getInt("fighter_blue_id"));
        fr.setFighterBlueName(rs.getString("blue_name"));
        fr.setWinnerId(rs.getInt("winner_id"));
        fr.setWinnerName(rs.getString("winner_name"));
        fr.setMethod(rs.getString("method"));
        fr.setRoundEnded(rs.getObject("round_ended", Integer.class));
        java.sql.Date fd = rs.getDate("fight_date");
        if (fd != null) fr.setFightDate(fd.toString());
        return fr;
    }
}
