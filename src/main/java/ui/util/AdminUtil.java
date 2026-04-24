package ui.util;

import java.io.IOException;
import java.util.Properties;
import dao.dao_util.CredentialManager;

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