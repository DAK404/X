/*
*                                                      |
*                                                     ||
*  |||||| ||||||||| |||||||| ||||||||| |||||||  |||  ||| ||||||| |||||||||  |||||| ||||||||
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

package Cataphract.API.Dragon;

import java.io.Console;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import Cataphract.API.IOStreams;

/**
* A class to handle the user login and authentication.
*
* @author DAK404 (https://github.com/DAK404)
* @version 3.13.1 (20-February-2024, Cataphract)
* @since 0.0.1 (Zen Quantum 0.0.1)
*/
public class Login
{
    /** Stores the Database path */
    private String databasePath = "jdbc:sqlite:" + IOStreams.convertFileSeparator(".|System|Cataphract|Private|Mud.dbx");

    /** Stores the username */
    private String _username;

    /** Instantiate Console class to accept console inputs */
    Console console = System.console();

    /**
    * Constructor for Login class.
    *
    * @param username The username to be used for login.
    * @throws Exception Throws any exceptions encountered during runtime.
    */
    public Login(String username) throws Exception
    {
        // If username is empty or null, assign a default user
        _username = (username == null || username.equals("")) ? "DEFAULT USER" : username;
    }

    /**
    * Method to authenticate user login.
    *
    * @param psw The password for authentication.
    * @param key The security key for authentication.
    * @return {@code true} if authentication is successful, {@code false} otherwise.
    * @throws Exception Throws any exceptions encountered during runtime.
    */
    public final boolean authenticationLogic(String psw, String key) throws Exception
    {
        // Check if password and security key match the database entry
        return retrieveDatabaseEntry("SELECT Password FROM MUD WHERE Username = ?", "Password").equals(psw) &&
        retrieveDatabaseEntry("SELECT SecurityKey FROM MUD WHERE Username = ?", "SecurityKey").equals(key);
    }

    /**
    * Method to check user privileges.
    *
    * @return {@code true} if the user has privileges, {@code false} otherwise.
    * @throws Exception Throws any exceptions encountered during runtime.
    */
    public final boolean checkPrivilegeLogic() throws Exception
    {
        // Check user privileges from database
        return retrieveDatabaseEntry("SELECT Privileges FROM MUD WHERE Username = ?", "Privileges").equals("Yes");
    }

    /**
    * Method to retrieve user name.
    *
    * @return The name of the user.
    * @throws Exception Throws any exceptions encountered during runtime.
    */
    public final String getNameLogic() throws Exception
    {
        // Retrieve user name from database
        return retrieveDatabaseEntry("SELECT Name FROM MUD WHERE Username = ?", "Name");
    }

    /**
    * Method to retrieve user PIN.
    *
    * @return The PIN of the user.
    * @throws Exception Throws any exceptions encountered during runtime.
    */
    public final String getPINLogic() throws Exception
    {
        // Retrieve user PIN from database
        return retrieveDatabaseEntry("SELECT PIN FROM MUD WHERE Username = ?", "PIN");
    }

    /**
    * Method to check if the user account exists.
    *
    * @return {@code true} if the user account exists, {@code false} otherwise.
    * @throws Exception Throws any exceptions encountered during runtime.
    */
    public final boolean checkUserExistence() throws Exception
    {
        // Check for existing user account
        return checkForExistingAccount();
    }

    /**
     * Enumerates all the user accounts in the database
     * 
     * @throws Exception Throws any exceptions encountered during runtime.
     */
    protected void listAllUserAccounts()throws Exception
    {
        // Check if the current user is an Administrator
        if(checkPrivilegeLogic())
        {
            // Define the format for displaying the usernames
            String format = "%1$-64s| %2$-32s| %3$-5s\n";
            String c = "-";
            
            // Print a newline for better formatting
            System.out.println();
            
            // Format and print the header for the user directory listing
            String disp = String.format(format, "Username", "Account Name", "Privileges");
            System.out.println(disp + c.repeat(disp.length()) + "\n");

            // List the directories in the Users directory
            File[] fileList = new File(IOStreams.convertFileSeparator(".|Users|Cataphract|")).listFiles();

            // Iterate through the list of files
            for(File users: fileList)
            {
                String usernames = users.getName();
                // Format and print the name, username and privileges
                System.out.format(String.format(format, usernames, new Login(usernames).getNameLogic(), new Login(usernames).checkPrivilegeLogic()));
            }

            System.out.println();
        }
        else
            IOStreams.printError("Insufficient Privileges.");
    }

    /**
    * Method to retrieve data from the database.
    *
    * @param sqlCommand The SQL command to execute.
    * @param parameter The parameter to retrieve.
    * @return The retrieved data.
    * @throws Exception Throws any exceptions encountered during runtime.
    */
    private final String retrieveDatabaseEntry(String sqlCommand, String parameter) throws Exception
    {
        // Default result value
        String result = "DEFAULT_STRING";

        // JDBC driver registration
        Class.forName("org.sqlite.JDBC");

        // Establish database connection
        Connection dbConnection = DriverManager.getConnection(databasePath);
        // Prepared statement for SQL command
        PreparedStatement preparedStatement = dbConnection.prepareStatement(sqlCommand);
        ResultSet resultSet = null;

        try
        {
            // Set username parameter for the prepared statement
            preparedStatement.setString(1, _username);
            // Execute query
            resultSet = preparedStatement.executeQuery();
            // Retrieve result
            result = resultSet.getString(parameter);
        }
        catch(Exception e)
        {
            // Print stack trace for any exceptions
            e.printStackTrace();
            result = "ERROR";
        }
        finally
        {
            // Close resources
            preparedStatement.close();
            dbConnection.close();
            resultSet.close();
        }

        // If result is null, assign "Error" string
        if(result == null)
        result = "Error";

        // Trigger garbage collection
        System.gc();
        return result;
    }

    /**
    * Method to check for an existing user account in the database.
    *
    * @return {@code true} if the user account exists, {@code false} otherwise.
    * @throws Exception Throws any exceptions encountered during runtime.
    */
    private boolean checkForExistingAccount() throws Exception
    {
        // Flag to indicate existence of user account
        boolean userExists = false;

        // JDBC driver registration
        Class.forName("org.sqlite.JDBC");
        
        // Establish database connection
        Connection dbConnection = DriverManager.getConnection(databasePath);

        // SQL query to check for existing user account
        String query = "SELECT (count(*) > 0) FROM MUD WHERE Username LIKE ?";
        PreparedStatement preparedStatement = dbConnection.prepareStatement(query);
        preparedStatement.setString(1, _username);
        ResultSet resultSet = preparedStatement.executeQuery();

        try
        {
            // Check if result set contains data
            if (resultSet.next())
            userExists = resultSet.getBoolean(1);
        }
        catch(Exception e)
        {
            // Print stack trace for any exceptions
            e.printStackTrace();
        }
        finally
        {
            // Close resources
            preparedStatement.close();
            dbConnection.close();
            resultSet.close();
        }

        // Trigger garbage collection
        System.gc();
        return userExists;
    }
}
