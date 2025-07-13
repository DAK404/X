package Cataphract.API.Wraith;

import java.nio.file.Path;

/**
 * Interface for file and directory operations.
 *
 * @author DAK404 (https://github.com/DAK404)
 * @version 1.4.1 (13-July-2025, Cataphract)
 * @since 0.0.1 (Cataphract 0.0.1)
 */
public interface FileOperationHandler {
    Path getCurrentDirectory();
    void copy(Path source, Path destination) throws Exception;
    void move(Path source, Path destination) throws Exception;
    void delete(Path path) throws Exception;
    void makeDirectory(Path path) throws Exception;
    void rename(Path source, Path newName) throws Exception;
    void listEntities(Path path) throws Exception;
    void viewDirectoryTree(Path path) throws Exception;
    void changeDirectory(Path destination) throws Exception;
    void resetToHomeDirectory();
}