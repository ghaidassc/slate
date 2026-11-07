package Service;

import Model.DetailNota;
import Model.Pesanan;
import dao.PesananDAO;
import dao.PesananDAOInterface;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author LOQ
 */
public class PesananService {

    // Abstraction: mengacu pada PesananDAOInterface
    private final PesananDAOInterface pesananDAO;

    public PesananService() {
        this.pesananDAO = new PesananDAO();
    }

    public List<Pesanan> getRekapPesanan() throws SQLException {
        return pesananDAO.getAllRekap();
    }

    public List<Pesanan> getRekapSemuaPesanan() throws SQLException {
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

    public DetailNota getDetailNota(int idPesanan) throws SQLException {
        return pesananDAO.getDetailNota(idPesanan);
    }

    public boolean perbaruiStatusPesananDanBayar(int idPesanan, String statusKirimBaru, String statusBayarBaru) throws SQLException {
        return pesananDAO.perbaruiStatusPesananDanBayar(idPesanan, statusKirimBaru, statusBayarBaru);
    }
    
    public Object[] getStatistikDashboardStaf() throws SQLException {
        return pesananDAO.getStatistikDashboardStaf();
    }

    public boolean prosesRefundPesanan(int idPesanan) throws SQLException {
        return pesananDAO.refundPesanan(idPesanan);
    }

    public int simpanPesananLengkap(int idPelanggan, int idStaf, String metodeBayar, String statusBayar, 
                                     double totalTagihan, double nominalBayar, List<Object[]> itemKeranjang) throws SQLException {
        return pesananDAO.simpanPesananLengkap(idPelanggan, idStaf, metodeBayar, statusBayar, totalTagihan, nominalBayar, itemKeranjang);
    }
}