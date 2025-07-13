package Cataphract.API.Dragon;

import java.io.File;

import Cataphract.API.Config;

/**
 * A class to handle user login and authentication.
 *
 * @author DAK404 (https://github.com/DAK404)
 * @version 4.0.0 (12-July-2025, Cataphract)
 * @since 0.0.1 (Zen Quantum 0.0.1)
 */
public final class Login {
    private final String username;

    /**
     * Constructor for Login class.
     *
     * @param username The username to be used for login.
     * @throws Exception If an error occurs during initialization.
     */
    public Login(String username) throws Exception {
        this.username = username == null || username.isEmpty() ? "DEFAULT USER" : username;
    }

    /**
     * Authenticates user login.
     *
     * @param psw The password (hashed).
     * @param key The security key (hashed or empty).
     * @return true if authentication succeeds, false otherwise.
     * @throws Exception If an error occurs.
     */
    public boolean authenticationLogic(String psw, String key) throws Exception {
        String storedPassword = DatabaseManager.retrieveSingleValue("SELECT Password FROM MUD WHERE Username = ?", "Password", username);
        String storedSecurityKey = DatabaseManager.retrieveSingleValue("SELECT SecurityKey FROM MUD WHERE Username = ?", "SecurityKey", username);
        return storedPassword.equals(psw) && (key.isEmpty() ? storedSecurityKey.isEmpty() : storedSecurityKey.equals(key));
    }

    /**
     * Checks user privileges.
     *
     * @return true if the user has admin privileges, false otherwise.
     * @throws Exception If an error occurs.
     */
    public boolean checkPrivilegeLogic() throws Exception {
        return DatabaseManager.retrieveSingleValue("SELECT Privileges FROM MUD WHERE Username = ?", "Privileges", username).equals("Yes");
    }

    /**
     * Retrieves the user's name.
     *
     * @return The user's name.
     * @throws Exception If an error occurs.
     */
    public String getNameLogic() throws Exception {
        return DatabaseManager.retrieveSingleValue("SELECT Name FROM MUD WHERE Username = ?", "Name", username);
    }

    /**
     * Retrieves the user's PIN (hashed).
     *
     * @return The user's PIN.
     * @throws Exception If an error occurs.
     */
    public String getPINLogic() throws Exception {
        return DatabaseManager.retrieveSingleValue("SELECT PIN FROM MUD WHERE Username = ?", "PIN", username);
    }

    /**
     * Checks if the user account exists.
     *
     * @return true if the user exists, false otherwise.
     * @throws Exception If an error occurs.
     */
    public boolean checkUserExistence() throws Exception {
        String result = DatabaseManager.retrieveSingleValue("SELECT count(*) > 0 FROM MUD WHERE Username = ?", "1", username);
        return Boolean.parseBoolean(result);
    }

    /**
     * Lists all user accounts (admin-only).
     *
     * @throws Exception If an error occurs.
     */
    public void listAllUserAccounts() throws Exception {
        if (!checkPrivilegeLogic()) {
            Config.io.printError("Insufficient Privileges.");
            return;
        }

        String format = "%1$-64s| %2$-32s| %3$-5s\n";
        String c = "-";
        Config.io.println("");
        String header = String.format(format, "Username", "Account Name", "Privileges");
        Config.io.println(header + c.repeat(header.length()) + "\n");

        File[] fileList = new File(Config.USER_HOME).listFiles();
        if (fileList != null) {
            for (File userDir : fileList) {
                String user = userDir.getName();
                Config.io.println(String.format(format, user, new Login(user).getNameLogic(), new Login(user).checkPrivilegeLogic() ? "Yes" : "No"));
            }
        }
        Config.io.println("");
    }
}