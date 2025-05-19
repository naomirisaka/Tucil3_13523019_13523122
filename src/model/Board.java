package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class Board {
    public char[][] grid;
    public int rows, cols;
    public String move; // Deskripsi gerakan terakhir (opsional)
    public Board parent; // Parent untuk tracking path (opsional)

    // Posisi pintu keluar, bisa di luar grid (misal exitCol == cols artinya di luar sebelah kanan)
    private int exitRow = -1;
    private int exitCol = -1;

    // Constructor lengkap
    // public Board(char[][] grid, int rows, int cols, int exitRow, int exitCol) {
    //     this.rows = rows;
    //     this.cols = cols;
    //     this.grid = new char[rows][cols];
    //     for (int i = 0; i < rows; i++) {
    //         this.grid[i] = Arrays.copyOf(grid[i], cols);
    //     }
    //     this.exitRow = exitRow;
    //     this.exitCol = exitCol;
    // }
public Board(char[][] grid, int rows, int cols, int exitRow, int exitCol) {
    this.rows = rows;
    this.cols = cols;
    this.grid = new char[rows][cols];
    for (int i = 0; i < rows; i++) {
        this.grid[i] = Arrays.copyOf(grid[i], cols);
    }
    this.exitRow = exitRow;
    this.exitCol = exitCol;

    // Print debug di terminal
    System.out.println(">>> Konstruktor Board dipanggil:");
    System.out.println("Exit position: (" + exitRow + ", " + exitCol + ")");
    System.out.println("Ukuran papan: " + rows + "x" + cols);
    System.out.println("Grid:");
    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            System.out.print(this.grid[i][j]);
        }
        System.out.println();
    }
    System.out.println("=======================");
}


    // Constructor tanpa exit (default exit pos = -1)
    public Board(char[][] grid) {
        this.rows = grid.length;
        this.cols = (rows > 0) ? grid[0].length : 0;
        this.grid = new char[rows][cols];
        for (int i = 0; i < rows; i++) {
            this.grid[i] = Arrays.copyOf(grid[i], cols);
        }
    }

    public void setExitPosition(int row, int col) {
        this.exitRow = row;
        this.exitCol = col;
    }

    public int getExitRow() { return exitRow; }
    public int getExitCol() { return exitCol; }
    public char[][] getGrid() { return grid; }
    public int getRows() { return rows; }
    public int getCols() { return cols; }

    // Check if the board is in goal state: primary piece 'P' exits through 'K'
    public boolean isGoal() {
        java.util.List<int[]> pPositions = new java.util.ArrayList<>();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid[i][j] == 'P') {
                    pPositions.add(new int[]{i, j});
                }
            }
        }

        if (pPositions.isEmpty()) return false;

        boolean isHorizontal = pPositions.stream().allMatch(p -> p[0] == pPositions.get(0)[0]);
        boolean isVertical = pPositions.stream().allMatch(p -> p[1] == pPositions.get(0)[1]);

        if (!isHorizontal && !isVertical) return false;

        // urutkan dari depan ke belakang
        pPositions.sort((a, b) -> isHorizontal ? Integer.compare(a[1], b[1]) : Integer.compare(a[0], b[0]));

        if (isHorizontal) {
            int row = pPositions.get(0)[0];
            int leftMost = pPositions.get(0)[1];
            int rightMost = pPositions.get(pPositions.size() - 1)[1];

            // Exit di kanan
            if (exitRow == row && exitCol == rightMost + 1) {
                for (int c = rightMost + 1; c < cols; c++) {
                    if (grid[row][c] != '.') return false;
                }
                return true;
            }

            // Exit di kiri
            if (exitRow == row && exitCol == leftMost - 1) {
                for (int c = leftMost - 1; c >= 0; c--) {
                    if (grid[row][c] != '.') return false;
                }
                return true;
            }

        } else if (isVertical) {
            int col = pPositions.get(0)[1];
            int topMost = pPositions.get(0)[0];
            int bottomMost = pPositions.get(pPositions.size() - 1)[0];

            // Exit di bawah
            if (exitCol == col && exitRow == bottomMost + 1) {
                for (int r = bottomMost + 1; r < rows; r++) {
                    if (grid[r][col] != '.') return false;
                }
                return true;
            }

            // Exit di atas
            if (exitCol == col && exitRow == topMost - 1) {
                for (int r = topMost - 1; r >= 0; r--) {
                    if (grid[r][col] != '.') return false;
                }
                return true;
            }
        }

        return false;
    }

    public java.util.List<Board> getNeighbors() {
        java.util.List<Board> neighbors = new java.util.ArrayList<>();

        java.util.Set<Character> vehicles = new java.util.HashSet<>();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                char c = grid[i][j];
                if (c != '.' && c != 'K') {
                    vehicles.add(c);
                }
            }
        }

        for (char v : vehicles) {
            List<int[]> positions = new ArrayList<>();
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (grid[i][j] == v) {
                        positions.add(new int[]{i, j});
                    }
                }
            }

            if (positions.isEmpty()) continue;

            boolean horizontal = true;
            int firstRow = positions.get(0)[0];
            for (int[] pos : positions) {
                if (pos[0] != firstRow) {
                    horizontal = false;
                    break;
                }
            }

            neighbors.addAll(getAllMovesForVehicle(v, horizontal));
        }

        // Tambahan: Jika primary piece P bisa langsung keluar (goal state), buat Board baru tanpa P
        if (isGoal()) {
            char[][] newGrid = new char[rows][cols];
            for (int i = 0; i < rows; i++) {
                newGrid[i] = Arrays.copyOf(grid[i], cols);
            }

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (newGrid[i][j] == 'P') {
                        newGrid[i][j] = '.';
                    }
                }
            }

            if (canExitDirectly()) {
                Board exitBoard = new Board(copyGridWithPRemoved(), rows, cols, exitRow, exitCol);
                exitBoard.parent = this;
                exitBoard.move = "Primary piece P exits through K";
                neighbors.add(exitBoard);
            }
        }

        return neighbors;
    }

    public boolean canExitDirectly() {
        List<int[]> positions = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid[i][j] == 'P') {
                    positions.add(new int[]{i, j});
                }
            }
        }

        if (positions.isEmpty()) return false;

        positions.sort(Comparator.comparingInt(p -> p[0])); // sort by row
        int topRow = positions.get(0)[0];
        int bottomRow = positions.get(positions.size() - 1)[0];
        int col = positions.get(0)[1];

        positions.sort(Comparator.comparingInt(p -> p[1])); // sort by col
        int leftCol = positions.get(0)[1];
        int rightCol = positions.get(positions.size() - 1)[1];
        int row = positions.get(0)[0];

        // === Horizontal Exit ===
        if (exitRow == row) {
            // Exit to right
            if (exitCol > rightCol) {
                for (int j = rightCol + 1; j < exitCol; j++) {
                    if (grid[row][j] != '.') return false;
                }
                return true;
            }
            // Exit to left
            if (exitCol < leftCol) {
                for (int j = exitCol + 1; j < leftCol; j++) {
                    if (grid[row][j] != '.') return false;
                }
                return true;
            }
        }

        // === Vertical Exit ===
        if (exitCol == col) {
            // Exit to bottom
            if (exitRow > bottomRow) {
                for (int i = bottomRow + 1; i < exitRow; i++) {
                    if (grid[i][col] != '.') return false;
                }
                return true;
            }
            // Exit to top
            if (exitRow < topRow) {
                for (int i = exitRow + 1; i < topRow; i++) {
                    if (grid[i][col] != '.') return false;
                }
                return true;
            }
        }

        return false;
    }

    public char[][] copyGridWithPRemoved() {
        char[][] newGrid = new char[rows][cols];
        for (int i = 0; i < rows; i++) {
            newGrid[i] = Arrays.copyOf(grid[i], cols);
            for (int j = 0; j < cols; j++) {
                if (newGrid[i][j] == 'P') newGrid[i][j] = '.';
            }
        }
        return newGrid;
    }

    public Board moveVehicleUntilBlocked(char vehicle, boolean horizontal, boolean negativeDirection) {
        Board current = this;
        while (true) {
            if (!current.canMoveOneStep(vehicle, horizontal, negativeDirection)) break;
            Board next = current.moveVehicle(vehicle, horizontal, negativeDirection);
            if (next == null) break;
            current = next;
        }
        return current == this ? null : current;
    }

    private boolean canMoveOneStep(char vehicle, boolean horizontal, boolean negativeDirection) {
        List<int[]> positions = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid[i][j] == vehicle) {
                    positions.add(new int[]{i, j});
                }
            }
        }

        if (positions.isEmpty()) return false;

        positions.sort((a, b) -> horizontal ? Integer.compare(a[1], b[1]) : Integer.compare(a[0], b[0]));

        int dRow = horizontal ? 0 : (negativeDirection ? -1 : 1);
        int dCol = horizontal ? (negativeDirection ? -1 : 1) : 0;

        int[] edge = negativeDirection ? positions.get(0) : positions.get(positions.size() - 1);
        int newRow = edge[0] + dRow;
        int newCol = edge[1] + dCol;

        if (newRow < 0 || newRow >= rows || newCol < 0 || newCol >= cols) return false;

        // 🚫 Cek apakah cell target kosong
        return grid[newRow][newCol] == '.';
    }

    private List<Board> getAllMovesForVehicle(char vehicle, boolean horizontal) {
        List<Board> neighbors = new ArrayList<>();

        // Arah negatif dan positif (kiri-kanan atau atas-bawah)
        for (boolean negative : new boolean[]{true, false}) {
            Board moved = moveVehicleUntilBlocked(vehicle, horizontal, negative);
            if (moved != null) {
                neighbors.add(moved);
            }
        }

        return neighbors;
    }

    private Board moveVehicle(char vehicle, boolean horizontal, boolean negativeDirection) {
        char[][] newGrid = new char[rows][cols];
        for (int i = 0; i < rows; i++) {
            newGrid[i] = Arrays.copyOf(grid[i], cols);
        }

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (newGrid[i][j] == vehicle) {
                    newGrid[i][j] = '.';
                }
            }
        }

        java.util.List<int[]> positions = new java.util.ArrayList<>();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid[i][j] == vehicle) {
                    positions.add(new int[]{i, j});
                }
            }
        }
        positions.sort((a, b) -> horizontal ? Integer.compare(a[1], b[1]) : Integer.compare(a[0], b[0]));

        int shiftRow = 0, shiftCol = 0;
        if (horizontal) {
            shiftCol = negativeDirection ? -1 : 1;
        } else {
            shiftRow = negativeDirection ? -1 : 1;
        }

        for (int[] pos : positions) {
            int r = pos[0] + shiftRow;
            int c = pos[1] + shiftCol;

            if (r < 0 || r >= rows || c < 0 || c >= cols) {
                if (vehicle == 'K') continue;
                else return null;
            }

            newGrid[r][c] = vehicle;
        }

        Board newBoard = new Board(newGrid, rows, cols, exitRow, exitCol);
        newBoard.parent = this;
        newBoard.move = "Move " + vehicle + " " + (horizontal ? (negativeDirection ? "left" : "right") : (negativeDirection ? "up" : "down"));
        return newBoard;
    }

    @Override
    public String toString() {
        return toStringWithExit();
    }

    public String toStringWithExit() {
        StringBuilder sb = new StringBuilder();

        boolean pExited = isGoal();

        if (exitRow == -1 && exitCol >= 0 && exitCol < cols) {
            for (int j = 0; j < cols; j++) {
                sb.append(j == exitCol ? 'K' : '.');
            }
            sb.append('\n');
        }

        for (int i = 0; i < rows; i++) {
            if (i == exitRow && exitCol == -1) sb.append('K');
            for (int j = 0; j < cols; j++) {
                char c = grid[i][j];
                if (c == 'P' && pExited) {
                    sb.append('.');
                } else {
                    sb.append(c);
                }
            }
            if (i == exitRow && exitCol == cols) sb.append('K');
            sb.append('\n');
        }

        if (exitRow == rows && exitCol >= 0 && exitCol < cols) {
            for (int j = 0; j < cols; j++) {
                sb.append(j == exitCol ? 'K' : '.');
            }
            sb.append('\n');
        }

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Board)) return false;
        Board other = (Board) o;
        return Arrays.deepEquals(this.grid, other.grid);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(grid);
    }

    // public static Board parse(File file) throws FileNotFoundException {
    //     Scanner scanner = new Scanner(file);

    //     int rows = 0, cols = 0;
    //     while (scanner.hasNextLine()) {
    //         String line = scanner.nextLine().trim();
    //         if (!line.isEmpty()) {
    //             String[] parts = line.split("\\s+");
    //             if (parts.length == 2) {
    //                 rows = Integer.parseInt(parts[0]);
    //                 cols = Integer.parseInt(parts[1]);
    //                 break;
    //             }
    //         }
    //     }

    //     int nonPrimaryPiecesCount = 0;
    //     while (scanner.hasNextLine()) {
    //         String line = scanner.nextLine().trim();
    //         if (!line.isEmpty()) {
    //             nonPrimaryPiecesCount = Integer.parseInt(line);
    //             break;
    //         }
    //     }

    //     char[][] grid = new char[rows][cols];
    //     int exitRow = -1, exitCol = -1;

    //     int currentRow = 0;
    //     while (scanner.hasNextLine() && currentRow < rows) {
    //         String line = scanner.nextLine();
    //         if (line == null) break;
    //         line = line.trim();
    //         if (line.isEmpty()) continue;

    //         for (int j = 0; j < cols; j++) {
    //             if (j < line.length()) {
    //                 grid[currentRow][j] = line.charAt(j);
    //             } else {
    //                 grid[currentRow][j] = '.';
    //             }
    //         }

    //         if (line.length() > cols) {
    //             for (int j = cols; j < line.length(); j++) {
    //                 if (line.charAt(j) == 'K') {
    //                     exitRow = currentRow;
    //                     exitCol = j;
    //                     break;
    //                 }
    //             }
    //         }

    //         currentRow++;
    //     }

    //     scanner.close();

    //     if (currentRow != rows) {
    //         throw new IllegalArgumentException("Jumlah baris konfigurasi tidak sesuai ukuran papan.");
    //     }

    //     if ((exitRow == 0 || exitRow == rows - 1) && (exitCol == 0 || exitCol == cols - 1)) {
    //         throw new IllegalArgumentException("Pintu keluar 'K' tidak boleh berada di sudut luar papan.");
    //     }
    //     if (exitRow >= 0 && exitRow < rows && exitCol >= 0 && exitCol < cols) {
    //         throw new IllegalArgumentException("Pintu keluar 'K' tidak boleh berada di dalam grid.");
    //     }

    //     java.util.List<int[]> pPositions = new java.util.ArrayList<>();
    //     for (int i = 0; i < rows; i++) {
    //         for (int j = 0; j < cols; j++) {
    //             if (grid[i][j] == 'P') {
    //                 pPositions.add(new int[]{i, j});
    //             }
    //         }
    //     }

    //     if (pPositions.isEmpty()) {
    //         throw new IllegalArgumentException("Primary piece 'P' tidak ditemukan.");
    //     }

    //     boolean sameRow = pPositions.stream().allMatch(p -> p[0] == pPositions.get(0)[0]);
    //     boolean sameCol = pPositions.stream().allMatch(p -> p[1] == pPositions.get(0)[1]);

    //     if (!sameRow && !sameCol) {
    //         throw new IllegalArgumentException("Semua posisi 'P' harus berada di satu baris atau satu kolom.");
    //     }

    //     if (exitRow != -1 && exitCol != -1) {
    //         boolean valid = false;
    //         if (exitCol == -1 || exitCol == cols) {
    //             for (int[] p : pPositions) {
    //                 if (p[0] == exitRow) valid = true;
    //             }
    //         } else if (exitRow == -1 || exitRow == rows) {
    //             for (int[] p : pPositions) {
    //                 if (p[1] == exitCol) valid = true;
    //             }
    //         }
    //         if (!valid) {
    //             throw new IllegalArgumentException("Primary piece 'P' harus berada satu baris atau kolom dengan posisi keluar 'K'.");
    //         }
    //     }

    //     return new Board(grid, rows, cols, exitRow, exitCol);
    // }
}
