package tn.smartfight.models;

public class BlogArticle {

    private int id;
    private int categoryId;
    private String categoryName;
    private int authorId;
    private String authorName;
    private String title;
    private String content;
    private String summary;
    private String imagePath;
    private String videoPath;
    private String status;
    private int viewCount;
    private String createdAt;
    private String updatedAt;

    public BlogArticle() {}

    // --- Helpers ---

    public boolean isPublished() {
        return "PUBLISHED".equals(status);
    }

    public boolean isDraft() {
        return "DRAFT".equals(status);
    }

    public boolean hasImage() {
        return imagePath != null && !imagePath.isEmpty();
    }

    public boolean hasVideo() {
        return videoPath != null && !videoPath.isEmpty();
    }

    @Override
    public String toString() {
        return title;
    }

    // --- Getters & Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public int getAuthorId() { return authorId; }
    public void setAuthorId(int authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getVideoPath() { return videoPath; }
    public void setVideoPath(String videoPath) { this.videoPath = videoPath; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getViewCount() { return viewCount; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
