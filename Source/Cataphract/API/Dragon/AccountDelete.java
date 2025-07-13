/*
*                                                      |
*                                                     ||
*  |||||| ||||||||| |||||||| ||||||||| |||||||  |||  ||| ||||||| |||||||||  |||||| |||||||||
* |||            ||    |||          ||       || |||  |||       ||       || |||        |||
* |||      ||||||||    |||    ||||||||  ||||||  ||||||||  ||||||  |||||||| |||        |||
* |||      |||  |||    |||    |||  |||  |||     |||  |||  ||  ||  |||  ||| |||        |||
*  ||||||  |||  |||    |||    |||  |||  |||     |||  |||  ||   || |||  |||  ||||||    |||
*                                               ||
*                                               |
*
* A Cross Platform OS Shell
* Powered By Truncheon Core
*/

/*
 * This file is part of the Cataphract project.
 * Copyright (C) 2024 DAK404 (https://github.com/DAK404)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package Cataphract.API.Dragon;

import java.io.File;

import Cataphract.API.Config;

/**
 * A class to delete user accounts on the system. Can be restricted by policy "account_delete".
 *
 * @author DAK404 (https://github.com/DAK404)
 * @version 4.0.0 (12-July-2025, Cataphract)
 * @since 0.0.1 (Mosaic 0.0.1)
 */
public final class AccountDelete implements AccountManager {
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
        Config.build.viewBuildInfo(false);
        if (!new Cataphract.API.Minotaur.PolicyCheck().retrievePolicyValue("account_delete").equals("on") && !isCurrentUserAdmin) {
            Config.io.printError("Policy Management System - Permission Denied.");
            return;
        }

        if (!authenticate(String.valueOf(Config.console.readPassword("Password: ")), String.valueOf(Config.console.readPassword("Security Key: ")))) {
            Config.io.printError("Invalid Login Credentials. Please Try Again.");
            return;
        }

        userManagementConsoleDelete();
    }

    @Override
    public boolean authenticate(String password, String securityKey) throws Exception {
        Config.io.println("Please login to continue.");
        Config.io.println("Username: " + new Login(currentUsername).getNameLogic());
        String hashedPassword = Config.cryptography.stringToSHA3_256(password);
        String hashedSecurityKey = Config.cryptography.stringToSHA3_256(securityKey);
        return new Login(currentUsername).authenticationLogic(hashedPassword, hashedSecurityKey);
    }

    /**
     * Provides a Config.console for user account deletion.
     *
     * @throws Exception If an error occurs.
     */
    private void userManagementConsoleDelete() throws Exception {
        Config.io.println("-------------------------------------------------");
        Config.io.println("|   User Management Config.console: Account Deletion   |");
        Config.io.println("-------------------------------------------------\n");

        if (isCurrentUserAdmin) {
            Config.io.printWarning("ADMINISTRATOR MODE ACTIVE!");
            String[] command;
            do {
                command = Config.io.splitStringToArray(Config.console.readLine("AccMgmt-Del!> "));
                if (command.length == 0 || command[0].isEmpty()) {
                    continue;
                }
                switch (command[0].toLowerCase()) {
                    case "exit":
                        break;
                    case "del":
                    case "delete":
                        if (command.length < 2) {
                            Config.io.printError("Incorrect Syntax.");
                        } else {
                            String username = command.length > 2 && command[1].equalsIgnoreCase("force") ? command[2] : Config.cryptography.stringToSHA3_256(command[1]);
                            boolean success = accountDeletionLogic(username);
                            Config.io.printInfo("Account Deletion: " + (success ? "Successful" : "Failed"));
                        }
                        break;
                    case "list":
                        new Login(currentUsername).listAllUserAccounts();
                        break;
                    default:
                        Config.io.printError("Command Not Found: " + command[0]);
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
        if (username.equals(Config.cryptography.stringToSHA3_256("Administrator"))) {
            Config.io.printError("Deletion of Administrator Account is not allowed!");
            return false;
        }

        if (!new Login(username).checkUserExistence()) {
            Config.io.println("User does not exist! Please enter the correct username (or the username hash) to continue");
            return false;
        }

        if (Config.console.readLine("Are you sure you wish to delete user account \"" + new Login(username).getNameLogic() + "\"? [ YES | NO ]\n> ").equalsIgnoreCase("yes")) {
            boolean dbSuccess = DatabaseManager.executeUpdate("DELETE FROM MUD WHERE Username = ?", username);
            boolean dirSuccess = FileManager.deleteDirectory(new File(Config.USER_HOME + username));
            boolean success = dbSuccess && dirSuccess;
            if (success) {
                Config.io.printAttention("Account Successfully Deleted.");
                if (!isCurrentUserAdmin) {
                    Thread.sleep(5000);
                    System.exit(211);
                }
            } else {
                Config.io.printError("System Error: Unable to delete account.");
            }
            return success;
        }
        return false;
    }
}