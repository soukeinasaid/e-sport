package controller;

import entity.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import utilies.Session;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;

public class MainLayoutController {
    @FXML
    private StackPane contentArea;
    
    @FXML
    private VBox menuContainer;

    @FXML
    private ImageView sidebarProfilePicture;

    @FXML
    private Label sidebarProfilePlaceholder;

    @FXML
    private Label sidebarUserName;

    @FXML
    public void initialize() {
        // Load user profile picture and name
        loadUserProfile();
        
        // Check if current user is admin and add admin button
        User currentUser = Session.getUser();
        if (currentUser != null && currentUser.isAdmin()) {
            addAdminButton();
        }
    }

    private void loadUserProfile() {
        User currentUser = Session.getUser();
        if (currentUser != null) {
            // Set user name
            sidebarUserName.setText(currentUser.getPrenom() + " " + currentUser.getNom());
            
            // Load profile picture if available
            String profilePictureBase64 = currentUser.getProfilePicture();
            if (profilePictureBase64 != null && !profilePictureBase64.isEmpty()) {
                try {
                    byte[] imageBytes = Base64.getDecoder().decode(profilePictureBase64);
                    ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
                    Image image = new Image(bis);
                    sidebarProfilePicture.setImage(image);
                    sidebarProfilePlaceholder.setVisible(false);
                } catch (Exception e) {
                    System.err.println("Error loading profile picture: " + e.getMessage());
                    sidebarProfilePlaceholder.setVisible(true);
                }
            } else {
                sidebarProfilePlaceholder.setVisible(true);
            }
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
}
