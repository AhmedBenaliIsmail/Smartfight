package service;

import dao.UserDao;
import dao.RoleDao;
import model.User;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * SERVICE LAYER: Handles business logic for User management, 
 * Authentication, and Role assignment.
 */
public class Userservice {
    private final UserDao userDao;
    private final RoleDao roleDao;

    public Userservice() {
        this.userDao = new UserDao();
        this.roleDao = new RoleDao();
    }

    /**
     * Authenticates a user.
     * Important: This version fetches the user AND their roles so 
     * that isAdmin() checks work immediately after login.
     */
    public Optional<User> authenticateUser(String username, String password) {
        return userDao.findByUsername(username)
            .filter(user -> password.equals(user.getPassword()));
    }

    /**
     * Checks if a user has the ADMIN role by ID.
     */
    public boolean isAdmin(int userId) {
        return userDao.findById(userId)
            .map(user -> user.hasRole("ADMIN"))
            .orElse(false);
    }

    /**
     * Registers a new user with a specific role.
     * Roles are mapped to IDs: ADMIN (1), USER (2).
     */
    public User registerUser(User user, String roleName) {
        if (userDao.usernameExists(user.getUsername())) {
            throw new IllegalArgumentException("Username '" + user.getUsername() + "' is already taken.");
        }
        
        // 1. Save user to 'users' table
        User created = userDao.createUser(user);
        if (created == null) throw new RuntimeException("Database error: Could not create user.");

        // 2. Assign the role in 'user_roles' table
        assignRole(created.getUserId(), roleName);
        
        return created;
    }

    /**
     * Backward-compatible overload used by older UI code.
     * Defaults to USER/FIGHTER role mapping.
     */
    public User registerUser(User user) {
        return registerUser(user, "USER");
    }

    /**
     * Assigns a role to a user.
     * ADMIN maps to 1, USER maps to 2 based on your SQL schema.
     */
    public boolean assignRole(Integer userId, String roleName) {
        Integer roleId = getRoleIdByName(roleName);
        if (roleId == null) {
            throw new IllegalArgumentException("Role '" + roleName + "' does not exist in system.");
        }
        return userDao.assignRoleToUser(userId, roleId);
    }

    /**
     * Bootstraps the system with a default admin if the database is empty.
     */
    public void createDefaultAdminIfNotExists() {
        Optional<User> adminCheck = userDao.findByUsername("admin");
        
        if (adminCheck.isEmpty()) {
            try {
                User defaultAdmin = new User("admin", "admin", "admin@mma.com");
                registerUser(defaultAdmin, "ADMIN");
                System.out.println("--> [System] Default admin created (admin/admin)");
            } catch (Exception e) {
                System.err.println("--> [Error] Could not create default admin: " + e.getMessage());
            }
        }
    }

    // --- STANDARD CRUD OPERATIONS ---

    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    public Optional<User> getUserById(Integer id) {
        return userDao.findById(id);
    }

    public boolean updateUser(User user) {
        // Validation: Don't allow changing to a username that someone else has
        userDao.findByUsername(user.getUsername()).ifPresent(existing -> {
            if (existing.getUserId() != user.getUserId()) {
                throw new IllegalArgumentException("Username already in use.");
            }
        });
        return userDao.updateUser(user);
    }

    public boolean deleteUser(Integer userId) {
        return userDao.deleteUser(userId);
    }

    /**
     * Changes a user's password.
     * If oldPassword is null, it performs an admin reset.
     */
    public boolean changePassword(Integer userId, String oldPassword, String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("New password cannot be empty.");
        }

        User user = userDao.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found."));

        // If old password is provided, enforce validation (self-service flow).
        if (oldPassword != null && !oldPassword.equals(user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect.");
        }

        user.setPassword(newPassword);
        return userDao.updateUser(user);
    }

    /**
     * Internal mapping for Role IDs to match your 'roles' table.
     * 1 = ADMIN, 2 = FIGHTER (often used as default USER), 3 = COACH, 4 = FAN
     */
    private Integer getRoleIdByName(String roleName) {
        switch (roleName.toUpperCase()) {
            case "ADMIN":   return 1;
            case "FIGHTER": 
            case "USER":    return 2; 
            case "COACH":   return 3;
            case "FAN":     return 4;
            default:        return null;
        }
    }
}