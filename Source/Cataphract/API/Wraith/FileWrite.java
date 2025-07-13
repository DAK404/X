package Cataphract.API.Wraith;

import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import Cataphract.API.Config;
import Cataphract.API.Dragon.Login;

/**
 * Implementation of FileWriter interface for editing files and logging messages.
 *
 * @author DAK404[](https://github.com/DAK404)
 * @version 1.4.2 (13-July-2025, Cataphract)
 * @since 0.0.1 (Cataphract 0.0.1)
 */
public class FileWrite implements FileWriter {
    private final PathUtils pathUtils;

    public FileWrite() {
        this.pathUtils = new PathUtils();
    }

    @Override
    public void editFile(String fileName, Path directory, String username) throws Exception {
        if (username == null) {
            Config.io.printError("Username is required to edit files.");
            return;
        }
        if (!canWriteFile(username)) {
            Config.io.printError("Policy Management System - Permission Denied.");
            return;
        }
        Path filePath = pathUtils.resolveRelativePath(directory, fileName);
        if (!pathUtils.isValidPathName(fileName)) {
            Config.io.printError("Invalid file name: " + fileName);
            return;
        }
        if (!Files.exists(directory)) {
            Config.io.printError("Directory does not exist: " + directory);
            return;
        }

        boolean append = true;
        if (Files.exists(filePath)) {
            String choice = Config.console.readLine(
                "[ ATTENTION ] : A file with the same name has been found in this directory. Do you want to OVERWRITE it, APPEND to the file, or GO BACK? \n\nOptions:\n[ OVERWRITE | APPEND | RETURN | HELP ]\n\n> "
            ).toLowerCase();
            switch (choice) {
                case "overwrite":
                    append = false;
                    Config.io.printAttention("The new content will overwrite the previous content present in the file!");
                    break;
                case "append":
                    Config.io.println("The new content will be added to the end of the file! Previous data will remain unchanged.");
                    break;
                case "help":
                    new FileRead().readHelpFile("edit");
                    return;
                case "return":
                default:
                    Config.io.println("Operation cancelled.");
                    return;
            }
        }

        Config.io.println("Wraith Text Editor 1.5");
        Config.io.println("______________________\n");
        Config.io.println("\nEditing File: " + fileName + "\n");

        try (BufferedWriter writer = new BufferedWriter(new java.io.FileWriter(filePath.toFile(), append));
             PrintWriter pr = new PrintWriter(writer)) {
            String input;
            do {
                input = Config.console.readLine();
                if (!input.equals("<exit>")) {
                    pr.println(input);
                }
            } while (!input.equals("<exit>"));
        } catch (Exception e) {
            Config.io.printError("Error writing to file: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void log(String message, String logFileName) throws Exception {
        Path logPath = pathUtils.getLogPath(logFileName, true);
        if (!pathUtils.isValidPathName(logFileName)) {
            Config.io.printError("Invalid log file name: " + logFileName);
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new java.io.FileWriter(logPath.toFile(), true));
             PrintWriter pr = new PrintWriter(writer)) {
            pr.println(Config.time.getDateTimeUsingSpecifiedFormat("dd-MMMM-yyyy HH:mm:ss") + " (" +
                       Config.time.getUnixEpoch() + "): " + message);
        } catch (Exception e) {
            Config.io.printError("Error writing to log: " + e.getMessage());
            throw e;
        }
    }

    private boolean canWriteFile(String username) throws Exception 
    {
        return Config.policyCheck.retrievePolicyValue("filewrite").equals("on") || new Login(username).checkPrivilegeLogic();
    }
}