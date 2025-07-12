package Cataphract.API.Dragon;

import java.io.Console;
import java.io.File;

import Cataphract.API.Build;
import Cataphract.API.IOStreams;
import Cataphract.API.Minotaur.Cryptography;

/**
 * A class to delete user accounts on the system. Can be restricted by policy "account_delete".
 *
 * @author DAK404 (https://github.com/DAK404)
 * @version 4.0.0 (12-July-2025, Cataphract)
 * @since 0.0.1 (Mosaic 0.0.1)
 */
public final class AccountDelete implements AccountManager {
    private final Console console = System.console();
    private final String currentUsername;
    private final boolean isCurrentUserAdmin;

    /**
     * Constructor for AccountDelete class.
     *
     * @param currentUsername The current username.
     * @throws Exception If an error occurs during initialization.
     */
    public AccountDelete(String currentUsername) throws Exception {
        this.currentUsername = currentUsername == null || currentUsername.isEmpty() ? "DEFAULT" : currentUsername;
        this.isCurrentUserAdmin = new Login(currentUsername).checkPrivilegeLogic();
    }

    @Override
    public void execute() throws Exception {
        Build.viewBuildInfo();
        if (!new Cataphract.API.Minotaur.PolicyCheck().retrievePolicyValue("account_delete").equals("on") && !isCurrentUserAdmin) {
            IOStreams.printError("Policy Management System - Permission Denied.");
            return;
        }

        if (!authenticate(String.valueOf(console.readPassword("Password: ")), String.valueOf(console.readPassword("Security Key: ")))) {
            IOStreams.printError("Invalid Login Credentials. Please Try Again.");
            return;
        }

        userManagementConsoleDelete();
    }

    @Override
    public boolean authenticate(String password, String securityKey) throws Exception {
        IOStreams.println("Please login to continue.");
        IOStreams.println("Username: " + new Login(currentUsername).getNameLogic());
        String hashedPassword = Cryptography.stringToSHA3_256(password);
        String hashedSecurityKey = Cryptography.stringToSHA3_256(securityKey);
        return new Login(currentUsername).authenticationLogic(hashedPassword, hashedSecurityKey);
    }

    /**
     * Provides a console for user account deletion.
     *
     * @throws Exception If an error occurs.
     */
    private void userManagementConsoleDelete() throws Exception {
        IOStreams.println("-------------------------------------------------");
        IOStreams.println("|   User Management Console: Account Deletion   |");
        IOStreams.println("-------------------------------------------------\n");

        if (isCurrentUserAdmin) {
            IOStreams.printWarning("ADMINISTRATOR MODE ACTIVE!");
            String[] command;
            do {
                command = IOStreams.splitStringToArray(console.readLine("AccMgmt-Del!> "));
                if (command.length == 0 || command[0].isEmpty()) {
                    continue;
                }
                switch (command[0].toLowerCase()) {
                    case "exit":
                        break;
                    case "del":
                    case "delete":
                        if (command.length < 2) {
                            IOStreams.printError("Incorrect Syntax.");
                        } else {
                            String username = command.length > 2 && command[1].equalsIgnoreCase("force") ? command[2] : Cryptography.stringToSHA3_256(command[1]);
                            boolean success = accountDeletionLogic(username);
                            IOStreams.printInfo("Account Deletion: " + (success ? "Successful" : "Failed"));
                        }
                        break;
                    case "list":
                        new Login(currentUsername).listAllUserAccounts();
                        break;
                    default:
                        IOStreams.printError("Command Not Found: " + command[0]);
                        break;
                }
            } while (!command[0].equalsIgnoreCase("exit"));
        } else {
            accountDeletionLogic(currentUsername);
        }
    }

    /**
     * Logic for account deletion.
     *
     * @param username The username to delete (hashed or unhashed).
     * @return true if deletion succeeds, false otherwise.
     * @throws Exception If an error occurs.
     */
    private boolean accountDeletionLogic(String username) throws Exception {
        if (username.equals(Cryptography.stringToSHA3_256("Administrator"))) {
            IOStreams.printError("Deletion of Administrator Account is not allowed!");
            return false;
        }

        if (!new Login(username).checkUserExistence()) {
            IOStreams.println("User does not exist! Please enter the correct username (or the username hash) to continue");
            return false;
        }

        if (console.readLine("Are you sure you wish to delete user account \"" + new Login(username).getNameLogic() + "\"? [ YES | NO ]\n> ").equalsIgnoreCase("yes")) {
            boolean dbSuccess = DatabaseManager.executeUpdate("DELETE FROM MUD WHERE Username = ?", username);
            boolean dirSuccess = FileManager.deleteDirectory(new File(Config.USER_HOME + username));
            boolean success = dbSuccess && dirSuccess;
            if (success) {
                IOStreams.printAttention("Account Successfully Deleted.");
                if (!isCurrentUserAdmin) {
                    Thread.sleep(5000);
                    System.exit(211);
                }
            } else {
                IOStreams.printError("System Error: Unable to delete account.");
            }
            return success;
        }
        return false;
    }
}