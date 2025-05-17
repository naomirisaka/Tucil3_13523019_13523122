package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import model.Board;

public class InputParser {

    public static Board parse(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);

        int rows = 0, cols = 0;

        // 1. Baca ukuran papan
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

        // 2. Baca jumlah kendaraan (skip)
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) break;
        }

        // 3. Ambil semua baris setelahnya
        List<String> rawLines = new ArrayList<>();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (!line.isEmpty() && !line.matches("\\.*")) {
                rawLines.add(line);
            }
        }


        scanner.close();

        int exitRow = -1, exitCol = -1;

        // 4. Cek baris atas
        if (rawLines.size() > rows && rawLines.get(0).length() >= cols) {
            String top = rawLines.get(0);
            for (int j = 0; j < cols; j++) {
                if (top.charAt(j) == 'K') {
                    exitRow = -1;
                    exitCol = j;
                    rawLines.remove(0);
                    break;
                }
            }
        }

        // 5. Cek baris bawah
        if (rawLines.size() > rows && rawLines.get(rawLines.size() - 1).length() >= cols) {
            String bottom = rawLines.get(rawLines.size() - 1);
            for (int j = 0; j < cols; j++) {
                if (bottom.charAt(j) == 'K') {
                    exitRow = rows;
                    exitCol = j;
                    rawLines.remove(rawLines.size() - 1);
                    break;
                }
            }
        }

        // 6. Validasi jumlah baris
        if (rawLines.size() != rows) {
            throw new IllegalArgumentException("Jumlah baris tidak sesuai dengan ukuran papan.");
        }

        // 7. Parse grid dan cari 'K' di kiri/kanan/dalam
        char[][] grid = new char[rows][cols];
        for (int i = 0; i < rows; i++) {
            String line = rawLines.get(i);

            // Check if 'K' is immediately left or right of the board line
            if (line.length() > cols) {
                if (line.charAt(0) == 'K') {
                    exitRow = i;
                    exitCol = -1;
                    line = line.substring(1); // Remove K from left
                } else if (line.charAt(line.length() - 1) == 'K') {
                    exitRow = i;
                    exitCol = cols;
                    line = line.substring(0, line.length() - 1); // Remove K from right
                }
            }


            for (int j = 0; j < cols; j++) {
                if (j < line.length()) {
                    char c = line.charAt(j);
                    if (c == 'K') {
                        exitRow = i;
                        exitCol = j;
                        grid[i][j] = '.'; // Kosongkan jika 'K'
                    } else {
                        grid[i][j] = c;
                    }
                } else {
                    grid[i][j] = '.'; // default jika kolom kurang
                }
            }
        }

        // 8. Validasi kendaraan
        validateGrid(grid);

        return new Board(grid, rows, cols, exitRow, exitCol);
    }

    private static void validateGrid(char[][] grid) {
        int rows = grid.length;
        int cols = grid[0].length;
        Set<Character> visited = new HashSet<>();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                char c = grid[i][j];
                if (c == '.' || visited.contains(c)) continue;

                visited.add(c);
                List<int[]> positions = new ArrayList<>();
                positions.add(new int[]{i, j});

                for (int x = 0; x < rows; x++) {
                    for (int y = 0; y < cols; y++) {
                        if ((x != i || y != j) && grid[x][y] == c) {
                            positions.add(new int[]{x, y});
                        }
                    }
                }

                boolean sameRow = true, sameCol = true;
                int baseRow = positions.get(0)[0];
                int baseCol = positions.get(0)[1];
                for (int[] pos : positions) {
                    if (pos[0] != baseRow) sameRow = false;
                    if (pos[1] != baseCol) sameCol = false;
                }

                if (!sameRow && !sameCol) {
                    throw new IllegalArgumentException("Kendaraan '" + c + "' tidak lurus (mungkin diagonal).");
                }
            }
        }
    }
}
