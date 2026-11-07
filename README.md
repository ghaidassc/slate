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

### 1. Encapsulation

**Data hiding.** Class `Pelanggan` menggunakan access modifier `private` pada atributnya agar tidak dapat diakses secara langsung dari luar class. Data pelanggan dapat diakses dan diubah melalui method getter dan setter.

```java
private int idPelanggan;
private String namaUsaha;
private String jenisPelanggan;
private String nomorTelepon;
private String alamat;

public String getNamaUsaha() {
    return namaUsaha;
}

public void setNamaUsaha(String namaUsaha) {
    this.namaUsaha = namaUsaha;
}
```

### 2. Inheritance

**Pewarisan class.** `AdminGudang` mewarisi atribut dan method dari superclass `Pengguna` menggunakan keyword `extends`. Konstruktor `super()` digunakan untuk menginisialisasi data umum pengguna, sedangkan atribut `shift` menjadi atribut khusus Admin Gudang.

```java
public class AdminGudang extends Pengguna {
    private String shift;

    public AdminGudang(int idPengguna, String namaPengguna,
            String sandiPengguna, String namaLengkap, String shift) {
        super(idPengguna, namaPengguna, sandiPengguna,
                namaLengkap, "admin_gudang");
        this.shift = shift;
    }
}
```

### 3. Polymorphism

**Method overriding.** `AdminGudang` dan `StafPenjualan` meng-override method `getDeskripsiHakAkses()` dari superclass `Pengguna`. Method tersebut menghasilkan deskripsi hak akses yang berbeda sesuai dengan peran masing-masing pengguna.

```java
@Override
public String getDeskripsiHakAkses() {
    return "Hak Akses: Penjualan, Pelanggan, dan Status Pengiriman "
            + "(Wilayah: " + wilayahPenjualan + ")";
}
```

### 4. Abstraction

**Abstract class dan abstract method.** Class `Pengguna` dideklarasikan sebagai `abstract` sehingga tidak dapat dibuat menjadi objek secara langsung. Class ini mendefinisikan method abstrak `getDeskripsiHakAkses()` yang harus diimplementasikan oleh class turunannya.

```java
public abstract class Pengguna {
    private int idPengguna;
    private String namaPengguna;
    private String sandiPengguna;
    private String namaLengkap;
    private String role;

    public abstract String getDeskripsiHakAkses();
}
```

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

Pada halaman login, pengguna memasukkan nama pengguna dan sandi yang telah terdaftar di dalam sistem. Pengguna juga perlu memilih hak akses sesuai dengan perannya, yaitu Admin Gudang atau Staf Penjualan. Setelah menekan tombol LOGIN, sistem akan memvalidasi informasi tersebut dan mengarahkan pengguna ke halaman utama sesuai hak aksesnya.

**2. Menu Admin Gudang**

<img width="1365" height="847" alt="image" src="https://github.com/user-attachments/assets/0863644b-9697-4f4b-a6f6-180c40847c1f" />

Halaman Admin Gudang menampilkan daftar semen yang tersedia beserta informasi dan jumlah stoknya. Admin dapat memantau kondisi persediaan melalui status stok yang ditampilkan pada tabel. Baris berstatus Kritis menandakan jumlah stok semen sudah berada di bawah atau sama dengan batas aman.

**3. Tambah Stok Semen**

<img width="1370" height="846" alt="image" src="https://github.com/user-attachments/assets/6dcf8906-6ce4-481b-a592-0e7681ebdd12" />

Admin Gudang dapat menambahkan pasokan semen yang baru masuk melalui halaman ini. Proses dilakukan dengan memilih varian semen, memasukkan nomor batch, serta mengisi jumlah pasokan yang diterima. Setelah disimpan, sistem memperbarui jumlah stok di database dan menampilkan riwayat pasokan pada tabel.

**4. Ubah Data Semen**

<img width="1364" height="849" alt="image" src="https://github.com/user-attachments/assets/8ff7a43e-568d-46e0-a229-41ac30087988" />

Halaman ini digunakan Admin Gudang untuk memperbarui informasi semen yang sudah terdaftar. Admin memilih ID semen yang ingin diubah, kemudian menyesuaikan informasi seperti nama, satuan, harga jual, atau batas aman stok. Setelah perubahan disimpan, sistem memperbarui data semen sehingga informasi yang ditampilkan sesuai dengan data terbaru.

**5. Tambah / Hapus Semen**

![Tambah dan Hapus Semen](docs/screenshots/05-kelola-semen.png)

Fitur ini digunakan untuk menambahkan varian semen baru atau menghapus data semen yang sudah tidak diperlukan. Untuk menambahkan semen, Admin Gudang mengisi informasi produk yang dibutuhkan sebelum menyimpannya ke sistem. Sementara itu, penghapusan dilakukan dengan memilih ID semen yang sesuai dan mengonfirmasi tindakan tersebut.

**6. Menu Staf Penjualan**

<img width="1370" height="848" alt="image" src="https://github.com/user-attachments/assets/308968e2-16aa-49d5-8061-99cbe8287a56" />

Halaman Staf Penjualan menampilkan rekap pesanan yang tercatat dalam sistem. Staf dapat memantau informasi pesanan dan memilih menu sesuai kebutuhan operasional. Menu yang tersedia meliputi pengelolaan data pelanggan, pencatatan pesanan, serta pembaruan status pembayaran dan pengiriman.

**7. Kelola Data Pelanggan**

<img width="1367" height="848" alt="image" src="https://github.com/user-attachments/assets/ace96dbf-8f45-4562-9796-85fe2ad1451b" />

Halaman ini digunakan untuk mengelola informasi pelanggan yang tersimpan dalam database. Staf Penjualan dapat menambahkan pelanggan baru atau mencari pelanggan berdasarkan nama toko maupun kontraktor. Selain itu, staf dapat mengubah dan menghapus data pelanggan yang dipilih melalui tabel sesuai kebutuhan.

**8. Pencatatan Pesanan**

<img width="1364" height="843" alt="image" src="https://github.com/user-attachments/assets/97bd89bc-82c1-498a-9a33-c25e6e042c45" />

Staf Penjualan memulai pencatatan pesanan dengan memilih pelanggan yang akan melakukan pembelian. Selanjutnya, staf dapat menambahkan satu atau beberapa jenis semen beserta jumlah yang dipesan, kemudian menentukan metode dan status pembayaran. Sistem memeriksa ketersediaan stok dan menolak pesanan apabila jumlah semen yang diminta melebihi persediaan.

**9. Pembaruan Status Pembayaran/Pengiriman**

<img width="1368" height="844" alt="image" src="https://github.com/user-attachments/assets/52d28539-651c-411b-8cd4-b207f88f4fd3" />

Fitur ini digunakan oleh Staf Penjualan untuk memperbarui informasi pembayaran maupun status pengiriman pesanan. Staf memilih pesanan yang ingin diperbarui dari tabel, kemudian mengubah status sesuai kondisi transaksi. Setelah perubahan disimpan, sistem memperbarui status pesanan dan memulihkan stok semen secara otomatis apabila pesanan berstatus Refund.

## Kontributor

 Nama Anggota Kelompok:

<img src="https://img.shields.io/badge/MUKHAMMAD_ISMUL_AZAM_ATMOKO-2509116034-ff69b4?style=for-the-badge&labelColor=1f2937" alt="Ismul Azzam Atmoko" height="72"><br>
<img src="https://img.shields.io/badge/ZIAN_ALRAIS-2509116064-00bfff?style=for-the-badge&labelColor=1f2937" alt="Zian Alrais" height="72"><br>
<img src="https://img.shields.io/badge/GHAIDA_SUCI_NAHIZA-2509116077-fde68a?style=for-the-badge&labelColor=1f2937" alt="Ghaida Suci Nahiza" height="72"><br>
<img src="https://img.shields.io/badge/MUHAMMAD_SYAWAL_SAMIR-2509116079-22c55e?style=for-the-badge&labelColor=1f2937" alt="Muhammad Syawal Samir" height="72">

