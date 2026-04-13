package com.ecommerce.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * PasswordUtils provides secure password hashing using BCrypt.
 * BCrypt is a salted hash function that protects against rainbow table and brute-force attacks.
 */
public class PasswordUtils {

    /**
     * Hashes a plain text password using BCrypt.
     * @param plainPassword the user's plain text password
     * @return the secure hashed password
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    /**
     * Verifies that a plain text password matches a previously hashed password.
     * @param plainPassword the plain text password to check
     * @param hashedPassword the stored hashed password
     * @return true if they match, false otherwise
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (hashedPassword == null || !hashedPassword.startsWith("$2a$")) {
            return false;
        }
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
