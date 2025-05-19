package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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

        // 2. Baca jumlah kendaraan non-primer
        int nonPrimaryPiecesCount = 0;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                nonPrimaryPiecesCount = Integer.parseInt(line);
                break;
            }
        }

        // 3. Baca semua baris konfigurasi, TANPA trim
        List<String> rawLines = new ArrayList<>();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (!line.trim().isEmpty()) {
                rawLines.add(line); // biarkan spasi dan 'K' tetap
            }
        }

        scanner.close();

        int exitRow = -1, exitCol = -1;

        // 4. Cek 'K' di atas
        if (rawLines.size() > rows) {
            String topLine = rawLines.get(0);
            if (topLine.length() == cols) {
                for (int j = 0; j < cols; j++) {
                    if (topLine.charAt(j) == 'K') {
                        exitRow = -1;
                        exitCol = j;
                        break;
                    }
                }
            }
        }

        // 5. Cek 'K' di bawah
        if (exitRow == -1 && rawLines.size() > rows) {
            String bottomLine = rawLines.get(rawLines.size() - 1);
            if (bottomLine.length() == cols) {
                for (int j = 0; j < cols; j++) {
                    if (bottomLine.charAt(j) == 'K') {
                        exitRow = rows;
                        exitCol = j;
                        break;
                    }
                }
            }
        }

        // 6. Validasi jumlah baris grid
        List<String> gridLines = rawLines.subList(rawLines.size() - rows, rawLines.size());
        if (gridLines.size() != rows) {
            throw new IllegalArgumentException("Jumlah baris konfigurasi tidak sesuai ukuran papan.");
        }

        // 7. Parsing isi grid dan deteksi K di kiri/kanan
        char[][] grid = new char[rows][cols];
        for (int i = 0; i < rows; i++) {
            String line = gridLines.get(i);

            // Deteksi 'K' di kolom kiri
            if (line.length() > 0 && line.charAt(0) == 'K') {
                exitRow = i;
                exitCol = -1;
            }

            // Deteksi 'K' di kolom kanan
            if (line.length() > cols && line.charAt(cols) == 'K') {
                exitRow = i;
                exitCol = cols;
            }

            // Isi grid dari line
            for (int j = 0; j < cols; j++) {
                if (j < line.length()) {
                    grid[i][j] = line.charAt(j) == 'K' ? '.' : line.charAt(j);
                } else {
                    grid[i][j] = '.';
                }
            }
        }

        // 8. Print hasil parsing ke terminal
        System.out.println("=== Hasil Parsing ===");
        System.out.println("Ukuran papan: " + rows + "x" + cols);
        System.out.println("Jumlah kendaraan non-primer: " + nonPrimaryPiecesCount);
        if (exitRow == -1) {
            System.out.println("Exit: baris di atas, kolom " + exitCol);
        } else if (exitRow == rows) {
            System.out.println("Exit: baris di bawah, kolom " + exitCol);
        } else if (exitCol == -1) {
            System.out.println("Exit: kolom kiri, baris " + exitRow);
        } else if (exitCol == cols) {
            System.out.println("Exit: kolom kanan, baris " + exitRow);
        } else if (exitRow != -1 && exitCol != -1) {
            System.out.println("Exit: di dalam grid, baris " + exitRow + ", kolom " + exitCol);
        } else {
            System.out.println("Exit: tidak ditemukan");
        }

        System.out.println("Grid:");
        for (int i = 0; i < rows; i++) {
            System.out.println(gridLines.get(i));
        }
        System.out.println("=====================");

        return new Board(grid, rows, cols, exitRow, exitCol);
    }
}
