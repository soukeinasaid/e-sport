package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.MainFX;
import org.example.entities.Team;
import org.example.entities.Tournoi;
import org.example.entities.User;
import org.example.services.TeamService;
import org.example.utils.SessionManager;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class ConfirmationController implements Initializable {

    @FXML private Label lblWelcome;
    @FXML private Label lblAvatarChar;
    @FXML private Label lblStatus;
    @FXML private Label lblTitle;
    @FXML private Label lblSub;
    @FXML private VBox confirmationContent;

    private static Team selectedTeam;
    private Tournoi currentTournament;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentTournament = FrontOfficeController.getCurrentTournament();
        if (selectedTeam == null || currentTournament == null) {
            showError("Erreur", "Aucune équipe sélectionnée.");
            handleBackToTournois();
            return;
        }

        // Welcome text & avatar letter
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            String name = user.getUsername();
            lblWelcome.setText(name);
            lblAvatarChar.setText(String.valueOf(name.charAt(0)).toUpperCase());
        }

        lblTitle.setText("✅  Confirmation — " + selectedTeam.getName());
        lblSub.setText("Votre participation est confirmée ! Préparez-vous pour le tournoi.");

        renderConfirmation();
    }

    private void renderConfirmation() {
        confirmationContent.getChildren().clear();

        VBox content = new VBox(16);
        content.setStyle("-fx-background-color: #1E2333; -fx-padding: 24; -fx-background-radius: 10;");

        Label teamLabel = new Label("👥 Équipe: " + selectedTeam.getName());
        teamLabel.setStyle("-fx-text-fill: #FFF; -fx-font-size: 18px; -fx-font-weight: 700;");

        Label tournoiLabel = new Label("🏆 Tournoi: " + currentTournament.getNom());
        tournoiLabel.setStyle("-fx-text-fill: #8A93A5; -fx-font-size: 14px;");

        Separator sep = new Separator();

        Label success = new Label("🎉 Félicitations ! Vous avez rejoint l'équipe avec succès.");
        success.setStyle("-fx-text-fill: #00FFD1; -fx-font-size: 16px; -fx-font-weight: 600;");

        Button btnViewBracket = new Button("📊 Voir le Bracket");
        btnViewBracket.getStyleClass().add("fo-btn-details");
        btnViewBracket.setOnAction(e -> handleViewBracket());

        content.getChildren().addAll(teamLabel, tournoiLabel, sep, success, btnViewBracket);
        confirmationContent.getChildren().add(content);
    }

    @FXML
    private void handleViewBracket() {
        // Navigate to bracket view
        try {
            MainFX.navigateTo("BracketView.fxml", "📊  Bracket — " + currentTournament.getNom());
        } catch (Exception e) {
            showError("Navigation", e.getMessage());
        }
    }

    @FXML
    private void handleBackToTournois() {
        try {
            MainFX.navigateTo("FrontOfficeView.fxml", "🏆  Tournois Disponibles");
        } catch (Exception e) {
            showError("Navigation", e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        SessionManager.logout();
        try {
            MainFX.navigateTo("LoginView.fxml", "⚔  TournoiManager — Connexion");
        } catch (Exception e) {
            showError("Logout", e.getMessage());
        }
    }

    private void showError(String h, String m) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erreur"); a.setHeaderText(h); a.setContentText(m); a.showAndWait();
    }

    public static void setSelectedTeam(Team team) {
        selectedTeam = team;
    }

    public static Team getSelectedTeam() {
        return selectedTeam;
    }
}
