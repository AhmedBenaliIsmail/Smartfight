package tn.smartfight.auth;

import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.*;

class PasswordVerifierTest {

    @Test
    void bcryptCost12Verifies() {
        String hash = BCrypt.hashpw("secret", BCrypt.gensalt(12));
        assertTrue(PasswordVerifier.verify("secret", hash));
        assertFalse(PasswordVerifier.verify("wrong", hash));
    }

    @Test
    void bcryptCost13Verifies() {
        String hash = BCrypt.hashpw("mypassword", BCrypt.gensalt(13));
        assertTrue(PasswordVerifier.verify("mypassword", hash));
        assertFalse(PasswordVerifier.verify("notmypassword", hash));
    }

    @Test
    void bcryptPhpPrefixVerifies() {
        String hash2a = BCrypt.hashpw("phppass", BCrypt.gensalt(12));
        String hash2y = "$2y$" + hash2a.substring(4);
        assertTrue(PasswordVerifier.verify("phppass", hash2y));
        assertFalse(PasswordVerifier.verify("wrong", hash2y));
    }

    @Test
    void unknownPrefixReturnsFalse() {
        assertFalse(PasswordVerifier.verify("anything", "$unknownalgo$somegibberish"));
    }
}
