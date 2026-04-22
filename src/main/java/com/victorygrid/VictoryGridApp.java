package com.victorygrid;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class VictoryGridApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(VictoryGridApp.class.getResource("/com/victorygrid/login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 400, 600);
        scene.getStylesheets().add(getClass().getResource("/com/victorygrid/styles.css").toExternalForm());
        stage.setTitle("VictoryGrid");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
