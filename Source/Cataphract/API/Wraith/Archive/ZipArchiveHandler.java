package Cataphract.API.Wraith.Archive;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import Cataphract.API.Config;
import Cataphract.API.Dragon.Login;
import Cataphract.API.Wraith.FileWriter;

/**
 * Implementation of ArchiveHandler for ZIP operations.
 *
 * @author DAK404 (https://github.com/DAK404)
 * @version 1.2.0 (13-July-2025, Cataphract)
 * @since 0.0.1 (Cataphract 0.0.1)
 */
public class ZipArchiveHandler implements ArchiveHandler {
    private static final int BUFFER_SIZE = 8192;
    private static final String UPDATE_ARCHIVE_PATH = ".|Update.zip";
    private final boolean isUserAdmin;
    private final FileWriter fileWrite;

    public ZipArchiveHandler(String username, FileWriter fileWrite) throws Exception {
        this.isUserAdmin = new Login(username).checkPrivilegeLogic();
        this.fileWrite = fileWrite;
    }

    @Override
    public void compress(Path sourcePath, Path destinationPath) throws Exception {
        if (!canPerformOperation("filemgmt")) {
            Config.io.printError("Policy Management System - Permission Denied.");
            return;
        }

        if (!Config.io.checkFileValidity(destinationPath.getFileName().toString())) {
            Config.io.printError("Invalid archive file name: " + destinationPath.getFileName());
            return;
        }

        try (FileOutputStream fos = new FileOutputStream(destinationPath.toFile());
             ZipOutputStream zipOut = new ZipOutputStream(fos)) {
            compressFile(sourcePath.toFile(), sourcePath.getFileName().toString(), zipOut);
            Config.io.printInfo("Successfully compressed to: " + destinationPath);
        } catch (Exception e) {
            Config.io.printError("Compression failed: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void decompress(Path archivePath, Path destinationPath) throws Exception {
        if (!canPerformOperation("filemgmt")) {
            Config.io.printError("Policy Management System - Permission Denied.");
            return;
        }

        decompressZip(archivePath, destinationPath, false);
    }

    @Override
    public void installUpdate() throws Exception {
        if (!canPerformOperation("update")) {
            Config.io.printError("Policy Management System - Permission Denied.");
            return;
        }

        Path archivePath = Path.of(Config.io.convertFileSeparator(UPDATE_ARCHIVE_PATH));
        decompressZip(archivePath, archivePath.getParent(), true);
    }

    private boolean canPerformOperation(String policyKey) {
        return Config.policyCheck.retrievePolicyValue(policyKey).equals("on") || isUserAdmin;
    }

    private void compressFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws Exception {
        if (!Config.io.checkFileValidity(fileName)) {
            Config.io.printError("Invalid file name: " + fileName);
            return;
        }

        if (fileToZip.isDirectory()) {
            String entryName = fileName.endsWith(File.separator) ? fileName : fileName + File.separator;
            zipOut.putNextEntry(new ZipEntry(entryName));
            File[] children = fileToZip.listFiles();
            if (children != null) {
                for (File child : children) {
                    compressFile(child, fileName + File.separator + child.getName(), zipOut);
                }
            }
        } else {
            try (FileInputStream fis = new FileInputStream(fileToZip)) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    zipOut.write(buffer, 0, bytesRead);
                }
                Config.io.printDebug("Compressed file: " + fileName);
            } catch (Exception e) {
                Config.io.printError("File compression error: " + fileName);
                throw e;
            }
        }
    }

    private void decompressZip(Path archivePath, Path destinationPath, boolean isUpdateMode) throws Exception {
        if (!Config.io.checkFileValidity(archivePath.getFileName().toString())) {
            Config.io.printError("Invalid archive file name: " + archivePath.getFileName());
            return;
        }

        try (FileInputStream fis = new FileInputStream(archivePath.toFile());
             ZipInputStream zipIn = new ZipInputStream(fis)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                String entryName = entry.getName();
                if (!Config.io.checkFileValidity(entryName)) {
                    Config.io.printError("Invalid entry name in archive: " + entryName);
                    continue;
                }

                Path entryPath = destinationPath.resolve(entryName);
                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    try (FileOutputStream fos = new FileOutputStream(entryPath.toFile())) {
                        if (isUpdateMode) {
                            Config.io.printInfo("Installing File: " + entryName);
                            fileWrite.log("Installing: " + entryName, "Update");
                        }
                        int bytesRead;
                        while ((bytesRead = zipIn.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }
                        Config.io.printDebug("Decompressed file: " + entryName);
                    } catch (Exception e) {
                        Config.io.printError("File decompression error: " + entryName);
                        throw e;
                    }
                }
                zipIn.closeEntry();
            }
            Config.io.printInfo("Successfully decompressed to: " + destinationPath);
        } catch (Exception e) {
            Config.io.printError("Decompression failed: " + e.getMessage());
            throw e;
        }
    }
}