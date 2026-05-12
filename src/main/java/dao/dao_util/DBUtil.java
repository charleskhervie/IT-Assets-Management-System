package dao.dao_util;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBUtil {
    private static final CredentialManager envFile = new CredentialManager();
    private DBUtil() {}

    public static Connection getConnection() throws SQLException {
        try {
            Properties props = envFile.load();
            String host = props.getProperty("host", "localhost");
            String port = props.getProperty("port", "3306");
            String user = props.getProperty("user");     // key name in your .env file
            String pass = props.getProperty("password"); // key name in your .env file
            String url = String.format(
                "jdbc:mysql://%s:%s/itams_db?useSSL=false&allowPublicKeyRetrieval=true",
                host,
                port
            );
            return DriverManager.getConnection(url, user, pass);
        } catch (Exception e) {
            throw new SQLException("Failed to connect: " + e.getMessage());
        }
    }

    public static boolean isValid(String user, String pass) {
        try {
            DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/itams_db?useSSL=false&allowPublicKeyRetrieval=true",
                user,
                pass
            ).close();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
}
