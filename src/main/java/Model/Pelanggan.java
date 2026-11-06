/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author LOQ
 */
public class Pelanggan {
    private int idPelanggan;
    private String namaUsaha;
    private String jenisPelanggan;
    private String nomorTelepon;
    private String alamat;

    // Konstruktor untuk data baru (tanpa ID)
    public Pelanggan(String namaUsaha, String jenisPelanggan, String nomorTelepon, String alamat) {
        this.namaUsaha = namaUsaha;
        this.jenisPelanggan = jenisPelanggan;
        this.nomorTelepon = nomorTelepon;
        this.alamat = alamat;
    }

    // Konstruktor lengkap (dengan ID)
    public Pelanggan(int idPelanggan, String namaUsaha, String jenisPelanggan, String nomorTelepon, String alamat) {
        this.idPelanggan = idPelanggan;
        this.namaUsaha = namaUsaha;
        this.jenisPelanggan = jenisPelanggan;
        this.nomorTelepon = nomorTelepon;
        this.alamat = alamat;
    }

    // Getter dan Setter
    public int getIdPelanggan() {
        return idPelanggan;
    }

    public void setIdPelanggan(int idPelanggan) {
        this.idPelanggan = idPelanggan;
    }

    public String getNamaUsaha() {
        return namaUsaha;
    }

    public void setNamaUsaha(String namaUsaha) {
        this.namaUsaha = namaUsaha;
    }

    public String getJenisPelanggan() {
        return jenisPelanggan;
    }

    public void setJenisPelanggan(String jenisPelanggan) {
        this.jenisPelanggan = jenisPelanggan;
    }

    public String getNomorTelepon() {
        return nomorTelepon;
    }

    public void setNomorTelepon(String nomorTelepon) {
        this.nomorTelepon = nomorTelepon;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }
}
