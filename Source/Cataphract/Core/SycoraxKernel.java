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

package Cataphract.Core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import Cataphract.API.Config;
import Cataphract.API.Wraith.FileDownload;
import Cataphract.API.Wraith.FileManager;
import Cataphract.API.Wyvern.NionUpdate;
import Cataphract.API.Dragon.AccountCreate;
import Cataphract.API.Dragon.AccountDelete;
import Cataphract.API.Dragon.AccountModify;
import Cataphract.API.Dragon.AuthInputHelper;
import Cataphract.API.Dragon.Login;

/**
 * Main class for the Sycorax operating system kernel.
 * Coordinates authentication, session management, and command processing.
 *
 * @author DAK404 (https://github.com/DAK404)
 * @version 1.4.1 (13-July-2025, Cataphract)
 * @since 0.0.1 (Cataphract 0.0.1)
 */
public class SycoraxKernel {
    private final AuthenticationManager authManager;
    private final SessionManager sessionManager;
    private final CommandProcessor commandProcessor;
    protected static final String LOG_FILE_NAME = "ShellLog";

    public SycoraxKernel() {
        this.authManager = new AuthenticationManager();
        this.sessionManager = new SessionManager();
        this.commandProcessor = new CommandProcessor(sessionManager);
    }

    /**
     * Starts the Sycorax kernel, handles login, and launches user shell.
     */
    public void startSycoraxKernel() throws Exception {
        try {
            Config.build.viewBuildInfo(false);
            Config.fileWriter.log("Starting Sycorax kernel", LOG_FILE_NAME);
            while (!authManager.login()) {
                Config.io.printError("Incorrect Credentials! Please try again.");
                Config.fileWriter.log("Failed login attempt", LOG_FILE_NAME);
                authManager.handleFailedLoginAttempt();
            }
            Config.io.printInfo("Login Successful. Loading Sycorax Kernel...");
            Config.fileWriter.log("Login successful for user: " + authManager.getUsername(), LOG_FILE_NAME);
            sessionManager.fetchUserDetails(authManager.getUsername());
            commandProcessor.runUserShell();
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
            Config.fileWriter.log("Kernel startup failed: " + e.getMessage(), LOG_FILE_NAME);
            throw e;
        }
    }
}

/**
 * Manages user authentication and login attempts.
 */
class AuthenticationManager {
    private String username = "DEFAULT_USERNAME";
    private int loginAttemptsRemaining = 5;

    /**
     * Attempts to log in the user using credentials from AuthInputHelper.
     * @return true if login is successful, false otherwise.
     */
    public boolean login() throws Exception {
        try {
            Config.build.viewBuildInfo(false);
            Config.io.printInfo("Authentication Attempts Left: " + loginAttemptsRemaining);
            Config.fileWriter.log("Attempting login, attempts remaining: " + loginAttemptsRemaining, SycoraxKernel.LOG_FILE_NAME);
            String[] credentials = AuthInputHelper.readCredentials(Config.console);
            if (credentials == null || credentials[0] == null || credentials[0].trim().isEmpty()) {
                Config.io.printError("Username cannot be empty.");
                Config.fileWriter.log("Login failed: Empty username", SycoraxKernel.LOG_FILE_NAME);
                return false;
            }
            username = credentials[0];
            String password = credentials[1];
            String securityKey = credentials[2];
            boolean success = new Login(username).authenticationLogic(password, securityKey);
            if (success) {
                Config.fileWriter.log("Login successful for user: " + username, SycoraxKernel.LOG_FILE_NAME);
            }
            return success;
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
            Config.fileWriter.log("Login error: " + e.getMessage(), SycoraxKernel.LOG_FILE_NAME);
            throw e;
        }
    }

    /**
     * Handles failed login attempts, locking the system after 5 failures.
     */
    public void handleFailedLoginAttempt() throws Exception {
        try {
            loginAttemptsRemaining--;
            Config.fileWriter.log("Failed login attempt, remaining: " + loginAttemptsRemaining, SycoraxKernel.LOG_FILE_NAME);
            if (loginAttemptsRemaining <= 0) {
                Config.io.printError("Authentication Attempts Exceeded! Further attempts are locked!");
                Config.fileWriter.log("Authentication attempts exceeded, locking system", SycoraxKernel.LOG_FILE_NAME);
                Thread.sleep(36000);
                loginAttemptsRemaining = 5;
                Config.fileWriter.log("Reset login attempts to 5 after lock", SycoraxKernel.LOG_FILE_NAME);
            }
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
            Config.fileWriter.log("Error handling failed login: " + e.getMessage(), SycoraxKernel.LOG_FILE_NAME);
            throw e;
        }
    }

    /**
     * Verifies the user's PIN for console unlocking.
     * @param storedPIN The stored PIN to compare against.
     * @return true if the entered PIN matches, false otherwise.
     */
    public boolean challengePIN(String storedPIN) throws Exception {
        try {
            String enteredPIN = String.valueOf(Config.console.readPassword("> PIN : "));
            boolean success = Config.cryptography.stringToSHA3_256(enteredPIN).equals(storedPIN);
            Config.fileWriter.log("PIN challenge " + (success ? "successful" : "failed") + " for user: " + username, SycoraxKernel.LOG_FILE_NAME);
            return success;
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
            Config.fileWriter.log("PIN challenge error: " + e.getMessage(), SycoraxKernel.LOG_FILE_NAME);
            throw e;
        }
    }

    public String getUsername() {
        return username;
    }
}

/**
 * Manages user session state and details.
 */
class SessionManager {
    private String accountName = "DEFAULT_USER";
    private String username = "DEFAULT_USERNAME";
    private String userUnlockPIN = "";
    private String systemName = "DEFAULT_SYSNAME";
    private boolean isUserAdmin = false;
    private char prompt = '?';

    /**
     * Fetches user details after successful login.
     * @param username The authenticated username.
     */
    public void fetchUserDetails(String username) throws Exception {
        try {
            this.username = username;
            this.accountName = new Login(username).getNameLogic();
            this.isUserAdmin = new Login(username).checkPrivilegeLogic();
            this.userUnlockPIN = new Login(username).getPINLogic();
            this.systemName = Config.policyCheck.retrievePolicyValue("sysname");
            this.prompt = isUserAdmin ? '!' : '*';
            Config.fileWriter.log("Fetched user details for: " + username, SycoraxKernel.LOG_FILE_NAME);
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
            Config.fileWriter.log("Error fetching user details: " + e.getMessage(), SycoraxKernel.LOG_FILE_NAME);
            throw e;
        }
    }

    /**
     * Clears session state on logout.
     * @throws Exception 
     */
    public void clearSessionState() throws Exception {
        try {
            username = "DEFAULT_USERNAME";
            accountName = "DEFAULT_USER";
            userUnlockPIN = "";
            isUserAdmin = false;
            prompt = '?';
            System.gc();
            Config.fileWriter.log("Session state cleared", SycoraxKernel.LOG_FILE_NAME);
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
            Config.fileWriter.log("Error clearing session state: " + e.getMessage(), SycoraxKernel.LOG_FILE_NAME);
        }
    }

    public String getPrompt() {
        return accountName + "@" + systemName + prompt + "> ";
    }

    public boolean isUserAdmin() {
        return isUserAdmin;
    }

    public String getUserUnlockPIN() {
        return userUnlockPIN;
    }

    public String getUsername() {
        return username;
    }
}

/**
 * Processes user commands and scripts.
 */
class CommandProcessor {
    private final SessionManager sessionManager;
    private final Map<String, Command> commands;
    private boolean scriptMode = false;

    public CommandProcessor(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        this.commands = new HashMap<>();
        initializeCommands();
    }

    private void initializeCommands() {
        commands.put("refresh", new RefreshCommand(sessionManager));
        commands.put("lock", new LockCommand(sessionManager));
        commands.put("policymgmt", new PolicyManagementCommand());
        commands.put("grinch", new FileManagementCommand(sessionManager.getUsername()));
        commands.put("filemanagement", new FileManagementCommand(sessionManager.getUsername()));
        commands.put("files", new FileManagementCommand(sessionManager.getUsername()));
        commands.put("exit", new ExitCommand());
        commands.put("restart", new RestartCommand());
        commands.put("script", new ScriptCommand(this));
        commands.put("update", new UpdateCommand(sessionManager.getUsername()));
        commands.put("usermgmt", new UserManagementCommand(sessionManager.getUsername()));
    }

    /**
     * Runs the user shell, reading and processing commands.
     */
    public void runUserShell() throws Exception {
        try {
            Config.build.viewBuildInfo(false);
            Config.fileWriter.log("Starting user shell for: " + sessionManager.getUsername(), SycoraxKernel.LOG_FILE_NAME);
            String input;
            do {
                input = Config.console.readLine(sessionManager.getPrompt());
                processCommand(input);
            } while (!input.equalsIgnoreCase("logout"));
            sessionManager.clearSessionState();
            Config.fileWriter.log("User shell terminated", SycoraxKernel.LOG_FILE_NAME);
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
            Config.fileWriter.log("User shell error: " + e.getMessage(), SycoraxKernel.LOG_FILE_NAME);
            throw e;
        }
    }

    /**
     * Processes a single command or delegates to Anvil.
     */
    public void processCommand(String input) throws Exception {
        try {
            if (input == null || input.trim().isEmpty()) return;
            Config.fileWriter.log("Processing command: " + input, SycoraxKernel.LOG_FILE_NAME);
            String[] commandArray = Config.io.splitStringToArray(input);
            Command command = commands.get(commandArray[0].toLowerCase());
            if (command != null) {
                command.execute(commandArray);
                Config.fileWriter.log("Executed command: " + commandArray[0], SycoraxKernel.LOG_FILE_NAME);
            } else {
                Config.anvil.anvilInterpreter(commandArray);
                Config.fileWriter.log("Delegated to Anvil: " + input, SycoraxKernel.LOG_FILE_NAME);
            }
        } catch (Exception e) {
            Config.io.printError("Command execution failed: " + e.getMessage());
            Config.exceptionHandler.handleException(e);
            Config.fileWriter.log("Command execution failed: " + e.getMessage(), SycoraxKernel.LOG_FILE_NAME);
            throw e;
        }
    }

    /**
     * Executes a script file line by line.
     */
    public boolean executeScript(String scriptFileName) throws Exception {
        try {
            if (scriptFileName == null || scriptFileName.trim().isEmpty() || scriptFileName.startsWith(" ")) {
                Config.io.printError("The name of the script file cannot be blank.");
                Config.fileWriter.log("Script execution failed: Invalid script file name", SycoraxKernel.LOG_FILE_NAME);
                return false;
            }
            if (! Config.policyCheck.retrievePolicyValue("script").equals("on") && !sessionManager.isUserAdmin()) {
                Config.io.printError("Insufficient Privileges to run scripts! Please contact the Administrator.");
                Config.fileWriter.log("Script execution failed: Insufficient privileges", SycoraxKernel.LOG_FILE_NAME);
                return false;
            }
            String filePath = Config.io.convertFileSeparator(".|Users|Cataphract|" + sessionManager.getUsername() + "|" + scriptFileName);
            File scriptFile = new File(filePath);
            if (!scriptFile.exists() || scriptFile.isDirectory()) {
                Config.io.printAttention("The specified script file is invalid or has not been found.\nPlease check the script file name and try again.");
                Config.fileWriter.log("Script execution failed: File not found - " + filePath, SycoraxKernel.LOG_FILE_NAME);
                return false;
            }
            if (scriptMode) {
                Config.io.printError("Cannot execute script within another script.");
                Config.fileWriter.log("Script execution failed: Nested script execution", SycoraxKernel.LOG_FILE_NAME);
                return false;
            }
            scriptMode = true;
            try (BufferedReader br = new BufferedReader(new FileReader(scriptFile))) {
                String scriptLine;
                Config.fileWriter.log("Executing script: " + scriptFileName, SycoraxKernel.LOG_FILE_NAME);
                while ((scriptLine = br.readLine()) != null) {
                    if (scriptLine.startsWith("#") || scriptLine.trim().isEmpty()) continue;
                    if (scriptLine.equalsIgnoreCase("End Script")) break;
                    processCommand(scriptLine);
                }
                Config.fileWriter.log("Script execution completed: " + scriptFileName, SycoraxKernel.LOG_FILE_NAME);
                return true;
            } finally {
                scriptMode = false;
                System.gc();
            }
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
            Config.fileWriter.log("Script execution error: " + e.getMessage(), SycoraxKernel.LOG_FILE_NAME);
            throw e;
        }
    }
}

/**
 * Interface for commands to ensure extensibility.
 */
interface Command {
    void execute(String[] args) throws Exception;
}

/**
 * Command implementations.
 */
class RefreshCommand implements Command {
    private final SessionManager sessionManager;

    public RefreshCommand(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void execute(String[] args) throws Exception {
        try {
            sessionManager.fetchUserDetails(sessionManager.getUsername());
            Config.io.printInfo("User details refreshed.");
            Config.fileWriter.log("Refreshed user details for: " + sessionManager.getUsername(), SycoraxKernel.LOG_FILE_NAME);
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
            Config.fileWriter.log("Refresh command error: " + e.getMessage(), SycoraxKernel.LOG_FILE_NAME);
            throw e;
        }
    }
}

class LockCommand implements Command {
    private final SessionManager sessionManager;

    public LockCommand(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void execute(String[] args) throws Exception {
        try {
            AuthenticationManager authManager = new AuthenticationManager();
            String input;
            Config.build.clearScreen();
            Config.fileWriter.log("Locking console for user: " + sessionManager.getUsername(), SycoraxKernel.LOG_FILE_NAME);
            do {
                StringBuilder lockPromptBuilder = new StringBuilder()
                        .append((char)27).append("[33;49m")
                        .append(Config.time.getDateTimeUsingSpecifiedFormat("yyyy-MMM-dd HH:mm:ss"))
                        .append("  LOCKED\n")
                        .append(sessionManager.getPrompt())
                        .append((char)27).append("[0m");
                input = Config.console.readLine(lockPromptBuilder.toString());
            } while (!input.equalsIgnoreCase("unlock"));
            Config.io.printAttention("Please Enter Unlock PIN To Continue.");
            while (!authManager.challengePIN(sessionManager.getUserUnlockPIN())) {
                Config.io.printError("Incorrect PIN.");
                Config.fileWriter.log("Incorrect PIN entered", SycoraxKernel.LOG_FILE_NAME);
                authManager.handleFailedLoginAttempt();
            }
            Config.build.viewBuildInfo(false);
            Config.fileWriter.log("Console unlocked for user: " + sessionManager.getUsername(), SycoraxKernel.LOG_FILE_NAME);
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
            Config.fileWriter.log("Lock command error: " + e.getMessage(), SycoraxKernel.LOG_FILE_NAME);
            throw e;
        }
    }
}

class PolicyManagementCommand implements Command {
    @Override
    public void execute(String[] args) throws Exception {
        try {
            Config.policyManager.policyEditorLogic();
            Config.fileWriter.log("Policy management executed", SycoraxKernel.LOG_FILE_NAME);
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
            Config.fileWriter.log("Policy management error: " + e.getMessage(), SycoraxKernel.LOG_FILE_NAME);
            throw e;
        }
    }
}

class FileManagementCommand implements Command {
    private final String username;

    public FileManagementCommand(String username) {
        this.username = username;
    }

    @Override
    public void execute(String[] args) throws Exception {
        try {
            new FileManager(username, new Login(username), new FileDownload(username)).fileManagementLogic();
            Config.fileWriter.log("File management executed for user: " + username, SycoraxKernel.LOG_FILE_NAME);
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
            Config.fileWriter.log("File management error: " + e.getMessage(), SycoraxKernel.LOG_FILE_NAME);
            throw e;
        }
    }
}

class ExitCommand implements Command {
    @Override
    public void execute(String[] args) throws Exception {
        try {
            Config.fileWriter.log("Exiting system", SycoraxKernel.LOG_FILE_NAME);
            System.exit(0);
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
            Config.fileWriter.log("Exit command error: " + e.getMessage(), SycoraxKernel.LOG_FILE_NAME);
        }
    }
}

class RestartCommand implements Command {
    @Override
    public void execute(String[] args) throws Exception {
        try {
            Config.fileWriter.log("Restarting system", SycoraxKernel.LOG_FILE_NAME);
            System.exit(100);
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
            Config.fileWriter.log("Restart command error: " + e.getMessage(), SycoraxKernel.LOG_FILE_NAME);
        }
    }
}

class ScriptCommand implements Command {
    private final CommandProcessor commandProcessor;

    public ScriptCommand(CommandProcessor commandProcessor) {
        this.commandProcessor = commandProcessor;
    }

    @Override
    public void execute(String[] args) throws Exception {
        try {
            if (args.length < 2) {
                Config.io.printError("Invalid Syntax: script <filename>");
                Config.fileWriter.log("Script command error: Invalid syntax", SycoraxKernel.LOG_FILE_NAME);
                return;
            }
            boolean success = commandProcessor.executeScript(args[1]);
            if (success) {
                Config.fileWriter.log("Script command executed: " + args[1], SycoraxKernel.LOG_FILE_NAME);
            }
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
            Config.fileWriter.log("Script command error: " + e.getMessage(), SycoraxKernel.LOG_FILE_NAME);
            throw e;
        }
    }
}

class UpdateCommand implements Command {
    private final String username;

    public UpdateCommand(String username) {
        this.username = username;
    }

    @Override
    public void execute(String[] args) throws Exception {
        try {
            new NionUpdate(username).updater();
            new File(Config.io.convertFileSeparator(".|Update.zip")).delete();
            Config.fileWriter.log("Update command executed for user: " + username, SycoraxKernel.LOG_FILE_NAME);
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
            Config.fileWriter.log("Update command error: " + e.getMessage(), SycoraxKernel.LOG_FILE_NAME);
            throw e;
        }
    }
}

class UserManagementCommand implements Command {
    private final String username;

    public UserManagementCommand(String username) {
        this.username = username;
    }

    @Override
    public void execute(String[] args) throws Exception {
        try {
            if (args.length < 2) {
                Config.io.printError("Module Usermgmt: Missing subcommand. Use: create, modify, or delete");
                Config.fileWriter.log("User management error: Missing subcommand", SycoraxKernel.LOG_FILE_NAME);
                return;
            }
            switch (args[1].toLowerCase()) {
                case "create":
                    new AccountCreate(username).execute();
                    Config.fileWriter.log("User management: Created account", SycoraxKernel.LOG_FILE_NAME);
                    break;
                case "modify":
                    new AccountModify(username).execute();
                    Config.fileWriter.log("User management: Modified account", SycoraxKernel.LOG_FILE_NAME);
                    break;
                case "delete":
                    new AccountDelete(username).execute();
                    Config.io.printInfo("Account deleted. Logging out...");
                    Config.fileWriter.log("User management: Deleted account, logging out", SycoraxKernel.LOG_FILE_NAME);
                    Config.console.readLine("Press Enter to logout...");
                    break;
                default:
                    Config.io.printError("Module Usermgmt: " + args[1] + " - Command Not Found");
                    Config.fileWriter.log("User management error: Unknown subcommand - " + args[1], SycoraxKernel.LOG_FILE_NAME);
                    break;
            }
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
            Config.fileWriter.log("User management error: " + e.getMessage(), SycoraxKernel.LOG_FILE_NAME);
            throw e;
        }
    }
}