/*
 * This file is part of the Cataphract project.
 * Copyright (C) 2024 DAK404 (https://github.com/DAK404)
 *
 * This program is distributed under the GNU General Public License as published by
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
package Cataphract.API.Wyvern;

import java.nio.file.Path;
import java.nio.file.Paths;

import Cataphract.API.Config;
import Cataphract.API.Wraith.FileDownload;
import Cataphract.API.Wraith.FileUnzip;
import Cataphract.API.Wraith.FileWrite;
import Cataphract.API.Dragon.Login;

/**
 * Manages the Cataphract update process by orchestrating download and installation steps.
 *
 * @author DAK404 (https://github.com/DAK404)
 * @version 2.1.0 (14-July-2025, Cataphract)
 * @since 0.0.1 (Mosaic 0.0.1)
 */
public class UpdateManager {
    protected static final String LOG_FILE_NAME = "UpdateLog";
    protected static final String UPDATE_FILE_NAME = "Cataphract.zip";

    private final String username;
    private final boolean isUserAdmin;
    private final FileDownload fileDownload;
    private final FileUnzip fileUnzip;
    private final FileWrite fileWrite;

    public UpdateManager(String username, FileDownload fileDownload, FileUnzip fileUnzip, FileWrite fileWrite) throws Exception {
        this.username = username == null || username.isEmpty() ? "DEFAULT_USER" : username;
        this.isUserAdmin = new Login(username).checkPrivilegeLogic();
        this.fileDownload = fileDownload;
        this.fileUnzip = fileUnzip;
        this.fileWrite = fileWrite;
    }

    /**
     * Performs the Cataphract update process, including permission check, download, and installation.
     *
     * @throws Exception If any step in the update process fails.
     */
    public void performUpdate() throws Exception {
        if (!hasUpdatePermission()) {
            Config.io.printError("Policy Management System - Permission Denied.");
            logOperation("Update failed: Insufficient permissions for user " + username);
            return;
        }

        displayUpdateHeader();
        logOperation("Starting Cataphract update process for user: " + username);

        UpdateDownloader downloader = new UpdateDownloader(fileDownload, fileWrite);
        UpdateInstaller installer = new UpdateInstaller(fileUnzip, fileWrite);

        executeUpdatePipeline(downloader, installer);
        Config.io.printAttention("It is recommended to restart Cataphract for the updates to be reflected.");
        logOperation("Update process completed successfully for user: " + username);
    }

    /**
     * Checks if the user has permission to perform updates.
     *
     * @return true if the user has permission, false otherwise.
     * @throws Exception If a database error occurs during policy check.
     */
    private boolean hasUpdatePermission() throws Exception {
        return isUserAdmin || "on".equalsIgnoreCase(Config.policyCheck.retrievePolicyValue("update"));
    }

    /**
     * Displays the update process header to the user.
     */
    private void displayUpdateHeader() {
        Config.io.println("---- Wyvern: Program Update Utility 2.1 ----");
        Config.io.printAttention("This will install the latest version of Cataphract. Ensure internet connectivity.");
        Config.io.printAttention("After updating, Cataphract will require a restart to apply updated files.");
        Config.io.printWarning("DO NOT turn off the system, change network states, or close this program.");
        Config.io.println("--------------------------------------------\n");
    }

    /**
     * Executes the update pipeline with the provided operations.
     *
     * @param operations The update operations to execute.
     * @throws Exception If any operation fails.
     */
    private void executeUpdatePipeline(IUpdateOperation... operations) throws Exception {
        for (IUpdateOperation operation : operations) {
            operation.execute();
        }
    }

    /**
     * Logs an update operation with a timestamp.
     *
     * @param message The message to log.
     * @throws Exception If logging fails.
     */
    private void logOperation(String message) throws Exception {
        fileWrite.log(String.format("User %s: %s", username, message), LOG_FILE_NAME);
    }
}

/**
 * Interface for update operations to enable extensibility.
 */
interface IUpdateOperation {
    void execute() throws Exception;
}

/**
 * Handles downloading the update file.
 */
class UpdateDownloader implements IUpdateOperation {
    private final FileDownload fileDownload;
    private final FileWrite fileWrite;

    UpdateDownloader(FileDownload fileDownload, FileWrite fileWrite) {
        this.fileDownload = fileDownload;
        this.fileWrite = fileWrite;
    }

    @Override
    public void execute() throws Exception {
        try {
            Config.io.println("Downloading update...");
            fileDownload.execute(new String[]{"download", Config.UPDATE_URL, UpdateManager.UPDATE_FILE_NAME});
            fileWrite.log("Download status: Complete", UpdateManager.LOG_FILE_NAME);
        } catch (Exception e) {
            fileWrite.log("Download failed: " + e.getMessage(), UpdateManager.LOG_FILE_NAME);
            throw new Exception("Update download failed: " + e.getMessage(), e);
        }
    }
}

/**
 * Handles installing the update by unzipping the downloaded file.
 */
class UpdateInstaller implements IUpdateOperation {
    private final FileUnzip fileUnzip;
    private final FileWrite fileWrite;

    UpdateInstaller(FileUnzip fileUnzip, FileWrite fileWrite) {
        this.fileUnzip = fileUnzip;
        this.fileWrite = fileWrite;
    }

    @Override
    public void execute() throws Exception {
        try {
            Config.io.println("Installing update...");
            Path archivePath = Paths.get(System.getProperty("user.dir"), UpdateManager.UPDATE_FILE_NAME);
            Path destinationPath = Paths.get(Config.SYSTEM_PATH);
            fileUnzip.execute(new String[]{"unzip", archivePath.toString(), destinationPath.toString()});
            fileWrite.log("Installation completed successfully", UpdateManager.LOG_FILE_NAME);
        } catch (Exception e) {
            fileWrite.log("Installation failed: " + e.getMessage(), UpdateManager.LOG_FILE_NAME);
            throw new Exception("Update installation failed: " + e.getMessage(), e);
        }
    }
}