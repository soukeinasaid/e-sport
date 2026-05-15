package controller;

import entity.User;
import service.UserService;
import utilies.ImageGeneratorAPI;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Base64;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class SignUpController {

    @FXML
    private TextField nomField;

    @FXML
    private TextField prenomField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label errorLabel;

    @FXML
    private ImageView profilePictureView;

    @FXML
    private Label uploadPlaceholderLabel;

    private UserService userService = new UserService();
    private String profilePictureBase64 = null;

    @FXML
    public void initialize() {
        // Generate a default avatar immediately when form opens
        generateDefaultAvatar();

        // Add listeners to name fields to regenerate avatar when name changes
        ChangeListener<String> nameChangeListener = new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                autoGenerateAvatar();
            }
        };

        nomField.textProperty().addListener(nameChangeListener);
        prenomField.textProperty().addListener(nameChangeListener);
    }

    private void generateDefaultAvatar() {
        // Generate a default cute avatar immediately
        new Thread(() -> {
            String avatarBase64 = ImageGeneratorAPI.generateDefaultAvatar("User", "Avatar");
            
            Platform.runLater(() -> {
                if (avatarBase64 != null) {
                    profilePictureBase64 = avatarBase64;
                    
                    try {
                        byte[] imageBytes = Base64.getDecoder().decode(avatarBase64);
                        ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
                        Image image = new Image(bis);
                        profilePictureView.setImage(image);
                        uploadPlaceholderLabel.setVisible(false);
                    } catch (Exception e) {
                        System.out.println("Failed to display generated avatar: " + e.getMessage());
                    }
                }
            });
        }).start();
    }

    private void autoGenerateAvatar() {
        String nom = nomField.getText();
        String prenom = prenomField.getText();

        if (!isBlank(nom) && !isBlank(prenom) && profilePictureBase64 == null) {
            // Auto-generate avatar in background
            new Thread(() -> {
                String avatarBase64 = ImageGeneratorAPI.generateDefaultAvatar(prenom, nom);
                
                Platform.runLater(() -> {
                    if (avatarBase64 != null) {
                        profilePictureBase64 = avatarBase64;
                        
                        try {
                            byte[] imageBytes = Base64.getDecoder().decode(avatarBase64);
                            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
                            Image image = new Image(bis);
                            profilePictureView.setImage(image);
                            uploadPlaceholderLabel.setVisible(false);
                        } catch (Exception e) {
                            System.out.println("Failed to display generated avatar: " + e.getMessage());
                        }
                    }
                });
            }).start();
        }
    }

    @FXML
    private void handleSignUp(ActionEvent event) {

        String nom = nomField.getText();
        String prenom = prenomField.getText();
        String email = emailField.getText();
        String motDePasse = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // 🔎 Validation
        if (isBlank(nom) || isBlank(prenom) || isBlank(email) ||
                isBlank(motDePasse) || isBlank(confirmPassword)) {

            showError("Veuillez remplir tous les champs.");
            return;
        }

        if (!motDePasse.equals(confirmPassword)) {
            showError("Les mots de passe ne correspondent pas.");
            return;
        }

        // 🔥 Création utilisateur correcte
        User user = new User(nom, prenom, email, motDePasse);
        user.setProfilePicture(profilePictureBase64);

        boolean success = userService.register(user);

        if (success) {
            showSuccess("Compte créé avec succès !");
            redirectToLogin(event);
        } else {
            showError("Erreur lors de la création du compte.");
        }
    }

    @FXML
    private void handleUploadImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Profile Picture");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                byte[] imageBytes = Files.readAllBytes(selectedFile.toPath());
                profilePictureBase64 = Base64.getEncoder().encodeToString(imageBytes);
                
                // Display the image
                Image image = new Image(selectedFile.toURI().toString());
                profilePictureView.setImage(image);
                uploadPlaceholderLabel.setVisible(false);
                
                showSuccess("Profile picture uploaded!");
            } catch (IOException e) {
                showError("Failed to load image: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleTakePhoto(ActionEvent event) {
        // Open system camera app to take a photo
        try {
            // For Windows, open the Camera app
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                Runtime.getRuntime().exec("cmd /c start microsoft.windows.camera:");
                showError("Camera app opened. Take a photo, save it, then use 'Upload Image' to select it.");
            } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                Runtime.getRuntime().exec("open /Applications/Photo\\ Booth.app");
                showError("Photo Booth opened. Take a photo, save it, then use 'Upload Image' to select it.");
            } else {
                showError("Camera not supported on this OS. Please use 'Upload Image' instead.");
            }
        } catch (IOException e) {
            showError("Could not open camera: " + e.getMessage() + ". Please use 'Upload Image' instead.");
        }
    }

    @FXML
    private void handleLoginRedirect(ActionEvent event) {
        redirectToLogin(event);
    }

    private void redirectToLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            Scene scene = new Scene(root);

            URL cssUrl = getClass().getResource("/css/style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            stage.setScene(scene);
            stage.setTitle("Login App");
            stage.show();

        } catch (IOException e) {
            showError("Impossible d'ouvrir l'interface de connexion.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: red;");
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void showSuccess(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: green;");
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}