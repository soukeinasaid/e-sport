// Simple compilation test to check for any missing dependencies
import entity.User;
import service.UserService;
import controller.AdminController;
import controller.UserManagementController;
import controller.MainLayoutController;
import controller.LoginController;
import utilies.DatabaseConfig;
import utilies.Session;

public class test_compilation {
    public static void main(String[] args) {
        System.out.println("Testing compilation...");
        
        // Test User entity
        User user = new User("Test", "User", "test@test.com", "password");
        System.out.println("User created: " + user.getEmail());
        System.out.println("User role: " + user.getRoleString());
        System.out.println("Is admin: " + user.isAdmin());
        
        // Test role conversion
        user.setRoleFromString("ADMIN");
        System.out.println("After role change: " + user.getRoleString());
        
        System.out.println("All basic functionality works!");
    }
}
