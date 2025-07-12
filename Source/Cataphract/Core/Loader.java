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

import Cataphract.API.Build;
import Cataphract.API.IOStreams;
import Cataphract.API.Astaroth.Calendar;
import Cataphract.API.Astaroth.Time;
import Cataphract.API.Minotaur.Cryptography;
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
            IOStreams.printError("No boot mode specified. Aborting...");
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
                    IOStreams.printError("Invalid Syntax for debug mode.");
                    System.exit(1);
                }
                bootMode = new DebugMode(args[1]);
                break;
            default:
                IOStreams.printError("Invalid Boot Mode. Aborting...");
                System.exit(3);
                return;
        }

        try 
        {
            bootMode.execute();
        } catch (Exception e) {
            new ExceptionHandler().handleException(e);
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
        Build.viewBuildInfo();
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
                IOStreams.println(String.valueOf(new Time().getUnixEpoch()));
                IOStreams.println(String.valueOf(new Time().getDateTimeUsingSpecifiedFormat("dd-MMMM-yyyy \nEEEE HH:mm:ss")));
                new Calendar().printCalendar(0, 0);
                new Calendar().printCalendar(8, 2077);
                System.exit(0);
                break;
            case "iostreams":
                IOStreams.printError("This is an error message.");
                IOStreams.printWarning("This is a warning message.");
                IOStreams.printAttention("This is an attention message.");
                IOStreams.printInfo("This is an information message.");
                IOStreams.println("This is a normal printline message. Printing the same with colors");
                for (int i = 0; i < 9; i++) {
                    for (int j = 0; j < 9; j++) {
                        IOStreams.print(i, j, "testing IOStreams");
                        System.out.print(" ");
                    }
                    System.out.println();
                }
                System.exit(0);
                break;
            default:
                IOStreams.printError("Undefined Debug Parameter.");
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
                IOStreams.printInfo("Integrity checks passed. Booting Cataphract...");
                new GuestShell().execute();
                break;
            case 1:
                IOStreams.printError("Unable to locate or parse Manifest Files! Aborting boot...");
                System.exit(4);
                break;
            case 2:
                IOStreams.printError("Unable to populate the Kernel files! Aborting boot...");
                System.exit(4);
                break;
            case 3:
                IOStreams.printError("File Signature verification failed! Aborting boot...");
                System.exit(4);
                break;
            case 4:
                IOStreams.printError("File verification failed: Found File Size Discrepancy! Aborting boot...");
                System.exit(4);
                break;
            case 5:
                Setup setup = new Setup();
                if (setup.setupCataphract()) {
                    System.exit(100);
                } else {
                    IOStreams.printError("Setup Failed!");
                }
                break;
            default:
                IOStreams.printError("Generic Failure. Cannot Boot.");
                System.exit(4);
                break;
        }
    }
}

class IntegrityChecker {
    private List<String> abraxisFilePathList = Loader.abraxisFilePathList;

    public byte checkIntegrity() {
        byte result = 55;

        IOStreams.printInfo("Stage 0: Checking Manifest Files...");
        if (manifestFilesCheck()) {
            IOStreams.printInfo("Stage 1: Manifest Files Found. Populating Kernel Files and Directories...");
            if (populateKernelFiles(new File("./"))) {
                IOStreams.printInfo("Stage 2: Kernel Files and Directories populated. Checking File Integrity - Phase 1...");
                if (checkFileHashes()) {
                    IOStreams.printInfo("Stage 3: File Integrity Check - Phase 1 Complete. Checking File Integrity - Phase 2...");
                    if (checkFileSizes()) {
                        result = 0;
                        IOStreams.printInfo("Stage 4: File Integrity Check - Phase 2 Complete. Checking System and User Files...");
                        if (!setupStatusCheck()) {
                            result = 5;
                            IOStreams.printAttention("Setting up Cataphract...");
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
        return new File(IOStreams.convertFileSeparator(".|.Manifest|Cataphract|KernelFilesHashes.m1")).exists() &&
               new File(IOStreams.convertFileSeparator(".|.Manifest|Cataphract|KernelFiles.m2")).exists();
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
            FileInputStream m1FileStream = new FileInputStream(IOStreams.convertFileSeparator(".|.Manifest|Cataphract|KernelFilesHashes.m1"));
            manifestM1Entries.loadFromXML(m1FileStream);
            m1FileStream.close();

            for (String fileName : abraxisFilePathList) {
                if (fileIgnoreList(fileName)) continue;
                String kernelFileHash = Cryptography.fileToSHA3_256(new File(fileName));
                String manifestHash = manifestM1Entries.getProperty(IOStreams.convertToNionSeparator(fileName));
                if (manifestHash == null || !manifestHash.equals(kernelFileHash)) {
                    IOStreams.printError("Integrity Check Failure at " + kernelFileHash + "\t" + fileName);
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            IOStreams.println("Manifest Parse Error! Unable to continue.");
            return false;
        }
    }

    private boolean checkFileSizes() {
        try 
        {
            Properties manifestM2Entries = new Properties();
            FileInputStream m2FileStream = new FileInputStream(IOStreams.convertFileSeparator(".|.Manifest|Cataphract|KernelFiles.m2"));
            manifestM2Entries.loadFromXML(m2FileStream);
            m2FileStream.close();

            int fileCount = 0;
            for (String fileName : abraxisFilePathList) {
                if (!fileName.endsWith(".class")) continue;
                long fileSizeM2 = Long.parseLong(manifestM2Entries.getProperty(IOStreams.convertToNionSeparator(fileName), "-1"));
                long fileSize = new File(fileName).length();
                if (fileSize != fileSizeM2) {
                    IOStreams.printError("Integrity Check Failure at " + fileName + "\t" + fileSize + ". Expected " + fileSizeM2);
                    return false;
                }
                fileCount++;
            }
            if (fileCount < manifestM2Entries.size()) {
                IOStreams.printError("Integrity Check Failure. Expected " + manifestM2Entries.size() + ". Found " + fileCount);
                return false;
            }
            return true;
        } catch (Exception e) {
            IOStreams.printError("File Operations Failure! Unable to continue.");
            return false;
        }
    }

    private boolean setupStatusCheck() {
        return new File(IOStreams.convertFileSeparator(".|System|Cataphract")).exists() && new File(IOStreams.convertFileSeparator(".|Users|Cataphract")).exists();
    }
}

class GuestShell {
    private Console console = System.console();

    public void execute() throws Exception {
        String input;
        do {
            input = console.readLine("> ");
            String[] commandArray = IOStreams.splitStringToArray(input);

            switch (commandArray[0].toLowerCase()) {
                case "exit":
                case "":
                    break;
                case "clear":
                    Build.clearScreen();
                    break;
                case "login":
                    new SycoraxKernel().startSycoraxKernel();
                    Build.viewBuildInfo();
                    IOStreams.println(3, 0, "Logout Successful");
                    break;
                default:
                    IOStreams.printError(commandArray[0] + " Command Not Found.");
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
        String oobeIntroduction = Build._Branding + """

        Welcome to Cataphract!

        As this is the first time the program is being run, several setup steps need to be completed for normal use.

        [*] ACCEPT EULA: Agree to the End User License Agreement to begin the setup.
        [*] CREATE SYSTEM DIRECTORIES: Create directories essential for Cataphract to function as expected.
        [*] INITIALIZE DATABASE: Initialize the user database to store the user credentials.
        [*] CREATE ADMINISTRATOR ACCOUNT: Create the administrator account to engage and maintain the functioning of the system.

        These steps are required to be performed by the system administrator. If the current user is an end user,
        please press [ CTRL + C ] keys and contact the System Administrator.

        If the current user is a System Administrator,\u00A0""";

        Build.clearScreen();
        IOStreams.confirmReturnToContinue(oobeIntroduction, ".\nSetup> ");
        showAndAcceptEULA();
        createSystemDirectories();
        initializeDatabase();
        initializeDefaultPolicies();
        createAdministratorAccount();
        displaySetupProgress();
        IOStreams.confirmReturnToContinue("Setup complete! ", ".\nSetup> ");

        return prereqInfoStatus && initAdminAccount && initDB && initDirs && initPolicies;
    }

    /**
     * Display the progress of the setup to the user: what's pending and what's completed
     */
    private void displaySetupProgress() {
        Build.viewBuildInfo();
        IOStreams.println("[ -- Program Setup Checklist -- ]");
        IOStreams.println("[*] Show Program Prerequisites   : " + (prereqInfoStatus ? "COMPLETED" : "PENDING"));
        IOStreams.println("[*] Initialize Directories       : " + (initDirs ? "COMPLETED" : "PENDING"));
        IOStreams.println("[*] Initialize Database System   : " + (initDB ? "COMPLETED" : "PENDING"));
        IOStreams.println("[*] Initialize Program Policies  : " + (initPolicies ? "COMPLETED" : "PENDING"));
        IOStreams.println("[*] Create Administrator Account : " + (initAdminAccount ? "COMPLETED" : "PENDING"));
        IOStreams.println("[ ----------------------------- ]\n");
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
        IOStreams.printInfo("Creating Directories...");
        String[] directoryNames = {".|System|Cataphract|Public|Logs", ".|UsersCataphract"};
        for (String dirs : directoryNames) {
            new File(IOStreams.convertFileSeparator(dirs)).mkdirs();
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