package com.victorygrid.controller;

import com.victorygrid.DashboardController;
import com.victorygrid.model.User;
import com.victorygrid.service.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ProfileController implements DashboardController.ViewController {
    
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> rankComboBox;
    @FXML private ImageView profileImageView;
    @FXML private Button uploadPhotoButton;
    @FXML private Label winsLabel;
    @FXML private Label lossesLabel;
    @FXML private Label winRateLabel;
    @FXML private Button updateButton;
    @FXML private Button cancelButton;
    @FXML private Label statusLabel;
    
    private User currentUser;
    private UserService userService;
    
    @FXML
    public void initialize() {
        userService = UserService.getInstance();
        
        // Initialize rank combo box with null check
        if (rankComboBox != null) {
            List<String> ranks = Arrays.asList("Bronze", "Silver", "Gold", "Platinum", "Diamond");
            rankComboBox.getItems().addAll(ranks);
        }
        
        // Set up event handlers with null checks
        if (updateButton != null) updateButton.setOnAction(e -> handleUpdateProfile());
        if (cancelButton != null) cancelButton.setOnAction(e -> handleCancel());
        if (uploadPhotoButton != null) uploadPhotoButton.setOnAction(e -> handleUploadPhoto());
        
        // Set up validation
        setupValidation();
        
        // Load default profile image
        loadDefaultProfileImage();
    }
    
    @Override
    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadUserData();
    }
    
    private void loadUserData() {
        if (currentUser != null) {
            if (usernameField != null) usernameField.setText(currentUser.getUsername());
            if (emailField != null) emailField.setText(currentUser.getEmail());
            if (rankComboBox != null) rankComboBox.setValue(currentUser.getRank());
            if (winsLabel != null) winsLabel.setText(String.valueOf(currentUser.getWins()));
            if (lossesLabel != null) lossesLabel.setText(String.valueOf(currentUser.getLosses()));
            if (winRateLabel != null) winRateLabel.setText(String.format("%.1f%%", currentUser.getWinRate()));
            
            // Clear password fields with null checks
            if (currentPasswordField != null) currentPasswordField.clear();
            if (newPasswordField != null) newPasswordField.clear();
            if (confirmPasswordField != null) confirmPasswordField.clear();
            
            // Clear status with null check
            if (statusLabel != null) statusLabel.setText("");
        }
    }
    
    private void setupValidation() {
        // Enable/disable update button based on form validity with null checks
        if (updateButton != null && usernameField != null && emailField != null) {
            updateButton.disableProperty().bind(
                usernameField.textProperty().isEmpty()
                    .or(emailField.textProperty().isEmpty())
            );
        }
    }
    
    @FXML
    private void handleUpdateProfile() {
        if (!validateForm()) {
            return;
        }
        
        try {
            // Check if current password is correct if user wants to change password
            if (!newPasswordField.getText().isEmpty()) {
                if (currentPasswordField.getText().isEmpty()) {
                    showStatus("Current password is required to change password", "error");
                    return;
                }
                
                // Verify current password
                Optional<User> authenticated = userService.authenticate(
                    currentUser.getUsername(), currentPasswordField.getText());
                if (authenticated.isEmpty()) {
                    showStatus("Current password is incorrect", "error");
                    return;
                }
            }
            
            // Update user object
            currentUser.setUsername(usernameField.getText());
            currentUser.setEmail(emailField.getText());
            currentUser.setRank(rankComboBox.getValue());
            
            // Update password if provided
            if (!newPasswordField.getText().isEmpty()) {
                currentUser.setPassword(newPasswordField.getText());
            }
            
            // Save to database
            boolean success = userService.updateUser(currentUser);
            if (success) {
                showStatus("Profile updated successfully!", "success");
                
                // If password was changed, re-authenticate
                if (!newPasswordField.getText().isEmpty()) {
                    Optional<User> updated = userService.getUserById(currentUser.getId());
                    updated.ifPresent(user -> this.currentUser = user);
                }
            } else {
                showStatus("Failed to update profile", "error");
            }
            
        } catch (SQLException e) {
            showStatus("Database error: " + e.getMessage(), "error");
        }
    }
    
    @FXML
    private void handleCancel() {
        loadUserData(); // Reset to original values
        showStatus("Changes cancelled", "info");
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
        
        // Check if username is taken (by another user)
        try {
            Optional<User> existingUser = userService.getUserByUsername(username);
            if (existingUser.isPresent() && existingUser.get().getId() != currentUser.getId()) {
                showStatus("Username is already taken", "error");
                return false;
            }
        } catch (SQLException e) {
            showStatus("Error checking username availability", "error");
            return false;
        }
        
        // Validate email
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            showStatus("Email is required", "error");
            return false;
        }
        
        if (!email.contains("@") || !email.contains(".")) {
            showStatus("Please enter a valid email address", "error");
            return false;
        }
        
        // Check if email is taken (by another user)
        try {
            Optional<User> existingUser = userService.getUserByEmail(email);
            if (existingUser.isPresent() && existingUser.get().getId() != currentUser.getId()) {
                showStatus("Email is already taken", "error");
                return false;
            }
        } catch (SQLException e) {
            showStatus("Error checking email availability", "error");
            return false;
        }
        
        // Validate password if changing
        if (!newPasswordField.getText().isEmpty()) {
            String newPassword = newPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();
            
            if (newPassword.length() < 6) {
                showStatus("New password must be at least 6 characters", "error");
                return false;
            }
            
            if (!newPassword.equals(confirmPassword)) {
                showStatus("New passwords do not match", "error");
                return false;
            }
        }
        
        // Validate rank
        if (rankComboBox.getValue() == null) {
            showStatus("Please select a rank", "error");
            return false;
        }
        
        return true;
    }
    
    private void handleUploadPhoto() {
        showStatus("Photo upload feature coming soon!", "info");
    }
    
    private void loadDefaultProfileImage() {
        try {
            // Load a default profile image
            Image defaultImage = new Image("/com/victorygrid/images/default-avatar.png");
            if (profileImageView != null) {
                profileImageView.setImage(defaultImage);
            }
        } catch (Exception e) {
            System.out.println("Could not load default profile image: " + e.getMessage());
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
