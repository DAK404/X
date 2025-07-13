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

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manages console I/O, file path conversion, file validation, and string parsing for Cataphract.
 */
public class IOStreams {
    private final ConsoleOutputHandler outputHandler;
    private final ConsoleInputHandler inputHandler;
    private final FilePathConverter pathConverter;
    private final FileValidator fileValidator;
    private final StringParser stringParser;

    public IOStreams() {
        this.outputHandler = new ANSIConsoleOutputHandler();
        this.inputHandler = new DefaultConsoleInputHandler();
        this.pathConverter = new NionFilePathConverter();
        this.fileValidator = new DefaultFileValidator();
        this.stringParser = new DefaultStringParser();
    }

    /**
     * Prints an information message.
     */
    public void printInfo(String message) {
        outputHandler.printInfo(message);
    }

    /**
     * Prints an error message.
     */
    public void printError(String message) {
        outputHandler.printError(message);
    }

    /**
     * Prints a warning message.
     */
    public void printWarning(String message) {
        outputHandler.printWarning(message);
    }

    /**
     * Prints an attention message.
     */
    public void printAttention(String message) {
        outputHandler.printAttention(message);
    }

    /**
     * Prints a debug message.
     */
    public void printDebug(String message) {
        outputHandler.printDebug(message);
    }

    /**
     * Prints a message without a newline.
     */
    public void print(String message) {
        outputHandler.print(message);
    }

    /**
     * Prints a message with a newline.
     */
    public void println(String message) {
        outputHandler.println(message);
    }

    /**
     * Prompts the user to press RETURN to continue.
     */
    public String confirmReturnToContinue() {
        return inputHandler.confirmReturnToContinue();
    }

    /**
     * Prompts the user to press RETURN with custom prefix and suffix.
     */
    public String confirmReturnToContinue(String prefix, String suffix) {
        return inputHandler.confirmReturnToContinue(prefix, suffix);
    }

    /**
     * Converts a Nion file path to OS-specific format.
     */
    public String convertFileSeparator(String nionPath) {
        return pathConverter.convertToOSPath(nionPath);
    }

    /**
     * Converts an OS-specific file path to Nion format.
     */
    public String convertToNionSeparator(String filePath) {
        return pathConverter.convertToNionPath(filePath);
    }

    /**
     * Checks if a file name is valid.
     */
    public boolean checkFileValidity(String fileName) {
        return fileValidator.isValidFileName(fileName);
    }

    /**
     * Splits a command string into an array.
     */
    public String[] splitStringToArray(String command) {
        return stringParser.splitCommand(command);
    }
}

/**
 * Interface for console output handling.
 */
interface ConsoleOutputHandler {
    void printInfo(String message);
    void printError(String message);
    void printWarning(String message);
    void printAttention(String message);
    void printDebug(String message);
    void print(String message);
    void println(String message);
}

/**
 * Console output handler using ANSI color codes.
 */
class ANSIConsoleOutputHandler implements ConsoleOutputHandler {
    private static final String[] TEXT_COLOR_FOREGROUND = {"30", "31", "32", "33", "34", "35", "36", "37", "39"};
    private static final String[] TEXT_COLOR_BACKGROUND = {"40", "41", "42", "43", "44", "45", "46", "47", "49"};

    @Override
    public void printInfo(String message) {
        println(2, 8, "[ INFORMATION ] " + message);
    }

    @Override
    public void printError(String message) {
        println(1, 8, "[    ERROR    ] " + message);
    }

    @Override
    public void printWarning(String message) {
        println(3, 8, "[   WARNING   ] " + message);
    }

    @Override
    public void printAttention(String message) {
        println(5, 8, "[  ATTENTION  ] " + message);
    }

    @Override
    public void printDebug(String message) {
        println(1, 7, "[    DEBUG    ] " + message);
    }

    @Override
    public void print(String message) {
        System.out.print(message);
    }

    @Override
    public void println(String message) {
        System.out.println(message);
    }

    private void println(int foregroundIndex, int backgroundIndex, String message) {
        try {
            System.out.println((char)27 + "[" + TEXT_COLOR_FOREGROUND[foregroundIndex] + ";" +
                               TEXT_COLOR_BACKGROUND[backgroundIndex] + "m" + message + (char)27 + "[0m");
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Invalid color index: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error printing message: " + e.getMessage());
        }
    }
}

/**
 * Interface for console input handling.
 */
interface ConsoleInputHandler {
    String confirmReturnToContinue();
    String confirmReturnToContinue(String prefix, String suffix);
}

/**
 * Default console input handler.
 */
class DefaultConsoleInputHandler implements ConsoleInputHandler {

    @Override
    public String confirmReturnToContinue() {
        return Config.console.readLine("Press RETURN to Continue.");
    }

    @Override
    public String confirmReturnToContinue(String prefix, String suffix) {
        return Config.console.readLine(prefix + "Press RETURN to Continue" + suffix);
    }
}

/**
 * Interface for file path conversion.
 */
interface FilePathConverter {
    String convertToOSPath(String nionPath);
    String convertToNionPath(String filePath);
}



/**
 * Optimized file path converter with cached patterns.
 */
class NionFilePathConverter implements FilePathConverter {
    private static final Pattern NION_TO_OS = Pattern.compile("\\|");
    private static final Pattern OS_TO_NION = Pattern.compile(Pattern.quote(File.separator));

    @Override
    public String convertToOSPath(String nionPath) {
        return nionPath == null ? "" : NION_TO_OS.matcher(nionPath).replaceAll(Matcher.quoteReplacement(File.separator));
    }

    @Override
    public String convertToNionPath(String filePath) {
        return filePath == null ? "" : OS_TO_NION.matcher(filePath).replaceAll("|");
    }
}

/**
 * Interface for file name validation.
 */
interface FileValidator {
    boolean isValidFileName(String fileName);
}

/**
 * Default file name validator.
 */
class DefaultFileValidator implements FileValidator {
    private static final Pattern INVALID_CHARS_PATTERN = Pattern.compile("[/\\\\|:*?\"<>]");

    @Override
    public boolean isValidFileName(String fileName) {
        return !(fileName == null || fileName.isEmpty() || fileName.startsWith(" ") ||
                 fileName.length() > 255 || INVALID_CHARS_PATTERN.matcher(fileName).find());
    }
}

/**
 * Interface for string parsing.
 */
interface StringParser {
    String[] splitCommand(String command);
}

/**
 * Default command string parser.
 */
class DefaultStringParser implements StringParser {
    private static final Pattern SPLIT_PATTERN = Pattern.compile(" (?=([^\"]*\"[^\"]*\")*[^\"]*$)");

    @Override
    public String[] splitCommand(String command) {
        String[] arr = SPLIT_PATTERN.split(command);
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].startsWith("\"") && arr[i].endsWith("\"")) {
                arr[i] = arr[i].substring(1, arr[i].length() - 1);
            }
        }
        return arr;
    }
}