# SLATE - Sistem Informasi Pengelolaan Stok, Pesanan, dan Pengiriman Semen

<div align="center">
  <img width="900" alt= src=<img width="1100" height="270" alt="image"<img width="1600" height="640" alt="image" src="https://github.com/user-attachments/assets/78ab3711-41b3-420a-bf97-99c493f7a34c" />

</div>

## Deskripsi Singkat

SLATE adalah aplikasi desktop berbasis Java untuk mengelola operasional sebuah perusahaan semen yang memiliki pabrik sendiri. Sistem ini dipakai secara internal oleh dua peran, yaitu **Admin Gudang** yang mengelola data semen dan stok hasil produksi, serta **Staf Penjualan** yang mengelola data pelanggan, mencatat pesanan, dan memperbarui status pembayaran serta pengiriman. Seluruh data tersimpan di database MySQL sehingga stok, pesanan, dan riwayat transaksi selalu terpusat dan terdokumentasi.

## Fitur Utama Program

**Umum**
- Login dengan pemilihan hak akses (Admin Gudang atau Staf Penjualan) dan logout dengan konfirmasi.

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

Flowchart lengkap tiap halaman (login, menu staf, kelola pelanggan, pencatatan pesanan, pembaruan status, menu admin, tambah stok, ubah data semen, tambah/hapus semen) tersedia di `docs/flowchart/FlowchartSLATE.drawio` dan dapat dibuka dengan [draw.io](https://app.diagrams.net). Ringkasan alur utamanya:


### Use Case


### ERD


## Struktur Folder/Package


## Penerapan 4 Pilar OOP

**1. Enkapsulasi.** Atribut pada class `Pengguna` (`idPengguna`, `namaPengguna`, `sandiPengguna`, `namaLengkap`, `role`) dideklarasikan `private` dan hanya dapat diakses lewat method getter. Prinsip yang sama dipakai pada `SesiPengguna`, yang menyimpan pengguna aktif dalam atribut `private static` dan hanya membukanya lewat `setPengguna()` dan `getPengguna()`, serta pada `Koneksi`, yang menyembunyikan URL, user, dan password database sebagai konstanta `private`.

**2. Pewarisan (Inheritance).** `AdminGudang` dan `StafPenjualan` mewarisi `Pengguna` dan memanggil `super(...)` untuk mengisi data umum (id, nama pengguna, sandi, nama lengkap, dan role). Masing-masing menambahkan atribut khusus perannya, yaitu `shift` pada `AdminGudang` dan `wilayahPenjualan` pada `StafPenjualan`. Seluruh form pada package `View` juga mewarisi `javax.swing.JFrame`.

**3. Polimorfisme.** `Pengguna` mendeklarasikan method abstrak `getDeskripsiHakAkses()`, yang di-override secara berbeda oleh `AdminGudang` dan `StafPenjualan`. Method `PenggunaDAO.login()` bertipe kembalian `Pengguna`, tetapi objek yang dikembalikan dapat berupa `AdminGudang` atau `StafPenjualan` sesuai role di database. Objek tersebut disimpan di `SesiPengguna` sebagai tipe `Pengguna`, lalu diperiksa dengan `instanceof` pada form staf untuk mengambil data khusus peran.

**4. Abstraksi.** `Pengguna` adalah abstract class yang mendefinisikan kerangka umum pengguna tanpa dapat diinstansiasi langsung, sehingga detail peran diserahkan kepada subclass. `PenggunaDAO` menyembunyikan query SQL login dari form, dan `Koneksi` menyembunyikan detail pembuatan koneksi JDBC, sehingga form cukup memanggil method tanpa mengetahui cara kerjanya.

## Cara Penggunaan Program

### Persiapan

1. Pasang **JDK**, **Apache NetBeans**, dan **XAMPP** (MySQL).
2. Jalankan MySQL dari Laragon, lalu buka phpMyAdmin.
3. Buat database bernama `slate_fix` (sesuai `Config/Koneksi.java`), lalu **Import** berkas `database/slate.sql`.
4. Clone repository ini, lalu buka sebagai project di NetBeans.
5. Klik kanan project, pilih **Reload POM** agar Maven mengunduh `mysql-connector-j`, lalu **Clean and Build**.
6. Jika pengaturan MySQL berbeda, sesuaikan `URL`, `USER`, dan `PASS` di `Config/Koneksi.java`.
7. Jalankan program dengan menjalankan class `Main.Slate` (atau `view.FormLogin`).

### Akun Contoh

| Peran | Nama pengguna | Sandi |
|---|---|---|
| Admin Gudang | `joko.susilo` | `admin_pass_01` |
| Staf Penjualan | `budi.santoso` | `staf_pass_01` |

Pilih **Hak Akses (Role)** yang sesuai dengan akun sebelum menekan tombol Login.

### Panduan Fitur

**1. Login**

![Halaman Login](docs/screenshots/01-login.png)

Masukkan nama pengguna dan sandi, pilih hak akses, lalu tekan **LOGIN**.

**2. Menu Admin Gudang**

![Menu Admin Gudang](docs/screenshots/02-menu-admin.png)

Menampilkan daftar semen beserta status stok. Baris berstatus **Kritis** menandakan stok sudah di bawah atau sama dengan batas aman.

**3. Tambah Stok Semen**

![Tambah Stok Semen](docs/screenshots/03-tambah-stok.png)

Pilih varian semen, isi nomor batch dan jumlah pasokan, lalu simpan. Stok di database bertambah otomatis dan riwayat pasokan tampil di tabel.

**4. Ubah Data Semen**

![Ubah Data Semen](docs/screenshots/04-ubah-data-semen.png)

Pilih ID semen, ubah nama, satuan, harga jual, atau batas aman, lalu simpan.

**5. Tambah / Hapus Semen**

![Tambah dan Hapus Semen](docs/screenshots/05-kelola-semen.png)

Daftarkan varian semen baru, atau pilih ID semen yang ingin dihapus dan konfirmasi.

**6. Menu Staf Penjualan**

![Menu Staf Penjualan](docs/screenshots/06-menu-staf.png)

Menampilkan rekap pesanan dan menu menuju kelola pelanggan, pencatatan pesanan, dan pembaruan status.

**7. Kelola Data Pelanggan**

![Kelola Data Pelanggan](docs/screenshots/07-kelola-pelanggan.png)

Tambah pelanggan baru, cari berdasarkan nama toko atau kontraktor, serta ubah atau hapus data yang dipilih dari tabel.

**8. Pencatatan Pesanan**

![Pencatatan Pesanan](docs/screenshots/08-pencatatan-pesanan.png)

Pilih pelanggan, tambahkan satu atau beberapa jenis semen beserta jumlahnya, pilih metode dan status pembayaran, lalu catat pesanan. Sistem menolak item yang melebihi stok.

**9. Pembaruan Status Pembayaran/Pengiriman**

![Pembaruan Status](docs/screenshots/09-status-pengiriman.png)

Pilih pesanan dari tabel, ubah status pembayaran atau pengiriman, lalu simpan. Status **Refund** memulihkan stok pesanan secara otomatis.

## Kontributor

 Nama Anggota Kelompok:

<img src="https://img.shields.io/badge/ISMULAZAM_ATMOKO-2509116034-ff69b4?style=for-the-badge&labelColor=1f2937" alt="Ismul Azzam Atmoko" height="72"><br>
<img src="https://img.shields.io/badge/ZIAN_ALRAIS-2509116064-00bfff?style=for-the-badge&labelColor=1f2937" alt="Zian Al Rais" height="72"><br>
<img src="https://img.shields.io/badge/GHAIDA_SUCI_NAHIZA-2509116077-fde68a?style=for-the-badge&labelColor=1f2937" alt="Ghaida Suci Nahiza" height="72"><br>
<img src="https://img.shields.io/badge/MUHAMMAD_SYAWAL_SAMIR-2509116079-22c55e?style=for-the-badge&labelColor=1f2937" alt="Muhammad Syawal Samir" height="72">

