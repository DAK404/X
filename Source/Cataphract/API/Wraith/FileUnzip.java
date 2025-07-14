package Cataphract.API.Wraith;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipInputStream;

import Cataphract.API.Config;
import Cataphract.API.Dragon.Login;

/**
 * Handles file unzipping operations for the Cataphract shell.
 * Implements IFileOperation to provide a standardized interface for unzip commands.
 */
public class FileUnzip implements IFileOperation {
    private final Login login;
    
    /**
     * Constructs a FileUnzip instance with dependencies.
     *
     * @param login        The login handler for privilege checks.
     * @param policyCheck  The policy checker for permission validation.
     * @param Config.io    The IO streams handler for path conversion and output.
     */
    public FileUnzip(Login login) {
        this.login = login;
    }

    /**
     * Executes the file unzip command.
     *
     * @param commandArray The command and its arguments (e.g., ["unzip", "archive.zip", "destination"]).
     * @throws Exception If the command execution fails.
     */
    @Override
    public void execute(String[] commandArray) throws Exception {
        if (commandArray.length < 3) {
            Config.io.printError("Invalid syntax. Expected: unzip <archive> <destination>");
            return;
        }

        String archive = commandArray[1];
        String destination = commandArray[2];

        if (!hasPermission("FileUnzip")) {
            Config.io.printError("Insufficient privileges to unzip files.");
            return;
        }

        Path archivePath = resolvePath(archive);
        Path destinationPath = resolvePath(destination);

        if (!Files.exists(archivePath)) {
            Config.io.printError("Archive does not exist: " + archive);
            return;
        }

        unzipFile(archivePath, destinationPath);
    }

    /**
     * Unzips the specified archive to the destination path.
     *
     * @param archivePath      The path to the zip archive.
     * @param destinationPath  The path where the archive will be extracted.
     * @throws Exception If the unzip operation fails.
     */
    private void unzipFile(Path archivePath, Path destinationPath) throws Exception {
        try (var zipStream = new ZipInputStream(Files.newInputStream(archivePath))) {
            var entry = zipStream.getNextEntry();
            while (entry != null) {
                Path entryPath = destinationPath.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    try (var outputStream = Files.newOutputStream(entryPath)) {
                        zipStream.transferTo(outputStream);
                    }
                }
                entry = zipStream.getNextEntry();
            }
            Config.io.println("Unzipped: " + archivePath + " to " + destinationPath);
        } catch (Exception e) {
            Config.io.printError("Error unzipping file: " + e.getMessage());
            Config.exceptionHandler.handleException(e);
        }
    }

    /**
     * Checks if the user has the specified permission.
     *
     * @param permission The permission to check (e.g., "FileUnzip").
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