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
        int kCount = 0;          // <-- hitung semua kemunculan 'K'

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

        // Nilai A, B, dan N harus > 0
        if (rows <= 0 || cols <= 0 || pieceAmt <= 0) {
            throw new IllegalArgumentException(
                "A, B, and N must be greater than 0 (A=" + rows +
                ", B=" + cols + ", N=" + pieceAmt + ")");
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
                kCount++;
                exitRow = -1;
                exitCol = top.length() - 1;
                rawLines.remove(0);
                if (exitCol > cols - 1 || exitCol <= 0) {
                    throw new IllegalArgumentException("The position of 'K' is invalid.");
                }
            }
        }   

        if (!rawLines.isEmpty()) {
            String bottom = rawLines.get(rawLines.size() - 1);
            if (bottom.trim().equals("K")) {
                kCount++;
                exitRow = rows;
                exitCol = bottom.length() - 1;
                rawLines.remove(rawLines.size() - 1);
                if (exitCol > cols - 1 || exitCol <= 0) {
                    throw new IllegalArgumentException("The position of 'K' is invalid.");
                }                    
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
                        kCount++;
                        rawLines.remove(0);
                        foundK = true;
                        break;
                    }
                }
            }
            if (foundK && rawLines.size() < rows) {
                throw new IllegalArgumentException("Number of board rows is less than configuration after removing top K row.");
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
                throw new IllegalArgumentException("Number of board rows is less than configuration after removing bottom K row.");
            }
        }

        if (rawLines.size() != rows) {
            throw new IllegalArgumentException("Number of board rows (" + rawLines.size() + ") does not match the configured rows = " + rows);
        }

        for (int i = 0; i < rows; i++) {
            String line = rawLines.get(i);
            rawLines.set(i, line.trim());
        }

        for (int i = 0; i < rows; i++) {
            String line = rawLines.get(i);

            if (line.length() < cols) {
                throw new IllegalArgumentException("Row " + i + " has length less than " + cols);
            }

            if (line.length() > cols) {
                if (line.charAt(0) == 'K') {
                    kCount++;
                    exitRow = i;
                    exitCol = -1; // K di pinggir kiri
                    line = line.substring(1);
                } else if (line.charAt(line.length() - 1) == 'K') {
                    kCount++;
                    exitRow = i;
                    exitCol = cols; // K di pinggir kanan
                    line = line.substring(0, line.length() - 1);
                } else {
                    throw new IllegalArgumentException("Row " + i + " has length more than " + cols + " without 'K' on the edge.");
                }

                if (line.length() != cols) {
                    throw new IllegalArgumentException("After trimming, row " + i + " length does not match the specified column count.");
                }
                rawLines.set(i, line);
            }

            for (int j = 0; j < cols; j++) {
                if (line.charAt(j) == 'K') {
                    throw new IllegalArgumentException("Character 'K' must only appear at the edge of the board (row " + i + ", column " + j + ").");
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
                    grid[i][j] = '.';   // treat as empty
                } else {
                    grid[i][j] = c;
                }
            }
        }

        validateGrid(grid);

        // Validasi jumlah kendaraan based on N (tanpa P & K)
        Set<Character> pieces = new HashSet<>();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                char c = grid[i][j];
                if (c != '.' && c != 'P') {   // abaikan P dan titik
                    pieces.add(c);
                }
            }
        }
        if (pieces.size() != pieceAmt) {
            throw new IllegalArgumentException(
                "Number of different vehicles (" + pieces.size() +
                ") does not match N = " + pieceAmt);
        }

        if (kCount != 1) {
            throw new IllegalArgumentException(
                "There must be exactly one 'K' character. Found " + kCount + ".");
        }

        if (exitRow == -1 && exitCol == -1) {
            throw new IllegalArgumentException("Exit position 'K' not found or invalid.");
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
                throw new IllegalArgumentException("Row " + i + " has length " + grid[i].length + ", expected " + cols + ".");
            }
        }

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                char c = grid[i][j];

                if (c != '.' && !Character.isUpperCase(c)) {
                    throw new IllegalArgumentException("Grid may only contain uppercase letters and '.' (invalid character '" + c + "' at row " + i + ", column " + j + ").");
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
                    throw new IllegalArgumentException("Character '" + c + "' is used by more than one vehicle.");
                }
                usedChars.add(c);

                if (positions.size() < 2 || positions.size() > 3) {
                    throw new IllegalArgumentException("Vehicle '" + c + "' has size " + positions.size() + " cells. Vehicle length must be 2 or 3 cells.");
                }

                boolean sameRow = true, sameCol = true;
                int baseRow = positions.get(0)[0];
                int baseCol = positions.get(0)[1];
                for (int[] pos : positions) {
                    if (pos[0] != baseRow) sameRow = false;
                    if (pos[1] != baseCol) sameCol = false;
                }
                if (!sameRow && !sameCol) {
                    throw new IllegalArgumentException("Vehicle '" + c + "' is not aligned straight (possibly diagonal).");
                }

                // setelah pengecekan sameRow / sameCol:
                if (sameRow) {
                    int min = positions.stream().mapToInt(p -> p[1]).min().getAsInt();
                    int max = positions.stream().mapToInt(p -> p[1]).max().getAsInt();
                    if (max - min + 1 != positions.size()) {
                        throw new IllegalArgumentException("Vehicle '" + c + "' is not contiguous (has a gap).");
                    }
                } else { // sameCol
                    int min = positions.stream().mapToInt(p -> p[0]).min().getAsInt();
                    int max = positions.stream().mapToInt(p -> p[0]).max().getAsInt();
                    if (max - min + 1 != positions.size()) {
                        throw new IllegalArgumentException("Vehicle '" + c + "' is not contiguous (has a gap).");
                    }
                }

                if (c == 'P') {
                    if (foundP) {
                        throw new IllegalArgumentException("There must be exactly one primary piece. None found.");
                    }
                    foundP = true;
                }
            }
        }

        if (!foundP) {
            throw new IllegalArgumentException("There must be exactly one primary piece. None was found.");
        }
    }
}
