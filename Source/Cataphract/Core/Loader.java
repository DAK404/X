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

import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import Cataphract.API.Config;
import Cataphract.API.Wraith.FileRead;
import Cataphract.API.Dragon.DatabaseInitializer;
import Cataphract.API.Dragon.AccountCreate;
import Cataphract.API.Minotaur.PolicyManager;
import Cataphract.API.ExceptionHandler;

public class Loader
{
    protected static List<String> abraxisFilePathList = new ArrayList<>();

    public static void main(String[] args) throws Exception
    {
        if (args.length == 0)
        {
            Config.io.printError("No boot mode specified. Aborting...");
            System.exit(1);
        }

        String mode = args[0].toLowerCase();
        BootMode bootMode;

        switch (mode) 
        {
            case "probe":
                bootMode = new ProbeMode();
                break;
            case "normal":
                bootMode = new NormalMode();
                break;
            case "debug":
                if (args.length < 2) 
                {
                    Config.io.printError("Invalid Syntax for debug mode.");
                    System.exit(1);
                }
                bootMode = new DebugMode(args[1]);
                break;
            default:
                Config.io.printError("Invalid Boot Mode. Aborting...");
                System.exit(3);
                return;
        }

        try 
        {
            bootMode.execute();
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
        }
    }
}

interface BootMode {
    void execute() throws Exception;
}

class ProbeMode implements BootMode {
    @Override
    public void execute() {
        System.exit(7);
    }
}

class NormalMode implements BootMode {
    @Override
    public void execute() throws Exception {
        Config.build.viewBuildInfo(false);
        new LoaderLogic().execute();
    }
}

class DebugMode implements BootMode {
    private final String debugOption;

    public DebugMode(String debugOption) {
        this.debugOption = debugOption;
    }

    @Override
    public void execute() throws Exception {
        switch (debugOption.toLowerCase()) {
            case "crash":
                throw new Exception("Simulated crash for debugging.");
            case "astaroth":
                Config.io.println(String.valueOf(Config.time.getUnixEpoch()));
                Config.io.println(String.valueOf(Config.time.getDateTimeUsingSpecifiedFormat("dd-MMMM-yyyy \nEEEE HH:mm:ss")));
                Config.calendar.printCalendar(0, 0);
                Config.calendar.printCalendar(8, 2077);
                System.exit(0);
                break;
            case "Config.io":
                Config.io.printError("This is an error message.");
                Config.io.printWarning("This is a warning message.");
                Config.io.printAttention("This is an attention message.");
                Config.io.printInfo("This is an information message.");
                Config.io.println("This is a normal printline message. Printing the same with colors");
                System.exit(0);
                break;
            default:
                Config.io.printError("Undefined Debug Parameter.");
                break;
        }
    }
}

class LoaderLogic {
    private final IntegrityChecker integrityChecker = new IntegrityChecker();

    public void execute() throws Exception {
        byte result = integrityChecker.checkIntegrity();

        switch (result) {
            case 0:
                Config.io.printInfo("Integrity checks passed. Booting Cataphract...");
                new GuestShell().execute();
                break;
            case 1:
                Config.io.printError("Unable to locate or parse Manifest Files! Aborting boot...");
                System.exit(4);
                break;
            case 2:
                Config.io.printError("Unable to populate the Kernel files! Aborting boot...");
                System.exit(4);
                break;
            case 3:
                Config.io.printError("File Signature verification failed! Aborting boot...");
                System.exit(4);
                break;
            case 4:
                Config.io.printError("File verification failed: Found File Size Discrepancy! Aborting boot...");
                System.exit(4);
                break;
            case 5:
                Setup setup = new Setup();
                if (setup.setupCataphract()) {
                    System.exit(100);
                } else {
                    Config.io.printError("Setup Failed!");
                }
                break;
            default:
                Config.io.printError("Generic Failure. Cannot Boot.");
                System.exit(4);
                break;
        }
    }
}

class IntegrityChecker {
    private List<String> abraxisFilePathList = Loader.abraxisFilePathList;

    public byte checkIntegrity() {
        byte result = 55;

        Config.io.printInfo("Stage 0: Checking Manifest Files...");
        if (manifestFilesCheck()) {
            Config.io.printInfo("Stage 1: Manifest Files Found. Populating Kernel Files and Directories...");
            if (populateKernelFiles(new File("./"))) {
                Config.io.printInfo("Stage 2: Kernel Files and Directories populated. Checking File Integrity - Phase 1...");
                if (checkFileHashes()) {
                    Config.io.printInfo("Stage 3: File Integrity Check - Phase 1 Complete. Checking File Integrity - Phase 2...");
                    if (checkFileSizes()) {
                        result = 0;
                        Config.io.printInfo("Stage 4: File Integrity Check - Phase 2 Complete. Checking System and User Files...");
                        if (!setupStatusCheck()) {
                            result = 5;
                            Config.io.printAttention("Setting up Cataphract...");
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

        abraxisFilePathList.clear();
        System.gc();
        return result;
    }

    private boolean manifestFilesCheck() {
        return new File(Config.io.convertFileSeparator(".|.Manifest|Cataphract|KernelFilesHashes.m1")).exists() &&
               new File(Config.io.convertFileSeparator(".|.Manifest|Cataphract|KernelFiles.m2")).exists();
    }

    private boolean populateKernelFiles(File fileDirectory) {
        try {
            File[] fileList = fileDirectory.listFiles();
            if (fileList == null) return false;

            for (File fileName : fileList) {
                if (fileIgnoreList(fileName.getName())) continue;
                if (fileName.isDirectory()) {
                    populateKernelFiles(fileName);
                } else {
                    abraxisFilePathList.add(fileName.getPath());
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean fileIgnoreList(String fileName) 
    {
        final String[] ignoreList = {".Manifest", "System", "Users", "org", "JRE", "Logs", "BuildSigner.java"};
        for (String files : ignoreList) 
        {
            if (fileName.equalsIgnoreCase(files)) return true;
        }
        return false;
    }

    private boolean checkFileHashes() {
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
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            Config.io.println("Manifest Parse Error! Unable to continue.");
            return false;
        }
    }

    private boolean checkFileSizes() {
        try 
        {
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
                    return false;
                }
                fileCount++;
            }
            if (fileCount < manifestM2Entries.size()) {
                Config.io.printError("Integrity Check Failure. Expected " + manifestM2Entries.size() + ". Found " + fileCount);
                return false;
            }
            return true;
        } catch (Exception e) {
            Config.io.printError("File Operations Failure! Unable to continue.");
            return false;
        }
    }

    private boolean setupStatusCheck() {
        return new File(Config.io.convertFileSeparator(".|System|Cataphract")).exists() && new File(Config.io.convertFileSeparator(".|Users|Cataphract")).exists();
    }
}

class GuestShell {
    private Console console = System.console();

    public void execute() throws Exception {
        String input;
        do {
            input = console.readLine("> ");
            String[] commandArray = Config.io.splitStringToArray(input);

            switch (commandArray[0].toLowerCase()) {
                case "exit":
                case "":
                    break;
                case "clear":
                    Config.build.clearScreen();
                    break;
                case "login":
                    new SycoraxKernel().startSycoraxKernel();
                    Config.build.viewBuildInfo(false);
                    Config.io.println("Logout Successful");
                    break;
                default:
                    Config.io.printError(commandArray[0] + " Command Not Found.");
                    break;
            }
        } while (!input.equalsIgnoreCase("exit"));
    }
}

class Setup 
{
    private boolean prereqInfoStatus = false;
    private boolean initDirs = false;
    private boolean initDB = false;
    private boolean initPolicies = false;
    private boolean initAdminAccount = false;
    private Console console = System.console();

    /**
     * The entry point to the setup. Sets the environment up to run Cataphract.
     * @return boolean returnValue - Returns if the setup was successful or not.
     */
    boolean setupCataphract() throws Exception {
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

        //Build.clearScreen();
        Config.io.confirmReturnToContinue(oobeIntroduction, ".\nSetup> ");
        showAndAcceptEULA();
        createSystemDirectories();
        initializeDatabase();
        initializeDefaultPolicies();
        createAdministratorAccount();
        displaySetupProgress();
        Config.io.confirmReturnToContinue("Setup complete! ", ".\nSetup> ");

        return prereqInfoStatus && initAdminAccount && initDB && initDirs && initPolicies;
    }

    /**
     * Display the progress of the setup to the user: what's pending and what's completed
     */
    private void displaySetupProgress() {
        Config.build.viewBuildInfo(false);
        Config.io.println("[ -- Program Setup Checklist -- ]");
        Config.io.println("[*] Show Program Prerequisites   : " + (prereqInfoStatus ? "COMPLETED" : "PENDING"));
        Config.io.println("[*] Initialize Directories       : " + (initDirs ? "COMPLETED" : "PENDING"));
        Config.io.println("[*] Initialize Database System   : " + (initDB ? "COMPLETED" : "PENDING"));
        Config.io.println("[*] Initialize Program Policies  : " + (initPolicies ? "COMPLETED" : "PENDING"));
        Config.io.println("[*] Create Administrator Account : " + (initAdminAccount ? "COMPLETED" : "PENDING"));
        Config.io.println("[ ----------------------------- ]\n");
    }

    /**
     * Logic to display the EULA to the user. User must accept it to complete the setup.
     */
    private void showAndAcceptEULA() throws Exception {
        displaySetupProgress();
        new FileRead().readHelpFile("EULA");
        if (!console.readLine("Do you accept the EULA? [ Y / N ]\nEULA?> ").equalsIgnoreCase("y")) {
            System.exit(0);
        }
        new FileRead().readHelpFile("LICENSE");
        prereqInfoStatus = true;
    }

    /**
     * Logic to create directories required by Cataphract.
     */
    private void createSystemDirectories() {
        displaySetupProgress();
        Config.io.printInfo("Creating Directories...");
        String[] directoryNames = {".|System|Cataphract|Public|Logs", ".|UsersCataphract"};
        for (String dirs : directoryNames) {
            new File(Config.io.convertFileSeparator(dirs)).mkdirs();
        }
        initDirs = true;
    }

    /**
     * Logic to initialize the database using the Dragon package.
     */
    private void initializeDatabase() {
        displaySetupProgress();
        initDB = DatabaseInitializer.initializeDatabase();
    }

    /**
     * Logic to initialize the default policies for the users.
     */
    private void initializeDefaultPolicies() {
        displaySetupProgress();
        new PolicyManager();
        initPolicies = true;
    }

    /**
     * Logic to create a default administrator account
     */
    private void createAdministratorAccount() throws Exception {
        new AccountCreate().createDefaultAdministratorAccount();
        initAdminAccount = true;
    }
}