package controller;

import entity.User;
import service.UserService;
import utilies.Session;

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

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

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

            // 🚀 Redirection vers MainLayout
            navigate(event, "/view/mainLayout.fxml", "forum",
                    "Impossible d'ouvrir l'interface principale.");

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
}