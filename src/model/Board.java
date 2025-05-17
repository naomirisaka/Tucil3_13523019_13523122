package model;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class Board {
    private final char[][] grid;
    private final int rows;
    private final int cols;
    private final int exitRow;
    private final int exitCol;

    private List<Piece> pieces;
    private Piece primaryPiece;

    public Board parent = null;
    public String move = null;
    private boolean hasExited = false;

    public boolean hasExited() {
        return hasExited;
    }

    public Board(char[][] grid, int rows, int cols, int exitRow, int exitCol) {
        this.rows = rows;
        this.cols = cols;
        this.grid = new char[rows][cols];
        this.exitRow = exitRow;
        this.exitCol = exitCol;

        for (int i = 0; i < rows; i++) {
            this.grid[i] = Arrays.copyOf(grid[i], cols);
        }

        parsePieces();
    }

    private void parsePieces() {
        boolean[][] visited = new boolean[rows][cols];
        pieces = new ArrayList<>();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                char id = grid[i][j];
                if (id == '.' || id == 'K' || visited[i][j]) continue;

                List<int[]> positions = new ArrayList<>();
                for (int x = 0; x < rows; x++) {
                    for (int y = 0; y < cols; y++) {
                        if (grid[x][y] == id) {
                            positions.add(new int[]{x, y});
                            visited[x][y] = true;
                        }
                    }
                }

                int minRow = positions.stream().mapToInt(p -> p[0]).min().orElse(0);
                int minCol = positions.stream().mapToInt(p -> p[1]).min().orElse(0);

                boolean isHorizontal = positions.stream().allMatch(p -> p[0] == minRow);
                int length = positions.size();

                Piece piece = new Piece(id, minRow, minCol, length, isHorizontal);
                pieces.add(piece);

                if (id == 'P') {
                    if (primaryPiece != null) {
                        throw new IllegalArgumentException("Lebih dari satu primary piece (P) ditemukan.");
                    }
                    primaryPiece = piece;
                }
            }
        }

        if (primaryPiece == null) {
            throw new IllegalArgumentException("Primary piece (P) tidak ditemukan.");
        }
    }

    public List<Board> getNeighbors() {
        List<Board> neighbors = new ArrayList<>();

        for (Piece piece : pieces) {
            for (int dir = -1; dir <= 1; dir += 2) {
                Board moved = moveUntilBlocked(piece, dir);
                while (moved != null) {
                    neighbors.add(moved);
                    moved = moved.moveOnce(piece, dir);
                }
            }
        }

        return neighbors;
    }

    private Board moveUntilBlocked(Piece piece, int dir) {
        Board current = moveOnce(piece, dir);
        Board last = null;
        while (current != null) {
            last = current;
            current = current.moveOnce(piece, dir);
        }
        return last;
    }

    private Board moveOnce(Piece piece, int dir) {
        char[][] newGrid = new char[rows][cols];
        for (int i = 0; i < rows; i++) {
            newGrid[i] = Arrays.copyOf(grid[i], cols);
        }

        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                if (newGrid[i][j] == piece.id)
                    newGrid[i][j] = '.';

        int dr = piece.isHorizontal ? 0 : dir;
        int dc = piece.isHorizontal ? dir : 0;
        int newHeadR = piece.row + dr;
        int newHeadC = piece.col + dc;
        int newTailR = piece.row + dr * piece.length;
        int newTailC = piece.col + dc * piece.length;

        boolean isPrimary = piece.id == 'P';
        boolean outOfBounds = newTailR < 0 || newTailR >= rows || newTailC < 0 || newTailC >= cols;

        if (outOfBounds) {
            if (!(isPrimary && newTailR == exitRow && newTailC == exitCol)) {
                return null;
            }
        } else {
            if (grid[newTailR][newTailC] != '.') {
                return null;
            }
        }

        boolean exitMove = outOfBounds && isPrimary && newTailR == exitRow && newTailC == exitCol;

        if (exitMove) {
            for (int i = 0; i < piece.length - 1; i++) {
                int r = newHeadR + (piece.isHorizontal ? 0 : i);
                int c = newHeadC + (piece.isHorizontal ? i : 0);
                if (r >= 0 && r < rows && c >= 0 && c < cols) {
                    newGrid[r][c] = piece.id;
                }
            }
        } else {
            for (int i = 0; i < piece.length; i++) {
                int r = newHeadR + (piece.isHorizontal ? 0 : i);
                int c = newHeadC + (piece.isHorizontal ? i : 0);
                newGrid[r][c] = piece.id;
            }
        }

        Board newBoard = new Board(newGrid, rows, cols, exitRow, exitCol);
        newBoard.parent = this;
        newBoard.move = "Move " + piece.id + " " +
                (piece.isHorizontal ? (dir == -1 ? "left" : "right") : (dir == -1 ? "up" : "down"));
        newBoard.hasExited = exitMove;

        return newBoard;
    }

    public boolean isGoal() {
        if (primaryPiece == null || exitRow == -1 || exitCol == -1) return false;

        if (primaryPiece.isHorizontal) {
            int endCol = primaryPiece.col + primaryPiece.length - 1;
            return primaryPiece.row == exitRow && (exitCol == endCol + 1 || exitCol == primaryPiece.col - 1);
        } else {
            int endRow = primaryPiece.row + primaryPiece.length - 1;
            return primaryPiece.col == exitCol && (exitRow == endRow + 1 || exitRow == primaryPiece.row - 1);
        }
    }

    public void writeSolutionToFile(String path) throws IOException {
        try (PrintWriter writer = new PrintWriter(path)) {
            Stack<Board> stack = new Stack<>();
            Board current = this;
            while (current != null) {
                stack.push(current);
                current = current.parent;
            }

            int step = 1;
            while (!stack.isEmpty()) {
                Board b = stack.pop();
                writer.println("Step " + step++ + ":");
                writer.println(b.toString());
                writer.println();
            }
        }
    }

    public String toString(boolean hideExit) {
        StringBuilder sb = new StringBuilder();

        boolean exitTop = (exitRow == -1);
        boolean exitBottom = (exitRow == rows);

        if (exitTop) {
            sb.append("  ");
            for (int j = 0; j < cols; j++) {
                sb.append((j == exitCol) ? "K " : "  ");
            }
            sb.append("\n");
        }

        sb.append("┌");
        for (int j = 0; j < cols; j++) sb.append("──");
        sb.append("┐\n");

        for (int i = 0; i < rows; i++) {
            boolean leftExit = (exitCol == -1 && exitRow == i);
            boolean rightExit = (exitCol == cols && exitRow == i);

            sb.append(leftExit ? "K" : "│");

            for (int j = 0; j < cols; j++) {
                char c = grid[i][j];
                if (hideExit && c == 'P') {
                    sb.append(". ");
                } else {
                    sb.append(c).append(' ');
                }
            }

            sb.append(rightExit ? "K\n" : "│\n");
        }

        sb.append("└");
        for (int j = 0; j < cols; j++) sb.append("──");
        sb.append("┘\n");

        if (exitBottom) {
            sb.append("  ");
            for (int j = 0; j < cols; j++) {
                sb.append((j == exitCol) ? "K " : "  ");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return toString(false);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Board other = (Board) obj;
        return Arrays.deepEquals(this.grid, other.grid)
            && this.exitRow == other.exitRow
            && this.exitCol == other.exitCol;
    }

    @Override
    public int hashCode() {
        int result = Arrays.deepHashCode(grid);
        result = 31 * result + exitRow;
        result = 31 * result + exitCol;
        return result;
    }

    public char[][] getGrid() {
        return grid;
    }

    public Piece getPrimaryPiece() {
        return primaryPiece;
    }

    public List<Piece> getPieces() {
        return pieces;
    }

    public int getExitRow() {
        return exitRow;
    }

    public int getExitCol() {
        return exitCol;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }
}