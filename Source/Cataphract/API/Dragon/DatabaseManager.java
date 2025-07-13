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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import Cataphract.API.Config;

/**
 * Utility class to manage database operations for the Cataphract shell.
 *
 * @author DAK404 (https://github.com/DAK404)
 * @version 4.0.0 (12-July-2025, Cataphract)
 */
public final class DatabaseManager {
    private static Connection connection;

    private DatabaseManager() {
        // Private constructor to prevent instantiation
    }

    /**
     * Gets a database connection, reusing an existing one if available.
     *
     * @return The database connection.
     * @throws SQLException If a database error occurs.
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection("jdbc:sqlite:" + Config.DB_PATH);
            } catch (ClassNotFoundException e) {
                throw new SQLException("JDBC driver not found: " + e.getMessage());
            }
        }
        return connection;
    }

    /**
     * Closes the database connection.
     *
     * @throws SQLException If an error occurs while closing.
     */
    public static void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    /**
     * Executes an update query (INSERT, UPDATE, DELETE).
     *
     * @param sql The SQL command.
     * @param params The parameters to set in the prepared statement.
     * @return true if the update succeeds, false otherwise.
     */
    public static boolean executeUpdate(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            Config.io.printError("Database Update Failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves a single value from the database.
     *
     * @param sql The SQL query.
     * @param column The column name to retrieve.
     * @param params The parameters to set in the prepared statement.
     * @return The retrieved value, or "Error" if not found.
     */
    public static String retrieveSingleValue(String sql, String column, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String result = rs.getString(column);
                    return result != null ? result : "Error";
                }
            }
        } catch (SQLException e) {
            Config.io.printError("Database Query Failed: " + e.getMessage());
        }
        return "Error";
    }
}