import utilies.DatabaseConfig;
import utilies.MainApp;

import java.sql.Connection;

/**
 * Main runnable file for the E-Sports Forum Application
 * This file can be used to run the application directly
 */
public class RunApp {
    
    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("  VictoryGrid E-Sports Forum");
        System.out.println("=================================");
        
        // Test database connection first
        System.out.println("Testing database connection...");
        Connection conn = DatabaseConfig.getInstance().getConnection();
        
        if (conn != null) {
            System.out.println("✅ Database connection successful!");
            System.out.println("Starting JavaFX application...");
            
            // Launch the JavaFX application
            MainApp.main(args);
        } else {
            System.err.println("❌ Database connection failed!");
            System.err.println("Please ensure:");
            System.err.println("1. MySQL server is running");
            System.err.println("2. Database 'smoka' exists");
            System.err.println("3. User 'root' has proper permissions");
            System.err.println("4. Port 3306 is not blocked");
            System.err.println("");
            System.err.println("Run the database_setup.sql script first:");
            System.err.println("mysql -u root -p < database_setup.sql");
            System.err.println("");
            System.err.println("Starting application anyway (some features may not work)...");
            
            // Still try to start the application
            try {
                MainApp.main(args);
            } catch (Exception e) {
                System.err.println("Failed to start application: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
