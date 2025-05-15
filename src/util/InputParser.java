package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import model.Board;

public class InputParser {
    public static Board parse(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);

        int rows = 0, cols = 0;

        // Baca ukuran papan
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                String[] size = line.split("\\s+");
                if (size.length == 2) {
                    rows = Integer.parseInt(size[0]);
                    cols = Integer.parseInt(size[1]);
                    break;
                }
            }
        }

        // Lewati baris jumlah kendaraan (tidak digunakan)
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (!line.isEmpty() && line.matches("\\d+")) {
                break;
            }
        }

        // Baca konfigurasi papan
        char[][] grid = new char[rows][cols];
        int currentRow = 0;

        while (scanner.hasNextLine() && currentRow < rows) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;

            char[] row = new char[cols];
            for (int j = 0; j < cols; j++) {
                row[j] = (j < line.length()) ? line.charAt(j) : '.'; // Isi . jika kurang
            }

            grid[currentRow++] = row;
        }

        if (currentRow != rows) {
            throw new IllegalArgumentException("Jumlah baris konfigurasi tidak sesuai ukuran papan.");
        }

        return new Board(grid, rows, cols);
    }
}
