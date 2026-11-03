/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author LOQ
 */
public class StafPenjualan extends Pengguna {
    private String wilayahPenjualan;

    public StafPenjualan(int idPengguna, String namaPengguna, String sandiPengguna, String namaLengkap, String wilayahPenjualan) {
        super(idPengguna, namaPengguna, sandiPengguna, namaLengkap, "staf_penjualan");
        this.wilayahPenjualan = wilayahPenjualan;
    }

    @Override
    public String getDeskripsiHakAkses() {
        return "Hak Akses: Penjualan, Pelanggan, dan Status Pengiriman (Wilayah: " + wilayahPenjualan + ")";
    }

    public String getWilayahPenjualan() { return wilayahPenjualan; }
}
