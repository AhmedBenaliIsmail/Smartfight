package tn.smartfight.dao;

import tn.smartfight.config.DBConnection;
import tn.smartfight.model.BlogArticle;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BlogDao {
    private static final Logger LOG = Logger.getLogger(BlogDao.class.getName());

    private final DataSource dataSource;

    public BlogDao() { this(DBConnection.getDataSource()); }
    public BlogDao(DataSource dataSource) { this.dataSource = dataSource; }

    public List<BlogArticle> findAll() {
        String sql = "SELECT id, title, content, summary, status, view_count, created_at, updated_at, " +
                "image_path, video_path, category_id, author_id FROM blog_article " +
                "WHERE status='PUBLISHED' ORDER BY created_at DESC";
        List<BlogArticle> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(mapArticle(rs));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "BlogDao.findAll failed", e);
        }
        return list;
    }

    public BlogArticle findById(int id) {
        String sql = "SELECT id, title, content, summary, status, view_count, created_at, updated_at, " +
                "image_path, video_path, category_id, author_id FROM blog_article WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapArticle(rs);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "BlogDao.findById failed id=" + id, e);
        }
        return null;
    }

    public List<BlogArticle> findAllAdmin() {
        String sql = "SELECT id, title, content, summary, status, view_count, created_at, updated_at, " +
                "image_path, video_path, category_id, author_id FROM blog_article ORDER BY created_at DESC";
        List<BlogArticle> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(mapArticle(rs));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "BlogDao.findAllAdmin failed", e);
        }
        return list;
    }

    public void create(BlogArticle a) {
        String sql = "INSERT INTO blog_article (title, content, summary, status, view_count, " +
                "created_at, updated_at, image_path, video_path, category_id, author_id) " +
                "VALUES (?, ?, ?, ?, 0, NOW(), NOW(), ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, a.getTitle());
            stmt.setString(2, a.getContent());
            stmt.setString(3, a.getSummary());
            stmt.setString(4, a.getStatus() != null ? a.getStatus() : "DRAFT");
            stmt.setString(5, a.getImagePath());
            stmt.setString(6, a.getVideoPath());
            if (a.getCategoryId() > 0) stmt.setInt(7, a.getCategoryId());
            else stmt.setNull(7, java.sql.Types.INTEGER);
            if (a.getAuthorId() > 0) stmt.setInt(8, a.getAuthorId());
            else stmt.setNull(8, java.sql.Types.INTEGER);
            stmt.executeUpdate();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "BlogDao.create failed", e);
        }
    }

    public void update(BlogArticle a) {
        String sql = "UPDATE blog_article SET title=?, content=?, summary=?, status=?, " +
                "updated_at=NOW(), image_path=?, video_path=?, category_id=? WHERE id=?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, a.getTitle());
            stmt.setString(2, a.getContent());
            stmt.setString(3, a.getSummary());
            stmt.setString(4, a.getStatus() != null ? a.getStatus() : "DRAFT");
            stmt.setString(5, a.getImagePath());
            stmt.setString(6, a.getVideoPath());
            if (a.getCategoryId() > 0) stmt.setInt(7, a.getCategoryId());
            else stmt.setNull(7, java.sql.Types.INTEGER);
            stmt.setInt(8, a.getId());
            stmt.executeUpdate();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "BlogDao.update failed id=" + a.getId(), e);
        }
    }

    public Map<Integer, String> loadCategories() {
        Map<Integer, String> map = new LinkedHashMap<>();
        String sql = "SELECT id, name FROM blog_category ORDER BY name";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) map.put(rs.getInt("id"), rs.getString("name"));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "BlogDao.loadCategories failed", e);
        }
        return map;
    }

    public void deleteById(int id) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM blog_article WHERE id=?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "BlogDao.deleteById failed id=" + id, e);
        }
    }

    private BlogArticle mapArticle(ResultSet rs) throws SQLException {
        BlogArticle a = new BlogArticle();
        a.setId(rs.getInt("id"));
        a.setTitle(rs.getString("title"));
        a.setContent(rs.getString("content"));
        a.setSummary(rs.getString("summary"));
        a.setStatus(rs.getString("status"));
        a.setViewCount(rs.getInt("view_count"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) a.setCreatedAt(ca.toLocalDateTime());
        Timestamp ua = rs.getTimestamp("updated_at");
        if (ua != null) a.setUpdatedAt(ua.toLocalDateTime());
        a.setImagePath(rs.getString("image_path"));
        a.setVideoPath(rs.getString("video_path"));
        a.setCategoryId(rs.getInt("category_id"));
        a.setAuthorId(rs.getInt("author_id"));
        return a;
    }
}
