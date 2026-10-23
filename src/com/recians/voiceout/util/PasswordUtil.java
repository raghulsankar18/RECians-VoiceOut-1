package com.recians.voiceout.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * PasswordUtil - Secure password hashing utility for RECian's VoiceOut.
 * 
 * Uses SHA-256 cryptographic hashing to ensure that plain-text passwords
 * are never stored in the database or exposed in logs.
 */
public class PasswordUtil {

    // College project salt to prevent rainbow table attacks
    private static final String SALT = "REC_VOICEOUT_2026_SALT";

    /**
     * Hashes a plain-text password using SHA-256 with salt.
     * 
     * @param plainPassword User's plain password
     * @return Hexadecimal SHA-256 hash string
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String saltedInput = SALT + plainPassword;
            byte[] hashBytes = md.digest(saltedInput.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available in current JVM", e);
        }
    }

    /**
     * Verifies whether an entered plain-text password matches a stored hash.
     * 
     * @param plainPassword The password entered by the user
     * @param storedHash The hash stored in MySQL
     * @return true if password matches, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) {
            return false;
        }
        String calculatedHash = hashPassword(plainPassword);
        return calculatedHash.equalsIgnoreCase(storedHash);
    }
}
