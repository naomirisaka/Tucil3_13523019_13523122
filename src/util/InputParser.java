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

        /* 1. Baca ukuran papan */
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

        /* 2. Baca jumlah kendaraan non‑primer */
        int nonPrimaryPiecesCount = 0;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                nonPrimaryPiecesCount = Integer.parseInt(line);
                break;
            }
        }

        /* 3. Ambil semua baris konfigurasi TANPA trim */
        List<String> rawLines = new ArrayList<>();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();          // spasi dipertahankan
            if (!line.replace(" ", "").isEmpty()) {    // abaikan baris kosong murni
                rawLines.add(line);
            }
        }
        scanner.close();

        /* --- DETEKSI EXIT --- */
        int exitRow = -2;          // sentinel: belum ditemukan
        int exitCol = -2;

        // Cek 'K' di atas grid
        if (rawLines.size() > rows && rawLines.get(0).indexOf('K') != -1) {
            exitRow = -1;
            exitCol = rawLines.get(0).indexOf('K');
        }

        // Cek 'K' di bawah grid
        if (exitRow == -2 && rawLines.size() > rows &&
            rawLines.get(rawLines.size() - 1).indexOf('K') != -1) {

            exitRow = rows;
            exitCol = rawLines.get(rawLines.size() - 1).indexOf('K');
        }

        /* Grid lines = baris terakhir sebanyak rows */
        List<String> gridLines = new ArrayList<>(
                rawLines.subList(rawLines.size() - rows, rawLines.size())
        );

        // Cari 'K' di sisi kiri / kanan grid
        for (int i = 0; i < rows && exitRow == -2; i++) {
            String ln = gridLines.get(i);
            if (ln.length() > 0 && ln.charAt(0) == 'K') {        // kiri
                exitRow = i;
                exitCol = -1;
            } else if (ln.length() > cols && ln.charAt(cols) == 'K') { // kanan
                exitRow = i;
                exitCol = cols;
            }
        }

        if (exitRow == -2) {   // masih sentinel
            throw new IllegalArgumentException("K (exit) tidak ditemukan.");
        }

        /* ---------- NORMALISASI BARIS GRID ---------- */
        for (int i = 0; i < rows; i++) {
            String ln = gridLines.get(i);

            if (exitCol == -1) {                 // exit di kolom kiri
                // Buang kolom 0 (bisa 'K' di baris exit, atau spasi di baris lain)
                if (ln.length() > 0) ln = ln.substring(1);
            } else if (exitCol == cols) {        // exit di kolom kanan
                // Buang kolom (cols)  (bisa 'K' atau spasi)
                if (ln.length() > cols) ln = ln.substring(0, cols);
            }

            // Pastikan panjang tepat cols, tambal dengan '.' bila kurang
            if (ln.length() < cols) {
                ln = ln + ".".repeat(cols - ln.length());
            }
            gridLines.set(i, ln);
        }

        /* ---------- KONVERSI KE ARRAY CHAR ---------- */
        char[][] grid = new char[rows][cols];
        for (int i = 0; i < rows; i++) {
            String ln = gridLines.get(i);
            for (int j = 0; j < cols; j++) {
                char c = ln.charAt(j);
                grid[i][j] = (c == 'K' || c == ' ') ? '.' : c;   // seharusnya sudah tak ada K lagi
            }
        }

        /* ---------- DEBUG PRINT ---------- */
        System.out.println("=== Hasil Parsing ===");
        System.out.printf("Ukuran papan: %dx%d%n", rows, cols);
        System.out.println("Jumlah kendaraan non-primer: " + nonPrimaryPiecesCount);
        if (exitRow == -1)
            System.out.println("Exit: baris di atas, kolom " + exitCol);
        else if (exitRow == rows)
            System.out.println("Exit: baris di bawah, kolom " + exitCol);
        else if (exitCol == -1)
            System.out.println("Exit: kolom kiri, baris " + exitRow);
        else if (exitCol == cols)
            System.out.println("Exit: kolom kanan, baris " + exitRow);
        else
            System.out.printf("Exit: di dalam grid, baris %d, kolom %d%n", exitRow, exitCol);

        System.out.println("Grid:");
        gridLines.forEach(System.out::println);
        System.out.println("=====================");

        return new Board(grid, rows, cols, exitRow, exitCol);
    }
}
