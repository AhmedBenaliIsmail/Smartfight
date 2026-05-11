package tn.smartfight.util;

import tn.smartfight.config.Session;
import tn.smartfight.model.User;

public class AccessGuard {
    public static boolean isAdmin() {
        User u = Session.getUser();
        return u != null && u.getRoles() != null && u.getRoles().contains("ROLE_ADMIN");
    }
}
