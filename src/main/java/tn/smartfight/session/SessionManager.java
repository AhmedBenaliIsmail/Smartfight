package tn.smartfight.session;

import tn.smartfight.models.User;

public class SessionManager {

    private static User currentUser;
    private static String roleName;

    private SessionManager() {}

    // ── Login helpers ────────────────────────────────────────────────────────

    public static void loginAs(User user, String role) {
        currentUser = user;
        roleName = role.toUpperCase();
    }

    public static void loginAsAdmin(User user) {
        loginAs(user, "ADMIN");
    }

    public static void loginAsFan(User user) {
        loginAs(user, "FAN");
    }

    public static void loginAsOrganizer(User user) {
        loginAs(user, "ORGANIZER");
    }

    public static void loginAsFighter(User user) {
        loginAs(user, "FIGHTER");
    }

    public static void loginAsCoach(User user) {
        loginAs(user, "COACH");
    }

    public static void logout() {
        currentUser = null;
        roleName = null;
    }

    // ── Role checks ──────────────────────────────────────────────────────────

    public static boolean isAdmin() {
        return "ADMIN".equals(roleName);
    }

    public static boolean isFan() {
        return "FAN".equals(roleName);
    }

    public static boolean isOrganizer() {
        return "ORGANIZER".equals(roleName);
    }

    public static boolean isFighter() {
        return "FIGHTER".equals(roleName);
    }

    public static boolean isCoach() {
        return "COACH".equals(roleName);
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public static User getCurrentUser() { return currentUser; }
    public static String getRoleName()  { return roleName; }

    public static int getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : -1;
    }

    public static String getCurrentUserName() {
        return currentUser != null ? currentUser.getFullName() : "";
    }
}
