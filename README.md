# SLATE - Sistem Informasi Pengelolaan Stok, Pesanan, dan Pengiriman Semen

<div align="center">
  <img width="900" alt= src=<img width="1100" height="270" alt="image"<img width="1600" height="640" alt="image" src="https://github.com/user-attachments/assets/78ab3711-41b3-420a-bf97-99c493f7a34c" />

</div>

## Deskripsi Singkat

SLATE adalah sistem berbasis Java untuk mengelola operasional sebuah perusahaan semen yang memiliki pabrik sendiri. Sistem ini dipakai secara internal oleh dua peran, yaitu **Admin Gudang** yang mengelola data semen dan stok hasil produksi, serta **Staf Penjualan** yang mengelola data pelanggan, mencatat pesanan, dan memperbarui status pembayaran serta pengiriman. Seluruh data tersimpan di database MySQL sehingga stok, pesanan, dan riwayat transaksi selalu terpusat dan terdokumentasi.

## Fitur Utama Program

**Umum**
- Login dengan pemilihan hak akses (Admin Gudang atau Staf Penjualan).
- Logout dengan konfirmasi.

**Admin Gudang**
- Melihat daftar semen beserta harga, stok, dan batas aman. Semen yang stoknya berada di bawah atau sama dengan batas aman ditandai **Kritis**.
- Menambah stok semen hasil produksi beserta nomor batch. Setiap penambahan tercatat atas nama admin yang bertugas.
- Mengubah data semen (nama, satuan, harga jual, batas aman).
- Menambah jenis semen baru dan menghapus semen yang tidak dipakai. Semen yang sudah punya riwayat transaksi tidak dapat dihapus (proteksi foreign key).

**Staf Penjualan**
- Mengelola data pelanggan (toko bangunan atau kontraktor): tambah, cari, ubah, dan hapus.
- Mencatat pesanan dengan beberapa jenis semen sekaligus. Sistem mengecek stok, menghitung total tagihan, mencatat metode dan status pembayaran, lalu memotong stok.
- Memperbarui status pembayaran (Belum Lunas atau Lunas) dan status pengiriman (Diproses, Dikirim, Diterima, atau Refund). Pada status Refund, stok dipulihkan dan pembayaran dicatat sebagai refund.
- Melihat riwayat dan rekap pesanan pada halaman menu.

## Tech Stack

| Komponen | Teknologi |
|---|---|
| Bahasa | Java |
| Antarmuka | Java Swing (NetBeans GUI Builder) |
| Database | MySQL 8.0 (dikelola lewat Laragon dan phpMyAdmin) |
| Konektivitas | JDBC |
| Build tool | Apache Maven |
| Library | `mysql-connector-j` 8.3.0, `exec-maven-plugin` 3.1.0 |
| IDE | Apache NetBeans |

Aplikasi tidak memakai framework web. Seluruh antarmuka dibangun dengan Swing/AWT bawaan JDK.

## Perancangan Sistem

### Flowchart

Flowchart adalah diagram yang menggambarkan urutan langkah atau alur kerja suatu proses menggunakan simbol-simbol tertentu. Flowchart digunakan untuk menjelaskan bagaimana program berjalan, mulai dari proses awal, keputusan, hingga proses berakhir.

<details>
  <summary>Halaman Menu Login</summary>
  <img width="1097" height="1012" alt="FlowchartSLATE (2)-Halaman Login drawio" src="https://github.com/user-attachments/assets/606618d5-662a-4140-9344-95fcd2c34d6d" />


  <p align="justify">Alur sistem dimulai ketika pengguna memasukkan username dan password pada halaman login. Sistem kemudian memvalidasi data tersebut dengan database. Jika data sesuai, pengguna diarahkan ke halaman berdasarkan role, yaitu Admin Gudang atau Staf Penjualan. Jika data tidak sesuai, sistem menampilkan pesan kesalahan dan meminta pengguna login kembali.</p>
</details>


<details>
  <summary>Halaman Menu Staf Penjualan</summary>
  <img width="1849" height="561" alt="FlowchartSLATE (2)-Halaman Menu Staf Penjualan drawio" src="https://github.com/user-attachments/assets/af5c4e6c-7ea1-4f41-b081-7dfd3cb42cbb" />


  <p align="justify">Setelah login berhasil, sistem mengambil data pesanan dari database dan menampilkan riwayat serta rekap pesanan. Staf Penjualan dapat memilih menu Kelola Data Pelanggan, Pencatatan Pesanan, atau Pembaruan Status Pembayaran/Pengiriman. Setiap menu akan mengarahkan pengguna ke fitur yang sesuai. Jika memilih keluar, sistem meminta konfirmasi sebelum kembali ke halaman login.</p>
</details>

<details>
  <summary>Fitur Kelola Data Pelanggan-Staf Penjualan</summary>
  <img width="2467" height="1822" alt="FlowchartSLATE (2)-Fitur Kelola Data Pelanggan-Staf Penjualan drawio" src="https://github.com/user-attachments/assets/5ced0fa6-b1ba-449e-9de1-2da32c7f5f78" />


  <p align="justify">Pada fitur ini, sistem menampilkan daftar pelanggan yang tersimpan dalam database. Staf Penjualan dapat menambahkan pelanggan baru, mencari pelanggan berdasarkan nama toko atau kontraktor, serta mengubah dan menghapus data pelanggan. Setiap proses perubahan dilakukan melalui validasi data dan konfirmasi untuk menghindari kesalahan. Setelah proses selesai, sistem kembali ke halaman pengelolaan pelanggan.</p>
</details>

<details>
  <summary>Fitur Pencatatan Pesanan-Staf Penjualan</summary>
  <img width="767" height="2481" alt="FlowchartSLATE (2)-Fitur Pencatatan Pesanan-Staf Penjualan drawio" src="https://github.com/user-attachments/assets/4c61424a-ceac-4538-accf-128b0bdf83fb" />


  <p align="justify">Proses pencatatan pesanan dimulai dengan menampilkan data pelanggan yang tersedia. Staf Penjualan memilih pelanggan dan memasukkan pesanan semen, kemudian sistem memvalidasi data pelanggan serta ketersediaan stok. Jika stok mencukupi, sistem menghitung total tagihan dan memeriksa status pembayaran. Pesanan yang telah lunas akan diproses dan stok dikurangi, sedangkan pesanan yang belum lunas menunggu pembayaran atau dibatalkan.</p>
</details>

<details>
  <summary>Fitur Pembaruan Status Pembayaran/Pengiriman-Staf Penjualan</summary>
  <img width="1642" height="1282" alt="FlowchartSLATE (2)-Fitur Pembaruan Status Pengiriman-Staf Penjualan drawio" src="https://github.com/user-attachments/assets/f0f8bc8f-311c-4bea-8317-b731b19d5ae8" />


  <p align="justify">Staf Penjualan memasukkan ID pesanan yang ingin diperbarui, kemudian sistem memvalidasi keberadaan pesanan tersebut di database. Setelah data sesuai, staf dapat memilih untuk memperbarui status pembayaran atau status pengiriman. Pembayaran yang telah lunas akan memicu pemotongan stok apabila belum dilakukan sebelumnya. Jika pesanan dibatalkan, sistem mengembalikan stok semen dan membatalkan proses pengiriman.</p>
</details>

<details>
  <summary>Halaman Menu Admin Gudang</summary>
  <img width="2037" height="449" alt="FlowchartSLATE (2)-Halaman Menu Admin Gudang drawio" src="https://github.com/user-attachments/assets/1f30be22-4cc3-475f-8921-1e33a56ec8cb" />


  <p align="justify">Setelah login, sistem mengambil data persediaan semen dari database dan menampilkan daftar semen beserta pilihan menu yang tersedia. Admin Gudang dapat memilih fitur Tambah Stok Semen, Ubah Data Semen, atau Tambah/Hapus Semen sesuai kebutuhan. Setiap fitur digunakan untuk mengelola persediaan dan informasi produk. Jika admin memilih keluar, sistem meminta konfirmasi sebelum kembali ke halaman login.</p>
</details>

<details>
  <summary>Fitur Tambah Stok Semen-Admin Gudang</summary>
  <img width="699" height="816" alt="FlowchartSLATE (2)-Fitur Tambah Stok Semen-Admin Gudang drawio" src="https://github.com/user-attachments/assets/3971a976-8e88-4dab-b921-eaf972693bbc" />


  <p align="justify">Admin Gudang memasukkan jumlah stok semen yang ingin ditambahkan ke dalam sistem. Sistem kemudian memvalidasi nominal yang dimasukkan untuk memastikan jumlah stok sesuai. Jika input tidak valid, sistem menampilkan pesan kesalahan dan meminta admin mengulangi input. Jika valid, stok disimpan ke database dan sistem menampilkan pemberitahuan bahwa penambahan stok berhasil.</p>
</details>

<details>
  <summary>Fitur Ubah Data Semen-Admin Gudang</summary>
  <img width="504" height="1712" alt="FlowchartSLATE (2)-Fitur Ubah Data Semen-Admin Gudang drawio" src="https://github.com/user-attachments/assets/73fe969d-c5da-4094-b2e9-79a188e5a324" />


  <p align="justify">Admin Gudang memilih ID semen yang ingin diperbarui, kemudian sistem memeriksa keberadaan data tersebut di database. Jika data ditemukan, admin dapat memasukkan informasi baru seperti harga jual dan detail semen. Sistem memvalidasi data yang dimasukkan sebelum meminta konfirmasi perubahan. Setelah perubahan dikonfirmasi, data diperbarui di database dan sistem menampilkan pemberitahuan keberhasilan.</p>
</details>

<details>
  <summary>Fitur Tambah/Hapus Semen-Admin Gudang</summary>
  <img width="1372" height="1557" alt="FlowchartSLATE (2)-Fitur Tambah_Hapus Semen-Admin Gudang drawio" src="https://github.com/user-attachments/assets/284d3963-37d2-46d4-abb4-bf07473707be" />


  <p align="justify">Pada fitur ini, Admin Gudang dapat memilih untuk menambahkan jenis semen baru atau menghapus data semen yang tersedia. Penambahan dilakukan dengan memasukkan nama semen, satuan, harga jual, stok, dan batas aman yang kemudian divalidasi oleh sistem. Untuk penghapusan, admin memasukkan ID semen yang ingin dihapus dan melakukan konfirmasi. Setelah proses berhasil, sistem menyimpan perubahan ke database dan kembali ke halaman Admin Gudang.</p>
</details>

---

### Use Case

Use case diagram digunakan untuk memetakan hubungan interaksi antara aktor (pengguna atau sistem eksternal) dengan fungsi-fungsi utama yang disediakan oleh sistem program secara visual. 

<details>
  <summary>Use Case SLATE</summary>
  <img width="2152" height="1817" alt="Untitled Diagram drawio (8)" src="https://github.com/user-attachments/assets/d4984b54-f2d5-4a0e-9622-35ebeeec23c6" />


  <p align="justify">Alur program diawali dengan use case LOGIN sebagai syarat akses bagi Staf Penjualan dan Admin Gudang. Staf Penjualan mengelola data pelanggan, mulai dari menambah pembeli, menyimpan alamat, hingga memperbarui informasi kontak. Staf juga membuat pesanan multi-item melalui pemilihan pelanggan, penambahan semen, pengecekan stok, perhitungan tagihan, dan pemotongan stok otomatis.

Staf Penjualan dapat memperbarui status pengiriman dan melakukan pembatalan serta refund apabila diperlukan. Proses pembatalan mencakup pengembalian uang, pemulihan stok, dan pencatatan status pesanan. Staf juga dapat melihat riwayat pesanan, rincian barang, bukti pelunasan, total pendapatan, serta laporan semen paling laku.

Admin Gudang bertanggung jawab mengelola persediaan semen, termasuk menambahkan stok masuk dan mengelola data semen. Admin dapat melihat katalog dan harga, mengubah informasi produk, serta menghapus data semen. Sistem juga menyediakan peringatan otomatis apabila stok berada di bawah batas aman. Seluruh aktivitas Admin Gudang diawali dengan proses LOGIN.</p>
</details>

---

### ERD

ERD (Entity Relationship Diagram) adalah diagram yang menggambarkan struktur database, termasuk entitas atau tabel, atribut, serta hubungan antarentitas. ERD digunakan untuk merancang database agar data tersusun dengan baik dan saling terhubung.

<details>
  <summary>Logical</summary>
  <img width="1407" height="842" alt="Slate Logical" src="https://github.com/user-attachments/assets/bd6b1874-6ee8-4f24-a64a-3e4ee97dcacc" />


  <p align="justify">ERD Logical menggambarkan struktur entitas serta hubungan antarentitas dalam sistem. Entitas pengguna menerapkan konsep supertype/subtype yang dibagi menjadi staf_penjualan dan admin_gudang sesuai tanggung jawab masing-masing. Struktur ini mencakup pengelolaan pelanggan, pesanan, pembayaran, semen, serta pencatatan stok masuk. Setiap entitas saling terhubung untuk mendukung proses penjualan dan pergudangan secara terintegrasi.</p>
</details>

<details>
  <summary>Relational</summary>
  <img width="1535" height="1032" alt="Slate Relational Revisi" src="https://github.com/user-attachments/assets/c17ee1e4-fff0-4310-a1cc-77c9f5fc354b" />


  <p align="justify">ERD Relational merupakan penerapan struktur ERD Logical ke dalam bentuk tabel basis data yang dilengkapi atribut, primary key, dan foreign key. Staf_penjualan mencatat pesanan pelanggan yang terhubung dengan pembayaran melalui relasi One-to-One. Pesanan yang terdiri dari beberapa jenis semen dikelola melalui tabel detail_pesanan, sedangkan stok masuk dicatat oleh admin_gudang melalui tabel stok_masuk dan detail_stok_masuk. Hubungan antar tabel ini mendukung pencatatan transaksi, pembayaran, serta pembaruan persediaan semen.</p>
</details>

---

## Struktur Folder/Package

Project disusun dalam beberapa lapisan agar tanggung jawab tiap bagian terpisah, yaitu tampilan, logika bisnis, akses data, dan model data. Alur pemanggilan umumnya adalah `View → Service → DAO → Koneksi → MySQL`. Pada beberapa form, operasi sederhana memanggil DAO secara langsung.

<img width="438" height="534" alt="Screenshot 2026-11-07 165838" src="https://github.com/user-attachments/assets/bbe21fb9-9e6b-4313-bc51-69efffbd2d65" />

<img width="422" height="514" alt="Screenshot 2026-11-07 165857" src="https://github.com/user-attachments/assets/cd849bfd-1e1c-4b75-be9c-9270f6380b37" />

- **`Config`**
  - `Koneksi.java`: mengatur koneksi JDBC ke MySQL (URL, user, dan password) dan menyediakan koneksi untuk seluruh DAO.
- **`Main`**
  - `Slate.java`: titik masuk program. Menjalankan `main()` dan menampilkan `FormLogin`.
- **`Model`**
  - `Pengguna`, `AdminGudang`, `StafPenjualan`: class data pengguna. `Pengguna` adalah abstract class yang diwarisi `AdminGudang` dan `StafPenjualan`.
  - `Pelanggan`, `Semen`, `Pesanan`, `Pembayaran`: class data yang merepresentasikan tabel di database.
  - `DetailNota`: menampung rincian nota pesanan (tanggal, total, pelanggan, alamat kirim, metode dan status bayar, serta daftar muatan) untuk ditampilkan.
- **`dao`**
  - `SemenDAO`, `PelangganDAO`, `PesananDAO`, `AdminGudangDAO`: *Data Access Object* yang menjalankan query SQL (tambah, ubah, hapus, dan ambil data).
  - `SemenDAOInterface`, `PelangganDAOInterface`, `PesananDAOInterface`, `AdminGudangDAOInterface`: interface yang menetapkan daftar method yang wajib dimiliki tiap DAO.
  - `PenggunaDAO`: menangani proses login, yaitu mencocokkan nama pengguna dan sandi, lalu mengembalikan objek `AdminGudang` atau `StafPenjualan` sesuai role.
- **`Service`**
  - `SemenService`, `PelangganService`, `PesananService`: lapisan logika bisnis di antara tampilan dan DAO. `PesananService` menyatukan proses pencatatan pesanan, pembaruan status, dan refund.
- **`Util`**
  - `SesiPengguna`: menyimpan data pengguna yang sedang login selama aplikasi berjalan.
  - `Validasi`: fungsi bantu validasi input (angka, nominal, nomor telepon 8 sampai 15 digit, dan konfirmasi y/n).
- **`View`**
  - `FormLogin`: halaman login dan pemilihan hak akses.
  - `FormMenuAdminGudang`, `FormMenuStaf`: menu utama Admin Gudang dan Staf Penjualan.
  - `FormTambahStokSemen`, `FormUbahDataSemen`, `FormKelolaSemen`: fitur Admin Gudang, yaitu tambah stok, ubah data semen, serta tambah dan hapus semen.
  - `FormKelolaPelanggan`, `FormPencatatanPesanan`, `FormStatusPengiriman`: fitur Staf Penjualan, yaitu kelola pelanggan, pencatatan pesanan, dan pembaruan status pembayaran/pengiriman.
  - `View.java`: pencetak tampilan teks untuk versi konsol.
  - `logo_slate.png.jpg`: logo yang ditampilkan di antarmuka.
- **`Controller`**
  - `Controller.java`: pengendali alur program versi konsol (login dan menu) yang bekerja bersama `View.java` dan `Util.Validasi`.

Setiap form di package `View` terdiri dari berkas `.java` dan `.form`. Berkas `.form` dipakai NetBeans GUI Builder, jadi keduanya harus selalu disimpan bersama.

## Penerapan 4 Pilar OOP

**1. Encapsulation**

Atribut pada class `Pengguna` (`idPengguna`, `namaPengguna`, `sandiPengguna`, `namaLengkap`, `role`) dideklarasikan `private` dan hanya dapat diakses lewat method getter. Prinsip yang sama dipakai pada `SesiPengguna`, yang menyimpan pengguna aktif dalam atribut `private static` dan hanya membukanya lewat `setPengguna()` dan `getPengguna()`, serta pada `Koneksi`, yang menyembunyikan URL, user, dan password database sebagai konstanta `private`.

**2. Inheritance**

`AdminGudang` dan `StafPenjualan` mewarisi `Pengguna` dan memanggil `super(...)` untuk mengisi data umum (id, nama pengguna, sandi, nama lengkap, dan role). Masing-masing menambahkan atribut khusus perannya, yaitu `shift` pada `AdminGudang` dan `wilayahPenjualan` pada `StafPenjualan`. Seluruh form pada package `View` juga mewarisi `javax.swing.JFrame`.

**3. Polimorphysm**

`Pengguna` mendeklarasikan method abstrak `getDeskripsiHakAkses()`, yang di-override secara berbeda oleh `AdminGudang` dan `StafPenjualan`. Method `PenggunaDAO.login()` bertipe kembalian `Pengguna`, tetapi objek yang dikembalikan dapat berupa `AdminGudang` atau `StafPenjualan` sesuai role di database. Objek tersebut disimpan di `SesiPengguna` sebagai tipe `Pengguna`, lalu diperiksa dengan `instanceof` pada form staf untuk mengambil data khusus peran.

**4. Abstraction**

`Pengguna` adalah abstract class yang mendefinisikan kerangka umum pengguna tanpa dapat diinstansiasi langsung, sehingga detail peran diserahkan kepada subclass. `PenggunaDAO` menyembunyikan query SQL login dari form, dan `Koneksi` menyembunyikan detail pembuatan koneksi JDBC, sehingga form cukup memanggil method tanpa mengetahui cara kerjanya.

## Cara Penggunaan Program

### Persiapan

1. Siapkan **Apache NetBeans**, dan **Laragon** (MySQL).
2. Jalankan MySQL dari Laragon, lalu buka phpMyAdmin.
3. Buat database bernama `slate_fix` (sesuai `Config/Koneksi.java`), lalu **Import** berkas `database/slate.sql`.
4. Clone repository ini, lalu buka sebagai project di NetBeans.
5. Jalankan program dengan menjalankan class `Main.Slate` (atau `view.FormLogin`).

### Akun Contoh

| Peran | Nama pengguna | Sandi Pengguna |
|---|---|---|
| Admin Gudang | `joko.susilo` | `admin_pass_01` |
| Staf Penjualan | `budi.santoso` | `staf_pass_01` |

Pilih **Hak Akses (Role)** yang sesuai dengan akun sebelum menekan tombol Login.

### Panduan Fitur

**1. Login**

<img width="1064" height="663" alt="image" src="https://github.com/user-attachments/assets/6d399632-dba8-43f1-bddd-df0353c9221a" />

Masukkan nama pengguna dan sandi, pilih hak akses, lalu tekan **LOGIN**.

**2. Menu Admin Gudang**

<img width="1365" height="847" alt="image" src="https://github.com/user-attachments/assets/0863644b-9697-4f4b-a6f6-180c40847c1f" />

Menampilkan daftar semen beserta status stok. Baris berstatus **Kritis** menandakan stok sudah di bawah atau sama dengan batas aman.

**3. Tambah Stok Semen**

<img width="1370" height="846" alt="image" src="https://github.com/user-attachments/assets/6dcf8906-6ce4-481b-a592-0e7681ebdd12" />

Pilih varian semen, isi nomor batch dan jumlah pasokan, lalu simpan. Stok di database bertambah otomatis dan riwayat pasokan tampil di tabel.

**4. Ubah Data Semen**

<img width="1364" height="849" alt="image" src="https://github.com/user-attachments/assets/8ff7a43e-568d-46e0-a229-41ac30087988" />

Pilih ID semen, ubah nama, satuan, harga jual, atau batas aman, lalu simpan.

**5. Tambah / Hapus Semen**

![Tambah dan Hapus Semen](docs/screenshots/05-kelola-semen.png)

Daftarkan varian semen baru, atau pilih ID semen yang ingin dihapus dan konfirmasi.

**6. Menu Staf Penjualan**

<img width="1370" height="848" alt="image" src="https://github.com/user-attachments/assets/308968e2-16aa-49d5-8061-99cbe8287a56" />

Menampilkan rekap pesanan dan menu menuju kelola pelanggan, pencatatan pesanan, dan pembaruan status.

**7. Kelola Data Pelanggan**

<img width="1367" height="848" alt="image" src="https://github.com/user-attachments/assets/ace96dbf-8f45-4562-9796-85fe2ad1451b" />

Tambah pelanggan baru, cari berdasarkan nama toko atau kontraktor, serta ubah atau hapus data yang dipilih dari tabel.

**8. Pencatatan Pesanan**

<img width="1364" height="843" alt="image" src="https://github.com/user-attachments/assets/97bd89bc-82c1-498a-9a33-c25e6e042c45" />

Pilih pelanggan, tambahkan satu atau beberapa jenis semen beserta jumlahnya, pilih metode dan status pembayaran, lalu catat pesanan. Sistem menolak item yang melebihi stok.

**9. Pembaruan Status Pembayaran/Pengiriman**

<img width="1368" height="844" alt="image" src="https://github.com/user-attachments/assets/52d28539-651c-411b-8cd4-b207f88f4fd3" />

Pilih pesanan dari tabel, ubah status pembayaran atau pengiriman, lalu simpan. Status **Refund** memulihkan stok pesanan secara otomatis.

## Kontributor

 Nama Anggota Kelompok:

<img src="https://img.shields.io/badge/MUKHAMMAD_ISMUL_AZAM_ATMOKO-2509116034-ff69b4?style=for-the-badge&labelColor=1f2937" alt="Ismul Azzam Atmoko" height="72"><br>
<img src="https://img.shields.io/badge/ZIAN_ALRAIS-2509116064-00bfff?style=for-the-badge&labelColor=1f2937" alt="Zian Al Rais" height="72"><br>
<img src="https://img.shields.io/badge/GHAIDA_SUCI_NAHIZA-2509116077-fde68a?style=for-the-badge&labelColor=1f2937" alt="Ghaida Suci Nahiza" height="72"><br>
<img src="https://img.shields.io/badge/MUHAMMAD_SYAWAL_SAMIR-2509116079-22c55e?style=for-the-badge&labelColor=1f2937" alt="Muhammad Syawal Samir" height="72">

