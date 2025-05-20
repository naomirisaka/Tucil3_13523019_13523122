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
            if (line == null) break;
            line = line.trim();
            if (line.isEmpty()) continue;

            for (int j = 0; j < cols; j++) {
                if (j < line.length()) {
                    char c = line.charAt(j);
                    if (c == 'K') {
                        // Jika K ada di dalam grid, anggap titik biasa, dan simpan posisi exit di grid
                        exitRow = currentRow;
                        exitCol = j;
                        grid[currentRow][j] = '.'; // K di grid diganti titik
                    } else {
                        grid[currentRow][j] = c;
                    }
                } else {
                    grid[currentRow][j] = '.';
                }
            }

            // Jika line lebih panjang dari cols, cek posisi K di luar grid (misal di kanan)
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

        // buat board dengan exit position yang mungkin di luar grid (exitCol bisa == cols)
        return new Board(grid, rows, cols, exitRow, exitCol);
    }
}
