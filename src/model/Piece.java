package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Piece {
    public char id;
    public int row, col;
    public int length;
    public boolean isHorizontal;

    public Piece(char id, int row, int col, int length, boolean isHorizontal) {
        this.id = id;
        this.row = row;
        this.col = col;
        this.length = length;
        this.isHorizontal = isHorizontal;
    }

    public List<int[]> getOccupiedCells() {
        List<int[]> positions = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            if (isHorizontal) {
                positions.add(new int[]{row, col + i});
            } else {
                positions.add(new int[]{row + i, col});
            }
        }
        return positions;
    }

    public char getOrientation() {
        return isHorizontal ? 'H' : 'V';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Piece)) return false;
        Piece p = (Piece) o;
        return id == p.id && row == p.row && col == p.col && length == p.length && isHorizontal == p.isHorizontal;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, row, col, length, isHorizontal);
    }
}
