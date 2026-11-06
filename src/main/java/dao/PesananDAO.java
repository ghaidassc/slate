/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import Config.Koneksi;
import Model.DetailNota;
import Model.Pesanan;
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

    public List<Object[]> getAntreanPengiriman() throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT p.id_pesanan, pl.nama_usaha, pb.status AS status_bayar, p.status AS status_kirim " +
                     "FROM pesanan p " +
                     "JOIN pelanggan pl ON p.pelanggan_id_pelanggan = pl.id_pelanggan " +
                     "LEFT JOIN pembayaran pb ON p.pembayaran_id_pembayaran = pb.id_pembayaran " +
                     "ORDER BY p.id_pesanan ASC";

        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String id = String.valueOf(rs.getInt("id_pesanan"));
                String namaMitra = rs.getString("nama_usaha");
                String statusBayar = rs.getString("status_bayar");
                String statusKirim = rs.getString("status_kirim");
                if (statusBayar == null) statusBayar = "Belum Lunas";
                list.add(new Object[]{id, namaMitra, statusBayar, statusKirim});
            }
        }
        return list;
    }

    public DetailNota getDetailNota(int idPesanan) throws SQLException {
        String sql = "SELECT p.id_pesanan, p.tgl_pesanan, p.total_tagihan_rp, p.status AS status_kirim, " +
                     "pl.nama_usaha, pl.alamat_kirim, " +
                     "b.metode_bayar, b.status AS status_bayar, " +
                     "dp.jumlah_qty, s.nama_semen, s.satuan " +
                     "FROM pesanan p " +
                     "JOIN pelanggan pl ON p.pelanggan_id_pelanggan = pl.id_pelanggan " +
                     "LEFT JOIN pembayaran b ON p.pembayaran_id_pembayaran = b.id_pembayaran " +
                     "LEFT JOIN detail_pesanan dp ON p.id_pesanan = dp.pesanan_id_pesanan " +
                     "LEFT JOIN semen s ON dp.semen_id_semen = s.id_semen " +
                     "WHERE p.id_pesanan = ?";

        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPesanan);
            try (ResultSet rs = ps.executeQuery()) {
                DetailNota nota = null;
                List<String> muatanList = new ArrayList<>();

                while (rs.next()) {
                    if (nota == null) {
                        nota = new DetailNota();
                        nota.setIdPesanan(rs.getInt("id_pesanan"));
                        nota.setTglPesanan(rs.getDate("tgl_pesanan"));
                        nota.setTotalTagihan(rs.getDouble("total_tagihan_rp"));
                        nota.setStatusKirim(rs.getString("status_kirim"));
                        nota.setNamaUsaha(rs.getString("nama_usaha"));

                        String alamat = rs.getString("alamat_kirim");
                        nota.setAlamatKirim((alamat == null || alamat.trim().isEmpty()) ? "Samarinda" : alamat);

                        String metode = rs.getString("metode_bayar");
                        nota.setMetodeBayar(metode == null ? "Tunai" : metode);

                        String statusBayar = rs.getString("status_bayar");
                        nota.setStatusBayar(statusBayar == null ? "Belum Lunas" : statusBayar);
                    }

                    int qty = rs.getInt("jumlah_qty");
                    String namaSemen = rs.getString("nama_semen");
                    String satuan = rs.getString("satuan");
                    if (namaSemen != null) {
                        muatanList.add(qty + " " + satuan + " (" + namaSemen + ")");
                    }
                }

                if (nota != null) {
                    nota.setDaftarMuatan(muatanList);
                }
                return nota;
            }
        }
    }

    public boolean perbaruiStatusPesananDanBayar(int idPesanan, String statusKirimBaru, String statusBayarBaru) throws SQLException {
        Connection conn = Koneksi.getKoneksi();
        try {
            conn.setAutoCommit(false);

            String statusKirimLama = "";
            double totalTagihan = 0;

            String sqlCek = "SELECT p.status AS status_kirim, p.total_tagihan_rp, pb.status AS status_bayar " +
                            "FROM pesanan p " +
                            "LEFT JOIN pembayaran pb ON p.pembayaran_id_pembayaran = pb.id_pembayaran " +
                            "WHERE p.id_pesanan = ?";
            try (PreparedStatement psCek = conn.prepareStatement(sqlCek)) {
                psCek.setInt(1, idPesanan);
                try (ResultSet rs = psCek.executeQuery()) {
                    if (rs.next()) {
                        statusKirimLama = rs.getString("status_kirim");
                        totalTagihan = rs.getDouble("total_tagihan_rp");
                    } else {
                        conn.rollback();
                        conn.setAutoCommit(true);
                        return false;
                    }
                }
            }

            // Jika diubah menjadi Refund -> Kembalikan stok semen ke gudang & tandai pembayaran refund
            if ("Refund".equalsIgnoreCase(statusKirimBaru) && !"Refund".equalsIgnoreCase(statusKirimLama)) {
                String sqlAmbilItem = "SELECT semen_id_semen, jumlah_qty FROM detail_pesanan WHERE pesanan_id_pesanan = ?";
                String sqlRestock = "UPDATE semen SET stok_qty = stok_qty + ? WHERE id_semen = ?";

                try (PreparedStatement psItem = conn.prepareStatement(sqlAmbilItem);
                     PreparedStatement psRestock = conn.prepareStatement(sqlRestock)) {
                    psItem.setInt(1, idPesanan);
                    try (ResultSet rsItem = psItem.executeQuery()) {
                        while (rsItem.next()) {
                            int idSemen = rsItem.getInt("semen_id_semen");
                            int qty = rsItem.getInt("jumlah_qty");
                            psRestock.setInt(1, qty);
                            psRestock.setInt(2, idSemen);
                            psRestock.executeUpdate();
                        }
                    }
                }

                String sqlUpdateBayar = "UPDATE pembayaran SET status = 'Refund', tgl_refund = CURDATE() WHERE pesanan_id_pesanan = ?";
                try (PreparedStatement psBayar = conn.prepareStatement(sqlUpdateBayar)) {
                    psBayar.setInt(1, idPesanan);
                    psBayar.executeUpdate();
                }
            } else {
                // Update status pembayaran (Belum Lunas / Lunas)
                String sqlUpdateBayar = "UPDATE pembayaran SET status = ?, jumlah_bayar_rp = ?, tgl_bayar = CURDATE() WHERE pesanan_id_pesanan = ?";
                try (PreparedStatement psBayar = conn.prepareStatement(sqlUpdateBayar)) {
                    psBayar.setString(1, statusBayarBaru);
                    psBayar.setDouble(2, "Lunas".equalsIgnoreCase(statusBayarBaru) ? totalTagihan : (totalTagihan / 2));
                    psBayar.setInt(3, idPesanan);
                    psBayar.executeUpdate();
                }
            }

            // Update status pesanan di tabel pesanan
            String sqlUpdatePesanan = "UPDATE pesanan SET status = ? WHERE id_pesanan = ?";
            try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdatePesanan)) {
                psUpdate.setString(1, statusKirimBaru);
                psUpdate.setInt(2, idPesanan);
                psUpdate.executeUpdate();
            }

            conn.commit();
            conn.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
            throw e;
        }
    }

    private int getNextId(String table, String col) throws SQLException {
        Connection conn = Koneksi.getKoneksi();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT MAX(" + col + ") FROM " + table);
        return (rs.next()) ? rs.getInt(1) + 1 : 1;
    }
    
    public Object[] getStatistikDashboardStaf() throws SQLException {
        double totalPendapatan = 0;
        int jumlahTransaksiSelesai = 0;
        String semenPalingLaku = "-";
        int totalQtyTerjual = 0;
        int pesananAktif = 0;
        int prosesKirim = 0;
        int menunggu = 0;

        // 1. Total Pendapatan & Transaksi Selesai (Bulan Berjalan)
        String sqlPendapatan = "SELECT COALESCE(SUM(total_tagihan_rp), 0) AS total_rp, COUNT(id_pesanan) AS total_selesai " +
                               "FROM pesanan " +
                               "WHERE status = 'Diterima' AND MONTH(tgl_pesanan) = MONTH(CURDATE()) AND YEAR(tgl_pesanan) = YEAR(CURDATE())";
        try (Connection conn = Koneksi.getKoneksi();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sqlPendapatan)) {
            if (rs.next()) {
                totalPendapatan = rs.getDouble("total_rp");
                jumlahTransaksiSelesai = rs.getInt("total_selesai");
            }
        }

        // 2. Semen Paling Laku
        String sqlLaku = "SELECT s.nama_semen, COALESCE(SUM(dp.jumlah_qty), 0) AS total_sak " +
                         "FROM detail_pesanan dp " +
                         "JOIN semen s ON dp.semen_id_semen = s.id_semen " +
                         "JOIN pesanan p ON dp.pesanan_id_pesanan = p.id_pesanan " +
                         "WHERE p.status != 'Refund' " +
                         "GROUP BY s.id_semen, s.nama_semen " +
                         "ORDER BY total_sak DESC LIMIT 1";
        try (Connection conn = Koneksi.getKoneksi();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sqlLaku)) {
            if (rs.next()) {
                semenPalingLaku = rs.getString("nama_semen");
                totalQtyTerjual = rs.getInt("total_sak");
            }
        }

        // 3. Pesanan Aktif Berjalan (Diproses & Dikirim)
        String sqlAktif = "SELECT " +
                          "SUM(CASE WHEN status IN ('Diproses', 'Dikirim', 'Sedang Dikirim') THEN 1 ELSE 0 END) AS total_aktif, " +
                          "SUM(CASE WHEN status IN ('Dikirim', 'Sedang Dikirim') THEN 1 ELSE 0 END) AS jml_kirim, " +
                          "SUM(CASE WHEN status = 'Diproses' THEN 1 ELSE 0 END) AS jml_menunggu " +
                          "FROM pesanan";
        try (Connection conn = Koneksi.getKoneksi();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sqlAktif)) {
            if (rs.next()) {
                pesananAktif = rs.getInt("total_aktif");
                prosesKirim = rs.getInt("jml_kirim");
                menunggu = rs.getInt("jml_menunggu");
            }
        }

        return new Object[]{
            totalPendapatan, jumlahTransaksiSelesai,
            semenPalingLaku, totalQtyTerjual,
            pesananAktif, prosesKirim, menunggu
        };
    }
}