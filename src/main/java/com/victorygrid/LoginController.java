package com.victorygrid;

import com.victorygrid.model.User;
import com.victorygrid.service.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.Optional;

public class LoginController {
    
    @FXML
    private TextField emailField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Button loginButton;
    
    @FXML
    private Button signupButton;
    
    @FXML
    private Label statusLabel;
    
    private UserService userService;
    
    @FXML
    public void initialize() {
        userService = UserService.getInstance();
        
        // Add event handlers
        loginButton.setOnAction(this::handleLogin);
        signupButton.setOnAction(this::handleSignup);
        
        // Allow Enter key to login
        passwordField.setOnAction(this::handleLogin);
    }
    
    @FXML
    private void handleLogin(ActionEvent event) {
        handleLogin();
    }
    
    @FXML
    private void handleLogin() {
        if (userService == null) {
            showStatus("System error: User service not available", "error");
            return;
        }
        
        String email = emailField != null ? emailField.getText().trim() : "";
        String password = passwordField != null ? passwordField.getText() : "";
        
        // Debug logging
        System.out.println("DEBUG: Attempting login with email: " + email);
        System.out.println("DEBUG: Password length: " + password.length());
        
        if (email.isEmpty() || password.isEmpty()) {
            showStatus("Please enter email and password", "error");
            return;
        }
        
        try {
            Optional<User> userOpt = userService.authenticate(email, password);
            System.out.println("DEBUG: Authentication result: " + (userOpt.isPresent() ? "SUCCESS" : "FAILED"));
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                System.out.println("DEBUG: Logged in user: " + user.getUsername() + ", Role: " + user.getRole());
                showStatus("Login successful! Welcome " + user.getUsername(), "success");
                navigateToDashboard(user);
            } else {
                System.out.println("DEBUG: Authentication failed for email: " + email);
                showStatus("Login failed. Try using username instead of email, or create a new account.", "error");
            }
        } catch (SQLException e) {
            System.out.println("DEBUG: SQL Exception during login: " + e.getMessage());
            showStatus("Database error: " + e.getMessage(), "error");
        }
    }
    
    @FXML
    private void handleSignup(ActionEvent event) {
        navigateToSignup();
    }
    
    private void navigateToSignup() {
        try {
            // Load signup view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/victorygrid/signup-view.fxml"));
            Parent signupRoot = loader.load();
            
            // Create new scene
            Scene signupScene = new Scene(signupRoot, 400, 650);
            signupScene.getStylesheets().add(getClass().getResource("/com/victorygrid/styles.css").toExternalForm());
            
            // Get current stage and set new scene
            Stage stage = (Stage) signupButton.getScene().getWindow();
            stage.setTitle("VictoryGrid - Sign Up");
            stage.setScene(signupScene);
            stage.setResizable(false);
            stage.centerOnScreen();
            
        } catch (Exception e) {
            showStatus("Error loading signup page: " + e.getMessage(), "error");
        }
    }
    
    private void navigateToDashboard(User user) {
        try {
            // Load dashboard
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/victorygrid/dashboard-view.fxml"));
            Parent dashboardRoot = loader.load();
            
            // Get controller and set current user
            DashboardController dashboardController = loader.getController();
            dashboardController.setCurrentUser(user);
            
            // Create new scene
            Scene dashboardScene = new Scene(dashboardRoot, 1200, 800);
            dashboardScene.getStylesheets().add(getClass().getResource("/com/victorygrid/dashboard-styles.css").toExternalForm());
            
            // Get current stage and set new scene
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setTitle("VictoryGrid - " + user.getUsername());
            stage.setScene(dashboardScene);
            stage.setMinWidth(1000);
            stage.setMinHeight(600);
            stage.centerOnScreen();
            
        } catch (Exception e) {
            showStatus("Error loading dashboard: " + e.getMessage(), "error");
            e.printStackTrace();
        }
    }
    
    private void showStatus(String message, String type) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            statusLabel.getStyleClass().removeAll("status-success", "status-error", "status-info");
            statusLabel.getStyleClass().add("status-" + type);
        }
    }
}
