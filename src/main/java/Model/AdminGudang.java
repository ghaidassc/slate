/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author LOQ
 */
public class AdminGudang extends Pengguna {
    private String shift;

    public AdminGudang(int idPengguna, String namaPengguna, String sandiPengguna, String namaLengkap, String shift) {
        super(idPengguna, namaPengguna, sandiPengguna, namaLengkap, "admin_gudang");
        this.shift = shift;
    }

    @Override
    public String getDeskripsiHakAkses() {
        return "Hak Akses: Pengelolaan Stok Semen Gudang (Shift: " + shift + ")";
    }

    public String getShift() { return shift; }
}
