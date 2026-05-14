package controller;

import entity.Centre;
import entity.Tournoi;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import service.CentreService;
import service.TournoiService;
import service.WeatherService;
import utilies.MainApp;
import utilies.Session;


import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class FrontOfficeController implements Initializable {


    @FXML
    private Label lblTotalCount;

    @FXML
    private Label lblActiveCount;

    @FXML
    private Label lblUpcomingCount;

    @FXML
    private TextField tfSearch;

    @FXML
    private ComboBox<String> cbFilter;

    @FXML
    private FlowPane cardsPane;

    private final TournoiService tournoiService = new TournoiService();
    private final CentreService centreService = new CentreService();
    private final WeatherService weatherService = new WeatherService();

    private List<Tournoi> allTournois;
    private List<Centre> centreList;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String[] BANNER_CLASSES = {
            "fo-card-banner-1",
            "fo-card-banner-2",
            "fo-card-banner-3",
            "fo-card-banner-4",
            "fo-card-banner-5",
            "fo-card-banner-6"
    };

    private static Tournoi currentTournament;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        loadCentres();
        loadData();
        setupSearch();
    }

    private void loadCentres() {
        try {
            centreList = centreService.listerTous();
        } catch (SQLException e) {
            showError("Centres", e.getMessage());
        }
    }

    private void loadData() {
        try {
            allTournois = tournoiService.listerTous();
            populateGameFilter();
            renderCards(allTournois);
            updateStats(allTournois);
        } catch (SQLException e) {
            showError("Chargement", e.getMessage());
        }
    }

    private void populateGameFilter() {
        cbFilter.getItems().clear();
        cbFilter.getItems().add("Tous les jeux");
        allTournois.stream()
                .map(Tournoi::getJeu)
                .distinct()
                .sorted()
                .forEach(g -> cbFilter.getItems().add(g));
        cbFilter.setValue("Tous les jeux");
    }

    private void setupSearch() {
        tfSearch.textProperty().addListener((obs, o, nv) -> applyFilter());
        cbFilter.valueProperty().addListener((obs, o, nv) -> applyFilter());
    }

    private void applyFilter() {
        if (allTournois == null) {
            return;
        }

        String q = tfSearch.getText().toLowerCase().trim();
        String game = cbFilter.getValue();
        List<Tournoi> filtered = allTournois.stream()
                .filter(t -> q.isEmpty()
                        || t.getNom().toLowerCase().contains(q)
                        || t.getLieu().toLowerCase().contains(q))
                .filter(t -> game == null || game.equals("Tous les jeux") || t.getJeu().equals(game))
                .toList();
        renderCards(filtered);
        updateStats(filtered);
    }

    private void renderCards(List<Tournoi> list) {
        cardsPane.getChildren().clear();
        if (list.isEmpty()) {
            VBox empty = new VBox(10);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(60, 0, 0, 0));
            empty.setPrefWidth(cardsPane.getPrefWrapLength());

            Label icon = new Label("🏆");
            icon.setStyle("-fx-font-size:56px;-fx-text-fill:#2A2F3F;");

            Label msg = new Label("Aucun tournoi trouvé");
            msg.setStyle("-fx-text-fill:#5A6480;-fx-font-size:16px;-fx-font-weight:700;");

            empty.getChildren().addAll(icon, msg);
            cardsPane.getChildren().add(empty);
            return;
        }

        int i = 0;
        for (Tournoi t : list) {
            cardsPane.getChildren().add(buildCard(t, i++ % BANNER_CLASSES.length));
        }
    }

    private VBox buildCard(Tournoi t, int colorIdx) {
        LocalDate today = LocalDate.now();
        String status;
        String statusClass;
        if (t.getDateDebut().isAfter(today)) {
            status = "🔵  À venir";
            statusClass = "fo-status-upcoming";
        } else if (t.getDateFin().isBefore(today)) {
            status = "⚫  Terminé";
            statusClass = "fo-status-finished";
        } else {
            status = "🟢  En cours";
            statusClass = "fo-status-active";
        }

        String centreName = centreList != null
                ? centreList.stream()
                  .filter(c -> c.getId() == t.getCentreId())
                  .findFirst()
                  .map(Centre::getName)
                  .orElse("—")
                : "—";

        Label gameBadge = new Label(t.getJeu().toUpperCase());
        gameBadge.getStyleClass().add("fo-card-game-badge");

        Label cardTitle = new Label(t.getNom());
        cardTitle.getStyleClass().add("fo-card-title");
        cardTitle.setWrapText(true);

        Label statusLbl = new Label(status);
        statusLbl.getStyleClass().add(statusClass);

        HBox badgeRow = new HBox(8, gameBadge, statusLbl);
        badgeRow.setAlignment(Pos.CENTER_LEFT);

        VBox banner = new VBox(8, badgeRow, cardTitle);
        banner.getStyleClass().addAll("fo-card-banner", BANNER_CLASSES[colorIdx]);
        banner.setAlignment(Pos.BOTTOM_LEFT);

        VBox body = new VBox(8);
        body.getStyleClass().add("fo-card-body");
        body.getChildren().addAll(
                infoRow("📍", t.getLieu()),
                infoRow("🏢", centreName),
                infoRow("📅", t.getDateDebut().format(DATE_FMT) + " → " + t.getDateFin().format(DATE_FMT))
        );

        Label prize = new Label(String.format("💰 %.0f €", t.getPrix()));
        prize.getStyleClass().add("fo-card-prize");

        Label teams = new Label("👥 " + t.getNbEquipes() + " équipes");
        teams.getStyleClass().add("fo-card-teams");

        HBox badges = new HBox(10, prize, teams);
        badges.setAlignment(Pos.CENTER_LEFT);
        body.getChildren().add(badges);

        Separator sep = new Separator();
        sep.getStyleClass().add("fo-card-divider");

        Button btnDet = new Button("👁  Voir les détails");
        btnDet.getStyleClass().add("fo-btn-details");
        btnDet.setMaxWidth(Double.MAX_VALUE);
        btnDet.setOnAction(e -> showDetails(t, centreName));

        VBox footer = new VBox(btnDet);
        footer.getStyleClass().add("fo-card-footer");

        VBox card = new VBox(banner, body, sep, footer);
        card.getStyleClass().add("fo-card");
        return card;
    }

    private HBox infoRow(String icon, String text) {
        Label ic = new Label(icon);
        ic.getStyleClass().add("fo-card-info-icon");

        Label tx = new Label(text);
        tx.getStyleClass().add("fo-card-info-text");
        tx.setWrapText(true);

        HBox row = new HBox(8, ic, tx);
        row.getStyleClass().add("fo-card-info-row");
        return row;
    }

    private void updateStats(List<Tournoi> list) {
        LocalDate today = LocalDate.now();
        long active = list.stream()
                .filter(t -> !t.getDateDebut().isAfter(today) && !t.getDateFin().isBefore(today))
                .count();
        long upcoming = list.stream()
                .filter(t -> t.getDateDebut().isAfter(today))
                .count();

        lblTotalCount.setText(String.valueOf(list.size()));
        lblActiveCount.setText(String.valueOf(active));
        lblUpcomingCount.setText(String.valueOf(upcoming));
    }

    private void showDetails(Tournoi t, String centreName) {
        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle("Détails du Tournoi");
        dlg.setHeaderText(null);

        VBox c = new VBox(12);
        c.setPadding(new Insets(24));
        c.setStyle("-fx-background-color:#181C28;");
        c.setPrefWidth(400);

        Label title = new Label("⚔  " + t.getNom());
        title.setStyle("-fx-text-fill:#FFF;-fx-font-size:18px;-fx-font-weight:700;");

        Label badge = new Label("🎮  " + t.getJeu());
        badge.setStyle("-fx-text-fill:#6FA3FF;-fx-background-color:rgba(89,126,232,0.15);"
                + "-fx-padding:4 12;-fx-background-radius:20;-fx-font-size:12px;");

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color:rgba(255,255,255,0.06);");

        c.getChildren().addAll(
                title,
                badge,
                sep,
                dRow("🏢 Centre", centreName),
                dRow("📅 Début", t.getDateDebut().format(DATE_FMT)),
                dRow("📅 Fin", t.getDateFin().format(DATE_FMT)),
                dRow("📍 Lieu", t.getLieu()),
                dRow("💰 Prix", String.format("%.2f €", t.getPrix())),
                dRow("👥 Équipes", t.getNbEquipes() + " équipes")
        );

        Label weatherTitle = new Label("☁  Météo");
        weatherTitle.setStyle("-fx-text-fill:#FFF;-fx-font-size:14px;-fx-font-weight:700;");
        Label weatherLabel = new Label("Chargement de la météo...");
        weatherLabel.setWrapText(true);
        weatherLabel.setStyle("-fx-text-fill:#8A93A5;-fx-font-size:13px;");
        c.getChildren().addAll(new Separator(), weatherTitle, weatherLabel);
        loadWeatherAsync(t, weatherLabel);

        dlg.getDialogPane().setContent(c);
        dlg.getDialogPane().setStyle("-fx-background-color:#181C28;-fx-padding:0;");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);

        LocalDate today = LocalDate.now();
        boolean isActive = !t.getDateDebut().isAfter(today) && !t.getDateFin().isBefore(today);
        boolean isFinished = t.getDateFin().isBefore(today);

        if (isActive || isFinished) {
            Button bracketBtn = new Button("📊 Voir Bracket");
            bracketBtn.setStyle("-fx-background-color:linear-gradient(to right,#FF6B35,#FF8A5C);"
                    + "-fx-text-fill:white;-fx-font-weight:bold;-fx-background-radius:10;-fx-padding:9 24;");
            bracketBtn.setOnAction(e -> {
                dlg.close();
                handleViewBracket(t);
            });

            Button leaderboardBtn = new Button("🏆 Voir Classements");
            leaderboardBtn.setStyle("-fx-background-color:linear-gradient(to right,#4CAF50,#66BB6A);"
                    + "-fx-text-fill:white;-fx-font-weight:bold;-fx-background-radius:10;-fx-padding:9 24;");
            leaderboardBtn.setOnAction(e -> {
                dlg.close();
                handleViewLeaderboard(t);
            });

            HBox buttons = new HBox(10, bracketBtn, leaderboardBtn);
            buttons.setAlignment(Pos.CENTER);
            c.getChildren().add(buttons);
        }

        Button participateBtn = new Button("🏆 Participer");
        participateBtn.setStyle("-fx-background-color:linear-gradient(to right,#4F6FDB,#597EE8);"
                + "-fx-text-fill:white;-fx-font-weight:bold;-fx-background-radius:10;-fx-padding:9 24;");
        participateBtn.setOnAction(e -> {
            dlg.close();
            handleParticipate(t);
        });

        Button closeBtn = new Button("✖ Fermer");
        closeBtn.setStyle("-fx-background-color:rgba(255,255,255,0.05);"
                + "-fx-text-fill:#8A93A5;-fx-font-size:13px;-fx-background-radius:10;-fx-padding:9 24;");
        closeBtn.setOnAction(e -> dlg.close());

        HBox bottomButtons = new HBox(10, participateBtn, closeBtn);
        bottomButtons.setAlignment(Pos.CENTER);
        c.getChildren().add(bottomButtons);

        dlg.showAndWait();
    }

    private HBox dRow(String label, String value) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill:#8A93A5;-fx-font-size:12px;-fx-min-width:110;");

        Label val = new Label(value);
        val.setStyle("-fx-text-fill:#FFF;-fx-font-size:13px;-fx-font-weight:600;");

        HBox r = new HBox(10, lbl, val);
        r.setAlignment(Pos.CENTER_LEFT);
        return r;
    }
        private void loadWeatherAsync(Tournoi tournoi, Label weatherLabel) {
            Thread weatherThread = new Thread(() -> {
                String weatherText;
                try {
                    weatherText = weatherService.getWeatherSummary(tournoi);
                } catch (Exception e) {
                    weatherText = "Météo indisponible pour le moment.";
                }

                String finalWeatherText = weatherText;
                Platform.runLater(() -> weatherLabel.setText(finalWeatherText));
            });
            weatherThread.setDaemon(true);
            weatherThread.start();
        }

    private void handleParticipate(Tournoi t) {
        currentTournament = t;
        try {
            MainApp.navigateTo("/view/ParticipationView.fxml", "🏆  Participation - " + t.getNom());
        } catch (Exception e) {
            showError("Navigation", e.getMessage());
        }
    }

    @FXML
    private void handleNavCentres() {
        try {
            MainApp.navigateTo("/view/FrontOfficeCentreView.fxml", "🏢  Front Office - Centres");
        } catch (Exception e) {
            showError("Navigation", e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        //SessionManager.logout();
        try {
            MainApp.navigateTo("/view/login.fxml", "⚔  TournoiManager - Connexion");
        } catch (Exception e) {
            showError("Logout", e.getMessage());
        }
    }

    private void showError(String h, String m) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erreur");
        a.setHeaderText(h);
        a.setContentText(m != null && !m.isBlank() ? m : "Une erreur est survenue. Consultez la console pour plus de détails.");
        a.showAndWait();
    }

    public static Tournoi getCurrentTournament() {
        return currentTournament;
    }

    private void handleViewBracket(Tournoi t) {
        currentTournament = t;

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/BracketView.fxml")
            );

            Parent bracketView = loader.load();

            // IMPORTANT: get MainLayout controller (parent window)
            Stage stage = (Stage) cardsPane.getScene().getWindow();

            // find root (MainLayout must already be active)
            Scene scene = stage.getScene();

            BorderPane root = (BorderPane) scene.getRoot();

            // inject into center (sidebar stays!)
            root.setCenter(bracketView);

            stage.setTitle("📊 Bracket - " + t.getNom());

        } catch (Exception e) {
            showError("Navigation", e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleViewLeaderboard(Tournoi t) {
        currentTournament = t;

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/LeaderboardView.fxml")
            );

            Parent bracketView = loader.load();

            // IMPORTANT: get MainLayout controller (parent window)
            Stage stage = (Stage) cardsPane.getScene().getWindow();

            // find root (MainLayout must already be active)
            Scene scene = stage.getScene();

            BorderPane root = (BorderPane) scene.getRoot();

            // inject into center (sidebar stays!)
            root.setCenter(bracketView);

            stage.setTitle("📊 Bracket - " + t.getNom());

        } catch (Exception e) {
            showError("Navigation", e.getMessage());
            e.printStackTrace();
        }
    }
}
