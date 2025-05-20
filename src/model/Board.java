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

    // Cek apakah posisi sudah mencapai goal (P di samping K)
    public boolean isGoal() {
        // Cek 'K' di dalam grid
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid[i][j] == 'K' && isPlayerAdjacent(i, j)) {
                    return true;
                }
            }
        }

        // Cek 'K' di luar grid (exitRow, exitCol)
        if (exitRow == -1 || exitCol == -1) return false;

        // Exit di sebelah kanan grid
        if (exitCol == cols && exitRow >= 0 && exitRow < rows) {
            if (grid[exitRow][cols - 1] == 'P') return true;
        }
        // Exit di sebelah kiri grid
        else if (exitCol == -1 && exitRow >= 0 && exitRow < rows) {
            if (grid[exitRow][0] == 'P') return true;
        }
        // Exit di bawah grid
        if (exitRow == rows && exitCol >= 0 && exitCol < cols) {
            if (grid[rows - 1][exitCol] == 'P') return true;
        }
        // Exit di atas grid
        else if (exitRow == -1 && exitCol >= 0 && exitCol < cols) {
            if (grid[0][exitCol] == 'P') return true;
        }

        return false;
    }

    // Cek apakah posisi tetangga (atas, bawah, kiri, kanan) adalah P
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

        // Temukan semua kendaraan unik
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
            // Cari semua posisi kendaraan v
            java.util.List<int[]> positions = new java.util.ArrayList<>();
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (grid[i][j] == v) {
                        positions.add(new int[]{i, j});
                    }
                }
            }

            // Tentukan orientasi kendaraan: horizontal atau vertikal
            // Kalau semua baris sama -> horizontal, kalau semua kolom sama -> vertikal
            boolean horizontal = true;
            int firstRow = positions.get(0)[0];
            for (int[] pos : positions) {
                if (pos[0] != firstRow) {
                    horizontal = false;
                    break;
                }
            }
            // boolean vertical = !horizontal;
            final boolean isHorizontal = horizontal;

            // Urut posisi berdasarkan orientasi supaya gampang cek gerakan
            positions.sort((a, b) -> isHorizontal ? Integer.compare(a[1], b[1]) : Integer.compare(a[0], b[0]));

            // Coba geser kendaraan 1 langkah ke arah negatif dan positif
            // Cek batas dan apakah posisi baru kosong ('.') atau K jika kendaraan K

            // Arah negatif (kiri/atas)
            int negRow = positions.get(0)[0];
            int negCol = positions.get(0)[1];
            int newNegRow = horizontal ? negRow : negRow - 1;
            int newNegCol = horizontal ? negCol - 1 : negCol;

            if (isValidMove(v, newNegRow, newNegCol, horizontal, true)) {
                Board newBoard = moveVehicle(v, horizontal, true);
                neighbors.add(newBoard);
            }

            // Arah positif (kanan/bawah)
            int size = positions.size();
            int posRow = positions.get(size - 1)[0];
            int posCol = positions.get(size - 1)[1];
            int newPosRow = horizontal ? posRow : posRow + 1;
            int newPosCol = horizontal ? posCol + 1 : posCol;

            if (isValidMove(v, newPosRow, newPosCol, horizontal, false)) {
                Board newBoard = moveVehicle(v, horizontal, false);
                neighbors.add(newBoard);
            }
        }

        return neighbors;
    }

    // Cek apakah gerakan valid: posisi baru harus di dalam batas dan kosong '.' atau (K jika kendaraan K)
    private boolean isValidMove(char vehicle, int newRow, int newCol, boolean horizontal, boolean isNegativeDirection) {
        // Cek posisi baru valid
        if (horizontal) {
            // Gerak horizontal: baris tetap, kolom berubah
            if (newCol < 0) {
                // Kalau kendaraan K boleh keluar grid di sebelah kiri
                
                return vehicle == 'K' && exitRow == newRow && exitCol == -1;
            }
            if (newCol >= cols) {
                // Kalau kendaraan K keluar sebelah kanan
                
                return vehicle == 'K' && exitRow == newRow && exitCol == cols;
            }
            if (newRow < 0 || newRow >= rows) return false;

            char target = grid[newRow][newCol];
            return target == '.' || (vehicle == 'K' && target == 'K');
        } else {
            // Gerak vertikal: kolom tetap, baris berubah
            if (newRow < 0) {
                if (vehicle == 'K' && exitRow == -1 && exitCol == newCol) {
                    return true;
                }
                return false;
            }
            if (newRow >= rows) {
                if (vehicle == 'K' && exitRow == rows && exitCol == newCol) {
                    return true;
                }
                return false;
            }
            if (newCol < 0 || newCol >= cols) return false;

            char target = grid[newRow][newCol];
            return target == '.' || (vehicle == 'K' && target == 'K');
        }
    }

    // Fungsi untuk menghasilkan board baru setelah memindahkan kendaraan 1 langkah ke kiri/atas (negatif) atau kanan/bawah (positif)
    private Board moveVehicle(char vehicle, boolean horizontal, boolean negativeDirection) {
        char[][] newGrid = new char[rows][cols];
        for (int i = 0; i < rows; i++) {
            newGrid[i] = Arrays.copyOf(grid[i], cols);
        }

        // Hapus kendaraan di posisi lama
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (newGrid[i][j] == vehicle) {
                    newGrid[i][j] = '.';
                }
            }
        }

        // Tambahkan kendaraan di posisi baru
        // Cari posisi lama kendaraan dulu
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

            // Kalau posisi baru keluar grid dan kendaraan adalah K, skip pasang di grid (karena di luar)
            if (r < 0 || r >= rows || c < 0 || c >= cols) {
                if (vehicle == 'K') continue;
                else return null; // Invalid move, jangan buat board baru
            }

            newGrid[r][c] = vehicle;
        }

        Board newBoard = new Board(newGrid, rows, cols, exitRow, exitCol);
        newBoard.parent = this;
        newBoard.move = "Move " + vehicle + " " + (horizontal ? (negativeDirection ? "left" : "right") : (negativeDirection ? "up" : "down"));
        return newBoard;
    }


    // ToString biasa untuk GUI/debugging
    @Override
    public String toString() {
        return toStringWithExit();
    }

    // ToString khusus yang menampilkan 'K' di luar grid (exit position)
    public String toStringWithExit() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < rows; i++) {
            // Jika exit di sebelah kiri di baris ini, tampilkan 'K' dulu
            if (i == exitRow && exitCol == -1) {
                sb.append('K');
            }
            for (int j = 0; j < cols; j++) {
                sb.append(grid[i][j]);
            }
            // Jika exit di sebelah kanan di baris ini, tampilkan 'K' setelah grid
            if (i == exitRow && exitCol == cols) {
                sb.append('K');
            }
            sb.append('\n');
        }

        // Jika exit di atas grid (baris -1), tampilkan baris 'K' di atas
        if (exitRow == -1 && exitCol >= 0 && exitCol < cols) {
            for (int j = 0; j < cols; j++) {
                sb.append(j == exitCol ? 'K' : '.');
            }
            sb.append('\n');
        }

        // Jika exit di bawah grid (baris == rows), tampilkan baris 'K' di bawah
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

    // Parsing file sesuai format input
    public static Board parse(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);

        int rows = 0, cols = 0;
        // Baca dimensi
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

        // Baca jumlah non-primary pieces (N), belum dipakai di sini tapi bisa disimpan
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

            // isi grid, padding '.' jika kurang dari cols
            for (int j = 0; j < cols; j++) {
                if (j < line.length()) {
                    grid[currentRow][j] = line.charAt(j);
                } else {
                    grid[currentRow][j] = '.';
                }
            }

            // Cek apakah ada K di luar grid (lebih dari cols)
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

        Board board = new Board(grid, rows, cols, exitRow, exitCol);

        return board;
    }
}
