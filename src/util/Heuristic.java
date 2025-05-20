package util;

import model.Board;

public class Heuristic {
    // First Heuristic
    public static int blockingCarsCount(Board board) {
        int row = 2; // baris utama
        int count = 0;
        boolean started = false;
        for (int j = 0; j < board.cols; j++) {
            char c = board.grid[row][j];
            if (c == 'P') started = true;
            else if (started && c != '.' && c != 'K') count++;
        }
        return count;
    }

    // Second Heuristic
    public static int blockingCarsWithMovability(Board board) {
        int row = 2;
        int pEndCol = -1;
        for (int j = 0; j < board.cols; j++) {
            if (board.grid[row][j] == 'P') pEndCol = j;
        }

        int score = 0;
        for (int j = pEndCol + 1; j < board.cols; j++) {
            char block = board.grid[row][j];
            if (block != '.' && block != 'K') {
                score += 1;
                if (!canMoveVertically(board, block)) {
                    score += 2;
                }
            }
        }

        return score;
    }

    private static boolean canMoveVertically(Board board, char vehicle) {
        for (int i = 0; i < board.rows; i++) {
            for (int j = 0; j < board.cols; j++) {
                if (board.grid[i][j] == vehicle) {
                    boolean up = i > 0 && board.grid[i - 1][j] == '.';
                    boolean down = i < board.rows - 1 && board.grid[i + 1][j] == '.';
                    return up || down;
                }
            }
        }
        return false;
    }

    // Third Heuristic
    public static int distanceToExit(Board board) {
        for (int i = 0; i < board.rows; i++) {
            for (int j = 0; j < board.cols; j++) {
                if (board.grid[i][j] == 'P') {
                    for (int k = j + 1; k < board.cols; k++) {
                        if (board.grid[i][k] == 'K') {
                            return k - j;
                        }
                    }
                }
            }
        }
        return Integer.MAX_VALUE;
    }

    public static int evaluate(Board board, String name) {
        return switch (name) {
            case "Blocking Heuristic" -> blockingCarsCount(board);
            case "Mobility Heuristic" -> blockingCarsWithMovability(board);
            case "Distance-to-Exit Heuristic" -> distanceToExit(board);
            default -> blockingCarsCount(board);
        };
    }
}
