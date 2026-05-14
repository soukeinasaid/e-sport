package controller;

import com.sun.tools.javac.Main;
import entity.Centre;
import entity.Tournoi;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import service.CentreService;
import service.TournoiService;
import utilies.MainApp;


import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class TournoiController implements Initializable {

    // ── Table ────────────────────────────────────────────
    @FXML private TableView<Tournoi>              tableView;
    @FXML private TableColumn<Tournoi, Integer>   colId;
    @FXML private TableColumn<Tournoi, String>    colNom;
    @FXML private TableColumn<Tournoi, String>    colJeu;
    @FXML private TableColumn<Tournoi, Integer>   colCentre;
    @FXML private TableColumn<Tournoi, LocalDate> colDateDebut;
    @FXML private TableColumn<Tournoi, LocalDate> colDateFin;
    @FXML private TableColumn<Tournoi, String>    colLieu;
    @FXML private TableColumn<Tournoi, Double>    colPrix;
    @FXML private TableColumn<Tournoi, Integer>   colNbEquipes;
    @FXML private TableColumn<Tournoi, Void>      colStatus;
    @FXML private TableColumn<Tournoi, Void>      colActions;

    // ── Toolbar & Filters ─────────────────────────────────
    @FXML private TextField        tfSearch;
    @FXML private ComboBox<String> cbFilterJeu;
    @FXML private ComboBox<String> cbFilterCentre;
    @FXML private ComboBox<String> cbFilterStatus;
    @FXML private ComboBox<String> cbSort;
    @FXML private Label            lblStatus;
    @FXML private Label            lblCount;

    // ── Topbar stats ──────────────────────────────────────
    @FXML private Label lblStatTotal;
    @FXML private Label lblStatActive;
    @FXML private Label lblStatUpcoming;

    // ── State ────────────────────────────────────────────
    private final TournoiService dao       = new TournoiService();
    private final CentreService centreDao = new CentreService();
    private ObservableList<Tournoi> masterList;
    private FilteredList<Tournoi>   filteredList;
    private SortedList<Tournoi>     sortedList;
    private List<Centre>            centreList;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ══════════════════════════════════════════════════════
    //  INITIALIZE
    // ══════════════════════════════════════════════════════
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadCentres();
        setupColumns();
        setupStatusColumn();
        setupActionsColumn();
        setupSearch();
        setupSort();
        loadData();
    }

    private void loadCentres() {
        try { centreList = centreDao.listerTous(); }
        catch (SQLException e) { showError("Erreur centres", e.getMessage()); }
    }

    // ── Table columns ─────────────────────────────────────
    private void setupColumns() {
        colId       .setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom      .setCellValueFactory(new PropertyValueFactory<>("nom"));
        colJeu      .setCellValueFactory(new PropertyValueFactory<>("jeu"));
        colLieu     .setCellValueFactory(new PropertyValueFactory<>("lieu"));
        colNbEquipes.setCellValueFactory(new PropertyValueFactory<>("nbEquipes"));

        // Centre name — reads centreId (Integer) and resolves to display name
        colCentre.setCellValueFactory(new PropertyValueFactory<>("centreId"));
        colCentre.setCellFactory(col -> new TableCell<Tournoi, Integer>() {
            @Override
            protected void updateItem(Integer centreId, boolean empty) {
                super.updateItem(centreId, empty);
                if (empty || centreId == null || centreId <= 0 || centreList == null) {
                    setText("—"); return;
                }
                String found = "—";
                for (Centre c : centreList) {
                    if (c.getId() == centreId) { found = c.getName(); break; }
                }
                setText(found);
            }
        });

        // Prix formatted
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colPrix.setCellFactory(col -> new TableCell<Tournoi, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.0f €", item));
            }
        });

        // Date début
        colDateDebut.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        colDateDebut.setCellFactory(col -> new TableCell<Tournoi, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(DATE_FMT));
            }
        });

        // Date fin
        colDateFin.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        colDateFin.setCellFactory(col -> new TableCell<Tournoi, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(DATE_FMT));
            }
        });
    }

    // ── Actions column ────────────────────────────────────
    private void setupActionsColumn() {
        colActions.setCellFactory(col -> new TableCell<Tournoi, Void>() {
            private final Button btnDetails = new Button("👁");
            private final Button btnEdit    = new Button("✎");
            private final Button btnDelete  = new Button("🗑");
            private final HBox   box        = new HBox(6, btnDetails, btnEdit, btnDelete);

            {
                btnDetails.getStyleClass().add("btn-details");
                btnEdit   .getStyleClass().add("btn-edit");
                btnDelete .getStyleClass().add("btn-delete");
                btnDetails.setTooltip(new Tooltip("Voir les détails"));
                btnEdit   .setTooltip(new Tooltip("Modifier"));
                btnDelete .setTooltip(new Tooltip("Supprimer"));
                box.setAlignment(Pos.CENTER);
                box.setPadding(new Insets(0, 4, 0, 4));

                btnDetails.setOnAction(e -> showDetails(getTableView().getItems().get(getIndex())));
                btnEdit   .setOnAction(e -> handleOpenEditDialog(getTableView().getItems().get(getIndex())));
                btnDelete .setOnAction(e -> confirmAndDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    // ── Status column ─────────────────────────────────────
    private void setupStatusColumn() {
        colStatus.setCellFactory(col -> new TableCell<Tournoi, Void>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) { setGraphic(null); return; }
                Tournoi t = getTableView().getItems().get(getIndex());
                LocalDate today = LocalDate.now();
                javafx.scene.control.Label badge;
                if (t.getDateDebut().isAfter(today)) {
                    badge = new javafx.scene.control.Label("🔵 À venir");
                    badge.getStyleClass().add("badge-upcoming");
                } else if (t.getDateFin().isBefore(today)) {
                    badge = new javafx.scene.control.Label("⚫ Terminé");
                    badge.getStyleClass().add("badge-finished");
                } else {
                    badge = new javafx.scene.control.Label("🟢 En cours");
                    badge.getStyleClass().add("badge-active");
                }
                setGraphic(badge);
            }
        });
    }

    // ── Search & Filters ──────────────────────────────────
    private void setupSearch() {
        tfSearch.textProperty().addListener((obs, o, nv) -> applyFilters());
        cbFilterJeu   .valueProperty().addListener((obs, o, nv) -> applyFilters());
        cbFilterCentre.valueProperty().addListener((obs, o, nv) -> applyFilters());
        cbFilterStatus.valueProperty().addListener((obs, o, nv) -> applyFilters());
    }

    private void populateFilters() {
        cbFilterJeu.getItems().clear();
        cbFilterJeu.getItems().add("Tous les jeux");
        masterList.stream().map(Tournoi::getJeu).distinct().sorted()
                .forEach(j -> cbFilterJeu.getItems().add(j));

        cbFilterCentre.getItems().clear();
        cbFilterCentre.getItems().add("Tous les centres");
        if (centreList != null) centreList.stream().map(Centre::getName).sorted()
                .forEach(n -> cbFilterCentre.getItems().add(n));

        cbFilterStatus.getItems().clear();
        cbFilterStatus.getItems().addAll("Tous statuts", "🟢 En cours", "🔵 À venir", "⚫ Terminé");
    }

    private void applyFilters() {
        if (filteredList == null) return;
        String q      = tfSearch.getText().toLowerCase().trim();
        String jeu    = cbFilterJeu   .getValue();
        String centre = cbFilterCentre.getValue();
        String status = cbFilterStatus.getValue();
        LocalDate today = LocalDate.now();

        filteredList.setPredicate(t -> {
            if (!q.isEmpty() && !t.getNom().toLowerCase().contains(q)
                    && !t.getJeu().toLowerCase().contains(q)
                    && !t.getLieu().toLowerCase().contains(q)) return false;
            if (jeu != null && !jeu.equals("Tous les jeux") && !t.getJeu().equals(jeu)) return false;
            if (centre != null && !centre.equals("Tous les centres")) {
                String cName = centreList == null ? "" : centreList.stream()
                                                         .filter(c -> c.getId() == t.getCentreId()).map(Centre::getName).findFirst().orElse("");
                if (!cName.equals(centre)) return false;
            }
            if (status != null && !status.equals("Tous statuts")) {
                boolean active   = !t.getDateDebut().isAfter(today) && !t.getDateFin().isBefore(today);
                boolean upcoming = t.getDateDebut().isAfter(today);
                boolean finished = t.getDateFin().isBefore(today);
                if (status.contains("cours")  && !active)   return false;
                if (status.contains("venir")  && !upcoming) return false;
                if (status.contains("ermin")  && !finished) return false;
            }
            return true;
        });
        updateCount();
        updateTopbarStats();
        applySort();
    }

    @FXML
    private void handleResetFilters() {
        tfSearch.clear();
        cbFilterJeu   .setValue(null);
        cbFilterCentre.setValue(null);
        cbFilterStatus.setValue(null);
        cbSort.setValue("ID croissant");
    }

    // ══════════════════════════════════════════════════════
    //  NAVIGATION
    // ══════════════════════════════════════════════════════

    @FXML
    private void handleNavCentres() {
        try { MainApp.navigateTo("CentreView.fxml", "🏢  Centre Manager"); }
        catch (Exception e) { showError("Navigation", e.getMessage()); }
    }

    @FXML
    private void handleLogout() {
        //Session.logout();
        try { MainApp.navigateTo("login.fxml", "⚔️  TournoiManager — Connexion"); }
        catch (Exception e) { showError("Logout", e.getMessage()); }
    }

    // ══════════════════════════════════════════════════════
    //  POPUP DIALOG — shared for ADD and EDIT
    // ═══════════════════════════════════════════���══════════

    @FXML
    private void handleOpenAddDialog() {
        showTournoiDialog(null);
    }

    private void handleOpenEditDialog(Tournoi existing) {
        showTournoiDialog(existing);
    }

    /**
     * Opens the Add/Edit popup dialog.
     * @param existing  null → Add mode,  non-null → Edit mode
     */
    private void showTournoiDialog(Tournoi existing) {
        boolean isEdit = (existing != null);

        // ── Form fields ──────────────────────────────────
        TextField        tfNom      = new TextField();
        ComboBox<String> cbJeu      = new ComboBox<>();
        ComboBox<Centre> cbCentre   = new ComboBox<>();
        DatePicker       dpDebut    = new DatePicker();
        DatePicker       dpFin      = new DatePicker();
        TextField        tfLieu     = new TextField();
        TextField        tfPrix     = new TextField();
        Spinner<Integer> spEquipes  = new Spinner<>(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(2, 256, 8));
        spEquipes.setEditable(true);

        // Styles
        String inputStyle =
                "-fx-background-color: #2A3142; -fx-text-fill: #FFF;" +
                        "-fx-prompt-text-fill: #5A6478; -fx-background-radius: 10;" +
                        "-fx-border-radius: 10; -fx-border-color: #3A4155; -fx-padding: 9 12; -fx-font-size: 13px;";
        tfNom  .setStyle(inputStyle); tfNom  .setPromptText("ex: Winter Cup 2025");
        tfLieu .setStyle(inputStyle); tfLieu .setPromptText("ex: Paris, Tunis...");
        tfPrix .setStyle(inputStyle); tfPrix .setPromptText("ex: 5000.00");

        String cbStyle =
                "-fx-background-color: #2A3142; -fx-text-fill: #FFF;" +
                        "-fx-background-radius: 10; -fx-border-radius: 10;" +
                        "-fx-border-color: #3A4155; -fx-font-size: 13px;";
        cbJeu   .setStyle(cbStyle); cbJeu   .setMaxWidth(Double.MAX_VALUE);
        cbCentre.setStyle(cbStyle); cbCentre.setMaxWidth(Double.MAX_VALUE);
        dpDebut .setStyle(cbStyle); dpDebut .setMaxWidth(Double.MAX_VALUE);
        dpFin   .setStyle(cbStyle); dpFin   .setMaxWidth(Double.MAX_VALUE);
        spEquipes.setStyle(cbStyle); spEquipes.setMaxWidth(Double.MAX_VALUE);

        cbJeu.setItems(FXCollections.observableArrayList(
                "League of Legends", "Valorant", "CS2", "FIFA 25", "Fortnite",
                "Rocket League", "Dota 2", "Overwatch 2", "Apex Legends",
                "Street Fighter 6", "Tekken 8", "Autre"
        ));
        cbJeu.setPromptText("Choisir un jeu...");

        if (centreList != null)
            cbCentre.setItems(FXCollections.observableArrayList(centreList));
        cbCentre.setPromptText("Centre (optionnel)");

        // Pre-fill in edit mode
        if (isEdit) {
            tfNom.setText(existing.getNom());
            cbJeu.setValue(existing.getJeu());
            dpDebut.setValue(existing.getDateDebut());
            dpFin.setValue(existing.getDateFin());
            tfLieu.setText(existing.getLieu());
            tfPrix.setText(String.valueOf(existing.getPrix()));
            spEquipes.getValueFactory().setValue(existing.getNbEquipes());
            if (existing.getCentreId() > 0 && centreList != null)
                centreList.stream().filter(c -> c.getId() == existing.getCentreId())
                        .findFirst().ifPresent(cbCentre::setValue);
        }

        // ── Layout ────────────────────────────────────────
        GridPane grid = new GridPane();
        grid.setHgap(16); grid.setVgap(14);
        grid.setPadding(new Insets(24, 28, 8, 28));
        grid.setStyle("-fx-background-color: #1E2333;");

        ColumnConstraints c1 = new ColumnConstraints(130);
        ColumnConstraints c2 = new ColumnConstraints(220, 220, Double.MAX_VALUE);
        c2.setHgrow(Priority.ALWAYS);
        ColumnConstraints c3 = new ColumnConstraints(130);
        ColumnConstraints c4 = new ColumnConstraints(220, 220, Double.MAX_VALUE);
        c4.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c2, c3, c4);

        // Row 0
        grid.add(fieldLabel("NOM DU TOURNOI"), 0, 0); grid.add(tfNom,    1, 0);
        grid.add(fieldLabel("JEU"),            2, 0); grid.add(cbJeu,    3, 0);
        // Row 1
        grid.add(fieldLabel("CENTRE"),         0, 1); grid.add(cbCentre, 1, 1);
        grid.add(fieldLabel("LIEU"),           2, 1); grid.add(tfLieu,   3, 1);
        // Row 2
        grid.add(fieldLabel("DATE DÉBUT"),     0, 2); grid.add(dpDebut,  1, 2);
        grid.add(fieldLabel("DATE FIN"),       2, 2); grid.add(dpFin,    3, 2);
        // Row 3
        grid.add(fieldLabel("PRIX (€)"),       0, 3); grid.add(tfPrix,   1, 3);
        grid.add(fieldLabel("NB ÉQUIPES"),     2, 3); grid.add(spEquipes, 3, 3);

        GridPane.setColumnSpan(tfNom,    1);
        GridPane.setColumnSpan(cbCentre, 1);

        // ── Dialog ────────────────────────────────────────
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle(isEdit ? "✎  Modifier le tournoi" : "➕  Nouveau Tournoi");
        dlg.setHeaderText(null);

        DialogPane dp = dlg.getDialogPane();
        dp.setStyle("-fx-background-color: #1E2333; -fx-padding: 0;");
        dp.setPrefWidth(740);

        // Header strip
        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 28, 18, 28));
        header.setStyle(
                "-fx-background-color: linear-gradient(to right, #252B39, #2E3445);" +
                        "-fx-border-color: rgba(255,255,255,0.06); -fx-border-width: 0 0 1 0;");

        Label icon  = new Label(isEdit ? "✎" : "➕");
        icon.setStyle("-fx-font-size: 22px; -fx-text-fill: #6FA3FF;");
        Label title = new Label(isEdit ? "Modifier le Tournoi" : "Nouveau Tournoi");
        title.setStyle("-fx-font-size: 17px; -fx-font-weight: 700; -fx-text-fill: #FFFFFF;");
        Label sub = new Label(isEdit
                ? "Mettez à jour les informations du tournoi"
                : "Remplissez les champs pour créer un nouveau tournoi");
        sub.setStyle("-fx-font-size: 12px; -fx-text-fill: #8A93A5;");
        VBox titleBox = new VBox(3, title, sub);
        header.getChildren().addAll(icon, titleBox);

        VBox root = new VBox(0, header, grid);
        root.setStyle("-fx-background-color: #1E2333;");
        dp.setContent(root);

        // Buttons
        ButtonType btnConfirm = new ButtonType(
                isEdit ? "✎  Enregistrer" : "✚  Ajouter", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel  = new ButtonType("✖  Annuler",  ButtonBar.ButtonData.CANCEL_CLOSE);
        dp.getButtonTypes().addAll(btnConfirm, btnCancel);

        // Style buttons
        Button confirmBtn = (Button) dp.lookupButton(btnConfirm);
        confirmBtn.setStyle(
                "-fx-background-color: linear-gradient(to right,#4F6FDB,#597EE8);" +
                        "-fx-text-fill: white; -fx-font-weight: 700; -fx-font-size: 13px;" +
                        "-fx-background-radius: 10; -fx-padding: 10 24; -fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian,rgba(89,126,232,0.5),12,0.4,0,4);");
        Button cancelBtn = (Button) dp.lookupButton(btnCancel);
        cancelBtn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.05);" +
                        "-fx-text-fill: #8A93A5; -fx-font-size: 13px;" +
                        "-fx-background-radius: 10; -fx-padding: 10 24; -fx-cursor: hand;" +
                        "-fx-border-color: #3A4155; -fx-border-radius: 10;");

        // Style button bar area
        dp.lookup(".button-bar").setStyle(
                "-fx-background-color: #1A1F2E;" +
                        "-fx-padding: 14 24 14 24;" +
                        "-fx-border-color: rgba(255,255,255,0.05); -fx-border-width: 1 0 0 0;");

        // Intercept confirm to validate
        confirmBtn.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            String errs = validate(tfNom, cbJeu, dpDebut, dpFin, tfLieu, tfPrix);
            if (!errs.isEmpty()) {
                e.consume();  // prevent dialog close
                showWarning("Formulaire invalide", errs);
            }
        });

        // ── Show & handle result ──────────────────────────
        Optional<ButtonType> result = dlg.showAndWait();
        if (result.isPresent() && result.get() == btnConfirm) {
            try {
                Centre sel = cbCentre.getValue();
                int centreId = (sel != null) ? sel.getId() : 0;
                Tournoi t = new Tournoi(
                        tfNom.getText().trim(),
                        cbJeu.getValue(),
                        dpDebut.getValue(),
                        dpFin.getValue(),
                        tfLieu.getText().trim(),
                        Double.parseDouble(tfPrix.getText().trim().replace(",", ".")),
                        spEquipes.getValue(),
                        centreId
                );
                if (isEdit) {
                    t.setId(existing.getId());
                    dao.modifier(t);
                    setStatus("✔  Tournoi modifié avec succès !", "success");
                } else {
                    dao.ajouter(t);
                    setStatus("✔  Tournoi ajouté avec succès !", "success");
                }
                loadData();
            } catch (SQLException ex) {
                showError("Erreur DB", ex.getMessage());
            }
        }
    }

    // ── Helper: field label ────────────────────────────────
    private Label fieldLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 10px; -fx-font-weight: 700; " +
                "-fx-text-fill: #8A93A5; -fx-padding: 0 0 0 2;");
        return lbl;
    }

    // ══════════════════════════════════════════════════════
    //  DELETE
    // ══════════════════════════════════════════════════════

    private void confirmAndDelete(Tournoi t) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("Supprimer \"" + t.getNom() + "\" ?");
        confirm.setContentText("Cette action est irréversible.");
        styleAlert(confirm);

        Optional<ButtonType> r = confirm.showAndWait();
        if (r.isPresent() && r.get() == ButtonType.OK) {
            try {
                dao.supprimer(t.getId());
                loadData();
                setStatus("🗑  Tournoi supprimé.", "warning");
            } catch (SQLException e) {
                showError("Erreur DB", e.getMessage());
            }
        }
    }

    // ══════════════════════════════════════════════════════
    //  DETAILS DIALOG
    // ══════════════════════════════════════════════════════

    private void showDetails(Tournoi t) {
        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle("Détails du Tournoi");
        dlg.setHeaderText(null);

        VBox content = new VBox(12);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: #252B39;");
        content.setPrefWidth(400);

        Label title = new Label("⚔  " + t.getNom());
        title.setStyle("-fx-text-fill:#FFF;-fx-font-size:18px;-fx-font-weight:700;");

        Label badge = new Label("🎮  " + t.getJeu());
        badge.setStyle("-fx-text-fill:#6FA3FF;-fx-background-color:rgba(89,126,232,0.15);" +
                "-fx-padding:4 12;-fx-background-radius:20;-fx-font-size:12px;");

        String centreName = "—";
        if (t.getCentreId() > 0 && centreList != null)
            centreName = centreList.stream().filter(c -> c.getId() == t.getCentreId())
                    .map(Centre::getName).findFirst().orElse("—");

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color:rgba(255,255,255,0.06);");

        content.getChildren().addAll(title, badge, sep,
                detailRow("🏢 Centre",     centreName),
                detailRow("📅 Début",      t.getDateDebut().format(DATE_FMT)),
                detailRow("📅 Fin",        t.getDateFin().format(DATE_FMT)),
                detailRow("📍 Lieu",       t.getLieu()),
                detailRow("💰 Prix",       String.format("%.2f €", t.getPrix())),
                detailRow("👥 Équipes",    t.getNbEquipes() + " équipes"),
                detailRow("🆔 ID",         String.valueOf(t.getId()))
        );

        dlg.getDialogPane().setContent(content);
        dlg.getDialogPane().setStyle("-fx-background-color:#252B39;-fx-padding:0;");
        dlg.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        Button closeBtn = (Button) dlg.getDialogPane().lookupButton(ButtonType.CLOSE);
        closeBtn.setStyle("-fx-background-color:linear-gradient(to right,#4F6FDB,#597EE8);" +
                "-fx-text-fill:white;-fx-font-weight:bold;" +
                "-fx-background-radius:10;-fx-padding:9 24;");
        dlg.showAndWait();
    }

    private HBox detailRow(String label, String value) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill:#8A93A5;-fx-font-size:12px;-fx-min-width:120;");
        Label val = new Label(value);
        val.setStyle("-fx-text-fill:#FFF;-fx-font-size:13px;-fx-font-weight:600;");
        HBox row = new HBox(10, lbl, val);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    // ══════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════

    private void loadData() {
        try {
            masterList   = FXCollections.observableArrayList(dao.listerTous());
            filteredList = new FilteredList<>(masterList, p -> true);
            sortedList = new SortedList<>(filteredList);
            tableView.setItems(sortedList);
            tableView.comparatorProperty().addListener((obs, o, nv) -> {
                if (nv != null) {
                    sortedList.setComparator(nv);
                }
            });
            populateFilters();
            updateCount();
            updateTopbarStats();
        } catch (SQLException e) { showError("Erreur de chargement", e.getMessage()); }
    }

    private void updateTopbarStats() {
        if (filteredList == null) return;
        LocalDate today = LocalDate.now();
        long active   = filteredList.stream().filter(t -> !t.getDateDebut().isAfter(today) && !t.getDateFin().isBefore(today)).count();
        long upcoming = filteredList.stream().filter(t ->  t.getDateDebut().isAfter(today)).count();
        if (lblStatTotal    != null) lblStatTotal   .setText(String.valueOf(filteredList.size()));
        if (lblStatActive   != null) lblStatActive  .setText(String.valueOf(active));
        if (lblStatUpcoming != null) lblStatUpcoming.setText(String.valueOf(upcoming));
    }

    private String validate(TextField tfNom, ComboBox<String> cbJeu,
                            DatePicker dpDebut, DatePicker dpFin,
                            TextField tfLieu, TextField tfPrix) {
        StringBuilder err = new StringBuilder();

        // Validation - Nom du tournoi
        String nom = tfNom.getText().trim();
        if (nom.isEmpty()) {
            err.append("• Le nom du tournoi est requis.\n");
        } else if (nom.length() < 3) {
            err.append("• Le nom doit contenir au minimum 3 caractères.\n");
        } else if (nom.length() > 100) {
            err.append("• Le nom ne peut pas dépasser 100 caractères.\n");
        } else if (!nom.matches("^[a-zA-Z0-9\\s\\-àâäçèéêëìîïñòôöùûüüÀÂÄÇÈÉÊËÌÎÏÑÒÔÖÙÛÜ]+$")) {
            err.append("• Le nom contient des caractères non autorisés.\n");
        }

        // Validation - Jeu
        if (cbJeu.getValue() == null) {
            err.append("• Le jeu est requis.\n");
        }

        // Validation - Dates
        if (dpDebut.getValue() == null) {
            err.append("• La date de début est requise.\n");
        }
        if (dpFin.getValue() == null) {
            err.append("• La date de fin est requise.\n");
        }
        if (dpDebut.getValue() != null && dpFin.getValue() != null) {
            if (dpFin.getValue().isBefore(dpDebut.getValue())) {
                err.append("• La date de fin doit être après la date de début.\n");
            } else if (dpDebut.getValue().equals(dpFin.getValue())) {
                err.append("• Les dates de début et fin doivent être différentes.\n");
            }
            // Check if dates are not in the past
            LocalDate today = LocalDate.now();
            if (dpDebut.getValue().isBefore(today)) {
                err.append("• La date de début ne peut pas être dans le passé.\n");
            }
        }

        // Validation - Lieu
        String lieu = tfLieu.getText().trim();
        if (lieu.isEmpty()) {
            err.append("• Le lieu est requis.\n");
        } else if (lieu.length() < 2) {
            err.append("• Le lieu doit contenir au minimum 2 caractères.\n");
        } else if (lieu.length() > 100) {
            err.append("• Le lieu ne peut pas dépasser 100 caractères.\n");
        }

        // Validation - Prix
        String prixStr = tfPrix.getText().trim();
        if (prixStr.isEmpty()) {
            err.append("• Le prix est requis.\n");
        } else {
            try {
                double v = Double.parseDouble(prixStr.replace(",", "."));
                if (v < 0) {
                    err.append("• Le prix doit être positif.\n");
                } else if (v == 0) {
                    err.append("• Le prix doit être supérieur à 0 €.\n");
                } else if (v > 1000000) {
                    err.append("• Le prix semble anormal (> 1 000 000 €).\n");
                }
            } catch (NumberFormatException e) {
                err.append("• Le prix doit être un nombre valide (ex: 5000.00).\n");
            }
        }

        return err.toString();
    }

    private void updateCount() {
        int n = filteredList != null ? filteredList.size() : 0;
        lblCount.setText(n + " tournoi" + (n > 1 ? "s" : ""));
    }

    private void setStatus(String msg, String type) {
        lblStatus.setText(msg);
        String c = "success".equals(type) ? "#00FFD1,rgba(0,255,209,0.1),rgba(0,255,209,0.3)"
                : "warning".equals(type) ? "#fbbf24,rgba(251,191,36,0.1),rgba(251,191,36,0.3)"
                  : "info"   .equals(type) ? "#6FA3FF,rgba(89,126,232,0.1),rgba(89,126,232,0.3)"
                    : "#00FFD1,rgba(0,255,209,0.1),rgba(0,255,209,0.3)";
        String[] parts = c.split(",");
        lblStatus.setStyle(
                "-fx-text-fill:" + parts[0] + ";-fx-background-color:" + parts[1] + ";" +
                        "-fx-border-color:" + parts[2] + ";-fx-padding:5 14;" +
                        "-fx-background-radius:20;-fx-border-radius:20;" +
                        "-fx-font-size:12px;-fx-font-weight:600;");
    }

    private void showError(String header, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erreur"); a.setHeaderText(header); a.setContentText(msg);
        styleAlert(a); a.showAndWait();
    }

    private void showWarning(String header, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Attention"); a.setHeaderText(header); a.setContentText(msg);
        styleAlert(a); a.showAndWait();
    }

    private void styleAlert(Alert a) {
        DialogPane dp = a.getDialogPane();
        dp.setStyle("-fx-background-color:#252B39;");
        Label ct = (Label) dp.lookup(".content.label");
        if (ct != null) ct.setStyle("-fx-text-fill:#BFC7D5;-fx-font-size:13px;");
        Label hd = (Label) dp.lookup(".header-panel .label");
        if (hd != null) hd.setStyle("-fx-text-fill:#FFF;-fx-font-size:14px;-fx-font-weight:bold;");
    }

    private void setupSort() {
        cbSort.getItems().addAll("ID croissant", "Nom A-Z", "Date début croissant", "Prix croissant", "Équipes croissant");
        cbSort.setValue("ID croissant");
        cbSort.valueProperty().addListener((obs, o, nv) -> applySort());
    }

    private void applySort() {
        if (sortedList == null) return;
        String sort = cbSort.getValue();
        Comparator<Tournoi> comp = switch (sort) {
            case "ID croissant" -> Comparator.comparing(Tournoi::getId);
            case "Nom A-Z" -> Comparator.comparing(Tournoi::getNom, String.CASE_INSENSITIVE_ORDER);
            case "Date début croissant" -> Comparator.comparing(Tournoi::getDateDebut);
            case "Prix croissant" -> Comparator.comparingDouble(Tournoi::getPrix);
            case "Équipes croissant" -> Comparator.comparing(Tournoi::getNbEquipes);
            default -> Comparator.comparing(Tournoi::getId);
        };
        sortedList.setComparator(comp);
    }


    public void handleBackToDashboard(ActionEvent actionEvent) {
        try {
            MainApp.navigateTo("admin_dashboard.fxml", "⚡ Admin Dashboard");
        } catch (Exception e) {
            showError("Navigation", e.getMessage());
        }
    }
}
