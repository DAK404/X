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

import Cataphract.API.Config;
import Cataphract.API.Dragon.Login;

/**
 * Implementation of FileReader interface for reading user and help files.
 *
 * @author DAK404 (https://github.com/DAK404)
 * @version 1.4.1 (13-July-2025, Cataphract)
 * @since 0.0.1 (Cataphract 0.0.1)
 */
public class FileRead implements FileReader {
    private final PathUtils pathUtils;
    private final boolean isUserAdmin;

    public FileRead(String username) throws Exception {
        this.pathUtils = new PathUtils();
        this.isUserAdmin = new Login(username).checkPrivilegeLogic();
    }

    @Override
    public void readUserFile(Path filePath) throws Exception {
        if (!canReadFile()) {
            Config.io.printError("Policy Management System - Permission Denied.");
            return;
        }
        readFile(filePath, false);
    }

    @Override
    public void readHelpFile(String helpFile) throws Exception {
        Path helpPath = pathUtils.getHelpFilePath(helpFile);
        readFile(helpPath, true);
    }

    private void readFile(Path filePath, boolean helpMode) throws Exception {
        if (!pathUtils.isValidPathName(filePath.getFileName().toString())) {
            Config.io.printError("Invalid file name: " + filePath.getFileName());
            return;
        }
        if (!Files.exists(filePath)) {
            Config.io.printError("File does not exist: " + filePath);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new java.io.FileReader(filePath.toFile()))) {
            if (helpMode) {
                Config.build.viewBuildInfo(false);
                boolean continueReading = true;
                String line;
                while ((line = reader.readLine()) != null && continueReading) {
                    if (line.equalsIgnoreCase("<end of page>")) {
                        String input = Config.io.confirmReturnToContinue("", "else type EXIT to quit Help Viewer.\n~DOC_HLP?> ");
                        if (input.equalsIgnoreCase("exit")) {
                            continueReading = false;
                        } else {
                            Config.build.viewBuildInfo(false);
                        }
                    } else if (line.equalsIgnoreCase("<end of help>")) {
                        Config.io.println("\n\nEnd of Help File.");
                        break;
                    } else if (!line.startsWith("#")) {
                        Config.io.println(line);
                    }
                }
            } else {
                reader.lines().forEach(Config.io::println);
            }
            Config.io.confirmReturnToContinue();
        } catch (Exception e) {
            Config.io.printError("Error reading file: " + e.getMessage());
            throw e;
        }
    }

    private boolean canReadFile() throws Exception {
        return Config.policyCheck.retrievePolicyValue("fileread").equals("on") || isUserAdmin;
    }
}