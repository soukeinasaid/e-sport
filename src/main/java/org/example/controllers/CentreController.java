package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import org.example.MainFX;
import org.example.entities.Centre;
import org.example.services.CentreService;
import org.example.services.TournoiService;
import org.example.utils.SessionManager;

import java.net.URL;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class CentreController implements Initializable {

    // ── Table ─────────────────────────────────────────────
    @FXML private TableView<Centre>              tableView;
    @FXML private TableColumn<Centre, Integer>   colId;
    @FXML private TableColumn<Centre, String>    colName;
    @FXML private TableColumn<Centre, String>    colCity;
    @FXML private TableColumn<Centre, String>    colAddress;
    @FXML private TableColumn<Centre, String>    colEmail;
    @FXML private TableColumn<Centre, Void>      colTournois;
    @FXML private TableColumn<Centre, Void>      colActions;

    // ── Toolbar & Filters ─────────────────────────────────
    @FXML private TextField        tfSearch;
    @FXML private ComboBox<String> cbFilterCity;
    @FXML private ComboBox<String> cbSort;
    @FXML private Label            lblStatus;
    @FXML private Label            lblCount;
    @FXML private Label            lblStatTotal;
    @FXML private Label            lblStatCities;

    // ── State ─────────────────────────────────────────────
    private final CentreService  dao        = new CentreService();
    private final TournoiService tournoiDao = new TournoiService();
    private ObservableList<Centre> masterList;
    private FilteredList<Centre>   filteredList;
    private SortedList<Centre>     sortedList;
    private java.util.List<org.example.entities.Tournoi> allTournois;

    // ══════════════════════════════════════════════════════
    //  INITIALIZE
    // ══════════════════════════════════════════════════════
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupColumns();
        setupTournoisColumn();
        setupActionsColumn();
        setupSearch();
        setupSort();
        loadData();
    }

    // ── Regular columns ───────────────────────────────────
    private void setupColumns() {
        colId     .setCellValueFactory(new PropertyValueFactory<>("id"));
        colName   .setCellValueFactory(new PropertyValueFactory<>("name"));
        colCity   .setCellValueFactory(new PropertyValueFactory<>("city"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colEmail  .setCellValueFactory(new PropertyValueFactory<>("contactEmail"));
    }

    // ── Tournament count column ─────────────────────────────
    private void setupTournoisColumn() {
        colTournois.setCellFactory(col -> new TableCell<Centre, Void>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) { setGraphic(null); return; }
                Centre c = getTableView().getItems().get(getIndex());
                long count = allTournois == null ? 0 :
                    allTournois.stream().filter(t -> t.getCentreId() == c.getId()).count();
                Label badge = new Label("🏆 " + count);
                badge.getStyleClass().add(count > 0 ? "badge-active" : "badge-finished");
                setGraphic(badge);
            }
        });
    }

    // ── Actions column ────────────────────────────────────
    private void setupActionsColumn() {
        colActions.setCellFactory(col -> new TableCell<Centre, Void>() {
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

    // ── Live search ───────────────────────────────────────
    private void setupSearch() {
        tfSearch.textProperty().addListener((obs, o, nv) -> applyFilters());
        cbFilterCity.valueProperty().addListener((obs, o, nv) -> applyFilters());
    }

    private void setupSort() {
        cbSort.getItems().addAll("ID croissant", "ID décroissant", "Nom A-Z", "Nom Z-A", "Ville A-Z", "Ville Z-A");
        cbSort.setValue("ID croissant");
        cbSort.valueProperty().addListener((obs, o, nv) -> applySort());
    }

    private void populateCityFilter() {
        cbFilterCity.getItems().clear();
        cbFilterCity.getItems().add("Toutes les villes");
        masterList.stream().map(Centre::getCity).distinct().sorted()
                  .forEach(city -> cbFilterCity.getItems().add(city));
    }

    private void applyFilters() {
        if (filteredList == null) return;
        String q    = tfSearch.getText().toLowerCase().trim();
        String city = cbFilterCity.getValue();
        filteredList.setPredicate(c -> {
            if (!q.isEmpty() && !c.getName().toLowerCase().contains(q)
                    && !c.getCity().toLowerCase().contains(q)
                    && (c.getContactEmail() == null || !c.getContactEmail().toLowerCase().contains(q))) return false;
            if (city != null && !city.equals("Toutes les villes") && !c.getCity().equals(city)) return false;
            return true;
        });
        updateCount();
        updateTopbarStats();
        applySort();
    }

    @FXML
    private void handleResetFilters() {
        tfSearch.clear();
        cbFilterCity.setValue(null);
        cbSort.setValue("ID croissant");
    }

    private void updateTopbarStats() {
        if (filteredList == null) return;
        long cities = filteredList.stream().map(Centre::getCity).distinct().count();
        if (lblStatTotal  != null) lblStatTotal .setText(String.valueOf(filteredList.size()));
        if (lblStatCities != null) lblStatCities.setText(String.valueOf(cities));
    }

    // ══════════════════════════════════════════════════════
    //  NAVIGATION
    // ══════════════════════════════════════════════════════

    @FXML
    private void handleNavTournois() {
        try { MainFX.navigateTo("TournoiView.fxml", "⚔  Tournoi Manager"); }
        catch (Exception e) { showError("Navigation", e.getMessage()); }
    }

    @FXML
    private void handleLogout() {
        SessionManager.logout();
        try { MainFX.navigateTo("LoginView.fxml", "⚔️  TournoiManager — Connexion"); }
        catch (Exception e) { showError("Logout", e.getMessage()); }
    }

    // ══════════════════════════════════════════════════════
    //  POPUP DIALOG — shared for ADD and EDIT
    // ══════════════════════════════════════════════════════

    @FXML
    private void handleOpenAddDialog() {
        showCentreDialog(null);
    }

    private void handleOpenEditDialog(Centre existing) {
        showCentreDialog(existing);
    }

    /**
     * Opens the Add/Edit popup dialog.
     * @param existing  null → Add mode,  non-null → Edit mode
     */
    private void showCentreDialog(Centre existing) {
        boolean isEdit = (existing != null);

        // ── Form fields ──────────────────────────────────
        TextField tfName    = new TextField();
        TextField tfAddress = new TextField();
        TextField tfCity    = new TextField();
        TextField tfEmail   = new TextField();
        TextField tfMapUrl  = new TextField();

        String inputStyle =
            "-fx-background-color: #2A3142; -fx-text-fill: #FFF;" +
            "-fx-prompt-text-fill: #5A6478; -fx-background-radius: 10;" +
            "-fx-border-radius: 10; -fx-border-color: #3A4155;" +
            "-fx-padding: 9 12; -fx-font-size: 13px;";

        tfName   .setStyle(inputStyle); tfName   .setPromptText("ex: Cyber Arena Tunis");
        tfAddress.setStyle(inputStyle); tfAddress.setPromptText("ex: 12 rue de la Liberté");
        tfCity   .setStyle(inputStyle); tfCity   .setPromptText("ex: Tunis, Paris...");
        tfEmail  .setStyle(inputStyle); tfEmail  .setPromptText("ex: contact@centre.tn");
        tfMapUrl .setStyle(inputStyle); tfMapUrl .setPromptText("https://maps.google.com/...");

        // Pre-fill in edit mode
        if (isEdit) {
            tfName   .setText(existing.getName()         != null ? existing.getName()         : "");
            tfAddress.setText(existing.getAddress()      != null ? existing.getAddress()      : "");
            tfCity   .setText(existing.getCity()         != null ? existing.getCity()         : "");
            tfEmail  .setText(existing.getContactEmail() != null ? existing.getContactEmail() : "");
            tfMapUrl .setText(existing.getMapUrl()       != null ? existing.getMapUrl()       : "");
        }

        // ── Layout: 2-column GridPane ─────────────────────
        GridPane grid = new GridPane();
        grid.setHgap(16); grid.setVgap(14);
        grid.setPadding(new Insets(24, 28, 12, 28));
        grid.setStyle("-fx-background-color: #1E2333;");

        ColumnConstraints c1 = new ColumnConstraints(130);
        ColumnConstraints c2 = new ColumnConstraints(220, 240, Double.MAX_VALUE);
        c2.setHgrow(Priority.ALWAYS);
        ColumnConstraints c3 = new ColumnConstraints(130);
        ColumnConstraints c4 = new ColumnConstraints(220, 240, Double.MAX_VALUE);
        c4.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c2, c3, c4);

        grid.add(fieldLabel("NOM DU CENTRE"), 0, 0); grid.add(tfName,    1, 0);
        grid.add(fieldLabel("VILLE"),          2, 0); grid.add(tfCity,    3, 0);
        grid.add(fieldLabel("ADRESSE"),        0, 1); GridPane.setColumnSpan(tfAddress, 3);
                                                       grid.add(tfAddress, 1, 1);
        grid.add(fieldLabel("EMAIL"),          0, 2); grid.add(tfEmail,   1, 2);
        grid.add(fieldLabel("MAP URL"),        2, 2); grid.add(tfMapUrl,  3, 2);

        // ── Dialog ───────────────���────────────────────────
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle(isEdit ? "✎  Modifier le Centre" : "➕  Nouveau Centre");
        dlg.setHeaderText(null);

        DialogPane dp = dlg.getDialogPane();
        dp.setStyle("-fx-background-color: #1E2333; -fx-padding: 0;");
        dp.setPrefWidth(720);

        // Header strip
        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 28, 18, 28));
        header.setStyle(
            "-fx-background-color: linear-gradient(to right, #252B39, #2E3445);" +
            "-fx-border-color: rgba(255,255,255,0.06); -fx-border-width: 0 0 1 0;");

        Label icon  = new Label(isEdit ? "✎" : "🏢");
        icon.setStyle("-fx-font-size: 22px; -fx-text-fill: #00FFD1;");
        Label titleLbl = new Label(isEdit ? "Modifier le Centre" : "Nouveau Centre");
        titleLbl.setStyle("-fx-font-size: 17px; -fx-font-weight: 700; -fx-text-fill: #FFFFFF;");
        Label sub = new Label(isEdit
                ? "Mettez à jour les informations du centre"
                : "Remplissez les champs pour ajouter un nouveau centre");
        sub.setStyle("-fx-font-size: 12px; -fx-text-fill: #8A93A5;");
        VBox titleBox = new VBox(3, titleLbl, sub);
        header.getChildren().addAll(icon, titleBox);

        VBox root = new VBox(0, header, grid);
        root.setStyle("-fx-background-color: #1E2333;");
        dp.setContent(root);

        // Buttons
        ButtonType btnConfirm = new ButtonType(
                isEdit ? "✎  Enregistrer" : "✚  Ajouter", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel  = new ButtonType("✖  Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dp.getButtonTypes().addAll(btnConfirm, btnCancel);

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

        dp.lookup(".button-bar").setStyle(
            "-fx-background-color: #1A1F2E;" +
            "-fx-padding: 14 24 14 24;" +
            "-fx-border-color: rgba(255,255,255,0.05); -fx-border-width: 1 0 0 0;");

        // Validate before close
        confirmBtn.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            String errs = validate(tfName, tfCity, tfEmail);
            if (!errs.isEmpty()) {
                e.consume();
                showWarning("Formulaire invalide", errs);
            }
        });

        // ── Show & handle result ──────────────────────────
        Optional<ButtonType> result = dlg.showAndWait();
        if (result.isPresent() && result.get() == btnConfirm) {
            try {
                Centre c = new Centre(
                        tfName   .getText().trim(),
                        tfAddress.getText().trim(),
                        tfCity   .getText().trim(),
                        tfEmail  .getText().trim(),
                        tfMapUrl .getText().trim()
                );
                if (isEdit) {
                    c.setId(existing.getId());
                    dao.modifier(c);
                    setStatus("✔  Centre modifié avec succès !", "success");
                } else {
                    dao.ajouter(c);
                    setStatus("✔  Centre ajouté avec succès !", "success");
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

    private void confirmAndDelete(Centre c) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("Supprimer \"" + c.getName() + "\" ?");
        confirm.setContentText("Cette action est irréversible.\n" +
                "Les tournois liés à ce centre perdront leur référence.");
        styleAlert(confirm);

        Optional<ButtonType> r = confirm.showAndWait();
        if (r.isPresent() && r.get() == ButtonType.OK) {
            try {
                dao.supprimer(c.getId());
                loadData();
                setStatus("🗑  Centre supprimé.", "warning");
            } catch (SQLException e) {
                showError("Erreur DB", e.getMessage());
            }
        }
    }

    // ── Details dialog ────────────────────────────────────
    private void showDetails(Centre c) {
        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle("Détails du Centre");
        dlg.setHeaderText(null);

        VBox content = new VBox(12);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: #252B39;");
        content.setPrefWidth(420);

        Label title = new Label("🏢  " + c.getName());
        title.setStyle("-fx-text-fill:#FFF;-fx-font-size:18px;-fx-font-weight:700;");

        Label cityBadge = new Label("📍  " + c.getCity());
        cityBadge.setStyle(
            "-fx-text-fill:#00FFD1;-fx-background-color:rgba(0,255,209,0.12);" +
            "-fx-padding:4 12;-fx-background-radius:20;-fx-font-size:12px;");

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color:rgba(255,255,255,0.06);");

        content.getChildren().addAll(title, cityBadge, sep,
                detailRow("🏠 Adresse", c.getAddress()      != null ? c.getAddress()      : "—"),
                detailRow("📧 Email",   c.getContactEmail() != null ? c.getContactEmail() : "—"),
                detailRow("🗺  Map URL", c.getMapUrl()       != null ? c.getMapUrl()       : "—"),
                detailRow("🆔 ID",      String.valueOf(c.getId()))
        );

        dlg.getDialogPane().setContent(content);
        dlg.getDialogPane().setStyle("-fx-background-color:#252B39;-fx-padding:0;");
        dlg.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        Button closeBtn = (Button) dlg.getDialogPane().lookupButton(ButtonType.CLOSE);
        closeBtn.setStyle(
            "-fx-background-color:linear-gradient(to right,#4F6FDB,#597EE8);" +
            "-fx-text-fill:white;-fx-font-weight:bold;" +
            "-fx-background-radius:10;-fx-padding:9 24;");

        dlg.showAndWait();
    }

    private HBox detailRow(String label, String value) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill:#8A93A5;-fx-font-size:12px;-fx-min-width:110;");
        Label val = new Label(value);
        val.setStyle("-fx-text-fill:#FFF;-fx-font-size:13px;-fx-font-weight:600;");
        val.setWrapText(true);
        HBox row = new HBox(10, lbl, val);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    // ══════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════

    private void loadData() {
        try {
            allTournois  = new org.example.services.TournoiService().listerTous();
            masterList   = FXCollections.observableArrayList(dao.listerTous());
            filteredList = new FilteredList<>(masterList, p -> true);
            sortedList = new SortedList<>(filteredList);
            tableView.setItems(sortedList);
            tableView.comparatorProperty().addListener((obs, o, nv) -> {
                if (nv != null) {
                    sortedList.setComparator(nv);
                }
            });
            populateCityFilter();
            updateCount();
            updateTopbarStats();
        } catch (SQLException e) { showError("Erreur de chargement", e.getMessage()); }
    }

    private String validate(TextField tfName, TextField tfCity, TextField tfEmail) {
        StringBuilder err = new StringBuilder();
        if (tfName.getText().trim().isEmpty()) err.append("• Le nom est requis.\n");
        if (tfCity.getText().trim().isEmpty()) err.append("• La ville est requise.\n");
        String email = tfEmail.getText().trim();
        if (!email.isEmpty() && !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
            err.append("• L'email n'est pas valide.\n");
        return err.toString();
    }

    private void updateCount() {
        int n = filteredList != null ? filteredList.size() : 0;
        lblCount.setText(n + " centre" + (n > 1 ? "s" : ""));
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

    private void applySort() {
        if (sortedList == null) return;
        String sort = cbSort.getValue();
        Comparator<Centre> comp = switch (sort) {
            case "ID croissant" -> Comparator.comparing(Centre::getId);
            case "ID décroissant" -> Comparator.comparing(Centre::getId).reversed();
            case "Nom A-Z" -> Comparator.comparing(Centre::getName, String.CASE_INSENSITIVE_ORDER);
            case "Nom Z-A" -> Comparator.comparing(Centre::getName, String.CASE_INSENSITIVE_ORDER).reversed();
            case "Ville A-Z" -> Comparator.comparing(Centre::getCity, String.CASE_INSENSITIVE_ORDER);
            case "Ville Z-A" -> Comparator.comparing(Centre::getCity, String.CASE_INSENSITIVE_ORDER).reversed();
            default -> Comparator.comparing(Centre::getId);
        };
        sortedList.setComparator(comp);
    }
}
