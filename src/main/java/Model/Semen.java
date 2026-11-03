/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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

    public Semen(int idSemen, String namaSemen, String satuan, double hargaJual, int stok, int batasAman) {
        this.idSemen = idSemen;
        this.namaSemen = namaSemen;
        this.satuan = satuan;
        this.hargaJual = hargaJual;
        this.stok = stok;
        this.batasAman = batasAman;
    }

    public int getIdSemen() { return idSemen; }
    public String getNamaSemen() { return namaSemen; }
    public String getSatuan() { return satuan; }
    public double getHargaJual() { return hargaJual; }
    public int getStok() { return stok; }
    public int getBatasAman() { return batasAman; }
}
