import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConfig {
    private static final String URL = "jdbc:mysql://localhost:3307/chatdb";
    private static final String USER = "root";  // default user in XAMPP
    private static final String PASSWORD = "";  // default password in XAMPP

    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
            throw e;
        }
    }
}
