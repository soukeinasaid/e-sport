package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.MainFX;
import org.example.entities.Centre;
import org.example.entities.Tournoi;
import org.example.services.CentreService;
import org.example.services.TournoiService;
import org.example.utils.SessionManager;

import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class FrontOfficeCentreController implements Initializable {

    @FXML private Label     lblWelcome;
    @FXML private Label     lblAvatarChar;
    @FXML private Label     lblStatus;
    @FXML private Label     lblTotalCentres;
    @FXML private Label     lblTotalCities;
    @FXML private TextField tfSearch;
    @FXML private FlowPane  cardsPane;

    private final CentreService  centreService  = new CentreService();
    private final TournoiService tournoiService = new TournoiService();
    private List<Centre>  allCentres;
    private List<Tournoi> allTournois;

    private static final String[] BANNER_CLASSES =
        {"fo-card-banner-2","fo-card-banner-3","fo-card-banner-4",
         "fo-card-banner-5","fo-card-banner-1","fo-card-banner-6"};

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (SessionManager.getCurrentUser() != null) {
            String name = SessionManager.getCurrentUser().getUsername();
            lblWelcome.setText(name);
            lblAvatarChar.setText(String.valueOf(name.charAt(0)).toUpperCase());
        }
        loadData();
        tfSearch.textProperty().addListener((obs, o, nv) -> applyFilter());
    }

    private void loadData() {
        try {
            allCentres  = centreService.listerTous();
            allTournois = tournoiService.listerTous();
            renderCards(allCentres);
            updateStats(allCentres);
        } catch (SQLException e) { showError("Chargement", e.getMessage()); }
    }

    private void applyFilter() {
        if (allCentres == null) return;
        String q = tfSearch.getText().toLowerCase().trim();
        List<Centre> filtered = allCentres.stream()
            .filter(c -> q.isEmpty()
                    || c.getName().toLowerCase().contains(q)
                    || c.getCity().toLowerCase().contains(q)
                    || (c.getAddress() != null && c.getAddress().toLowerCase().contains(q)))
            .collect(Collectors.toList());
        renderCards(filtered);
        updateStats(filtered);
    }

    private void renderCards(List<Centre> list) {
        cardsPane.getChildren().clear();
        if (list.isEmpty()) {
            VBox empty = new VBox(12);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(70, 0, 0, 0));
            empty.setPrefWidth(cardsPane.getPrefWrapLength());
            Label icon = new Label("🏢"); icon.setStyle("-fx-font-size:56px;-fx-text-fill:#2A2F3F;");
            Label msg  = new Label("Aucun centre trouvé"); msg.setStyle("-fx-text-fill:#5A6480;-fx-font-size:16px;-fx-font-weight:700;");
            empty.getChildren().addAll(icon, msg);
            cardsPane.getChildren().add(empty);
            return;
        }
        int i = 0;
        for (Centre c : list)
            cardsPane.getChildren().add(buildCard(c, i++ % BANNER_CLASSES.length));
    }

    private VBox buildCard(Centre centre, int colorIdx) {
        // Count tournaments for this centre
        long tournoiCount = allTournois == null ? 0 :
            allTournois.stream().filter(t -> t.getCentreId() == centre.getId()).count();

        // ── Banner ──
        Label cityBadge = new Label("📍  " + centre.getCity().toUpperCase());
        cityBadge.getStyleClass().add("fo-card-game-badge");

        Label nameLbl = new Label("🏢  " + centre.getName());
        nameLbl.getStyleClass().add("fo-card-title");
        nameLbl.setWrapText(true);

        VBox banner = new VBox(8, cityBadge, nameLbl);
        banner.getStyleClass().addAll("fo-card-banner", BANNER_CLASSES[colorIdx]);
        banner.setAlignment(Pos.BOTTOM_LEFT);

        // ── Body ──
        VBox body = new VBox(10);
        body.getStyleClass().add("fo-card-body");

        if (centre.getAddress() != null && !centre.getAddress().isEmpty())
            body.getChildren().add(infoRow("🏠", centre.getAddress()));
        if (centre.getContactEmail() != null && !centre.getContactEmail().isEmpty())
            body.getChildren().add(infoRow("📧", centre.getContactEmail()));

        // Tournois badge + Map badge
        Label tBadge = new Label("🏆  " + tournoiCount + " tournoi" + (tournoiCount != 1 ? "s" : ""));
        tBadge.getStyleClass().add("fo-card-teams");

        HBox badges = new HBox(10, tBadge);
        badges.setAlignment(Pos.CENTER_LEFT);

        // Map URL button
        if (centre.getMapUrl() != null && !centre.getMapUrl().isEmpty()) {
            Label mapBtn = new Label("🗺  Voir sur la carte");
            mapBtn.setStyle(
                "-fx-text-fill:#00FFD1;-fx-background-color:rgba(0,255,209,0.1);" +
                "-fx-border-color:rgba(0,255,209,0.25);-fx-border-radius:10;" +
                "-fx-background-radius:10;-fx-padding:4 12;-fx-font-size:11px;" +
                "-fx-font-weight:700;-fx-cursor:hand;");
            mapBtn.setOnMouseClicked(e -> {
                try { java.awt.Desktop.getDesktop().browse(new java.net.URI(centre.getMapUrl())); }
                catch (Exception ex) { /* ignore */ }
            });
            badges.getChildren().add(mapBtn);
        }

        body.getChildren().add(badges);

        // ── Separator ──
        Separator sep = new Separator();
        sep.getStyleClass().add("fo-card-divider");

        // ── Footer ──
        Button btnDetails = new Button("👁  Voir les tournois");
        btnDetails.getStyleClass().add("fo-btn-details");
        btnDetails.setMaxWidth(Double.MAX_VALUE);
        btnDetails.setOnAction(e -> showTournoisForCentre(centre));

        VBox footer = new VBox(btnDetails);
        footer.getStyleClass().add("fo-card-footer");

        // ── Assemble card ──
        VBox card = new VBox(banner, body, sep, footer);
        card.getStyleClass().add("fo-card");
        return card;
    }

    private HBox infoRow(String icon, String text) {
        Label ic = new Label(icon); ic.getStyleClass().add("fo-card-info-icon");
        Label tx = new Label(text); tx.getStyleClass().add("fo-card-info-text"); tx.setWrapText(true);
        HBox row = new HBox(8, ic, tx); row.getStyleClass().add("fo-card-info-row"); return row;
    }

    private void updateStats(List<Centre> list) {
        long cities = list.stream().map(Centre::getCity).distinct().count();
        lblTotalCentres.setText(String.valueOf(list.size()));
        lblTotalCities .setText(String.valueOf(cities));
    }

    // Show all tournaments for the selected centre
    private void showTournoisForCentre(Centre centre) {
        if (allTournois == null) return;
        List<Tournoi> linked = allTournois.stream()
            .filter(t -> t.getCentreId() == centre.getId())
            .collect(Collectors.toList());

        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle("Tournois — " + centre.getName()); dlg.setHeaderText(null);

        VBox root = new VBox(12);
        root.setPadding(new Insets(24)); root.setStyle("-fx-background-color:#181C28;"); root.setPrefWidth(480);

        Label title = new Label("🏢  " + centre.getName());
        title.setStyle("-fx-text-fill:#FFF;-fx-font-size:17px;-fx-font-weight:700;");
        Label sub = new Label("📍  " + centre.getCity());
        sub.setStyle("-fx-text-fill:#8A93A5;-fx-font-size:12px;");
        Separator sep = new Separator(); sep.setStyle("-fx-background-color:rgba(255,255,255,0.06);");

        root.getChildren().addAll(title, sub, sep);

        if (linked.isEmpty()) {
            Label none = new Label("Aucun tournoi n'est associé à ce centre.");
            none.setStyle("-fx-text-fill:#5A6480;-fx-font-size:13px;-fx-padding:10 0 0 0;");
            root.getChildren().add(none);
        } else {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            for (Tournoi t : linked) {
                HBox row = new HBox(12);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle(
                    "-fx-background-color:rgba(89,126,232,0.08);-fx-background-radius:10;" +
                    "-fx-border-color:rgba(89,126,232,0.15);-fx-border-radius:10;-fx-padding:10 14;");

                VBox info = new VBox(4);
                Label nom = new Label("⚔  " + t.getNom());
                nom.setStyle("-fx-text-fill:#FFF;-fx-font-size:13px;-fx-font-weight:700;");
                Label dates = new Label("📅  " + t.getDateDebut().format(fmt) + " → " + t.getDateFin().format(fmt)
                        + "   💰  " + String.format("%.0f €", t.getPrix()));
                dates.setStyle("-fx-text-fill:#8A93A5;-fx-font-size:11px;");
                info.getChildren().addAll(nom, dates);

                Label gameBadge = new Label(t.getJeu());
                gameBadge.setStyle(
                    "-fx-text-fill:#6FA3FF;-fx-background-color:rgba(89,126,232,0.18);" +
                    "-fx-padding:3 10;-fx-background-radius:20;-fx-font-size:10px;-fx-font-weight:700;");
                Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
                row.getChildren().addAll(info, spacer, gameBadge);
                root.getChildren().add(row);
            }
        }

        dlg.getDialogPane().setContent(root);
        dlg.getDialogPane().setStyle("-fx-background-color:#181C28;-fx-padding:0;");
        dlg.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        Button closeBtn = (Button) dlg.getDialogPane().lookupButton(ButtonType.CLOSE);
        closeBtn.setStyle("-fx-background-color:linear-gradient(to right,#4F6FDB,#597EE8);" +
                          "-fx-text-fill:white;-fx-font-weight:bold;-fx-background-radius:10;-fx-padding:9 24;");
        dlg.showAndWait();
    }

    // ── Navigation ────────────────────────────────────────
    @FXML
    private void handleNavTournois() {
        try { MainFX.navigateTo("FrontOfficeView.fxml", "🏆  Front Office — Tournois"); }
        catch (Exception e) { showError("Navigation", e.getMessage()); }
    }

    @FXML
    private void handleLogout() {
        SessionManager.logout();
        try { MainFX.navigateTo("LoginView.fxml", "⚔  TournoiManager — Connexion"); }
        catch (Exception e) { showError("Logout", e.getMessage()); }
    }

    private void showError(String h, String m) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erreur"); a.setHeaderText(h); a.setContentText(m); a.showAndWait();
    }
}
