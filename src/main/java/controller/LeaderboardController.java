package controller;

import entity.Team;
import entity.TeamStats;
import entity.Tournoi;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import service.TeamService;
import utilies.MainApp;
import utilies.Session;

import java.net.URL;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class LeaderboardController implements Initializable {

    @FXML
    private Label lblStatus;

    @FXML
    private Label lblTitle;

    @FXML
    private Label lblSub;

    @FXML
    private TableView<TeamStats> leaderboardTable;

    @FXML
    private TableColumn<TeamStats, Integer> colRank;

    @FXML
    private TableColumn<TeamStats, String> colTeam;

    @FXML
    private TableColumn<TeamStats, Integer> colWins;

    @FXML
    private TableColumn<TeamStats, Integer> colLosses;

    @FXML
    private TableColumn<TeamStats, Integer> colMatches;

    @FXML
    private Label summaryTeamsLabel;

    @FXML
    private Label summaryTopTeamLabel;

    @FXML
    private Label summaryBestRateLabel;

    @FXML
    private Label statsTitleLabel;

    @FXML
    private Label statsSubtitleLabel;

    @FXML
    private Label selectedRankLabel;

    @FXML
    private Label statWinRate;

    @FXML
    private Label statWins;

    @FXML
    private Label statLosses;

    @FXML
    private Label statMatches;

    @FXML
    private Label statsInsightLabel;

    private final TeamService teamService = new TeamService();
    private final ObservableList<TeamStats> leaderboardData = FXCollections.observableArrayList();

    private Tournoi currentTournament;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        currentTournament = FrontOfficeController.getCurrentTournament();
        if (currentTournament == null) {
            showError("Erreur", "Aucun tournoi sélectionné.");
            handleBackToTournois();
            return;
        }

        initializeTable();

        lblTitle.setText("Classements");
        lblSub.setText("Visualisez le podium, comparez les bilans et suivez la meilleure dynamique du tournoi.");
        lblStatus.setText(currentTournament.getNom());

        resetSummary();
        resetStatsPanel();
        loadLeaderboard();

        leaderboardTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                showTeamStats(newValue);
            }
        });
    }

    private void initializeTable() {
        colRank.setCellValueFactory(new PropertyValueFactory<>("rank"));
        colTeam.setCellValueFactory(new PropertyValueFactory<>("teamName"));
        colWins.setCellValueFactory(new PropertyValueFactory<>("wins"));
        colLosses.setCellValueFactory(new PropertyValueFactory<>("losses"));
        colMatches.setCellValueFactory(new PropertyValueFactory<>("matchesPlayed"));

        leaderboardTable.setItems(leaderboardData);
        leaderboardTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        leaderboardTable.setPlaceholder(new Label("Aucune donnée disponible pour ce tournoi."));
    }

    private void loadLeaderboard() {
        leaderboardData.clear();

        try {
            List<Team> teams = teamService.listerParTournoi(currentTournament.getId());

            if (teams.isEmpty()) {
                statsTitleLabel.setText("Aucune équipe inscrite");
                statsSubtitleLabel.setText("Le classement apparaîtra dès qu'une équipe participera à ce tournoi.");
                statsInsightLabel.setText("Ajoutez des équipes ou des résultats pour générer des statistiques.");
                return;
            }

            int rank = 1;
            for (Team team : teams) {
                int wins = teamService.getWins(team.getId());
                int losses = teamService.getLosses(team.getId());
                int matchesPlayed = teamService.getMatchesPlayed(team.getId());

                leaderboardData.add(new TeamStats(
                        rank++,
                        team.getName(),
                        wins,
                        losses,
                        matchesPlayed,
                        team.getId()
                ));
            }

            leaderboardData.sort(Comparator
                    .comparingInt(TeamStats::getWins)
                    .reversed()
                    .thenComparingInt(TeamStats::getLosses)
                    .thenComparing(TeamStats::getTeamName, String.CASE_INSENSITIVE_ORDER));

            for (int i = 0; i < leaderboardData.size(); i++) {
                leaderboardData.get(i).setRank(i + 1);
            }

            updateSummary();
            leaderboardTable.getSelectionModel().selectFirst();
            if (!leaderboardData.isEmpty()) {
                showTeamStats(leaderboardData.get(0));
            }
        } catch (SQLException e) {
            String errorMsg = e.getMessage() != null ? e.getMessage() : e.toString();
            showError("Erreur de chargement", "Impossible de charger le classement.\n" + errorMsg);
        }
    }

    private void updateSummary() {
        summaryTeamsLabel.setText(String.valueOf(leaderboardData.size()));

        TeamStats topTeam = leaderboardData.isEmpty() ? null : leaderboardData.get(0);
        if (topTeam != null) {
            summaryTopTeamLabel.setText(topTeam.getTeamName());
        }

        double bestRate = leaderboardData.stream()
                .filter(team -> team.getMatchesPlayed() > 0)
                .mapToDouble(team -> (double) team.getWins() / team.getMatchesPlayed() * 100)
                .max()
                .orElse(0.0);
        summaryBestRateLabel.setText(String.format("%.0f%%", bestRate));
    }

    private void showTeamStats(TeamStats stats) {
        double winRate = stats.getMatchesPlayed() > 0
                ? (double) stats.getWins() / stats.getMatchesPlayed() * 100
                : 0.0;

        selectedRankLabel.setText("#" + stats.getRank());
        statsTitleLabel.setText(stats.getTeamName());
        statsSubtitleLabel.setText(getTeamHeadline(stats, winRate));
        statWinRate.setText(String.format("%.1f%%", winRate));
        statWins.setText(String.valueOf(stats.getWins()));
        statLosses.setText(String.valueOf(stats.getLosses()));
        statMatches.setText(String.valueOf(stats.getMatchesPlayed()));
        statsInsightLabel.setText(buildInsight(stats, winRate));
    }

    private String getTeamHeadline(TeamStats stats, double winRate) {
        if (stats.getMatchesPlayed() == 0) {
            return "Aucun match joué pour le moment.";
        }
        if (winRate >= 75) {
            return "Très grosse dynamique sur ce tournoi.";
        }
        if (winRate >= 50) {
            return "Bilan solide et régulier.";
        }
        return "Peut encore progresser dans le bracket.";
    }

    private String buildInsight(TeamStats stats, double winRate) {
        if (stats.getMatchesPlayed() == 0) {
            return "Les indicateurs se mettront à jour après le premier match joué.";
        }

        return String.format(
                "%s a disputé %d match%s, gagné %d fois et affiche un taux de victoire de %.1f%%.",
                stats.getTeamName(),
                stats.getMatchesPlayed(),
                stats.getMatchesPlayed() > 1 ? "s" : "",
                stats.getWins(),
                winRate
        );
    }

    private void resetSummary() {
        summaryTeamsLabel.setText("--");
        summaryTopTeamLabel.setText("--");
        summaryBestRateLabel.setText("--");
    }

    private void resetStatsPanel() {
        selectedRankLabel.setText("--");
        statsTitleLabel.setText("Sélection d'équipe");
        statsSubtitleLabel.setText("Choisissez une ligne dans le classement pour afficher la fiche de performance.");
        statWinRate.setText("--");
        statWins.setText("--");
        statLosses.setText("--");
        statMatches.setText("--");
        statsInsightLabel.setText("Les statistiques détaillées apparaîtront ici.");
    }

    @FXML
    private void handleBackToTournois() {
        try {
            // Load the FrontOfficeView
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/FrontOfficeView.fxml"));
            Parent frontOfficeView = loader.load();

            // Get the current stage
            Stage stage = (Stage) leaderboardTable.getScene().getWindow();

            // Get the root BorderPane (MainLayout)
            BorderPane root = (BorderPane) stage.getScene().getRoot();

            // Set the FrontOfficeView in the center
            root.setCenter(frontOfficeView);

            // Update window title
            stage.setTitle("🏆 Tournois disponibles");

        } catch (Exception e) {
            showError("Navigation", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            Session.setUser(null);
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Stage stage = (Stage) leaderboardTable.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Connexion");
            stage.show();
        } catch (Exception e) {
            showError("Logout", e.getMessage());
        }
    }

    @FXML
    private void handleViewBracket() {
        if (currentTournament == null) {
            showError("Navigation", "Aucun tournoi sélectionné.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/BracketView.fxml"));
            Parent bracketView = loader.load();

            Stage stage = (Stage) leaderboardTable.getScene().getWindow();
            BorderPane root = (BorderPane) stage.getScene().getRoot();
            root.setCenter(bracketView);

            stage.setTitle("📊 Bracket - " + currentTournament.getNom());
        } catch (Exception e) {
            showError("Navigation", e.getMessage());
        }
    }

    private void showError(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(header);
        alert.setContentText(
                message != null && !message.isBlank()
                        ? message
                        : "Une erreur est survenue. Consultez la console pour plus de détails."
        );
        alert.showAndWait();
    }
}