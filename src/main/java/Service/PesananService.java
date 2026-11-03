/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;
import Model.Pesanan;
import Model.PesananDAO;
import java.sql.SQLException;
import java.util.List;
/**
 *
 * @author LOQ
 */
public class PesananService {
    private final PesananDAO pesananDAO = new PesananDAO();

    public List<Pesanan> getRekapPesanan() throws SQLException {
        return pesananDAO.getAllRekap();
    }

    public boolean buatPesananBaru(int idPelanggan, int idSemen, int qty, double total, boolean lunas, int idStaf) throws SQLException {
        return pesananDAO.buatPesanan(idPelanggan, idSemen, qty, total, lunas, idStaf);
    }

    public boolean ubahStatusBayar(int idPesanan, String status) throws SQLException {
        return pesananDAO.updateStatusPembayaran(idPesanan, status);
    }

    public boolean ubahStatusKirim(int idPesanan, String status) throws SQLException {
        return pesananDAO.updateStatusPengiriman(idPesanan, status);
    }
}
