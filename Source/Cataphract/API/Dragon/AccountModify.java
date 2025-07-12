package Cataphract.API.Dragon;

import java.io.Console;
import java.io.File;

import Cataphract.API.Build;
import Cataphract.API.IOStreams;
import Cataphract.API.Minotaur.Cryptography;
import Cataphract.API.Minotaur.PolicyCheck;

/**
 * A class to modify user accounts on the system. Can be restricted by policy "account_modify".
 *
 * @author DAK404 (https://github.com/DAK404)
 * @version 4.0.0 (12-July-2025, Cataphract)
 * @since 0.0.1 (Zen Quantum 0.0.1)
 */
public final class AccountModify implements AccountManager {
    private final Console console = System.console();
    private final String currentUsername;
    private final String currentAccountName;
    private final boolean isCurrentUserAdmin;
    private String targetUser;

    /**
     * Constructor to initialize the current user's details.
     *
     * @param user The currently logged-in username.
     * @throws Exception If an error occurs during initialization.
     */
    public AccountModify(String user) throws Exception {
        this.currentUsername = user == null || user.isEmpty() ? "DEFAULT" : user;
        this.currentAccountName = new Login(user).getNameLogic();
        this.isCurrentUserAdmin = new Login(user).checkPrivilegeLogic();
        this.targetUser = currentUsername;
    }

    @Override
    public void execute() throws Exception {
        if (!new PolicyCheck().retrievePolicyValue("account_modify").equals("on") && !isCurrentUserAdmin) {
            IOStreams.printError("Policy Management System - Permission Denied.");
            return;
        }

        Build.viewBuildInfo();
        if (!authenticate(String.valueOf(console.readPassword("Password: ")), String.valueOf(console.readPassword("Security Key: ")))) {
            IOStreams.printError("Incorrect Credentials! Aborting...");
            return;
        }

        accountManagementMenu();
    }

    @Override
    public boolean authenticate(String password, String securityKey) throws Exception {
        Build.viewBuildInfo();
        IOStreams.printAttention("Please authenticate to continue.");
        IOStreams.println("Username: " + currentAccountName);
        String hashedPassword = Cryptography.stringToSHA3_256(password);
        String hashedSecurityKey = securityKey.isEmpty() ? "" : Cryptography.stringToSHA3_256(securityKey);
        return new Login(currentUsername).authenticationLogic(hashedPassword, hashedSecurityKey);
    }

    /**
     * Displays and manages the account modification menu.
     *
     * @throws Exception If an error occurs during menu processing.
     */
    private void accountManagementMenu() throws Exception {
        String input;
        do {
            displayMenu();
            input = console.readLine(currentAccountName + "} ");
            String[] commandArray = IOStreams.splitStringToArray(input);

            if (commandArray.length == 0 || commandArray[0].isEmpty()) {
                continue;
            }

            switch (commandArray[0].toLowerCase()) {
                case "exit":
                    break;
                case "name":
                    changeCredential("Name", CredentialValidator.NAME_POLICY, CredentialValidator::validateName, false);
                    break;
                case "password":
                    changeCredential("Password", CredentialValidator.PASSWORD_POLICY, CredentialValidator::validatePassword, true);
                    break;
                case "key":
                    changeCredential("Security Key", CredentialValidator.SECURITY_KEY_POLICY, CredentialValidator::validateSecurityKey, true);
                    break;
                case "pin":
                    changeCredential("PIN", CredentialValidator.PIN_POLICY, CredentialValidator::validatePin, true);
                    break;
                case "promote":
                case "demote":
                    if (commandArray.length < 2) {
                        IOStreams.printError("Invalid Syntax. Use: \npromote <target_username>\n   OR\ndemote <target_username>");
                    } else {
                        accountPromoteDemoteLogic(commandArray[0], commandArray[1]);
                    }
                    break;
                case "view":
                    if (commandArray.length < 2) {
                        IOStreams.printError("Invalid Syntax. Use: \nview <target_username>");
                    } else {
                        viewUserInformation(Cryptography.stringToSHA3_256(commandArray[1]));
                    }
                    break;
                case "list":
                    new Login(currentUsername).listAllUserAccounts();
                    break;
                case "clear":
                    displayMenu();
                    break;
                default:
                    IOStreams.printError("Invalid account modification option. Please specify a valid option.");
                    break;
            }
        } while (!input.equalsIgnoreCase("exit"));
    }

    /**
     * Displays the account management menu.
     */
    private void displayMenu() {
        Build.viewBuildInfo();
        IOStreams.println("-------------------------------------------------");
        IOStreams.println("| User Management Console: Account Modification |");
        IOStreams.println("-------------------------------------------------\n");
        IOStreams.println("How do you wish to manage or modify your account?\n");
        IOStreams.println("[1] Change Account Name");
        IOStreams.println("[2] Change Account Password");
        IOStreams.println("[3] Change Account Security Key");
        IOStreams.println("[4] Change Session Unlock PIN\n");
        IOStreams.println("[ NAME | PASSWORD | KEY | PIN | HELP | EXIT ]");
        if (isCurrentUserAdmin) {
            IOStreams.println(1, 8, "\n       [ DANGER ZONE ]       ");
            IOStreams.println(1, 8, "--- ADMINISTRATOR TOOLKIT ---");
            IOStreams.println(1, 8, "[!] Promote Account to Administrator");
            IOStreams.println(1, 8, "[!] Demote Account to Standard User");
            IOStreams.println(1, 8, "[!] View Account Details of a User");
            IOStreams.println(1, 8, "[!] List All User Accounts\n");
            IOStreams.println(1, 8, "[ PROMOTE | DEMOTE | VIEW | LIST ]");
        }
        IOStreams.println("");
    }

    /**
     * Changes a credential for the target user.
     *
     * @param field The database field to update (e.g., Name, Password).
     * @param policy The policy string to display.
     * @param validator The validation function.
     * @param isPassword Whether the input is a password.
     * @throws Exception If an error occurs during validation or update.
     */
    private void changeCredential(String field, String policy, java.util.function.Predicate<String> validator, boolean isPassword) throws Exception {
        String value = CredentialValidator.validateCredential(field, policy, validator, console, isPassword);
        if (value != null) {
            String hashedValue = isPassword && !value.isEmpty() ? Cryptography.stringToSHA3_256(value) : value;
            boolean success = DatabaseManager.executeUpdate(
                "UPDATE MUD SET " + field + " = ? WHERE Username = ?",
                hashedValue, targetUser
            );
            IOStreams.printInfo(success ? "Account Modification Successful!" : "Account Modification Failed.");
        }
    }

    /**
     * Handles account promotion or demotion.
     *
     * @param action The action (promote or demote).
     * @param targetUsername The target username.
     * @throws Exception If an error occurs.
     */
    private void accountPromoteDemoteLogic(String action, String targetUsername) throws Exception {
        if (targetUsername.equalsIgnoreCase("Administrator")) {
            IOStreams.printError("Cannot promote or demote the user Administrator.");
            return;
        }
        if (!isCurrentUserAdmin) {
            IOStreams.printError("Invalid Privileges! Cannot Modify User Privileges.");
            return;
        }

        targetUser = Cryptography.stringToSHA3_256(targetUsername);
        if (!new Login(targetUser).checkUserExistence()) {
            IOStreams.printError("Specified User does not exist!");
            return;
        }

        IOStreams.printAttention("YOU ARE ABOUT TO " + action.toUpperCase() + " \"" + new Login(targetUser).getNameLogic() + "\". ARE YOU SURE? [ Y | N ]");
        if (console.readLine("Change Privileges?> ").equalsIgnoreCase("y")) {
            boolean success = DatabaseManager.executeUpdate(
                "UPDATE MUD SET Privileges = ? WHERE Username = ?",
                action.equalsIgnoreCase("promote") ? "Yes" : "No", targetUser
            );
            IOStreams.printInfo(success ? action.toUpperCase() + "D " + new Login(targetUser).getNameLogic() + " successfully!" : "Account Modification Failed.");
        }
    }

    /**
     * Views information for a target user.
     *
     * @param targetUsername The target username (hashed).
     * @throws Exception If an error occurs.
     */
    private void viewUserInformation(String targetUsername) throws Exception {
        if (!isCurrentUserAdmin) {
            IOStreams.printError("Invalid Privileges! Cannot View User Details.");
            return;
        }
        if (!new Login(targetUsername).checkUserExistence()) {
            IOStreams.printError("User Does Not Exist! Please Enter A Valid Username.");
            return;
        }

        String userHomePath = Config.USER_HOME + targetUsername;
        IOStreams.println("\n--- User Information ---");
        IOStreams.println("Account Name        : " + new Login(targetUsername).getNameLogic());
        IOStreams.println("Account Privileges  : " + (new Login(targetUsername).checkPrivilegeLogic() ? "Administrator" : "Standard"));
        IOStreams.println("User Home Directory : " + (new File(userHomePath).exists() ? userHomePath : "Home Directory Does Not Exist!") + "\n");
    }
}