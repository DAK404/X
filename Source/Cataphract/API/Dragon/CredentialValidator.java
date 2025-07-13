package Cataphract.API.Dragon;

import java.util.function.Predicate;

import Cataphract.API.Config;

/**
 * Utility class for credential validation.
 *
 * @author DAK404 (https://github.com/DAK404)
 * @version 4.0.0 (12-July-2025, Cataphract)
 */
public final class CredentialValidator 
{
    public static final String NAME_POLICY = """
        Account Name Policy Information
        -------------------------------
        * Name cannot be 'Administrator'
        * Name must contain English Alphabet, can have numbers
        * Name must have at least 2 characters
        * Name cannot contain spaces
        -------------------------------
        """;

    public static final String USERNAME_POLICY = """
        Account Username Policy Information
        -----------------------------------
        * Username cannot contain the word 'Administrator'
        * Username can contain numbers, special characters and symbols.
        -----------------------------------
        """;

    public static final String PASSWORD_POLICY = """
        Account Password Policy Information
        -----------------------------------
        * Password must contain at least 8 characters
        * Password is recommended to have special characters and numbers
        -----------------------------------
        """;

    public static final String SECURITY_KEY_POLICY = """
        Account Security Key Policy Information
        -----------------------------------
        * Security Key must contain at least 8 characters
        * Security Key is recommended to have special characters and numbers
        -----------------------------------
        """;

    public static final String PIN_POLICY = """
        Account PIN Policy Information
        -------------------------------
        * PIN must contain at least 4 characters
        * PIN is recommended to have special characters and numbers
        -------------------------------
        """;

    /**
     * Private constructor (To prevent invocation by subclass constructors)
     */
    private CredentialValidator() {}

    /**
     * Validates an account name.
     *
     * @param name The account name.
     * @return true if valid, false otherwise.
     */
    public static boolean validateName(String name) {
        return name != null && !name.contains(" ") && !name.isEmpty() && name.matches("^[a-zA-Z0-9]*$") &&
               !name.equalsIgnoreCase("Administrator") && name.length() >= 2;
    }

    /**
     * Validates a username, checking for uniqueness.
     *
     * @param username The username.
     * @return true if valid and unique, false otherwise.
     */
    public static boolean validateUsername(String username)
    {
        try
        {
            if (username == null || username.isEmpty() || username.equalsIgnoreCase("Administrator")) {
                return false;
            }
            String hashedUsername = Config.cryptography.stringToSHA3_256(username);
            if (new Login(hashedUsername).checkUserExistence()) 
            {
                Config.io.printError("Username has already been enrolled! Please try again with another username.");
                return false;
            }
            return true;
        }
        catch(Exception e)
        {
            Config.io.printError("Error with Username.");
        }
        return false;
    }

    /**
     * Validates a password.
     *
     * @param password The password.
     * @return true if valid, false otherwise.
     */
    public static boolean validatePassword(String password) {
        return password != null && !password.isEmpty() && password.length() >= 8;
    }

    /**
     * Validates a security key, allowing empty or null inputs.
     *
     * @param key The security key.
     * @return true if valid (empty, null, or >= 8 characters), false otherwise.
     */
    public static boolean validateSecurityKey(String key) {
        return key == null || key.isEmpty() || key.length() >= 8;
    }

    /**
     * Validates a PIN.
     *
     * @param pin The PIN.
     * @return true if valid, false otherwise.
     */
    public static boolean validatePin(String pin) {
        return pin != null && !pin.isEmpty() && pin.length() >= 4;
    }

    /**
     * Generic credential validation with confirmation.
     *
     * @param prompt The prompt to display.
     * @param policy The policy string.
     * @param validator The validation function.
     * @param console The console for input.
     * @param isPassword Whether the input is a password (requires confirmation).
     * @return The validated input, or null if invalid.
     */
    public static String validateCredential(String prompt, String policy, Predicate<String> validator, java.io.Console console, boolean isPassword) {
        String input = isPassword ? String.valueOf(console.readPassword(policy + prompt + "> ")) : console.readLine(policy + prompt + "> ");
        String confirm = isPassword ? String.valueOf(console.readPassword("Confirm " + prompt + "> ")) : input;

        if (input != null && input.equals(confirm) && validator.test(input)) {
            return input;
        }
        console.readLine("Invalid " + prompt + ". Press ENTER to try again.");
        return null;
    }
}