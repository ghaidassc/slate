package Model;

public class Pelanggan {
    private int idPelanggan;
    private String namaUsaha;
    private String jenisPelanggan;
    private String nomorTelepon;
    private String alamat;

    // Konstruktor data baru (tanpa ID)
    public Pelanggan(String namaUsaha, String jenisPelanggan, String nomorTelepon, String alamat) {
        this.namaUsaha = namaUsaha != null ? namaUsaha.trim() : "";
        this.jenisPelanggan = jenisPelanggan != null ? jenisPelanggan.trim() : "";
        this.nomorTelepon = nomorTelepon != null ? nomorTelepon.trim() : "";
        this.alamat = alamat != null ? alamat.trim() : "";
    }

    // Konstruktor lengkap (dengan ID)
    public Pelanggan(int idPelanggan, String namaUsaha, String jenisPelanggan, String nomorTelepon, String alamat) {
        this(namaUsaha, jenisPelanggan, nomorTelepon, alamat);
        this.idPelanggan = idPelanggan;
    }

    /**
     * Logika validasi bisnis
     */
    public void validasi() throws IllegalArgumentException {
        if (namaUsaha.isEmpty() || nomorTelepon.isEmpty() || alamat.isEmpty()) {
            throw new IllegalArgumentException("Semua data formulir wajib diisi!");
        }
        if (nomorTelepon.length() < 10) {
            throw new IllegalArgumentException("Nomor telepon minimal 10 digit!");
        }
        if (!nomorTelepon.matches("^[0-9+]+$")) {
            throw new IllegalArgumentException("Nomor telepon hanya boleh berisi angka!");
        }
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

    public String getNamaPelanggan() { return namaUsaha; }
    public String getNoHp() { return nomorTelepon; }
}