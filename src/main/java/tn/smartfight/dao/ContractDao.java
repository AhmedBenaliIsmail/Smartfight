package tn.smartfight.dao;

import tn.smartfight.config.DBConnection;
import tn.smartfight.model.FighterContract;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ContractDao {
    private static final Logger LOG = Logger.getLogger(ContractDao.class.getName());
    private final DataSource ds;

    public ContractDao() { this(DBConnection.getDataSource()); }
    public ContractDao(DataSource ds) { this.ds = ds; }

    private static final String SELECT_ALL =
            "SELECT fc.id, fc.base_pay, fc.win_bonus, fc.calculated_payout, fc.is_paid, " +
            "fc.fighter_id, fc.event_id, fc.missed_weight, fc.manager_fee_percent, " +
            "CONCAT(f.firstName,' ',f.lastName) AS fighterName, e.eventName " +
            "FROM fighter_contract fc " +
            "LEFT JOIN fighters f ON f.fighterId=fc.fighter_id " +
            "LEFT JOIN events e ON e.eventId=fc.event_id";

    public List<FighterContract> findAll() {
        List<FighterContract> list = new ArrayList<>();
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(SELECT_ALL + " ORDER BY fc.id DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "findAll failed", e);
        }
        return list;
    }

    public FighterContract findById(int id) {
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_ALL + " WHERE fc.id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "findById failed id=" + id, e);
        }
        return null;
    }

    public int create(FighterContract fc) {
        String sql = "INSERT INTO fighter_contract (base_pay, win_bonus, calculated_payout, is_paid, fighter_id, event_id, missed_weight, manager_fee_percent) " +
                "VALUES (?,?,?,0,?,?,?,?)";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setBigDecimal(1, fc.getBasePay());
            ps.setBigDecimal(2, fc.getWinBonus());
            ps.setObject(3, fc.getCalculatedPayout());
            ps.setInt(4, fc.getFighterId());
            ps.setInt(5, fc.getEventId());
            ps.setBoolean(6, fc.isMissedWeight());
            ps.setDouble(7, fc.getManagerFeePercent());
            ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) return gk.getInt(1);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "create failed", e);
        }
        return -1;
    }

    public void update(FighterContract fc) {
        String sql = "UPDATE fighter_contract SET base_pay=?, win_bonus=?, calculated_payout=?, is_paid=?, missed_weight=?, manager_fee_percent=? WHERE id=?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBigDecimal(1, fc.getBasePay());
            ps.setBigDecimal(2, fc.getWinBonus());
            ps.setObject(3, fc.getCalculatedPayout());
            ps.setBoolean(4, fc.isPaid());
            ps.setBoolean(5, fc.isMissedWeight());
            ps.setDouble(6, fc.getManagerFeePercent());
            ps.setInt(7, fc.getId());
            ps.executeUpdate();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "update failed id=" + fc.getId(), e);
        }
    }

    public void deleteById(int id) {
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM fighter_contract WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "deleteById failed id=" + id, e);
        }
    }

    private FighterContract map(ResultSet rs) throws SQLException {
        FighterContract fc = new FighterContract();
        fc.setId(rs.getInt("id"));
        BigDecimal bp = rs.getBigDecimal("base_pay");
        fc.setBasePay(bp != null ? bp : BigDecimal.ZERO);
        BigDecimal wb = rs.getBigDecimal("win_bonus");
        fc.setWinBonus(wb != null ? wb : BigDecimal.ZERO);
        fc.setCalculatedPayout(rs.getBigDecimal("calculated_payout"));
        fc.setPaid(rs.getBoolean("is_paid"));
        fc.setFighterId(rs.getInt("fighter_id"));
        fc.setEventId(rs.getInt("event_id"));
        fc.setMissedWeight(rs.getBoolean("missed_weight"));
        fc.setManagerFeePercent(rs.getDouble("manager_fee_percent"));
        fc.setFighterName(rs.getString("fighterName"));
        fc.setEventName(rs.getString("eventName"));
        return fc;
    }
}
