package dao.dao_util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseSetup {
    private static final String DB_NAME = "itams_db";
    
    public static boolean databaseExists(String host, int port, String user, String password) {
        String checkUrl = String.format("jdbc:mysql://%s:%d/", host, port);
        try (Connection conn = DriverManager.getConnection(checkUrl + "?useSSL=false&allowPublicKeyRetrieval=true", user, password);
             Statement stmt = conn.createStatement()) {
            String query = "SELECT 1 FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '" + DB_NAME + "'";
            return stmt.executeQuery(query).next();
        } catch (SQLException e) {
            return false;
        }
    }
    
    public static boolean tablesExist(String host, int port, String user, String password) {
        String dbUrl = String.format("jdbc:mysql://%s:%d/%s", host, port, DB_NAME);
        try (Connection conn = DriverManager.getConnection(dbUrl + "?useSSL=false&allowPublicKeyRetrieval=true", user, password);
             Statement stmt = conn.createStatement()) {
            String query = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '" + DB_NAME + "'";
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            return false;
        }
        return false;
    }
    
    public static boolean createDatabase(String host, int port, String user, String password) {
        try {
            // Create database if it doesn't exist
            String checkUrl = String.format("jdbc:mysql://%s:%d/", host, port);
            try (Connection conn = DriverManager.getConnection(checkUrl + "?useSSL=false&allowPublicKeyRetrieval=true", user, password);
                 Statement stmt = conn.createStatement()) {
                String createDbSQL = "CREATE DATABASE IF NOT EXISTS " + DB_NAME;
                stmt.executeUpdate(createDbSQL);
            }
            
            // Create tables if they don't exist
            if (!tablesExist(host, port, user, password)) {
                executeSQLScript(host, port, user, password);
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("Error creating database: " + e.getMessage());
            return false;
        }
    }
    
    private static boolean executeSQLScript(String host, int port, String user, String password) {
        String dbUrl = String.format("jdbc:mysql://%s:%d/%s", host, port, DB_NAME);
        try (Connection conn = DriverManager.getConnection(dbUrl + "?useSSL=false&allowPublicKeyRetrieval=true", user, password);
             Statement stmt = conn.createStatement()) {
            
            // Read SQL script from resources
            String sqlScript = readSQLScript();
            if (sqlScript == null || sqlScript.isEmpty()) {
                System.err.println("SQL script not found or empty");
                return false;
            }
            
            // Split script by semicolon and execute each statement
            String[] statements = sqlScript.split(";");
            for (String sqlStatement : statements) {
                String trimmed = sqlStatement.trim();
                if (!trimmed.isEmpty()) {
                    try {
                        stmt.execute(trimmed);
                    } catch (SQLException e) {
                        // Log but continue - some statements might fail (like DROP IF EXISTS)
                        System.out.println("SQL execution note: " + e.getMessage());
                    }
                }
            }
            
            return true;
        } catch (SQLException e) {
            System.err.println("Error executing SQL script: " + e.getMessage());
            return false;
        }
    }
    
    private static String readSQLScript() {
        try (InputStream is = DatabaseSetup.class.getResourceAsStream("/sql/itams_db.sql")) {
            if (is == null) {
                System.err.println("SQL script resource not found");
                return null;
            }
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                // Skip comments and empty lines
                if (!line.trim().startsWith("--") && !line.trim().startsWith("/*") && !line.trim().isEmpty()) {
                    sb.append(line).append("\n");
                }
            }
            return sb.toString();
        } catch (IOException e) {
            System.err.println("Error reading SQL script: " + e.getMessage());
            return null;
        }
    }
    
    public static boolean testConnection(String host, int port, String user, String password) {
        String url = String.format("jdbc:mysql://%s:%d/", host, port);
        try (Connection conn = DriverManager.getConnection(url + "?useSSL=false&allowPublicKeyRetrieval=true", user, password)) {
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
}
