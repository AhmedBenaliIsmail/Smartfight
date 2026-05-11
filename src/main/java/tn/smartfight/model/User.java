package tn.smartfight.model;

import java.time.LocalDateTime;
import java.util.List;

public class User {
    private int userId;
    private String username;
    private String password;
    private String email;
    private LocalDateTime createdDate;
    private int predictionPoints;
    private boolean isVerified;
    private String verificationToken;
    private List<String> roles;

    public User() {}

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public int getPredictionPoints() { return predictionPoints; }
    public void setPredictionPoints(int predictionPoints) { this.predictionPoints = predictionPoints; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public String getVerificationToken() { return verificationToken; }
    public void setVerificationToken(String verificationToken) { this.verificationToken = verificationToken; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}
