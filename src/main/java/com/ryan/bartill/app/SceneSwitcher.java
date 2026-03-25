package com.ryan.bartill.app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneSwitcher {

    public static void switchTo(Stage stage, String fxml, String title) throws Exception {
        var loader = new FXMLLoader(SceneSwitcher.class.getResource(fxml));
        Parent root = loader.load();

        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(root);
            stage.setScene(scene);
        } else {
            scene.setRoot(root);
        }

        // Always ensure stylesheet is applied
        String css = SceneSwitcher.class.getResource("/css/dark-theme.css").toExternalForm();
        if (!scene.getStylesheets().contains(css)) {
            scene.getStylesheets().add(css);
        }

        stage.setTitle(title);

        // Disable the "Press ESC to exit full screen" message
        stage.setFullScreenExitHint("");
        stage.setFullScreenExitKeyCombination(null);

        // IMPORTANT: If we're going to main POS, force fullscreen/max
        if ("/fxml/main.fxml".equals(fxml)) {
            stage.setMaximized(true);
            stage.setFullScreen(true);
        }
    }
}