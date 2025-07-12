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
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
* A class implementing console output with prefixes, colors, and a "Press RETURN To Continue" functionality
*
* @author DAK404 (https://github.com/DAK404)
* @version 1.2.0 (11-October-2023, Cataphract)
* @since 1.1.0 (Truncheon Katana 1.2.0)
*/
public class IOStreams
{
    /** Instantiate Console to get user inputs. */
    private static Console console = System.console();

    /** Array that holds the text foreground values. */
    private final static String[] _textColorForeground = {"30", "31", "32", "33", "34", "35", "36", "37", "39"};

    /** Array that holds the text background values. */
    private final static String[] _textColorBackground = {"40", "41", "42", "43", "44", "45", "46", "47", "49"};

    /** Precompiled pattern to check if a said file is valid or invalid */
    private static final Pattern INVALID_CHARS_PATTERN = Pattern.compile("[/\\\\|:*?\"<>]");

    /** Precompile the regex to split string to array */
    private static final Pattern SPLIT_PATTERN = Pattern.compile(" (?=([^\"]*\"[^\"]*\")*[^\"]*$)");


    /**
    * Sole constructor. (For invocation by subclass constructors, typically implicit.)
    */
    public IOStreams()
    {
    }

    /**
    * Prints the text specified, prefixed with an Information tag
    *
    * @param message The text specified to be printed onto the console.
    */
    public static void printInfo(String message)
    {
        println(2, 8, "[ INFORMATION ] " + message);
    }

    /**
    * Prints the text specified, prefixed with an Error tag
    *
    * @param message The text specified to be printed onto the console.
    */
    public static void printError(String message)
    {
        println(1, 8, "[    ERROR    ] " + message);
    }

    /**
    * Prints the text specified, prefixed with a Warning tag
    *
    * @param message The text specified to be printed onto the console.
    */
    public static void printWarning(String message)
    {
        println(3, 8, "[   WARNING   ] " + message);
    }

    /**
    * Prints the text specified, prefixed with an Attention tag
    *
    * @param message The text specified to be printed onto the console.
    */
    public static void printAttention(String message)
    {
        println(5, 8, "[  ATTENTION  ] " + message);
    }

    /**
    * Prints the text specified, prefixed with a Debug tag
    *
    * @param message The text specified to be printed onto the console.
    */
    public static void printDebug(String message)
    {
        println(1, 7, "[    DEBUG    ] " + message);
    }

    /**
    * Prints the text specified without newline character at the end. Does not include any formatting.
    *
    * @param message The text specified to be printed onto the console.
    */
    public static void print(String message)
    {
        System.out.print(message);
    }

    /**
    * Prints the text specified. Does not include any formatting.
    *
    * @param message The text specified to be printed onto the console.
    */
    public static void println(String message)
    {
        System.out.println(message);
    }

    /**
    * Text that can have a formatted background and foreground color to make a piece of text distinct from the rest. Prints the line and moves the curson to the next line.
    *
    * @param foregroundIndex Index value for the foreground color
    * @param backgroundIndex Index value for the background color
    * @param message The intended message that needs to be printed on the screen
    */
    public static void print(int foregroundIndex, int backgroundIndex, String message)
    {
        try
        {
            //Print the text with the specified indices correlating with the table specified
            System.out.print((char)27 + "[" + _textColorForeground[foregroundIndex] + ";" + _textColorBackground[backgroundIndex] + "m" + message + (char)27 + "[0m");
        }
        catch(ArrayIndexOutOfBoundsException e)
        {
            //Refuse to print anything else, prevent abuse of this method
            System.out.println(e + "Invalid Syntax.");
        }
        catch(Exception e)
        {
            //Catch any generic errors
            System.out.println(e);
        }
    }

     /**
    * Text that can have a formatted background and foreground color to make a piece of text distinct from the rest.
    *
    * @param foregroundIndex Index value for the foreground color
    * @param backgroundIndex Index value for the background color
    * @param message The intended message that needs to be printed on the screen
    */
    public static void println(int foregroundIndex, int backgroundIndex, String message)
    {
        print(foregroundIndex, backgroundIndex, message + "\n");
    }

    /**
    * Provide a method that will ask the user to press the RETURN key.
    * Useful when there is a long text to be read by the user.
    *
    * @return String The value provided to the input
    */
    public static String confirmReturnToContinue()
    {
        return console.readLine("Press RETURN to Continue.");
    }

    /**
    * [ OVERLOAD ] Provide a method that will ask the user to press the RETURN key, with a prefix text and a suffix text.
    *
    * Useful when someone wants to insert text before or after the default "Press RETURN to Continue..." string
    * You might want to use a unicode escape character \u00A0 to essentially add a non breaking space to multiline Strings.     *
     *
     * @param prefix The prefix to be added before displaying the "Press RETURN to Continue" message
     * @param suffix The suffix to be added after displaying the "Press RETURN to Continue" message
     * @return String The value provided to the input
     */
    public static String confirmReturnToContinue(String prefix, String suffix)
    {
        return console.readLine(prefix + "Press RETURN to Continue" + suffix);
    }

    /**
    * Logic to convert from Nion File Separator format of file paths to an OS dependent file separator format.
    *
    * @param nionPath String that contains the file path in Nion File Separator format
    * @return String The value of the String converted to the OS dependent file separator format
    */
    public static String convertFileSeparator(String nionPath)
    {
        return nionPath.replaceAll("\\|", Matcher.quoteReplacement(File.separator));
    }

    /**
    * Logic to convert from an OS dependent file separator format of file paths to Nion File Separator format.
    *
    * @param filePath String that contains the file path in OS dependent file separator format.
    * @return String The value of the String converted to Nion File Separator format.
    */
    public static String convertToNionSeparator(String filePath)
    {
        return filePath.replaceAll(Matcher.quoteReplacement(File.separator), "|");
    }

    /**
    * Checks the validity of the file name.
    *
    * @return {@code true} if the file name is valid, {@code false} otherwise.
    * @throws Exception Throws any exceptions encountered during runtime.
    */
    public static boolean checkFileValidity(String fileName)
    {
        return !(fileName == null || fileName.isEmpty() || fileName.startsWith(" ") || fileName.length() > 255 || INVALID_CHARS_PATTERN.matcher(fileName).find());
    }

    /**
    * Method to split an input string to individual words by at the occurrence of a blank space.
    *
    * @param command String that will need to be split into an array.
    * @return String[] The string split into an array.
    */
    public static String[] splitStringToArray(String command)
    {
        //Regex to split the string at the occurrence of a blank space
        String[] arr = SPLIT_PATTERN.split(command);

        //Fix to remove the quotes, make the logic to split the input at every space only.
        //Check the EasyGuide Documentation on why this is implemented the way it is.
        for(int i = 0; i < arr.length; i++)
        if(arr[i].startsWith("\"") && arr[i].endsWith("\""))
            arr[i] = arr[i].substring(1, arr[i].length()-1);

        //return the array of words split.
        return arr;
    }
}