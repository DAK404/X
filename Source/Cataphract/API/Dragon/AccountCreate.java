package Cataphract.API.Dragon;

import java.io.Console;

import Cataphract.API.Build;
import Cataphract.API.IOStreams;
import Cataphract.API.Minotaur.Cryptography;
import Cataphract.API.Minotaur.PolicyCheck;

/**
 * A class to create new user accounts on the system. Can be restricted by policy "account_create".
 *
 * @author DAK404 (https://github.com/DAK404)
 * @version 4.0.0 (12-July-2025, Cataphract)
 * @since 0.0.1 (Zen Quantum 0.0.1)
 */
public final class AccountCreate implements AccountManager 
{
    private final Console console = System.console();
    private final String currentUsername;
    private final boolean isCurrentUserAdmin;
    private final UserAccount account = new UserAccount();

    /**
     * Constructor for initial setup (e.g., creating default admin account).
     */
    public AccountCreate() 
    {
        this.currentUsername = "DEFAULT";
        this.isCurrentUserAdmin = false;
    }

    /**
     * Constructor for creating a new user account.
     *
     * @param username The currently logged-in username.
     * @throws Exception If an error occurs during initialization.
     */
    public AccountCreate(String username) throws Exception 
    {
        this.currentUsername = username == null || username.isEmpty() ? "DEFAULT" : username;
        this.isCurrentUserAdmin = new Login(username).checkPrivilegeLogic();
    }

    @Override
    public void execute() throws Exception 
    {
        if (!new PolicyCheck().retrievePolicyValue("account_create").equals("on") && !isCurrentUserAdmin) {
            IOStreams.printError("Policy Management System - Permission Denied.");
            return;
        }

        Build.viewBuildInfo();
        IOStreams.println("Please authenticate to continue.");
        IOStreams.println("Username: " + new Login(currentUsername).getNameLogic());
        String password = String.valueOf(console.readPassword("Password: "));
        String securityKey = String.valueOf(console.readPassword("SecurityKey: "));

        if (!authenticate(password, securityKey)) 
        {
            IOStreams.println("Failed to authenticate user. Exiting...");
            return;
        }

        if (isCurrentUserAdmin) {
            promptForAdminPrivileges();
        }

        setCredentials();
        addAccountToDatabase();
        IOStreams.confirmReturnToContinue();
    }

    @Override
    public boolean authenticate(String password, String securityKey) throws Exception 
    {
        Build.viewBuildInfo();
        IOStreams.println("Username: " + new Login(currentUsername).getNameLogic());
        String hashedPassword = Cryptography.stringToSHA3_256(password);
        String hashedSecurityKey = securityKey.isEmpty() ? "" : Cryptography.stringToSHA3_256(securityKey);
        return new Login(currentUsername).authenticationLogic(hashedPassword, hashedSecurityKey);
    }

    /**
     * Prompts for granting admin privileges to the new account.
     */
    private void promptForAdminPrivileges() 
    {
        IOStreams.printAttention("The currently logged in user is an administrator.\nYou have the privileges to create other administrator accounts or standard user accounts.\n");
        IOStreams.printWarning("Administrative rights have additional privileges over standard users! Beware on who the administrative privileges are granted to!\n");
        IOStreams.println("Would you like to grant administrative privileges to the new user account? [ Y | N ]");
        account.setAdmin(console.readLine("Grant Administrator Privileges?> ").equalsIgnoreCase("Y"));
    }

    /**
     * Sets all credentials for the new account.
     *
     * @throws Exception If an error occurs during input validation.
     */
    private void setCredentials() throws Exception 
    {
        account.setName(setCredential("Account Name", CredentialValidator.NAME_POLICY, CredentialValidator::validateName));
        account.setUsername(Cryptography.stringToSHA3_256(setCredential("Account Username", CredentialValidator.USERNAME_POLICY, CredentialValidator::validateUsername)));
        account.setPassword(Cryptography.stringToSHA3_256(setCredential("Account Password", CredentialValidator.PASSWORD_POLICY, CredentialValidator::validatePassword, true)));
        String securityKey = setCredential("Account Security Key", CredentialValidator.SECURITY_KEY_POLICY, CredentialValidator::validateSecurityKey, true);
        account.setSecurityKey(securityKey.isEmpty() ? "" : Cryptography.stringToSHA3_256(securityKey));
        account.setPin(Cryptography.stringToSHA3_256(setCredential("Account PIN", CredentialValidator.PIN_POLICY, CredentialValidator::validatePin, true)));
    }

    /**
     * Generic method to set a credential with validation.
     *
     * @param prompt The prompt to display.
     * @param policy The policy string.
     * @param validator The validation function.
     * @param isPassword Whether the input is a password.
     * @return The validated input.
     */
    private String setCredential(String prompt, String policy, java.util.function.Predicate<String> validator, boolean isPassword) throws Exception {
        while (true) 
        {
            credentialDashboard();
            String input = CredentialValidator.validateCredential(prompt, policy, validator, console, isPassword);
            if (input != null) 
            {
                return input;
            }
        }
    }

    private String setCredential(String prompt, String policy, java.util.function.Predicate<String> validator) throws Exception {
        return setCredential(prompt, policy, validator, false);
    }

    /**
     * Displays the credential dashboard.
     */
    private void credentialDashboard() 
    {
        Build.viewBuildInfo();
        IOStreams.println("-------------------------------------------------");
        IOStreams.println("| User Management Console: Account Creation     |");
        IOStreams.println("-------------------------------------------------\n");
        IOStreams.println("Account Name  : " + (account.getName().isEmpty() ? "NOT SET" : account.getName()));
        IOStreams.println("Username      : " + (account.getUsername().isEmpty() ? "NOT SET" : account.getUsername()));
        IOStreams.println("Password      : " + (account.getPassword().isEmpty() ? "NOT SET" : "********"));
        IOStreams.println("SecurityKey   : " + (account.getSecurityKey().isEmpty() ? "NOT SET" : "********"));
        IOStreams.println("PIN           : " + (account.getPin().isEmpty() ? "NOT SET" : "****"));
        IOStreams.println("Account Privileges: " + (account.isAdmin() ? "Administrator" : "Standard") + "\n");
        IOStreams.println("========================================");
    }

    /**
     * Adds the account to the database.
     */
    private void addAccountToDatabase() 
    {
        boolean success = DatabaseManager.executeUpdate(
            "INSERT INTO MUD(Username, Name, Password, SecurityKey, PIN, Privileges) VALUES(?,?,?,?,?,?)",
            account.getUsername(), account.getName(), account.getPassword(), account.getSecurityKey(), account.getPin(), account.isAdmin() ? "Yes" : "No"
        );
        if (success) {
            FileManager.createUserDirectory(account.getUsername());
            IOStreams.printInfo("Account Creation Successful!");
        } else {
            IOStreams.printError("Account Creation Failed.");
        }
    }

    /**
     * Creates a default Administrator account during setup.
     *
     * @throws Exception If an error occurs during account creation.
     */
    public void createDefaultAdministratorAccount() throws Exception 
    {
        if (new Login("Administrator").checkUserExistence()) 
        {
            return;
        }

        account.setAdmin(true);
        account.setName("Administrator");
        account.setUsername(Cryptography.stringToSHA3_256("Administrator"));

        IOStreams.println("Administrator : " + account.isAdmin());
        IOStreams.println("Account Name  : " + account.getName());
        IOStreams.println("Username      : " + account.getUsername());

        account.setPassword(Cryptography.stringToSHA3_256(setCredential("Account Password", CredentialValidator.PASSWORD_POLICY, CredentialValidator::validatePassword, true)));
        String securityKey = setCredential("Account Security Key", CredentialValidator.SECURITY_KEY_POLICY, CredentialValidator::validateSecurityKey, true);
        account.setSecurityKey(securityKey.isEmpty() ? "" : Cryptography.stringToSHA3_256(securityKey));
        account.setPin(Cryptography.stringToSHA3_256(setCredential("Account PIN", CredentialValidator.PIN_POLICY, CredentialValidator::validatePin, true)));

        addAccountToDatabase();
        IOStreams.confirmReturnToContinue();
    }
}