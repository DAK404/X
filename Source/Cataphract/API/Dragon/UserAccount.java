package Cataphract.API.Dragon;

/**
 * Encapsulates user account data.
 *
 * @author DAK404 (https://github.com/DAK404)
 * @version 4.0.0 (12-July-2025, Cataphract)
 */
public class UserAccount {
    private String name = "";
    private String username = "";
    private String password = "";
    private String securityKey = "";
    private String pin = "";
    private boolean isAdmin = false;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getSecurityKey() { return securityKey; }
    public void setSecurityKey(String securityKey) { this.securityKey = securityKey; }
    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }
    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean isAdmin) { this.isAdmin = isAdmin; }
}