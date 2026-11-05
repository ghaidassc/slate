/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author LOQ
 */
public class Pembayaran {
    private int idPembayaran;
    private String tglBayar;
    private String metodeBayar;
    private double jumlahBayar;
    private String status;
    private String tglRefund;
    private int pesananId;

    public Pembayaran(int idPembayaran, String tglBayar, String metodeBayar, double jumlahBayar, String status, String tglRefund, int pesananId) {
        this.idPembayaran = idPembayaran;
        this.tglBayar = tglBayar;
        this.metodeBayar = metodeBayar;
        this.jumlahBayar = jumlahBayar;
        this.status = status;
        this.tglRefund = tglRefund;
        this.pesananId = pesananId;
    }

    public int getIdPembayaran() { return idPembayaran; }
    public String getTglBayar() { return tglBayar; }
    public String getMetodeBayar() { return metodeBayar; }
    public double getJumlahBayar() { return jumlahBayar; }
    public String getStatus() { return status; }
    public String getTglRefund() { return tglRefund; }
    public int getPesananId() { return pesananId; }
}
