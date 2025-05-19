package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import model.Board;

// fails to parse input file w K on top/bottom properly
public class InputParser {

    public static Board parse(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);

        int rows = 0, cols = 0;
        int pieceAmt = 0;

        // Baca baris konfigurasi ukuran papan
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

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                String[] parts = line.split("\\s+");
                if (parts.length == 1) {
                    pieceAmt = Integer.parseInt(parts[0]);
                    break;
                }
            }
        }

        List<String> rawLines = new ArrayList<>();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().stripTrailing();
            if (!line.isEmpty()) {
                rawLines.add(line);
            }
        }

        int exitRow = -1, exitCol = -1;
        if (!rawLines.isEmpty()) {
            String top = rawLines.get(0);
            if (top.trim().equals("K")) {
                exitRow = -1;
                exitCol = top.length() - 1;
                rawLines.remove(0);
            }
        }

        if (!rawLines.isEmpty()) {
            String bottom = rawLines.get(rawLines.size() - 1);
            if (bottom.trim().equals("K")) {
                exitRow = rows;
                exitCol = bottom.length() - 1;
                rawLines.remove(rawLines.size() - 1);
            }
        }

        if (!rawLines.isEmpty() && rawLines.get(0).length() >= cols) {
            String top = rawLines.get(0);
            boolean foundK = false;
            for (int j = 0; j < cols; j++) {
                if (top.charAt(j) == 'K') {
                    if (!((j == 0 && top.length() > cols) || (j == top.length() - 1 && top.length() > cols))) {
                        exitRow = j;
                        exitCol = -1; // K di pinggir kiri
                        rawLines.remove(0);
                        foundK = true;
                        break;
                    }
                }
            }
            if (foundK && rawLines.size() < rows) {
                throw new IllegalArgumentException("Jumlah baris isi papan kurang dari konfigurasi setelah hapus baris K atas.");
            }
        }

        if (!rawLines.isEmpty() && rawLines.get(rawLines.size() - 1).length() >= cols) {
            String bottom = rawLines.get(rawLines.size() - 1);
            boolean foundK = false;
            for (int j = 0; j < cols; j++) {
                if (bottom.charAt(j) == 'K') {
                    if (!((j == 0 && bottom.length() > cols) || (j == bottom.length() - 1 && bottom.length() > cols))) {
                        exitRow = rows;
                        exitCol = j;
                        rawLines.remove(rawLines.size() - 1);
                        foundK = true;
                        break;
                    }
                }
            }
            if (foundK && rawLines.size() < rows) {
                throw new IllegalArgumentException("Jumlah baris isi papan kurang dari konfigurasi setelah hapus baris K bawah.");
            }
        }

        if (rawLines.size() != rows) {
            throw new IllegalArgumentException("Jumlah baris isi papan (" + rawLines.size() + ") tidak sesuai dengan konfigurasi rows = " + rows);
        }

        for (int i = 0; i < rows; i++) {
            String line = rawLines.get(i);
            rawLines.set(i, line.trim());
        }

        for (int i = 0; i < rows; i++) {
            String line = rawLines.get(i);

            if (line.length() < cols) {
                throw new IllegalArgumentException("Baris ke-" + i + " memiliki panjang kurang dari " + cols);
            }

            if (line.length() > cols) {
                if (line.charAt(0) == 'K') {
                    exitRow = i;
                    exitCol = -1; // K di pinggir kiri
                    line = line.substring(1);
                } else if (line.charAt(line.length() - 1) == 'K') {
                    exitRow = i;
                    exitCol = cols; // K di pinggir kanan
                    line = line.substring(0, line.length() - 1);
                } else {
                    throw new IllegalArgumentException("Baris ke-" + i + " memiliki panjang lebih dari " + cols + " tanpa 'K' di pinggir.");
                }

                if (line.length() != cols) {
                    throw new IllegalArgumentException("Setelah potong, baris ke-" + i + " panjangnya tidak sesuai cols.");
                }
                rawLines.set(i, line);
            }

            for (int j = 0; j < cols; j++) {
                if (line.charAt(j) == 'K') {
                    throw new IllegalArgumentException("Karakter 'K' hanya boleh di pinggir papan (baris " + i + ", kolom " + j + ").");
                }
            }
        }

        char[][] grid = new char[rows][cols];
        for (int i = 0; i < rows; i++) {
            String line = rawLines.get(i);
            for (int j = 0; j < cols; j++) {
                char c = line.charAt(j);
                if (c == 'K') {
                    exitRow = i;
                    exitCol = j;
                    grid[i][j] = '.';
                } else {
                    grid[i][j] = c;
                }
            }
        }

        validateGrid(grid);

        if (exitRow == -1 && exitCol == -1) {
            throw new IllegalArgumentException("Posisi pintu keluar 'K' tidak ditemukan atau tidak valid.");
        }

        return new Board(grid, rows, cols, exitRow, exitCol);
    }

    private static void validateGrid(char[][] grid) {
        int rows = grid.length;
        int cols = grid[0].length;

        Set<Character> visited = new HashSet<>();
        Set<Character> usedChars = new HashSet<>();
        
        boolean foundP = false;  

        for (int i = 0; i < rows; i++) {
            if (grid[i].length != cols) {
                throw new IllegalArgumentException("Baris ke-" + i + " memiliki panjang " + grid[i].length + ", seharusnya " + cols + ".");
            }
        }

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                char c = grid[i][j];

                if (c != '.' && !Character.isUpperCase(c)) {
                    throw new IllegalArgumentException("Grid hanya boleh berisi huruf kapital dan '.' (karakter '" + c + "' tidak valid di baris " + i + ", kolom " + j + ").");
                }

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

                if (usedChars.contains(c)) {
                    throw new IllegalArgumentException("Karakter '" + c + "' digunakan oleh lebih dari satu kendaraan.");
                }
                usedChars.add(c);

                if (positions.size() < 2 || positions.size() > 3) {
                    throw new IllegalArgumentException("Kendaraan '" + c + "' berukuran " + positions.size() + " sel. Panjang kendaraan harus 2 atau 3 sel.");
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

                if (c == 'P') {
                    if (foundP) {
                        throw new IllegalArgumentException("Harus ada tepat satu kendaraan 'P'. Ditemukan lebih dari satu.");
                    }
                    foundP = true;
                }
            }
        }

        if (!foundP) {
            throw new IllegalArgumentException("Harus ada tepat satu kendaraan 'P'. Tidak ditemukan.");
        }
    }
}
