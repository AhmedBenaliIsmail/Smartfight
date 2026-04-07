package dao;

import app.util.DBCNX;
import model.Ranking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RankingDao {

    public List<Ranking> getAllRankings() {
        List<Ranking> rankings = new ArrayList<>();
        String sql = "SELECT id, fighter_id, rank_position, points, season FROM ranking ORDER BY rank_position ASC";
        try (Connection conn = DBCNX.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                rankings.add(map(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rankings;
    }

    public Ranking getRankingByFighterAndSeason(int fighterId, String season) {
        String sql = "SELECT id, fighter_id, rank_position, points, season FROM ranking WHERE fighter_id = ? AND season = ? LIMIT 1";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fighterId);
            ps.setString(2, season);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addRanking(Ranking ranking) {
        String sql = "INSERT INTO ranking (fighter_id, rank_position, points, season, updated_at) VALUES (?, ?, ?, ?, NOW())";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ranking.getFighterId());
            ps.setInt(2, ranking.getRankPosition());
            ps.setDouble(3, ranking.getPoints());
            ps.setString(4, ranking.getSeason());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateRanking(Ranking ranking) {
        String sql = "UPDATE ranking SET rank_position = ?, points = ?, season = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DBCNX.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ranking.getRankPosition());
            ps.setDouble(2, ranking.getPoints());
            ps.setString(3, ranking.getSeason());
            ps.setInt(4, ranking.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private Ranking map(ResultSet rs) throws Exception {
        Ranking r = new Ranking();
        r.setId(rs.getInt("id"));
        r.setFighterId(rs.getInt("fighter_id"));
        r.setRankPosition(rs.getInt("rank_position"));
        r.setPoints(rs.getDouble("points"));
        r.setSeason(rs.getString("season"));
        return r;
    }
}