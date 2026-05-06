package utilies;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    final String URL = "jdbc:mysql://127.0.0.1:3306/smoka";
    final String USER = "root";
    final String PASSWORD = "";

    private Connection connection;
    private static utilies.DatabaseConfig instance;

    private DatabaseConfig() {}   // constructeur vide

    public static utilies.DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new utilies.DatabaseConfig();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                try {
                    connection = DriverManager.getConnection(URL, USER, PASSWORD);
                    System.out.println("Connected to database successfully");
                    
                    // Test the connection
                    if (!connection.isValid(5)) {
                        throw new SQLException("Connection is not valid");
                    }
                } catch (SQLException e) {
                    System.err.println("Database connection failed: " + e.getMessage());
                    System.err.println("Please ensure MySQL server is running on " + URL);
                    System.err.println("Check if:");
                    System.err.println("1. MySQL server is installed and running");
                    System.err.println("2. Database 'smoka' exists");
                    System.err.println("3. User 'root' has proper permissions");
                    System.err.println("4. No firewall blocking port 3306");
                    throw e;
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to get database connection: " + e.getMessage());
            return null;
        }
        return connection;
    }
}
