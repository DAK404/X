package Cataphract.API.Dragon;

/**
 * Interface for account management operations.
 *
 * @author DAK404 (https://github.com/DAK404)
 * @version 4.0.0 (12-July-2025, Cataphract)
 */
public interface AccountManager {
    /**
     * Executes the main account management logic.
     *
     * @throws Exception If an error occurs.
     */
    void execute() throws Exception;

    /**
     * Authenticates the current user.
     *
     * @param password The password.
     * @param securityKey The security key.
     * @return true if authentication succeeds, false otherwise.
     * @throws Exception If an error occurs.
     */
    boolean authenticate(String password, String securityKey) throws Exception;
}