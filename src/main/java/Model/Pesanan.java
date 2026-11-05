/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author LOQ
 */
public class Pesanan {
    private int idPesanan;
    private String tglPesanan;
    private double totalTagihan;
    private String status;
    private int stafId;
    private int pembayaranId;
    private int pelangganId;
    private String namaPelanggan;
    private String statusBayar;

    public Pesanan(int idPesanan, String tglPesanan, double totalTagihan, String status, int stafId, int pembayaranId, int pelangganId) {
        this.idPesanan = idPesanan;
        this.tglPesanan = tglPesanan;
        this.totalTagihan = totalTagihan;
        this.status = status;
        this.stafId = stafId;
        this.pembayaranId = pembayaranId;
        this.pelangganId = pelangganId;
    }

    public int getIdPesanan() { return idPesanan; }
    public String getTglPesanan() { return tglPesanan; }
    public double getTotalTagihan() { return totalTagihan; }
    public String getStatus() { return status; }
    public int getStafId() { return stafId; }
    public int getPembayaranId() { return pembayaranId; }
    public int getPelangganId() { return pelangganId; }

    public String getNamaPelanggan() { return namaPelanggan; }
    public void setNamaPelanggan(String namaPelanggan) { this.namaPelanggan = namaPelanggan; }

    public String getStatusBayar() { return statusBayar; }
    public void setStatusBayar(String statusBayar) { this.statusBayar = statusBayar; }
}
