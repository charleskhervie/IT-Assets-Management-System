package dao.dao_util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DBUtil {
    private static final String BASE_URL = "jdbc:mysql://localhost:3306"
        + "?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/itams_db"
        + "?useSSL=false&allowPublicKeyRetrieval=true";
    private static final CredentialManager envFile = new CredentialManager();

    private DBUtil() {}

    // Call this once at app startup in App.java
    public static void initializeDatabase() {
        try {
            Properties props = envFile.load();
            String user = props.getProperty("user");
            String pass = props.getProperty("password");

            // Connect without specifying a DB first
            try (Connection conn = DriverManager.getConnection(BASE_URL, user, pass)) {
                // Check if itams_db already exists
                var rs = conn.getMetaData().getCatalogs();
                boolean dbExists = false;
                while (rs.next()) {
                    if ("itams_db".equalsIgnoreCase(rs.getString(1))) {
                        dbExists = true;
                        break;
                    }
                }

                if (!dbExists) {
                    System.out.println("Database not found. Creating itams_db...");
                    runSqlScript(conn);
                    System.out.println("Database created and sample data loaded.");
                }
            }
        } catch (Exception e) {
            System.err.println("Database initialization failed: " + e.getMessage());
        }
    }

    private static void runSqlScript(Connection conn) throws SQLException, IOException {
        // Load itams_db.sql from resources folder
        InputStream is = DBUtil.class.getResourceAsStream("/sql/itams_db.sql");
        if (is == null) throw new IOException("itams_db.sql not found in resources");

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            line = line.trim();
            // Skip comments
            if (line.startsWith("--") || line.startsWith("//") || line.isEmpty()) continue;
            sb.append(line).append("\n");
        }

        // Split by semicolon to get individual statements
        String[] statements = sb.toString().split(";");
        try (Statement stmt = conn.createStatement()) {
            for (String sql : statements) {
                sql = sql.trim();
                if (!sql.isEmpty()) {
                    stmt.execute(sql);
                }
            }
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            Properties props = envFile.load();
            String user = props.getProperty("user");
            String pass = props.getProperty("password");
            return DriverManager.getConnection(DB_URL, user, pass);
        } catch (Exception e) {
            throw new SQLException("Failed to connect: " + e.getMessage());
        }
    }

   public static boolean isValid(String user, String pass, String host, String port) {
        String url = "jdbc:mysql://" + host + ":" + port
            + "?useSSL=false&allowPublicKeyRetrieval=true";
        try {
            DriverManager.getConnection(url, user, pass).close();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
}