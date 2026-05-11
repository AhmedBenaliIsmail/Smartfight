package tn.smartfight.util;

import java.security.SecureRandom;
import java.util.HexFormat;

public class TokenGenerator {
    private static final SecureRandom RNG = new SecureRandom();

    public static String generate() {
        byte[] bytes = new byte[32];
        RNG.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
