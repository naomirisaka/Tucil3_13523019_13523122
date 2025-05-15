package gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import algo.AStarSolver;
import algo.GreedyBFSSolver;
import algo.IDSSolver;
import algo.Solver;
import algo.UCSSolver;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Board;
import util.InputParser;

public class GUIApp extends Application {
    private TextArea outputArea = new TextArea();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Rush Hour Solver");

        Label fileLabel = new Label("Pilih file input konfigurasi:");
        Button fileButton = new Button("Browse...");
        Label selectedFileLabel = new Label("Belum ada file dipilih");
        FileChooser fileChooser = new FileChooser();

        Label algoLabel = new Label("Pilih algoritma:");
        ComboBox<String> algoComboBox = new ComboBox<>();
        algoComboBox.getItems().addAll("A*", "UCS", "GBFS", "IDS");
        algoComboBox.setValue("A*");

        Button solveButton = new Button("Jalankan Solver");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.getChildren().addAll(fileLabel, fileButton, selectedFileLabel, algoLabel, algoComboBox, solveButton, outputArea);

        outputArea.setEditable(false);
        outputArea.setStyle("-fx-font-family: monospace;");
        outputArea.setPrefHeight(400);

        Scene scene = new Scene(layout, 600, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        final Board[] board = new Board[1];

        fileButton.setOnAction((ActionEvent e) -> {
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                selectedFileLabel.setText(file.getName());
                try {
                    board[0] = InputParser.parse(file);
                    outputArea.setText("File berhasil dimuat.\n");
                    outputArea.appendText(board[0].toString());
                } catch (FileNotFoundException ex) {
                    outputArea.setText("Gagal membaca file: " + ex.getMessage() + "\n");
                    ex.printStackTrace();
                }
            }
        });

        solveButton.setOnAction(e -> {
            if (board[0] == null) {
                outputArea.setText("Silakan pilih file input terlebih dahulu.\n");
                return;
            }

            Solver solver;
            switch (algoComboBox.getValue()) {
                case "A*":
                    solver = new AStarSolver();
                    break;
                case "UCS":
                    solver = new UCSSolver();
                    break;
                case "GBFS":
                    solver = new GreedyBFSSolver();
                    break;
                case "IDS":
                    solver = new IDSSolver();
                    break;
                default:
                    outputArea.setText("Algoritma tidak dikenali.\n");
                    return;
            }

            List<Board> solution = solver.solve(board[0]);
            if (solution.isEmpty()) {
                outputArea.setText("Tidak ditemukan solusi.\n");
                return;
            }

            outputArea.clear();
            outputArea.appendText("Papan Awal:\n");
            outputArea.appendText(solution.get(0).toString() + "\n");

            for (int i = 1; i < solution.size(); i++) {
                outputArea.appendText("Gerakan " + i + ":\n");
                outputArea.appendText(solution.get(i).toString() + "\n");
            }

            outputArea.appendText("Node dikunjungi: " + solver.getVisitedNodeCount() + "\n");
            outputArea.appendText("Waktu eksekusi: " + solver.getExecutionTime() + " ms\n");
        });
    }

    public static void main(String[] args) {
        launch(args);
    }

    public TextArea getOutputArea() {
        return outputArea;
    }

    public void setOutputArea(TextArea outputArea) {
        this.outputArea = outputArea;
    }
}
