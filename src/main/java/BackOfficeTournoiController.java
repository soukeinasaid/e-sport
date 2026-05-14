import entity.Centre;
import entity.Tournoi;
import entity.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import service.CentreService;
import service.TournoiService;
import utilies.Session;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class BackOfficeTournoiController implements Initializable {

    @FXML private Label lblTotalCentres;
    @FXML private Label lblTotalCities;
    @FXML private Label lblStatus;
    @FXML private TextField tfSearch;
    @FXML private FlowPane cardsPane;
    @FXML private Button btnAddTournament;

    private final CentreService centreService = new CentreService();
    private final TournoiService tournoiService = new TournoiService();
    private List<Centre> allCentres;
    private List<Tournoi> allTournois;

    private static final String[] BANNER_CLASSES = {
            "fo-card-banner-1", "fo-card-banner-2", "fo-card-banner-3",
            "fo-card-banner-4", "fo-card-banner-5", "fo-card-banner-6"
    };

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Set status based on user role
        User currentUser = Session.getUser();
        if (currentUser != null) {
            lblStatus.setText(currentUser.isAdmin() ? "🛡️ Admin Mode" : "👤 User Mode");

            // Show admin button only for admins
            if (currentUser.isAdmin()) {
                btnAddTournament.setVisible(true);
                btnAddTournament.setManaged(true);
            }
        }

        loadData();
        setupSearch();
    }

    private void setupSearch() {
        tfSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());
    }

    private void loadData() {
        try {
            allCentres = centreService.listerTous();
            allTournois = tournoiService.listerTous();
            renderCards(allCentres);
            updateStats(allCentres);
        } catch (SQLException e) {
            showError("Chargement", "Impossible de charger les données: " + e.getMessage());
        }
    }

    private void applyFilter() {
        if (allCentres == null) return;
        String query = tfSearch.getText().toLowerCase().trim();

        List<Centre> filtered = allCentres.stream()
                .filter(c -> query.isEmpty() ||
                        c.getName().toLowerCase().contains(query) ||
                        c.getCity().toLowerCase().contains(query) ||
                        (c.getAddress() != null && c.getAddress().toLowerCase().contains(query)))
                .collect(Collectors.toList());

        renderCards(filtered);
        updateStats(filtered);
    }

    private void renderCards(List<Centre> centres) {
        cardsPane.getChildren().clear();

        if (centres.isEmpty()) {
            showEmptyState();
            return;
        }

        int colorIndex = 0;
        for (Centre centre : centres) {
            cardsPane.getChildren().add(buildCentreCard(centre, BANNER_CLASSES[colorIndex % BANNER_CLASSES.length]));
            colorIndex++;
        }
    }

    private VBox buildCentreCard(Centre centre, String bannerClass) {
        // Count tournaments for this centre
        long tournamentCount = allTournois.stream()
                .filter(t -> t.getCentreId() == centre.getId())
                .count();

        // Banner section
        Label cityBadge = new Label("📍 " + centre.getCity().toUpperCase());
        cityBadge.getStyleClass().add("fo-card-game-badge");

        Label nameLabel = new Label(centre.getName());
        nameLabel.getStyleClass().add("fo-card-title");
        nameLabel.setWrapText(true);

        VBox banner = new VBox(8, cityBadge, nameLabel);
        banner.getStyleClass().addAll("fo-card-banner", bannerClass);
        banner.setAlignment(Pos.BOTTOM_LEFT);

        // Body section
        VBox body = new VBox(8);
        body.getStyleClass().add("fo-card-body");

        if (centre.getAddress() != null && !centre.getAddress().isEmpty()) {
            body.getChildren().add(infoRow("📍", centre.getAddress()));
        }

        if (centre.getContactEmail() != null && !centre.getContactEmail().isEmpty()) {
            body.getChildren().add(infoRow("📧", centre.getContactEmail()));
        }

        // Tournament count badge
        Label tournamentBadge = new Label("🏆 " + tournamentCount + " tournoi" + (tournamentCount != 1 ? "s" : ""));
        tournamentBadge.getStyleClass().add("fo-card-teams");

        HBox badges = new HBox(10, tournamentBadge);
        badges.setAlignment(Pos.CENTER_LEFT);

        // Map button if available
        if (centre.getMapUrl() != null && !centre.getMapUrl().isEmpty()) {
            Button mapButton = new Button("🗺 Voir carte");
            mapButton.getStyleClass().add("fo-btn-map");
            mapButton.setOnAction(e -> openMap(centre.getMapUrl()));
            badges.getChildren().add(mapButton);
        }

        body.getChildren().add(badges);

        // Separator
        Separator separator = new Separator();
        separator.getStyleClass().add("fo-card-divider");

        // Footer with action buttons
        VBox footer = new VBox(8);
        footer.getStyleClass().add("fo-card-footer");

        Button viewTournamentsBtn = new Button("👁 Voir les tournois");
        viewTournamentsBtn.getStyleClass().add("fo-btn-details");
        viewTournamentsBtn.setMaxWidth(Double.MAX_VALUE);
        viewTournamentsBtn.setOnAction(e -> showTournamentsDialog(centre));
        footer.getChildren().add(viewTournamentsBtn);

        // Admin actions (only for admin users)
        User currentUser = Session.getUser();
        if (currentUser != null && currentUser.isAdmin()) {
            HBox adminActions = new HBox(8);
            adminActions.setAlignment(Pos.CENTER);

            Button editBtn = new Button("✏️ Modifier");
            editBtn.getStyleClass().add("fo-btn-edit");
            editBtn.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(editBtn, Priority.ALWAYS);
            editBtn.setOnAction(e -> showAddEditTournamentDialog(centre, null));

            Button addTournamentBtn = new Button("➕ Ajouter tournoi");
            addTournamentBtn.getStyleClass().add("fo-btn-admin");
            addTournamentBtn.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(addTournamentBtn, Priority.ALWAYS);
            addTournamentBtn.setOnAction(e -> showAddEditTournamentDialog(centre, null));

            adminActions.getChildren().addAll(editBtn, addTournamentBtn);
            footer.getChildren().add(adminActions);
        }

        VBox card = new VBox(banner, body, separator, footer);
        card.getStyleClass().add("fo-card");
        return card;
    }

    private HBox infoRow(String icon, String text) {
        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("fo-card-info-icon");

        Label textLabel = new Label(text);
        textLabel.getStyleClass().add("fo-card-info-text");
        textLabel.setWrapText(true);

        HBox row = new HBox(8, iconLabel, textLabel);
        row.getStyleClass().add("fo-card-info-row");
        return row;
    }

    private void showEmptyState() {
        VBox emptyState = new VBox(12);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(70, 0, 0, 0));
        emptyState.setPrefWidth(cardsPane.getPrefWrapLength());

        Label icon = new Label("🏢");
        icon.setStyle("-fx-font-size:56px;-fx-text-fill:#2A2F3F;");

        Label message = new Label("Aucun centre trouvé");
        message.setStyle("-fx-text-fill:#5A6480;-fx-font-size:16px;-fx-font-weight:700;");

        emptyState.getChildren().addAll(icon, message);
        cardsPane.getChildren().add(emptyState);
    }

    private void updateStats(List<Centre> centres) {
        long uniqueCities = centres.stream().map(Centre::getCity).distinct().count();
        lblTotalCentres.setText(String.valueOf(centres.size()));
        lblTotalCities.setText(String.valueOf(uniqueCities));
    }

    private void showTournamentsDialog(Centre centre) {
        List<Tournoi> centreTournaments = allTournois.stream()
                .filter(t -> t.getCentreId() == centre.getId())
                .collect(Collectors.toList());

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Tournois - " + centre.getName());
        dialog.setHeaderText(null);

        VBox content = new VBox(12);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color:#181C28;");
        content.setPrefWidth(500);

        Label title = new Label("🏢 " + centre.getName());
        title.setStyle("-fx-text-fill:#FFF;-fx-font-size:18px;-fx-font-weight:700;");

        Label subtitle = new Label("📍 " + centre.getCity());
        subtitle.setStyle("-fx-text-fill:#8A93A5;-fx-font-size:13px;");

        Separator separator = new Separator();
        separator.setStyle("-fx-background-color:rgba(255,255,255,0.06);");

        content.getChildren().addAll(title, subtitle, separator);

        if (centreTournaments.isEmpty()) {
            Label noTournaments = new Label("Aucun tournoi organisé dans ce centre.");
            noTournaments.setStyle("-fx-text-fill:#5A6480;-fx-font-size:13px;-fx-padding:20 0;");
            content.getChildren().add(noTournaments);
        } else {
            for (Tournoi tournament : centreTournaments) {
                VBox tournamentCard = new VBox(6);
                tournamentCard.setStyle(
                        "-fx-background-color:rgba(89,126,232,0.08);" +
                                "-fx-background-radius:10;" +
                                "-fx-padding:12;" +
                                "-fx-border-color:rgba(89,126,232,0.15);" +
                                "-fx-border-radius:10;"
                );

                Label name = new Label("⚔ " + tournament.getNom());
                name.setStyle("-fx-text-fill:#FFF;-fx-font-size:14px;-fx-font-weight:700;");

                Label details = new Label(
                        "📅 " + tournament.getDateDebut().format(DATE_FMT) + " → " + tournament.getDateFin().format(DATE_FMT) +
                                " | 💰 " + String.format("%.0f €", tournament.getPrix()) +
                                " | 👥 " + tournament.getNbEquipes() + " équipes"
                );
                details.setStyle("-fx-text-fill:#8A93A5;-fx-font-size:11px;");

                Label gameBadge = new Label(tournament.getJeu());
                gameBadge.setStyle(
                        "-fx-text-fill:#6FA3FF;" +
                                "-fx-background-color:rgba(89,126,232,0.18);" +
                                "-fx-padding:3 10;" +
                                "-fx-background-radius:20;" +
                                "-fx-font-size:11px;" +
                                "-fx-font-weight:700;"
                );

                tournamentCard.getChildren().addAll(name, details, gameBadge);
                content.getChildren().add(tournamentCard);
            }
        }

        // Admin actions in dialog
        User currentUser = Session.getUser();
        if (currentUser != null && currentUser.isAdmin() && !centreTournaments.isEmpty()) {
            Separator adminSep = new Separator();
            adminSep.setStyle("-fx-background-color:rgba(255,255,255,0.06);-fx-padding:10 0;");
            content.getChildren().add(adminSep);

            Label adminLabel = new Label("Actions administrateur:");
            adminLabel.setStyle("-fx-text-fill:#FFF;-fx-font-size:13px;-fx-font-weight:700;");
            content.getChildren().add(adminLabel);

            for (Tournoi tournament : centreTournaments) {
                HBox actionRow = new HBox(10);
                actionRow.setAlignment(Pos.CENTER_LEFT);
                actionRow.setPadding(new Insets(5, 0, 5, 0));

                Label tournamentName = new Label(tournament.getNom());
                tournamentName.setStyle("-fx-text-fill:#FFF;-fx-font-size:13px;");
                HBox.setHgrow(tournamentName, Priority.ALWAYS);

                Button editBtn = new Button("✏️ Modifier");
                editBtn.getStyleClass().add("fo-btn-edit");
                editBtn.setOnAction(e -> {
                    dialog.close();
                    showAddEditTournamentDialog(centre, tournament);
                });

                Button deleteBtn = new Button("🗑️ Supprimer");
                deleteBtn.getStyleClass().add("fo-btn-delete");
                deleteBtn.setOnAction(e -> {
                    dialog.close();
                    deleteTournament(tournament);
                });

                actionRow.getChildren().addAll(tournamentName, editBtn, deleteBtn);
                content.getChildren().add(actionRow);
            }
        }

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setStyle("-fx-background-color:#181C28;-fx-padding:0;");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        Button closeBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.CLOSE);
        closeBtn.setStyle("-fx-background-color:linear-gradient(to right,#4F6FDB,#597EE8);" +
                "-fx-text-fill:white;-fx-font-weight:bold;-fx-background-radius:10;-fx-padding:9 24;");

        dialog.showAndWait();
    }

    @FXML
    private void handleAddTournament(ActionEvent event) {
        // Show centre selection first, then add tournament
        if (allCentres == null || allCentres.isEmpty()) {
            showError("Erreur", "Aucun centre disponible. Veuillez d'abord créer un centre.");
            return;
        }

        ChoiceDialog<Centre> centreDialog = new ChoiceDialog<>(allCentres.get(0), allCentres);
        centreDialog.setTitle("Sélectionner un centre");
        centreDialog.setHeaderText("Choisissez le centre pour ce tournoi");
        centreDialog.setContentText("Centre:");

        Optional<Centre> result = centreDialog.showAndWait();
        result.ifPresent(centre -> showAddEditTournamentDialog(centre, null));
    }

    private void showAddEditTournamentDialog(Centre centre, Tournoi existingTournament) {
        boolean isEditing = existingTournament != null;
        Dialog<Tournoi> dialog = new Dialog<>();
        dialog.setTitle(isEditing ? "Modifier le tournoi" : "Ajouter un tournoi");
        dialog.setHeaderText(isEditing ? "Modifier: " + existingTournament.getNom() : "Nouveau tournoi pour: " + centre.getName());

        VBox content = new VBox(12);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color:#181C28;");
        content.setPrefWidth(400);

        // Form fields
        TextField nomField = new TextField();
        nomField.setPromptText("Nom du tournoi");
        if (isEditing) nomField.setText(existingTournament.getNom());

        TextField jeuField = new TextField();
        jeuField.setPromptText("Jeu (ex: League of Legends)");
        if (isEditing) jeuField.setText(existingTournament.getJeu());

        DatePicker dateDebutPicker = new DatePicker();
        dateDebutPicker.setPromptText("Date de début");
        if (isEditing) dateDebutPicker.setValue(existingTournament.getDateDebut());

        DatePicker dateFinPicker = new DatePicker();
        dateFinPicker.setPromptText("Date de fin");
        if (isEditing) dateFinPicker.setValue(existingTournament.getDateFin());

        TextField lieuField = new TextField();
        lieuField.setPromptText("Lieu");
        if (isEditing) lieuField.setText(existingTournament.getLieu());

        TextField prixField = new TextField();
        prixField.setPromptText("Prix (€)");
        if (isEditing) prixField.setText(String.valueOf(existingTournament.getPrix()));

        TextField nbEquipesField = new TextField();
        nbEquipesField.setPromptText("Nombre d'équipes");
        if (isEditing) nbEquipesField.setText(String.valueOf(existingTournament.getNbEquipes()));

        content.getChildren().addAll(
                createFormField("Nom:", nomField),
                createFormField("Jeu:", jeuField),
                createFormField("Date de début:", dateDebutPicker),
                createFormField("Date de fin:", dateFinPicker),
                createFormField("Lieu:", lieuField),
                createFormField("Prix (€):", prixField),
                createFormField("Nombre d'équipes:", nbEquipesField)
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.setStyle("-fx-background-color:linear-gradient(to right,#4F6FDB,#597EE8);-fx-text-fill:white;");

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                try {
                    String nom = nomField.getText();
                    String jeu = jeuField.getText();
                    LocalDate dateDebut = dateDebutPicker.getValue();
                    LocalDate dateFin = dateFinPicker.getValue();
                    String lieu = lieuField.getText();
                    double prix = Double.parseDouble(prixField.getText());
                    int nbEquipes = Integer.parseInt(nbEquipesField.getText());

                    Tournoi tournoi = new Tournoi(nom, jeu, dateDebut, dateFin, lieu, prix, nbEquipes, centre.getId());
                    if (isEditing) {
                        tournoi.setId(existingTournament.getId());
                    }
                    return tournoi;
                } catch (Exception e) {
                    showError("Erreur", "Veuillez remplir tous les champs correctement.");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(tournoi -> {
            try {
                if (isEditing) {
                    tournoiService.modifier(tournoi);
                    showInfo("Succès", "Tournoi modifié avec succès!");
                } else {
                    tournoiService.ajouter(tournoi);
                    showInfo("Succès", "Tournoi ajouté avec succès!");
                }
                loadData(); // Refresh
            } catch (SQLException e) {
                showError("Erreur", "Impossible de sauvegarder le tournoi: " + e.getMessage());
            }
        });
    }

    private VBox createFormField(String labelText, Control field) {
        VBox container = new VBox(4);
        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill:#8A93A5;-fx-font-size:12px;-fx-font-weight:600;");
        container.getChildren().addAll(label, field);
        return container;
    }

    private void deleteTournament(Tournoi tournament) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("Supprimer le tournoi");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer le tournoi \"" + tournament.getNom() + "\" ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    tournoiService.supprimer(tournament.getId());
                    loadData(); // Refresh
                    showInfo("Succès", "Tournoi supprimé avec succès!");
                } catch (SQLException e) {
                    showError("Erreur", "Impossible de supprimer le tournoi: " + e.getMessage());
                }
            }
        });
    }

    private void openMap(String url) {
        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
        } catch (Exception e) {
            showError("Erreur", "Impossible d'ouvrir la carte: " + e.getMessage());
        }
    }

    private void showInfo(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}