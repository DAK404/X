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

import java.io.FileInputStream;
import java.util.Properties;

import Cataphract.API.Config;

/**
 * Manages policy value retrieval for the Cataphract shell.
 */
public class PolicyCheck {
    private final PolicyProvider policyProvider;
    private final String policyFilePath;

    /**
     * Constructs a PolicyCheck with default XML policy provider and path.
     */
    public PolicyCheck() {
        this(new XmlPolicyProvider(), Config.io.convertFileSeparator(".|System|Cataphract|Private|Policy.burn"));
    }

    /**
     * Constructs a PolicyCheck with custom provider and file path.
     * @param policyProvider The policy provider implementation.
     * @param policyFilePath The path to the policy file.
     */
    public PolicyCheck(PolicyProvider policyProvider, String policyFilePath) {
        this.policyProvider = policyProvider;
        this.policyFilePath = policyFilePath;
    }

    /**
     * Retrieves the policy value for the specified parameter.
     * @param policyParameter The policy key to look up.
     * @return The policy value, or "error" if not found or an error occurs.
     */
    public String retrievePolicyValue(String policyParameter) {
        if (policyParameter == null || policyParameter.trim().isEmpty()) {
            Config.io.printError("Invalid policy parameter: null or empty.");
            return "error";
        }
        try {
            String value = policyProvider.retrievePolicy(policyFilePath, policyParameter);
            return value != null ? value : "error";
        } catch (Exception e) {
            Config.io.printError("Error retrieving policy '" + policyParameter + "': " + e.getMessage());
            Config.exceptionHandler.handleException(e);
            return "error";
        }
    }
}

/**
 * Interface for policy value retrieval.
 */
interface PolicyProvider {
    String retrievePolicy(String filePath, String policyParameter) throws Exception;
}

/**
 * XML-based policy provider using Properties.
 */
class XmlPolicyProvider implements PolicyProvider {
    @Override
    public String retrievePolicy(String filePath, String policyParameter) throws Exception {
        try (FileInputStream configStream = new FileInputStream(filePath)) {
            Properties prop = new Properties();
            prop.loadFromXML(configStream);
            return prop.getProperty(policyParameter);
        }
    }
}