package util;

import model.Board;

public class Heuristic {
    public static int blockingCars(Board board) {
        int row = 2; // Asumsi primary piece selalu di baris ke-3
        int count = 0;
        boolean started = false;
        for (int j = 0; j < board.cols; j++) {
            char c = board.grid[row][j];
            if (c == 'P') started = true;
            else if (started && c != '.' && c != 'K') count++;
        }
        return count;
    }
}
