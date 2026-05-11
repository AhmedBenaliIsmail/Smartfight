package tn.smartfight.dao;

import tn.smartfight.config.DBConnection;
import tn.smartfight.model.Fighter;
import tn.smartfight.model.FighterDetails;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FighterDao {
    private static final Logger LOG = Logger.getLogger(FighterDao.class.getName());

    private final DataSource dataSource;

    public FighterDao() { this(DBConnection.getDataSource()); }
    public FighterDao(DataSource dataSource) { this.dataSource = dataSource; }

    public List<Fighter> findAll() {
        String sql = "SELECT fighterId, firstName, lastName, nickname, nationality, " +
                "photo_filename, wins, losses, draws, koWins, technical_wins, decisionWins, ko_losses, " +
                "eloRating, performanceScore, winStreak, strengthOfSchedule, last_fight_date, titleDefenses, " +
                "height, reach, weight, age, weight_division_id, manager_id, " +
                "strikes_thrown, strikes_landed, ai_style_tag, ai_description, strength, weakness " +
                "FROM fighters ORDER BY eloRating DESC";
        List<Fighter> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(mapFighter(rs));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "FighterDao.findAll failed", e);
        }
        return list;
    }

    public FighterDetails getById(int id) {
        String sql = "SELECT fighterId, firstName, lastName, nickname, nationality, " +
                "photo_filename, wins, losses, draws, koWins, technical_wins, decisionWins, ko_losses, " +
                "eloRating, performanceScore, winStreak, strengthOfSchedule, last_fight_date, titleDefenses, " +
                "height, reach, weight, age, weight_division_id, manager_id, " +
                "strikes_thrown, strikes_landed, ai_style_tag, ai_description, strength, weakness " +
                "FROM fighters WHERE fighterId = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapDetails(rs);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "FighterDao.getById failed id=" + id, e);
        }
        return null;
    }

    public Fighter findById(int id) {
        String sql = "SELECT fighterId, firstName, lastName, nickname, nationality, " +
                "photo_filename, wins, losses, draws, koWins, technical_wins, decisionWins, ko_losses, " +
                "eloRating, performanceScore, winStreak, strengthOfSchedule, last_fight_date, titleDefenses, " +
                "height, reach, weight, age, weight_division_id, manager_id, " +
                "strikes_thrown, strikes_landed, ai_style_tag, ai_description, strength, weakness " +
                "FROM fighters WHERE fighterId = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapFighter(rs);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "FighterDao.findById failed id=" + id, e);
        }
        return null;
    }

    public void create(FighterDetails f) {
        String sql = "INSERT INTO fighters " +
                "(firstName, lastName, nickname, nationality, photo_filename, " +
                "wins, losses, draws, koWins, technical_wins, decisionWins, ko_losses, " +
                "eloRating, performanceScore, winStreak, strengthOfSchedule, last_fight_date, titleDefenses, " +
                "height, reach, weight, age, weight_division_id, manager_id, " +
                "strikes_thrown, strikes_landed, ai_style_tag, ai_description, strength, weakness) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParams(stmt, f);
            stmt.executeUpdate();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "FighterDao.create failed", e);
        }
    }

    public void update(FighterDetails f) {
        String sql = "UPDATE fighters SET " +
                "firstName=?, lastName=?, nickname=?, nationality=?, photo_filename=?, " +
                "wins=?, losses=?, draws=?, koWins=?, technical_wins=?, decisionWins=?, ko_losses=?, " +
                "eloRating=?, performanceScore=?, winStreak=?, strengthOfSchedule=?, last_fight_date=?, titleDefenses=?, " +
                "height=?, reach=?, weight=?, age=?, weight_division_id=?, manager_id=?, " +
                "strikes_thrown=?, strikes_landed=?, ai_style_tag=?, ai_description=?, strength=?, weakness=? " +
                "WHERE fighterId=?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParams(stmt, f);
            stmt.setInt(31, f.getFighterId());
            stmt.executeUpdate();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "FighterDao.update failed id=" + f.getFighterId(), e);
        }
    }

    public void deleteById(int id) {
        String sql = "DELETE FROM fighters WHERE fighterId = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "FighterDao.deleteById failed id=" + id, e);
        }
    }

    private void setParams(PreparedStatement stmt, FighterDetails f) throws SQLException {
        stmt.setString(1, f.getFirstName());
        stmt.setString(2, f.getLastName());
        stmt.setString(3, f.getNickname());
        stmt.setString(4, f.getNationality());
        stmt.setString(5, f.getPhotoFilename());
        stmt.setInt(6, f.getWins());
        stmt.setInt(7, f.getLosses());
        stmt.setInt(8, f.getDraws());
        stmt.setInt(9, f.getKoWins());
        stmt.setInt(10, f.getTechnicalWins());
        stmt.setInt(11, f.getDecisionWins());
        stmt.setInt(12, f.getKoLosses());
        stmt.setDouble(13, f.getEloRating());
        stmt.setDouble(14, f.getPerformanceScore());
        stmt.setInt(15, f.getWinStreak());
        stmt.setDouble(16, f.getStrengthOfSchedule());
        stmt.setObject(17, f.getLastFightDate());
        stmt.setInt(18, f.getTitleDefenses());
        if (f.getHeight() != null) stmt.setInt(19, f.getHeight()); else stmt.setNull(19, Types.INTEGER);
        if (f.getReach() != null) stmt.setInt(20, f.getReach()); else stmt.setNull(20, Types.INTEGER);
        if (f.getWeight() != null) stmt.setInt(21, f.getWeight()); else stmt.setNull(21, Types.INTEGER);
        if (f.getAge() != null) stmt.setInt(22, f.getAge()); else stmt.setNull(22, Types.INTEGER);
        if (f.getWeightDivisionId() != null) stmt.setInt(23, f.getWeightDivisionId()); else stmt.setNull(23, Types.INTEGER);
        if (f.getManagerId() != null) stmt.setInt(24, f.getManagerId()); else stmt.setNull(24, Types.INTEGER);
        stmt.setInt(25, f.getStrikesThrown());
        stmt.setInt(26, f.getStrikesLanded());
        stmt.setString(27, f.getAiStyleTag());
        stmt.setString(28, f.getAiDescription());
        stmt.setString(29, f.getStrength());
        stmt.setString(30, f.getWeakness());
    }

    private Fighter mapFighter(ResultSet rs) throws SQLException {
        Fighter f = new Fighter();
        f.setFighterId(rs.getInt("fighterId"));
        f.setFirstName(rs.getString("firstName"));
        f.setLastName(rs.getString("lastName"));
        f.setNickname(rs.getString("nickname"));
        f.setNationality(rs.getString("nationality"));
        f.setPhotoFilename(rs.getString("photo_filename"));
        f.setWins(rs.getInt("wins"));
        f.setLosses(rs.getInt("losses"));
        f.setDraws(rs.getInt("draws"));
        f.setKoWins(rs.getInt("koWins"));
        f.setTechnicalWins(rs.getInt("technical_wins"));
        f.setDecisionWins(rs.getInt("decisionWins"));
        f.setKoLosses(rs.getInt("ko_losses"));
        f.setEloRating(rs.getDouble("eloRating"));
        f.setPerformanceScore(rs.getDouble("performanceScore"));
        f.setWinStreak(rs.getInt("winStreak"));
        f.setStrengthOfSchedule(rs.getDouble("strengthOfSchedule"));
        Date ld = rs.getDate("last_fight_date");
        if (ld != null) f.setLastFightDate(ld.toLocalDate());
        f.setTitleDefenses(rs.getInt("titleDefenses"));
        int h = rs.getInt("height"); if (!rs.wasNull()) f.setHeight(h);
        int r = rs.getInt("reach"); if (!rs.wasNull()) f.setReach(r);
        int w = rs.getInt("weight"); if (!rs.wasNull()) f.setWeight(w);
        int a = rs.getInt("age"); if (!rs.wasNull()) f.setAge(a);
        int wd = rs.getInt("weight_division_id"); if (!rs.wasNull()) f.setWeightDivisionId(wd);
        int mid = rs.getInt("manager_id"); if (!rs.wasNull()) f.setManagerId(mid);
        f.setStrikesThrown(rs.getInt("strikes_thrown"));
        f.setStrikesLanded(rs.getInt("strikes_landed"));
        f.setAiStyleTag(rs.getString("ai_style_tag"));
        f.setAiDescription(rs.getString("ai_description"));
        f.setStrength(rs.getString("strength"));
        f.setWeakness(rs.getString("weakness"));
        return f;
    }

    private FighterDetails mapDetails(ResultSet rs) throws SQLException {
        FighterDetails f = new FighterDetails();
        f.setFighterId(rs.getInt("fighterId"));
        f.setFirstName(rs.getString("firstName"));
        f.setLastName(rs.getString("lastName"));
        f.setNickname(rs.getString("nickname"));
        f.setNationality(rs.getString("nationality"));
        f.setPhotoFilename(rs.getString("photo_filename"));
        f.setWins(rs.getInt("wins"));
        f.setLosses(rs.getInt("losses"));
        f.setDraws(rs.getInt("draws"));
        f.setKoWins(rs.getInt("koWins"));
        f.setTechnicalWins(rs.getInt("technical_wins"));
        f.setDecisionWins(rs.getInt("decisionWins"));
        f.setKoLosses(rs.getInt("ko_losses"));
        f.setEloRating(rs.getDouble("eloRating"));
        f.setPerformanceScore(rs.getDouble("performanceScore"));
        f.setWinStreak(rs.getInt("winStreak"));
        f.setStrengthOfSchedule(rs.getDouble("strengthOfSchedule"));
        Date ld = rs.getDate("last_fight_date");
        if (ld != null) f.setLastFightDate(ld.toLocalDate());
        f.setTitleDefenses(rs.getInt("titleDefenses"));
        int h = rs.getInt("height"); if (!rs.wasNull()) f.setHeight(h);
        int r = rs.getInt("reach"); if (!rs.wasNull()) f.setReach(r);
        int w = rs.getInt("weight"); if (!rs.wasNull()) f.setWeight(w);
        int a = rs.getInt("age"); if (!rs.wasNull()) f.setAge(a);
        int wd = rs.getInt("weight_division_id"); if (!rs.wasNull()) f.setWeightDivisionId(wd);
        int mid = rs.getInt("manager_id"); if (!rs.wasNull()) f.setManagerId(mid);
        f.setStrikesThrown(rs.getInt("strikes_thrown"));
        f.setStrikesLanded(rs.getInt("strikes_landed"));
        f.setAiStyleTag(rs.getString("ai_style_tag"));
        f.setAiDescription(rs.getString("ai_description"));
        f.setStrength(rs.getString("strength"));
        f.setWeakness(rs.getString("weakness"));
        return f;
    }
}
