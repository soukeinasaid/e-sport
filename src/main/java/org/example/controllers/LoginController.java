package org.example.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.example.MainFX;
import org.example.entities.User;
import org.example.services.UserService;
import org.example.utils.SessionManager;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    // ── Tab buttons ───────────────────────────────────────
    @FXML private Button btnTabLogin;
    @FXML private Button btnTabRegister;

    // ── Login panel ───────────────────────────────────────
    @FXML private VBox          paneLogin;
    @FXML private TextField     tfLoginUsername;
    @FXML private PasswordField pfLoginPassword;
    @FXML private Label         lblLoginError;

    // ── Register panel ────────────────────────────────────
    @FXML private VBox          paneRegister;
    @FXML private TextField     tfRegUsername;
    @FXML private TextField     tfRegEmail;
    @FXML private PasswordField pfRegPassword;
    @FXML private PasswordField pfRegConfirm;
    @FXML private Label         lblRegError;

    private final UserService userService = new UserService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        showLogin();
    }

    // ══════════════════════════════════════════════════════
    //  PANEL TOGGLE
    // ══════════════════════════════════════════════════════

    @FXML
    private void showLogin() {
        lblLoginError.setText("");
        // Panel swap
        paneRegister.setVisible(false);
        paneRegister.setManaged(false);
        paneLogin.setVisible(true);
        paneLogin.setManaged(true);
        fadeIn(paneLogin);
        // Tab highlight
        btnTabLogin   .getStyleClass().setAll("tab-btn", "tab-active");
        btnTabRegister.getStyleClass().setAll("tab-btn");
    }

    @FXML
    private void showRegister() {
        lblRegError.setText("");
        // Panel swap
        paneLogin.setVisible(false);
        paneLogin.setManaged(false);
        paneRegister.setVisible(true);
        paneRegister.setManaged(true);
        fadeIn(paneRegister);
        // Tab highlight
        btnTabLogin   .getStyleClass().setAll("tab-btn");
        btnTabRegister.getStyleClass().setAll("tab-btn", "tab-active");
    }

    private void fadeIn(VBox node) {
        FadeTransition ft = new FadeTransition(Duration.millis(220), node);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    // ══════════════════════════════════════════════════════
    //  LOGIN
    // ══════════════════════════════════════════════════════

    @FXML
    private void handleLogin() {
        String username = tfLoginUsername.getText().trim();
        String password = pfLoginPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            setLoginError("Veuillez remplir tous les champs.");
            return;
        }

        try {
            User user = userService.login(username, password);
            if (user == null) {
                setLoginError("Nom d'utilisateur ou mot de passe incorrect.");
                shake(paneLogin);
                return;
            }
            SessionManager.login(user);

            if (user.getRole() == User.Role.ADMIN) {
                MainFX.navigateTo("TournoiView.fxml", "⚔  Back Office — " + user.getUsername());
            } else {
                MainFX.navigateTo("FrontOfficeView.fxml", "🎮  Front Office — " + user.getUsername());
            }
        } catch (Exception e) {
            setLoginError("Erreur : " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════
    //  REGISTER
    // ══════════════════════════════════════════════════════

    @FXML
    private void handleRegister() {
        String username = tfRegUsername.getText().trim();
        String email    = tfRegEmail.getText().trim();
        String password = pfRegPassword.getText();
        String confirm  = pfRegConfirm.getText();

        StringBuilder err = new StringBuilder();
        if (username.isEmpty())
            err.append("Le nom d'utilisateur est requis.\n");
        if (email.isEmpty() || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
            err.append("Un email valide est requis.\n");
        if (password.length() < 6)
            err.append("Le mot de passe doit contenir au moins 6 caractères.\n");
        if (!password.equals(confirm))
            err.append("Les mots de passe ne correspondent pas.\n");

        if (err.length() > 0) { setRegError(err.toString().trim()); return; }

        try {
            if (userService.usernameExists(username)) {
                setRegError("Ce nom d'utilisateur est déjà pris."); return;
            }
            if (userService.emailExists(email)) {
                setRegError("Cet email est déjà utilisé."); return;
            }

            userService.register(new User(username, email, password, User.Role.USER));

            // Auto-login after registration
            User logged = userService.login(username, password);
            SessionManager.login(logged);
            MainFX.navigateTo("FrontOfficeView.fxml", "🎮  Front Office — " + username);

        } catch (SQLException e) {
            setRegError("Erreur DB : " + e.getMessage());
        } catch (Exception e) {
            setRegError("Erreur : " + e.getMessage());
        }
    }

    // ── Helpers ────────────────────────────────────────────
    private void setLoginError(String msg) { lblLoginError.setText("⚠  " + msg); }
    private void setRegError  (String msg) { lblRegError  .setText("⚠  " + msg); }

    private void shake(VBox node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(55), node);
        tt.setFromX(0); tt.setToX(12); tt.setCycleCount(4); tt.setAutoReverse(true); tt.play();
    }
}
