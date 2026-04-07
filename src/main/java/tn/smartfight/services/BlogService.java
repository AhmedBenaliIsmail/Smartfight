package tn.smartfight.services;

import tn.smartfight.database.DBConnection;
import tn.smartfight.models.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BlogService {

    private final Connection conn = DBConnection.getInstance().getConnection();

    // ---------------------------------------------------------------
    // Shared JOIN fragment — selects image_path and video_path.
    // No BLOB columns are fetched in list queries.
    // ---------------------------------------------------------------
    private static final String ARTICLE_BASE =
        "SELECT ba.id, ba.category_id, ba.author_id, ba.title, ba.content, ba.summary, " +
        "       ba.status, ba.view_count, ba.created_at, ba.updated_at, " +
        "       ba.image_path, ba.video_path, " +
        "       bc.name AS category_name, " +
        "       CONCAT(u.first_name,' ',u.last_name) AS author_name " +
        "FROM blog_article ba " +
        "JOIN blog_category bc ON ba.category_id = bc.id " +
        "JOIN user u ON ba.author_id = u.id ";

    // ---------------------------------------------------------------
    // getAllCategories
    // ---------------------------------------------------------------
    public List<BlogCategory> getAllCategories() {
        List<BlogCategory> list = new ArrayList<>();
        String sql = "SELECT id, name, description, slug FROM blog_category ORDER BY name";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                BlogCategory c = new BlogCategory();
                c.setId(rs.getInt("id"));
                c.setName(rs.getString("name"));
                c.setDescription(rs.getString("description"));
                c.setSlug(rs.getString("slug"));
                list.add(c);
            }
        } catch (SQLException e) {
            System.err.println("[BlogService] getAllCategories: " + e.getMessage());
        }
        return list;
    }

    // ---------------------------------------------------------------
    // getAllPublished
    // ---------------------------------------------------------------
    public List<BlogArticle> getAllPublished() {
        List<BlogArticle> list = new ArrayList<>();
        String sql = ARTICLE_BASE +
                     "WHERE ba.status = 'PUBLISHED' ORDER BY ba.created_at DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapArticle(rs));
            }
        } catch (SQLException e) {
            System.err.println("[BlogService] getAllPublished: " + e.getMessage());
        }
        return list;
    }

    // ---------------------------------------------------------------
    // getAllForAdmin
    // ---------------------------------------------------------------
    public List<BlogArticle> getAllForAdmin() {
        List<BlogArticle> list = new ArrayList<>();
        String sql = ARTICLE_BASE + "ORDER BY ba.created_at DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapArticle(rs));
            }
        } catch (SQLException e) {
            System.err.println("[BlogService] getAllForAdmin: " + e.getMessage());
        }
        return list;
    }

    // ---------------------------------------------------------------
    // getById
    // ---------------------------------------------------------------
    public BlogArticle getById(int id) {
        String sql = ARTICLE_BASE + "WHERE ba.id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapArticle(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[BlogService] getById: " + e.getMessage());
        }
        return null;
    }

    // ---------------------------------------------------------------
    // getByCategory
    // ---------------------------------------------------------------
    public List<BlogArticle> getByCategory(int categoryId) {
        List<BlogArticle> list = new ArrayList<>();
        String sql = ARTICLE_BASE +
                     "WHERE ba.status = 'PUBLISHED' AND ba.category_id = ? " +
                     "ORDER BY ba.created_at DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapArticle(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[BlogService] getByCategory: " + e.getMessage());
        }
        return list;
    }

    // ---------------------------------------------------------------
    // search
    // ---------------------------------------------------------------
    public List<BlogArticle> search(String keyword) {
        List<BlogArticle> list = new ArrayList<>();
        String sql = ARTICLE_BASE +
                     "WHERE ba.status = 'PUBLISHED' " +
                     "AND (ba.title LIKE ? OR ba.content LIKE ?) " +
                     "ORDER BY ba.created_at DESC";
        String pattern = "%" + keyword + "%";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapArticle(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[BlogService] search: " + e.getMessage());
        }
        return list;
    }

    // ---------------------------------------------------------------
    // create — returns the generated article ID
    // ---------------------------------------------------------------
    public int create(BlogArticle a) {
        String sql = "INSERT INTO blog_article " +
                     "(category_id, author_id, title, content, summary, image_path, video_path, status) " +
                     "VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getCategoryId());
            ps.setInt(2, a.getAuthorId());
            ps.setString(3, a.getTitle());
            ps.setString(4, a.getContent());
            ps.setString(5, a.getSummary());
            ps.setString(6, a.getImagePath());
            ps.setString(7, a.getVideoPath());
            ps.setString(8, a.getStatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[BlogService] create: " + e.getMessage());
        }
        return -1;
    }

    // ---------------------------------------------------------------
    // update — uses COALESCE so null paths preserve the existing value
    // ---------------------------------------------------------------
    public void update(BlogArticle a) {
        String sql = "UPDATE blog_article " +
                     "SET category_id=?, title=?, content=?, summary=?, " +
                     "image_path = COALESCE(?, image_path), " +
                     "video_path = COALESCE(?, video_path), " +
                     "status=?, updated_at=CURRENT_TIMESTAMP WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, a.getCategoryId());
            ps.setString(2, a.getTitle());
            ps.setString(3, a.getContent());
            ps.setString(4, a.getSummary());
            ps.setString(5, a.getImagePath());
            ps.setString(6, a.getVideoPath());
            ps.setString(7, a.getStatus());
            ps.setInt(8, a.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[BlogService] update: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // delete
    // ---------------------------------------------------------------
    public void delete(int id) {
        String sql = "DELETE FROM blog_article WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[BlogService] delete: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // setStatus
    // ---------------------------------------------------------------
    public void setStatus(int id, String status) {
        String sql = "UPDATE blog_article SET status=?, updated_at=CURRENT_TIMESTAMP WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[BlogService] setStatus: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // incrementViewCount
    // ---------------------------------------------------------------
    public void incrementViewCount(int id) {
        String sql = "UPDATE blog_article SET view_count = view_count+1 WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[BlogService] incrementViewCount: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // Private helper
    // ---------------------------------------------------------------
    private BlogArticle mapArticle(ResultSet rs) throws SQLException {
        BlogArticle a = new BlogArticle();
        a.setId(rs.getInt("id"));
        a.setCategoryId(rs.getInt("category_id"));
        a.setAuthorId(rs.getInt("author_id"));
        a.setTitle(rs.getString("title"));
        a.setContent(rs.getString("content"));
        a.setSummary(rs.getString("summary"));
        a.setImagePath(rs.getString("image_path"));
        a.setVideoPath(rs.getString("video_path"));
        a.setStatus(rs.getString("status"));
        a.setViewCount(rs.getInt("view_count"));
        a.setCategoryName(rs.getString("category_name"));
        a.setAuthorName(rs.getString("author_name"));
        java.sql.Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) a.setCreatedAt(createdAt.toString());
        java.sql.Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) a.setUpdatedAt(updatedAt.toString());
        return a;
    }
}
