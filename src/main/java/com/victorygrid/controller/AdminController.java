package com.victorygrid.controller;

import com.victorygrid.DashboardController;
import com.victorygrid.model.User;
import com.victorygrid.service.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class AdminController implements DashboardController.ViewController {
    
    @FXML private TextField searchField;
    @FXML private ComboBox<String> roleFilter;
    @FXML private ComboBox<String> rankFilter;
    @FXML private Button searchButton;
    @FXML private Button clearButton;
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, String> rankColumn;
    @FXML private TableColumn<User, Integer> winsColumn;
    @FXML private TableColumn<User, Integer> lossesColumn;
    @FXML private TableColumn<User, Double> winRateColumn;
    @FXML private TableColumn<User, Boolean> activeColumn;
    @FXML private TableColumn<User, Void> actionsColumn;
    @FXML private Button addUserButton;
    @FXML private Button editUserButton;
    @FXML private Button deleteUserButton;
    @FXML private Button refreshButton;
    @FXML private Label userCountLabel;
    @FXML private Label statusLabel;
    
    // Edit Dialog Fields
    @FXML private VBox editDialog;
    @FXML private Text dialogTitle;
    @FXML private TextField editUsername;
    @FXML private TextField editEmail;
    @FXML private PasswordField editPassword;
    @FXML private ComboBox<String> editRole;
    @FXML private ComboBox<String> editRank;
    @FXML private TextField editWins;
    @FXML private TextField editLosses;
    @FXML private CheckBox editActive;
    @FXML private Button saveUserButton;
    @FXML private Button cancelEditButton;
    
    private User currentUser;
    private UserService userService;
    private ObservableList<User> userList;
    private User editingUser;
    
    @FXML
    public void initialize() {
        userService = UserService.getInstance();
        userList = FXCollections.observableArrayList();
        
        setupTable();
        setupFilters();
        setupEventHandlers();
        setupEditDialog();
        
        loadUsers();
    }
    
    @Override
    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadUsers();
    }
    
    private void setupTable() {
        // Add null checks for table columns
        if (userTable == null) {
            System.err.println("User table is not initialized");
            return;
        }
        
        // Set up columns with null checks
        if (idColumn != null) idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (usernameColumn != null) usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        if (emailColumn != null) emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        if (roleColumn != null) roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        if (rankColumn != null) rankColumn.setCellValueFactory(new PropertyValueFactory<>("rank"));
        if (winsColumn != null) winsColumn.setCellValueFactory(new PropertyValueFactory<>("wins"));
        if (lossesColumn != null) lossesColumn.setCellValueFactory(new PropertyValueFactory<>("losses"));
        if (winRateColumn != null) winRateColumn.setCellValueFactory(new PropertyValueFactory<>("winRate"));
        if (activeColumn != null) {
            activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
            activeColumn.setCellFactory(CheckBoxTableCell.forTableColumn(activeColumn));
        }
        
        // Add actions column with null check
        if (actionsColumn != null) {
            actionsColumn.setCellFactory(param -> new TableCell<>() {
                private final Button editButton = new Button("Edit");
                private final Button deleteButton = new Button("Delete");
                private final HBox buttons = new HBox(5, editButton, deleteButton);
                
                {
                    editButton.getStyleClass().add("table-action-button");
                    deleteButton.getStyleClass().add("table-action-button");
                    deleteButton.getStyleClass().add("danger");
                    
                    editButton.setOnAction(e -> {
                        User user = getTableView().getItems().get(getIndex());
                        if (user != null) editUser(user);
                    });
                    
                    deleteButton.setOnAction(e -> {
                        User user = getTableView().getItems().get(getIndex());
                        if (user != null) deleteUser(user);
                    });
                }
                
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : buttons);
                }
            });
        }
        
        userTable.setItems(userList);
        userTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> updateActionButtons());
    }
    
    private void setupFilters() {
        // Role filter with null check
        if (roleFilter != null) {
            roleFilter.getItems().addAll("All", "PLAYER", "ADMIN");
            roleFilter.setValue("All");
        }
        
        // Rank filter with null check
        if (rankFilter != null) {
            rankFilter.getItems().addAll("All", "Bronze", "Silver", "Gold", "Platinum", "Diamond");
            rankFilter.setValue("All");
        }
        
        // Edit dialog combos with null checks
        if (editRole != null) {
            editRole.getItems().addAll("PLAYER", "ADMIN");
        }
        if (editRank != null) {
            editRank.getItems().addAll("Bronze", "Silver", "Gold", "Platinum", "Diamond");
        }
    }
    
    private void setupEventHandlers() {
        // Add null checks for all buttons and fields
        if (searchButton != null) searchButton.setOnAction(e -> handleSearch());
        if (clearButton != null) clearButton.setOnAction(e -> handleClearSearch());
        if (addUserButton != null) addUserButton.setOnAction(e -> handleAddUser());
        if (editUserButton != null) editUserButton.setOnAction(e -> handleEditSelected());
        if (deleteUserButton != null) deleteUserButton.setOnAction(e -> handleDeleteSelected());
        if (refreshButton != null) refreshButton.setOnAction(e -> loadUsers());
        
        // Edit dialog handlers with null checks
        if (saveUserButton != null) saveUserButton.setOnAction(e -> handleSaveUser());
        if (cancelEditButton != null) cancelEditButton.setOnAction(e -> closeEditDialog());
        
        // Enter key search with null check
        if (searchField != null) searchField.setOnAction(e -> handleSearch());
    }
    
    private void setupEditDialog() {
        if (editDialog != null) {
            editDialog.visibleProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    editDialog.requestFocus();
                }
            });
        }
    }
    
    private void loadUsers() {
        try {
            if (userService == null) {
                showStatus("UserService not initialized", "error");
                return;
            }
            
            List<User> users = userService.getAllUsers();
            if (userList != null) {
                userList.clear();
                userList.addAll(users);
            }
            
            if (userCountLabel != null) {
                userCountLabel.setText("Total Users: " + users.size());
            }
            
            showStatus("Users loaded successfully", "success");
        } catch (SQLException e) {
            showStatus("Error loading users: " + e.getMessage(), "error");
        } catch (Exception e) {
            showStatus("Unexpected error loading users: " + e.getMessage(), "error");
        }
    }
    
    private void handleSearch() {
        try {
            if (userService == null) {
                showStatus("UserService not initialized", "error");
                return;
            }
            
            List<User> users;
            String searchTerm = (searchField != null) ? searchField.getText().trim() : "";
            String role = (roleFilter != null) ? roleFilter.getValue() : "All";
            String rank = (rankFilter != null) ? rankFilter.getValue() : "All";
            
            if (!searchTerm.isEmpty()) {
                users = userService.searchUsers(searchTerm);
            } else if (!"All".equals(role)) {
                users = userService.getUsersByRole(role);
            } else {
                users = userService.getAllUsers();
            }
            
            // Apply additional filters
            if (!"All".equals(rank)) {
                users = users.stream()
                    .filter(user -> rank.equals(user.getRank()))
                    .toList();
            }
            
            if (userList != null) {
                userList.clear();
                userList.addAll(users);
            }
            
            if (userCountLabel != null) {
                userCountLabel.setText("Filtered Users: " + users.size());
            }
            
            showStatus("Search completed", "success");
            
        } catch (SQLException e) {
            showStatus("Search error: " + e.getMessage(), "error");
        } catch (Exception e) {
            showStatus("Unexpected search error: " + e.getMessage(), "error");
        }
    }
    
    private void handleClearSearch() {
        if (searchField != null) searchField.clear();
        if (roleFilter != null) roleFilter.setValue("All");
        if (rankFilter != null) rankFilter.setValue("All");
        loadUsers();
    }
    
    private void handleAddUser() {
        editingUser = null;
        if (dialogTitle != null) dialogTitle.setText("Add New User");
        clearEditForm();
        showEditDialog();
    }
    
    private void handleEditSelected() {
        if (userTable == null) {
            showStatus("User table not initialized", "error");
            return;
        }
        
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            editUser(selected);
        } else {
            showStatus("Please select a user to edit", "info");
        }
    }
    
    private void editUser(User user) {
        editingUser = user;
        dialogTitle.setText("Edit User");
        
        editUsername.setText(user.getUsername());
        editEmail.setText(user.getEmail());
        editPassword.clear();
        editRole.setValue(user.getRole());
        editRank.setValue(user.getRank());
        editWins.setText(String.valueOf(user.getWins()));
        editLosses.setText(String.valueOf(user.getLosses()));
        editActive.setSelected(user.isActive());
        
        showEditDialog();
    }
    
    private void handleDeleteSelected() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            deleteUser(selected);
        } else {
            showStatus("Please select a user to delete", "info");
        }
    }
    
    private void deleteUser(User user) {
        if (user.getId() == currentUser.getId()) {
            showStatus("Cannot delete your own account", "error");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete User: " + user.getUsername());
        alert.setContentText("Are you sure you want to delete this user? This action cannot be undone.");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                boolean success = userService.deleteUser(user.getId());
                if (success) {
                    loadUsers();
                    showStatus("User deleted successfully", "success");
                } else {
                    showStatus("Failed to delete user", "error");
                }
            } catch (SQLException e) {
                showStatus("Delete error: " + e.getMessage(), "error");
            }
        }
    }
    
    private void handleSaveUser() {
        if (!validateEditForm()) {
            return;
        }
        
        try {
            User user = editingUser != null ? editingUser : new User();
            
            user.setUsername(editUsername.getText().trim());
            user.setEmail(editEmail.getText().trim());
            user.setRole(editRole.getValue());
            user.setRank(editRank.getValue());
            
            if (!editPassword.getText().isEmpty()) {
                user.setPassword(editPassword.getText());
            }
            
            try {
                user.setWins(Integer.parseInt(editWins.getText()));
                user.setLosses(Integer.parseInt(editLosses.getText()));
            } catch (NumberFormatException e) {
                showStatus("Wins and losses must be numbers", "error");
                return;
            }
            
            user.setActive(editActive.isSelected());
            
            boolean success;
            if (editingUser != null) {
                success = userService.updateUser(user);
            } else {
                user = userService.createUser(user);
                success = user.getId() > 0;
            }
            
            if (success) {
                closeEditDialog();
                loadUsers();
                showStatus("User " + (editingUser != null ? "updated" : "created") + " successfully", "success");
            } else {
                showStatus("Failed to save user", "error");
            }
            
        } catch (SQLException e) {
            showStatus("Save error: " + e.getMessage(), "error");
        }
    }
    
    private boolean validateEditForm() {
        String username = editUsername.getText().trim();
        if (username.isEmpty()) {
            showStatus("Username is required", "error");
            return false;
        }
        
        String email = editEmail.getText().trim();
        if (email.isEmpty()) {
            showStatus("Email is required", "error");
            return false;
        }
        
        if (!email.contains("@") || !email.contains(".")) {
            showStatus("Please enter a valid email", "error");
            return false;
        }
        
        if (editRole.getValue() == null) {
            showStatus("Role is required", "error");
            return false;
        }
        
        if (editRank.getValue() == null) {
            showStatus("Rank is required", "error");
            return false;
        }
        
        return true;
    }
    
    private void clearEditForm() {
        editUsername.clear();
        editEmail.clear();
        editPassword.clear();
        editRole.setValue(null);
        editRank.setValue(null);
        editWins.setText("0");
        editLosses.setText("0");
        editActive.setSelected(true);
    }
    
    private void showEditDialog() {
        editDialog.setVisible(true);
    }
    
    private void closeEditDialog() {
        editDialog.setVisible(false);
        editingUser = null;
    }
    
    private void updateActionButtons() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        boolean hasSelection = selected != null;
        
        editUserButton.setDisable(!hasSelection);
        deleteUserButton.setDisable(!hasSelection);
    }
    
    private void showStatus(String message, String type) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            statusLabel.getStyleClass().removeAll("status-success", "status-error", "status-info");
            statusLabel.getStyleClass().add("status-" + type);
        }
    }
}
