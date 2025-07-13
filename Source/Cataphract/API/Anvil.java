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

import Cataphract.API.Astaroth.Calendar;
import Cataphract.API.Astaroth.Time;
import java.util.HashMap;
import java.util.Map;

/**
 * Interprets common shell commands for Cataphract.
 */
public class Anvil {
    private final Map<String, AnvilCommand> commands;

    public Anvil() {
        this.commands = new HashMap<>();
        initializeCommands();
    }

    /**
     * Interprets and executes a command.
     * @param commandArray The command and its arguments.
     * @throws Exception If the command execution fails.
     */
    public void anvilInterpreter(String[] commandArray) throws Exception {
        if (commandArray == null || commandArray.length == 0) {
            Config.io.printError("Invalid command: empty input");
            return;
        }
        AnvilCommand command = commands.get(commandArray[0].toLowerCase());
        if (command != null) {
            command.execute(commandArray);
        } else {
            Config.io.printError(commandArray[0] + " - Command Not Found");
        }
    }

    private void initializeCommands() {
        commands.put("time", new TimeCommand(Config.time));
        commands.put("calendar", new CalendarCommand(Config.calendar));
        commands.put("clear", new ClearCommand());
        commands.put("echo", new EchoCommand());
        commands.put("wait", new WaitCommand());
        commands.put("confirm", new ConfirmCommand());
    }
}

/**
 * Interface for Anvil commands.
 */
interface AnvilCommand {
    void execute(String[] args) throws Exception;
}

/**
 * Displays the current or formatted time.
 */
class TimeCommand implements AnvilCommand {
    private final Time timeProvider;
    

    public TimeCommand(Time timeProvider) {
        this.timeProvider = timeProvider;
    }

    @Override
    public void execute(String[] args) throws Exception {
        if (args.length < 2) {
            Config.io.println(timeProvider.getTime());
        } else {
            Config.io.println(timeProvider.getDateTimeUsingSpecifiedFormat(args[1]));
        }
    }
}

/**
 * Displays a calendar for a given month and year.
 */
class CalendarCommand implements AnvilCommand {
    private final Calendar calendarProvider;
    

    public CalendarCommand(Calendar calendarProvider) {
        this.calendarProvider = calendarProvider;
    }

    @Override
    public void execute(String[] args) throws Exception {
        try {
            int month = 0;
            int year = 0;
            if (args.length >= 2) {
                month = Integer.parseInt(args[1]);
            }
            if (args.length >= 3) {
                year = Integer.parseInt(args[2]);
            }
            calendarProvider.printCalendar(month, year);
        } catch (NumberFormatException e) {
            Config.io.printError("Please provide a numeric input for month and year!");
        }
    }
}

/**
 * Clears the terminal screen.
 */
class ClearCommand implements AnvilCommand {

    @Override
    public void execute(String[] args) throws Exception {
        Build build = new Build();
        if (args.length < 2) {
            build.viewBuildInfo(false); // No debug output
        } else if (args[1].equalsIgnoreCase("force")) {
            build.clearScreen();
        }
    }
}

/**
 * Prints a string to the console.
 */
class EchoCommand implements AnvilCommand {
    
    public EchoCommand() {
    }

    @Override
    public void execute(String[] args) throws Exception {
        if (args.length < 2) {
            Config.io.printError("Invalid Syntax.");
            Config.io.printInfo("Expected Syntax: echo <String> OR echo \"<String With Spaces>\"");
            return;
        }
        Config.io.println(args[1]);
    }
}

/**
 * Displays help information from a file.
 */
class HelpCommand implements AnvilCommand {
    public HelpCommand() {
    }

    @Override
    public void execute(String[] args) throws Exception {
        String helpFile = args.length < 2 ? "API|Anvil.help" : args[1];
        Config.fileRead.readHelpFile(helpFile);
    }
}

/**
 * Pauses execution for a specified duration.
 */
class WaitCommand implements AnvilCommand {
    

    public WaitCommand() {
    }

    @Override
    public void execute(String[] args) throws Exception {
        if (args.length < 2) {
            Config.io.printError("Invalid Syntax.");
            Config.io.printInfo("Expected Syntax: wait <milliseconds> (Integer > 0)");
            return;
        }
        try {
            int milliseconds = Integer.parseInt(args[1]);
            if (milliseconds < 1) {
                Config.io.printError("Invalid Syntax.");
                Config.io.printInfo("Expected Syntax: wait <milliseconds> (Integer > 0)");
                return;
            }
            Thread.sleep(milliseconds);
        } catch (NumberFormatException e) {
            Config.io.printError("Invalid Argument! Expected Argument: milliseconds (Integer)");
            Config.io.printInfo("Expected Syntax: wait <milliseconds> (Integer)");
        }
    }
}

/**
 * Prompts the user to press RETURN to continue.
 */
class ConfirmCommand implements AnvilCommand {

    public ConfirmCommand() {
    }

    @Override
    public void execute(String[] args) throws Exception {
        Config.io.confirmReturnToContinue();
    }
}