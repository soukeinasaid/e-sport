package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.MainFX;
import org.example.entities.Match;
import org.example.entities.Team;
import org.example.entities.Tournoi;
import org.example.entities.User;
import org.example.services.MatchService;
import org.example.services.TeamService;
import org.example.utils.SessionManager;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class BracketController implements Initializable {

    @FXML private Label lblWelcome;
    @FXML private Label lblAvatarChar;
    @FXML private Label lblStatus;
    @FXML private Label lblTitle;
    @FXML private Label lblSub;
    @FXML private VBox bracketContainer;

    private final MatchService matchService = new MatchService();
    private final TeamService teamService = new TeamService();
    private Tournoi currentTournament;
    private List<Match> matches;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentTournament = FrontOfficeController.getCurrentTournament();
        if (currentTournament == null) {
            showError("Erreur", "Aucun tournoi sélectionné.");
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

        lblTitle.setText("📊  Bracket — " + currentTournament.getNom());
        lblSub.setText("Suivez l'avancement du tournoi et vos matchs !");

        loadMatches();
    }

    private void loadMatches() {
        try {
            matches = matchService.listerParTournoi(currentTournament.getId());
            if (matches.isEmpty()) {
                // Generate bracket if no matches
                List<Team> teams = teamService.listerParTournoi(currentTournament.getId());
                if (!teams.isEmpty()) {
                    matchService.genererBracket(currentTournament.getId(), teams);
                    matches = matchService.listerParTournoi(currentTournament.getId());
                }
            }
            renderBracket();
        } catch (SQLException e) {
            showError("Erreur de chargement", e.getMessage());
        }
    }

    private void renderBracket() {
        bracketContainer.getChildren().clear();
        if (matches.isEmpty()) {
            Label empty = new Label("Aucun match disponible. Attendez que le bracket soit généré.");
            empty.setStyle("-fx-text-fill: #8A93A5; -fx-font-size: 14px;");
            bracketContainer.getChildren().add(empty);
            return;
        }

        // Group by round
        matches.stream()
            .collect(java.util.stream.Collectors.groupingBy(Match::getRound))
            .entrySet().stream()
            .sorted(java.util.Map.Entry.comparingByKey())
            .forEach(entry -> {
                int round = entry.getKey();
                List<Match> roundMatches = entry.getValue();

                Label roundLabel = new Label("Round " + round);
                roundLabel.setStyle("-fx-text-fill: #FFF; -fx-font-size: 16px; -fx-font-weight: 700;");

                VBox roundBox = new VBox(10);
                roundBox.getChildren().add(roundLabel);

                for (Match m : roundMatches) {
                    VBox matchCard = buildMatchCard(m);
                    roundBox.getChildren().add(matchCard);
                }

                bracketContainer.getChildren().add(roundBox);
            });
    }

    private VBox buildMatchCard(Match m) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #1E2333; -fx-padding: 16; -fx-background-radius: 10;");

        try {
            String team1 = m.getTeam1Id() != null ? teamService.trouverParId(m.getTeam1Id()).getName() : "TBD";
            String team2 = m.getTeam2Id() != null ? teamService.trouverParId(m.getTeam2Id()).getName() : "TBD";
            String winner = m.getWinnerId() != null ? teamService.trouverParId(m.getWinnerId()).getName() : "—";

            Label teams = new Label(team1 + " vs " + team2);
            teams.setStyle("-fx-text-fill: #FFF; -fx-font-size: 14px; -fx-font-weight: 600;");

            Label status = new Label("Statut: " + m.getStatus().name());
            status.setStyle("-fx-text-fill: #8A93A5; -fx-font-size: 12px;");

            Label winnerLabel = new Label("Gagnant: " + winner);
            winnerLabel.setStyle("-fx-text-fill: #00FFD1; -fx-font-size: 12px;");

            card.getChildren().addAll(teams, status, winnerLabel);
        } catch (SQLException e) {
            showError("Erreur", e.getMessage());
        }

        return card;
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

    @FXML
    private void handleClassements() {
        try {
            MainFX.navigateTo("LeaderboardView.fxml", "📊  Classements & Statistiques");
        } catch (Exception e) {
            showError("Navigation", e.getMessage());
        }
    }

    private void showError(String h, String m) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erreur"); a.setHeaderText(h); a.setContentText(m); a.showAndWait();
    }
}
