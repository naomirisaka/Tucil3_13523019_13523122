package util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.Board;

public class Heuristic {

    // First Heuristic: Blocking Pieces Count
    public static int blockingPiecesCount(Board board) {
        // Cari posisi 'P'
        int pRow = -1, pCol = -1;
        outer:
        for (int i = 0; i < board.rows; i++) {
            for (int j = 0; j < board.cols; j++) {
                if (board.grid[i][j] == 'P') {
                    pRow = i;
                    pCol = j;
                    break outer;
                }
            }
        }

        // Cari orientasi 'P'
        boolean isHorizontal = (pCol + 1 < board.cols && board.grid[pRow][pCol + 1] == 'P');

        // Cari posisi 'K' (exit)
        int exitRow = board.getExitRow();
        int exitCol = board.getExitCol();

        int count = 0;

        if (isHorizontal) {
            // Bergerak horizontal ke arah exit
            int dir = Integer.compare(exitCol, pCol); // +1 atau -1
            int j = pCol;
            while ((j += dir) >= 0 && j < board.cols) {
                char c = board.grid[pRow][j];
                if (c != '.' && c != 'K' && c != 'P') count++;
            }
        } else {
            // Bergerak vertikal ke arah exit
            int dir = Integer.compare(exitRow, pRow);
            int i = pRow;
            while ((i += dir) >= 0 && i < board.rows) {
                char c = board.grid[i][pCol];
                if (c != '.' && c != 'K' && c != 'P') count++;
            }
        }

        return count;
    }

    // Second Heuristic: Blocking Pieces with Movability Penalty
    public static int blockingPiecesWithMovability(Board board) {
        int[] pRowCol = findFrontOfP(board);
        if (pRowCol == null) return Integer.MAX_VALUE;
        int row = pRowCol[0];
        int col = pRowCol[1];

        int exitCol = board.getExitCol();
        int score = 0;
        Set<Character> seen = new HashSet<>();

        boolean isPrimaryHorizontal = isPrimaryPieceHorizontal(board);

        for (int j = col + 1; j < board.getCols() && j <= exitCol; j++) {
            char block = board.grid[row][j];
            if (block != '.' && block != 'K' && block != 'P' && !seen.contains(block)) {
                seen.add(block);
                score += 1;

                if (isPrimaryHorizontal) {
                    if (!canMoveVertically(board, block)) {
                        score += 2;
                    }
                } else {
                    if (!canMoveHorizontally(board, block)) {
                        score += 2;
                    }
                }
            }
        }

        return score;
    }

    private static boolean canMoveVertically(Board board, char vehicle) {
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getCols(); j++) {
                if (board.grid[i][j] == vehicle) {
                    boolean up = i > 0 && board.grid[i - 1][j] == '.';
                    boolean down = i < board.getRows() - 1 && board.grid[i + 1][j] == '.';
                    return up || down;
                }
            }
        }
        return false;
    }

    private static boolean canMoveHorizontally(Board board, char vehicle) {
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getCols(); j++) {
                if (board.grid[i][j] == vehicle) {
                    boolean left = j > 0 && board.grid[i][j - 1] == '.';
                    boolean right = j < board.getCols() - 1 && board.grid[i][j + 1] == '.';
                    return left || right;
                }
            }
        }
        return false;
    }

    // Determine if primary piece P is horizontal
    private static boolean isPrimaryPieceHorizontal(Board board) {
        List<int[]> positions = new ArrayList<>();
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getCols(); j++) {
                if (board.grid[i][j] == 'P') {
                    positions.add(new int[]{i, j});
                }
            }
        }
        if (positions.size() < 2) return false; // fallback
        int firstRow = positions.get(0)[0];
        for (int[] pos : positions) {
            if (pos[0] != firstRow) return false;
        }
        return true;
    }

    // Third Heuristic: Distance to Exit (Horizontal or Vertical)
    public static int distanceToExit(Board board) {
        int[] pHead = findFrontOfP(board);
        if (pHead == null) return Integer.MAX_VALUE;
        int pRow = pHead[0];
        int pCol = pHead[1];

        int exitRow = board.getExitRow();
        int exitCol = board.getExitCol();
        if (exitRow < 0 || exitCol < 0) return Integer.MAX_VALUE;

        // Tentukan orientasi P
        boolean isHorizontal = false;
        if (pCol + 1 < board.cols && board.grid[pRow][pCol + 1] == 'P') {
            isHorizontal = true;
        }

        if (isHorizontal) {
            // Bergerak horizontal
            if (pRow != exitRow) return Integer.MAX_VALUE; // bukan arah keluar
            return Math.abs(exitCol - pCol);
        } else {
            // Bergerak vertikal
            if (pCol != exitCol) return Integer.MAX_VALUE; // bukan arah keluar
            return Math.abs(exitRow - pRow);
        }
    }

    // Helper: Find front-right most position of P
    private static int[] findFrontOfP(Board board) {
        int lastCol = -1;
        int pRow = -1;
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getCols(); j++) {
                if (board.grid[i][j] == 'P') {
                    if (j > lastCol) {
                        lastCol = j;
                        pRow = i;
                    }
                }
            }
        }
        return (lastCol != -1) ? new int[]{pRow, lastCol} : null;
    }

    // Dispatcher
    public static int evaluate(Board board, String name) {
        return switch (name) {
            case "Blocking Pieces" -> blockingPiecesCount(board);
            case "Blocking Pieces With Movability" -> blockingPiecesWithMovability(board);
            case "Distance-to-Exit" -> distanceToExit(board);
            default -> blockingPiecesCount(board);
        };
    }
}