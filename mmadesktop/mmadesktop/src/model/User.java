package model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class User {
    private Integer userId;
    private String username;
    private String password; 
    private String email;
    private LocalDateTime createdDate;
    private Set<Role> roles = new HashSet<>();

    // Default constructor - essential for many DB frameworks
    public User() {
        this.createdDate = LocalDateTime.now();
    }

    // Constructor for registration
    public User(String username, String password, String email) {
        this(); // Calls the default constructor to set the createdDate
        this.username = username;
        this.password = password;
        this.email = email;
    }

    // --- GETTERS & SETTERS ---

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = (roles != null) ? roles : new HashSet<>(); }

    // --- UTILITY METHODS ---

    public void addRole(Role role) {
        if (role != null) {
            this.roles.add(role);
        }
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
    }

    /**
     * Checks if the user has a specific role.
     * Case-insensitive to prevent login bugs.
     */
    public boolean hasRole(String roleName) {
        if (roleName == null || roles == null) return false;
        return roles.stream()
                    .anyMatch(r -> r.getRoleName().equalsIgnoreCase(roleName));
    }

    /**
     * Returns a comma-separated string of roles (useful for Tables)
     */
    public String getRolesAsString() {
        if (roles.isEmpty()) return "No Roles";
        return roles.stream()
                    .map(Role::getRoleName)
                    .collect(Collectors.joining(", "));
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", roles=" + getRolesAsString() +
                '}';
    }
}