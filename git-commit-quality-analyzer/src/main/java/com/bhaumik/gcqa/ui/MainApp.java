// Entry point for the JavaFX application

package com.bhaumik.gcqa.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/bhaumik/gcqa/ui/main-view.fxml")
        );
        Scene scene = new Scene(loader.load(), 900, 500);
        scene.getStylesheets().add(
            getClass().getResource("/com/bhaumik/gcqa/ui/style.css").toExternalForm()
        );
        stage.setTitle("Git Commit Quality Analyzer");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
