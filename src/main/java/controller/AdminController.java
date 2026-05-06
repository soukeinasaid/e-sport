package controller;

import entity.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import service.UserService;
import utilies.Session;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class AdminController {

    @FXML
    private Label totalUsersLabel;

    @FXML
    private Label totalAdminsLabel;

    @FXML
    private Label totalNormalUsersLabel;

    @FXML
    private Label recentUsersLabel;

    @FXML
    private Label welcomeLabel;

    @FXML
    private VBox contentArea;

    private UserService userService;

    @FXML
    public void initialize() {
        userService = new UserService();
        loadStatistics();
        setupWelcomeMessage();
    }

    private void setupWelcomeMessage() {
        User currentUser = Session.getUser();
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getPrenom() + " " + currentUser.getNom() + " (Admin)");
        }
    }

    private void loadStatistics() {
        try {
            List<User> allUsers = userService.getAll();
            
            int totalUsers = allUsers.size();
            int totalAdmins = (int) allUsers.stream().filter(User::isAdmin).count();
            int totalNormalUsers = totalUsers - totalAdmins;
            
            totalUsersLabel.setText(String.valueOf(totalUsers));
            totalAdminsLabel.setText(String.valueOf(totalAdmins));
            totalNormalUsersLabel.setText(String.valueOf(totalNormalUsers));
            
            // Count recent users (last 7 days - simplified for now)
            recentUsersLabel.setText(String.valueOf(allUsers.size()));
            
        } catch (Exception e) {
            System.err.println("Error loading statistics: " + e.getMessage());
            totalUsersLabel.setText("0");
            totalAdminsLabel.setText("0");
            totalNormalUsersLabel.setText("0");
            recentUsersLabel.setText("0");
        }
    }

    @FXML
    private void loadUserManagement(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/user_management.fxml"));
            Parent userManagementView = loader.load();
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(userManagementView);
            
        } catch (IOException e) {
            System.err.println("Error loading user management: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void loadForumManagement(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/forum.fxml"));
            Parent forumView = loader.load();
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(forumView);
            
        } catch (IOException e) {
            System.err.println("Error loading forum management: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void refreshStatistics(ActionEvent event) {
        loadStatistics();
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
            Session.setUser(null);

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
}
