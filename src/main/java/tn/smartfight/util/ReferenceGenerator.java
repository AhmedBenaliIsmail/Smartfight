package tn.smartfight.util;

import java.security.SecureRandom;

public class ReferenceGenerator {
    private static final SecureRandom RNG = new SecureRandom();

    public static String generate() {
        return String.format("SF-%08X", RNG.nextInt() & 0x7FFFFFFF);
    }
}
