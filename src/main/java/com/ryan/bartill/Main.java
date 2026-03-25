package com.ryan.bartill;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(
                getClass().getResource("/fxml/login.fxml")
        );

        Scene scene = new Scene(root);

        scene.getStylesheets().add(
                getClass().getResource("/css/dark-theme.css").toExternalForm()
        );

        stage.setFullScreenExitHint("");
        stage.setFullScreen(true);
        stage.setMaximized(true);
        stage.setTitle("Bar Till System");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}