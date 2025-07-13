
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

package Cataphract.API.Wraith;

import java.io.FileOutputStream;
import java.net.URI;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;

import Cataphract.API.Config;
import Cataphract.API.Dragon.Login;

/**
 * Implementation of FileDownloader for downloading files from URLs.
 *
 * @author DAK404 (https://github.com/DAK404)
 * @version 1.4.1 (13-July-2025, Cataphract)
 * @since 0.0.1 (Cataphract 0.0.1)
 */
public class FileDownload implements FileDownloader {
    private static final String UPDATE_FILE_NAME = "Update.zip";
    private final PathUtils pathUtils;
    private final boolean isUserAdmin;

    public FileDownload(String username) throws Exception {
        this.pathUtils = new PathUtils();
        this.isUserAdmin = new Login(username).checkPrivilegeLogic();
    }

    @Override
    public void downloadFile(String url, Path destination) throws Exception {
        if (!canDownload()) {
            Config.io.printError("Policy Management System - Permission Denied.");
            return;
        }
        if (url == null || url.isEmpty() || !pathUtils.isValidPathName(destination.getFileName().toString())) {
            Config.io.printError("Invalid URL or file name.");
            return;
        }
        Path parentDir = destination.getParent();
        if (!Files.exists(parentDir)) {
            Config.io.printError("Destination directory does not exist: " + parentDir);
            return;
        }
        downloadUsingNIO(url, destination);
        Config.io.printInfo("Downloaded file to: " + destination);
    }

    @Override
    public void downloadUpdate() throws Exception {
        if (!canUpdate()) {
            Config.io.printError("Policy Management System - Permission Denied.");
            return;
        }
        String updateUrl = Config.UPDATE_URL;
        Path destination = pathUtils.resolveRelativePath(Path.of("."), UPDATE_FILE_NAME);
        Config.io.printInfo("Downloading update from: " + updateUrl);
        downloadUsingNIO(updateUrl, destination);
        Config.io.printInfo("Update downloaded to: " + destination);
    }

    private void downloadUsingNIO(String urlStr, Path destination) throws Exception {
        try (ReadableByteChannel rbc = Channels.newChannel(new URI(urlStr).toURL().openStream());
             FileOutputStream fos = new FileOutputStream(destination.toFile())) {
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } catch (Exception e) {
            Config.io.printError("Download failed: " + e.getMessage());
            throw e;
        }
    }

    private boolean canDownload() throws Exception {
        return Config.policyCheck.retrievePolicyValue("download").equals("on") || isUserAdmin;
    }

    private boolean canUpdate() throws Exception {
        return Config.policyCheck.retrievePolicyValue("update").equals("on") || isUserAdmin;
    }
}