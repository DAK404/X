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

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import Cataphract.API.Config;
import Cataphract.API.Wraith.Archive.ZipArchiveHandler;

/**
 * Interprets and executes file management commands.
 *
 * @author DAK404 (https://github.com/DAK404)
 * @version 1.4.1 (13-July-2025, Cataphract)
 * @since 0.0.1 (Cataphract 0.0.1)
 */
public class CommandInterpreter {
    private final FileOperationHandler fileOps;
    private final ZipArchiveHandler zipHandler;
    private final Cataphract.API.Wraith.FileDownloader fileDownloader;
    private final PathUtils pathUtils;
    private final Map<String, CommandAction> commands;

    @FunctionalInterface
    private interface CommandAction {
        void execute(String[] args) throws Exception;
    }

    public CommandInterpreter(FileOperationHandler fileOps, ZipArchiveHandler zipHandler,
                             Cataphract.API.Wraith.FileDownloader fileDownloader) {
        this.fileOps = fileOps;
        this.zipHandler = zipHandler;
        this.fileDownloader = fileDownloader;
        this.pathUtils = new PathUtils();
        this.commands = initializeCommands();
    }

    public void interpret(String command) throws Exception {
        if (command == null || command.trim().isEmpty() || command.equalsIgnoreCase("exit")) {
            return;
        }

        String[] commandArray = Config.io.splitStringToArray(command);
        if (commandArray.length == 0) {
            return;
        }

        CommandAction action = commands.getOrDefault(commandArray[0].toLowerCase(),
                args -> Config.io.printError("Unknown command: " + args[0]));
        action.execute(commandArray);
    }

    private Map<String, CommandAction> initializeCommands() {
        Map<String, CommandAction> cmdMap = new HashMap<>();
        cmdMap.put("move", this::handleMove);
        cmdMap.put("mov", this::handleMove);
        cmdMap.put("mv", this::handleMove);
        cmdMap.put("cut", this::handleMove);
        cmdMap.put("copy", this::handleCopy);
        cmdMap.put("cp", this::handleCopy);
        cmdMap.put("delete", this::handleDelete);
        cmdMap.put("del", this::handleDelete);
        cmdMap.put("rm", this::handleDelete);
        cmdMap.put("rename", this::handleRename);
        cmdMap.put("mkdir", this::handleMkdir);
        cmdMap.put("edit", this::handleEdit);
        cmdMap.put("read", this::handleRead);
        cmdMap.put("pwd", args -> Config.io.println(fileOps.getCurrentDirectory().toString()));
        cmdMap.put("cd", this::handleCd);
        cmdMap.put("cd..", args -> fileOps.changeDirectory(null));
        cmdMap.put("tree", args -> fileOps.viewDirectoryTree(fileOps.getCurrentDirectory()));
        cmdMap.put("dir", args -> fileOps.listEntities(fileOps.getCurrentDirectory()));
        cmdMap.put("ls", args -> fileOps.listEntities(fileOps.getCurrentDirectory()));
        cmdMap.put("download", this::handleDownload);
        cmdMap.put("home", args -> fileOps.resetToHomeDirectory());
        cmdMap.put("zip", this::handleZip);
        cmdMap.put("unzip", this::handleUnzip);
        return cmdMap;
    }

    private void handleMove(String[] args) throws Exception {
        if (args.length < 3) {
            Config.io.printError("Invalid Syntax: move <source> <destination>");
            return;
        }
        Path source = pathUtils.resolveRelativePath(fileOps.getCurrentDirectory(), args[1]);
        Path destination = pathUtils.resolveRelativePath(fileOps.getCurrentDirectory(), args[2]);
        fileOps.move(source, destination);
    }

    private void handleCopy(String[] args) throws Exception {
        if (args.length < 3) {
            Config.io.printError("Invalid Syntax: copy <source> <destination>");
            return;
        }
        Path source = pathUtils.resolveRelativePath(fileOps.getCurrentDirectory(), args[1]);
        Path destination = pathUtils.resolveRelativePath(fileOps.getCurrentDirectory(), args[2]);
        fileOps.copy(source, destination);
    }

    private void handleDelete(String[] args) throws Exception {
        if (args.length < 2) {
            Config.io.printError("Invalid Syntax: delete <path>");
            return;
        }
        Path path = pathUtils.resolveRelativePath(fileOps.getCurrentDirectory(), args[1]);
        fileOps.delete(path);
    }

    private void handleRename(String[] args) throws Exception {
        if (args.length < 3) {
            Config.io.printError("Invalid Syntax: rename <source> <newName>");
            return;
        }
        Path source = pathUtils.resolveRelativePath(fileOps.getCurrentDirectory(), args[1]);
        Path newName = pathUtils.resolveRelativePath(fileOps.getCurrentDirectory(), args[2]);
        fileOps.rename(source, newName);
    }

    private void handleMkdir(String[] args) throws Exception {
        if (args.length < 2) {
            Config.io.printError("Invalid Syntax: mkdir <directory>");
            return;
        }
        Path path = pathUtils.resolveRelativePath(fileOps.getCurrentDirectory(), args[1]);
        fileOps.makeDirectory(path);
    }

    private void handleEdit(String[] args) throws Exception {
        if (args.length < 2) {
            Config.io.printError("Invalid Syntax: edit <file>");
            return;
        }
        Path filePath = pathUtils.resolveRelativePath(fileOps.getCurrentDirectory(), args[1]);
        Config.fileWriter.editFile(filePath.getFileName().toString(), filePath.getParent());
    }

    private void handleRead(String[] args) throws Exception {
        if (args.length < 2) {
            Config.io.printError("Invalid Syntax: read <file>");
            return;
        }
        Path filePath = pathUtils.resolveRelativePath(fileOps.getCurrentDirectory(), args[1]);
        Config.fileReader.readUserFile(filePath);
    }

    private void handleDownload(String[] args) throws Exception {
        if (args.length < 3) {
            Config.io.printError("Invalid Syntax: download <url> <destination>");
            return;
        }
        Path destination = pathUtils.resolveRelativePath(fileOps.getCurrentDirectory(), args[2]);
        fileDownloader.downloadFile(args[1], destination);
    }

    private void handleZip(String[] args) throws Exception {
        if (args.length < 3) {
            Config.io.printError("Invalid Syntax: zip <archiveName> <source>");
            return;
        }
        Path archiveName = pathUtils.resolveRelativePath(fileOps.getCurrentDirectory(), args[1]);
        Path source = pathUtils.resolveRelativePath(fileOps.getCurrentDirectory(), args[2]);
        zipHandler.compress(source, archiveName);
    }

    private void handleUnzip(String[] args) throws Exception {
        if (args.length < 3) {
            Config.io.printError("Invalid Syntax: unzip <archive> <destination>");
            return;
        }
        Path archive = pathUtils.resolveRelativePath(fileOps.getCurrentDirectory(), args[1]);
        Path destination = pathUtils.resolveRelativePath(fileOps.getCurrentDirectory(), args[2]);
        zipHandler.decompress(archive, destination);
    }

    private void handleCd(String[] args) throws Exception {
        if (args.length < 2) {
            Config.io.printError("Invalid Syntax: cd <directory>");
            return;
        }
        Path destination = pathUtils.resolveRelativePath(fileOps.getCurrentDirectory(), args[1]);
        fileOps.changeDirectory(destination);
    }
}