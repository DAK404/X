package Truncheon.API.Dragon;

//Import the required Java IO classes
import java.io.Console;

//Import the required Java SQL classes
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import Truncheon.API.BuildInfo;
import Truncheon.API.IOStreams;

public class AccountModify
{
    private String _currentUsername = "";
    private String _currentAccountName = "";
    private boolean _currentAccountAdmin = false;

    private String _newPassword = "";
    private String _newSecKey = "";
    private String _newPIN = "";

    private String _accountNamePolicy;
    private String _accountPasswordPolicy;
    private String _accountKeyPolicy;
    private String _accountPINPolicy;

    String parameter = "";
    String value = "";
    String targetUser = "";

    private Console console = System.console();

    public AccountModify(String user)throws Exception
    {
        _currentUsername = user;
        _currentAccountName = new Truncheon.API.Dragon.LoginAuth(user).getNameLogic();
        _currentAccountAdmin = new LoginAuth(user).checkPrivilegeLogic();
    }

    public final void accountModifyLogic()throws Exception
    {
        System.gc();

        System.out.println(_currentUsername);
        System.out.println(_currentAccountName);
        System.out.println(_currentAccountAdmin);

        console.readLine();

        if(!login())
        IOStreams.printError("Incorrect Credentials! Aborting...");
        else
        accountManagementMenu();
    }

    private boolean login()throws Exception
    {
        BuildInfo.viewBuildInfo();
        IOStreams.printAttention("Please authenticate to continue.");
        IOStreams.println("Username: " + _currentAccountName);
        String password = new Truncheon.API.Minotaur.Cryptography().stringToSHA3_256(String.valueOf(console.readPassword("Password: ")));
        String key = new Truncheon.API.Minotaur.Cryptography().stringToSHA3_256(String.valueOf(console.readPassword("Security Key: ")));

        return new Truncheon.API.Dragon.LoginAuth(_currentUsername).authenticationLogic(password, key);
    }

    private void accountManagementMenu()throws Exception
    {
        BuildInfo.viewBuildInfo();

        accountManagementMenuDisplay();

        System.out.println("Hello World! this is still under construct. Please try again later.");
        IOStreams.confirmReturnToContinue();

        String tempInput;

        accountManagementMenuDisplay();

        do
        {

            tempInput = console.readLine("\n" + _currentAccountName + "} ");

            targetUser = _currentUsername;

            String[] usermgmtModifyCommandArray = Truncheon.API.Anvil.splitStringToArray(tempInput);

            switch(usermgmtModifyCommandArray[0].toLowerCase())
            {
                case "name":
                parameter = "Name";
                break;

                case "password":
                parameter = "Password";
                break;

                case "key":
                parameter = "SecurityKey";
                break;

                case "pin":
                parameter = "PIN";
                break;

                case "promote":
                case "demote":
                break;

                case "clear":
                accountManagementMenuDisplay();
                break;

                default:
                break;
            }
        }
        while(!tempInput.equalsIgnoreCase("exit"));
    }

    private void accountManagementMenuDisplay()
    {
        Truncheon.API.BuildInfo.clearScreen();
        IOStreams.println("-------------------------------------------------");
        IOStreams.println("| User Management Console: Account Modification |");
        IOStreams.println("-------------------------------------------------\n");

        IOStreams.println("How do you wish to manage or modify your account?\n");

        IOStreams.println("[1] Change Account Name");
        IOStreams.println("[2] Change Account Password");
        IOStreams.println("[3] Change Account Security Key");
        IOStreams.println("[4] Change Session Unlock PIN\n");

        IOStreams.println("[ NAME | PASSWORD | KEY | PIN | HELP | EXIT ]");
        if(_currentAccountAdmin)
        {
            IOStreams.println(1, 8, "\n       [ DANGER ZONE ]       ");
            IOStreams.println(1, 8, "--- ADMINISTRATOR TOOLKIT ---");
            IOStreams.println(1, 8,"[!] Promote Account to Administrator");
            IOStreams.println(1, 8,"[!] Demote Account to Standard User\n");

            IOStreams.println(1, 8,"[ PROMOTE | DEMOTE ]");
        }
        IOStreams.println("");
    }

    //action is either promote or demote, same as commandArray[0]
    private void accountPromoteDemoteLogic(String action)
    {
    }

    private void changeAccountName()
    {

    }

    private void changeAccountPassword()
    {

    }

    private void changeAccountSecurityKey()
    {

    }

    private void changeAccountPIN()
    {

    }

    private void commitChangesToDatabase(String parameter, String value, String targetUser)
    {
        try
        {
            String databasePath = "jdbc:sqlite:./System/Truncheon/Private/Mud.dbx";
            String sqlCommand = "UPDATE MUD SET " + parameter + " = ? WHERE Username = ?";

            Class.forName("org.sqlite.JDBC");
            Connection dbConnection = DriverManager.getConnection(databasePath);
            PreparedStatement preparedStatement = dbConnection.prepareStatement(sqlCommand);

            preparedStatement.setString(1, value);
            preparedStatement.setString(2, targetUser);

            preparedStatement.executeUpdate();

            preparedStatement.close();
            dbConnection.close();

            IOStreams.printInfo("Account Modification Successful!");
        }
        catch(Exception e)
        {
            IOStreams.printInfo("Account Modification Failed.");
        }
        System.gc();
    }
}
