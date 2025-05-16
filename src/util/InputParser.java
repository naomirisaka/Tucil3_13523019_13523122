package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Map;

import model.Board;
import model.Piece;

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

            // Cek karakter di dalam grid
            for (int j = 0; j < cols; j++) {
                if (j < line.length()) {
                    char c = line.charAt(j);
                    if (c == 'K') {
                        // ❌ K tidak boleh berada di dalam grid
                        scanner.close();
                        throw new RuntimeException("Exit (K) tidak boleh berada di dalam grid pada baris " + currentRow + ", kolom " + j);
                    } else {
                        grid[currentRow][j] = c;
                    }
                } else {
                    grid[currentRow][j] = '.';
                }
            }

            // ✅ Cek K di luar grid
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

        if (exitRow == -1 || exitCol == -1) {
            throw new RuntimeException("Exit (K) tidak ditemukan di luar grid.");
        }

        Map<Character, Piece> pieces = Board.detectPieces(grid, rows, cols);

        Board board = new Board(grid, rows, cols, exitRow, exitCol, pieces);
        board.buildGridFromPieces();

        return board;
    }
}
