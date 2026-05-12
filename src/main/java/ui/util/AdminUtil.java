package ui.util;

import java.io.IOException;
import java.util.Properties;
import dao.dao_util.CredentialManager;
/**
 * Utility class for Administrative Authorization.
 * 
 * Provides centralized logic to determine the application's current 
 * authorization state and access level.
 * 
 * - Evaluates the security context by interfacing with {@link CredentialManager} 
 *   to check for active session properties.
 * - Determines if "Admin" privileges should be granted based on the 
 *   persisted application mode on files like {@link Dashboard}.
 */
public class AdminUtil {

    private static final CredentialManager credentialManager = new CredentialManager();

    private AdminUtil() {}

    public static boolean isAdminMode() {
        if (!credentialManager.exists()) return true;
        try {
            Properties props = credentialManager.load();
            return "Admin".equalsIgnoreCase(props.getProperty("app_mode", "Admin"));
        } catch (IOException e) {
            return true;
        }
    }
    
}