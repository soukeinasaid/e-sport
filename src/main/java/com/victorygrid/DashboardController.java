package com.victorygrid;

import com.victorygrid.model.User;
import com.victorygrid.service.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class DashboardController {
    
    @FXML private Button closeButton;
    @FXML private Button homeButton;
    @FXML private Button dashboardButton;
    @FXML private Button tournamentsButton;
    @FXML private Button teamsButton;
    @FXML private Button matchesButton;
    @FXML private Button statsButton;
    @FXML private Button profileButton;
    @FXML private Button adminButton;
    
    @FXML private ImageView userAvatar;
    @FXML private Text usernameLabel;
    @FXML private Text rankLabel;
    
    @FXML private StackPane contentPane;
    
    private User currentUser;
    private UserService userService;
    
    @FXML
    public void initialize() {
        userService = UserService.getInstance();
        
        // Set up button event handlers
        closeButton.setOnAction(e -> handleClose());
        homeButton.setOnAction(e -> loadView("home"));
        dashboardButton.setOnAction(e -> loadView("dashboard"));
        tournamentsButton.setOnAction(e -> loadView("tournaments"));
        teamsButton.setOnAction(e -> loadView("teams"));
        matchesButton.setOnAction(e -> loadView("matches"));
        statsButton.setOnAction(e -> loadView("stats"));
        profileButton.setOnAction(e -> loadView("profile"));
        adminButton.setOnAction(e -> loadView("admin"));
        
        // Load default view (profile)
        loadView("profile");
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateUserInterface();
    }
    
    private void updateUserInterface() {
        if (currentUser != null) {
            usernameLabel.setText(currentUser.getUsername());
            rankLabel.setText(currentUser.getRank() + " Rank");
            
            // Show admin button only for admin users
            adminButton.setVisible("ADMIN".equals(currentUser.getRole()));
            
            // Update button styles
            updateNavButtonStyles();
        }
    }
    
    private void updateNavButtonStyles() {
        // Reset all button styles
        homeButton.getStyleClass().remove("selected");
        dashboardButton.getStyleClass().remove("selected");
        tournamentsButton.getStyleClass().remove("selected");
        teamsButton.getStyleClass().remove("selected");
        matchesButton.getStyleClass().remove("selected");
        statsButton.getStyleClass().remove("selected");
        profileButton.getStyleClass().remove("selected");
        adminButton.getStyleClass().remove("selected");
    }
    
    private void loadView(String viewName) {
        try {
            String fxmlPath = "/com/victorygrid/" + viewName + "-view.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();
            
            // Pass current user to the controller if it supports it
            Object controller = loader.getController();
            if (controller instanceof ViewController) {
                ((ViewController) controller).setCurrentUser(currentUser);
            }
            
            contentPane.getChildren().clear();
            contentPane.getChildren().add(view);
            
            // Update selected button style
            updateNavButtonStyles();
            switch (viewName) {
                case "home" -> homeButton.getStyleClass().add("selected");
                case "dashboard" -> dashboardButton.getStyleClass().add("selected");
                case "tournaments" -> tournamentsButton.getStyleClass().add("selected");
                case "teams" -> teamsButton.getStyleClass().add("selected");
                case "matches" -> matchesButton.getStyleClass().add("selected");
                case "stats" -> statsButton.getStyleClass().add("selected");
                case "profile" -> profileButton.getStyleClass().add("selected");
                case "admin" -> adminButton.getStyleClass().add("selected");
            }
            
        } catch (IOException e) {
            System.err.println("Error loading view: " + viewName);
            e.printStackTrace();
            
            // Load error view or show message
            loadErrorView(viewName);
        }
    }
    
    private void loadErrorView(String viewName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/victorygrid/controller/error-view.fxml"));
            Node errorView = loader.load();
            
            // Pass error information to error controller
            Object controller = loader.getController();
            if (controller instanceof ErrorViewController) {
                ((ErrorViewController) controller).setError("Could not load view: " + viewName);
            }
            
            contentPane.getChildren().clear();
            contentPane.getChildren().add(errorView);
        } catch (IOException e) {
            System.err.println("Error loading error view: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void handleClose() {
        // Handle logout or close application
        try {
            // Show confirmation dialog
            showLogoutConfirmation();
        } catch (Exception e) {
            System.err.println("Error handling close: " + e.getMessage());
            // Force close if confirmation fails
            System.exit(0);
        }
    }
    
    private void showLogoutConfirmation() {
        // For now, just exit. In production, show confirmation dialog
        System.exit(0);
    }
    
    // Interface for view controllers
    public interface ViewController {
        void setCurrentUser(User user);
    }
    
    // Simple error controller interface
    public interface ErrorViewController {
        void setError(String error);
    }
}
