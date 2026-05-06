package utilies;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        URL fxmlUrl = getClass().getResource("/view/login.fxml");
        if (fxmlUrl == null) {
            throw new IllegalStateException("Fichier FXML introuvable: /view/login.fxml");
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

    public static void main(String[] args) {
        launch();
    }
}
