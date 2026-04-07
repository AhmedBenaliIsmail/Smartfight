package tn.smartfight.services;

import tn.smartfight.database.DBConnection;

import java.io.IOException;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

public class MediaSeeder {

    private static final List<String> IMAGE_FILES = Arrays.asList(
        "09dfab70-c534-11f0-9839-6584ec7afe70.jpg",
        "63b4559eeed82c1956d19ac3_Riveros-Hit-Rivero Vs Biacho-Bilbao Arena Spain December 3 2021 (optimized) SILVER.jpeg",
        "Sanneh-UFC-10-30-23.webp",
        "Sanneh-Boxing-Virtuosos.webp",
        "062825-Ilia-Topuria-Knockout-GettyImages-2222692229.avif",
        "img_1774945665692.png"
    );

    private static final List<String> VIDEO_FILES = Arrays.asList(
        "videoplayback.mp4",
        "videoplayback (1).mp4",
        "videoplayback (2).mp4"
    );

    public static void seed() {
        Connection conn = DBConnection.getInstance().getConnection();
        Path mediaDir = MediaCache.MEDIA_DIR;

        try {
            // --- Images ---
            List<Integer> noImage = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id FROM blog_article WHERE image_path IS NULL ORDER BY id")) {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) noImage.add(rs.getInt("id"));
            }
            int imgSeeded = 0;
            for (int i = 0; i < noImage.size(); i++) {
                String srcFile = IMAGE_FILES.get(i % IMAGE_FILES.size());
                Path src = mediaDir.resolve(srcFile);
                if (!Files.exists(src)) continue;

                int articleId = noImage.get(i);
                String ext = srcFile.substring(srcFile.lastIndexOf('.'));
                String filename = "img_" + articleId + ext;
                Path dest = mediaDir.resolve(filename);
                if (Files.notExists(dest)) {
                    try { Files.copy(src, dest); }
                    catch (IOException e) { System.err.println("[MediaSeeder] " + e.getMessage()); continue; }
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE blog_article SET image_path=? WHERE id=?")) {
                    ps.setString(1, filename);
                    ps.setInt(2, articleId);
                    ps.executeUpdate();
                }
                imgSeeded++;
            }

            // --- Videos ---
            List<Integer> noVideo = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id FROM blog_article WHERE video_path IS NULL ORDER BY id")) {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) noVideo.add(rs.getInt("id"));
            }
            int videoSeeded = 0;
            int videoCount = Math.min(noVideo.size(), VIDEO_FILES.size());
            for (int i = 0; i < videoCount; i++) {
                Path src = mediaDir.resolve(VIDEO_FILES.get(i));
                if (!Files.exists(src)) continue;

                int articleId = noVideo.get(i);
                String filename = "vid_" + articleId + ".mp4";
                Path dest = mediaDir.resolve(filename);
                if (Files.notExists(dest)) {
                    try { Files.copy(src, dest); }
                    catch (IOException e) { System.err.println("[MediaSeeder] " + e.getMessage()); continue; }
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE blog_article SET video_path=? WHERE id=?")) {
                    ps.setString(1, filename);
                    ps.setInt(2, articleId);
                    ps.executeUpdate();
                }
                videoSeeded++;
            }

            System.out.println("[MediaSeeder] Seeded images=" + imgSeeded + ", videos=" + videoSeeded);
        } catch (Exception e) {
            System.err.println("[MediaSeeder] " + e.getMessage());
        }
    }
}
