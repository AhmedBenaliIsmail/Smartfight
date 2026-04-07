package tn.smartfight.database;

import tn.smartfight.services.MediaCache;

import java.io.IOException;
import java.nio.file.*;
import java.sql.*;

/**
 * Runs once on startup to migrate the blog_article table.
 *
 * Phase A (legacy → BLOB): previously migrated image_url/video_url to BLOB columns.
 * Phase B (BLOB → path):   extracts BLOB data to media/ files, adds image_path/video_path
 *                           VARCHAR columns, populates them, then drops the BLOB columns.
 */
public class DBMigration {

    public static void run() {
        Connection conn = DBConnection.getInstance().getConnection();
        try {
            DatabaseMetaData meta = conn.getMetaData();

            // --- Phase A: drop old URL columns if they still exist ---
            boolean hasImageUrl = columnExists(meta, "blog_article", "image_url");
            boolean hasVideoUrl = columnExists(meta, "blog_article", "video_url");
            try (Statement st = conn.createStatement()) {
                if (hasImageUrl) {
                    st.execute("ALTER TABLE blog_article DROP COLUMN image_url");
                    System.out.println("[DBMigration] Dropped image_url column");
                }
                if (hasVideoUrl) {
                    st.execute("ALTER TABLE blog_article DROP COLUMN video_url");
                    System.out.println("[DBMigration] Dropped video_url column");
                }
            }

            // --- Phase B: add path columns ---
            boolean hasImagePath = columnExists(meta, "blog_article", "image_path");
            boolean hasVideoPath = columnExists(meta, "blog_article", "video_path");
            try (Statement st = conn.createStatement()) {
                if (!hasImagePath) {
                    st.execute("ALTER TABLE blog_article ADD COLUMN image_path VARCHAR(255) DEFAULT NULL");
                    System.out.println("[DBMigration] Added image_path column");
                }
                if (!hasVideoPath) {
                    st.execute("ALTER TABLE blog_article ADD COLUMN video_path VARCHAR(255) DEFAULT NULL");
                    System.out.println("[DBMigration] Added video_path column");
                }
            }

            // --- Phase B: extract BLOB data to files and populate path columns ---
            boolean hasImageData = columnExists(meta, "blog_article", "image_data");
            boolean hasVideoData = columnExists(meta, "blog_article", "video_data");

            if (hasImageData) {
                extractImages(conn);
            }
            if (hasVideoData) {
                extractVideos(conn);
            }

            // --- Phase B: drop BLOB columns ---
            // Re-fetch metadata after potential ALTERs above
            meta = conn.getMetaData();
            hasImageData = columnExists(meta, "blog_article", "image_data");
            hasVideoData = columnExists(meta, "blog_article", "video_data");
            try (Statement st = conn.createStatement()) {
                if (hasImageData) {
                    st.execute("ALTER TABLE blog_article DROP COLUMN image_data");
                    System.out.println("[DBMigration] Dropped image_data BLOB column");
                }
                if (hasVideoData) {
                    st.execute("ALTER TABLE blog_article DROP COLUMN video_data");
                    System.out.println("[DBMigration] Dropped video_data BLOB column");
                }
            }

        } catch (SQLException e) {
            System.err.println("[DBMigration] " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Extract image BLOBs to media/ and update image_path in DB
    // ------------------------------------------------------------------
    private static void extractImages(Connection conn) throws SQLException {
        String sel = "SELECT id, image_data FROM blog_article WHERE image_data IS NOT NULL";
        String upd = "UPDATE blog_article SET image_path = ? WHERE id = ? AND image_path IS NULL";
        try (PreparedStatement selPs = conn.prepareStatement(sel);
             ResultSet rs = selPs.executeQuery();
             PreparedStatement updPs = conn.prepareStatement(upd)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                byte[] bytes = rs.getBytes("image_data");
                if (bytes == null || bytes.length == 0) continue;

                // Check if a local file already exists from the old MediaCache
                Path existing = MediaCache.findLocalFile("img_" + id);
                String filename;
                if (existing != null) {
                    filename = existing.getFileName().toString();
                } else {
                    // Default to .jpg — browsers and JavaFX detect format from content
                    filename = "img_" + id + ".jpg";
                    Path dest = MediaCache.MEDIA_DIR.resolve(filename);
                    try {
                        Files.write(dest, bytes);
                    } catch (IOException e) {
                        System.err.println("[DBMigration] Could not write image for article " + id + ": " + e.getMessage());
                        continue;
                    }
                }
                updPs.setString(1, filename);
                updPs.setInt(2, id);
                updPs.executeUpdate();
                System.out.println("[DBMigration] Migrated image for article " + id + " -> " + filename);
            }
        }
    }

    // ------------------------------------------------------------------
    // Extract video BLOBs to media/ and update video_path in DB
    // ------------------------------------------------------------------
    private static void extractVideos(Connection conn) throws SQLException {
        String sel = "SELECT id, video_data FROM blog_article WHERE video_data IS NOT NULL";
        String upd = "UPDATE blog_article SET video_path = ? WHERE id = ? AND video_path IS NULL";
        try (PreparedStatement selPs = conn.prepareStatement(sel);
             ResultSet rs = selPs.executeQuery();
             PreparedStatement updPs = conn.prepareStatement(upd)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                byte[] bytes = rs.getBytes("video_data");
                if (bytes == null || bytes.length == 0) continue;

                Path existing = MediaCache.findLocalFile("vid_" + id);
                String filename;
                if (existing != null) {
                    filename = existing.getFileName().toString();
                } else {
                    filename = "vid_" + id + ".mp4";
                    Path dest = MediaCache.MEDIA_DIR.resolve(filename);
                    try {
                        Files.write(dest, bytes);
                    } catch (IOException e) {
                        System.err.println("[DBMigration] Could not write video for article " + id + ": " + e.getMessage());
                        continue;
                    }
                }
                updPs.setString(1, filename);
                updPs.setInt(2, id);
                updPs.executeUpdate();
                System.out.println("[DBMigration] Migrated video for article " + id + " -> " + filename);
            }
        }
    }

    private static boolean columnExists(DatabaseMetaData meta, String table, String column) throws SQLException {
        try (ResultSet rs = meta.getColumns(null, null, table, column)) {
            return rs.next();
        }
    }
}
