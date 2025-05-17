package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import model.Board;

public class InputParser {

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
        int exitRow = -1;
        int exitCol = -1;

        int currentRow = 0;
        while (scanner.hasNextLine() && currentRow < rows) {
            String line = scanner.nextLine();

            if (line == null || line.trim().isEmpty()) continue;

            // Cek K di luar kolom kiri
            if (line.startsWith("K")) {
                exitRow = currentRow;
                exitCol = -1; // kiri luar
            }

            // Isi grid
            for (int j = 0; j < cols; j++) {
                if (j < line.length()) {
                    char c = line.charAt(j);
                    if (c == 'K') {
                        // K di dalam grid = salah
                        throw new IllegalArgumentException("Posisi K tidak boleh berada di dalam grid.");
                    }
                    grid[currentRow][j] = c;
                } else {
                    grid[currentRow][j] = '.';
                }
            }

            // Cek K di luar kolom kanan
            if (line.length() > cols && line.charAt(cols) == 'K') {
                exitRow = currentRow;
                exitCol = cols; // kanan luar
            }

            currentRow++;
        }

        scanner.close();

        if (exitRow == -1 || exitCol == -1) {
            throw new IllegalArgumentException("Pintu keluar 'K' tidak ditemukan di sisi luar grid.");
        }

        return new Board(grid, rows, cols, exitRow, exitCol);
    }
}