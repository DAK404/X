package Cataphract.API.Wraith.Archive;

import java.nio.file.Path;

/**
 * Interface for archive operations (e.g., zip, unzip).
 *
 * @author DAK404 (https://github.com/DAK404)
 * @version 1.2.0 (13-July-2025, Cataphract)
 * @since 0.0.1 (Cataphract 0.0.1)
 */
public interface ArchiveHandler {
    /**
     * Compresses a directory or file into an archive.
     *
     * @param sourcePath      Path to the source file or directory.
     * @param destinationPath Path to the output archive file.
     * @throws Exception If an error occurs during compression.
     */
    void compress(Path sourcePath, Path destinationPath) throws Exception;

    /**
     * Decompresses an archive to the specified destination.
     *
     * @param archivePath     Path to the archive file.
     * @param destinationPath Path to the output directory.
     * @throws Exception If an error occurs during decompression.
     */
    void decompress(Path archivePath, Path destinationPath) throws Exception;

    /**
     * Installs an update by decompressing a predefined archive.
     *
     * @throws Exception If an error occurs during update installation.
     */
    void installUpdate() throws Exception;
}