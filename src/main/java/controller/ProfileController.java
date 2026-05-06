package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import entity.User;
import service.UserService;
import utilies.Session;

public class ProfileController {

    @FXML
    private TextField nomField;

    @FXML
    private TextField prenomField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    private UserService userService = new UserService();

    private User currentUser;

    // ================= INIT =================
    @FXML
    public void initialize() {

        currentUser = Session.getUser();

        if (currentUser != null) {
            nomField.setText(currentUser.getNom());
            prenomField.setText(currentUser.getPrenom());
            emailField.setText(currentUser.getEmail());
            passwordField.setText(currentUser.getMotDePasse());
        }
    }

    // ================= UPDATE =================
    @FXML
    public void handleUpdate() {

        String nom = nomField.getText();
        String prenom = prenomField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty()) {
            showAlert("Erreur", "Remplir tous les champs !");
            return;
        }

        try {
            currentUser.setNom(nom);
            currentUser.setPrenom(prenom);
            currentUser.setEmail(email);
            currentUser.setMotDePasse(password);

            userService.update(currentUser);

            // 🔐 update session
            Session.setUser(currentUser);

            showAlert("Succès", "Profil mis à jour !");

        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la modification !");
            e.printStackTrace();
        }
    }

    // ================= UTIL =================
    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.show();
    }
}