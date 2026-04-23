package dao.dao_util;

import java.io.*;
import java.util.Properties;

public class CredentialManager {
    private static final String ENV_FILE = "app.env";

    public CredentialManager() {} 

    public void write(String user, String pass, String mode) throws IOException {
        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", pass);
        props.setProperty("app_mode", mode);
        try (OutputStream output = new FileOutputStream(ENV_FILE)) {
            props.store(output, null);
        }
    }

    public void writeAppSession(String appUser, String appMode) throws IOException {
        Properties props = new Properties();

        if (exists()) {
            try (InputStream input = new FileInputStream(ENV_FILE)) {
                props.load(input);
            }
        }

        props.setProperty("app_user", appUser);
        props.setProperty("app_mode", appMode);

        try (OutputStream output = new FileOutputStream(ENV_FILE)) {
            props.store(output, null);
        }
    }

    public Properties load() throws IOException {
        Properties props = new Properties();
        try (InputStream input = new FileInputStream(ENV_FILE)) {
            props.load(input);
            return props;
        }
    }

    public boolean exists() {
        return new File(ENV_FILE).exists();
    }

    public void clear() {
        new File(ENV_FILE).delete();
    }
}