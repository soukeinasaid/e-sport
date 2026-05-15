package utilies;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseSetup {
    
    public static void main(String[] args) {
        setupDatabase();
    }
    
    public static void setupDatabase() {
        String url = "jdbc:mysql://localhost:3306/";
        String dbName = "smoka";
        String username = "root";
        String password = "";
        
        try {
            // Connect to MySQL server (without specifying database)
            Connection conn = DriverManager.getConnection(url, username, password);
            Statement stmt = conn.createStatement();
            
            System.out.println("Connected to MySQL server");
            
            // Create database if not exists
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            System.out.println("Database created or already exists");
            
            // Switch to the database
            stmt.executeUpdate("USE " + dbName);
            System.out.println("Using database: " + dbName);
            
            // Create utilisateur table first
            String createUtilisateur = "CREATE TABLE IF NOT EXISTS utilisateur (" +
                "idUser INT AUTO_INCREMENT PRIMARY KEY," +
                "nom VARCHAR(100) NOT NULL," +
                "prenom VARCHAR(100) NOT NULL," +
                "email VARCHAR(100) NOT NULL UNIQUE," +
                "motDePasse VARCHAR(255) NOT NULL," +
                "role ENUM('USER', 'ADMIN') DEFAULT 'USER'," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB";
            stmt.executeUpdate(createUtilisateur);
            System.out.println("Table 'utilisateur' created or already exists");
            
            // Create forum table second (references utilisateur)
            String createForum = "CREATE TABLE IF NOT EXISTS forum (" +
                "idForum INT AUTO_INCREMENT PRIMARY KEY," +
                "titre VARCHAR(255) NOT NULL," +
                "description TEXT NOT NULL," +
                "dateCreation TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "idUser INT NOT NULL," +
                "FOREIGN KEY (idUser) REFERENCES utilisateur(idUser) ON DELETE CASCADE" +
                ") ENGINE=InnoDB";
            stmt.executeUpdate(createForum);
            System.out.println("Table 'forum' created or already exists");
            
            // Drop and recreate favorites table to fix foreign key issues
            try {
                stmt.executeUpdate("DROP TABLE IF EXISTS favorites");
                System.out.println("Dropped existing favorites table");
            } catch (Exception e) {
                System.out.println("No existing favorites table to drop");
            }
            
            // Create favorites table with simplified foreign keys
            String createFavorites = "CREATE TABLE favorites (" +
                "idFavorite INT AUTO_INCREMENT PRIMARY KEY," +
                "idForum INT NOT NULL," +
                "idUser INT NOT NULL," +
                "dateAdded TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "INDEX idx_forum (idForum)," +
                "INDEX idx_user (idUser)," +
                "UNIQUE KEY unique_favorite (idForum, idUser)" +
                ") ENGINE=InnoDB";
            stmt.executeUpdate(createFavorites);
            System.out.println("Table 'favorites' created successfully");
            
            // Insert sample data if tables are empty
            insertSampleData(stmt);
            
            conn.close();
            System.out.println("Database setup completed successfully!");
            
        } catch (SQLException e) {
            System.err.println("Database setup failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void insertSampleData(Statement stmt) throws SQLException {
        try {
            // Check if utilisateur table is empty
            boolean hasUsers = stmt.executeQuery("SELECT COUNT(*) FROM utilisateur").next() && 
                             stmt.executeQuery("SELECT COUNT(*) FROM utilisateur").getInt(1) > 0;
            
            if (!hasUsers) {
                // Insert sample users
                stmt.executeUpdate("INSERT INTO utilisateur (nom, prenom, email, motDePasse, role) VALUES " +
                    "('Admin', 'User', 'admin@smoka.com', 'admin123', 'ADMIN'), " +
                    "('John', 'Doe', 'john@example.com', 'password123', 'USER'), " +
                    "('Jane', 'Smith', 'jane@example.com', 'password123', 'USER')");
                System.out.println("Sample users inserted");
                
                // Insert sample forum posts
                stmt.executeUpdate("INSERT INTO forum (titre, description, idUser) VALUES " +
                    "('Welcome to Smoka Forum!', 'This is the first post in our forum. Feel free to share your thoughts and ideas.', 1), " +
                    "('How to use this forum', 'You can create, edit, and delete posts. Use search bar to find specific topics.', 1), " +
                    "('Introduction Thread', 'Hello everyone! I''m new here and excited to be part of this community.', 2), " +
                    "('Technical Discussion', 'Let''s discuss the latest technologies and trends in software development.', 3)");
                System.out.println("Sample forum posts inserted");
            }
        } catch (Exception e) {
            System.out.println("Sample data insertion skipped (data may already exist): " + e.getMessage());
        }
    }
}
