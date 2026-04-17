package itams.auth;

import java.util.Properties;

import dao.dao_util.CredentialManager;

public final class SessionContext {

    public static final String ADMIN_ROLE = "Admin";
    public static final String EMPLOYEE_ROLE = "Employee";

    private static String username;
    private static String role;

    private SessionContext() {
    }

    public static synchronized void setSession(String currentUsername, String currentRole) {
        username = currentUsername;
        role = normalizeRole(currentRole);
    }

    public static synchronized void initializeFromCredentials() {
        CredentialManager credentialManager = new CredentialManager();
        if (!credentialManager.exists()) {
            return;
        }

        try {
            Properties properties = credentialManager.load();
            String savedUsername = properties.getProperty("username", "").trim();
            String savedRole = properties.getProperty("app_mode", "").trim();
            if (!savedUsername.isEmpty()) {
                setSession(savedUsername, savedRole);
            }
        } catch (Exception ignored) {
            // Fall back to unauthenticated state.
        }
    }

    public static synchronized String getUsername() {
        return (username == null || username.isBlank()) ? "Unknown Admin" : username;
    }

    public static synchronized String getRole() {
        return normalizeRole(role);
    }

    public static synchronized boolean hasAuthenticatedUser() {
        return username != null && !username.isBlank() && role != null && !role.isBlank();
    }

    public static synchronized boolean isAdmin() {
        return ADMIN_ROLE.equalsIgnoreCase(getRole());
    }

    private static String normalizeRole(String value) {
        if (value == null || value.isBlank()) {
            return EMPLOYEE_ROLE;
        }
        if (value.equalsIgnoreCase("admin")) {
            return ADMIN_ROLE;
        }
        return EMPLOYEE_ROLE;
    }
}