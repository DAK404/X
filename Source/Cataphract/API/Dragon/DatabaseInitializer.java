package Cataphract.API.Dragon;

import Cataphract.API.IOStreams;
import Cataphract.API.ExceptionHandler;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

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
            new File(IOStreams.convertFileSeparator(".|System|Cataphract|Private")).mkdirs();

            // Check if database already exists
            IOStreams.printInfo("Checking for existing Master User Database...");
            if (new File(DATABASE_PATH.replace("jdbc:sqlite:", "")).exists()) {
                IOStreams.printInfo("Master User Database already exists. Skipping initialization.");
                return true;
            }

            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");

            // Establish connection and create table
            try (Connection dbConnection = DriverManager.getConnection(DATABASE_PATH);
                 Statement statement = dbConnection.createStatement()) {
                statement.execute(CREATE_MUD_TABLE);
                IOStreams.printInfo("Master User Database initialized successfully.");
                return true;
            }
        } catch (Exception e) {
            new ExceptionHandler().handleException(e);
            IOStreams.printError("Failed to initialize Master User Database: " + e.getMessage());
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