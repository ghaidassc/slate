package Model;

/**
 *
 * @author LOQ
 */
public class Semen {
    private int idSemen;
    private String namaSemen;
    private String satuan;
    private double hargaJual;
    private int stok;
    private int batasAman;

    public Semen() {}

    public Semen(int idSemen, String namaSemen, String satuan, double hargaJual, int stok, int batasAman) {
        this.idSemen = idSemen;
        this.namaSemen = namaSemen;
        this.satuan = satuan;
        this.hargaJual = hargaJual;
        this.stok = stok;
        this.batasAman = batasAman;
    }

    public int getIdSemen() { return idSemen; }
    public void setIdSemen(int idSemen) { this.idSemen = idSemen; }

    public String getNamaSemen() { return namaSemen; }
    public void setNamaSemen(String namaSemen) { this.namaSemen = namaSemen; }

    public String getSatuan() { return satuan; }
    public void setSatuan(String satuan) { this.satuan = satuan; }

    public double getHargaJual() { return hargaJual; }
    public void setHargaJual(double hargaJual) { this.hargaJual = hargaJual; }

    public int getStok() { return stok; }
    public void setStok(int stok) { this.stok = stok; }

    public int getBatasAman() { return batasAman; }
    public void setBatasAman(int batasAman) { this.batasAman = batasAman; }
}