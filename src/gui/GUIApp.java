package gui;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.control.*;

public class GUIApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Rush Hour Solver");

        Label label = new Label("Welcome to Rush Hour Solver");
        Button btn = new Button("Start Solving");

        VBox layout = new VBox(10, label, btn);
        Scene scene = new Scene(layout, 400, 200);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
