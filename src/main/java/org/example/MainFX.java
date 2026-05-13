package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        navigateTo("LoginView.fxml", "⚔  TournoiManager — Connexion");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    /** Switch the main window to any FXML view. */
    public static void navigateTo(String fxmlFile, String title) throws Exception {
        Parent root = FXMLLoader.load(
                MainFX.class.getResource("/" + fxmlFile)
        );
        Scene scene = new Scene(root, 1200, 750);
        primaryStage.setTitle(title);
        primaryStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}