package dao;

import Model.DetailNota;
import Model.Pesanan;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author gedascc
 */
public interface PesananDAOInterface {
    List<Pesanan> getAllRekap() throws SQLException;
    boolean buatPesanan(int idPelanggan, int idSemen, int qty, double totalTagihan, boolean isLunas, int idStaf) throws SQLException;
    int simpanPesananLengkap(int idPelanggan, int idStaf, String metodeBayar, String statusBayar, 
                             double totalTagihan, double nominalBayar, List<Object[]> itemKeranjang) throws SQLException;
    boolean refundPesanan(int idPesanan) throws SQLException;
    boolean updateStatusPembayaran(int idPesanan, String statusBayarBaru) throws SQLException;
    boolean updateStatusPengiriman(int idPesanan, String statusKirimBaru) throws SQLException;
    List<Object[]> getAntreanPengiriman() throws SQLException;
    DetailNota getDetailNota(int idPesanan) throws SQLException;
    boolean perbaruiStatusPesananDanBayar(int idPesanan, String statusKirimBaru, String statusBayarBaru) throws SQLException;
    Object[] getStatistikDashboardStaf() throws SQLException;
}