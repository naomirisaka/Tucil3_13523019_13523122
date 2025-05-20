package gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import algo.AStarSolver;
import algo.GreedyBFSSolver;
import algo.IDSSolver;
import algo.Solver;
import algo.UCSSolver;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Board;
import util.InputParser;

public class GUIApp extends Application {
    private TextArea outputArea = new TextArea();
    private int currentStep = 0;
    private List<Board> solution;
    private Timeline animationTimeline;
    private Solver solver;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Rush Hour Puzzle Solver");

        Label fileLabel = new Label("Select input configuration file:");
        Button fileButton = new Button("Browse ... ");
        Label selectedFileLabel = new Label("No file selected.");
        FileChooser fileChooser = new FileChooser();

        Label algoLabel = new Label("Choose the algorithm:");
        ComboBox<String> algoComboBox = new ComboBox<>();
        algoComboBox.getItems().addAll("A*", "UCS", "GBFS", "IDS");
        algoComboBox.setValue("A*");

        Label depthLabel = new Label("IDS Max Depth:");
        depthLabel.setVisible(false);
        TextField depthInput = new TextField("20");
        depthInput.setMaxWidth(100);
        depthInput.setVisible(false);

        Label heuristicLabel = new Label("Choose the heuristic:");
        ComboBox<String> heuristicComboBox = new ComboBox<>();
        heuristicComboBox.getItems().addAll("Blocking Heuristic", "Mobility Heuristic", "Distance-to-Exit Heuristic");
        heuristicComboBox.setValue("Blocking Heuristic");

        Label outputTypeLabel = new Label("Choose output mode:");
        ComboBox<String> outputTypeComboBox = new ComboBox<>();
        outputTypeComboBox.getItems().addAll("Pagination", "Animation");
        outputTypeComboBox.setValue("Pagination");

        Label delayLabel = new Label("Animation delay (ms):");
        delayLabel.setVisible(false);
        TextField delayInput = new TextField("500");
        delayInput.setMaxWidth(100);
        delayInput.setVisible(false);

        Button solveButton = new Button("Solve");

        // Pagination controls
        Button prevButton = new Button("Previous Step");
        Button nextButton = new Button("Next Step");
        Button finalButton = new Button("Final Step");
        Label stepLabel = new Label("Step 0 / 0");
        prevButton.setVisible(false);
        nextButton.setVisible(false);
        finalButton.setVisible(false);
        stepLabel.setVisible(false);

        algoComboBox.setOnAction(event -> {
            boolean isIDS = algoComboBox.getValue().equals("IDS");
            heuristicComboBox.setDisable(isIDS);
            depthLabel.setVisible(isIDS);
            depthInput.setVisible(isIDS);
        });

        outputTypeComboBox.setOnAction(event -> {
            boolean isAnimation = outputTypeComboBox.getValue().equals("Animation");
            delayLabel.setVisible(isAnimation);
            delayInput.setVisible(isAnimation);
        });

        HBox root = new HBox(20);
        root.setPadding(new Insets(20));

        VBox inputPanel = new VBox(10);
        inputPanel.getChildren().addAll(
            fileLabel, fileButton, selectedFileLabel,
            algoLabel, algoComboBox,
            depthLabel, depthInput,
            heuristicLabel, heuristicComboBox,
            outputTypeLabel, outputTypeComboBox,
            delayLabel, delayInput,
            solveButton
        );

        outputArea.setEditable(false);
        outputArea.setStyle("-fx-font-family: monospace;");
        outputArea.setPrefHeight(600);
        outputArea.setPrefWidth(400);

        VBox outputPanel = new VBox(10);
        Label outputLabel = new Label("Solver Output:");
        HBox paginationControls = new HBox(10);
        paginationControls.getChildren().addAll(prevButton, stepLabel, nextButton, finalButton);
        outputPanel.getChildren().addAll(outputLabel, outputArea, paginationControls);

        root.getChildren().addAll(inputPanel, outputPanel);

        Scene scene = new Scene(root, 900, 650);
        primaryStage.setScene(scene);
        primaryStage.show();

        final Board[] board = new Board[1];

        fileButton.setOnAction((ActionEvent e) -> {
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                selectedFileLabel.setText(file.getName());
                try {
                    board[0] = InputParser.parse(file);
                    outputArea.setText("File loaded successfully.\n");
                    outputArea.appendText(board[0].toString());
                } catch (FileNotFoundException ex) {
                    outputArea.setText("Failed to read the file: " + ex.getMessage() + "\n");
                }
            }
        });

        solveButton.setOnAction(e -> {
            if (board[0] == null) {
                outputArea.setText("Please select an input file first.\n");
                return;
            }

            String selectedAlgorithm = algoComboBox.getValue();
            String selectedHeuristic = heuristicComboBox.getValue();

            if (selectedAlgorithm.equals("A*")) {
                solver = new AStarSolver(selectedHeuristic);
            } else if (selectedAlgorithm.equals("UCS")) {
                solver = new UCSSolver();
            } else if (selectedAlgorithm.equals("GBFS")) {
                solver = new GreedyBFSSolver(selectedHeuristic);
            } else if (selectedAlgorithm.equals("IDS")) {
                int maxDepth;
                try {
                    maxDepth = Integer.parseInt(depthInput.getText());
                } catch (NumberFormatException ex) {
                    outputArea.setText("Invalid max depth for IDS.\n");
                    return;
                }
                solver = new IDSSolver(maxDepth);
            } else {
                outputArea.setText("Unknown algorithm selected.\n");
                return;
            }

            solution = solver.solve(board[0]);
            if (solution.isEmpty()) {
                outputArea.setText("No solution found.\n");
                return;
            }

            currentStep = 0;
            String mode = outputTypeComboBox.getValue();
            outputArea.clear();

            if (mode.equals("Pagination")) {
                prevButton.setVisible(true);
                nextButton.setVisible(true);
                finalButton.setVisible(true);
                stepLabel.setVisible(true);
                displayStep(currentStep, stepLabel);
            } else if (mode.equals("Animation")) {
                int delay = Integer.parseInt(delayInput.getText());
                animationTimeline = new Timeline(
                    new KeyFrame(Duration.millis(delay), ev -> {
                        if (currentStep < solution.size()) {
                            displayStep(currentStep, stepLabel);
                            currentStep++;
                        } else {
                            animationTimeline.stop();
                        }
                    })
                );
                animationTimeline.setCycleCount(Timeline.INDEFINITE);
                animationTimeline.play();
            }
        });

        prevButton.setOnAction(e -> {
            if (currentStep > 0) {
                currentStep--;
                displayStep(currentStep, stepLabel);
            }
        });

        nextButton.setOnAction(e -> {
            if (currentStep < solution.size() - 1) {
                currentStep++;
                displayStep(currentStep, stepLabel);
            }
        });

        finalButton.setOnAction(e -> {
            currentStep = solution.size() - 1;
            displayStep(currentStep, stepLabel);
        });
    }

    private void displayStep(int step, Label stepLabel) {
        outputArea.clear();
        outputArea.appendText("Step " + step + ":\n");
        outputArea.appendText(solution.get(step).toString());
        stepLabel.setText("Step " + (step + 1) + " / " + solution.size());

        if (step == solution.size() - 1 && solver != null) {
            outputArea.appendText("\nNodes Visited: " + solver.getVisitedNodeCount() + "\n");
            outputArea.appendText("Execution Time: " + solver.getExecutionTime() + " ms\n");
        }
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
