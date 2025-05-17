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

        // 1. Baca ukuran
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

        // 3. Ambil semua baris setelahnya
        List<String> rawLines = new ArrayList<>();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (!line.trim().isEmpty()) {
                rawLines.add(line);
            }
        }

        scanner.close();

        // 4. Inisialisasi exit
        int exitRow = -1, exitCol = -1;

        // 5. Cek apakah ada baris 'K' di atas grid (harus panjangnya == cols)
        if (rawLines.size() > rows && rawLines.get(0).length() == cols) {
            String topLine = rawLines.get(0);
            for (int j = 0; j < cols; j++) {
                if (topLine.charAt(j) == 'K') {
                    exitRow = -1;
                    exitCol = j;
                    rawLines.remove(0); // Hapus baris ini karena bukan bagian dari grid
                    break;
                }
            }
        }

        // 6. Cek apakah ada baris 'K' di bawah grid
        if (rawLines.size() > rows && rawLines.get(rawLines.size() - 1).length() == cols) {
            String bottomLine = rawLines.get(rawLines.size() - 1);
            for (int j = 0; j < cols; j++) {
                if (bottomLine.charAt(j) == 'K') {
                    exitRow = rows;
                    exitCol = j;
                    rawLines.remove(rawLines.size() - 1); // Bukan bagian dari grid
                    break;
                }
            }
        }

        // 7. Sekarang rawLines harus berisi tepat `rows` baris grid
        if (rawLines.size() != rows) {
            throw new IllegalArgumentException("Jumlah baris konfigurasi tidak sesuai ukuran papan.");
        }

        // 8. Parse grid dan cari K di dalam/kanan/kiri grid
        char[][] grid = new char[rows][cols];
        for (int i = 0; i < rows; i++) {
            String line = rawLines.get(i);

            // K di kiri atau kanan
            if (line.length() > cols) {
                if (line.charAt(0) == 'K') {
                    exitRow = i;
                    exitCol = -1;
                } else if (line.charAt(cols) == 'K') {
                    exitRow = i;
                    exitCol = cols;
                }
            }

            for (int j = 0; j < cols; j++) {
                if (j < line.length()) {
                    char c = line.charAt(j);
                    if (c == 'K') {
                        exitRow = i;
                        exitCol = j;
                        grid[i][j] = '.';
                    } else {
                        grid[i][j] = c;
                    }
                } else {
                    grid[i][j] = '.';
                }
            }
        }

        return new Board(grid, rows, cols, exitRow, exitCol);
    }
}
