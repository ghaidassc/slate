/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gedascc
 */
public class DetailNota {
    private int idPesanan;
    private Date tglPesanan;
    private double totalTagihan;
    private String statusKirim;
    private String namaUsaha;
    private String alamatKirim;
    private String metodeBayar;
    private String statusBayar;
    private List<String> daftarMuatan = new ArrayList<>();

    public DetailNota() {}

    public int getIdPesanan() { return idPesanan; }
    public void setIdPesanan(int idPesanan) { this.idPesanan = idPesanan; }

    public Date getTglPesanan() { return tglPesanan; }
    public void setTglPesanan(Date tglPesanan) { this.tglPesanan = tglPesanan; }

    public double getTotalTagihan() { return totalTagihan; }
    public void setTotalTagihan(double totalTagihan) { this.totalTagihan = totalTagihan; }

    public String getStatusKirim() { return statusKirim; }
    public void setStatusKirim(String statusKirim) { this.statusKirim = statusKirim; }

    public String getNamaUsaha() { return namaUsaha; }
    public void setNamaUsaha(String namaUsaha) { this.namaUsaha = namaUsaha; }

    public String getAlamatKirim() { return alamatKirim; }
    public void setAlamatKirim(String alamatKirim) { this.alamatKirim = alamatKirim; }

    public String getMetodeBayar() { return metodeBayar; }
    public void setMetodeBayar(String metodeBayar) { this.metodeBayar = metodeBayar; }

    public String getStatusBayar() { return statusBayar; }
    public void setStatusBayar(String statusBayar) { this.statusBayar = statusBayar; }

    public List<String> getDaftarMuatan() { return daftarMuatan; }
    public void setDaftarMuatan(List<String> daftarMuatan) { this.daftarMuatan = daftarMuatan; }
}
