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
    private String alamatKirim;
    private String noTelepon;

    public Pelanggan(int idPelanggan, String namaUsaha, String jenisPelanggan, String alamatKirim, String noTelepon) {
        this.idPelanggan = idPelanggan;
        this.namaUsaha = namaUsaha;
        this.jenisPelanggan = jenisPelanggan;
        this.alamatKirim = alamatKirim;
        this.noTelepon = noTelepon;
    }

    public int getIdPelanggan() { return idPelanggan; }
    public String getNamaUsaha() { return namaUsaha; }
    public String getJenisPelanggan() { return jenisPelanggan; }
    public String getAlamatKirim() { return alamatKirim; }
    public String getNoTelepon() { return noTelepon; }
}
