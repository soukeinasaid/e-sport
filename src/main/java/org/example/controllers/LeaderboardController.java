package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.MainFX;
import org.example.entities.Team;
import org.example.entities.TeamStats;
import org.example.entities.Tournoi;
import org.example.entities.User;
import org.example.services.TeamService;
import org.example.utils.SessionManager;

import java.net.URL;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class LeaderboardController implements Initializable {

    @FXML private Label lblWelcome;
    @FXML private Label lblAvatarChar;
    @FXML private Label lblStatus;
    @FXML private Label lblTitle;
    @FXML private Label lblSub;
    @FXML private TableView<TeamStats> leaderboardTable;
    @FXML private Label statsLabel;

    private final TeamService teamService = new TeamService();
    private Tournoi currentTournament;
    private ObservableList<TeamStats> leaderboardData = FXCollections.observableArrayList();

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

        lblTitle.setText("📊  Classements — " + currentTournament.getNom());
        lblSub.setText("Découvrez les classements et statistiques détaillées des équipes !");

        loadLeaderboard();

        // Add listener for table selection
        leaderboardTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showTeamStats(newSelection);
            }
        });
    }

    private void loadLeaderboard() {
        try {
            List<Team> teams = teamService.listerParTournoi(currentTournament.getId());
            leaderboardData.clear();
            if (teams.isEmpty()) {
                statsLabel.setText("Aucune équipe inscrite dans ce tournoi.\nLe classement sera affiché dès qu'une équipe participera.");
                leaderboardTable.setItems(leaderboardData);
                return;
            }
            int rank = 1;
            for (Team team : teams) {
                int wins = teamService.getWins(team.getId());
                int losses = teamService.getLosses(team.getId());
                int matchesPlayed = teamService.getMatchesPlayed(team.getId());
                TeamStats stats = new TeamStats(rank++, team.getName(), wins, losses, matchesPlayed, team.getId());
                leaderboardData.add(stats);
            }
            // Sort by wins descending
            leaderboardData.sort(Comparator.comparing(TeamStats::getWins).reversed());
            // Update ranks
            for (int i = 0; i < leaderboardData.size(); i++) {
                leaderboardData.get(i).setRank(i + 1);
            }
            leaderboardTable.setItems(leaderboardData);
        } catch (SQLException e) {
            showError("Erreur de chargement", e.getMessage());
        }
    }

    private void showTeamStats(TeamStats stats) {
        String text = String.format("Équipe: %s\nVictoires: %d\nDéfaites: %d\nMatchs Joués: %d\nTaux de Victoire: %.2f%%",
                stats.getTeamName(),
                stats.getWins(),
                stats.getLosses(),
                stats.getMatchesPlayed(),
                stats.getMatchesPlayed() > 0 ? (double) stats.getWins() / stats.getMatchesPlayed() * 100 : 0);
        statsLabel.setText(text);
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
    private void handleClassements() {
        // Already here
    }

    @FXML
    private void handleStatistiques() {
        // For now, same as classements
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
    private void handleViewBracket() {
        try {
            MainFX.navigateTo("BracketView.fxml", "📊  Bracket — " + currentTournament.getNom());
        } catch (Exception e) {
            showError("Navigation", e.getMessage());
        }
    }

    private void showError(String h, String m) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erreur"); a.setHeaderText(h); a.setContentText(m); a.showAndWait();
    }
}
