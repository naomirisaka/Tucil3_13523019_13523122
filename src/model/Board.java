package model;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Board {
    public char[][] grid;
    public int rows, cols;
    public int exitRow = -1, exitCol = -1;
    public Board parent = null;

    public Map<Character, Piece> pieces;

    public Board(char[][] grid, int rows, int cols, int exitRow, int exitCol, Map<Character, Piece> pieces) {
        this.rows = rows;
        this.cols = cols;
        this.exitRow = exitRow;
        this.exitCol = exitCol;

        this.grid = new char[rows][cols];
        for (int i = 0; i < rows; i++) {
            this.grid[i] = Arrays.copyOf(grid[i], cols);
        }

        this.pieces = new HashMap<>();
        for (var e : pieces.entrySet()) {
            this.pieces.put(e.getKey(), e.getValue().copy());
        }
    }

    public void buildGridFromPieces() {
        for (int i = 0; i < rows; i++) {
            Arrays.fill(grid[i], '.');
        }

        for (Piece p : pieces.values()) {
            int r = p.row;
            int c = p.col;
            for (int i = 0; i < p.size; i++) {
                if (r < 0 || r >= rows || c < 0 || c >= cols) {
                    // Untuk K bisa keluar grid, skip
                    if (p.id == 'K') break;
                    else throw new RuntimeException("Piece " + p.id + " keluar batas grid");
                }
                grid[r][c] = p.id;
                if (p.horizontal) c++;
                else r++;
            }
        }
    }

    public static Board parse(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);

        int rows = 0, cols = 0;
        // Baca dimensi
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

        char[][] rawGrid = new char[rows][cols];
        int exitRow = -1, exitCol = -1;

        int currentRow = 0;
        while (scanner.hasNextLine() && currentRow < rows) {
            String line = scanner.nextLine();
            if (line == null) break;
            line = line.trim();
            if (line.isEmpty()) continue;

            for (int j = 0; j < cols; j++) {
                if (j < line.length()) {
                    rawGrid[currentRow][j] = line.charAt(j);
                } else {
                    rawGrid[currentRow][j] = '.';
                }
            }

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

        Map<Character, Piece> pieces = detectPieces(rawGrid, rows, cols);

        Board board = new Board(rawGrid, rows, cols, exitRow, exitCol, pieces);
        board.buildGridFromPieces();
        return board;
    }

    public boolean isGoal() {
        Piece player = pieces.get('P');
        Piece exit = pieces.get('K');

        if (player == null || exit == null) return false;

        List<int[]> playerPositions = getPiecePositions(player);
        for (int[] posP : playerPositions) {
            int r = posP[0];
            int c = posP[1];

            int[][] dirs = { {-1,0}, {1,0}, {0,-1}, {0,1} };
            for (int[] d : dirs) {
                int nr = r + d[0];
                int nc = c + d[1];

                if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) {
                    if (nr == exitRow && nc == exitCol) return true;
                } else {
                    if (grid[nr][nc] == 'K') return true;
                }
            }
        }

        return false;
    }

    private List<int[]> getPiecePositions(Piece p) {
        List<int[]> posList = new ArrayList<>();
        int r = p.row, c = p.col;
        for (int i = 0; i < p.size; i++) {
            posList.add(new int[]{r, c});
            if (p.horizontal) c++;
            else r++;
        }
        return posList;
    }

    public List<Board> getNeighbors() {
        List<Board> neighbors = new ArrayList<>();

        for (Piece piece : pieces.values()) {
            // Try to move forward (+1)
            Board forward = movePiece(piece, +1);
            if (forward != null) {
                forward.parent = this;
                neighbors.add(forward);
            }
            // Try to move backward (-1)
            Board backward = movePiece(piece, -1);
            if (backward != null) {
                backward.parent = this;
                neighbors.add(backward);
            }
        }

        return neighbors;
    }

    private Board movePiece(Piece p, int direction) {
        // Make a copy of pieces, updating the piece to move
        Map<Character, Piece> newPieces = new HashMap<>();
        for (var e : pieces.entrySet()) {
            if (e.getKey() == p.id) {
                Piece moved = p.copy();
                if (moved.horizontal) {
                    moved.col += direction;
                } else {
                    moved.row += direction;
                }
                // Check if move is legal (inside board and no collisions)
                if (!isMoveLegal(moved, newPieces)) {
                    return null; // illegal move, reject
                }
                newPieces.put(moved.id, moved);
            } else {
                newPieces.put(e.getKey(), e.getValue().copy());
            }
        }

        Board newBoard = new Board(new char[rows][cols], rows, cols, exitRow, exitCol, newPieces);
        newBoard.buildGridFromPieces();
        return newBoard;
    }

    private boolean isMoveLegal(Piece movedPiece, Map<Character, Piece> piecesMap) {
        // Check boundaries
        if (movedPiece.row < 0 || movedPiece.row >= rows || movedPiece.col < 0 || movedPiece.col >= cols) {
            return false;
        }

        // Check collision with other pieces
        for (Piece other : piecesMap.values()) {
            if (other.id == movedPiece.id) continue;
            if (piecesOverlap(movedPiece, other)) return false;
        }
        return true;
    }

    private boolean piecesOverlap(Piece a, Piece b) {
        // Assuming pieces have length attribute and orientation, check if positions overlap

        // Get all cells occupied by a
        List<int[]> cellsA = a.getOccupiedCells();
        List<int[]> cellsB = b.getOccupiedCells();

        for (int[] cellA : cellsA) {
            for (int[] cellB : cellsB) {
                if (cellA[0] == cellB[0] && cellA[1] == cellB[1]) return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                sb.append(grid[i][j]);
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    public static Map<Character, Piece> detectPieces(char[][] grid, int rows, int cols) {
        Map<Character, Piece> pieces = new HashMap<>();

        Set<Character> seen = new HashSet<>();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                char c = grid[i][j];
                if (c == '.' || c == 'K') continue;
                if (seen.contains(c)) continue;

                List<int[]> positions = new ArrayList<>();
                for (int r = 0; r < rows; r++) {
                    for (int col = 0; col < cols; col++) {
                        if (grid[r][col] == c) {
                            positions.add(new int[]{r, col});
                        }
                    }
                }

                boolean horizontal = true;
                int firstRow = positions.get(0)[0];
                for (var pos : positions) {
                    if (pos[0] != firstRow) {
                        horizontal = false;
                        break;
                    }
                }

                int size = positions.size();

                int minRow = positions.stream().mapToInt(p -> p[0]).min().orElse(0);
                int minCol = positions.stream().mapToInt(p -> p[1]).min().orElse(0);

                pieces.put(c, new Piece(c, minRow, minCol, horizontal, size));
                seen.add(c);
            }
        }
        return pieces;
    }
}
