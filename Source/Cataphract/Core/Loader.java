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

package Cataphract.Core;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import Cataphract.API.Config;
import Cataphract.API.Dragon.DatabaseInitializer;
import Cataphract.API.Dragon.AccountCreate;

/**
 * Initializes the Cataphract shell with different boot modes.
 *
 * @author DAK404 (https://github.com/DAK404)
 * @version 1.4.1 (13-July-2025, Cataphract)
 * @since 0.0.1 (Cataphract 0.0.1)
 */
public class Loader {
    protected static final String LOG_FILE_NAME = "LoaderLog";
    protected static List<String> abraxisFilePathList = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        try {
            if (args.length == 0) {
                Config.io.printError("No boot mode specified. Aborting...");
                Config.fileWriter.log("Boot failed: No boot mode specified", LOG_FILE_NAME);
                System.exit(1);
            }

            String mode = args[0].toLowerCase();
            Config.fileWriter.log("Starting Loader in mode: " + mode, LOG_FILE_NAME);
            BootMode bootMode;

            switch (mode) {
                case "probe":
                    bootMode = new ProbeMode();
                    break;
                case "normal":
                    bootMode = new NormalMode();
                    break;
                case "debug":
                    if (args.length < 2) {
                        Config.io.printError("Invalid Syntax for debug mode.");
                        Config.fileWriter.log("Boot failed: Invalid debug mode syntax", LOG_FILE_NAME);
                        System.exit(1);
                    }
                    bootMode = new DebugMode(args[1]);
                    break;
                default:
                    Config.io.printError("Invalid Boot Mode. Aborting...");
                    Config.fileWriter.log("Boot failed: Invalid boot mode - " + mode, LOG_FILE_NAME);
                    System.exit(3);
                    return;
            }

            bootMode.execute();
            Config.fileWriter.log("Boot mode " + mode + " executed", LOG_FILE_NAME);
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
            Config.fileWriter.log("Loader error: " + e.getMessage(), LOG_FILE_NAME);

        }
    }
}

interface BootMode {
    void execute() throws Exception;
}

class ProbeMode implements BootMode {
    @Override
    public void execute() throws Exception {
        try {
            Config.fileWriter.log("Executing ProbeMode", Loader.LOG_FILE_NAME);
            System.exit(7);
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
            Config.fileWriter.log("ProbeMode error: " + e.getMessage(), Loader.LOG_FILE_NAME);

        }
    }
}

class NormalMode implements BootMode {
    @Override
    public void execute() throws Exception {
        try {
            Config.build.viewBuildInfo(false);
            Config.fileWriter.log("Executing NormalMode", Loader.LOG_FILE_NAME);
            new LoaderLogic().execute();
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
            Config.fileWriter.log("NormalMode error: " + e.getMessage(), Loader.LOG_FILE_NAME);

        }
    }
}

class DebugMode implements BootMode {
    private final String debugOption;

    public DebugMode(String debugOption) {
        this.debugOption = debugOption;
    }

    @Override
    public void execute() throws Exception {
        try {
            Config.fileWriter.log("Executing DebugMode with option: " + debugOption, Loader.LOG_FILE_NAME);
            switch (debugOption.toLowerCase()) {
                case "crash":
                    throw new Exception("Simulated crash for debugging.");
                case "astaroth":
                    Config.io.println(String.valueOf(Config.time.getUnixEpoch()));
                    Config.io.println(String.valueOf(Config.time.getDateTimeUsingSpecifiedFormat("dd-MMMM-yyyy \nEEEE HH:mm:ss")));
                    Config.calendar.printCalendar(0, 0);
                    Config.calendar.printCalendar(8, 2077);
                    Config.fileWriter.log("DebugMode: Astaroth calendar printed", Loader.LOG_FILE_NAME);
                    System.exit(0);
                    break;
                case "io":
                    Config.io.printError("This is an error message.");
                    Config.io.printWarning("This is a warning message.");
                    Config.io.printAttention("This is an attention message.");
                    Config.io.printInfo("This is an information message.");
                    Config.io.println("This is a normal printline message. Printing the same with colors");
                    Config.fileWriter.log("DebugMode: IO test messages printed", Loader.LOG_FILE_NAME);
                    System.exit(0);
                    break;
                default:
                    Config.io.printError("Undefined Debug Parameter.");
                    Config.fileWriter.log("DebugMode error: Undefined parameter - " + debugOption, Loader.LOG_FILE_NAME);
                    System.exit(1);
                    break;
            }
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
            Config.fileWriter.log("DebugMode error: " + e.getMessage(), Loader.LOG_FILE_NAME);

        }
    }
}

class LoaderLogic {
    private final IntegrityChecker integrityChecker = new IntegrityChecker();

    public void execute() throws Exception {
        try {
            Config.fileWriter.log("Starting LoaderLogic integrity checks", Loader.LOG_FILE_NAME);
            byte result = integrityChecker.checkIntegrity();

            switch (result) {
                case 0:
                    Config.io.printInfo("Integrity checks passed. Booting Cataphract...");
                    Config.fileWriter.log("Integrity checks passed, booting GuestShell", Loader.LOG_FILE_NAME);
                    new GuestShell().execute();
                    break;
                case 1:
                    Config.io.printError("Unable to locate or parse Manifest Files! Aborting boot...");
                    Config.fileWriter.log("Integrity check failed: Missing manifest files", Loader.LOG_FILE_NAME);
                    System.exit(4);
                    break;
                case 2:
                    Config.io.printError("Unable to populate the Kernel files! Aborting boot...");
                    Config.fileWriter.log("Integrity check failed: Failed to populate kernel files", Loader.LOG_FILE_NAME);
                    System.exit(4);
                    break;
                case 3:
                    Config.io.printError("File Signature verification failed! Aborting boot...");
                    Config.fileWriter.log("Integrity check failed: File signature verification", Loader.LOG_FILE_NAME);
                    System.exit(4);
                    break;
                case 4:
                    Config.io.printError("File verification failed: Found File Size Discrepancy! Aborting boot...");
                    Config.fileWriter.log("Integrity check failed: File size discrepancy", Loader.LOG_FILE_NAME);
                    System.exit(4);
                    break;
                case 5:
                    Config.fileWriter.log("Initiating Cataphract setup", Loader.LOG_FILE_NAME);
                    Setup setup = new Setup();
                    if (setup.setupCataphract()) {
                        Config.fileWriter.log("Setup completed, restarting", Loader.LOG_FILE_NAME);
                        System.exit(100);
                    } else {
                        Config.io.printError("Setup Failed!");
                        Config.fileWriter.log("Setup failed", Loader.LOG_FILE_NAME);
                        System.exit(4);
                    }
                    break;
                default:
                    Config.io.printError("Generic Failure. Cannot Boot.");
                    Config.fileWriter.log("Integrity check failed: Generic failure", Loader.LOG_FILE_NAME);
                    System.exit(4);
                    break;
            }
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
            Config.fileWriter.log("LoaderLogic error: " + e.getMessage(), Loader.LOG_FILE_NAME);

        }
    }
}

class IntegrityChecker {
    private List<String> abraxisFilePathList = Loader.abraxisFilePathList;

    public byte checkIntegrity() throws Exception {
        try {
            Config.fileWriter.log("Starting integrity checks", Loader.LOG_FILE_NAME);
            byte result = 55;

            Config.io.printInfo("Stage 0: Checking Manifest Files...");
            Config.fileWriter.log("Checking manifest files", Loader.LOG_FILE_NAME);
            if (manifestFilesCheck()) {
                Config.io.printInfo("Stage 1: Manifest Files Found. Populating Kernel Files and Directories...");
                Config.fileWriter.log("Populating kernel files", Loader.LOG_FILE_NAME);
                if (populateKernelFiles(new File("./"))) {
                    Config.io.printInfo("Stage 2: Kernel Files and Directories populated. Checking File Integrity - Phase 1...");
                    Config.fileWriter.log("Checking file hashes", Loader.LOG_FILE_NAME);
                    if (checkFileHashes()) {
                        Config.io.printInfo("Stage 3: File Integrity Check - Phase 1 Complete. Checking File Integrity - Phase 2...");
                        Config.fileWriter.log("Checking file sizes", Loader.LOG_FILE_NAME);
                        if (checkFileSizes()) {
                            Config.io.printInfo("Stage 4: File Integrity Check - Phase 2 Complete. Checking System and User Files...");
                            Config.fileWriter.log("Checking system and user files", Loader.LOG_FILE_NAME);
                            if (!setupStatusCheck()) {
                                result = 5;
                                Config.io.printAttention("Setting up Cataphract...");
                                Config.fileWriter.log("Setup required", Loader.LOG_FILE_NAME);
                            } else {
                                result = 0;
                            }
                        } else {
                            result = 4;
                        }
                    } else {
                        result = 3;
                    }
                } else {
                    result = 2;
                }
            } else {
                result = 1;
            }

            Config.fileWriter.log("Integrity check result: " + result, Loader.LOG_FILE_NAME);
            abraxisFilePathList.clear();
            System.gc();
            return result;
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
            Config.fileWriter.log("Integrity check error: " + e.getMessage(), Loader.LOG_FILE_NAME);
            return 55;
        }
    }

    private boolean manifestFilesCheck() throws Exception {
        try {
            boolean exists = new File(Config.io.convertFileSeparator(".|.Manifest|Cataphract|KernelFilesHashes.m1")).exists() &&
                            new File(Config.io.convertFileSeparator(".|.Manifest|Cataphract|KernelFiles.m2")).exists();
            Config.fileWriter.log("Manifest files check: " + (exists ? "Passed" : "Failed"), Loader.LOG_FILE_NAME);
            return exists;
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
            Config.fileWriter.log("Manifest files check error: " + e.getMessage(), Loader.LOG_FILE_NAME);
            return false;
        }
    }

    private boolean populateKernelFiles(File fileDirectory) throws Exception {
        try {
            File[] fileList = fileDirectory.listFiles();
            if (fileList == null) {
                Config.fileWriter.log("Failed to list files in directory: " + fileDirectory.getPath(), Loader.LOG_FILE_NAME);
                return false;
            }

            for (File fileName : fileList) {
                if (fileIgnoreList(fileName.getName())) continue;
                if (fileName.isDirectory()) {
                    populateKernelFiles(fileName);
                } else {
                    abraxisFilePathList.add(fileName.getPath());
                }
            }
            Config.fileWriter.log("Populated " + abraxisFilePathList.size() + " kernel files", Loader.LOG_FILE_NAME);
            return true;
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
            Config.fileWriter.log("Populate kernel files error: " + e.getMessage(), Loader.LOG_FILE_NAME);
            return false;
        }
    }

    private boolean fileIgnoreList(String fileName) {
        final String[] ignoreList = {".Manifest", "System", "Users", "org", "JRE", "Logs", "BuildSigner.java"};
        for (String files : ignoreList) {
            if (fileName.equalsIgnoreCase(files)) return true;
        }
        return false;
    }

    private boolean checkFileHashes() throws Exception {
        try {
            Properties manifestM1Entries = new Properties();
            FileInputStream m1FileStream = new FileInputStream(Config.io.convertFileSeparator(".|.Manifest|Cataphract|KernelFilesHashes.m1"));
            manifestM1Entries.loadFromXML(m1FileStream);
            m1FileStream.close();

            for (String fileName : abraxisFilePathList) {
                if (fileIgnoreList(fileName)) continue;
                String kernelFileHash = Config.cryptography.fileToSHA3_256(new File(fileName));
                String manifestHash = manifestM1Entries.getProperty(Config.io.convertToNionSeparator(fileName));
                if (manifestHash == null || !manifestHash.equals(kernelFileHash)) {
                    Config.io.printError("Integrity Check Failure at " + kernelFileHash + "\t" + fileName);
                    Config.fileWriter.log("File hash check failed for: " + fileName, Loader.LOG_FILE_NAME);
                    return false;
                }
            }
            Config.fileWriter.log("File hash check passed", Loader.LOG_FILE_NAME);
            return true;
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
            Config.fileWriter.log("File hash check error: " + e.getMessage(), Loader.LOG_FILE_NAME);
            return false;
        }
    }

    private boolean checkFileSizes() throws Exception {
        try {
            Properties manifestM2Entries = new Properties();
            FileInputStream m2FileStream = new FileInputStream(Config.io.convertFileSeparator(".|.Manifest|Cataphract|KernelFiles.m2"));
            manifestM2Entries.loadFromXML(m2FileStream);
            m2FileStream.close();

            int fileCount = 0;
            for (String fileName : abraxisFilePathList) {
                if (!fileName.endsWith(".class")) continue;
                long fileSizeM2 = Long.parseLong(manifestM2Entries.getProperty(Config.io.convertToNionSeparator(fileName), "-1"));
                long fileSize = new File(fileName).length();
                if (fileSize != fileSizeM2) {
                    Config.io.printError("Integrity Check Failure at " + fileName + "\t" + fileSize + ". Expected " + fileSizeM2);
                    Config.fileWriter.log("File size check failed for: " + fileName, Loader.LOG_FILE_NAME);
                    return false;
                }
                fileCount++;
            }
            if (fileCount < manifestM2Entries.size()) {
                Config.io.printError("Integrity Check Failure. Expected " + manifestM2Entries.size() + ". Found " + fileCount);
                Config.fileWriter.log("File size check failed: File count mismatch", Loader.LOG_FILE_NAME);
                return false;
            }
            Config.fileWriter.log("File size check passed", Loader.LOG_FILE_NAME);
            return true;
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
            Config.fileWriter.log("File size check error: " + e.getMessage(), Loader.LOG_FILE_NAME);
            return false;
        }
    }

    private boolean setupStatusCheck() throws Exception {
        try {
            boolean exists = new File(Config.io.convertFileSeparator(".|System|Cataphract")).exists() &&
                            new File(Config.io.convertFileSeparator(".|Users|Cataphract")).exists();
            Config.fileWriter.log("Setup status check: " + (exists ? "Passed" : "Failed"), Loader.LOG_FILE_NAME);
            return exists;
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
            Config.fileWriter.log("Setup status check error: " + e.getMessage(), Loader.LOG_FILE_NAME);
            return false;
        }
    }
}

class GuestShell {
    public void execute() throws Exception {
        try {
            Config.fileWriter.log("Starting GuestShell", Loader.LOG_FILE_NAME);
            String input;
            do {
                input = Config.console.readLine("> ");
                String[] commandArray = Config.io.splitStringToArray(input);
                Config.fileWriter.log("GuestShell command: " + input, Loader.LOG_FILE_NAME);

                switch (commandArray[0].toLowerCase()) {
                    case "exit":
                    case "":
                        break;
                    case "clear":
                        Config.build.clearScreen();
                        Config.fileWriter.log("GuestShell: Cleared screen", Loader.LOG_FILE_NAME);
                        break;
                    case "login":
                        new SycoraxKernel().startSycoraxKernel();
                        Config.build.viewBuildInfo(false);
                        Config.io.println("Logout Successful");
                        Config.fileWriter.log("GuestShell: Login completed, logged out", Loader.LOG_FILE_NAME);
                        break;
                    default:
                        Config.io.printError(commandArray[0] + " Command Not Found.");
                        Config.fileWriter.log("GuestShell error: Command not found - " + commandArray[0], Loader.LOG_FILE_NAME);
                        break;
                }
            } while (!input.equalsIgnoreCase("exit"));
            Config.fileWriter.log("GuestShell terminated", Loader.LOG_FILE_NAME);
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
            Config.fileWriter.log("GuestShell error: " + e.getMessage(), Loader.LOG_FILE_NAME);

        }
    }
}

class Setup {
    private boolean prereqInfoStatus = false;
    private boolean initDirs = false;
    private boolean initDB = false;
    private boolean initPolicies = false;
    private boolean initAdminAccount = false;

    /**
     * The entry point to the setup. Sets the environment up to run Cataphract.
     * @return boolean returnValue - Returns if the setup was successful or not.
     */
    boolean setupCataphract() throws Exception {
        try {
            Config.fileWriter.log("Starting Cataphract setup", Loader.LOG_FILE_NAME);
            String oobeIntroduction = """

            Welcome to Cataphract!

            As this is the first time the program is being run, several setup steps need to be completed for normal use.

            [*] ACCEPT EULA: Agree to the End User License Agreement to begin the setup.
            [*] CREATE SYSTEM DIRECTORIES: Create directories essential for Cataphract to function as expected.
            [*] INITIALIZE DATABASE: Initialize the user database to store the user credentials.
            [*] CREATE ADMINISTRATOR ACCOUNT: Create the administrator account to engage and maintain the functioning of the system.

            These steps are required to be performed by the system administrator. If the current user is an end user,
            please press [ CTRL + C ] keys and contact the System Administrator.

            If the current user is a System Administrator,\u00A0""";

            Config.io.confirmReturnToContinue(oobeIntroduction, ".\nSetup> ");
            Config.fileWriter.log("Displayed setup introduction", Loader.LOG_FILE_NAME);

            if (!showAndAcceptEULA()) {
                Config.fileWriter.log("Setup failed: EULA not accepted", Loader.LOG_FILE_NAME);
                return false;
            }
            if (!createSystemDirectories()) {
                Config.fileWriter.log("Setup failed: Directory creation failed", Loader.LOG_FILE_NAME);
                return false;
            }
            if (!initializeDatabase()) {
                Config.fileWriter.log("Setup failed: Database initialization failed", Loader.LOG_FILE_NAME);
                return false;
            }
            if (!initializeDefaultPolicies()) {
                Config.fileWriter.log("Setup failed: Policy initialization failed", Loader.LOG_FILE_NAME);
                return false;
            }
            if (!createAdministratorAccount()) {
                Config.fileWriter.log("Setup failed: Admin account creation failed", Loader.LOG_FILE_NAME);
                return false;
            }

            displaySetupProgress();
            Config.io.confirmReturnToContinue("Setup complete! ", ".\nSetup> ");
            Config.fileWriter.log("Setup completed successfully", Loader.LOG_FILE_NAME);
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
            Config.fileWriter.log("Setup error: " + e.getMessage(), Loader.LOG_FILE_NAME);
        }
        return prereqInfoStatus && initAdminAccount && initDB && initDirs && initPolicies;
    }

    /**
     * Display the progress of the setup to the user: what's pending and what's completed
     */
    private void displaySetupProgress() throws Exception {
        try {
            Config.build.viewBuildInfo(false);
            Config.io.println("[ -- Program Setup Checklist -- ]");
            Config.io.println("[*] Show Program Prerequisites   : " + (prereqInfoStatus ? "COMPLETED" : "PENDING"));
            Config.io.println("[*] Initialize Directories       : " + (initDirs ? "COMPLETED" : "PENDING"));
            Config.io.println("[*] Initialize Database System   : " + (initDB ? "COMPLETED" : "PENDING"));
            Config.io.println("[*] Initialize Program Policies  : " + (initPolicies ? "COMPLETED" : "PENDING"));
            Config.io.println("[*] Create Administrator Account : " + (initAdminAccount ? "COMPLETED" : "PENDING"));
            Config.io.println("[ ----------------------------- ]\n");
            Config.fileWriter.log("Displayed setup progress", Loader.LOG_FILE_NAME);
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
            Config.fileWriter.log("Setup progress display error: " + e.getMessage(), Loader.LOG_FILE_NAME);

        }
    }

    /**
     * Logic to display the EULA to the user. User must accept it to complete the setup.
     */
    private boolean showAndAcceptEULA() throws Exception {
        try {
            displaySetupProgress();
            Config.io.println("Please read the End User License Agreement:");
            Config.fileReader.readHelpFile("EULA");
            String input = Config.console.readLine("Do you accept the EULA? [ Y / N ]\nEULA?> ").toLowerCase();
            boolean accepted = input.equals("y") || input.equals("yes");
            Config.fileWriter.log("EULA acceptance: " + (accepted ? "Accepted" : "Not accepted"), Loader.LOG_FILE_NAME);
            if (accepted) {
                Config.fileReader.readHelpFile("LICENSE");
                prereqInfoStatus = true;
            }
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
            Config.fileWriter.log("EULA acceptance error: " + e.getMessage(), Loader.LOG_FILE_NAME);
        }
        return prereqInfoStatus;
    }

    /**
     * Logic to create directories required by Cataphract.
     */
    private boolean createSystemDirectories() throws Exception {
        try {
            displaySetupProgress();
            Config.io.printInfo("Creating Directories...");
            String[] directoryNames = {
                ".|System|Cataphract|Private|",
                ".|System|Cataphract|Public|Logs|",
                ".|Users|Cataphract|",
                ".|.Manifest|Cataphract|",
                ".|docs|Cataphract|Help|"
            };
            for (String dir : directoryNames) {
                File directory = new File(Config.io.convertFileSeparator(dir));
                if (!directory.exists() && !directory.mkdirs()) {
                    Config.io.printError("Failed to create directory: " + dir);
                    Config.fileWriter.log("Directory creation failed: " + dir, Loader.LOG_FILE_NAME);
                    initDirs = false;
                    break;
                }
            }
            Config.fileWriter.log("System directories created successfully", Loader.LOG_FILE_NAME);
            initDirs = true;
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
            Config.fileWriter.log("Directory creation error: " + e.getMessage(), Loader.LOG_FILE_NAME);
        }
        return initDirs;
    }

    /**
     * Logic to initialize the database using the Dragon package.
     */
    private boolean initializeDatabase() throws Exception {
        try {
            displaySetupProgress();
            initDB = DatabaseInitializer.initializeDatabase();
            Config.fileWriter.log("Database initialized " + (initDB ? "successfully" : "unsuccessfully"), Loader.LOG_FILE_NAME);
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
            Config.fileWriter.log("Database initialization error: " + e.getMessage(), Loader.LOG_FILE_NAME);
        }
        return initDB;
    }

    /**
     * Logic to initialize the default policies for the users.
     */
    private boolean initializeDefaultPolicies() throws Exception
    {
        try {
            displaySetupProgress();
            Config.io.printInfo("Initializing default policies...");
            Config.policyManager.initializePolicyFile();
            String policyFilePath = Config.io.convertFileSeparator(".|System|Cataphract|Private|Policy.burn");
            boolean policiesInitialized = new File(policyFilePath).exists();
            if (!policiesInitialized) {
                Config.io.printError("Failed to initialize policy file: " + policyFilePath);
                Config.fileWriter.log("Policy initialization failed: Policy file not created", Loader.LOG_FILE_NAME);
            }
            Config.fileWriter.log("Default policies initialized successfully", Loader.LOG_FILE_NAME);
            initPolicies = true;
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
            Config.fileWriter.log("Policy initialization error: " + e.getMessage(), Loader.LOG_FILE_NAME);
        }
        return initPolicies;
    }

    /**
     * Logic to create a default administrator account
     */
    private boolean createAdministratorAccount() throws Exception {
        try {
            displaySetupProgress();
            Config.io.println("Creating administrator account...");
            new AccountCreate("system").execute();
            Config.fileWriter.log("Administrator account created successfully", Loader.LOG_FILE_NAME);
            initAdminAccount = true;
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
            Config.fileWriter.log("Admin account creation error: " + e.getMessage(), Loader.LOG_FILE_NAME);
        }
        return initAdminAccount;
    }
}