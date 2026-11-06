/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package View;
import Model.Pelanggan;
import Model.Pesanan;
import Model.Semen;
import java.util.List;
/**
 *
 * @author LOQ
 */
public class View {

    public void cetakHeader(String judul) {
        System.out.println("\n====================================================================================");
        System.out.println("                         === " + judul.toUpperCase() + " ===");
        System.out.println("====================================================================================");
    }

    public void cetakPesan(String pesan) {
        System.out.println("[INFO] " + pesan);
    }

    public void tampilkanDaftarPelanggan(List<Pelanggan> list) {
        System.out.println("\n-------------------------------- DAFTAR PELANGGAN ----------------------------------");
        System.out.printf("%-4s %-30s %-15s %-40s %-15s%n", "ID", "Nama Usaha", "Jenis", "Alamat Kirim", "No. Telepon");
        System.out.println("------------------------------------------------------------------------------------");
        for (Pelanggan p : list) {
            System.out.printf("%-4d %-30s %-15s %-40s %-15s%n",
                    p.getIdPelanggan(), p.getNamaUsaha(), p.getJenisPelanggan(), p.getAlamat(), p.getNomorTelepon());
        }
        System.out.println("------------------------------------------------------------------------------------\n");
    }

    public void tampilkanDaftarSemen(List<Semen> list) {
        System.out.println("\n---------------------------------- DAFTAR SEMEN ------------------------------------");
        System.out.printf("%-4s %-20s %-8s %-14s %-8s %-10s%n", "ID", "Nama Semen", "Satuan", "Harga Jual", "Stok", "Batas Aman");
        System.out.println("------------------------------------------------------------------------------------");
        for (Semen s : list) {
            System.out.printf("%-4d %-20s %-8s Rp%-12.2f %-8d %-10d%n",
                    s.getIdSemen(), s.getNamaSemen(), s.getSatuan(), s.getHargaJual(), s.getStok(), s.getBatasAman());
        }
        System.out.println("------------------------------------------------------------------------------------\n");
    }

    public void tampilkanRekapPesanan(List<Pesanan> list) {
        System.out.println("\n----------------------------- DAFTAR REKAP PESANAN ---------------------------------");
        System.out.printf("%-4s %-11s %-30s %-15s %-10s %-12s%n", "ID", "Tanggal", "Pelanggan", "Total Tagihan", "Kirim", "Bayar");
        System.out.println("------------------------------------------------------------------------------------");
        for (Pesanan p : list) {
            System.out.printf("%-4d %-11s %-30s Rp%-13.2f %-10s %-12s%n",
                    p.getIdPesanan(), p.getTglPesanan(), p.getNamaPelanggan(), p.getTotalTagihan(), p.getStatus(), p.getStatusBayar());
        }
        System.out.println("------------------------------------------------------------------------------------\n");
    }

    public void tampilkanPeringatanStok(List<Semen> list) {
        if (!list.isEmpty()) {
            System.out.println("\n[!] === PERINGATAN STOK MENIPIS ===");
            for (Semen s : list) {
                System.out.printf("    - Stok %s sisa %d (Batas aman: %d)%n", s.getNamaSemen(), s.getStok(), s.getBatasAman());
            }
            System.out.println("----------------------------------\n");
        }
    }
}