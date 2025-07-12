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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;

import Cataphract.API.IOStreams;
import Cataphract.API.Dragon.Login;
import Cataphract.API.Minotaur.PolicyCheck;
import Cataphract.API.Build;

/**
* A class provides methods for reading files and displaying their contents.
*
* @author DAK404 (https://github.com/DAK404)
* @version 3.6.2 (20-February-2024, Cataphract)
* @since 0.0.1 (Zen Quantum 0.0.1)
*/
public class FileRead
{
    /** Store the current username to check privileges */
    private String _username = "";

    /** Flag to indicate whether help mode is enabled. */
    private boolean helpMode = false;

    /** Static variable to store the file name. */
    private static File fileName = null;

    /**
    * Constructor to be used for reading help files
    */
    public FileRead()
    {
    }

    /**
    * Constructor to be used for reading user generated files
    *
    * @param username The username of the currently logged in user
    */
    public FileRead(String username)
    {
        _username = username;
    }

    /**
    * Handles the logic for reading the file.
    *
    * @throws Exception Throws any exceptions encountered during runtime.
    */
    private void readFileLogic() throws Exception
    {
        try
        {
            if (!IOStreams.checkFileValidity(fileName.getName()))
            {
                // If the file name is invalid
                IOStreams.printError("Invalid File Name! Please Enter A Valid File Name.");
            }
            else if (!fileName.exists())
            {
                // If the specified file does not exist
                IOStreams.printError("The Specified File Does Not Exist. Please Enter A Valid File Name.");
            }
            else
            {
                // Flag to control file reading loop
                boolean continueFileRead = true;

                // Display build information
                Build.viewBuildInfo();

                // Create a BufferedReader to read from the file
                BufferedReader bufferObject = new BufferedReader(new FileReader(fileName));

                // Variable to store file contents
                String fileContents = "";

                // If help mode is enabled
                if (helpMode)
                {
                    // Continue until the end of file or instructed to stop
                    while (fileContents != null && continueFileRead)
                    {
                        // Read a line from the file
                        fileContents = bufferObject.readLine();

                        if (fileContents != null && fileContents.equalsIgnoreCase("<end of page>"))
                        {
                            // If it reaches the end of the page marker, prompt the user to continue or exit the help viewer
                            if (IOStreams.confirmReturnToContinue("", "else type EXIT to quit Help Viewer.\\n" + "~DOC_HLP?> ").equalsIgnoreCase("exit"))
                            // Set flag to stop reading
                            continueFileRead = false;
                            else
                            {
                                // Clear the screen and display build information and continue reading the file
                                Build.viewBuildInfo();
                                continue;
                            }
                        }
                        // If it reaches the end of the help file marker
                        else if (fileContents != null && fileContents.equalsIgnoreCase("<end of help>"))
                        {
                            // Print end of help file message
                            IOStreams.println("\n\nEnd of Help File.");
                            break;
                        }
                        // If it encounters a comment line, skip this line
                        else if (fileContents != null && fileContents.startsWith("#"))
                        {
                            continue;
                        }
                        // Print the file contents
                        if (fileContents != null)
                        {
                            IOStreams.println(fileContents);
                        }
                    }
                }
                // If help mode is not enabled
                else
                {
                    // Read the file until the end of file is reached
                    while ((fileContents = bufferObject.readLine()) != null)
                    {
                        // Print the file contents
                        IOStreams.println(fileContents);
                    }
                }

                // Close the streams
                bufferObject.close();

                // Request garbage collection to free up resources
                System.gc();

                // Prompt to return to continue
                IOStreams.confirmReturnToContinue();
            }
        }
        catch (FileNotFoundException fnfe)
        {
            IOStreams.printError("The specified file " + fileName + "does not exist.");
        }
        catch (Exception e)
        {
            // Print error message if an exception occurs
            IOStreams.printError("An Error Occurred While Reading The File: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
    * Reads a file specified by the user.
    *
    * @param userFileName The name of the user's file to be read.
    * @throws Exception Throws any exceptions encountered during runtime.
    */
    public void readUserFile(String userFileName) throws Exception
    {
        // Check the policy if file reading is allowed in the policy file, can be bypassed by the accounts with administrator privileges
        if (new PolicyCheck().retrievePolicyValue("update").equals("on") || new Login(_username).checkPrivilegeLogic())
        {
            // Set the file name
            fileName = new File(IOStreams.convertFileSeparator(userFileName));
            // Perform file reading logic
            readFileLogic();
        }
        else
        IOStreams.printError("Policy Management System - Permission Denied.");
    }

    /**
    * Reads a help file.
    *
    * @param helpFile The name of the help file to be read.
    * @throws Exception Throws any exceptions encountered during runtime.
    */
    public void readHelpFile(String helpFile) throws Exception
    {
        // Enable help mode
        helpMode = true;
        // Set the file name
        fileName = new File(IOStreams.convertFileSeparator(".|docs|Cataphract|Help|" + helpFile));
        // Perform file reading logic
        readFileLogic();
    }
}
