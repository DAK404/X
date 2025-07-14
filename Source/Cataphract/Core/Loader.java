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
package Cataphract.Core;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import Cataphract.API.Config;
import Cataphract.API.Wraith.FileRead;
import Cataphract.API.Wraith.FileWrite;
import Cataphract.API.Dragon.DatabaseInitializer;
import Cataphract.API.Dragon.Login;
import Cataphract.API.Minotaur.PolicyManager;
import Cataphract.API.Dragon.AccountCreate;

/**
 * Initializes the Cataphract shell with different boot modes.
 *
 * @author DAK404 (https://github.com/DAK404)
 * @version 1.5.0 (14-July-2025, Cataphract)
 * @since 0.0.1 (Cataphract 0.0.1)
 */
public class Loader {
    protected static final String LOG_FILE_NAME = "LoaderLog";
    private final FileWrite fileWrite;
    private final BootModeFactory bootModeFactory;

    /**
     * Constructs a Loader with injected dependencies.
     *
     * @param Config.io   The IO streams handler for console output.
     * @param fileWrite   The file write handler for logging.
     * @param bootModeFactory The factory for creating boot modes.
     */
    public Loader(FileWrite fileWrite, BootModeFactory bootModeFactory) {
        this.fileWrite = fileWrite;
        this.bootModeFactory = bootModeFactory;
    }

    /**
     * Main entry point for Cataphract boot process.
     *
     * @param args Command-line arguments specifying the boot mode.
     * @throws Exception If an error occurs during boot.
     */
    public static void main(String[] args) throws Exception {
        FileWrite fileWrite = new FileWrite(null);
        Loader loader = new Loader(fileWrite, new DefaultBootModeFactory());
        loader.boot(args);
    }

    /**
     * Executes the boot process based on provided arguments.
     *
     * @param args Command-line arguments specifying the boot mode.
     * @throws Exception If an error occurs during boot.
     */
    public void boot(String[] args) throws Exception {
        try {
            if (args.length == 0) {
                Config.io.printError("No boot mode specified. Aborting...");
                fileWrite.log("Boot failed: No boot mode specified", LOG_FILE_NAME);
                System.exit(1);
                return;
            }

            String mode = args[0].toLowerCase();
            fileWrite.log("Starting Loader in mode: " + mode, LOG_FILE_NAME);
            BootMode bootMode = bootModeFactory.createBootMode(mode, args);
            bootMode.execute();
            fileWrite.log("Boot mode " + mode + " executed", LOG_FILE_NAME);
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
            fileWrite.log("Loader error: " + e.getMessage(), LOG_FILE_NAME);
            throw e;
        }
    }
}

/**
 * Interface for boot modes to ensure extensibility.
 */
interface BootMode {
    void execute() throws Exception;
}

/**
 * Factory for creating boot modes.
 */
interface BootModeFactory {
    BootMode createBootMode(String mode, String[] args) throws Exception;
}

/**
 * Default implementation of BootModeFactory.
 */
class DefaultBootModeFactory implements BootModeFactory {
    @Override
    public BootMode createBootMode(String mode, String[] args) throws Exception {
        FileWrite fileWrite = new FileWrite(null);
        switch (mode) {
            case "probe":
                return new ProbeMode(fileWrite);
            case "normal":
                return new NormalMode(fileWrite);
            case "debug":
                if (args.length < 2) {
                    Config.io.printError("Invalid Syntax for debug mode.");
                    fileWrite.log("Boot failed: Invalid debug mode syntax", Loader.LOG_FILE_NAME);
                    System.exit(1);
                }
                return new DebugMode(args[1],  fileWrite);
            default:
                Config.io.printError("Invalid Boot Mode. Aborting...");
                fileWrite.log("Boot failed: Invalid boot mode - " + mode, Loader.LOG_FILE_NAME);
                System.exit(3);
                throw new IllegalArgumentException("Invalid boot mode: " + mode);
        }
    }
}

/**
 * Boot mode for probing system state.
 */
class ProbeMode implements BootMode {
    private final FileWrite fileWrite;

    public ProbeMode(FileWrite fileWrite) {
        this.fileWrite = fileWrite;
    }

    @Override
    public void execute() throws Exception {
        fileWrite.log("Executing ProbeMode", Loader.LOG_FILE_NAME);
        System.exit(7);
    }
}

/**
 * Boot mode for normal operation.
 */
class NormalMode implements BootMode {
    private final FileWrite fileWrite;

    public NormalMode(FileWrite fileWrite) {
        this.fileWrite = fileWrite;
    }

    @Override
    public void execute() throws Exception {
        Config.build.viewBuildInfo(false);
        fileWrite.log("Executing NormalMode", Loader.LOG_FILE_NAME);
        new LoaderLogic(fileWrite).execute();
    }
}

/**
 * Boot mode for debugging with specified options.
 */
class DebugMode implements BootMode {
    private final String debugOption;
    private final FileWrite fileWrite;

    public DebugMode(String debugOption, FileWrite fileWrite) {
        this.debugOption = debugOption;
        this.fileWrite = fileWrite;
    }

    @Override
    public void execute() throws Exception {
        fileWrite.log("Executing DebugMode with option: " + debugOption, Loader.LOG_FILE_NAME);
        switch (debugOption.toLowerCase()) {
            case "crash":
                throw new Exception("Simulated crash for debugging.");
            case "astaroth":
                Config.io.println(String.valueOf(Config.time.getUnixEpoch()));
                Config.io.println(Config.time.getDateTimeUsingSpecifiedFormat("dd-MMMM-yyyy \nEEEE HH:mm:ss"));
                Config.calendar.printCalendar(0, 0);
                Config.calendar.printCalendar(8, 2077);
                fileWrite.log("DebugMode: Astaroth calendar printed", Loader.LOG_FILE_NAME);
                System.exit(0);
                break;
            case "io":
                Config.io.printError("This is an error message.");
                Config.io.printWarning("This is a warning message.");
                Config.io.printAttention("This is an attention message.");
                Config.io.printInfo("This is an information message.");
                Config.io.println("This is a normal printline message. Printing the same with colors");
                fileWrite.log("DebugMode: IO test messages printed", Loader.LOG_FILE_NAME);
                System.exit(0);
                break;
            default:
                Config.io.printError("Undefined Debug Parameter.");
                fileWrite.log("DebugMode error: Undefined parameter - " + debugOption, Loader.LOG_FILE_NAME);
                System.exit(1);
                break;
        }
    }
}

/**
 * Logic for executing integrity checks and booting the shell.
 */
class LoaderLogic {
    private final FileWrite fileWrite;
    private final IntegrityChecker integrityChecker;

    public LoaderLogic(FileWrite fileWrite) {
        this.fileWrite = fileWrite;
        this.integrityChecker = new IntegrityChecker(fileWrite);
    }

    public void execute() throws Exception {
        fileWrite.log("Starting LoaderLogic integrity checks", Loader.LOG_FILE_NAME);
        byte result = integrityChecker.checkIntegrity();

        switch (result) {
            case 0:
                Config.io.printInfo("Integrity checks passed. Booting Cataphract...");
                fileWrite.log("Integrity checks passed, booting GuestShell", Loader.LOG_FILE_NAME);
                new GuestShell(fileWrite).execute();
                break;
            case 1:
                Config.io.printError("Unable to locate or parse Manifest Files! Aborting boot...");
                fileWrite.log("Integrity check failed: Missing manifest files", Loader.LOG_FILE_NAME);
                System.exit(4);
                break;
            case 2:
                Config.io.printError("Unable to populate the Kernel files! Aborting boot...");
                fileWrite.log("Integrity check failed: Failed to populate kernel files", Loader.LOG_FILE_NAME);
                System.exit(4);
                break;
            case 3:
                Config.io.printError("File Signature verification failed! Aborting boot...");
                fileWrite.log("Integrity check failed: File signature verification", Loader.LOG_FILE_NAME);
                System.exit(4);
                break;
            case 4:
                Config.io.printError("File verification failed: Found File Size Discrepancy! Aborting boot...");
                fileWrite.log("Integrity check failed: File size discrepancy", Loader.LOG_FILE_NAME);
                System.exit(4);
                break;
            case 5:
                fileWrite.log("Initiating Cataphract setup", Loader.LOG_FILE_NAME);
                Setup setup = new Setup(fileWrite);
                if (setup.setupCataphract()) {
                    fileWrite.log("Setup completed, restarting", Loader.LOG_FILE_NAME);
                    System.exit(211);
                } else {
                    Config.io.printError("Setup Failed!");
                    fileWrite.log("Setup failed", Loader.LOG_FILE_NAME);
                    System.exit(4);
                }
                break;
            default:
                Config.io.printError("Generic Failure. Cannot Boot.");
                fileWrite.log("Integrity check failed: Generic failure", Loader.LOG_FILE_NAME);
                System.exit(4);
                break;
        }
    }
}

/**
 * Performs integrity checks for Cataphract boot.
 */
class IntegrityChecker {
    private final FileWrite fileWrite;
    private final Set<String> kernelFilePaths;

    public IntegrityChecker(FileWrite fileWrite) {
        this.fileWrite = fileWrite;
        this.kernelFilePaths = new HashSet<>();
    }

    public byte checkIntegrity() throws Exception {
        try {
            fileWrite.log("Starting integrity checks", Loader.LOG_FILE_NAME);
            byte result = 55;

            Config.io.printInfo("Stage 0: Checking Manifest Files...");
            fileWrite.log("Checking manifest files", Loader.LOG_FILE_NAME);
            if (manifestFilesCheck()) {
                Config.io.printInfo("Stage 1: Manifest Files Found. Populating Kernel Files and Directories...");
                fileWrite.log("Populating kernel files", Loader.LOG_FILE_NAME);
                if (populateKernelFiles(new File("./"))) {
                    Config.io.printInfo("Stage 2: Kernel Files and Directories populated. Checking File Integrity - Phase 1...");
                    fileWrite.log("Checking file hashes", Loader.LOG_FILE_NAME);
                    if (checkFileHashes()) {
                        Config.io.printInfo("Stage 3: File Integrity Check - Phase 1 Complete. Checking File Integrity - Phase 2...");
                        fileWrite.log("Checking file sizes", Loader.LOG_FILE_NAME);
                        if (checkFileSizes()) {
                            Config.io.printInfo("Stage 4: File Integrity Check - Phase 2 Complete. Checking System and User Files...");
                            fileWrite.log("Checking system and user files", Loader.LOG_FILE_NAME);
                            if (!setupStatusCheck()) {
                                result = 5;
                                Config.io.printAttention("Setting up Cataphract...");
                                fileWrite.log("Setup required", Loader.LOG_FILE_NAME);
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

            fileWrite.log("Integrity check result: " + result, Loader.LOG_FILE_NAME);
            kernelFilePaths.clear();
            System.gc();
            return result;
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
            fileWrite.log("Integrity check error: " + e.getMessage(), Loader.LOG_FILE_NAME);
            throw e;
        }
    }

    private boolean manifestFilesCheck() throws Exception {
        boolean exists = new File(Config.io.convertFileSeparator(".|.Manifest|Cataphract|KernelFilesHashes.m1")).exists() &&
                        new File(Config.io.convertFileSeparator(".|.Manifest|Cataphract|KernelFiles.m2")).exists();
        fileWrite.log("Manifest files check: " + (exists ? "Passed" : "Failed"), Loader.LOG_FILE_NAME);
        return exists;
    }

    private boolean populateKernelFiles(File fileDirectory) throws Exception {
        File[] fileList = fileDirectory.listFiles();
        if (fileList == null) {
            fileWrite.log("Failed to list files in directory: " + fileDirectory.getPath(), Loader.LOG_FILE_NAME);
            return false;
        }

        for (File fileName : fileList) {
            if (fileIgnoreList(fileName.getName())) continue;
            if (fileName.isDirectory()) {
                populateKernelFiles(fileName);
            } else {
                kernelFilePaths.add(fileName.getPath());
            }
        }
        fileWrite.log("Populated " + kernelFilePaths.size() + " kernel files", Loader.LOG_FILE_NAME);
        return true;
    }

    private boolean fileIgnoreList(String fileName) {
        final String[] ignoreList = {".Manifest", "System", "Users", "org", "JRE", "Logs", "BuildSigner.java"};
        for (String files : ignoreList) {
            if (fileName.equalsIgnoreCase(files)) return true;
        }
        return false;
    }

    private boolean checkFileHashes() throws Exception {
        Properties manifestM1Entries = new Properties();
        try (FileInputStream m1FileStream = new FileInputStream(Config.io.convertFileSeparator(".|.Manifest|Cataphract|KernelFilesHashes.m1"))) {
            manifestM1Entries.loadFromXML(m1FileStream);
        }

        for (String fileName : kernelFilePaths) {
            if (fileIgnoreList(fileName)) continue;
            String kernelFileHash = Config.cryptography.fileToSHA3_256(new File(fileName));
            String manifestHash = manifestM1Entries.getProperty(Config.io.convertToNionSeparator(fileName));
            if (manifestHash == null || !manifestHash.equals(kernelFileHash)) {
                Config.io.printError("Integrity Check Failure at " + kernelFileHash + "\t" + fileName);
                fileWrite.log("File hash check failed for: " + fileName, Loader.LOG_FILE_NAME);
                return false;
            }
        }
        fileWrite.log("File hash check passed", Loader.LOG_FILE_NAME);
        return true;
    }

    private boolean checkFileSizes() throws Exception {
        Properties manifestM2Entries = new Properties();
        try (FileInputStream m2FileStream = new FileInputStream(Config.io.convertFileSeparator(".|.Manifest|Cataphract|KernelFiles.m2"))) {
            manifestM2Entries.loadFromXML(m2FileStream);
        }

        int fileCount = 0;
        for (String fileName : kernelFilePaths) {
            if (!fileName.endsWith(".class")) continue;
            long fileSizeM2 = Long.parseLong(manifestM2Entries.getProperty(Config.io.convertToNionSeparator(fileName), "-1"));
            long fileSize = new File(fileName).length();
            if (fileSize != fileSizeM2) {
                Config.io.printError("Integrity Check Failure at " + fileName + "\t" + fileSize + ". Expected " + fileSizeM2);
                fileWrite.log("File size check failed for: " + fileName, Loader.LOG_FILE_NAME);
                return false;
            }
            fileCount++;
        }
        if (fileCount < manifestM2Entries.size()) {
            Config.io.printError("Integrity Check Failure. Expected " + manifestM2Entries.size() + ". Found " + fileCount);
            fileWrite.log("File size check failed: File count mismatch", Loader.LOG_FILE_NAME);
            return false;
        }
        fileWrite.log("File size check passed", Loader.LOG_FILE_NAME);
        return true;
    }

    private boolean setupStatusCheck() throws Exception {
        boolean exists = new File(Config.io.convertFileSeparator(".|System|Cataphract")).exists() &&
                        new File(Config.io.convertFileSeparator(".|Users|Cataphract")).exists();
        fileWrite.log("Setup status check: " + (exists ? "Passed" : "Failed"), Loader.LOG_FILE_NAME);
        return exists;
    }
}

/**
 * Handles Cataphract setup process.
 */
class Setup {
    private final FileWrite fileWrite;
    private boolean prereqInfoStatus = false;
    private boolean initDirs = false;
    private boolean initDB = false;
    private boolean initPolicies = false;
    private boolean initAdminAccount = false;

    public Setup(FileWrite fileWrite) {
        this.fileWrite = fileWrite;
    }

    /**
     * Sets up the Cataphract environment.
     *
     * @return true if setup is successful, false otherwise.
     * @throws Exception If an error occurs during setup.
     */
    public boolean setupCataphract() throws Exception {
        fileWrite.log("Starting Cataphract setup", Loader.LOG_FILE_NAME);
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
        fileWrite.log("Displayed setup introduction", Loader.LOG_FILE_NAME);

        if (!showAndAcceptEULA()) {
            fileWrite.log("Setup failed: EULA not accepted", Loader.LOG_FILE_NAME);
            return false;
        }
        if (!createSystemDirectories()) {
            fileWrite.log("Setup failed: Directory creation failed", Loader.LOG_FILE_NAME);
            return false;
        }
        if (!initializeDatabase()) {
            fileWrite.log("Setup failed: Database initialization failed", Loader.LOG_FILE_NAME);
            return false;
        }
        if (!initializeDefaultPolicies()) {
            fileWrite.log("Setup failed: Policy initialization failed", Loader.LOG_FILE_NAME);
            return false;
        }
        if (!createAdministratorAccount()) {
            fileWrite.log("Setup failed: Admin account creation failed", Loader.LOG_FILE_NAME);
            return false;
        }

        displaySetupProgress();
        Config.io.confirmReturnToContinue("Setup complete! ", ".\nSetup> ");
        fileWrite.log("Setup completed successfully", Loader.LOG_FILE_NAME);
        return prereqInfoStatus && initAdminAccount && initDB && initDirs && initPolicies;
    }

    private void displaySetupProgress() throws Exception {
        Config.build.viewBuildInfo(false);
        Config.io.println("[ -- Program Setup Checklist -- ]");
        Config.io.println("[*] Show Program Prerequisites   : " + (prereqInfoStatus ? "COMPLETED" : "PENDING"));
        Config.io.println("[*] Initialize Directories       : " + (initDirs ? "COMPLETED" : "PENDING"));
        Config.io.println("[*] Initialize Database System   : " + (initDB ? "COMPLETED" : "PENDING"));
        Config.io.println("[*] Initialize Program Policies  : " + (initPolicies ? "COMPLETED" : "PENDING"));
        Config.io.println("[*] Create Administrator Account : " + (initAdminAccount ? "COMPLETED" : "PENDING"));
        Config.io.println("[ ----------------------------- ]\n");
        fileWrite.log("Displayed setup progress", Loader.LOG_FILE_NAME);
    }

    private boolean showAndAcceptEULA() throws Exception {
        displaySetupProgress();
        FileRead fileRead = new FileRead(new Login(null));
        Config.io.println("Please read the End User License Agreement:");
        //Config.fileRead.readHelpFile("EULA");
        fileRead.execute(new String[]{"help", "EULA"});
        String input = Config.console.readLine("Do you accept the EULA? [ Y / N ]\nEULA?> ").toLowerCase();
        boolean accepted = input.equals("y") || input.equals("yes");
        fileWrite.log("EULA acceptance: " + (accepted ? "Accepted" : "Not accepted"), Loader.LOG_FILE_NAME);
        if (accepted) {
            fileRead.execute(new String[]{"help", "LICENSE"});
            prereqInfoStatus = true;
        }
        return prereqInfoStatus;
    }

    private boolean createSystemDirectories() throws Exception {
        Config.io.printInfo("Creating Directories...");
        String[] directoryNames = {
            ".|System|Cataphract|Private|",
            ".|System|Cataphract|Public|Logs|",
            ".|Users|Cataphract|",
            ".|.Manifest|Cataphract|",
            ".|docs|Cataphract|Help|",
        };
        for (String dir : directoryNames) {
            File directory = new File(Config.io.convertFileSeparator(dir));
            if (!directory.exists() && !directory.mkdirs()) {
                Config.io.printError("Failed to create directory: " + dir);
                fileWrite.log("Directory creation failed: " + dir, Loader.LOG_FILE_NAME);
                initDirs = false;
                break;
            }
        }
        fileWrite.log("System directories created successfully", Loader.LOG_FILE_NAME);
        initDirs = true;
        return initDirs;
    }

    private boolean initializeDatabase() throws Exception {
        initDB = DatabaseInitializer.initializeDatabase();
        fileWrite.log("Database initialized " + (initDB ? "successfully" : "unsuccessfully"), Loader.LOG_FILE_NAME);
        return initDB;
    }

    private boolean initializeDefaultPolicies() throws Exception {
        Config.io.printInfo("Initializing default policies...");
        new PolicyManager().initializePolicyFile();
        String policyFilePath = Config.io.convertFileSeparator(".|System|Cataphract|Private|Policy.burn");
        boolean policiesInitialized = new File(policyFilePath).exists();
        if (!policiesInitialized) {
            Config.io.printError("Failed to initialize policy file: " + policyFilePath);
            fileWrite.log("Policy initialization failed: Policy file not created", Loader.LOG_FILE_NAME);
        }
        fileWrite.log("Default policies initialized successfully", Loader.LOG_FILE_NAME);
        initPolicies = true;
        return initPolicies;
    }

    private boolean createAdministratorAccount() throws Exception {
        Config.io.println("Creating administrator account...");
        new AccountCreate("Administrator").createDefaultAdministratorAccount();
        fileWrite.log("Administrator account created successfully", Loader.LOG_FILE_NAME);
        initAdminAccount = true;
        return initAdminAccount;
    }
}

/**
 * Guest shell for unauthenticated users.
 */
class GuestShell {
    private final FileWrite fileWrite;

    public GuestShell(FileWrite fileWrite) {
        this.fileWrite = fileWrite;
    }

    public void execute() throws Exception {
        fileWrite.log("Starting GuestShell", Loader.LOG_FILE_NAME);
        String input;
        do {
            input = Config.console.readLine("> ");
            String[] commandArray = Config.io.splitStringToArray(input);
            fileWrite.log("GuestShell command: " + input, Loader.LOG_FILE_NAME);

            switch (commandArray[0].toLowerCase()) {
                case "exit":
                case "":
                    break;
                case "clear":
                    Config.build.clearScreen();
                    fileWrite.log("GuestShell: Cleared screen", Loader.LOG_FILE_NAME);
                    break;
                case "login":
                    new SycoraxKernel(fileWrite).startSycoraxKernel();
                    Config.build.viewBuildInfo(false);
                    Config.io.println("Logout Successful");
                    fileWrite.log("GuestShell: Login completed, logged out", Loader.LOG_FILE_NAME);
                    break;
                default:
                    Config.io.printError(commandArray[0] + " Command Not Found.");
                    fileWrite.log("GuestShell error: Command not found - " + commandArray[0], Loader.LOG_FILE_NAME);
                    break;
            }
        } while (!input.equalsIgnoreCase("exit"));
        fileWrite.log("GuestShell terminated", Loader.LOG_FILE_NAME);
    }
}