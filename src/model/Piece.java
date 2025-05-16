package model;

import java.util.List;
import java.util.ArrayList;

public class Piece {
    public char id;
    public int row, col;        // posisi kepala (kiri atas)
    public boolean horizontal;  // true = horizontal, false = vertical
    public int size;            // panjang kendaraan

    public Piece(char id, int row, int col, boolean horizontal, int size) {
        this.id = id;
        this.row = row;
        this.col = col;
        this.horizontal = horizontal;
        this.size = size;
    }

    public Piece copy() {
        return new Piece(id, row, col, horizontal, size);
    }

    @Override
    public String toString() {
        return String.format("Piece{id=%c, row=%d, col=%d, hor=%b, size=%d}", id, row, col, horizontal, size);
    }

    public List<int[]> getOccupiedCells() {
        List<int[]> cells = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            int r = row + (horizontal ? 0 : i);
            int c = col + (horizontal ? i : 0);
            cells.add(new int[]{r, c});
        }
        return cells;
    }
}
