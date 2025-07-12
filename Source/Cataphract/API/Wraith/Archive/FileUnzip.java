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

package Cataphract.API.Wraith.Archive;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import Cataphract.API.IOStreams;
import Cataphract.API.Dragon.Login;
import Cataphract.API.Minotaur.PolicyCheck;
import Cataphract.API.Wraith.FileWrite;

/**
 * A utility class for unzipping files.
 *
 * @author DAK404 (https://github.com/DAK404)
 * @version 1.0.0 (11-October-2023, Cataphract)
 * @since 0.0.1 (Cataphract 0.0.1)
 */
public class FileUnzip
{
    /** Store value of user privileges. */
    private boolean _isUserAdmin = false;

    /** Store value of the update mode; True if program is updating, False if program is not */
    private boolean updateMode = false;

    /**
     * Constructor to check if the current user is an administrator or not.
     *
     * @param username The currently logged in username.
     * @throws Exception Throws any exceptions encountered during runtime.
     */
    public FileUnzip(String username) throws Exception
    {
        _isUserAdmin = new Login(username).checkPrivilegeLogic();
    }

    /**
     * Unzips a user file in the specified destination directory
     *
     * @param fileName          Name of the file to be unzipped.
     * @param unzipDestination  Destination for the file to be unzipped in.
     * @throws Exception Throws any exceptions encountered during runtime.
     */
    public void unzip(String fileName, String unzipDestination) throws Exception
    {
        fileName = IOStreams.convertFileSeparator(fileName);
        unzipDestination = IOStreams.convertFileSeparator(unzipDestination);
        // Check the policy if file unzip is allowed in the policy file, can be bypassed by the accounts with administrator privileges
        if (new PolicyCheck().retrievePolicyValue("filemgmt").equals("on") || _isUserAdmin)
            unzipLogic(fileName, unzipDestination);
        else
            IOStreams.printError("Policy Management System - Permission Denied.");
    }

    /**
     * Installs an update by unzipping the "Update.zip" file.
     *
     * @throws Exception Throws any exceptions encountered during runtime.
     */
    public void installUpdate() throws Exception
    {
        updateMode = true;
        if (new PolicyCheck().retrievePolicyValue("update").equals("on") || _isUserAdmin)
            unzipLogic(IOStreams.convertFileSeparator(".|Update.zip"), IOStreams.convertFileSeparator(".|"));
        else
            IOStreams.printError("Policy Management System - Permission Denied.");
        updateMode = false;
    }

    /**
     * Unzips a given zip file into the specified output directory.
     *
     * @param zipFilePath       Path to the zip file.
     * @param outputDirectory   Directory where the unzipped files will be placed.
     */
    private void unzipLogic(String zipFilePath, String outputDirectory)
    {
        try
        {
            // Open the zip file for reading
            FileInputStream fis = new FileInputStream(zipFilePath);
            FileOutputStream fos = null;
            try (ZipInputStream zipIn = new ZipInputStream(fis))
            {
                byte[] buffer = new byte[1024];
                ZipEntry entry;

                // Iterate through each entry in the zip file
                while ((entry = zipIn.getNextEntry()) != null)
                {
                    String entryName = entry.getName();
                    Path entryPath = Paths.get(outputDirectory, entryName);

                    if (entry.isDirectory())
                    {
                        // Create directories for directory entries
                        Files.createDirectories(entryPath);
                    }
                    else
                    {
                        // Create directories for file entries and write the file content
                        Files.createDirectories(entryPath.getParent());
                        try
                        {
                            if (updateMode)
                            {
                                IOStreams.printInfo("Installing File: " + entryName);
                                FileWrite.logger("Installing: " + entryName, "Update");
                            }

                            fos = new FileOutputStream(entryPath.toFile());
                            int bytesRead;

                            while ((bytesRead = zipIn.read(buffer)) != -1)
                            {
                                fos.write(buffer, 0, bytesRead);
                            }
                            fos.close(); // Close the FileOutputStream here
                        }
                        catch (Exception e)
                        {
                            IOStreams.printError("File Error: " + entryName);
                        }
                    }
                }
                zipIn.closeEntry();
            }
            catch (Exception e)
            {
                IOStreams.println("Error: " + e);
                e.printStackTrace();
            }
        }
        catch (FileNotFoundException fnfe)
        {
            IOStreams.printError("File Parse Failure: File Not Found.");
        }
        catch (Exception e)
        {
            // Handle exceptions (e.g., file not found, I/O errors)
            IOStreams.printError("Unable to proceed.");
            e.printStackTrace();
        }

        System.gc();
    }
}
