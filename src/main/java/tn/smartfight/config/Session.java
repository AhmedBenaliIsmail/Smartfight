package tn.smartfight.config;

import tn.smartfight.model.User;

public class Session {
    private static User current;

    public static void setUser(User user) { current = user; }
    public static User getUser() { return current; }
    public static void clear() { current = null; }
}
