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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import Cataphract.API.Config;

/**
 * Utility class for path resolution and validation.
 *
 * @author DAK404 (https://github.com/DAK404)
 * @version 1.4.1 (13-July-2025, Cataphract)
 * @since 1.4.1 (Cataphract 1.4.1)
 */
public class PathUtils {

    public PathUtils() {
    }

    public Path resolveRelativePath(Path currentDir, String relativePath) {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            return currentDir;
        }
        Path resolved = currentDir.resolve(Config.io.convertFileSeparator(relativePath));
        if (!resolved.toString().startsWith(Config.USER_HOME)) {
            Config.io.printError("Access denied: Path outside user home directory.");
            return currentDir;
        }
        return resolved;
    }

    public boolean isValidPathName(String fileName) {
        return Config.io.checkFileValidity(fileName);
    }

    public Path getUserHomePath(String username) {
        return Paths.get(Config.USER_HOME + username + "|");
    }

    public Path getHelpFilePath(String helpFile) {
        if (!helpFile.endsWith(".hlp")) {
            helpFile += ".hlp";
        }
        return Paths.get(Config.io.convertFileSeparator(".|docs|Cataphract|Help|" + helpFile));
    }

    public Path getLogPath(String logFileName, boolean createIfAbsent) throws Exception {
        Path logDir = Paths.get(Config.io.convertFileSeparator(".|System|Cataphract|Public|Logs|"));
        if (!Files.exists(logDir)) {
            logDir = Paths.get(Config.io.convertFileSeparator(".|Logs|Cataphract|"));
            if (createIfAbsent) {
                Files.createDirectories(logDir);
            }
        }
        return logDir.resolve(logFileName + ".log");
    }
}