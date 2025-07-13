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

package Cataphract.API.Dragon;

import java.io.File;

import Cataphract.API.Config;

/**
 * Utility class for file and directory operations.
 *
 * @author DAK404 (https://github.com/DAK404)
 * @version 4.0.0 (12-July-2025, Cataphract)
 */
public final class FileManager 
{
    private FileManager() {
        // Private constructor to prevent instantiation
    }

    /**
     * Creates a user directory.
     *
     * @param username The username for the directory.
     * @return true if successful, false otherwise.
     */
    public static boolean createUserDirectory(String username) {
        return new File(Config.io.convertFileSeparator(Config.USER_HOME + username)).mkdirs();
    }

    /**
     * Deletes a directory recursively.
     *
     * @param directory The directory to delete.
     * @return true if successful, false otherwise.
     */
    public static boolean deleteDirectory(File directory) {
        try {
            if (directory.isDirectory()) {
                for (File file : directory.listFiles()) {
                    deleteDirectory(file);
                }
            }
            return directory.delete();
        } catch (Exception e) {
            Config.io.printError("Directory Deletion Failed: " + e.getMessage());
            return false;
        }
    }
}