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