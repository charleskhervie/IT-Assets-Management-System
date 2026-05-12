package dao.dao_util;

import java.io.*;
import java.util.Properties;

public class CredentialManager {
    private static final String ENV_FILE;
    private static final String LEGACY_ENV_FILE;

    static {
        String appEnvDir = resolveAppDirectory();
        ENV_FILE = appEnvDir + File.separator + ".env";
        LEGACY_ENV_FILE = appEnvDir + File.separator + "app.env";
        System.out.println("[INFO] CredentialManager: Using directory: " + appEnvDir);
    }

    public CredentialManager() {}

    private static String resolveAppDirectory() {
        String exeDir = System.getProperty("exe.dir");
        if (exeDir != null && !exeDir.isBlank()) {
            return exeDir;
        }
        return System.getProperty("user.dir");
    }

    private File getPrimaryEnvFile() {
        return new File(ENV_FILE);
    }

    private File getLegacyEnvFile() {
        return new File(LEGACY_ENV_FILE);
    }

    private File getExistingEnvFile() throws IOException {
        File primary = getPrimaryEnvFile();
        if (primary.exists()) {
            return primary;
        }

        File legacy = getLegacyEnvFile();
        if (legacy.exists()) {
            Properties props = new Properties();
            try (InputStream input = new FileInputStream(legacy)) {
                props.load(input);
            }
            persist(props);
            return primary;
        }

        return primary;
    }

    private void persist(Properties props) throws IOException {
        File envFile = getPrimaryEnvFile();
        File parent = envFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Failed to create directory for " + envFile.getAbsolutePath());
        }

        try (OutputStream output = new FileOutputStream(envFile)) {
            props.store(output, null);
        }

        File legacy = getLegacyEnvFile();
        if (!legacy.equals(envFile) && legacy.exists() && !legacy.delete()) {
            System.out.println("[WARN] Failed to delete legacy env file: " + legacy.getAbsolutePath());
        }
    }

    public void write(String host, int port, String user, String pass, String mode) throws IOException {
        Properties props = new Properties();
        props.setProperty("host", host);
        props.setProperty("port", String.valueOf(port));
        props.setProperty("user", user);
        props.setProperty("password", pass);
        props.setProperty("app_mode", mode);
        
        System.out.println("[DEBUG] Saving credentials to: " + ENV_FILE);
        System.out.println("[DEBUG] Working directory: " + System.getProperty("user.dir"));
        
        try {
            persist(props);
            System.out.println("[SUCCESS] Credentials saved successfully!");
        } catch (IOException e) {
            System.out.println("[ERROR] Failed to save credentials: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public void writeWithoutHost(String user, String pass, String mode) throws IOException {
        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", pass);
        props.setProperty("app_mode", mode);
        persist(props);
    }

    public void writeAppSession(String appUser, String appMode) throws IOException {
        Properties props = new Properties();

        if (exists()) {
            try (InputStream input = new FileInputStream(getExistingEnvFile())) {
                props.load(input);
            }
        }

        props.setProperty("app_user", appUser);
        props.setProperty("app_mode", appMode);

        persist(props);
    }

    public Properties load() throws IOException {
        Properties props = new Properties();
        try (InputStream input = new FileInputStream(getExistingEnvFile())) {
            props.load(input);
            return props;
        }
    }

    public boolean exists() {
        if (getPrimaryEnvFile().exists()) {
            return true;
        }

        if (getLegacyEnvFile().exists()) {
            try {
                return getExistingEnvFile().exists();
            } catch (IOException e) {
                return true;
            }
        }

        return false;
    }

    public void clear() {
        File primary = getPrimaryEnvFile();
        if (primary.exists()) {
            primary.delete();
        }

        File legacy = getLegacyEnvFile();
        if (legacy.exists()) {
            legacy.delete();
        }
    }
}
