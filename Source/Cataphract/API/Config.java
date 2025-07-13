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

package Cataphract.API;

import java.io.Console;

import Cataphract.API.Astaroth.Time;
import Cataphract.API.Dragon.AccountCreate;
import Cataphract.API.Dragon.UserAccount;
import Cataphract.API.Astaroth.Calendar;
import Cataphract.API.Minotaur.Cryptography;
import Cataphract.API.Minotaur.PolicyCheck;
import Cataphract.API.Wraith.FileRead;
import Cataphract.API.Wraith.FileWrite;
import Cataphract.API.Wraith.PathUtils;

/**
 * Centralized configuration and dependency provider for the Cataphract shell.
 */
public final class Config {
    // Dependency instances
    public static final Console console;

    public static final FileWrite fileWrite = new FileWrite();
    public static final FileRead fileRead = new FileRead();

    public static final Anvil anvil = new Anvil();
    public static final Build build = new Build();
    public static final IOStreams io = new IOStreams();
    public static final ExceptionHandler exceptionHandler = new ExceptionHandler();

    // DRAGON CLASSES //
    public static final AccountCreate accountCreate = new AccountCreate();
    public static final UserAccount userAccount = new UserAccount();

    // ASTAROTH CLASSES //
    public static final Calendar calendar = new Calendar();
    public static final Time time = new Time();

    // MINOTAUR CLASSES //
    public static final Cryptography cryptography = new Cryptography();
    public static final PolicyCheck policyCheck = new PolicyCheck();

    // WRAITH CLASSES //
    public static final PathUtils pathUtils = new PathUtils();

    // Path constants
    public static final String DB_PATH = io.convertFileSeparator(".|System|Cataphract|Private|Mud.dbx");
    public static final String USER_HOME = io.convertFileSeparator(".|Users|Cataphract|");
    public static final String UPDATE_URL = "https://github.com/DAK404/Cataphract/releases/download/TestBuilds/Cataphract.zip";
    public static final String LOG_FILE_NAME = "ExceptionLog";

    static {
        console = System.console();
        if (console == null) {
            io.printWarning("System.console() is unavailable. Input operations may fail.");
        }
    }

    /**
     * Private constructor to prevent instantiation.
     */
    private Config() {}
}