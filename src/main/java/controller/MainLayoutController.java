package controller;

import entity.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import utilies.Session;

import java.io.IOException;
import java.net.URL;

public class MainLayoutController {
    @FXML
    private StackPane contentArea;

    @FXML
    private VBox menuContainer;

    @FXML
    public void initialize() {
        // Check if current user is admin and add admin button
        User currentUser = Session.getUser();
        if (currentUser != null && currentUser.isAdmin()) {
            addAdminButton();
        }
    }

    private void addAdminButton() {
        Button adminButton = new Button("🛡️ Admin Dashboard");
        adminButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px; -fx-background-color: #ff6b6b; -fx-text-fill: white;");
        adminButton.setMaxWidth(Double.MAX_VALUE);
        adminButton.setOnAction(this::loadAdminDashboard);

        // Insert after Dashboard button (index 1)
        if (menuContainer.getChildren().size() > 1) {
            menuContainer.getChildren().add(2, adminButton);
        } else {
            menuContainer.getChildren().add(adminButton);
        }
    }

    private void loadPage(String fxml) {
        try {
            Node page = FXMLLoader.load(getClass().getResource("/view/" + fxml));
            contentArea.getChildren().setAll(page);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void loadHome(ActionEvent actionEvent) {
    }


    public void loadTournaments(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/FrontOfficeView.fxml"));
            Parent  loadTournaments= loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(loadTournaments);

        } catch (IOException e) {
            System.err.println("Error loading admin dashboard: " + e.getMessage());
            e.printStackTrace();
        }

    }

    public void loadDashboard(ActionEvent actionEvent) {
    }

    public void loadTeams(ActionEvent actionEvent) {
    }

    public void loadMatches(ActionEvent actionEvent) {
    }

    public void loadStats(ActionEvent actionEvent) {
    }

    public void loadForum(ActionEvent actionEvent) {
        loadPage("forum.fxml");
    }

    public void loadProfile(ActionEvent actionEvent) {
        loadPage("profile.fxml");
    }

    public void loadAdminDashboard(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/admin_dashboard.fxml"));
            Parent adminDashboard = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(adminDashboard);

        } catch (IOException e) {
            System.err.println("Error loading admin dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @FXML
    private void handleLogout(ActionEvent event) {

        try {
            Session.setUser(null);

            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);

            URL cssUrl = getClass().getResource("/css/style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.err.println("CSS introuvable: /css/style.css");
            }
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
/*
    public void loadBracket(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/BracketView.fxml"));
            Parent bracketView = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(bracketView);

        } catch (IOException e) {
            System.err.println("Error loading bracket: " + e.getMessage());
            e.printStackTrace();
        }
    }
/*
    public void loadLeaderBoardt(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LeaderboardView.fxml"));
            Parent bracketView = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(bracketView);

        } catch (IOException e) {
            System.err.println("Error loading bracket: " + e.getMessage());
            e.printStackTrace();
        }
    }*/

    public void loadCenter(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/FrontOfficeCentreView.fxml"));
            Parent bracketView = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(bracketView);

        } catch (IOException e) {
            System.err.println("Error loading bracket: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
