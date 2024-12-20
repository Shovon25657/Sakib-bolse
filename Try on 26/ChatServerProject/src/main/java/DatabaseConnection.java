import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3307/chat_database"; // Adjust database name if necessary
    private static final String USER = "root"; // Default user in XAMPP
    private static final String PASSWORD = ""; // Default password for XAMPP MySQL is empty

    public static Connection getConnection() throws SQLException {
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new SQLException("MySQL JDBC Driver not found", e);
        }
    }
}
