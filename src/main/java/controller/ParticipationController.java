package controller;

import entity.Team;
import entity.Tournoi;
import entity.User;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import service.TeamService;
import utilies.MainApp;
import utilies.Session;


import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class ParticipationController implements Initializable {

    @FXML private Label lblWelcome;
    @FXML private Label lblAvatarChar;
    @FXML private Label lblStatus;
    @FXML private Label lblTitle;
    @FXML private Label lblSub;
    @FXML private VBox teamsContainer;
    @FXML private TextField tfTeamName;

    private final TeamService teamService = new TeamService();
    private Tournoi currentTournament;
    private List<Team> teams;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Get current tournament from FrontOfficeController
        currentTournament = FrontOfficeController.getCurrentTournament();
        if (currentTournament == null) {
            showError("Erreur", "Aucun tournoi sélectionné.");
            handleBackToTournois();
            return;
        }

        // Welcome text & avatar letter
        User user = Session.getUser();
        if (user != null) {
            String name = user.getNom();
            lblWelcome.setText(name);
            lblAvatarChar.setText(String.valueOf(name.charAt(0)).toUpperCase());
        }

        lblTitle.setText("🏆  Choisir une Équipe — " + currentTournament.getNom());
        lblSub.setText("Rejoignez une équipe existante ou créez la vôtre pour participer !");

        loadTeams();
    }

    private void loadTeams() {
        try {
            teams = teamService.listerParTournoi(currentTournament.getId());
            renderTeams();
        } catch (SQLException e) {
            showError("Erreur de chargement", e.getMessage());
        }
    }

    private void renderTeams() {
        teamsContainer.getChildren().clear();
        if (teams.isEmpty()) {
            Label empty = new Label("Aucune équipe créée pour ce tournoi.");
            empty.setStyle("-fx-text-fill: #8A93A5; -fx-font-size: 14px;");
            teamsContainer.getChildren().add(empty);
            return;
        }

        for (Team team : teams) {
            VBox teamCard = buildTeamCard(team);
            teamsContainer.getChildren().add(teamCard);
        }
    }

    private VBox buildTeamCard(Team team) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #1E2333; -fx-padding: 16; -fx-background-radius: 10;");

        Label name = new Label("👥 " + team.getName());
        name.setStyle("-fx-text-fill: #FFF; -fx-font-size: 16px; -fx-font-weight: 700;");

        try {
            List<Integer> members = teamService.getMembres(team.getId());
            Label count = new Label("Membres: " + members.size());
            count.setStyle("-fx-text-fill: #8A93A5; -fx-font-size: 12px;");

            Button btnJoin = new Button("Rejoindre");
            btnJoin.getStyleClass().add("fo-btn-details");
            btnJoin.setOnAction(e -> handleJoinTeam(team));

            card.getChildren().addAll(name, count, btnJoin);
        } catch (SQLException e) {
            showError("Erreur", e.getMessage());
        }

        return card;
    }

    @FXML
    private void handleCreateTeam() {
        String name = tfTeamName.getText().trim();
        if (name.isEmpty()) {
            showError("Erreur", "Le nom de l'équipe est requis.");
            return;
        }

        User user = Session.getUser();
        if (user == null) {
            showError("Erreur", "Utilisateur non connecté.");
            return;
        }

        try {
            Team newTeam = new Team(name, currentTournament.getId(), user.getIdUser());
            teamService.ajouter(newTeam);
            teamService.ajouterJoueur(newTeam.getId(), user.getIdUser());
            // Navigate to confirmation
            navigateToConfirmation(newTeam);
        } catch (SQLException e) {
            showError("Erreur DB", e.getMessage());
        }
    }

    private void handleJoinTeam(Team team) {
        User user = Session.getUser();
        if (user == null) {
            showError("Erreur", "Utilisateur non connecté.");
            return;
        }

        try {
            // Check if already in team
            List<Integer> members = teamService.getMembres(team.getId());
            if (members.contains(user.getIdUser())) {
                showError("Erreur", "Vous êtes déjà dans cette équipe.");
                return;
            }
            teamService.ajouterJoueur(team.getId(), user.getIdUser());
            navigateToConfirmation(team);
        } catch (SQLException e) {
            showError("Erreur DB", e.getMessage());
        }
    }

    private void navigateToConfirmation(Team team) {
        // Set selected team in static or something
        ConfirmationController.setSelectedTeam(team);
        try {
            MainApp.navigateTo("ConfirmationView.fxml", "✅  Confirmation — " + team.getName());
        } catch (Exception e) {
            showError("Navigation", e.getMessage());
        }
    }

    @FXML
    private void handleBackToTournois() {
        try {
            MainApp.navigateTo("FrontOfficeView.fxml", "🏆  Tournois Disponibles");
        } catch (Exception e) {
            showError("Navigation", e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
       // SessionManager.logout();
        try {
            MainApp.navigateTo("login.fxml", "⚔  TournoiManager — Connexion");
        } catch (Exception e) {
            showError("Logout", e.getMessage());
        }
    }

    private void showError(String h, String m) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erreur"); a.setHeaderText(h); a.setContentText(m); a.showAndWait();
    }
}
