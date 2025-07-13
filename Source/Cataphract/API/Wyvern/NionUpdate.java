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

package Cataphract.API.Wyvern;

import Cataphract.API.Config;
import Cataphract.API.Dragon.Login;
import Cataphract.API.Minotaur.PolicyCheck;
import Cataphract.API.Wraith.FileDownload;
import Cataphract.API.Wraith.Archive.ZipArchiveHandler;

/**
* A class that provides utilities for updating Cataphract
*
* @author DAK404 (https://github.com/DAK404)
* @version 2.0.0 (13-July-2025, Cataphract)
* @since 0.0.1 (Mosaic 0.0.1)
*/
public class NionUpdate {
    /** Stores the username of the current user. */
    private final String username;

    /** Stores the value of user privileges. */
    private final boolean isUserAdmin;

    /** Log file name for update events. */
    private static final String LOG_FILE_NAME = "UpdateLog";

    /**
    * Constructor to store username, check privileges, and initialize dependencies.
    *
    * @param username Username of the current user
    * @throws Exception Throws any exceptions encountered during runtime
    */
    public NionUpdate(String username) throws Exception {
        this.username = username;
        this.isUserAdmin = new Login(username).checkPrivilegeLogic();
    }

    /**
    * Updates Cataphract, fetching the latest release from remote.
    *
    * @throws Exception Throws any exceptions encountered during runtime
    */
    public void updater() throws Exception {
        try {
            // Check if updating is allowed or if user has admin privileges
            if (!new PolicyCheck().retrievePolicyValue("update").equals("on") && !isUserAdmin) {
                Config.io.printError("Policy Management System - Permission Denied.");
                return;
            }

            Config.fileWriter.log("Starting Cataphract update process", LOG_FILE_NAME);

            Config.io.println("---- Wyvern: Program Update Utility 2.0 ----");
            Config.io.printAttention("[*] This will install the latest version of Cataphract. Please ensure that there is internet connectivity.");
            Config.io.printAttention("[*] After updating, Cataphract will require a restart to apply updated files.\n");
            Config.io.printWarning("DO NOT TURN OFF THE SYSTEM, CHANGE NETWORK STATES, OR CLOSE THIS PROGRAM.\nBY DOING THIS, YOU MIGHT RISK THE LOSS OF DATA OR PROGRAM INSTABILITY.");
            Config.io.println("--------------------------------------------\n");

            downloadUpdate();
            installUpdate();
            Config.io.printAttention("It is recommended to restart Cataphract for the updates to be reflected.");
            Config.fileWriter.log("Update process completed successfully", LOG_FILE_NAME);
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
        }
    }

    /**
    * Downloads the update.
    *
    * @throws Exception Throws any exceptions encountered during runtime
    */
    private void downloadUpdate() throws Exception {
        try {
            new FileDownload(username).downloadUpdate();
            Config.fileWriter.log("Download status: Complete", LOG_FILE_NAME);
        } catch (Exception e) {
            Config.fileWriter.log("Download failed: " + e.getMessage(), LOG_FILE_NAME);
            throw e;
        }
    }

    /**
    * Unzips the update file to the Cataphract install directory.
    *
    * @throws Exception Throws any exceptions encountered during runtime
    */
    private void installUpdate() throws Exception {
        try {
            Config.io.println("Installing Update...");
            Config.fileWriter.log("Installing update", LOG_FILE_NAME);
            new ZipArchiveHandler(username, Config.fileWriter).installUpdate();
        } catch (Exception e) {
            Config.fileWriter.log("Installation failed: " + e.getMessage(), LOG_FILE_NAME);
            throw e;
        }
    }
}