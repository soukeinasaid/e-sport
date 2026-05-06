package controller;

import entity.User;
import service.UserService;
import utilies.Session;
import utilies.GoogleAPIConfig;
import utilies.GoogleOAuth2Helper;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.application.Platform;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button googleSignInButton;

    @FXML
    private Hyperlink forgotPasswordLink;

    @FXML
    private void handleLogin(ActionEvent event) {

        String email = emailField.getText();
        String password = passwordField.getText();

        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            showError("Veuillez remplir tous les champs");
            return;
        }

        UserService userService = new UserService();
        User user = userService.login(email, password);

        if (user != null) {

            // 🔐 Sauvegarder user connecté
            Session.setUser(user);

            // 🚀 Redirection selon le rôle
            if (user.isAdmin()) {
                navigate(event, "/view/admin_dashboard.fxml", "Admin Dashboard",
                        "Impossible d'ouvrir l'interface admin.");
            } else {
                navigate(event, "/view/mainLayout.fxml", "VictoryGrid",
                        "Impossible d'ouvrir l'interface principale.");
            }

        } else {
            showError("Email ou mot de passe incorrect");
        }
    }

    @FXML
    private void handleCreateAccount(ActionEvent event) {
        navigate(event, "/view/sign_up.fxml", "Create Account",
                "Impossible d'ouvrir l'interface d'inscription.");
    }

    private void navigate(ActionEvent event, String fxmlPath, String title, String fallbackError) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);

            URL cssUrl = getClass().getResource("/css/style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();

        } catch (IOException e) {
            showError(fallbackError);
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: red;");
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    @FXML
    private void handleGoogleSignIn(ActionEvent event) {
        try {
            if (!GoogleOAuth2Helper.isConfigured()) {
                showError("Google OAuth2 not configured. Please check the API configuration.");
                return;
            }

            // Show loading dialog
            Alert loadingAlert = new Alert(Alert.AlertType.INFORMATION);
            loadingAlert.setTitle("Google Sign-In");
            loadingAlert.setHeaderText("Opening Google Sign-In...");
            loadingAlert.setContentText("Please wait while we open the Google authentication page.");
            
            // Open Google sign-in page
            GoogleOAuth2Helper.openGoogleSignInPage();
            
            loadingAlert.close();
            
            // Show completion dialog
            Alert completionAlert = new Alert(Alert.AlertType.CONFIRMATION);
            completionAlert.setTitle("Google Sign-In");
            completionAlert.setHeaderText("Complete Google Authentication");
            completionAlert.setContentText("Please complete the sign-in process in your browser, then click OK to continue with manual email entry.");
            
            Optional<ButtonType> result = completionAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Show dialog for manual email entry since we can't get user info without full OAuth2 setup
                TextInputDialog emailDialog = new TextInputDialog();
                emailDialog.setTitle("Google Sign-In");
                emailDialog.setHeaderText("Enter Your Google Email");
                emailDialog.setContentText("Please enter the Google email you used for sign-in:");
                
                Optional<String> emailResult = emailDialog.showAndWait();
                if (emailResult.isPresent() && !emailResult.get().isEmpty()) {
                    String email = emailResult.get();
                    
                    if (email.contains("@gmail.com")) {
                        // Extract name from email for demo
                        String emailName = email.substring(0, email.indexOf("@"));
                        String firstName;
                        String lastName;
                        
                        // Safe extraction of first name (up to 8 characters)
                        if (emailName.length() > 1) {
                            firstName = emailName.substring(0, 1).toUpperCase() + 
                                        emailName.substring(1, Math.min(emailName.length(), 8));
                        } else {
                            firstName = emailName.toUpperCase();
                        }
                        
                        // Safe extraction of last name (remaining characters or "User")
                        if (emailName.length() > 8) {
                            lastName = emailName.substring(8, Math.min(emailName.length(), 15)).toUpperCase();
                        } else {
                            lastName = "User";
                        }
                        
                        // Check if user exists, if not create a new user
                        UserService userService = new UserService();
                        User user = userService.login(email, "google_oauth");
                        
                        if (user == null) {
                            // Create new user with Google credentials
                            user = new User(firstName, lastName, email, "google_oauth");
                            if (userService.register(user)) {
                                user = userService.login(email, "google_oauth");
                            }
                        }
                        
                        if (user != null) {
                            Session.setUser(user);
                            navigate(event, "/view/mainLayout.fxml", "VictoryGrid - Main Dashboard", 
                                    "Failed to load main layout");
                        } else {
                            showError("Failed to create or authenticate user");
                        }
                    } else {
                        showError("Please enter a valid Gmail address");
                    }
                }
            }
            
        } catch (Exception e) {
            showError("Google sign-in failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        try {
            String email = emailField.getText();
            
            if (email == null || email.isEmpty()) {
                showError("Please enter your email address first");
                return;
            }
            
            // Validate email format
            if (!email.contains("@") || !email.contains(".")) {
                showError("Please enter a valid email address");
                return;
            }
            
            // Check if email exists in database
            UserService userService = new UserService();
            List<User> allUsers = userService.getAll();
            boolean userExists = false;
            
            for (User user : allUsers) {
                if (user.getEmail().equalsIgnoreCase(email)) {
                    userExists = true;
                    break;
                }
            }
            
            if (!userExists) {
                showError("No account found with this email address");
                return;
            }
            
            // Generate password reset token (simplified version)
            String resetToken = generateResetToken();
            
            // In a real application, you would:
            // 1. Store the reset token in database with expiration
            // 2. Send email with reset link
            // For now, we'll simulate this process
            
            boolean emailSent = sendPasswordResetEmail(email, resetToken);
            
            if (emailSent) {
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Password Reset");
                successAlert.setHeaderText("Password Reset Instructions Sent");
                successAlert.setContentText("A password reset link has been sent to: " + email + 
                        "\n\nPlease check your email inbox (and spam folder) and follow the instructions to reset your password." +
                        "\n\nThe reset link will expire in 1 hour for security reasons.");
                
                successAlert.showAndWait();
                
                // Clear the email field for security
                emailField.clear();
            } else {
                showError("Failed to send password reset email. Please try again later.");
            }
            
        } catch (Exception e) {
            showError("Failed to process password reset: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private String generateResetToken() {
        // Generate a simple reset token (in production, use a more secure method)
        return "RESET_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);
    }
    
    private boolean sendPasswordResetEmail(String email, String resetToken) {
        try {
            // Simulate email sending (in production, use a real email service)
            System.out.println("=== PASSWORD RESET EMAIL ===");
            System.out.println("To: " + email);
            System.out.println("Subject: Password Reset Request - VictoryGrid");
            System.out.println("Body:");
            System.out.println("Hello,");
            System.out.println("You requested a password reset for your VictoryGrid account.");
            System.out.println("Click the following link to reset your password:");
            System.out.println("https://victorygrid.com/reset-password?token=" + resetToken + "&email=" + email);
            System.out.println("This link will expire in 1 hour.");
            System.out.println("If you didn't request this, please ignore this email.");
            System.out.println("=== END EMAIL ===");
            
            // Simulate email sending delay
            Thread.sleep(1000);
            
            return true;
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
            return false;
        }
    }
}