package Cataphract.API.Wraith;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import Cataphract.API.Config;
import Cataphract.API.Dragon.Login;

/**
 * Handles file writing and logging operations for the Cataphract shell.
 */
public class FileWrite {
    private final Login login;

    /**
     * Constructs a FileWrite instance with dependencies for file editing and logging.
     *
     * @param login        The login handler for privilege checks (nullable for logging-only instances).
     * @param pathUtils    The path utility for resolving log paths.
     */
    public FileWrite(Login login) {
        this.login = login;
    }

    /**
     * Executes the file write command.
     *
     * @param commandArray The command and its arguments (e.g., ["write", "file.txt"]).
     * @throws Exception If the command execution fails.
     */
    public void execute(String[] commandArray) throws Exception {
        if (commandArray.length < 2) {
            Config.io.printError("Invalid syntax. Expected: write <file>");
            return;
        }

        String fileName = commandArray[1];
        String dir = System.getProperty("user.dir");

        if (!hasPermission("FileWrite")) {
            Config.io.printError("Insufficient privileges to write files.");
            return;
        }

        editFile(fileName, dir);
    }

    /**
     * Edits a file by overwriting or appending based on user input.
     *
     * @param fileName The name of the file to edit.
     * @param dir      The directory path where the file is located.
     * @throws Exception If the write or logging operation fails.
     */
    public void editFile(String fileName, String dir) throws Exception {
        if (Config.console == null) {
            Config.io.printError("Cannot edit file: Console input is unavailable.");
            return;
        }

        Path filePath = resolvePath(fileName, dir);
        if (!Files.exists(Path.of(dir))) {
            Config.io.printError("Directory does not exist: " + dir);
            return;
        }

        boolean append = true;
        if (Files.exists(filePath)) {
            Config.io.printAttention("A file with the same name has been found in this directory. Do you want to OVERWRITE it, APPEND to the file, or GO BACK?");
            Config.io.println("Options: [ OVERWRITE | APPEND | RETURN | HELP ]");
            String choice = Config.console.readLine().toLowerCase();
            switch (choice) {
                case "overwrite":
                    append = false;
                    Config.io.printAttention("The new content will overwrite the previous content present in the file!");
                    break;
                case "append":
                    Config.io.println("The new content will be added to the end of the file! Previous data will remain unchanged.");
                    break;
                case "help":
                    Config.io.println("Work in Progress");
                    return;
                case "return":
                    return;
                default:
                    Config.io.printError("Invalid choice. Exiting...");
                    return;
            }
        }

        writeFile(filePath, append);
        log(String.format("User %s edited file: %s", login != null ? login.getNameLogic() : "SYSTEM", Config.io.convertToNionSeparator(filePath.toString())), "FileWriteLog");
    }

    /**
     * Writes user input to the specified file.
     *
     * @param filePath The path to the file to write.
     * @param append   Whether to append (true) or overwrite (false).
     * @throws Exception If the write operation fails.
     */
    private void writeFile(Path filePath, boolean append) throws Exception {
        try {
            Config.io.println("Wraith Text Editor 1.5");
            Config.io.println("______________________\n");
            Config.io.println("Editing File: " + filePath.getFileName() + "\n");

            StringBuilder content = new StringBuilder();
            String line;
            while (!(line = Config.console.readLine()).equalsIgnoreCase("<exit>")) {
                content.append(line).append("\n");
            }

            Files.writeString(filePath, content.toString(), StandardOpenOption.CREATE,
                    append ? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING);
            Config.io.println("Wrote to file: " + filePath);
        } catch (Exception e) {
            Config.io.printError("Error writing to file: " + e.getMessage());
            Config.exceptionHandler.handleException(e);
        }
    }

    /**
     * Logs a message to a file with a timestamp.
     *
     * @param printToFile The message to log.
     * @param fileName    The name of the log file (without .log extension).
     * @throws Exception If the logging operation fails.
     */
    public void log(String printToFile, String fileName) throws Exception {
        try {
            if (!Config.io.checkFileValidity(fileName)) {
                Config.io.printError("The provided log file name is invalid. Please provide a valid file name.");
                return;
            }

            Path logPath = Config.pathUtils.getLogPath(fileName, true);
            String logMessage = String.format("%s (%d): %s%n",
                    Config.time.getDateTimeUsingSpecifiedFormat("dd-MMMM-yyyy HH:mm:ss"),
                    Config.time.getUnixEpoch(),
                    printToFile);
            Files.writeString(logPath, logMessage, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception e) {
            Config.io.printError("Error logging to file: " + e.getMessage());
            Config.exceptionHandler.handleException(e);
        }
    }

    /**
     * Checks if the user has the specified permission.
     *
     * @param permission The permission to check (e.g., "FileWrite").
     * @return true if the user has the permission, false otherwise.
     * @throws Exception If a database error occurs.
     */
    private boolean hasPermission(String permission) throws Exception {
        if (login == null || Config.policyCheck == null) {
            Config.io.printError("Cannot check permissions: Authentication components not initialized.");
            return false;
        }
        String policyValue = Config.policyCheck.retrievePolicyValue(permission);
        return login.checkPrivilegeLogic() || "true".equalsIgnoreCase(policyValue);
    }

    /**
     * Resolves the file name to a path within the specified directory.
     *
     * @param fileName The file name.
     * @param dir      The directory path.
     * @return The resolved path.
     */
    private Path resolvePath(String fileName, String dir) {
        return Path.of(dir, Config.io.convertFileSeparator(fileName));
    }
}