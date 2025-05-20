# Tugas Kecil 3 IF2211 Strategi Algoritma
### Penyelesaian Puzzle Rush Hour Menggunakan Algoritma Pathfinding
| Nama | NIM |
|------|-----|
| Shannon Aurellius Anastasya Lie | 13523019 |
| Naomi Risaka Sitorus | 13523122 |

## Deskripsi
<p align = "justify">Rush Hour merupakan sebuah permainan puzzle logika berbasis grid dengan tujuan mengeluarkan mobil utama (primary piece) dari kemacetan menuju pintu keluar. Setiap potongan (piece) yang ada di grid dapat digerakan sesuai orientasinya, baik horizontal atau vertikal, agar tidak menghalangi jalan mobil utama. Program Rush Hour Puzzle Solver membantu mencari solusi permainan di GUI berdasarkan konfigurasi papan yang diberikan dengan pendekatan algoritma pathfinding Uniform Cost Search (UCS), Greedy Best First Search (GBFS), A* (A-Star), atau IDS (Iterative Deepening Search). Tersedia beberapa pilihan heuristik untuk digunakan dalam algoritma GBFS dan A*, yakni blocking pieces count, blocking pieces with movability, dan distance to exit.</p>

## Struktur
```
├───.gitignore
├───pom.xml
├───README.md
├───bin
│   ├───Tucil3_13523019_13523122-1.0-SNAPSHOT.jar
│   ├───classes
│   │   ├───algo
│   │   ├───fonts
│   │   ├───gui
│   │   ├───model
│   │   └───util
│   ├───generated-sources
│   ├───maven-archiver
│   ├───maven-status
│   └───test-classes
├───doc
│   └───Tucil3_13523019_13523122.pdf
├───resources
│   └───fonts
│       └───StayPlayful.ttf
├───src
│   ├───algo                  # Algoritma pathfinding
│   │   ├───AStarSolver.java
│   │   ├───GBFSSolver.java
│   │   ├───IDSSolver.java
│   │   ├───Solver.java
│   │   └───UCSSolver.java
│   ├───gui                   # GUI sekaligus main program  
│   │   └───GUIApp.java
│   ├───model                 
│   │   └───Board.java        # Papan permainan
│   └───util                  
│       ├───Heuristic.java    # Heuristik untuk evaluasi simpul
│       └───InputParser.java  # Membaca file konfigurasi papan
└───test
    ├───test1.txt
    ├───test1_sol1.txt
    ├───test1_sol2.txt
    ├───test1_sol3.txt
    ├───test1_sol4.txt
    ├───test2.txt
    ├───test2_sol5.txt
    ├───test2_sol6.txt
    ├───test2_sol7.txt
    ├───test3.txt
    ├───test4.txt
    └───test5.txt
```

## Requirements
- Java JDK versi 17 atau yang lebih baru
- JavaFX SDK versi 23 
- Maven versi 4 atau yang lebih baru
- IDE yang mendukung bahasa Java
> [!NOTE]
> Sesuaikan kembali konfigurasi file pom.xml dengan requirements yang terinstall di perangkat Anda. 

## Cara Menjalankan
1. Clone repository ini dengan menjalankan perintah di bawah ini pada terminal IDE:
   ```sh
   https://github.com/naomirisaka/Tucil3_13523019_13523122.git
2. Buka folder hasil clone di IDE.
3. Jalankan program dengan:
   ```sh
   mvn javafx:run
   
## Cara Menggunakan
1. Setelah menjalankan program, masukkan file konfigurasi papan permainan.
2. Masukkan parameter pencarian solusi permainan, termasuk algoritma yang dipilih serta heuristik yang digunakan (apabila diperlukan).
3. Tekan tombol "Solve" untuk melakukan pencarian solusi. Apabila konfigurasi tidak valid, tombol "Solve" tidak dapat ditekan dan muncul pesan error.
4. Jika solusi ditemukan, program akan menampilkan langkah-langkah pencarian solusi di GUI, baik dalam bentuk paginasi atau animasi. Solusi dapat disimpan ke dalam bentuk file teks (.txt) dengan menekan tombol "Save Solution".
5. Jika solusi tidak ditemukan, program akan menampilkan pesan "No solution was found for this puzzle.".
