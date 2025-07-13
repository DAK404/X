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

import java.lang.management.ManagementFactory;

/**
 * Manages build information display, screen clearing, and debugging for Cataphract.
 */
public class Build {
    private final BrandingManager brandingManager;
    private final ScreenClearer screenClearer;
    private final DebugManager debugManager;
    private final IOStreams ioStreams;

    /**
     * Constructs a Build instance with default components and ANSI screen clearing.
     */
    public Build() {
        this(new ANSIScreenClearer(), false);
    }

    /**
     * Constructs a Build instance with configurable screen clearer and ANSI preference.
     * @param useANSIClear If true, prefers ANSI screen clearing; otherwise, uses process-based clearing.
     */
    public Build(boolean useANSIClear) {
        this(useANSIClear ? new ANSIScreenClearer() : new ProcessScreenClearer(), false);
    }

    /**
     * Constructs a Build instance with custom components.
     * @param screenClearer The screen clearer implementation.
     * @param debugEnabled If true, enables debug output by default.
     */
    public Build(ScreenClearer screenClearer, boolean debugEnabled) {
        this.brandingManager = new BrandingManager();
        this.screenClearer = screenClearer;
        this.debugManager = new DebugManager(debugEnabled);
        this.ioStreams = new IOStreams();
    }

    /**
     * Displays build information, optionally with debug output.
     * @param includeDebug Whether to include debug information.
     */
    public void viewBuildInfo(boolean includeDebug) {
        try {
            screenClearer.clearScreen();
            brandingManager.displayBranding(ioStreams);
            if (includeDebug) {
                debugManager.displayDebugInfo(ioStreams);
            }
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
        }
    }

    /**
     * Clears the terminal screen.
     */
    public void clearScreen() {
        try {
            screenClearer.clearScreen();
        } catch (Exception e) {
            Config.exceptionHandler.handleException(e);
        }
    }
}

/**
 * Manages branding and build information.
 */
class BrandingManager {
    private static final String BRANDING = """
                                                      |
                                                     ||
  |||||| ||||||||| |||||||| ||||||||| |||||||  |||  ||| ||||||| |||||||||  |||||| |||||||||
 |||            ||    |||          ||       || |||  |||       ||       || |||        |||
 |||      ||||||||    |||    ||||||||  ||||||  ||||||||  ||||||  |||||||| |||        |||
 |||      |||  |||    |||    |||  |||  |||     |||  |||  ||  ||  |||  ||| |||        |||
  ||||||  |||  |||    |||    |||  |||  |||     |||  |||  ||   || |||  |||  ||||||    |||
                                               ||
                                               |

    """;

    private static final String[] BUILD_INFO = {
        "Cataphract",       // Kernel Name
        "1.3.0",           // Version
        "14-August-2024",  // Build Date
        "20240814-003624_NION", // Build ID
        "Development"      // Branch/Build Type
    };

    /**
     * Displays the kernel branding and version.
     */
    public void displayBranding(IOStreams ioStreams) {
        ioStreams.println(BRANDING + "\nVersion " + BUILD_INFO[1]);
    }

    public String getKernelName() {
        return BUILD_INFO[0];
    }

    public String getVersion() {
        return BUILD_INFO[1];
    }

    public String getBuildDate() {
        return BUILD_INFO[2];
    }

    public String getBuildId() {
        return BUILD_INFO[3];
    }

    public String getBuildType() {
        return BUILD_INFO[4];
    }
}

/**
 * Interface for screen clearing strategies.
 */
interface ScreenClearer {
    void clearScreen() throws Exception;
}

/**
 * Clears the screen using ANSI escape codes.
 */
class ANSIScreenClearer implements ScreenClearer {
    private final IOStreams ioStreams;

    public ANSIScreenClearer() {
        this.ioStreams = new IOStreams();
    }

    @Override
    public void clearScreen() throws Exception {
        ioStreams.print("\033[H\033[2J");
        ioStreams.println("");
    }
}

/**
 * Clears the screen using OS-specific processes.
 */
class ProcessScreenClearer implements ScreenClearer {
    private final IOStreams ioStreams;

    public ProcessScreenClearer() {
        this.ioStreams = new IOStreams();
    }

    @Override
    public void clearScreen() throws Exception {
        String os = System.getProperty("os.name").toLowerCase();
        ProcessBuilder pb;
        if (os.contains("win")) {
            pb = new ProcessBuilder("cmd", "/c", "cls");
        } else {
            pb = new ProcessBuilder("/bin/bash", "-c", "reset");
        }
        pb.inheritIO().start().waitFor();
        ioStreams.println("");
    }
}

/**
 * Manages debug information output.
 */
class DebugManager {
    private final boolean debugEnabled;

    public DebugManager(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }

    /**
     * Displays debug information if enabled.
     */
    public void displayDebugInfo(IOStreams ioStreams) {
        if (!debugEnabled) return;

        Runtime runtime = Runtime.getRuntime();
        long memoryUsed = runtime.totalMemory() - runtime.freeMemory();

        ioStreams.println("! DEBUG INFORMATION SPEW START !");
        ioStreams.println("\n000000000000000000000000000000");
        ioStreams.println("! DEBUG - MEMORY INFORMATION !");
        ioStreams.println("000000000000000000000000000000");
        ioStreams.println("> Process ID   : " + ManagementFactory.getRuntimeMXBean().getPid());
        ioStreams.println("> Total Memory : " + runtime.totalMemory() + " Bytes");
        ioStreams.println("> Free Memory  : " + runtime.freeMemory() + " Bytes");
        ioStreams.println("> Used Memory  : " + memoryUsed + " Bytes");
        ioStreams.println("000000000000000000000000000000\n");
        ioStreams.println("!  DEBUG INFORMATION SPEW END  !");
        System.gc();
    }
}