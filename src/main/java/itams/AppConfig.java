package itams;

import dao.dao_util.CredentialManager;
import dao.dao_util.DatabaseSetup;
import java.io.IOException;
import java.util.Properties;

public class AppConfig {
    private static final CredentialManager credentialManager = new CredentialManager();
    
    public static boolean isConfigured() {
        // Check if credentials file exists
        if (!credentialManager.exists()) {
            return false;
        }
        
        // Load credentials and verify database exists
        try {
            Properties props = credentialManager.load();
            String host = props.getProperty("host", "localhost");
            int port = Integer.parseInt(props.getProperty("port", "3306"));
            String user = props.getProperty("user");
            String password = props.getProperty("password");
            
            if (user == null || password == null) {
                return false;
            }
            
            // Test connection and check if database exists and tables exist
            return DatabaseSetup.testConnection(host, port, user, password) &&
                   DatabaseSetup.databaseExists(host, port, user, password) &&
                   DatabaseSetup.tablesExist(host, port, user, password);
        } catch (IOException | NumberFormatException e) {
            return false;
        }
    }
    
    public static CredentialManager getCredentialManager() {
        return credentialManager;
    }
}
