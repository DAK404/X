package Cataphract.API.Wraith;

import java.nio.file.Path;

/**
 * Interface for file downloading operations.
 *
 * @author DAK404 (https://github.com/DAK404)
 * @version 1.4.1 (13-July-2025, Cataphract)
 * @since 0.0.1 (Cataphract 0.0.1)
 */
public interface FileDownloader {
    void downloadFile(String url, Path destination) throws Exception;
    void downloadUpdate() throws Exception;
}