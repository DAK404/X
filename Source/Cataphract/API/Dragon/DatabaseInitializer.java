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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import Cataphract.API.Config;

public final class DatabaseInitializer {
    private static final String DATABASE_PATH = DatabaseInitializer.getDatabasePath();
    private static final String CREATE_MUD_TABLE = 
        "CREATE TABLE IF NOT EXISTS MUD (" +
        "Username TEXT," +
        "Name TEXT NOT NULL," +
        "Password TEXT NOT NULL," +
        "SecurityKey TEXT NOT NULL," +
        "PIN TEXT NOT NULL," +
        "Privileges TEXT NOT NULL," +
        "PRIMARY KEY(Username));";

    private DatabaseInitializer() {} // Prevent instantiation

    /**
     * Initializes the Cataphract database, creating it if it doesn't exist.
     * @return true if initialization is successful or database already exists, false otherwise.
     */
    public static boolean initializeDatabase() {
        try {
            // Create directory for database if it doesn't exist
            new File(Config.io.convertFileSeparator(".|System|Cataphract|Private")).mkdirs();

            // Check if database already exists
            Config.io.printInfo("Checking for existing Master User Database...");
            if (new File(DATABASE_PATH.replace("jdbc:sqlite:", "")).exists()) {
                Config.io.printInfo("Master User Database already exists. Skipping initialization.");
                return true;
            }

            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");

            // Establish connection and create table
            try (Connection dbConnection = DriverManager.getConnection(DATABASE_PATH);
                 Statement statement = dbConnection.createStatement()) {
                statement.execute(CREATE_MUD_TABLE);
                Config.io.printInfo("Master User Database initialized successfully.");
                return true;
            }
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
            Config.io.printError("Failed to initialize Master User Database: " + e.getMessage());
            return false;
        } finally {
            System.gc(); // Encourage garbage collection
        }
    }

    /**
     * Provides the database connection URL for use by other classes.
     * @return The JDBC connection URL for the database.
     */
    public static String getDatabasePath() {
        return DATABASE_PATH;
    }
}