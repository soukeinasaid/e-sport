package com.victorygrid;

import com.victorygrid.model.User;
import com.victorygrid.service.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.Optional;

public class SignupController {
    
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Button signupButton;
    @FXML private Button cancelButton;
    @FXML private Label statusLabel;
    
    private UserService userService;
    
    @FXML
    public void initialize() {
        userService = UserService.getInstance();
        
        // Initialize role ComboBox
        roleComboBox.setItems(FXCollections.observableArrayList("PLAYER", "ADMIN"));
        roleComboBox.setValue("PLAYER");
        
        // Add event handlers
        signupButton.setOnAction(e -> handleSignup());
        cancelButton.setOnAction(e -> handleCancel());
        
        // Enable/disable signup button based on form validity
        signupButton.disableProperty().bind(
            usernameField.textProperty().isEmpty()
                .or(emailField.textProperty().isEmpty())
                .or(passwordField.textProperty().isEmpty())
                .or(confirmPasswordField.textProperty().isEmpty())
        );
    }
    
    @FXML
    private void handleSignup() {
        if (!validateForm()) {
            return;
        }
        
        try {
            // Check if username already exists
            Optional<User> existingUser = userService.getUserByUsername(usernameField.getText().trim());
            if (existingUser.isPresent()) {
                showStatus("Username already exists", "error");
                return;
            }
            
            // Check if email already exists
            existingUser = userService.getUserByEmail(emailField.getText().trim());
            if (existingUser.isPresent()) {
                showStatus("Email already exists", "error");
                return;
            }
            
            // Create new user
            User newUser = new User();
            newUser.setUsername(usernameField.getText().trim());
            newUser.setEmail(emailField.getText().trim());
            newUser.setPassword(passwordField.getText());
            newUser.setRole(roleComboBox.getValue());
            newUser.setRank("Bronze"); // Default rank for new users
            
            // Save user to database
            User createdUser = userService.createUser(newUser);
            if (createdUser.getId() > 0) {
                showStatus("Account created successfully! Redirecting to login...", "success");
                
                // Redirect to login page after 2 seconds
                javafx.application.Platform.runLater(() -> {
                    try {
                        Thread.sleep(2000);
                        navigateToLogin();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            } else {
                showStatus("Failed to create account", "error");
            }
            
        } catch (SQLException e) {
            showStatus("Registration error: " + e.getMessage(), "error");
        }
    }
    
    @FXML
    private void handleCancel() {
        navigateToLogin();
    }
    
    private boolean validateForm() {
        // Validate username
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            showStatus("Username is required", "error");
            return false;
        }
        
        if (username.length() < 3) {
            showStatus("Username must be at least 3 characters", "error");
            return false;
        }
        
        // Validate email
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            showStatus("Email is required", "error");
            return false;
        }
        
        if (!email.contains("@") || !email.contains(".")) {
            showStatus("Please enter a valid email", "error");
            return false;
        }
        
        // Validate password
        String password = passwordField.getText();
        if (password.isEmpty()) {
            showStatus("Password is required", "error");
            return false;
        }
        
        if (password.length() < 6) {
            showStatus("Password must be at least 6 characters", "error");
            return false;
        }
        
        // Validate password confirmation
        String confirmPassword = confirmPasswordField.getText();
        if (!password.equals(confirmPassword)) {
            showStatus("Passwords do not match", "error");
            return false;
        }
        
        // Validate role
        String role = roleComboBox.getValue();
        if (role == null || role.isEmpty()) {
            showStatus("Please select an account type", "error");
            return false;
        }
        
        return true;
    }
    
    private void navigateToLogin() {
        try {
            // Load login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/victorygrid/login-view.fxml"));
            Parent loginRoot = loader.load();
            
            // Create new scene
            Scene loginScene = new Scene(loginRoot, 400, 600);
            loginScene.getStylesheets().add(getClass().getResource("/com/victorygrid/styles.css").toExternalForm());
            
            // Get current stage and set new scene
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.setTitle("VictoryGrid - Login");
            stage.setScene(loginScene);
            stage.setResizable(false);
            stage.centerOnScreen();
            
        } catch (Exception e) {
            showStatus("Error loading login page: " + e.getMessage(), "error");
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
