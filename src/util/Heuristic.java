package util;

import model.Board;

public class Heuristic {
    // First Heuristic
    public static int blockingCarsCount(Board board) {
        int row = 2; // baris utama
        int count = 0;
        boolean started = false;
        for (int j = 0; j < board.getCols(); j++) {
            char c = board.getGrid()[row][j];
            if (c == 'P') started = true;
            else if (started && c != '.' && c != 'K') count++;
        }
        return count;
    }

    // Second Heuristic
    public static int blockingCarsWithMovability(Board board) {
        int row = 2;
        int pEndCol = -1;
        for (int j = 0; j < board.getCols(); j++) {
            if (board.getGrid()[row][j] == 'P') pEndCol = j;
        }

        int score = 0;
        for (int j = pEndCol + 1; j < board.getCols(); j++) {
            char block = board.getGrid()[row][j];
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
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getCols(); j++) {
                if (board.getGrid()[i][j] == vehicle) {
                    boolean up = i > 0 && board.getGrid()[i - 1][j] == '.';
                    boolean down = i < board.getRows() - 1 && board.getGrid()[i + 1][j] == '.';
                    return up || down;
                }
            }
        }
        return false;
    }

    // Third Heuristic
    public static int distanceToExit(Board board) {
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getCols(); j++) {
                if (board.getGrid()[i][j] == 'P') {
                    for (int k = j + 1; k < board.getCols(); k++) {
                        if (board.getGrid()[i][k] == 'K') {
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
