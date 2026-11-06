/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;
import Model.Pengguna;
import dao.PenggunaDAO;
import Model.Semen;
import Service.PelangganService;
import Service.PesananService;
import Service.SemenService;
import Util.Validasi;
import View.View;

import java.sql.SQLException;
/**
 *
 * @author LOQ
 */
public class Controller {
    private final View view = new View();
    private final PenggunaDAO penggunaDAO = new PenggunaDAO();
    private final PelangganService pelangganService = new PelangganService();
    private final SemenService semenService = new SemenService();
    private final PesananService pesananService = new PesananService();

    private Pengguna penggunaAktif;

    public void run() {
        while (true) {
            view.cetakHeader("Halaman Login");
            System.out.println("1. Login");
            System.out.println("2. Keluar");
            int pilih = Validasi.readInt("Pilih: ");

            if (pilih == 1) {
                if (prosesLogin()) {
                    if ("staf_penjualan".equalsIgnoreCase(penggunaAktif.getRole())) {
                        menuStaf();
                    } else if ("admin_gudang".equalsIgnoreCase(penggunaAktif.getRole())) {
                        menuAdmin();
                    }
                }
            } else if (pilih == 2) {
                view.cetakPesan("Terima kasih telah menggunakan sistem.");
                break;
            } else {
                view.cetakPesan("Pilihan tidak ada.");
            }
        }
    }

    private boolean prosesLogin() {
        String user = Validasi.readString("Nama Pengguna: ");
        String pass = Validasi.readString("Sandi Pengguna: ");
        try {
            penggunaAktif = penggunaDAO.login(user, pass);
            if (penggunaAktif != null) {
                view.cetakPesan("Login Berhasil! Selamat datang, " + penggunaAktif.getNamaLengkap());
                return true;
            } else {
                view.cetakPesan("Nama Pengguna/Sandi Pengguna Salah");
            }
        } catch (SQLException e) {
            view.cetakPesan("Error DB: " + e.getMessage());
        }
        return false;
    }

    // --- MENU STAF PENJUALAN ---
    private void menuStaf() {
        while (true) {
            view.cetakHeader("Menu Staf Penjualan");
            try {
                view.tampilkanRekapPesanan(pesananService.getRekapPesanan());
            } catch (SQLException e) {
                view.cetakPesan("Error: " + e.getMessage());
            }

            System.out.println("1. Kelola Data Pelanggan");
            System.out.println("2. Pencatatan Pesanan");
            System.out.println("3. Pembaruan Status Pengiriman / Pembayaran");
            System.out.println("4. Keluar");
            int pilih = Validasi.readInt("Pilih menu: ");

            if (pilih == 1) {
                kelolaPelanggan();
            } else if (pilih == 2) {
                pencatatanPesanan();
            } else if (pilih == 3) {
                pembaruanStatus();
            } else if (pilih == 4) {
                if (Validasi.readKonfirmasi("Yakin keluar ke menu utama?")) {
                    return;
                }
            } else {
                view.cetakPesan("Pilihan tidak ada.");
            }
        }
    }

    private void kelolaPelanggan() {
        while (true) {
            view.cetakHeader("Kelola Data Pelanggan");
            try {
                view.tampilkanDaftarPelanggan(pelangganService.getAllPelanggan());
            } catch (SQLException e) {
                view.cetakPesan("Error: " + e.getMessage());
            }

            System.out.println("1. Tambah Pelanggan");
            System.out.println("2. Cari Toko / Kontraktor");
            System.out.println("3. Ubah Data Pelanggan");
            System.out.println("4. Hapus Data Pelanggan");
            System.out.println("5. Kembali");
            int pilih = Validasi.readInt("Pilih menu: ");

            try {
                if (pilih == 1) {
                    String nama = Validasi.readString("Nama Usaha: ");
                    String jenis = Validasi.readInt("Jenis (1. Toko Bangunan / 2. Kontraktor): ") == 1 ? "Toko Bangunan" : "Kontraktor";
                    String alamat = Validasi.readString("Alamat Kirim: ");
                    
                    // Ganti di sini menggunakan readTelepon
                    String telp = Validasi.readTelepon("Nomor Telepon: ");
                    
                    if (pelangganService.tambahPelanggan(nama, jenis, alamat, telp)) {
                        view.cetakPesan("Tambah Pelanggan Berhasil");
                    }
                } else if (pilih == 2) {
                    String q = Validasi.readString("Nama yang Dicari: ");
                    view.tampilkanDaftarPelanggan(pelangganService.cariPelanggan(q));
                } else if (pilih == 3) {
                    int id = Validasi.readInt("ID Pelanggan diubah: ");
                    String nama = Validasi.readString("Nama Usaha Baru: ");
                    String jenis = Validasi.readInt("Jenis (1. Toko / 2. Kontraktor): ") == 1 ? "Toko Bangunan" : "Kontraktor";
                    String alamat = Validasi.readString("Alamat Baru: ");
                    
                    // Ganti di sini juga menggunakan readTelepon
                    String telp = Validasi.readTelepon("Telp Baru: ");
                    
                    if (pelangganService.ubahPelanggan(id, nama, jenis, alamat, telp)) {
                        view.cetakPesan("Ubah Data Pelanggan Berhasil");
                    }
                } else if (pilih == 4) {
                    int id = Validasi.readInt("ID Pelanggan dihapus: ");
                    if (Validasi.readKonfirmasi("Yakin hapus?") && pelangganService.hapusPelanggan(id)) {
                        view.cetakPesan("Hapus Data Pelanggan Berhasil");
                    }
                } else if (pilih == 5) {
                    return;
                } else {
                    view.cetakPesan("Pilihan tidak ada.");
                }
            } catch (SQLException e) {
                view.cetakPesan("Error DB: " + e.getMessage());
            }
        }
    }

    private void pencatatanPesanan() {
        view.cetakHeader("Pencatatan Pesanan");
        try {
            view.tampilkanDaftarPelanggan(pelangganService.getAllPelanggan());
            int idPlg = Validasi.readInt("Pilih ID Pelanggan: ");

            view.tampilkanDaftarSemen(semenService.getAllSemen());
            int idSemen = Validasi.readInt("Pilih ID Semen: ");
            int qty = Validasi.readInt("Jumlah Semen: ");

            Semen s = semenService.getSemenById(idSemen);
            if (s == null || s.getStok() < qty) {
                view.cetakPesan("Stok Tidak Mencukupi / Semen Tidak Ada");
                return;
            }

            double total = s.getHargaJual() * qty;
            view.cetakPesan("Total Tagihan: Rp " + total);
            boolean lunas = Validasi.readKonfirmasi("Apakah Pembayaran Lunas Sekarang?");

            if (pesananService.buatPesananBaru(idPlg, idSemen, qty, total, lunas, penggunaAktif.getIdPengguna())) {
                view.cetakPesan("Pencatatan Pesanan Berhasil! Menyiapkan Pengiriman...");
            }
        } catch (SQLException e) {
            view.cetakPesan("Error DB: " + e.getMessage());
        }
    }

    private void pembaruanStatus() {
        view.cetakHeader("Pembaruan Status");
        try {
            view.tampilkanRekapPesanan(pesananService.getRekapPesanan());
            int idP = Validasi.readInt("Pilih ID Pesanan: ");
            System.out.println("1. Ubah Status Pembayaran\n2. Ubah Status Pengiriman");
            int j = Validasi.readInt("Pilih Jenis: ");

            if (j == 1) {
                boolean lunas = Validasi.readKonfirmasi("Status Pembayaran Lunas?");
                if (pesananService.ubahStatusBayar(idP, lunas ? "Lunas" : "Belum Lunas")) {
                    view.cetakPesan("Pembaruan Status Pembayaran Berhasil");
                }
            } else if (j == 2) {
                String status = Validasi.readString("Masukkan Status Pengiriman (Diproses/Dikirim/Diterima/Refund): ");
                if (pesananService.ubahStatusKirim(idP, status)) {
                    view.cetakPesan("Pembaruan Status Pengiriman Berhasil");
                }
            } else {
                view.cetakPesan("Pilihan jenis status tidak ada.");
            }
        } catch (SQLException e) {
            view.cetakPesan("Error DB: " + e.getMessage());
        }
    }

    // --- MENU ADMIN GUDANG ---
    private void menuAdmin() {
        while (true) {
            view.cetakHeader("Menu Admin Gudang");
            try {
                view.tampilkanPeringatanStok(semenService.getPeringatanStok());
                view.tampilkanDaftarSemen(semenService.getAllSemen());
            } catch (SQLException e) {
                view.cetakPesan("Error: " + e.getMessage());
            }

            System.out.println("1. Tambah Stok Semen");
            System.out.println("2. Ubah Data Semen");
            System.out.println("3. Tambah/Hapus Semen");
            System.out.println("4. Keluar");
            int pilih = Validasi.readInt("Pilih menu: ");

            try {
                if (pilih == 1) {
                    int id = Validasi.readInt("ID Semen: ");
                    int tmb = Validasi.readInt("Tambahan Stok: ");
                    if (semenService.tambahStok(id, tmb)) {
                        view.cetakPesan("Stok Semen Berhasil Ditambahkan");
                    }
                } else if (pilih == 2) {
                    int id = Validasi.readInt("ID Semen Diubah: ");
                    String n = Validasi.readString("Nama Baru: ");
                    String s = Validasi.readString("Satuan Baru: ");
                    double h = Validasi.readDouble("Harga Baru: ");
                    int b = Validasi.readInt("Batas Aman Baru: ");
                    if (semenService.ubahDataSemen(id, n, s, h, b)) {
                        view.cetakPesan("Data Semen Berhasil Diubah");
                    }
                } else if (pilih == 3) {
                    int op = Validasi.readInt("1. Tambah Semen Baru\n2. Hapus Semen\nPilih: ");
                    if (op == 1) {
                        String n = Validasi.readString("Nama Semen: ");
                        String s = Validasi.readString("Satuan: ");
                        double h = Validasi.readDouble("Harga Jual: ");
                        int st = Validasi.readInt("Stok Awal: ");
                        int b = Validasi.readInt("Batas Aman: ");
                        if (semenService.tambahSemenBaru(n, s, h, st, b)) {
                            view.cetakPesan("Semen Berhasil Ditambah");
                        }
                    } else if (op == 2) {
                        int id = Validasi.readInt("ID Semen Dihapus: ");
                        if (Validasi.readKonfirmasi("Yakin hapus?") && semenService.hapusSemen(id)) {
                            view.cetakPesan("Semen Berhasil Dihapus");
                        }
                    } else {
                        view.cetakPesan("Pilihan tidak ada.");
                    }
                } else if (pilih == 4) {
                    if (Validasi.readKonfirmasi("Yakin keluar ke menu utama?")) {
                        return;
                    }
                } else {
                    view.cetakPesan("Pilihan tidak ada.");
                }
            } catch (SQLException e) {
                view.cetakPesan("Error DB: " + e.getMessage());
            }
        }
    }
}
