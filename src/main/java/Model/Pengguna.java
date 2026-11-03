/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author LOQ
 */
public abstract class Pengguna {
    private int idPengguna;
    private String namaPengguna;
    private String sandiPengguna;
    private String namaLengkap;
    private String role;

    public Pengguna(int idPengguna, String namaPengguna, String sandiPengguna, String namaLengkap, String role) {
        this.idPengguna = idPengguna;
        this.namaPengguna = namaPengguna;
        this.sandiPengguna = sandiPengguna;
        this.namaLengkap = namaLengkap;
        this.role = role;
    }

    // Abstract method untuk mendukung Polimorfisme
    public abstract String getDeskripsiHakAkses();

    public int getIdPengguna() { return idPengguna; }
    public String getNamaPengguna() { return namaPengguna; }
    public String getSandiPengguna() { return sandiPengguna; }
    public String getNamaLengkap() { return namaLengkap; }
    public String getRole() { return role; }
}
