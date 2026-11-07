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
    public void setShift(String shift) { this.shift = shift; }
}