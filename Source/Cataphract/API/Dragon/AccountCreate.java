package Cataphract.API.Dragon;

import Cataphract.API.Config;

/**
 * A class to create new user accounts on the system. Can be restricted by policy "account_create".
 *
 * @author DAK404 (https://github.com/DAK404)
 * @version 4.0.0 (12-July-2025, Cataphract)
 * @since 0.0.1 (Zen Quantum 0.0.1)
 */
public final class AccountCreate implements AccountManager 
{
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
        if (! Config.policyCheck.retrievePolicyValue("account_create").equals("on") && !isCurrentUserAdmin) {
            Config.io.printError("Policy Management System - Permission Denied.");
            return;
        }

        Config.build.viewBuildInfo(false);
        Config.io.println("Please authenticate to continue.");
        Config.io.println("Username: " + new Login(currentUsername).getNameLogic());
        String password = String.valueOf(Config.console.readPassword("Password: "));
        String securityKey = String.valueOf(Config.console.readPassword("SecurityKey: "));

        if (!authenticate(password, securityKey)) 
        {
            Config.io.println("Failed to authenticate user. Exiting...");
            return;
        }

        if (isCurrentUserAdmin) {
            promptForAdminPrivileges();
        }

        setCredentials();
        addAccountToDatabase();
        Config.io.confirmReturnToContinue();
    }

    @Override
    public boolean authenticate(String password, String securityKey) throws Exception 
    {
        Config.build.viewBuildInfo(false);
        Config.io.println("Username: " + new Login(currentUsername).getNameLogic());
        String hashedPassword = Config.cryptography.stringToSHA3_256(password);
        String hashedSecurityKey = securityKey.isEmpty() ? "" : Config.cryptography.stringToSHA3_256(securityKey);
        return new Login(currentUsername).authenticationLogic(hashedPassword, hashedSecurityKey);
    }

    /**
     * Prompts for granting admin privileges to the new account.
     */
    private void promptForAdminPrivileges() 
    {
        Config.io.printAttention("The currently logged in user is an administrator.\nYou have the privileges to create other administrator accounts or standard user accounts.\n");
        Config.io.printWarning("Administrative rights have additional privileges over standard users! Beware on who the administrative privileges are granted to!\n");
        Config.io.println("Would you like to grant administrative privileges to the new user account? [ Y | N ]");
        account.setAdmin(Config.console.readLine("Grant Administrator Privileges?> ").equalsIgnoreCase("Y"));
    }

    /**
     * Sets all credentials for the new account.
     *
     * @throws Exception If an error occurs during input validation.
     */
    private void setCredentials() throws Exception 
    {
        account.setName(setCredential("Account Name", CredentialValidator.NAME_POLICY, CredentialValidator::validateName));
        account.setUsername(Config.cryptography.stringToSHA3_256(setCredential("Account Username", CredentialValidator.USERNAME_POLICY, CredentialValidator::validateUsername)));
        account.setPassword(Config.cryptography.stringToSHA3_256(setCredential("Account Password", CredentialValidator.PASSWORD_POLICY, CredentialValidator::validatePassword, true)));
        String securityKey = setCredential("Account Security Key", CredentialValidator.SECURITY_KEY_POLICY, CredentialValidator::validateSecurityKey, true);
        account.setSecurityKey(securityKey.isEmpty() ? "" : Config.cryptography.stringToSHA3_256(securityKey));
        account.setPin(Config.cryptography.stringToSHA3_256(setCredential("Account PIN", CredentialValidator.PIN_POLICY, CredentialValidator::validatePin, true)));
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
            String input = CredentialValidator.validateCredential(prompt, policy, validator, Config.console, isPassword);
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
        Config.build.viewBuildInfo(false);
        Config.io.println("-------------------------------------------------");
        Config.io.println("| User Management Config.console: Account Creation     |");
        Config.io.println("-------------------------------------------------\n");
        Config.io.println("Account Name  : " + (account.getName().isEmpty() ? "NOT SET" : account.getName()));
        Config.io.println("Username      : " + (account.getUsername().isEmpty() ? "NOT SET" : account.getUsername()));
        Config.io.println("Password      : " + (account.getPassword().isEmpty() ? "NOT SET" : "********"));
        Config.io.println("SecurityKey   : " + (account.getSecurityKey().isEmpty() ? "NOT SET" : "********"));
        Config.io.println("PIN           : " + (account.getPin().isEmpty() ? "NOT SET" : "****"));
        Config.io.println("Account Privileges: " + (account.isAdmin() ? "Administrator" : "Standard") + "\n");
        Config.io.println("========================================");
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
            Config.io.printInfo("Account Creation Successful!");
        } else {
            Config.io.printError("Account Creation Failed.");
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
        account.setUsername(Config.cryptography.stringToSHA3_256("Administrator"));

        Config.io.println("Administrator : " + account.isAdmin());
        Config.io.println("Account Name  : " + account.getName());
        Config.io.println("Username      : " + account.getUsername());

        account.setPassword(Config.cryptography.stringToSHA3_256(setCredential("Account Password", CredentialValidator.PASSWORD_POLICY, CredentialValidator::validatePassword, true)));
        String securityKey = setCredential("Account Security Key", CredentialValidator.SECURITY_KEY_POLICY, CredentialValidator::validateSecurityKey, true);
        account.setSecurityKey(securityKey.isEmpty() ? "" : Config.cryptography.stringToSHA3_256(securityKey));
        account.setPin(Config.cryptography.stringToSHA3_256(setCredential("Account PIN", CredentialValidator.PIN_POLICY, CredentialValidator::validatePin, true)));

        addAccountToDatabase();
        Config.io.confirmReturnToContinue();
    }
}