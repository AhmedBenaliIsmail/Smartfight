package tn.smartfight.auth;

import de.mkammerer.argon2.Argon2Factory;
import org.mindrot.jbcrypt.BCrypt;

public class PasswordVerifier {

    public static boolean verify(String rawPassword, String hash) {
        if (hash == null || hash.isBlank()) return false;
        try {
            if (hash.startsWith("$argon2")) {
                return Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id)
                        .verify(hash, rawPassword.toCharArray());
            }
            // Symfony stores bcrypt as $2y$; jBCrypt needs $2a$
            String jbcryptHash = hash.startsWith("$2y$") ? "$2a$" + hash.substring(4) : hash;
            if (jbcryptHash.startsWith("$2a$") || jbcryptHash.startsWith("$2b$")) {
                return BCrypt.checkpw(rawPassword, jbcryptHash);
            }
        } catch (Exception ignored) {}
        return false;
    }

    public static String hashBcrypt(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(13));
    }
}
