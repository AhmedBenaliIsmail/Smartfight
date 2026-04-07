-- ============================================================
-- SmartFight: Migrate blog_article media from BLOBs to paths
-- Run this ONCE against your MySQL database.
-- The Java app's DBMigration.java handles this automatically
-- on startup, but you can also run it manually here.
-- ============================================================

USE smartfight;

-- Step 1: Add path columns (safe to run even if they already exist)
ALTER TABLE blog_article
    ADD COLUMN IF NOT EXISTS image_path VARCHAR(255) DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS video_path VARCHAR(255) DEFAULT NULL;

-- Step 2: Populate paths from existing filenames in the media/ folder.
-- The Java app (DBMigration.run()) does this automatically on startup
-- by scanning the media/ folder for img_{id}.* and vid_{id}.* files.
-- If you prefer to set paths manually, use statements like:
--   UPDATE blog_article SET image_path = CONCAT('img_', id, '.jpg') WHERE image_path IS NULL;
--   UPDATE blog_article SET video_path = CONCAT('vid_', id, '.mp4') WHERE video_path IS NULL;

-- Step 3: Drop the BLOB columns AFTER verifying the Java app has
-- populated image_path / video_path and media/ files are in place.
-- Uncomment these lines when ready:
--
-- ALTER TABLE blog_article DROP COLUMN image_data;
-- ALTER TABLE blog_article DROP COLUMN video_data;

-- Verify the result:
-- SELECT id, image_path, video_path FROM blog_article;
