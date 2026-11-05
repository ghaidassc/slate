/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;
import Model.Pesanan;
import Model.PesananDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
    
    public List<Object[]> getAntreanPengiriman() throws SQLException {
        return pesananDAO.getAntreanPengiriman();
    }

    public Model.DetailNota getDetailNota(int idPesanan) throws SQLException {
        return pesananDAO.getDetailNota(idPesanan);
    }

    public boolean perbaruiStatusPesananDanBayar(int idPesanan, String statusKirimBaru, String statusBayarBaru) throws SQLException {
        return pesananDAO.perbaruiStatusPesananDanBayar(idPesanan, statusKirimBaru, statusBayarBaru);
    }
    
    public Object[] getStatistikDashboardStaf() throws SQLException {
        return pesananDAO.getStatistikDashboardStaf();
    }
    
    public List<Pesanan> getRekapSemuaPesanan() throws SQLException {
    return pesananDAO.getAllRekap();
    }
    
    public boolean prosesRefundPesanan(int idPesanan) throws SQLException {
        Connection conn = null;
        try {
            conn = Config.Koneksi.getKoneksi();
            conn.setAutoCommit(false);

            // 1. Ambil detail produk semen dan jumlah sak dari pesanan tersebut
            String sqlCariDetail = "SELECT semen_id_semen, jumlah_qty FROM detail_pesanan WHERE pesanan_id_pesanan = ?";
            try (PreparedStatement psDetail = conn.prepareStatement(sqlCariDetail)) {
                psDetail.setInt(1, idPesanan);
                try (ResultSet rs = psDetail.executeQuery()) {
                    String sqlRestok = "UPDATE semen SET stok_qty = stok_qty + ? WHERE id_semen = ?";
                    try (PreparedStatement psRestok = conn.prepareStatement(sqlRestok)) {
                        while (rs.next()) {
                            int idSemen = rs.getInt("semen_id_semen");
                            int qty = rs.getInt("jumlah_qty");

                            // Kembalikan stok semen ke gudang
                            psRestok.setInt(1, qty);
                            psRestok.setInt(2, idSemen);
                            psRestok.executeUpdate();
                        }
                    }
                }
            }

            // 2. Ubah status pesanan menjadi 'Refund'
            String sqlPesanan = "UPDATE pesanan SET status = 'Refund' WHERE id_pesanan = ?";
            try (PreparedStatement psP = conn.prepareStatement(sqlPesanan)) {
                psP.setInt(1, idPesanan);
                psP.executeUpdate();
            }

            // 3. Ubah status pembayaran menjadi 'Refund'
            String sqlBayar = "UPDATE pembayaran p JOIN pesanan ps ON p.pesanan_id_pesanan = ps.id_pesanan " +
                              "SET p.status = 'Refund' WHERE ps.id_pesanan = ?";
            try (PreparedStatement psB = conn.prepareStatement(sqlBayar)) {
                psB.setInt(1, idPesanan);
                psB.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.setAutoCommit(true);
        }
    }
}
