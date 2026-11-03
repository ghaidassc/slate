/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;
import Config.Koneksi;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author LOQ
 */
public class PesananDAO {

    public List<Pesanan> getAllRekap() throws SQLException {
        List<Pesanan> list = new ArrayList<>();
        Connection conn = Koneksi.getKoneksi();
        String sql = "SELECT p.id_pesanan, p.tgl_pesanan, pl.nama_usaha, p.total_tagihan_rp, "
                   + "p.status AS status_kirim, pb.status AS status_bayar "
                   + "FROM pesanan p "
                   + "JOIN pelanggan pl ON p.pelanggan_id_pelanggan = pl.id_pelanggan "
                   + "JOIN pembayaran pb ON pb.id_pembayaran = p.pembayaran_id_pembayaran "
                   + "ORDER BY p.id_pesanan ASC";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            Pesanan p = new Pesanan(
                rs.getInt("id_pesanan"),
                rs.getString("tgl_pesanan"),
                rs.getDouble("total_tagihan_rp"),
                rs.getString("status_kirim"),
                0, 0, 0
            );
            p.setNamaPelanggan(rs.getString("nama_usaha"));
            p.setStatusBayar(rs.getString("status_bayar"));
            list.add(p);
        }
        return list;
    }

    public boolean buatPesanan(int idPelanggan, int idSemen, int qty, double totalTagihan, boolean isLunas, int idStaf) throws SQLException {
        Connection conn = Koneksi.getKoneksi();
        try {
            conn.setAutoCommit(false);

            // 1. Potong Stok Semen
            PreparedStatement psStok = conn.prepareStatement("UPDATE semen SET stok_qty = stok_qty - ? WHERE id_semen = ?");
            psStok.setInt(1, qty);
            psStok.setInt(2, idSemen);
            psStok.executeUpdate();

            // 2. Tambah Pembayaran
            int idPembayaran = getNextId("pembayaran", "id_pembayaran");
            PreparedStatement psBayar = conn.prepareStatement("INSERT INTO pembayaran (id_pembayaran, tgl_bayar, metode_bayar, jumlah_bayar_rp, status, pesanan_id_pesanan) VALUES (?, ?, ?, ?, ?, ?)");
            psBayar.setInt(1, idPembayaran);
            psBayar.setDate(2, Date.valueOf(LocalDate.now()));
            psBayar.setString(3, "Tunai");
            psBayar.setDouble(4, totalTagihan);
            psBayar.setString(5, isLunas ? "Lunas" : "Belum Lunas");
            psBayar.setInt(6, 0);
            psBayar.executeUpdate();

            // 3. Tambah Pesanan
            int idPesanan = getNextId("pesanan", "id_pesanan");
            PreparedStatement psPesanan = conn.prepareStatement("INSERT INTO pesanan (id_pesanan, tgl_pesanan, total_tagihan_rp, status, staf_penjualan_id_pengguna, pembayaran_id_pembayaran, pelanggan_id_pelanggan) VALUES (?, ?, ?, ?, ?, ?, ?)");
            psPesanan.setInt(1, idPesanan);
            psPesanan.setDate(2, Date.valueOf(LocalDate.now()));
            psPesanan.setDouble(3, totalTagihan);
            psPesanan.setString(4, "Diproses");
            psPesanan.setInt(5, idStaf);
            psPesanan.setInt(6, idPembayaran);
            psPesanan.setInt(7, idPelanggan);
            psPesanan.executeUpdate();

            // Update FK Pembayaran
            PreparedStatement psUpBayar = conn.prepareStatement("UPDATE pembayaran SET pesanan_id_pesanan = ? WHERE id_pembayaran = ?");
            psUpBayar.setInt(1, idPesanan);
            psUpBayar.setInt(2, idPembayaran);
            psUpBayar.executeUpdate();

            // 4. Tambah Detail Pesanan
            int idDetail = getNextId("detail_pesanan", "id_detail");
            PreparedStatement psDetail = conn.prepareStatement("INSERT INTO detail_pesanan (id_detail, jumlah_qty, harga_satuan_rp, subtotal_rp, pesanan_id_pesanan, semen_id_semen) VALUES (?, ?, ?, ?, ?, ?)");
            psDetail.setInt(1, idDetail);
            psDetail.setInt(2, qty);
            psDetail.setDouble(3, totalTagihan / qty);
            psDetail.setDouble(4, totalTagihan);
            psDetail.setInt(5, idPesanan);
            psDetail.setInt(6, idSemen);
            psDetail.executeUpdate();

            conn.commit();
            conn.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            conn.rollback();
            conn.setAutoCommit(true);
            throw e;
        }
    }

    public boolean updateStatusPembayaran(int idPesanan, String statusBayarBaru) throws SQLException {
        Connection conn = Koneksi.getKoneksi();
        String sql = "UPDATE pembayaran SET status = ? WHERE pesanan_id_pesanan = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, statusBayarBaru);
        ps.setInt(2, idPesanan);
        return ps.executeUpdate() > 0;
    }

    public boolean updateStatusPengiriman(int idPesanan, String statusKirimBaru) throws SQLException {
        Connection conn = Koneksi.getKoneksi();
        String sql = "UPDATE pesanan SET status = ? WHERE id_pesanan = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, statusKirimBaru);
        ps.setInt(2, idPesanan);
        return ps.executeUpdate() > 0;
    }

    private int getNextId(String table, String col) throws SQLException {
        Connection conn = Koneksi.getKoneksi();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT MAX(" + col + ") FROM " + table);
        return (rs.next()) ? rs.getInt(1) + 1 : 1;
    }
}
