package utilies;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class MainApp extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        URL fxmlUrl = getClass().getResource("/view/login.fxml");
        if (fxmlUrl == null) {
            throw new IllegalStateException("Fichier FXML introuvable: /view/sign_up.fxml");
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Scene scene = new Scene(loader.load());

        URL cssUrl = getClass().getResource("/css/style.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS introuvable: /css/style.css");
        }

        stage.setScene(scene);
        stage.setTitle("Login App");
        stage.show();
    }
    public static void navigateTo(String fxml, String title) {
        try {
            URL resource = MainApp.class.getResource(fxml);

            if (resource == null) {
                throw new RuntimeException("FXML file not found: " + fxml);
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();

            primaryStage.setTitle(title);
            primaryStage.setScene(new Scene(root));
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        launch();
    }
}
