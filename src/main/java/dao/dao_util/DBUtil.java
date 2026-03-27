package dao.dao_util;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBUtil {
    private static final String URL = "jdbc:mysql://localhost:3306/itams"
    + "?useSSL=false&allowPublicKeyRetrieval=true";
    private static final CredentialManager envFile = new CredentialManager();
    private DBUtil() {}

    public static Connection getConnection() throws SQLException {
        try {
            Properties props = envFile.load();
            return DriverManager.getConnection(URL, props.getProperty("username"), props.getProperty("password"));
        } catch (Exception e) {
            throw new SQLException("Failed to connect: " + e.getMessage());
        }
    }

    public static boolean isValid(String user, String pass) {
        try {
            DriverManager.getConnection(URL, user, pass).close();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
}