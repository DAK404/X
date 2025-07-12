package Cataphract.API.Dragon;

import Cataphract.API.IOStreams;

/**
 * Configuration constants for the Cataphract shell.
 *
 * @author DAK404 (https://github.com/DAK404)
 * @version 4.0.0 (12-July-2025, Cataphract)
 */
public final class Config 
{
    public static final String DB_PATH = IOStreams.convertFileSeparator(".|System|Cataphract|Private|Mud.dbx");
    public static final String USER_HOME = IOStreams.convertFileSeparator(".|Users|Cataphract|");

    /**
     * Private constructor (To prevent invocation by subclass constructors)
     */
    private Config() {}
}