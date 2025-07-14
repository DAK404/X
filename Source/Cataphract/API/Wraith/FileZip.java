package Cataphract.API.Wraith;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import Cataphract.API.Config;
import Cataphract.API.IOStreams;
import Cataphract.API.Minotaur.PolicyCheck;
import Cataphract.API.Dragon.Login;

/**
 * Handles file zipping operations for the Cataphract shell.
 * Implements IFileOperation to provide a standardized interface for zip commands.
 */
public class FileZip implements IFileOperation {
    private final Login login;
    private final PolicyCheck policyCheck;
    private final IOStreams ioStreams;

    /**
     * Constructs a FileZip instance with dependencies.
     *
     * @param login        The login handler for privilege checks.
     * @param policyCheck  The policy checker for permission validation.
     * @param ioStreams    The IO streams handler for path conversion and output.
     */
    public FileZip(Login login, PolicyCheck policyCheck, IOStreams ioStreams) {
        this.login = login;
        this.policyCheck = policyCheck;
        this.ioStreams = ioStreams;
    }

    /**
     * Executes the file zip command.
     *
     * @param commandArray The command and its arguments (e.g., ["zip", "archive.zip", "file1", "file2"]).
     * @throws Exception If the command execution fails.
     */
    @Override
    public void execute(String[] commandArray) throws Exception {
        if (commandArray.length < 3) {
            ioStreams.printError("Invalid syntax. Expected: zip <archive> <file1> [file2 ...]");
            return;
        }

        String archive = commandArray[1];

        if (!hasPermission("FileZip")) {
            ioStreams.printError("Insufficient privileges to zip files.");
            return;
        }

        Path archivePath = resolvePath(archive);
        if (!isValidFileName(archive)) {
            ioStreams.printError("Invalid archive name: " + archive);
            return;
        }

        String[] files = new String[commandArray.length - 2];
        System.arraycopy(commandArray, 2, files, 0, files.length);
        zipFiles(archivePath, files);
    }

    /**
     * Zips the specified files into the archive.
     *
     * @param archivePath The path to the output zip archive.
     * @param fileNames   The names of the files to zip.
     * @throws Exception If the zip operation fails.
     */
    private void zipFiles(Path archivePath, String[] fileNames) throws Exception {
        try (var zipStream = new ZipOutputStream(Files.newOutputStream(archivePath))) {
            for (String fileName : fileNames) {
                Path filePath = resolvePath(fileName);
                if (!Files.exists(filePath)) {
                    ioStreams.printError("File does not exist: " + fileName);
                    continue;
                }
                addToZip(filePath, zipStream);
            }
            ioStreams.println("Created zip archive: " + archivePath);
        } catch (Exception e) {
            ioStreams.printError("Error creating zip archive: " + e.getMessage());
            Config.exceptionHandler.handleException(e);
        }
    }

    /**
     * Adds a file or directory to the zip archive.
     *
     * @param filePath   The path to the file or directory to add.
     * @param zipStream  The zip output stream.
     * @throws Exception If the operation fails.
     */
    private void addToZip(Path filePath, ZipOutputStream zipStream) throws Exception {
        String entryName = ioStreams.convertToNionSeparator(filePath.getFileName().toString());
        if (Files.isDirectory(filePath)) {
            try (var stream = Files.walk(filePath)) {
                stream.filter(path -> !Files.isDirectory(path)).forEach(path -> {
                    try {
                        String relativePath = ioStreams.convertToNionSeparator(filePath.getParent().relativize(path).toString());
                        ZipEntry entry = new ZipEntry(relativePath);
                        zipStream.putNextEntry(entry);
                        Files.copy(path, zipStream);
                        zipStream.closeEntry();
                    } catch (Exception e) {
                        ioStreams.printError("Error adding " + path + " to zip: " + e.getMessage());
                    }
                });
            }
        } else {
            ZipEntry entry = new ZipEntry(entryName);
            zipStream.putNextEntry(entry);
            Files.copy(filePath, zipStream);
            zipStream.closeEntry();
        }
    }

    /**
     * Checks if the user has the specified permission.
     *
     * @param permission The permission to check (e.g., "FileZip").
     * @return true if the user has the permission, false otherwise.
     * @throws Exception If a database error occurs.
     */
    private boolean hasPermission(String permission) throws Exception {
        String policyValue = policyCheck.retrievePolicyValue(permission);
        return login.checkPrivilegeLogic() || "true".equalsIgnoreCase(policyValue);
    }

    /**
     * Validates the file name.
     *
     * @param fileName The file name to validate.
     * @return true if the file name is valid, false otherwise.
     */
    private boolean isValidFileName(String fileName) {
        return ioStreams.checkFileValidity(fileName);
    }

    /**
     * Resolves the file name to a path within the current directory.
     *
     * @param fileName The file name.
     * @return The resolved path.
     */
    private Path resolvePath(String fileName) {
        return Path.of(System.getProperty("user.dir"), ioStreams.convertFileSeparator(fileName));
    }
}