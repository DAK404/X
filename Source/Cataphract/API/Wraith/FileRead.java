package Cataphract.API.Wraith;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;

import Cataphract.API.Config;
import Cataphract.API.Dragon.Login;

/**
 * Handles file reading operations for the Cataphract shell.
 */
public class FileRead {
    private final Login login;
    private boolean helpMode = false;

    /**
     * Constructs a FileRead instance with dependencies.
     *
     * @param login        The login handler for privilege checks.
     * @param policyCheck  The policy checker for permission validation.
     * @param Config.io    The IO streams handler for path conversion and output.
     */
    public FileRead(Login login) {
        this.login = login;
    }

    /**
     * Executes the file read command.
     *
     * @param commandArray The command and its arguments (e.g., ["help"], ["read", "file.txt"]).
     * @throws Exception If the command execution fails.
     */
    public void execute(String[] commandArray) throws Exception {
        if (commandArray.length == 0) {
            Config.io.printError("Invalid syntax. Expected: help or read <file>");
            return;
        }

        boolean isHelpCommand = commandArray[0].equalsIgnoreCase("help");

        if (isHelpCommand) {
            helpMode = true;
            readFileLogic(Path.of(Config.io.convertFileSeparator(".|docs|Cataphract|Help|" + commandArray[1])));
            return;
        }

        if (commandArray.length < 2 || !commandArray[0].equalsIgnoreCase("read")) {
            Config.io.printError("Invalid syntax. Expected: read <file>");
            return;
        }

        String fileName = commandArray[1];

        if (!hasPermission("FileRead")) {
            Config.io.printError("Insufficient privileges to read files.");
            return;
        }

        Path filePath = resolvePath(fileName);

        if (!Files.exists(filePath)) {
            Config.io.printError("File does not exist: " + fileName);
            return;
        }

        if (Files.isDirectory(filePath)) {
            Config.io.printError("Cannot read a directory: " + fileName);
            return;
        }

        readFileLogic(filePath);
    }

    /**
     * Reads and outputs the contents of the specified file, with special handling for help files.
     *
     * @param filePath The path to the file to read.
     * @throws Exception If the read operation fails.
     */
    private void readFileLogic(Path filePath) throws Exception {
        try {
            if (!Config.io.checkFileValidity(filePath.getFileName().toString())) {
                Config.io.printError("Invalid File Name! Please Enter A Valid File Name.");
                return;
            }

            if (!Files.exists(filePath)) {
                Config.io.printError("The Specified File Does Not Exist. Please Enter A Valid File Name.");
                return;
            }

            boolean continueFileRead = true;
            Config.build.viewBuildInfo(false);

            try (BufferedReader bufferObject = new BufferedReader(new FileReader(filePath.toFile()))) {
                String fileContents;

                if (helpMode) {
                    while ((fileContents = bufferObject.readLine()) != null && continueFileRead) {
                        if (fileContents.equalsIgnoreCase("<end of page>")) {
                            String input = Config.io.confirmReturnToContinue("", "else type EXIT to quit Help Viewer.\n~DOC_HLP?> ");
                            if (input.equalsIgnoreCase("exit")) {
                                continueFileRead = false;
                            } else {
                                Config.build.viewBuildInfo(false);
                                continue;
                            }
                        } else if (fileContents.equalsIgnoreCase("<end of help>")) {
                            Config.io.println("\n\nEnd of Help File.");
                            break;
                        } else if (fileContents.startsWith("#")) {
                            continue;
                        } else {
                            Config.io.println(fileContents);
                        }
                    }
                } else {
                    while ((fileContents = bufferObject.readLine()) != null) {
                        Config.io.println(fileContents);
                    }
                }
            }

            Config.io.confirmReturnToContinue();
            System.gc();
        } catch (FileNotFoundException fnfe) {
            Config.io.printError("The specified file " + filePath + " does not exist.");
        } catch (Exception e) {
            Config.io.printError("An Error Occurred While Reading The File: " + e.getMessage());
            Config.exceptionHandler.handleException(e);
        }
    }

    /**
     * Checks if the user has the specified permission.
     *
     * @param permission The permission to check (e.g., "FileRead").
     * @return true if the user has the permission, false otherwise.
     * @throws Exception If a database error occurs.
     */
    private boolean hasPermission(String permission) throws Exception {
        String policyValue = Config.policyCheck.retrievePolicyValue(permission);
        return login.checkPrivilegeLogic() || "true".equalsIgnoreCase(policyValue);
    }

    /**
     * Resolves the file name to a path in the current directory for user files.
     *
     * @param fileName The file name (e.g., "file.txt").
     * @return The resolved path.
     */
    private Path resolvePath(String fileName) {
        return Path.of(System.getProperty("user.dir"), Config.io.convertFileSeparator(fileName));
    }
}
