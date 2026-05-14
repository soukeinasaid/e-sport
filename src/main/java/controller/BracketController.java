package controller;
import entity.Match;
import entity.Team;
import entity.Tournoi;
import entity.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import service.MatchService;
import service.TeamService;
import utilies.MainApp;
import utilies.Session;


import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class BracketController implements Initializable {


    @FXML
    private Label lblStatus;

    @FXML
    private Label lblTitle;

    @FXML
    private Label lblSub;

    @FXML
    private VBox bracketContainer;

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

        User user = Session.getUser();

        lblTitle.setText("📊  Bracket - " + currentTournament.getNom());
        lblSub.setText("Suivez l'avancement du tournoi et vos matchs !");
        lblStatus.setText("● En ligne");

        loadMatches();
    }

    private void loadMatches() {
        try {
            matches = matchService.listerParTournoi(currentTournament.getId());
            if (matches.isEmpty()) {
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/FrontOfficeView.fxml"));
            Parent frontOfficeView = loader.load();

            Stage stage = (Stage) lblStatus.getScene().getWindow();
            BorderPane root = (BorderPane) stage.getScene().getRoot();
            root.setCenter(frontOfficeView);

            stage.setTitle("🏆 Tournois disponibles");

        } catch (Exception e) {
            e.printStackTrace();
            showError("Navigation", e.getMessage());
        }

    }

    @FXML
    private void handleLogout() {
        try {
            MainApp.navigateTo("login.fxml", "⚔  TournoiManager - Connexion");
        } catch (Exception e) {
            showError("Logout", e.getMessage());
        }
    }

    @FXML
    private void handleClassements() {
        try {
            MainApp.navigateTo("LeaderboardView.fxml", "📊  Classements & Statistiques");
        } catch (Exception e) {
            showError("Navigation", e.getMessage());
        }
    }

    private void showError(String h, String m) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erreur");
        a.setHeaderText(h);
        a.setContentText(m != null && !m.isBlank() ? m : "Une erreur est survenue. Consultez la console pour plus de détails.");
        a.showAndWait();
    }


}
