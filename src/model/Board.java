package model;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Board {
    public char[][] grid;
    public int rows, cols;
    public String move; // Deskripsi gerakan terakhir (jika perlu)
    public Board parent; // Parent board untuk tracking path (jika diperlukan)

    // Konstruktor utama
    public Board(char[][] grid, int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.grid = new char[rows][cols];
        for (int i = 0; i < rows; i++) {
            this.grid[i] = Arrays.copyOf(grid[i], cols);
        }
    }

    // Konstruktor alternatif (diperbaiki agar tidak error)
    public Board(char[][] grid) {
        this.rows = grid.length;
        this.cols = (rows > 0) ? grid[0].length : 0;
        this.grid = new char[rows][cols];
        for (int i = 0; i < rows; i++) {
            this.grid[i] = Arrays.copyOf(grid[i], cols);
        }
    }

    public boolean isGoal() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid[i][j] == 'K') {
                    boolean isEdge = (i == 0 || i == rows - 1 || j == 0 || j == cols - 1);
                    boolean isCorner = 
                        (i == 0 && j == 0) || 
                        (i == 0 && j == cols - 1) || 
                        (i == rows - 1 && j == 0) || 
                        (i == rows - 1 && j == cols - 1);

                    if (isEdge && !isCorner) {
                        // Tambahan: pastikan 'P' ada di sebelah 'K'
                        if ((i > 0 && grid[i - 1][j] == 'P') ||
                            (i < rows - 1 && grid[i + 1][j] == 'P') ||
                            (j > 0 && grid[i][j - 1] == 'P') ||
                            (j < cols - 1 && grid[i][j + 1] == 'P')) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }


    // Helper method untuk cek apakah P (player) berada tepat di samping K
    private boolean isPlayerAdjacent(int x, int y) {
        int[][] dirs = { {-1, 0}, {1, 0}, {0, -1}, {0, 1} };
        for (int[] dir : dirs) {
            int nx = x + dir[0], ny = y + dir[1];
            if (nx >= 0 && ny >= 0 && nx < rows && ny < cols) {
                if (grid[nx][ny] == 'P') {
                    return true;
                }
            }
        }
        return false;
    }

    // Placeholder: fungsi tetangga (harus diimplementasikan)
    public List<Board> getNeighbors() {
        // TODO: Implementasi untuk menghasilkan board tetangga (gerakan mobil)
        return new ArrayList<>();
    }

    // Teks dengan warna ANSI (untuk CLI)
    public String toStringColored() {
        StringBuilder sb = new StringBuilder();
        for (char[] row : grid) {
            for (char c : row) {
                switch (c) {
                    case 'P' -> sb.append("\u001B[31m").append(c).append("\u001B[0m");
                    case 'K' -> sb.append("\u001B[32m").append(c).append("\u001B[0m");
                    default -> sb.append(c);
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    // Konversi ke string biasa (untuk GUI atau debugging)
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (char[] row : grid) {
            for (char c : row) sb.append(c);
            sb.append("\n");
        }
        return sb.toString();
    }

    // Equals dan hashCode agar Board bisa dibandingkan di struktur data
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
    Scanner scanner;
    scanner = new Scanner(file);

    // Lewati 2 baris awal (A B dan N)
    if (scanner.hasNextLine()) scanner.nextLine(); // "A B"
    if (scanner.hasNextLine()) scanner.nextLine(); // "N"

    // Ambil ukuran papan
    String line = scanner.nextLine().trim();
    String[] size = line.split("\\s+");
    int rows = Integer.parseInt(size[0]);
    int cols = Integer.parseInt(size[1]);

    // Ambil jumlah pieces
    int n = Integer.parseInt(scanner.nextLine().trim());

    // Baca konfigurasi papan
    char[][] grid = new char[rows][cols];
    int filled = 0;
    while (scanner.hasNextLine() && filled < rows) {
        String row = scanner.nextLine().trim();
        if (!row.isEmpty()) {
            grid[filled++] = row.toCharArray();
        }
    }

    return new Board(grid);
}
}
