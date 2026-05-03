package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import utilies.Session;

import java.io.IOException;
import java.net.URL;

public class MainLayoutController {
    @FXML
    private StackPane contentArea;

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
