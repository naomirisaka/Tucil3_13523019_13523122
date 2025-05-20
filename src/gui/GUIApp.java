package gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Board;
import util.InputParser;
import algo.*;

public class GUIApp extends Application {
    private List<Board> solution;
    private int currentStep = 0;
    private Timeline animationTimeline;
    private Solver solver;
    private final Map<Character, Color> pieceColors = new HashMap<>();

    private final GridPane boardGrid = new GridPane();
    private final Label stepLabel = new Label("Step 0 / 0");
    private final TextArea outputArea = new TextArea();
    private VBox rightPanel = new VBox(10);
    private final Label nodeVisitedLabel;  
    private final Label execTimeLabel = new Label();
    private final Label movementBlock = new Label();
    private final Label statusLabel = new Label();

    public GUIApp() {
        this.nodeVisitedLabel = new Label();
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Rush Hour Puzzle Solver");

        initializeColorPalette();

        // title 
        Font titleFont = Font.loadFont(getClass().getResourceAsStream("/fonts/StayPlayful.ttf"), 32);
        Label title = new Label("Rush Hour Puzzle Solver");
        title.setFont(titleFont);
        HBox titleBox = new HBox(title);
        titleBox.setAlignment(Pos.CENTER);

        // input file
        Label fileLabel = new Label("Select input configuration file:");
        Button fileButton = new Button("Browse...");
        Label selectedFileLabel = new Label("No file selected.");
        HBox inputFileBox = new HBox(10, fileButton, selectedFileLabel); 
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));

        // algorithm selection
        Label algoLabel = new Label("Choose the algorithm:");
        ComboBox<String> algoComboBox = new ComboBox<>();
        algoComboBox.getItems().addAll("A*", "UCS", "GBFS", "IDS");
        algoComboBox.setValue("A*");

        // heuristic selection
        Label heuristicLabel = new Label("Heuristic:");
        ComboBox<String> heuristicComboBox = new ComboBox<>();
        heuristicComboBox.getItems().addAll("Blocking Pieces Count", "Blocking Pieces With Movability", "Distance-to-Exit");
        heuristicComboBox.setValue("Blocking Pieces Count");

        // ids depth input
        Label depthLabel = new Label("IDS Max Depth:");
        TextField depthInput = new TextField("20");
        depthLabel.setVisible(false);
        depthInput.setVisible(false);

        // output format selection
        Label outputTypeLabel = new Label("Output:");
        ComboBox<String> outputTypeComboBox = new ComboBox<>();
        outputTypeComboBox.getItems().addAll("Pagination", "Animation");
        outputTypeComboBox.setValue("Pagination");

        // animation delay input
        Label delayLabel = new Label("Animation delay (ms):");
        TextField delayInput = new TextField("500");
        delayLabel.setVisible(false);
        delayInput.setVisible(false);

        Button solveButton = new Button("Solve");
        Button exportButton = new Button("Save Solution");

        // controls
        Button prevButton = new Button("Previous");
        Button nextButton = new Button("Next");
        Button finalButton = new Button("Final");

        prevButton.setVisible(false);
        nextButton.setVisible(false);
        finalButton.setVisible(false);
        stepLabel.setVisible(false);
        exportButton.setVisible(false);

        // input panel
        VBox inputPanel = new VBox(10);
        inputPanel.getChildren().addAll(
            fileLabel, inputFileBox,algoLabel, algoComboBox,
            depthLabel, depthInput,
            heuristicLabel, heuristicComboBox,
            outputTypeLabel, outputTypeComboBox,
            delayLabel, delayInput,
            solveButton, exportButton
        );
        inputPanel.setPadding(new Insets(10));

        outputArea.setPrefWidth(300);
        outputArea.setEditable(false);
        outputArea.setStyle("-fx-font-family: monospace;");

        HBox controls = new HBox(10, prevButton, stepLabel, nextButton, finalButton);

        ToggleButton toggleLogButton = new ToggleButton("Show Logs");
        toggleLogButton.setOnAction(ev -> {
            if (toggleLogButton.isSelected()) {
                toggleLogButton.setText("Hide Logs");
                if (!rightPanel.getChildren().contains(outputArea)) {
                    rightPanel.getChildren().add(outputArea);
                }
            } else {
                toggleLogButton.setText("Show Logs");
                rightPanel.getChildren().remove(outputArea);
            }
        });

        // right panel
        rightPanel.getChildren().addAll(
            new Label("Board:"),
            boardGrid,
            controls,
            movementBlock,
            nodeVisitedLabel,
            execTimeLabel,
            statusLabel,
            toggleLogButton
        ); 
        rightPanel.setPadding(new Insets(10));

        ScrollPane rightScrollPane = new ScrollPane(rightPanel);
        HBox.setHgrow(rightScrollPane, Priority.ALWAYS);
        rightScrollPane.setMaxWidth(Double.MAX_VALUE);
        rightScrollPane.setFitToWidth(true); 
        rightScrollPane.setPrefViewportHeight(600);

        // main panel
        HBox mainPanels = new HBox(20, inputPanel, rightScrollPane);

        VBox root = new VBox(10, titleBox, mainPanels);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        final Board[] board = new Board[1];

        fileButton.setOnAction(e -> {
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                selectedFileLabel.setText(file.getName());
                stepLabel.setVisible(false);
                prevButton.setVisible(false);
                nextButton.setVisible(false);
                finalButton.setVisible(false);
                movementBlock.setVisible(false);
                exportButton.setVisible(false);
                nodeVisitedLabel.setVisible(false);
                execTimeLabel.setVisible(false);
                try {
                    board[0] = InputParser.parse(file);
                    outputArea.setText("File loaded successfully.\n");
                    statusLabel.setText("");
                    drawBoard(board[0]);
                    solveButton.setDisable(false);
                } catch (FileNotFoundException ex) {
                    boardGrid.getChildren().clear();
                    statusLabel.setVisible(true);
                    outputArea.setText("Failed to read the file: " + ex.getMessage());
                    statusLabel.setText("Failed to read the file: " + ex.getMessage());
                    solveButton.setDisable(true);
                } catch (IllegalArgumentException ex) {
                    boardGrid.getChildren().clear();
                    statusLabel.setVisible(true);
                    outputArea.setText("Error: " + ex.getMessage());
                    statusLabel.setText("Error: " + ex.getMessage());
                    solveButton.setDisable(true);
                }
            }
        });

        algoComboBox.setOnAction(e -> {
            String selectedAlgo = algoComboBox.getValue();
            heuristicComboBox.setDisable(!selectedAlgo.equals("A*") && !selectedAlgo.equals("GBFS"));
            depthLabel.setVisible(selectedAlgo.equals("IDS"));
            depthInput.setVisible(selectedAlgo.equals("IDS"));
        });

        outputTypeComboBox.setOnAction(e -> {
            boolean isAnimation = outputTypeComboBox.getValue().equals("Animation");
            delayLabel.setVisible(isAnimation);
            delayInput.setVisible(isAnimation);
        });

        solveButton.setOnAction(e -> {
            if (board[0] == null) {
                outputArea.setText("Please load a puzzle file.");
                return;
            }

            switch (algoComboBox.getValue()) {
                case "A*" -> solver = new AStarSolver(heuristicComboBox.getValue());
                case "UCS" -> solver = new UCSSolver();
                case "GBFS" -> solver = new GBFSSolver(heuristicComboBox.getValue());
                case "IDS" -> {
                    try {
                        int maxDepth = Integer.parseInt(depthInput.getText());
                        solver = new IDSSolver(maxDepth);
                    } catch (NumberFormatException ex) {
                        outputArea.setText("Invalid IDS max depth.");
                        statusLabel.setText("Invalid IDS max depth.");
                        return;
                    }
                }
                default -> {
                    outputArea.setText("Unknown algorithm.");
                    return;
                }
            }

            solution = solver.solve(board[0]);
            if (solution.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("No Solution");
                alert.setHeaderText(null);
                alert.setContentText("No solution was found for this puzzle.");
                alert.showAndWait();
                nodeVisitedLabel.setText("");
                execTimeLabel.setText("");
                stepLabel.setVisible(false);
                movementBlock.setVisible(false);
                prevButton.setVisible(false);
                nextButton.setVisible(false);
                finalButton.setVisible(false);
                exportButton.setVisible(false);
                statusLabel.setVisible(false);
                return;
            }

            currentStep = 0;
            stepLabel.setVisible(true);
            prevButton.setVisible(true);
            nextButton.setVisible(true);
            finalButton.setVisible(true);
            exportButton.setVisible(true);
            movementBlock.setVisible(true);
            nodeVisitedLabel.setVisible(true);
            execTimeLabel.setVisible(true);
            statusLabel.setVisible(true);

            String mode = outputTypeComboBox.getValue();
            if (mode.equals("Pagination")) {
                displayStep(currentStep);
            } else {
                int delay = Integer.parseInt(delayInput.getText());
                animationTimeline = new Timeline(new KeyFrame(Duration.millis(delay), ev -> {
                    if (currentStep < solution.size()) {
                        displayStep(currentStep);

                        Board current = solution.get(currentStep);
                        if (current.move != null && current.move.equals("Primary piece exits through K")) {
                            currentStep = solution.size() - 1;
                            displayStep(currentStep);
                            animationTimeline.stop();
                        } else {
                            currentStep++;
                        }
                    } else {
                        animationTimeline.stop();
                    }
                }));
                animationTimeline.setCycleCount(Timeline.INDEFINITE);
                animationTimeline.play();
            }
        });

        prevButton.setOnAction(e -> {
            if (currentStep > 0) displayStep(--currentStep);
        });

        nextButton.setOnAction(e -> {
            if (currentStep < solution.size() - 1) {
                Board current = solution.get(currentStep);
                if (current.move != null && current.move.equals("Primary piece exits through K")) {
                    currentStep = solution.size() - 1;
                } else {
                    currentStep++;
                }
                displayStep(currentStep);
            }
        });

        finalButton.setOnAction(e -> {
            currentStep = solution.size() - 1;
            displayStep(currentStep);
        });

        exportButton.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Export Solution");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
            chooser.setInitialFileName("solution.txt"); 

            File file = chooser.showSaveDialog(primaryStage);
            if (file != null) {
                if (!file.getName().toLowerCase().endsWith(".txt")) {
                    file = new File(file.getParentFile(), file.getName() + ".txt");
                }

                try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                    for (int i = 0; i < solution.size(); i++) {
                        Board current = solution.get(i);
                        if (i == 0) {
                            writer.println("Initial state");
                            writer.println("Step " + i + ":");
                        } else {
                            writer.println("Step " + i + ":");
                            String move = current.move;
                            if (move != null) {
                                String[] parts = move.split(" ");
                                if (parts.length == 3) {
                                    char piece = parts[1].charAt(0);
                                    String direction = parts[2];
                                    writer.println("Block " + piece + " moved " + direction);
                                }
                            }
                        }

                        writer.println(current.toString());
                        writer.println();
                    }

                    writer.println("Nodes Visited: " + solver.getVisitedNodeCount());
                    writer.println("Execution Time: " + solver.getExecutionTime() + " ms");

                    outputArea.appendText("\nExported to " + file.getName());
                    statusLabel.setText("Exported to " + file.getName());
                } catch (Exception ex) {
                    outputArea.appendText("\nFailed to export: " + ex.getMessage());
                    statusLabel.setText("Failed to export: " + ex.getMessage());
                }
            }
        });
    }

    private void initializeColorPalette() {
        List<Color> palette = List.of(
            Color.web("#e6194b"), // red (for P)
            Color.web("#3cb44b"), // green (for K)
            Color.web("#ffe119"), // yellow
            Color.web("#4363d8"), // blue
            Color.web("#f58231"), // orange
            Color.web("#911eb4"), // purple
            Color.web("#46f0f0"), // cyan
            Color.web("#f032e6"), // magenta
            Color.web("#bcf60c"), // lime
            Color.web("#fabebe"), // pink
            Color.web("#008080"), // teal
            Color.web("#e6beff"), // lavender
            Color.web("#9a6324"), // brown
            Color.web("#fffac8"), // beige
            Color.web("#800000"), // maroon
            Color.web("#aaffc3"), // mint
            Color.web("#808000"), // olive
            Color.web("#ffd8b1"), // coral
            Color.web("#000075"), // navy
            Color.web("#808080")  // gray
        );

        int paletteIndex = 0;
        for (char c = 'A'; c <= 'Z'; c++) {
            if (c != 'P' && c != 'K') {
                pieceColors.put(c, palette.get(paletteIndex % palette.size()));
                paletteIndex++;
            }
        }
        pieceColors.put('P', Color.RED);
        pieceColors.put('K', Color.GREEN);
    }

    private void drawBoard(Board board) {
        boardGrid.getChildren().clear();
        boardGrid.setGridLinesVisible(true);
        char[][] grid = board.getGrid();
        int rows = board.getRows(), cols = board.getCols();

        boolean shiftRight = board.getExitCol() == -1;
        boolean shiftDown = board.getExitRow() == -1;

        boardGrid.getChildren().clear();
        boardGrid.getColumnConstraints().clear();
        boardGrid.getRowConstraints().clear();

        for (int j = 0; j < cols + (shiftRight ? 1 : 0); j++) {
            ColumnConstraints col = new ColumnConstraints(50);
            boardGrid.getColumnConstraints().add(col);
        }

        for (int i = 0; i < rows + (shiftDown ? 1 : 0); i++) {
            RowConstraints row = new RowConstraints(50);
            boardGrid.getRowConstraints().add(row);
        }

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                char ch = grid[i][j];
                StackPane cell = new StackPane();
                cell.setPrefSize(50, 50);
                cell.setStyle("-fx-border-color: black;");
                if (ch != '.') {
                    if (ch != 'P' || !board.isGoal()) {
                        Color color = pieceColors.getOrDefault(ch, Color.GREY);
                        cell.setBackground(new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY)));
                        Text label = new Text(String.valueOf(ch));
                        label.setFont(Font.font(20));
                        label.setFill(Color.BLACK);
                        cell.getChildren().add(label);
                    }
                }
                boardGrid.add(cell, j + (shiftRight ? 1 : 0), i + (shiftDown ? 1 : 0));
            }
        }

        int exitRow = board.getExitRow();
        int exitCol = board.getExitCol();

        if (exitRow >= -1 && exitRow <= rows && exitCol >= -1 && exitCol <= cols) {
            StackPane exitCell = new StackPane();
            exitCell.setPrefSize(50, 50);
            exitCell.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));

            BorderWidths borderWidths;

            if (exitRow == -1) {
                borderWidths = new BorderWidths(1, 1, 0, 1);
            } else if (exitRow == rows) {
                borderWidths = new BorderWidths(0, 1, 1, 1);
            } else if (exitCol == -1) {
                borderWidths = new BorderWidths(1, 0, 1, 1);
            } else if (exitCol == cols) {
                borderWidths = new BorderWidths(1, 1, 1, 0);
            } else {
                borderWidths = new BorderWidths(0);
            }

            exitCell.setBorder(new Border(new BorderStroke(
                Color.BLACK,
                BorderStrokeStyle.SOLID,
                CornerRadii.EMPTY,
                borderWidths
            )));

            Text label = new Text("K");
            label.setFont(Font.font(20));
            label.setFill(Color.BLACK);
            exitCell.getChildren().add(label);

            int gridRow = exitRow + (shiftDown ? 1 : 0);
            int gridCol = exitCol + (shiftRight ? 1 : 0);

            if (exitCol == cols) {
                if (boardGrid.getColumnConstraints().size() <= cols + (shiftRight ? 1 : 0)) {
                    boardGrid.getColumnConstraints().add(new ColumnConstraints(50));
                }
            } else if (exitCol == -1) {
                boardGrid.getColumnConstraints().add(0, new ColumnConstraints(50));
            }

            if (exitRow == rows) {
                if (boardGrid.getRowConstraints().size() <= rows + (shiftDown ? 1 : 0)) {
                    boardGrid.getRowConstraints().add(new RowConstraints(50));
                }
            } else if (exitRow == -1) {
                boardGrid.getRowConstraints().add(0, new RowConstraints(50));
            }

            boardGrid.add(exitCell, gridCol, gridRow);
        }
    }

    private void displayStep(int step) {
        Board current = solution.get(step);
        drawBoard(current);

        StringBuilder sb = new StringBuilder();
        sb.append("Step ").append(step).append(":\n");

        // default label text
        String movementText = "Initial state";

        // tampilkan info gerakan jika bukan board awal
        if (step > 0) {
            String move = current.move;
            if (move != null) {
                if (move.equals("Primary piece P exits through K")) {
                    sb.append("Primary piece P exited the board through the exit!\n");
                    movementText = "Primary piece P exited the board!";
                } else {
                    String[] parts = move.split(" ");
                    if (parts.length == 3) {
                        char piece = parts[1].charAt(0);
                        String direction = parts[2];

                        sb.append("Block ").append(piece).append(" moved ").append(direction).append("\n");
                        movementText = "Block " + piece + " moved " + direction;
                    }
                }
            }
        } else {
            sb.append("Initial state\n");
        }

        sb.append(current.toString());

        outputArea.setText(sb.toString());
        movementBlock.setText(movementText);
        stepLabel.setText("Step " + step + " / " + (solution.size() - 1));
        nodeVisitedLabel.setText("Nodes Visited: " + solver.getVisitedNodeCount());
        execTimeLabel.setText("Execution Time: " + solver.getExecutionTime() + " ms");
    }

    public static void main(String[] args) {
        launch(args);
    }

    public VBox getRightPanel() {
        return rightPanel;
    }

    public void setRightPanel(VBox rightPanel) {
        this.rightPanel = rightPanel;
    }
}