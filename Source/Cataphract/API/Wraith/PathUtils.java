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