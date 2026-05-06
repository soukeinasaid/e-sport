package controller;

import entity.User;
import service.UserService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

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

    private UserService userService = new UserService();

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

        boolean success = userService.register(user);

        if (success) {
            showSuccess("Compte créé avec succès !");
            redirectToLogin(event);
        } else {
            showError("Erreur lors de la création du compte.");
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