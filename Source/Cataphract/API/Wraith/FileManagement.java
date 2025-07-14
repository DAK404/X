package Cataphract.API.Wraith;

import java.io.FileOutputStream;
import java.net.URI;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Path;

import Cataphract.API.Config;
import Cataphract.API.Dragon.Login;

/**
 * Handles file management operations (download, delete, move) for the Cataphract shell.
 * Implements IFileOperation to provide a standardized interface for file management commands.
 */
public class FileManagement implements IFileOperation {
    private final Login login;
    private final FileRead fileRead;
    /**
     * Constructs a FileManagement instance with dependencies.
     *
     * @param login        The login handler for privilege checks.
     * @param policyCheck  The policy checker for permission validation.
     * @param Config.io    The IO streams handler for path conversion and output.
     * @param time         The time utility for logging timestamps.
     * @param pathUtils    The path utility for resolving paths.
     */
    public FileManagement(Login login, FileRead fileRead) {
        this.login = login;
        this.fileRead = fileRead;
    }

    /**
     * Executes the file management command (download, delete, move).
     *
     * @param commandArray The command and its arguments (e.g., ["file", "download", "url", "filename"]).
     * @throws Exception If the command execution fails.
     */
    @Override
    public void execute(String[] commandArray) throws Exception {
        if (commandArray.length < 2) {
            Config.io.printError("Invalid syntax. Expected: file <download|delete|move|read|help> <args>");
            return;
        }

        String subCommand = commandArray[1].toLowerCase();
        switch (subCommand) {
            case "download":
                if (commandArray.length < 4) {
                    Config.io.printError("Invalid syntax. Expected: file download <url> <filename>");
                    return;
                }
                if (!hasPermission("download")) {
                    Config.io.printError("Insufficient privileges to download files.");
                    return;
                }
                downloadFile(commandArray[2], resolvePath(commandArray[3]));
                break;
            case "delete":
                if (commandArray.length < 3) {
                    Config.io.printError("Invalid syntax. Expected: file delete <filename>");
                    return;
                }
                if (!hasPermission("delete")) {
                    Config.io.printError("Insufficient privileges to delete files.");
                    return;
                }
                deleteFile(resolvePath(commandArray[2]));
                break;
            case "move":
                if (commandArray.length < 4) {
                    Config.io.printError("Invalid syntax. Expected: file move <source> <destination>");
                    return;
                }
                if (!hasPermission("move")) {
                    Config.io.printError("Insufficient privileges to move files.");
                    return;
                }
                moveFile(resolvePath(commandArray[2]), resolvePath(commandArray[3]));
                break;

            case "read":
                if (commandArray.length < 2)
                {
                    Config.io.printError("Invalid syntax. Expected: file read <filename>");
                    return;
                }
                if (!hasPermission("read")) {
                    Config.io.printError("Insufficient privileges to read files.");
                    return;
                }
                readFile(commandArray[2]);
                break;

            case "help":
                readHelp();
                break;
            default:
                Config.io.printError("File Management - Unknown subcommand: " + subCommand);
        }
    }

    /**
     * Downloads a file from the specified URL to the given destination path.
     *
     * @param url          The URL of the file to download.
     * @param destination  The path where the file will be saved.
     * @throws Exception If the download fails.
     */
    private void downloadFile(String url, Path destination) throws Exception {
        try (var channel = Channels.newChannel(URI.create(url).toURL().openStream());
             var outputStream = new FileOutputStream(destination.toFile());
             var fileChannel = outputStream.getChannel()) {
            fileChannel.transferFrom(channel, 0, Long.MAX_VALUE);
            Config.io.println("Downloaded: " + destination);
            logOperation("Downloaded file: " + Config.io.convertToNionSeparator(destination.toString()));
        } catch (Exception e) {
            Config.io.printError("Error downloading file: " + e.getMessage());
            Config.exceptionHandler.handleException(e);
        }
    }

    /**
     * Deletes the specified file.
     *
     * @param filePath The path to the file to delete.
     * @throws Exception If the deletion fails.
     */
    private void deleteFile(Path filePath) throws Exception {
        try {
            if (!Files.exists(filePath)) {
                Config.io.printError("File does not exist: " + filePath);
                return;
            }
            Files.delete(filePath);
            Config.io.println("Deleted: " + filePath);
            logOperation("Deleted file: " + Config.io.convertToNionSeparator(filePath.toString()));
        } catch (Exception e) {
            Config.io.printError("Error deleting file: " + e.getMessage());
            Config.exceptionHandler.handleException(e);
        }
    }

    private void readFile(String fileName) throws Exception
    {
        fileRead.execute(new String[]{fileName});
    }

    private void readHelp() throws Exception
    {
        fileRead.execute(new String[]{"help", "API|Wraith|Grinch.help"});
    }

    /**
     * Moves the specified file to the destination path.
     *
     * @param source      The path to the source file.
     * @param destination The path to the destination.
     * @throws Exception If the move operation fails.
     */
    private void moveFile(Path source, Path destination) throws Exception {
        try {
            if (!Files.exists(source)) {
                Config.io.printError("Source file does not exist: " + source);
                return;
            }
            Files.move(source, destination);
            Config.io.println("Moved: " + source + " to " + destination);
            logOperation("Moved file: " + Config.io.convertToNionSeparator(source.toString()) + " to " + Config.io.convertToNionSeparator(destination.toString()));
        } catch (Exception e) {
            Config.io.printError("Error moving file: " + e.getMessage());
            Config.exceptionHandler.handleException(e);
        }
    }

    /**
     * Logs the file operation with a timestamp.
     *
     * @param message The operation message to log.
     * @throws Exception If the logging operation fails.
     */
    private void logOperation(String message) throws Exception {
        FileWrite logger = new FileWrite(null);
        logger.log(message, "FileManagementLog");
    }

    /**
     * Checks if the user has the specified permission.
     *
     * @param permission The permission to check (e.g., "Download", "Delete", "Move").
     * @return true if the user has the permission, false otherwise.
     * @throws Exception If a database error occurs.
     */
    private boolean hasPermission(String permission) throws Exception {
        String policyValue = Config.policyCheck.retrievePolicyValue(permission);
        return login.checkPrivilegeLogic() || "true".equalsIgnoreCase(policyValue);
    }

    /**
     * Resolves the file name to a path within the current directory.
     *
     * @param fileName The file name.
     * @return The resolved path.
     */
    private Path resolvePath(String fileName) {
        return Path.of(System.getProperty("user.dir"), Config.io.convertFileSeparator(fileName));
    }
}

/**
 * Interface for file operations in the Cataphract shell.
 */
interface IFileOperation {
    void execute(String[] commandArray) throws Exception;
}