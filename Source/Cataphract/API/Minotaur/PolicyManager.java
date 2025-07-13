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

package Cataphract.API.Minotaur;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.SecureRandom;
import java.util.Properties;

import Cataphract.API.Config;
import Cataphract.API.Dragon.Login;

/**
 * Manages policy editing for the Cataphract shell.
 */
public class PolicyManager {
    private final PolicyStorage policyStorage;
    private final Authenticator authenticator;
    private final String policyFilePath;
    private final String[] defaultPolicies = {"update", "download", "script", "filemgmt", "read", "edit", "policy", "account_create", "account_delete", "account_modify"};
    private boolean isUserAdmin;

    /**
     * Constructs a PolicyManager with default XML storage and path.
     */
    public PolicyManager() {
        this(new XmlPolicyStorage(), new LoginAuthenticator(), Config.io.convertFileSeparator(".|System|Cataphract|Private|Policy.burn"));
    }

    /**
     * Constructs a PolicyManager with custom storage, authenticator, and path.
     */
    public PolicyManager(PolicyStorage policyStorage, Authenticator authenticator, String policyFilePath) {
        this.policyStorage = policyStorage;
        this.authenticator = authenticator;
        this.policyFilePath = policyFilePath;
        initializePolicyFile();
    }

    /**
     * Runs the interactive policy editor CLI.
     */
    public void policyEditorLogic() {
        try {
            if (!authenticateUser()) {
                Config.io.printError("Authentication Failure. Exiting...");
                return;
            }
            String policyStatus = Config.policyCheck.retrievePolicyValue("policy");
            if (policyStatus.equalsIgnoreCase("on") || isUserAdmin) {
                runPolicyEditor();
            } else {
                Config.io.printError("Policy Management Disabled: Insufficient Privileges");
            }
        } catch (Exception e) {
            Config.io.printError("Error in policy editor: " + e.getMessage());
            Config.exceptionHandler.handleException(e);
        }
    }

    private void initializePolicyFile() {
        try {
            if (!new File(policyFilePath).exists()) {
                resetPolicyFile();
            }
        } catch (Exception e) {
            Config.io.printError("Error initializing policy file: " + e.getMessage());
            Config.exceptionHandler.handleException(e);
        }
    }

    private boolean authenticateUser() throws Exception {
        Config.build.viewBuildInfo(false);
        Config.io.printAttention("This module requires authentication. Please enter credentials.");
        String username = Config.console.readLine("Username: ");
        if (username == null || username.trim().isEmpty()) {
            Config.io.printError("Username cannot be empty.");
            return false;
        }
        String hashedUsername = Config.cryptography.stringToSHA3_256(username);
        char[] password = Config.console.readPassword("Password: ");
        String hashedPassword = Config.cryptography.stringToSHA3_256(password != null ? new String(password) : "");
        char[] securityKey = Config.console.readPassword("Security Key: ");
        String hashedSecurityKey = Config.cryptography.stringToSHA3_256(securityKey != null ? new String(securityKey) : "");
        boolean authenticated = authenticator.authenticate(hashedUsername, hashedPassword, hashedSecurityKey);
        isUserAdmin = authenticator.isAdmin(hashedUsername);
        return authenticated;
    }

    private void runPolicyEditor() throws Exception {
        String suggestedInputs = "[ MODIFY " + (isUserAdmin ? "| RESET " : "") + "| REFRESH | HELP | EXIT ]";
        Config.build.viewBuildInfo(false);
        displayPolicyInfo(suggestedInputs);
        String input;
        do {
            input = Config.console.readLine("PolicyEditor)> ");
            if (input == null) {
                input = "";
            }
            String[] policyCommandArray = Config.io.splitStringToArray(input);
            if (policyCommandArray.length == 0 || policyCommandArray[0].isEmpty()) {
                continue;
            }
            switch (policyCommandArray[0].toLowerCase()) {
                case "modify":
                    if (policyCommandArray.length < 3) {
                        Config.io.printError("Invalid Syntax: Expected 'modify <policy> <value>'");
                    } else {
                        savePolicy(policyCommandArray[1], policyCommandArray[2]);
                    }
                    break;
                case "reset":
                    if (isUserAdmin) {
                        Config.io.printAttention("Resetting Policy File...");
                        resetPolicyFile();
                    } else {
                        Config.io.printError("Reset restricted to administrators.");
                    }
                    break;
                case "refresh":
                    displayPolicyInfo(suggestedInputs);
                    break;
                case "help":
                    Config.io.println("Commands: " + suggestedInputs);
                    break;
                case "exit":
                    break;
                default:
                    Config.io.printError("Invalid command. Use: " + suggestedInputs);
                    break;
            }
        } while (!input.equalsIgnoreCase("exit"));
    }

    private void displayPolicyInfo(String suggestedInputs) throws Exception {
        Config.build.viewBuildInfo(false);
        Config.io.println("--------------------------------------------");
        Config.io.println("         Minotaur Policy Editor 2.0         ");
        Config.io.println("--------------------------------------------");
        Config.io.println("      - Current Policy Configuration -      ");
        Config.io.println("--------------------------------------------");
        Config.io.println("\nPolicy File  : " + policyFilePath);
        Config.io.println("Policy Format: XML\n");
        Properties props = policyStorage.loadPolicies(policyFilePath);
        props.list(System.out);
        Config.io.println("\n--------------------------------------------\n");
        Config.io.println(suggestedInputs + "\n");
    }

    private void savePolicy(String policyName, String policyValue) throws Exception {
        if (policyName == null || policyValue == null || policyName.trim().isEmpty()) {
            Config.io.printError("Invalid policy name or value.");
            return;
        }
        policyStorage.savePolicy(policyFilePath, policyName, policyValue);
        Config.io.printInfo("Policy '" + policyName + "' set to '" + policyValue + "'.");
    }

    private void resetPolicyFile() throws Exception {
        new File(policyFilePath).delete();
        for (String policy : defaultPolicies) {
            policyStorage.savePolicy(policyFilePath, policy, "on");
        }
        SecureRandom random = new SecureRandom();
        policyStorage.savePolicy(policyFilePath, "sysname", "SYSTEM" + (100000 + random.nextInt(900000)));
        policyStorage.savePolicy(policyFilePath, "module", "off");
        policyStorage.savePolicy(policyFilePath, "policy", "off");
        policyStorage.savePolicy(policyFilePath, "auth", "off");
    }
}

/**
 * Interface for policy storage operations.
 */
interface PolicyStorage {
    Properties loadPolicies(String filePath) throws Exception;
    void savePolicy(String filePath, String policyName, String policyValue) throws Exception;
}

/**
 * XML-based policy storage using Properties.
 */
class XmlPolicyStorage implements PolicyStorage {
    @Override
    public Properties loadPolicies(String filePath) throws Exception {
        try (FileInputStream configStream = new FileInputStream(filePath)) {
            Properties props = new Properties();
            props.loadFromXML(configStream);
            return props;
        }
    }

    @Override
    public void savePolicy(String filePath, String policyName, String policyValue) throws Exception {
        Properties props = new Properties();
        File file = new File(filePath);
        if (file.exists()) {
            try (FileInputStream configStream = new FileInputStream(file)) {
                props.loadFromXML(configStream);
            }
        }
        props.setProperty(policyName, policyValue);
        try (FileOutputStream output = new FileOutputStream(filePath)) {
            props.storeToXML(output, "CataphractSettings");
        }
    }
}

/**
 * Interface for authentication operations.
 */
interface Authenticator {
    boolean authenticate(String username, String password, String securityKey) throws Exception;
    boolean isAdmin(String username) throws Exception;
}

/**
 * Authenticator using Login class.
 */
class LoginAuthenticator implements Authenticator {
    @Override
    public boolean authenticate(String username, String password, String securityKey) throws Exception {
        return new Login(username).authenticationLogic(password, securityKey);
    }

    @Override
    public boolean isAdmin(String username) throws Exception {
        return new Login(username).checkPrivilegeLogic();
    }
}