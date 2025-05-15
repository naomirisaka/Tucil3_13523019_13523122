package algo;

import java.util.List;

import model.Board;

public interface Solver {
    /**
     * Menyelesaikan puzzle Rush Hour dari papan awal
     * @param initialBoard papan awal permainan
     * @return daftar Board yang merepresentasikan langkah-langkah solusi (termasuk papan awal & akhir)
     */
    List<Board> solve(Board initialBoard);

    /**
     * Mengembalikan jumlah node (papan konfigurasi) yang telah dikunjungi selama solving
     * @return jumlah node
     */
    int getVisitedNodeCount();

    /**
     * Mengembalikan waktu eksekusi solving dalam satuan milidetik
     * @return waktu dalam ms
     */
    long getExecutionTime();
}
