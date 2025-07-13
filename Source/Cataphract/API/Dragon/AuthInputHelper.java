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

package Cataphract.API.Dragon;

import java.io.Console;

import Cataphract.API.Config;

/**
 * Utility class for reading authentication inputs securely.
 */
public final class AuthInputHelper 
{
    private AuthInputHelper() {} // Prevent instantiation

    /**
     * Reads username, password, and security key from the console.
     * @param console The console instance.
     * @return An array containing [username, hashedPassword, hashedSecurityKey], or null if username is invalid.
     * @throws Exception If there is an error during handling user credentials input
     */
    public static String[] readCredentials(Console console) throws Exception {
        String username = console.readLine("> Username: ");
        if (username == null || username.trim().isEmpty()) {
            return null; // Signal invalid input
        }
        username = Config.cryptography.stringToSHA3_256(username);
        char[] passwordChars = console.readPassword("Password: ");
        String password = passwordChars != null ? Config.cryptography.stringToSHA3_256(String.valueOf(passwordChars)) : "";
        char[] securityKeyChars = console.readPassword("Security Key (press ENTER to skip): ");
        String securityKey = securityKeyChars != null && securityKeyChars.length > 0 
            ? Config.cryptography.stringToSHA3_256(String.valueOf(securityKeyChars)) 
            : "";
        return new String[] { username, password, securityKey };
    }
}