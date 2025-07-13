package Cataphract.API.Dragon;

import java.io.Console;

import Cataphract.API.Config;

/**
 * Utility class for reading authentication inputs securely.
 */
public final class AuthInputHelper 
{
    private AuthInputHelper() {} // Prevent instantiation

    /**
     * Reads username, password, and security key from the console.
     * @param console The console instance.
     * @return An array containing [username, hashedPassword, hashedSecurityKey], or null if username is invalid.
     * @throws Exception If there is an error during handling user credentials input
     */
    public static String[] readCredentials(Console console) throws Exception {
        String username = console.readLine("> Username: ");
        if (username == null || username.trim().isEmpty()) {
            return null; // Signal invalid input
        }
        username = Config.cryptography.stringToSHA3_256(username);
        char[] passwordChars = console.readPassword("Password: ");
        String password = passwordChars != null ? Config.cryptography.stringToSHA3_256(String.valueOf(passwordChars)) : "";
        char[] securityKeyChars = console.readPassword("Security Key (press ENTER to skip): ");
        String securityKey = securityKeyChars != null && securityKeyChars.length > 0 
            ? Config.cryptography.stringToSHA3_256(String.valueOf(securityKeyChars)) 
            : "";
        return new String[] { username, password, securityKey };
    }
}