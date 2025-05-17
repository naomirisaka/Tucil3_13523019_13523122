package model;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

public class Board {
    public char[][] grid;
    public int rows, cols;
    public String move; // Deskripsi gerakan terakhir (opsional)
    public Board parent; // Parent untuk tracking path (opsional)

    // Posisi pintu keluar, bisa di luar grid (misal exitCol == cols artinya di luar sebelah kanan)
    private int exitRow = -1;
    private int exitCol = -1;

    // Constructor lengkap
    public Board(char[][] grid, int rows, int cols, int exitRow, int exitCol) {
        this.rows = rows;
        this.cols = cols;
        this.grid = new char[rows][cols];
        for (int i = 0; i < rows; i++) {
            this.grid[i] = Arrays.copyOf(grid[i], cols);
        }
        this.exitRow = exitRow;
        this.exitCol = exitCol;
    }

    // Constructor tanpa exit (default exit pos = -1)
    public Board(char[][] grid) {
        this.rows = grid.length;
        this.cols = (rows > 0) ? grid[0].length : 0;
        this.grid = new char[rows][cols];
        for (int i = 0; i < rows; i++) {
            this.grid[i] = Arrays.copyOf(grid[i], cols);
        }
    }

    public void setExitPosition(int row, int col) {
        this.exitRow = row;
        this.exitCol = col;
    }

    public int getExitRow() { return exitRow; }
    public int getExitCol() { return exitCol; }
    public char[][] getGrid() { return grid; }
    public int getRows() { return rows; }
    public int getCols() { return cols; }

    // Check if the board is in goal state: primary piece 'P' exits through 'K'
    public boolean isGoal() {
        java.util.List<int[]> pPositions = new java.util.ArrayList<>();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid[i][j] == 'P') {
                    pPositions.add(new int[]{i, j});
                }
            }
        }

        if (pPositions.isEmpty()) return false;

        // Determine orientation: horizontal or vertical
        boolean isHorizontal = pPositions.stream().allMatch(p -> p[0] == pPositions.get(0)[0]);

        // Find front and back of 'P'
        pPositions.sort((a, b) -> isHorizontal ? Integer.compare(a[1], b[1]) : Integer.compare(a[0], b[0]));
        int[] front = pPositions.get(0);
        int[] back = pPositions.get(pPositions.size() - 1);

        // Check if one of the ends is adjacent to the exit position
        if (isHorizontal) {
            // Left side
            if (exitRow == front[0] && exitCol == front[1] - 1) return true;
            // Right side
            if (exitRow == back[0] && exitCol == back[1] + 1) return true;
        } else {
            // Top side
            if (exitRow == front[0] - 1 && exitCol == front[1]) return true;
            // Bottom side
            if (exitRow == back[0] + 1 && exitCol == back[1]) return true;
        }

        return false;
    }

    private boolean isPlayerAdjacent(int x, int y) {
        int[][] dirs = { {-1,0}, {1,0}, {0,-1}, {0,1} };
        for (int[] dir : dirs) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            if (nx >= 0 && ny >= 0 && nx < rows && ny < cols) {
                if (grid[nx][ny] == 'P') {
                    return true;
                }
            }
        }
        return false;
    }

    public java.util.List<Board> getNeighbors() {
        java.util.List<Board> neighbors = new java.util.ArrayList<>();

        java.util.Set<Character> vehicles = new java.util.HashSet<>();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                char c = grid[i][j];
                if (c != '.' && c != 'K') {
                    vehicles.add(c);
                }
            }
        }

        for (char v : vehicles) {
            java.util.List<int[]> positions = new java.util.ArrayList<>();
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (grid[i][j] == v) {
                        positions.add(new int[]{i, j});
                    }
                }
            }

            boolean horizontal = true;
            int firstRow = positions.get(0)[0];
            for (int[] pos : positions) {
                if (pos[0] != firstRow) {
                    horizontal = false;
                    break;
                }
            }
            final boolean isHorizontal = horizontal;

            positions.sort((a, b) -> isHorizontal ? Integer.compare(a[1], b[1]) : Integer.compare(a[0], b[0]));

            int negRow = positions.get(0)[0];
            int negCol = positions.get(0)[1];
            int newNegRow = horizontal ? negRow : negRow - 1;
            int newNegCol = horizontal ? negCol - 1 : negCol;

            if (isValidMove(v, newNegRow, newNegCol, horizontal, true)) {
                Board newBoard = moveVehicle(v, horizontal, true);
                if (newBoard != null) neighbors.add(newBoard);
            }

            int size = positions.size();
            int posRow = positions.get(size - 1)[0];
            int posCol = positions.get(size - 1)[1];
            int newPosRow = horizontal ? posRow : posRow + 1;
            int newPosCol = horizontal ? posCol + 1 : posCol;

            if (isValidMove(v, newPosRow, newPosCol, horizontal, false)) {
                Board newBoard = moveVehicle(v, horizontal, false);
                if (newBoard != null) neighbors.add(newBoard);
            }
        }

        return neighbors;
    }

    private boolean isValidMove(char vehicle, int newRow, int newCol, boolean horizontal, boolean isNegativeDirection) {
        if (horizontal) {
            if (newCol < 0) return vehicle == 'K' && exitRow == newRow && exitCol == -1;
            if (newCol >= cols) return vehicle == 'K' && exitRow == newRow && exitCol == cols;
            if (newRow < 0 || newRow >= rows) return false;
            char target = grid[newRow][newCol];
            return target == '.' || (vehicle == 'K' && target == 'K');
        } else {
            if (newRow < 0) return vehicle == 'K' && exitRow == -1 && exitCol == newCol;
            if (newRow >= rows) return vehicle == 'K' && exitRow == rows && exitCol == newCol;
            if (newCol < 0 || newCol >= cols) return false;
            char target = grid[newRow][newCol];
            return target == '.' || (vehicle == 'K' && target == 'K');
        }
    }

    private Board moveVehicle(char vehicle, boolean horizontal, boolean negativeDirection) {
        char[][] newGrid = new char[rows][cols];
        for (int i = 0; i < rows; i++) {
            newGrid[i] = Arrays.copyOf(grid[i], cols);
        }

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (newGrid[i][j] == vehicle) {
                    newGrid[i][j] = '.';
                }
            }
        }

        java.util.List<int[]> positions = new java.util.ArrayList<>();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid[i][j] == vehicle) {
                    positions.add(new int[]{i, j});
                }
            }
        }
        positions.sort((a, b) -> horizontal ? Integer.compare(a[1], b[1]) : Integer.compare(a[0], b[0]));

        int shiftRow = 0, shiftCol = 0;
        if (horizontal) {
            shiftCol = negativeDirection ? -1 : 1;
        } else {
            shiftRow = negativeDirection ? -1 : 1;
        }

        for (int[] pos : positions) {
            int r = pos[0] + shiftRow;
            int c = pos[1] + shiftCol;

            if (r < 0 || r >= rows || c < 0 || c >= cols) {
                if (vehicle == 'K') continue;
                else return null;
            }

            newGrid[r][c] = vehicle;
        }

        Board newBoard = new Board(newGrid, rows, cols, exitRow, exitCol);
        newBoard.parent = this;
        newBoard.move = "Move " + vehicle + " " + (horizontal ? (negativeDirection ? "left" : "right") : (negativeDirection ? "up" : "down"));
        return newBoard;
    }

    @Override
    public String toString() {
        return toStringWithExit();
    }

    public String toStringWithExit() {
        StringBuilder sb = new StringBuilder();

        if (exitRow == -1 && exitCol >= 0 && exitCol < cols) {
            for (int j = 0; j < cols; j++) {
                sb.append(j == exitCol ? 'K' : '.');
            }
            sb.append('\n');
        }

        for (int i = 0; i < rows; i++) {
            if (i == exitRow && exitCol == -1) sb.append('K');
            for (int j = 0; j < cols; j++) {
                char c = grid[i][j];
                sb.append(c == 'P' ? '.' : c);
            }
            if (i == exitRow && exitCol == cols) sb.append('K');
            sb.append('\n');
        }

        if (exitRow == rows && exitCol >= 0 && exitCol < cols) {
            for (int j = 0; j < cols; j++) {
                sb.append(j == exitCol ? 'K' : '.');
            }
            sb.append('\n');
        }

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Board)) return false;
        Board other = (Board) o;
        return Arrays.deepEquals(this.grid, other.grid);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(grid);
    }

    public static Board parse(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);

        int rows = 0, cols = 0;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                String[] parts = line.split("\\s+");
                if (parts.length == 2) {
                    rows = Integer.parseInt(parts[0]);
                    cols = Integer.parseInt(parts[1]);
                    break;
                }
            }
        }

        int nonPrimaryPiecesCount = 0;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                nonPrimaryPiecesCount = Integer.parseInt(line);
                break;
            }
        }

        char[][] grid = new char[rows][cols];
        int exitRow = -1, exitCol = -1;

        int currentRow = 0;
        while (scanner.hasNextLine() && currentRow < rows) {
            String line = scanner.nextLine();
            if (line == null) break;
            line = line.trim();
            if (line.isEmpty()) continue;

            for (int j = 0; j < cols; j++) {
                if (j < line.length()) {
                    grid[currentRow][j] = line.charAt(j);
                } else {
                    grid[currentRow][j] = '.';
                }
            }

            if (line.length() > cols) {
                for (int j = cols; j < line.length(); j++) {
                    if (line.charAt(j) == 'K') {
                        exitRow = currentRow;
                        exitCol = j;
                        break;
                    }
                }
            }

            currentRow++;
        }

        scanner.close();

        if (currentRow != rows) {
            throw new IllegalArgumentException("Jumlah baris konfigurasi tidak sesuai ukuran papan.");
        }

        if ((exitRow == 0 || exitRow == rows - 1) && (exitCol == 0 || exitCol == cols - 1)) {
            throw new IllegalArgumentException("Pintu keluar 'K' tidak boleh berada di sudut luar papan.");
        }
        if (exitRow >= 0 && exitRow < rows && exitCol >= 0 && exitCol < cols) {
            throw new IllegalArgumentException("Pintu keluar 'K' tidak boleh berada di dalam grid.");
        }

        java.util.List<int[]> pPositions = new java.util.ArrayList<>();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid[i][j] == 'P') {
                    pPositions.add(new int[]{i, j});
                }
            }
        }

        if (pPositions.isEmpty()) {
            throw new IllegalArgumentException("Primary piece 'P' tidak ditemukan.");
        }

        boolean sameRow = pPositions.stream().allMatch(p -> p[0] == pPositions.get(0)[0]);
        boolean sameCol = pPositions.stream().allMatch(p -> p[1] == pPositions.get(0)[1]);

        if (!sameRow && !sameCol) {
            throw new IllegalArgumentException("Semua posisi 'P' harus berada di satu baris atau satu kolom.");
        }

        if (exitRow != -1 && exitCol != -1) {
            boolean valid = false;
            if (exitCol == -1 || exitCol == cols) {
                for (int[] p : pPositions) {
                    if (p[0] == exitRow) valid = true;
                }
            } else if (exitRow == -1 || exitRow == rows) {
                for (int[] p : pPositions) {
                    if (p[1] == exitCol) valid = true;
                }
            }
            if (!valid) {
                throw new IllegalArgumentException("Primary piece 'P' harus berada satu baris atau kolom dengan posisi keluar 'K'.");
            }
        }

        return new Board(grid, rows, cols, exitRow, exitCol);
    }
}
