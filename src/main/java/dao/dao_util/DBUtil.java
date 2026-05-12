package dao.dao_util;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
/**
 * utility class for managing database connectivity and connection pooling 
 * logic 
 * 
 * <p>it provides methods to establish a {@link java.sql.Connection} using 
 * credentials retrieved from an external app.env file and offers 
 * verification tools to validate database access.</p>
 * 
 */
public class DBUtil {
    private static final String URL = "jdbc:mysql://localhost:3306/itams_db"
    + "?useSSL=false&allowPublicKeyRetrieval=true";
    private static final CredentialManager envFile = new CredentialManager();
    private DBUtil() {}

    public static Connection getConnection() throws SQLException {
        try {
            Properties props = envFile.load();
            String user = props.getProperty("user");    
            String pass = props.getProperty("password"); 
            return DriverManager.getConnection(URL, user, pass);
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