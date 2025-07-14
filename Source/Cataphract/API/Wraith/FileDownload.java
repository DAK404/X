package Cataphract.API.Wraith;

import java.io.FileOutputStream;
import java.net.URI;
import java.nio.channels.Channels;
import java.nio.file.Path;

import Cataphract.API.Config;
import Cataphract.API.Dragon.Login;

/**
 * Handles file downloading operations for the Cataphract shell.
 * Implements IFileOperation to provide a standardized interface for download commands.
 */
public class FileDownload implements IFileOperation {
    private final Login login;

    /**
     * Constructs a FileDownload instance with dependencies.
     *
     * @param login        The login handler for privilege checks.
     * @param policyCheck  The policy checker for permission validation.
     * @param Config.io    The IO streams handler for path conversion and output.
     */
    public FileDownload(Login login) {
        this.login = login;
    }

    /**
     * Executes the file download command.
     *
     * @param commandArray The command and its arguments (e.g., ["download", "url", "filename"]).
     * @throws Exception If the command execution fails.
     */
    @Override
    public void execute(String[] commandArray) throws Exception {
        if (commandArray.length < 3) {
            Config.io.printError("Invalid syntax. Expected: download <url> <filename>");
            return;
        }

        String url = commandArray[1];
        String filename = commandArray[2];

        if (!hasPermission("Download")) {
            Config.io.printError("Insufficient privileges to download files.");
            return;
        }

        if (!isValidFileName(filename)) {
            Config.io.printError("Invalid file name: " + filename);
            return;
        }

        Path destination = resolvePath(filename);
        downloadFile(url, destination);
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
        } catch (Exception e) {
            Config.io.printError("Error downloading file: " + e.getMessage());
            Config.exceptionHandler.handleException(e);
        }
    }

    /**
     * Checks if the user has the specified permission.
     *
     * @param permission The permission to check (e.g., "Download").
     * @return true if the user has the permission, false otherwise.
     * @throws Exception If a database error occurs.
     */
    private boolean hasPermission(String permission) throws Exception {
        String policyValue = Config.policyCheck.retrievePolicyValue(permission);
        return login.checkPrivilegeLogic() || "true".equalsIgnoreCase(policyValue);
    }

    /**
     * Validates the file name.
     *
     * @param fileName The file name to validate.
     * @return true if the file name is valid, false otherwise.
     */
    private boolean isValidFileName(String fileName) {
        return Config.io.checkFileValidity(fileName);
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