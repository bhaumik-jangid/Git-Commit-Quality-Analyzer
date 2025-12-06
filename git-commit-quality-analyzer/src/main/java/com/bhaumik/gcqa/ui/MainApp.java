package com.bhaumik.gcqa.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        Label label = new Label("Git Commit Quality Analyzer - UI coming soon...");
        Scene scene = new Scene(label, 600, 400);
        stage.setTitle("Git Commit Quality Analyzer");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

