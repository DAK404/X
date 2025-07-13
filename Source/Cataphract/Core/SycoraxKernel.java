/*
 * A Cross Platform OS Shell
 * Powered By Truncheon Core
 *
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
import java.io.Console;
import java.util.HashMap;
import java.util.Map;

import Cataphract.API.Config;
import Cataphract.API.Minotaur.PolicyCheck;
import Cataphract.API.Minotaur.PolicyManager;
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
 */
public class SycoraxKernel {
    private final AuthenticationManager authManager;
    private final SessionManager sessionManager;
    private final CommandProcessor commandProcessor;

    public SycoraxKernel() {
        this.authManager = new AuthenticationManager();
        this.sessionManager = new SessionManager();
        this.commandProcessor = new CommandProcessor(sessionManager);
    }

    /**
     * Starts the Sycorax kernel, handles login, and launches user shell.
     */
    public void startSycoraxKernel() throws Exception {
        Config.build.viewBuildInfo(false);
        while (!authManager.login()) {
            Config.io.printError("Incorrect Credentials! Please try again.");
            authManager.handleFailedLoginAttempt();
        }
        Config.io.printInfo("Login Successful. Loading Sycorax Kernel...");
        sessionManager.fetchUserDetails(authManager.getUsername());
        commandProcessor.runUserShell();
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
        Config.build.viewBuildInfo(false);
        Config.io.printInfo("Authentication Attempts Left: " + loginAttemptsRemaining);
        String[] credentials = AuthInputHelper.readCredentials(System.console());
        if (credentials == null) {
            Config.io.printError("Username cannot be empty.");
            return false;
        }
        username = credentials[0];
        String password = credentials[1];
        String securityKey = credentials[2];
        return new Login(username).authenticationLogic(password, securityKey);
    }

    /**
     * Handles failed login attempts, locking the system after 5 failures.
     */
    public void handleFailedLoginAttempt() throws Exception {
        loginAttemptsRemaining--;
        if (loginAttemptsRemaining <= 0) {
            Config.io.printError("Authentication Attempts Exceeded! Further attempts are locked!");
            Thread.sleep(36000);
            loginAttemptsRemaining = 5;
        }
    }

    /**
     * Verifies the user's PIN for console unlocking.
     * @param storedPIN The stored PIN to compare against.
     * @return true if the entered PIN matches, false otherwise.
     */
    public boolean challengePIN(String storedPIN) throws Exception {
        String enteredPIN = String.valueOf(System.console().readPassword("> PIN : "));
        return Config.cryptography.stringToSHA3_256(enteredPIN).equals(storedPIN);
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
        this.username = username;
        this.accountName = new Login(username).getNameLogic();
        this.isUserAdmin = new Login(username).checkPrivilegeLogic();
        this.userUnlockPIN = new Login(username).getPINLogic();
        this.systemName = new PolicyCheck().retrievePolicyValue("sysname");
        this.prompt = isUserAdmin ? '!' : '*';
    }

    /**
     * Clears session state on logout.
     */
    public void clearSessionState() {
        username = "DEFAULT_USERNAME";
        accountName = "DEFAULT_USER";
        userUnlockPIN = "";
        isUserAdmin = false;
        prompt = '?';
        System.gc();
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
        Console console = System.console();
        String input;
        Config.build.viewBuildInfo(false);
        do {
            input = console.readLine(sessionManager.getPrompt());
            processCommand(input);
        } while (!input.equalsIgnoreCase("logout"));
        sessionManager.clearSessionState();
    }

    /**
     * Processes a single command or delegates to Anvil.
     */
    public void processCommand(String input) throws Exception {
        if (input == null || input.trim().isEmpty()) return;
        String[] commandArray = Config.io.splitStringToArray(input);
        Command command = commands.get(commandArray[0].toLowerCase());
        if (command != null) {
            command.execute(commandArray);
        } else {
            Config.anvil.anvilInterpreter(commandArray);
        }
    }

    /**
     * Executes a script file line by line.
     */
    public boolean executeScript(String scriptFileName) throws Exception {
        if (scriptFileName == null || scriptFileName.trim().isEmpty() || scriptFileName.startsWith(" ")) {
            Config.io.printError("The name of the script file cannot be blank.");
            return false;
        }
        if (!new PolicyCheck().retrievePolicyValue("script").equals("on") || !sessionManager.isUserAdmin()) {
            Config.io.printError("Insufficient Privileges to run scripts! Please contact the Administrator.");
            return false;
        }
        String filePath = Config.io.convertFileSeparator(".|Users|Cataphract|" + sessionManager.getUsername() + "|" + scriptFileName);
        File scriptFile = new File(filePath);
        if (!scriptFile.exists() || scriptFile.isDirectory()) {
            Config.io.printAttention("The specified script file is invalid or has not been found.\nPlease check the script file name and try again.");
            return false;
        }
        if (scriptMode) {
            Config.io.printError("Cannot execute script within another script.");
            return false;
        }
        scriptMode = true;
        try (BufferedReader br = new BufferedReader(new FileReader(scriptFile))) {
            String scriptLine;
            while ((scriptLine = br.readLine()) != null) {
                if (scriptLine.startsWith("#") || scriptLine.trim().isEmpty()) continue;
                if (scriptLine.equalsIgnoreCase("End Script")) break;
                processCommand(scriptLine);
            }
            return true;
        } finally {
            scriptMode = false;
            System.gc();
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
        sessionManager.fetchUserDetails(sessionManager.getUsername());
        Config.io.printInfo("User details refreshed.");
    }
}

class LockCommand implements Command {
    private final SessionManager sessionManager;

    public LockCommand(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void execute(String[] args) throws Exception {
        AuthenticationManager authManager = new AuthenticationManager();
        String input;
        Config.build.clearScreen();
        do {
            StringBuilder lockPromptBuilder = new StringBuilder()
                    .append((char)27).append("[33;49m")
                    .append(Config.time.getDateTimeUsingSpecifiedFormat("yyyy-MMM-dd HH:mm:ss"))
                    .append("  LOCKED\n")
                    .append(sessionManager.getPrompt())
                    .append((char)27).append("[0m");
            input = System.console().readLine(lockPromptBuilder.toString());
        } while (!input.equalsIgnoreCase("unlock"));
        Config.io.printAttention("Please Enter Unlock PIN To Continue.");
        while (!authManager.challengePIN(sessionManager.getUserUnlockPIN())) {
            Config.io.printError("Incorrect PIN.");
            authManager.handleFailedLoginAttempt();
        }
        Config.build.viewBuildInfo(false);
    }
}

class PolicyManagementCommand implements Command {
    @Override
    public void execute(String[] args) throws Exception {
        new PolicyManager().policyEditorLogic();
    }
}

class FileManagementCommand implements Command {
    private final String username;

    public FileManagementCommand(String username) {
        this.username = username;
    }

    @Override
    public void execute(String[] args) throws Exception {
        new FileManagement(username).fileManagementLogic();
    }
}

class ExitCommand implements Command {
    @Override
    public void execute(String[] args) {
        System.exit(0);
    }
}

class RestartCommand implements Command {
    @Override
    public void execute(String[] args) {
        System.exit(100);
    }
}

class ScriptCommand implements Command {
    private final CommandProcessor commandProcessor;

    public ScriptCommand(CommandProcessor commandProcessor) {
        this.commandProcessor = commandProcessor;
    }

    @Override
    public void execute(String[] args) throws Exception {
        if (args.length < 2) {
            Config.io.printError("Invalid Syntax: script <filename>");
            return;
        }
        commandProcessor.executeScript(args[1]);
    }
}

class UpdateCommand implements Command {
    private final String username;

    public UpdateCommand(String username) {
        this.username = username;
    }

    @Override
    public void execute(String[] args) throws Exception {
        new NionUpdate(username).updater();
        new File(Config.io.convertFileSeparator(".|Update.zip")).delete();
    }
}

class UserManagementCommand implements Command {
    private final String username;

    public UserManagementCommand(String username) {
        this.username = username;
    }

    @Override
    public void execute(String[] args) throws Exception {
        if (args.length < 2) {
            Config.io.printError("Module Usermgmt: Missing subcommand. Use: create, modify, or delete");
            return;
        }
        switch (args[1].toLowerCase()) {
            case "create":
                new AccountCreate(username).execute();
                break;
            case "modify":
                new AccountModify(username).execute();
                break;
            case "delete":
                new AccountDelete(username).execute();
                Config.io.printInfo("Account deleted. Logging out...");
                System.console().readLine("Press Enter to logout...");
                break;
            default:
                Config.io.printError("Module Usermgmt: " + args[1] + " - Command Not Found");
                break;
        }
    }
}