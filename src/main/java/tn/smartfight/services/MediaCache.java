package tn.smartfight.services;

import tn.smartfight.models.BlogArticle;

import java.io.*;
import java.nio.file.*;

/**
 * File-path-only media store. Media files live in the media/ folder;
 * the database stores only the filename (e.g. "img_5.jpg").
 *
 * Both the Java app and the Symfony web app resolve filenames against
 * their own configured media root directory.
 */
public class MediaCache {

    public static final Path MEDIA_DIR =
            Paths.get(System.getProperty("user.dir"), "media");

    public static void init() {
        try {
            Files.createDirectories(MEDIA_DIR);
        } catch (IOException e) {
            System.err.println("[MediaCache] Could not create media dir: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // SAVE — returns the filename stored in the DB
    // ------------------------------------------------------------------

    /** Copy image file to media/ and return the filename for DB storage. */
    public static String saveImage(File source, int articleId) throws IOException {
        String filename = "img_" + articleId + getExt(source.getName());
        Files.copy(source.toPath(), MEDIA_DIR.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        return filename;
    }

    /** Copy video file to media/ and return the filename for DB storage. */
    public static String saveVideo(File source, int articleId) throws IOException {
        String filename = "vid_" + articleId + getExt(source.getName());
        Files.copy(source.toPath(), MEDIA_DIR.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        return filename;
    }

    // ------------------------------------------------------------------
    // READ — images
    // ------------------------------------------------------------------

    /**
     * Returns an InputStream for the article's image, or null if not available.
     */
    public static InputStream getImageStream(BlogArticle article) {
        if (!article.hasImage()) return null;
        Path local = MEDIA_DIR.resolve(article.getImagePath());
        if (Files.exists(local)) {
            try { return Files.newInputStream(local); }
            catch (IOException ignored) {}
        }
        return null;
    }

    // ------------------------------------------------------------------
    // READ — videos
    // ------------------------------------------------------------------

    /**
     * Returns a file:// URI for the given video filename, or null if missing.
     */
    public static String getVideoUri(String videoPath) {
        if (videoPath == null || videoPath.isEmpty()) return null;
        Path local = MEDIA_DIR.resolve(videoPath);
        if (Files.exists(local)) {
            return local.toUri().toString();
        }
        return null;
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /** Find first file in media/ matching prefix (any extension). */
    public static Path findLocalFile(String prefix) {
        try (DirectoryStream<Path> stream =
                Files.newDirectoryStream(MEDIA_DIR, prefix + ".*")) {
            for (Path p : stream) return p;
        } catch (IOException ignored) {}
        return null;
    }

    private static String getExt(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : ".bin";
    }
}
