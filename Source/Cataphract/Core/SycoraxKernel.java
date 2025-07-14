/*
 * This file is part of the Cataphract project.
 * Copyright (C) 2024 DAK404 (https://github.com/DAK404)
 *
 * This program is distributed under the GNU General Public License as published by
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
import Cataphract.API.Wraith.FileManagement;
import Cataphract.API.Wraith.FileRead;
import Cataphract.API.Wraith.FileUnzip;
import Cataphract.API.Wraith.FileWrite;
import Cataphract.API.Wyvern.UpdateManager;
import Cataphract.API.Dragon.AccountCreate;
import Cataphract.API.Dragon.AccountDelete;
import Cataphract.API.Dragon.AccountModify;
import Cataphract.API.Dragon.AuthInputHelper;
import Cataphract.API.Dragon.Login;
import Cataphract.API.Minotaur.PolicyManager;

/**
 * Main class for the Sycorax operating system kernel.
 * Coordinates authentication, session management, and command processing.
 *
 * @author DAK404 (https://github.com/DAK404)
 * @version 1.5.0 (14-July-2025, Cataphract)
 * @since 0.0.1 (Cataphract 0.0.1)
 */
public class SycoraxKernel {
    protected static final String LOG_FILE_NAME = "ShellLog";
    private final AuthenticationManager authManager;
    private final SessionManager sessionManager;
    private final CommandProcessor commandProcessor;
    private final FileWrite fileWrite;

    /**
     * Constructs a SycoraxKernel with injected dependencies.
     *
     * @param build       The build information handler.
     * @param Config.io   The IO streams handler for console output.
     * @param fileWrite   The file write handler for logging.
     */
    public SycoraxKernel(FileWrite fileWrite) {
        this.fileWrite = fileWrite;
        this.authManager = new AuthenticationManager(fileWrite);
        this.sessionManager = new SessionManager(fileWrite);
        this.commandProcessor = new CommandProcessor(sessionManager, fileWrite);
    }

    /**
     * Starts the Sycorax kernel, handles login, and launches user shell.
     *
     * @throws Exception If an error occurs during kernel startup.
     */
    public void startSycoraxKernel() throws Exception {
        try {
            Config.build.viewBuildInfo(false);
            fileWrite.log("Starting Sycorax kernel", LOG_FILE_NAME);
            while (!authManager.login()) {
                Config.io.printError("Incorrect Credentials! Please try again.");
                fileWrite.log("Failed login attempt", LOG_FILE_NAME);
                authManager.handleFailedLoginAttempt();
            }
            Config.io.printInfo("Login Successful. Loading Sycorax Kernel...");
            fileWrite.log("Login successful for user: " + authManager.getUsername(), LOG_FILE_NAME);
            sessionManager.fetchUserDetails(authManager.getUsername());
            commandProcessor.runUserShell();
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
            fileWrite.log("Kernel startup failed: " + e.getMessage(), LOG_FILE_NAME);
        }
    }
}

/**
 * Manages user authentication and login attempts.
 */
class AuthenticationManager {
    private String username = "DEFAULT_USERNAME";
    private int loginAttemptsRemaining = 5;
    private final FileWrite fileWrite;

    public AuthenticationManager(FileWrite fileWrite) {
        this.fileWrite = fileWrite;
    }

    /**
     * Attempts to log in the user using credentials from AuthInputHelper.
     *
     * @return true if login is successful, false otherwise.
     * @throws Exception If an error occurs during authentication.
     */
    public boolean login() throws Exception {
        try {
            Config.build.viewBuildInfo(false);
            Config.io.printInfo("Authentication Attempts Left: " + loginAttemptsRemaining);
            fileWrite.log("Attempting login, attempts remaining: " + loginAttemptsRemaining, SycoraxKernel.LOG_FILE_NAME);
            String[] credentials = AuthInputHelper.readCredentials(Config.console);
            if (credentials == null || credentials[0] == null || credentials[0].trim().isEmpty()) {
                Config.io.printError("Username cannot be empty.");
                fileWrite.log("Login failed: Empty username", SycoraxKernel.LOG_FILE_NAME);
                return false;
            }
            username = credentials[0];
            String password = credentials[1];
            String securityKey = credentials[2];
            boolean success = new Login(username).authenticationLogic(password, securityKey);
            if (success) {
                fileWrite.log("Login successful for user: " + username, SycoraxKernel.LOG_FILE_NAME);
            }
            return success;
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
            fileWrite.log("Login error: " + e.getMessage(), SycoraxKernel.LOG_FILE_NAME);
            throw e;
        }
    }

    /**
     * Handles failed login attempts, locking the system after 5 failures.
     *
     * @throws Exception If an error occurs during lockout.
     */
    public void handleFailedLoginAttempt() throws Exception {
        loginAttemptsRemaining--;
        fileWrite.log("Failed login attempt, remaining: " + loginAttemptsRemaining, SycoraxKernel.LOG_FILE_NAME);
        if (loginAttemptsRemaining <= 0) {
            Config.io.printError("Authentication Attempts Exceeded! Further attempts are locked!");
            fileWrite.log("Authentication attempts exceeded, locking system", SycoraxKernel.LOG_FILE_NAME);
            Thread.sleep(36000);
            loginAttemptsRemaining = 5;
            fileWrite.log("Reset login attempts to 5 after lock", SycoraxKernel.LOG_FILE_NAME);
        }
    }

    /**
     * Verifies the user's PIN for console unlocking.
     *
     * @param storedPIN The stored PIN to compare against.
     * @return true if the entered PIN matches, false otherwise.
     * @throws Exception If an error occurs during PIN verification.
     */
    public boolean challengePIN(String storedPIN) throws Exception {
        String enteredPIN = String.valueOf(Config.console.readPassword("> PIN : "));
        boolean success = Config.cryptography.stringToSHA3_256(enteredPIN).equals(storedPIN);
        fileWrite.log("PIN challenge " + (success ? "successful" : "failed") + " for user: " + username, SycoraxKernel.LOG_FILE_NAME);
        return success;
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
    private final FileWrite fileWrite;

    public SessionManager(FileWrite fileWrite) {
        this.fileWrite = fileWrite;
    }

    /**
     * Fetches user details after successful login.
     *
     * @param username The authenticated username.
     * @throws Exception If an error occurs during detail fetching.
     */
    public void fetchUserDetails(String username) throws Exception {
        this.username = username;
        this.accountName = new Login(username).getNameLogic();
        this.isUserAdmin = new Login(username).checkPrivilegeLogic();
        this.userUnlockPIN = new Login(username).getPINLogic();
        this.systemName = Config.policyCheck.retrievePolicyValue("sysname");
        this.prompt = isUserAdmin ? '!' : '*';
        fileWrite.log("Fetched user details for: " + username, SycoraxKernel.LOG_FILE_NAME);
    }

    /**
     * Clears session state on logout.
     *
     * @throws Exception If an error occurs during state clearing.
     */
    public void clearSessionState() throws Exception {
        username = "DEFAULT_USERNAME";
        accountName = "DEFAULT_USER";
        userUnlockPIN = "";
        isUserAdmin = false;
        prompt = '?';
        System.gc();
        fileWrite.log("Session state cleared", SycoraxKernel.LOG_FILE_NAME);
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
    private final FileWrite fileWrite;
    private final Map<String, Command> commands;
    private boolean scriptMode = false;

    public CommandProcessor(SessionManager sessionManager, FileWrite fileWrite) {
        this.sessionManager = sessionManager;
        this.fileWrite = fileWrite;
        this.commands = new HashMap<>();
        initializeCommands();
    }

    private void initializeCommands() {
        commands.put("refresh", new RefreshCommand(sessionManager, fileWrite));
        commands.put("lock", new LockCommand(sessionManager, fileWrite));
        commands.put("policymgmt", new PolicyManagementCommand(fileWrite));
        commands.put("file", new FileManagementCommand(sessionManager.getUsername(), fileWrite));
        commands.put("exit", new ExitCommand(fileWrite));
        commands.put("restart", new RestartCommand(fileWrite));
        commands.put("script", new ScriptCommand(this, fileWrite));
        commands.put("update", new UpdateCommand(sessionManager.getUsername(), fileWrite));
        commands.put("usermgmt", new UserManagementCommand(sessionManager.getUsername(), fileWrite));
    }

    /**
     * Runs the user shell, reading and processing commands.
     *
     * @throws Exception If an error occurs during shell execution.
     */
    public void runUserShell() throws Exception {
        Config.build.viewBuildInfo(false);
        fileWrite.log("Starting user shell for: " + sessionManager.getUsername(), SycoraxKernel.LOG_FILE_NAME);
        String input;
        do {
            input = Config.console.readLine(sessionManager.getPrompt());
            processCommand(input);
        } while (!input.equalsIgnoreCase("logout"));
        sessionManager.clearSessionState();
        fileWrite.log("User shell terminated", SycoraxKernel.LOG_FILE_NAME);
    }

    /**
     * Processes a single command or delegates to Anvil.
     *
     * @param input The command input to process.
     * @throws Exception If an error occurs during command execution.
     */
    public void processCommand(String input) throws Exception {
        if (input == null || input.trim().isEmpty()) return;
        fileWrite.log("Processing command: " + input, SycoraxKernel.LOG_FILE_NAME);
        String[] commandArray = Config.io.splitStringToArray(input);
        Command command = commands.get(commandArray[0].toLowerCase());
        if (command != null) {
            command.execute(commandArray);
            fileWrite.log("Executed command: " + commandArray[0], SycoraxKernel.LOG_FILE_NAME);
        } else {
            Config.anvil.anvilInterpreter(commandArray);
            fileWrite.log("Delegated to Anvil: " + input, SycoraxKernel.LOG_FILE_NAME);
        }
    }

    /**
     * Executes a script file line by line.
     *
     * @param scriptFileName The name of the script file.
     * @return true if the script executes successfully, false otherwise.
     * @throws Exception If an error occurs during script execution.
     */
    public boolean executeScript(String scriptFileName) throws Exception {
        if (scriptFileName == null || scriptFileName.trim().isEmpty() || scriptFileName.startsWith(" ")) {
            Config.io.printError("The name of the script file cannot be blank.");
            fileWrite.log("Script execution failed: Invalid script file name", SycoraxKernel.LOG_FILE_NAME);
            return false;
        }
        if (!Config.policyCheck.retrievePolicyValue("script").equals("on") && !sessionManager.isUserAdmin()) {
            Config.io.printError("Insufficient Privileges to run scripts! Please contact the Administrator.");
            fileWrite.log("Script execution failed: Insufficient privileges", SycoraxKernel.LOG_FILE_NAME);
            return false;
        }
        String filePath = Config.io.convertFileSeparator(".|Users|Cataphract|" + sessionManager.getUsername() + "|" + scriptFileName);
        File scriptFile = new File(filePath);
        if (!scriptFile.exists() || scriptFile.isDirectory()) {
            Config.io.printAttention("The specified script file is invalid or has not been found.\nPlease check the script file name and try again.");
            fileWrite.log("Script execution failed: File not found - " + filePath, SycoraxKernel.LOG_FILE_NAME);
            return false;
        }
        if (scriptMode) {
            Config.io.printError("Cannot execute script within another script.");
            fileWrite.log("Script execution failed: Nested script execution", SycoraxKernel.LOG_FILE_NAME);
            return false;
        }
        scriptMode = true;
        try (BufferedReader br = new BufferedReader(new FileReader(scriptFile))) {
            String scriptLine;
            fileWrite.log("Executing script: " + scriptFileName, SycoraxKernel.LOG_FILE_NAME);
            while ((scriptLine = br.readLine()) != null) {
                if (scriptLine.startsWith("#") || scriptLine.trim().isEmpty()) continue;
                if (scriptLine.equalsIgnoreCase("End Script")) break;
                processCommand(scriptLine);
            }
            fileWrite.log("Script execution completed: " + scriptFileName, SycoraxKernel.LOG_FILE_NAME);
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
 * Command to refresh user session details.
 */
class RefreshCommand implements Command {
    private final SessionManager sessionManager;
    private final FileWrite fileWrite;

    public RefreshCommand(SessionManager sessionManager, FileWrite fileWrite) {
        this.sessionManager = sessionManager;
        this.fileWrite = fileWrite;
    }

    @Override
    public void execute(String[] args) throws Exception {
        sessionManager.fetchUserDetails(sessionManager.getUsername());
        Config.io.printInfo("User details refreshed.");
        fileWrite.log("Refreshed user details for: " + sessionManager.getUsername(), SycoraxKernel.LOG_FILE_NAME);
    }
}

/**
 * Command to lock the console.
 */
class LockCommand implements Command {
    private final SessionManager sessionManager;
    private final FileWrite fileWrite;

    public LockCommand(SessionManager sessionManager, FileWrite fileWrite) {
        this.sessionManager = sessionManager;
        this.fileWrite = fileWrite;
    }

    @Override
    public void execute(String[] args) throws Exception {
        AuthenticationManager authManager = new AuthenticationManager(fileWrite);
        String input;
        Config.build.clearScreen();
        fileWrite.log("Locking console for user: " + sessionManager.getUsername(), SycoraxKernel.LOG_FILE_NAME);
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
            fileWrite.log("Incorrect PIN entered", SycoraxKernel.LOG_FILE_NAME);
            authManager.handleFailedLoginAttempt();
        }
        Config.build.viewBuildInfo(false);
        fileWrite.log("Console unlocked for user: " + sessionManager.getUsername(), SycoraxKernel.LOG_FILE_NAME);
    }
}

/**
 * Command to manage policies.
 */
class PolicyManagementCommand implements Command {
    private final FileWrite fileWrite;

    public PolicyManagementCommand(FileWrite fileWrite) {
        this.fileWrite = fileWrite;
    }

    @Override
    public void execute(String[] args) throws Exception {
        new PolicyManager().policyEditorLogic();
        fileWrite.log("Policy management executed", SycoraxKernel.LOG_FILE_NAME);
    }
}

/**
 * Command to manage files.
 */
class FileManagementCommand implements Command {
    private final String username;
    private final FileWrite fileWrite;

    public FileManagementCommand(String username, FileWrite fileWrite) {
        this.username = username;
        this.fileWrite = fileWrite;
    }

    @Override
    public void execute(String[] args) throws Exception {
        Login login = new Login(username);
        new FileManagement(login, new FileRead(login)).execute(args);
        fileWrite.log("File management executed for user: " + username, SycoraxKernel.LOG_FILE_NAME);
    }
}

/**
 * Command to exit the system.
 */
class ExitCommand implements Command {
    private final FileWrite fileWrite;

    public ExitCommand(FileWrite fileWrite) {
        this.fileWrite = fileWrite;
    }

    @Override
    public void execute(String[] args) throws Exception {
        fileWrite.log("Exiting system", SycoraxKernel.LOG_FILE_NAME);
        System.exit(0);
    }
}

/**
 * Command to restart the system.
 */
class RestartCommand implements Command {
    private final FileWrite fileWrite;

    public RestartCommand(FileWrite fileWrite) {
        this.fileWrite = fileWrite;
    }

    @Override
    public void execute(String[] args) throws Exception {
        fileWrite.log("Restarting system", SycoraxKernel.LOG_FILE_NAME);
        System.exit(211);
    }
}

/**
 * Command to execute scripts.
 */
class ScriptCommand implements Command {
    private final CommandProcessor commandProcessor;
    private final FileWrite fileWrite;

    public ScriptCommand(CommandProcessor commandProcessor, FileWrite fileWrite) {
        this.commandProcessor = commandProcessor;
        this.fileWrite = fileWrite;
    }

    @Override
    public void execute(String[] args) throws Exception {
        if (args.length < 2) {
            Config.io.printError("Invalid Syntax: script <filename>");
            fileWrite.log("Script command error: Invalid syntax", SycoraxKernel.LOG_FILE_NAME);
            return;
        }
        boolean success = commandProcessor.executeScript(args[1]);
        if (success) {
            fileWrite.log("Script command executed: " + args[1], SycoraxKernel.LOG_FILE_NAME);
        }
    }
}

/**
 * Command to update the system.
 */
class UpdateCommand implements Command {
    private final String username;
    private final FileWrite fileWrite;

    public UpdateCommand(String username, FileWrite fileWrite) {
        this.username = username;
        this.fileWrite = fileWrite;
    }

    @Override
    public void execute(String[] args) throws Exception {
        new UpdateManager(
            username,
            new FileDownload(new Login(username)),
            new FileUnzip(new Login(username)),
            fileWrite
        ).performUpdate();
        new File(Config.io.convertFileSeparator(".|Cataphract.zip")).delete();
        fileWrite.log("Update command executed for user: " + username, SycoraxKernel.LOG_FILE_NAME);
    }
}

/**
 * Command to manage user accounts.
 */
class UserManagementCommand implements Command {
    private final String username;
    private final FileWrite fileWrite;

    public UserManagementCommand(String username, FileWrite fileWrite) {
        this.username = username;
        this.fileWrite = fileWrite;
    }

    @Override
    public void execute(String[] args) throws Exception {
        if (args.length < 2) {
            Config.io.printError("Module Usermgmt: Missing subcommand. Use: create, modify, or delete");
            fileWrite.log("User management error: Missing subcommand", SycoraxKernel.LOG_FILE_NAME);
            return;
        }
        switch (args[1].toLowerCase()) {
            case "create":
                new AccountCreate(username).execute();
                fileWrite.log("User management: Created account", SycoraxKernel.LOG_FILE_NAME);
                break;
            case "modify":
                new AccountModify(username).execute();
                fileWrite.log("User management: Modified account", SycoraxKernel.LOG_FILE_NAME);
                break;
            case "delete":
                new AccountDelete(username).execute();
                Config.io.printInfo("Account deleted. Logging out...");
                fileWrite.log("User management: Deleted account, logging out", SycoraxKernel.LOG_FILE_NAME);
                Config.console.readLine("Press Enter to logout...");
                break;
            default:
                Config.io.printError("Module Usermgmt: " + args[1] + " - Command Not Found");
                fileWrite.log("User management error: Unknown subcommand - " + args[1], SycoraxKernel.LOG_FILE_NAME);
                break;
        }
    }
}