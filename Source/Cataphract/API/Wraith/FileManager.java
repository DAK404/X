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

package Cataphract.API.Wraith;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import Cataphract.API.Config;
import Cataphract.API.Dragon.AuthInputHelper;
import Cataphract.API.Dragon.Login;
import Cataphract.API.Wraith.Archive.ZipArchiveHandler;

/**
 * Manages file operations and command processing for Cataphract.
 *
 * @author DAK404 (https://github.com/DAK404)
 * @version 1.4.1 (13-July-2025, Cataphract)
 * @since 0.0.1 (Cataphract 0.0.1)
 */
public class FileManager implements FileOperationHandler {
    private final String username;
    private final String name;
    private final Path userHomeDirectory;
    private Path presentWorkingDirectory;
    private final Login login;
    private final PathUtils pathUtils;
    private final CommandInterpreter interpreter;

    public FileManager(String username, Login login, FileDownloader fileDownloader) throws Exception {
        this.username = username;
        this.login = login;
        this.pathUtils = new PathUtils();
        this.userHomeDirectory = pathUtils.getUserHomePath(username);
        this.presentWorkingDirectory = userHomeDirectory;
        this.interpreter = new CommandInterpreter(this, new ZipArchiveHandler(username), fileDownloader);

        String tempName;
        try {
            tempName = login.getNameLogic();
        } catch (Exception e) {
            Config.io.printError("Failed to retrieve user name: " + e.getMessage());
            tempName = username;
            Config.exceptionHandler.handleException(e);
        }
        this.name = tempName;
    }

    @Override
    public Path getCurrentDirectory() {
        return presentWorkingDirectory;
    }

    @Override
    public void copy(Path source, Path destination) throws Exception {
        try {
            source = pathUtils.resolveRelativePath(presentWorkingDirectory, source.toString());
            destination = pathUtils.resolveRelativePath(presentWorkingDirectory, destination.toString());
            if (!Files.exists(source)) {
                Config.io.printError("Source does not exist: " + source);
                return;
            }
            copyMoveHelper(source, destination, false);
            Config.io.printInfo("Copied " + source + " to " + destination);
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
        }
    }

    @Override
    public void move(Path source, Path destination) throws Exception {
        try {
            source = pathUtils.resolveRelativePath(presentWorkingDirectory, source.toString());
            destination = pathUtils.resolveRelativePath(presentWorkingDirectory, destination.toString());
            if (!Files.exists(source)) {
                Config.io.printError("Source does not exist: " + source);
                return;
            }
            copyMoveHelper(source, destination, true);
            Config.io.printInfo("Moved " + source + " to " + destination);
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
        }
    }

    @Override
    public void delete(Path path) throws Exception {
        try {
            path = pathUtils.resolveRelativePath(presentWorkingDirectory, path.toString());
            if (!Files.exists(path)) {
                Config.io.printError("Path does not exist: " + path);
                return;
            }
            deleteEntityHelper(path);
            Config.io.printInfo("Deleted: " + path);
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
        }
    }

    @Override
    public void makeDirectory(Path path) throws Exception {
        try {
            path = pathUtils.resolveRelativePath(presentWorkingDirectory, path.toString());
            if (!pathUtils.isValidPathName(path.getFileName().toString())) {
                Config.io.printError("Invalid directory name: " + path.getFileName());
                return;
            }
            Files.createDirectories(path);
            Config.io.printInfo("Created directory: " + path);
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
        }
    }

    @Override
    public void rename(Path source, Path newName) throws Exception {
        try {
            source = pathUtils.resolveRelativePath(presentWorkingDirectory, source.toString());
            newName = pathUtils.resolveRelativePath(presentWorkingDirectory, newName.toString());
            if (!Files.exists(source)) {
                Config.io.printError("Source does not exist: " + source);
                return;
            }
            if (Files.exists(newName)) {
                Config.io.printError("Destination already exists: " + newName);
                return;
            }
            Files.move(source, newName, StandardCopyOption.REPLACE_EXISTING);
            Config.io.printInfo("Renamed " + source + " to " + newName);
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
        }
    }

    @Override
    public void listEntities(Path path) throws Exception {
        try {
            path = pathUtils.resolveRelativePath(presentWorkingDirectory, path.toString());
            if (!Files.exists(path)) {
                Config.io.printError("Path does not exist: " + path);
                return;
            }
            String format = "%1$-32s| %2$-24s| %3$-10s| %4$-32s\n";
            String separator = "-".repeat(100);
            Config.io.println("\n" + String.format(format, "Name", "Size [KB]", "Type", "MD5 Hash") + separator);

            Files.list(path).forEach(p -> {
                try {
                    String displayName = p.getFileName().toString().replace(username, name);
                    String size = Files.isDirectory(p) ? "" : (Files.size(p) / 1024 + " KB");
                    String type = Files.isDirectory(p) ? "Directory" : "File";
                    String md5 = Files.isDirectory(p) ? "" : Config.cryptography.fileToMD5(p.toFile());
                    Config.io.println(String.format(format, displayName, size, type, md5));
                } catch (Exception e) {
                    Config.io.printError("Error listing: " + p.getFileName());
                    Config.exceptionHandler.handleException(e);
                }
            });
            Config.io.println("");
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
        }
    }

    @Override
    public void viewDirectoryTree(Path path) throws Exception {
        try {
            path = pathUtils.resolveRelativePath(presentWorkingDirectory, path.toString());
            if (!Files.exists(path)) {
                Config.io.printError("Path does not exist: " + path);
                return;
            }
            Config.io.println("\n--- [ TREE VIEW ] ---\n");
            viewDirTreeHelper(0, path);
            Config.io.println("");
        }
 catch (Exception e) {
            Config.exceptionHandler.handleException(e);
        }
    }

    @Override
    public void changeDirectory(Path destination) throws Exception {
        try {
            if (destination == null || destination.toString().equals("..")) {
                Path parent = presentWorkingDirectory.getParent();
                Path root = pathUtils.getUserHomePath("");
                if (parent == null || parent.equals(root)) {
                    Config.io.printError("Permission Denied.");
                    resetToHomeDirectory();
                    return;
                }
                presentWorkingDirectory = parent;
            } else {
                Path newPath = pathUtils.resolveRelativePath(presentWorkingDirectory, destination.toString());
                if (!Files.exists(newPath) || !Files.isDirectory(newPath)) {
                    Config.io.printError("'" + destination + "' does not exist or is not a directory");
                    return;
                }
                presentWorkingDirectory = newPath;
            }
            Config.io.printInfo("Changed directory to: " + presentWorkingDirectory);
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
        }
    }

    @Override
    public void resetToHomeDirectory() {
        try {
            presentWorkingDirectory = userHomeDirectory;
            Config.io.printInfo("Reset to home directory: " + userHomeDirectory);
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
        }
    }

    public void fileManagementLogic() throws Exception {
        try {
            if (!canPerformFileManagement()) {
                Config.io.printError("Policy Management System - Permission Denied.");
                return;
            }
            if (!authenticate()) {
                Config.io.printError("Invalid Credentials.");
                return;
            }
            String prompt = name + "@" + Config.io.convertFileSeparator(presentWorkingDirectory.toString()).replace(username, name) + "> ";
            String input;
            do {
                input = Config.console.readLine(prompt);
                interpreter.interpret(input);
            } while (!input.equalsIgnoreCase("exit"));
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
        }
    }

    public void fileManagementLogic(String scriptFileName) throws Exception {
        try {
            if (!canPerformScriptExecution()) {
                Config.io.printError("Policy Management System - Permission Denied.");
                return;
            }
            Path scriptPath = pathUtils.resolveRelativePath(presentWorkingDirectory, scriptFileName + ".fmx");
            if (!pathUtils.isValidPathName(scriptFileName) || Files.isDirectory(scriptPath) || !Files.exists(scriptPath)) {
                Config.io.printError("Invalid Script File: " + scriptFileName);
                return;
            }
            if (!authenticate()) {
                Config.io.printError("Invalid Credentials.");
                return;
            }
            try (BufferedReader br = new BufferedReader(new java.io.FileReader(scriptPath.toFile()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith("#") || line.trim().isEmpty()) {
                        continue;
                    }
                    if (line.equalsIgnoreCase("End Script") || line.equalsIgnoreCase("End Grinch")) {
                        break;
                    }
                    interpreter.interpret(line);
                }
            }
            Config.io.printInfo("Script execution completed: " + scriptFileName);
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
        }
    }

    private boolean authenticate() throws Exception {
        try {
            String[] credentials = AuthInputHelper.readCredentials(Config.console);
            if (credentials == null) {
                Config.io.printError("Invalid username.");
                return false;
            }
            return login.authenticationLogic(credentials[1], credentials[2]);
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
            return false;
        }
    }

    private boolean canPerformFileManagement() throws Exception {
        try {
            return Config.policyCheck.retrievePolicyValue("filemgmt").equals("on") || login.checkPrivilegeLogic();
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
            return false;
        }
    }

    private boolean canPerformScriptExecution() throws Exception {
        try {
            return (Config.policyCheck.retrievePolicyValue("filemgmt").equals("on") && 
                    Config.policyCheck.retrievePolicyValue("script").equals("on")) || 
                   login.checkPrivilegeLogic();
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
            return false;
        }
    }

    private void copyMoveHelper(Path source, Path destination, boolean move) throws Exception {
        if (Files.isDirectory(source)) {
            Files.createDirectories(destination);
            try (var stream = Files.list(source)) {
                stream.forEach(child -> {
                    try {
                        copyMoveHelper(child, destination.resolve(child.getFileName()), move);
                    } catch (Exception e) {
                        Config.io.printError("Error processing: " + child.getFileName());
                        Config.exceptionHandler.handleException(e);
                    }
                });
            }
        } else {
            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
            if (move) {
                Files.delete(source);
            }
        }
    }

    private void deleteEntityHelper(Path path) throws Exception {
        if (Files.isDirectory(path)) {
            try (var stream = Files.list(path)) {
                stream.forEach(child -> {
                    try {
                        deleteEntityHelper(child);
                    } catch (Exception e) {
                        Config.io.printError("Error deleting: " + child.getFileName());
                        Config.exceptionHandler.handleException(e);
                    }
                });
            }
        }
        Files.delete(path);
    }

    private void viewDirTreeHelper(int indent, Path path) {
        try {
            Config.io.print("|");
            for (int i = 0; i < indent; i++) {
                Config.io.print("-");
            }
            String displayName = path.getFileName().toString().replace(username, name + " [ USER HOME DIRECTORY ]");
            Config.io.println(displayName);
            if (Files.isDirectory(path)) {
                try (var stream = Files.list(path)) {
                    stream.forEach(child -> viewDirTreeHelper(indent + 2, child));
                } catch (Exception e) {
                    Config.io.printError("Error listing directory: " + path.getFileName());
                    Config.exceptionHandler.handleException(e);
                }
            }
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
        }
    }
}