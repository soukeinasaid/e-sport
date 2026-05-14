package controller;

import entity.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import service.UserService;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

public class UserManagementController {

    @FXML
    private TableView<User> userTableView;

    private ObservableList<User> allUsers;
    private UserService userService;

    @FXML
    private TableColumn<User, Integer> idColumn;

    @FXML
    private TableColumn<User, String> nameColumn;

    @FXML
    private TableColumn<User, String> emailColumn;

    @FXML
    private TableColumn<User, String> roleColumn;

    @FXML
    private TableColumn<User, String> actionsColumn;

    @FXML
    private TextField searchField;
    @FXML
    private Button searchBtn;
    @FXML
    private Button allUsersBtn;
    @FXML
    private Button adminUsersBtn;
    @FXML
    private Button normalUsersBtn;

    @FXML
    private Button addUserButton;

    @FXML
    private Button refreshButton;

    @FXML
    private VBox addUserForm;

    @FXML
    private TextField newUserNameField;

    @FXML
    private TextField newUserFirstNameField;

    @FXML
    private TextField newUserEmailField;

    @FXML
    private PasswordField newUserPasswordField;

    @FXML
    private ComboBox<String> newUserRoleComboBox;

    @FXML
    private Button saveUserButton;

    @FXML
    private Button cancelAddUserButton;

    @FXML
    public void initialize() {
        userService = new UserService();
        allUsers = FXCollections.observableArrayList();

        setupTableColumns();
        setupAddUserForm();
        loadUsers();

        // Add search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterUsers());
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("idUser"));
        nameColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getPrenom() + " " + cellData.getValue().getNom()));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getRoleString()));

        // Add custom cell factory for role badges
        roleColumn.setCellFactory(param -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(role);
                    if ("ADMIN".equals(role)) {
                        getStyleClass().add("role-badge");
                    } else {
                        getStyleClass().add("role-badge");
                        getStyleClass().add("user");
                    }
                }
            }
        });

        // Add custom cell factory for actions
        actionsColumn.setCellFactory(param -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    HBox actionBox = new HBox(5);

                    Button editButton = new Button("✏️ Edit");
                    editButton.setStyle("-fx-background-color: linear-gradient(to right, #4CAF50, #66BB6A); -fx-text-fill: white; -fx-font-weight: 600; -fx-border-radius: 6; -fx-background-radius: 6;");
                    editButton.setOnAction(e -> editUser(user));

                    Button deleteButton = new Button("🗑️ Delete");
                    deleteButton.setStyle("-fx-background-color: linear-gradient(to right, #f44336, #ef5350); -fx-text-fill: white; -fx-font-weight: 600; -fx-border-radius: 6; -fx-background-radius: 6;");
                    deleteButton.setOnAction(e -> deleteUser(user));

                    Button roleButton = new Button("🔧 Role");
                    roleButton.setStyle("-fx-background-color: linear-gradient(to right, #2196F3, #42A5F5); -fx-text-fill: white; -fx-font-weight: 600; -fx-border-radius: 6; -fx-background-radius: 6;");
                    roleButton.setOnAction(e -> toggleUserRole(user));

                    // Don't allow deleting current admin
                    User currentUser = utilies.Session.getUser();
                    if (currentUser != null && currentUser.getIdUser() == user.getIdUser()) {
                        deleteButton.setDisable(true);
                        deleteButton.setTooltip(new Tooltip("Cannot delete yourself"));
                    }

                    actionBox.getChildren().addAll(editButton, roleButton, deleteButton);
                    setGraphic(actionBox);
                }
            }
        });

        userTableView.setItems(allUsers);
    }


    private void setupAddUserForm() {
        newUserRoleComboBox.getItems().addAll("USER", "ADMIN");
        newUserRoleComboBox.setValue("USER");

        // Initially hide the form
        addUserForm.setVisible(false);
        addUserForm.setManaged(false);
    }

    private void loadUsers() {
        try {
            List<User> users = userService.getAll();
            allUsers.clear();
            allUsers.addAll(users);
            userTableView.setItems(allUsers);
            userTableView.refresh();
        } catch (Exception e) {
            showAlert("Error", "Failed to load users: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void filterUsers() {
        String searchText = searchField.getText().toLowerCase();

        ObservableList<User> filteredList = FXCollections.observableArrayList();

        for (User user : allUsers) {
            boolean matchesSearch = searchText.isEmpty() ||
                    (user.getPrenom() + " " + user.getNom()).toLowerCase().contains(searchText) ||
                    user.getEmail().toLowerCase().contains(searchText);

            if (matchesSearch) {
                filteredList.add(user);
            }
        }

        userTableView.setItems(filteredList);
    }

    @FXML
    private void showAddUserForm(ActionEvent event) {
        addUserForm.setVisible(true);
        addUserForm.setManaged(true);
        clearAddUserForm();
    }

    @FXML
    private void hideAddUserForm(ActionEvent event) {
        addUserForm.setVisible(false);
        addUserForm.setManaged(false);
        clearAddUserForm();
    }

    private void clearAddUserForm() {
        newUserNameField.clear();
        newUserFirstNameField.clear();
        newUserEmailField.clear();
        newUserPasswordField.clear();
        newUserRoleComboBox.setValue("USER");
    }

    @FXML
    private void saveNewUser(ActionEvent event) {
        String nom = newUserNameField.getText();
        String prenom = newUserFirstNameField.getText();
        String email = newUserEmailField.getText();
        String password = newUserPasswordField.getText();
        String role = newUserRoleComboBox.getValue();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please fill all fields", Alert.AlertType.ERROR);
            return;
        }

        User newUser = new User(nom, prenom, email, password);
        newUser.setRoleFromString(role);

        if (userService.register(newUser)) {
            showAlert("Success", "User created successfully", Alert.AlertType.INFORMATION);
            hideAddUserForm(null);
            loadUsers();
        } else {
            showAlert("Error", "Failed to create user", Alert.AlertType.ERROR);
        }
    }

    private void editUser(User user) {
        // Create edit dialog
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Edit User");
        dialog.setHeaderText("Edit user: " + user.getPrenom() + " " + user.getNom());

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(user.getNom());
        TextField firstNameField = new TextField(user.getPrenom());
        TextField emailField = new TextField(user.getEmail());
        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("USER", "ADMIN");
        roleComboBox.setValue(user.getRoleString());

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("First Name:"), 0, 1);
        grid.add(firstNameField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Role:"), 0, 3);
        grid.add(roleComboBox, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Convert the result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                user.setNom(nameField.getText());
                user.setPrenom(firstNameField.getText());
                user.setEmail(emailField.getText());
                user.setRoleFromString(roleComboBox.getValue());
                return user;
            }
            return null;
        });

        Optional<User> result = dialog.showAndWait();
        result.ifPresent(updatedUser -> {
            if (userService.update(updatedUser)) {
                showAlert("Success", "User updated successfully", Alert.AlertType.INFORMATION);
                loadUsers();
            } else {
                showAlert("Error", "Failed to update user", Alert.AlertType.ERROR);
            }
        });
    }

    private void deleteUser(User user) {
        User currentUser = utilies.Session.getUser();
        if (currentUser != null && currentUser.getIdUser() == user.getIdUser()) {
            showAlert("Error", "Cannot delete yourself", Alert.AlertType.ERROR);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete User");
        alert.setContentText("Are you sure you want to delete " + user.getPrenom() + " " + user.getNom() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (userService.delete(user.getIdUser())) {
                showAlert("Success", "User deleted successfully", Alert.AlertType.INFORMATION);
                loadUsers();
            } else {
                showAlert("Error", "Failed to delete user", Alert.AlertType.ERROR);
            }
        }
    }

    private void toggleUserRole(User user) {
        User currentUser = utilies.Session.getUser();
        if (currentUser != null && currentUser.getIdUser() == user.getIdUser()) {
            showAlert("Error", "Cannot change your own role", Alert.AlertType.ERROR);
            return;
        }

        String newRole = user.isAdmin() ? "USER" : "ADMIN";

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Change Role");
        alert.setHeaderText("Change User Role");
        alert.setContentText("Change " + user.getPrenom() + " " + user.getNom() + "'s role to " + newRole + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            user.setRoleFromString(newRole);
            if (userService.update(user)) {
                showAlert("Success", "User role changed to " + newRole, Alert.AlertType.INFORMATION);
                loadUsers();
            } else {
                showAlert("Error", "Failed to change user role", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void refreshUsers(ActionEvent event) {
        loadUsers();
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String searchText = searchField.getText().toLowerCase();
        ObservableList<User> filteredList = FXCollections.observableArrayList();

        for (User user : allUsers) {
            String fullName = (user.getPrenom() + " " + user.getNom()).toLowerCase();
            String email = user.getEmail().toLowerCase();

            if (fullName.contains(searchText) || email.contains(searchText)) {
                filteredList.add(user);
            }
        }

        userTableView.setItems(filteredList);
    }

    @FXML
    private void handleAllUsers(ActionEvent event) {
        userTableView.setItems(allUsers);
        updateFilterButtons("all");
    }

    @FXML
    private void handleAdminUsers(ActionEvent event) {
        ObservableList<User> adminUsers = FXCollections.observableArrayList();
        for (User user : allUsers) {
            if (user.isAdmin()) {
                adminUsers.add(user);
            }
        }
        userTableView.setItems(adminUsers);
        updateFilterButtons("admin");
    }

    @FXML
    private void handleNormalUsers(ActionEvent event) {
        ObservableList<User> normalUsers = FXCollections.observableArrayList();
        for (User user : allUsers) {
            if (!user.isAdmin()) {
                normalUsers.add(user);
            }
        }
        userTableView.setItems(normalUsers);
        updateFilterButtons("user");
    }

    private void updateFilterButtons(String activeFilter) {
        allUsersBtn.getStyleClass().remove("active");
        adminUsersBtn.getStyleClass().remove("active");
        normalUsersBtn.getStyleClass().remove("active");

        switch (activeFilter) {
            case "admin":
                adminUsersBtn.getStyleClass().add("active");
                break;
            case "user":
                normalUsersBtn.getStyleClass().add("active");
                break;
            default:
                allUsersBtn.getStyleClass().add("active");
                break;
        }
    }

    @FXML
    private void handleBackToMain(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/mainLayout.fxml"));
            Parent mainLayout = loader.load();

            // Get the current stage and set the new scene
            javafx.stage.Stage stage = (javafx.stage.Stage) ((Node) event.getSource()).getScene().getWindow();
            javafx.scene.Scene scene = new javafx.scene.Scene(mainLayout);

            URL cssUrl = getClass().getResource("/css/style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            stage.setScene(scene);
            stage.setTitle("VictoryGrid - Main Dashboard");
            stage.show();

        } catch (IOException e) {
            System.err.println("Error returning to main layout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            utilies.Session.setUser(null);

            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));

            javafx.stage.Stage stage = (javafx.stage.Stage) ((Node) event.getSource()).getScene().getWindow();
            javafx.scene.Scene scene = new javafx.scene.Scene(root);

            URL cssUrl = getClass().getResource("/css/style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    @FXML
    private void handleTournamentManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/TournoiView.fxml"));
            Parent tournamentView = loader.load();

            // Navigate up to find the BorderPane (MainLayout)
            Stage stage = (Stage) userTableView.getScene().getWindow();
            Scene scene = stage.getScene();

            // The root might be a BorderPane (MainLayout) or something else
            // Find the BorderPane by traversing up
            Parent root = scene.getRoot();

            // If root is a ScrollPane (like UserManagementView), we need to find its parent
            if (root instanceof ScrollPane) {
                // Get the parent of this ScrollPane (should be BorderPane from MainLayout)
                Parent parent = root.getParent();
                if (parent instanceof BorderPane) {
                    ((BorderPane) parent).setCenter(tournamentView);
                } else {
                    // Fallback: replace the entire scene root
                    scene.setRoot(tournamentView);
                }
            } else if (root instanceof BorderPane) {
                ((BorderPane) root).setCenter(tournamentView);
            } else {
                scene.setRoot(tournamentView);
            }

            stage.setTitle("🏆 Gestion des Tournois");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCenterManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CentreView.fxml"));
            Parent tournamentView = loader.load();

            // Navigate up to find the BorderPane (MainLayout)
            Stage stage = (Stage) userTableView.getScene().getWindow();
            Scene scene = stage.getScene();

            // The root might be a BorderPane (MainLayout) or something else
            // Find the BorderPane by traversing up
            Parent root = scene.getRoot();

            // If root is a ScrollPane (like UserManagementView), we need to find its parent
            if (root instanceof ScrollPane) {
                // Get the parent of this ScrollPane (should be BorderPane from MainLayout)
                Parent parent = root.getParent();
                if (parent instanceof BorderPane) {
                    ((BorderPane) parent).setCenter(tournamentView);
                } else {
                    // Fallback: replace the entire scene root
                    scene.setRoot(tournamentView);
                }
            } else if (root instanceof BorderPane) {
                ((BorderPane) root).setCenter(tournamentView);
            } else {
                scene.setRoot(tournamentView);
            }

            stage.setTitle("🏆 Gestion des centres");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
