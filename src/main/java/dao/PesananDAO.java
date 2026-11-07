package dao;

import Config.Koneksi;
import Model.DetailNota;
import Model.Pesanan;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PesananDAO implements PesananDAOInterface {

    /**
     * Normalisasi nilai metode bayar agar sesuai dengan tipe kolom MySQL
     * (menghindari error truncation pada ENUM('Tunai','Transfer') atau VARCHAR pendek)
     */
    private String normalisasiMetodeBayar(String metode) {
        if (metode != null && metode.toLowerCase().contains("transfer")) {
            return "Transfer";
        }
        return "Tunai";
    }

    @Override
    public List<Pesanan> getAllRekap() throws SQLException {
        List<Pesanan> list = new ArrayList<>();
        String sql = "SELECT p.id_pesanan, p.tgl_pesanan, pl.nama_usaha, p.total_tagihan_rp, "
                   + "p.status AS status_kirim, pb.status AS status_bayar "
                   + "FROM pesanan p "
                   + "JOIN pelanggan pl ON p.pelanggan_id_pelanggan = pl.id_pelanggan "
                   + "JOIN pembayaran pb ON pb.id_pembayaran = p.pembayaran_id_pembayaran "
                   + "ORDER BY p.id_pesanan ASC";

        try (Connection conn = Koneksi.getKoneksi();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
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
        }
        return list;
    }

    @Override
    public boolean buatPesanan(int idPelanggan, int idSemen, int qty, double totalTagihan, boolean isLunas, int idStaf) throws SQLException {
        Connection conn = null;
        try {
            conn = Koneksi.getKoneksi();
            conn.setAutoCommit(false);

            try (PreparedStatement psStok = conn.prepareStatement("UPDATE semen SET stok_qty = stok_qty - ? WHERE id_semen = ? AND stok_qty >= ?")) {
                psStok.setInt(1, qty);
                psStok.setInt(2, idSemen);
                psStok.setInt(3, qty);
                if (psStok.executeUpdate() == 0) {
                    throw new SQLException("Stok fisik di gudang tidak mencukupi!");
                }
            }

            int idPembayaran = getNextId(conn, "pembayaran", "id_pembayaran");
            String sqlBayar = "INSERT INTO pembayaran (id_pembayaran, tgl_bayar, metode_bayar, jumlah_bayar_rp, status, pesanan_id_pesanan) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement psBayar = conn.prepareStatement(sqlBayar)) {
                psBayar.setInt(1, idPembayaran);
                psBayar.setDate(2, Date.valueOf(LocalDate.now()));
                psBayar.setString(3, "Tunai");
                psBayar.setDouble(4, totalTagihan);
                psBayar.setString(5, isLunas ? "Lunas" : "Belum Lunas");
                psBayar.setInt(6, 0);
                psBayar.executeUpdate();
            }

            int idPesanan = getNextId(conn, "pesanan", "id_pesanan");
            String sqlPesanan = "INSERT INTO pesanan (id_pesanan, tgl_pesanan, total_tagihan_rp, status, staf_penjualan_id_pengguna, pembayaran_id_pembayaran, pelanggan_id_pelanggan) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement psPesanan = conn.prepareStatement(sqlPesanan)) {
                psPesanan.setInt(1, idPesanan);
                psPesanan.setDate(2, Date.valueOf(LocalDate.now()));
                psPesanan.setDouble(3, totalTagihan);
                psPesanan.setString(4, "Diproses");
                psPesanan.setInt(5, idStaf);
                psPesanan.setInt(6, idPembayaran);
                psPesanan.setInt(7, idPelanggan);
                psPesanan.executeUpdate();
            }

            try (PreparedStatement psUpBayar = conn.prepareStatement("UPDATE pembayaran SET pesanan_id_pesanan = ? WHERE id_pembayaran = ?")) {
                psUpBayar.setInt(1, idPesanan);
                psUpBayar.setInt(2, idPembayaran);
                psUpBayar.executeUpdate();
            }

            int idDetail = getNextId(conn, "detail_pesanan", "id_detail");
            String sqlDetail = "INSERT INTO detail_pesanan (id_detail, jumlah_qty, harga_satuan_rp, subtotal_rp, pesanan_id_pesanan, semen_id_semen) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail)) {
                psDetail.setInt(1, idDetail);
                psDetail.setInt(2, qty);
                psDetail.setDouble(3, totalTagihan / qty);
                psDetail.setDouble(4, totalTagihan);
                psDetail.setInt(5, idPesanan);
                psDetail.setInt(6, idSemen);
                psDetail.executeUpdate();
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

    @Override
    public int simpanPesananLengkap(int idPelanggan, int idStaf, String metodeBayar, String statusBayar, 
                                     double totalTagihan, double nominalBayar, List<Object[]> itemKeranjang) throws SQLException {
        if (itemKeranjang.isEmpty()) {
            throw new IllegalArgumentException("Daftar item pesanan masih kosong!");
        }

        Connection conn = null;
        try {
            conn = Koneksi.getKoneksi();
            conn.setAutoCommit(false);

            try (Statement st = conn.createStatement()) {
                st.execute("SET FOREIGN_KEY_CHECKS = 0");
            }

            int nextIdPesanan = getNextId(conn, "pesanan", "id_pesanan");
            int nextIdBayar = getNextId(conn, "pembayaran", "id_pembayaran");

            // Normalisasi nilai metode bayar sebelum insert (Transfer Bank -> Transfer)
            String metodeDb = normalisasiMetodeBayar(metodeBayar);

            String sqlBayar = "INSERT INTO pembayaran (id_pembayaran, tgl_bayar, metode_bayar, jumlah_bayar_rp, status, pesanan_id_pesanan) VALUES (?, CURDATE(), ?, ?, ?, ?)";
            try (PreparedStatement psBayar = conn.prepareStatement(sqlBayar)) {
                psBayar.setInt(1, nextIdBayar);
                psBayar.setString(2, metodeDb);
                psBayar.setDouble(3, nominalBayar);
                psBayar.setString(4, statusBayar);
                psBayar.setInt(5, nextIdPesanan);
                psBayar.executeUpdate();
            }

            String sqlPesanan = "INSERT INTO pesanan (id_pesanan, tgl_pesanan, total_tagihan_rp, status, staf_penjualan_id_pengguna, pembayaran_id_pembayaran, pelanggan_id_pelanggan) VALUES (?, CURDATE(), ?, 'Diproses', ?, ?, ?)";
            try (PreparedStatement psPesanan = conn.prepareStatement(sqlPesanan)) {
                psPesanan.setInt(1, nextIdPesanan);
                psPesanan.setDouble(2, totalTagihan);
                psPesanan.setInt(3, idStaf);
                psPesanan.setInt(4, nextIdBayar);
                psPesanan.setInt(5, idPelanggan);
                psPesanan.executeUpdate();
            }

            String sqlDetail = "INSERT INTO detail_pesanan (id_detail, jumlah_qty, harga_satuan_rp, subtotal_rp, pesanan_id_pesanan, semen_id_semen) VALUES (?, ?, ?, ?, ?, ?)";
            String sqlKurangStok = "UPDATE semen SET stok_qty = stok_qty - ? WHERE id_semen = ? AND stok_qty >= ?";

            int nextIdDetail = getNextId(conn, "detail_pesanan", "id_detail");

            try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail);
                 PreparedStatement psStok = conn.prepareStatement(sqlKurangStok)) {

                for (Object[] item : itemKeranjang) {
                    int idSemen = (int) item[0];
                    int qty = (int) item[1];
                    double harga = (double) item[2];
                    double subtotal = (double) item[3];

                    psStok.setInt(1, qty);
                    psStok.setInt(2, idSemen);
                    psStok.setInt(3, qty);
                    int barisBerubah = psStok.executeUpdate();
                    
                    if (barisBerubah == 0) {
                        conn.rollback();
                        throw new SQLException("Stok fisik di gudang tidak mencukupi untuk memproses pesanan!");
                    }

                    psDetail.setInt(1, nextIdDetail++);
                    psDetail.setInt(2, qty);
                    psDetail.setDouble(3, harga);
                    psDetail.setDouble(4, subtotal);
                    psDetail.setInt(5, nextIdPesanan);
                    psDetail.setInt(6, idSemen);
                    psDetail.executeUpdate();
                }
            }

            try (Statement st = conn.createStatement()) {
                st.execute("SET FOREIGN_KEY_CHECKS = 1");
            }

            conn.commit();
            return nextIdPesanan;
        } catch (SQLException ex) {
            if (conn != null) {
                try {
                    conn.rollback();
                    try (Statement st = conn.createStatement()) {
                        st.execute("SET FOREIGN_KEY_CHECKS = 1");
                    }
                } catch (SQLException ignored) {}
            }
            throw ex;
        } finally {
            if (conn != null) conn.setAutoCommit(true);
        }
    }

    @Override
    public int getStokSemen(int idSemen) throws SQLException {
        String sql = "SELECT * FROM semen WHERE id_semen = ?";
        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idSemen);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    try {
                        return rs.getInt("stok_qty");
                    } catch (Exception e) {
                        return rs.getInt("stok");
                    }
                }
            }
        } catch (Exception ignored) {}
        return 10000;
    }

    @Override
    public List<Object[]> getAllSemen() throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT * FROM semen ORDER BY id_semen ASC";
        try (Connection conn = Koneksi.getKoneksi();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id_semen");
                String nama = rs.getString("nama_semen");
                
                int stok = 0;
                try {
                    stok = rs.getInt("stok_qty");
                } catch (Exception e) {
                    try { stok = rs.getInt("stok"); } catch (Exception ignored) {}
                }
                
                String satuan = "Sak";
                try {
                    satuan = rs.getString("satuan");
                } catch (Exception e) {
                    if (nama.toLowerCase().contains("curah") || nama.toLowerCase().contains("ton")) {
                        satuan = "Ton";
                    }
                }
                
                double harga = 0;
                try {
                    harga = rs.getDouble("harga_satuan_rp");
                } catch (Exception e1) {
                    try {
                        harga = rs.getDouble("harga");
                    } catch (Exception e2) {
                        if (nama.contains("50kg")) harga = 68000;
                        else if (nama.contains("40kg")) harga = 56000;
                        else harga = 1100000;
                    }
                }
                if (harga == 0) {
                    if (nama.contains("50kg")) harga = 68000;
                    else if (nama.contains("40kg")) harga = 56000;
                    else harga = 1100000;
                }
                
                list.add(new Object[]{id, nama, harga, stok, satuan});
            }
        } catch (Exception ignored) {}

        if (list.isEmpty()) {
            list.add(new Object[]{1, "Semen Sak 50kg", 68000.0, 10000, "Sak"});
            list.add(new Object[]{2, "Semen Sak 40kg", 56000.0, 11900, "Sak"});
            list.add(new Object[]{3, "Semen Curah", 1100000.0, 500, "Ton"});
        }
        return list;
    }

    @Override
    public boolean refundPesanan(int idPesanan) throws SQLException {
        Connection conn = null;
        try {
            conn = Koneksi.getKoneksi();
            conn.setAutoCommit(false);

            String sqlCariDetail = "SELECT semen_id_semen, jumlah_qty FROM detail_pesanan WHERE pesanan_id_pesanan = ?";
            try (PreparedStatement psDetail = conn.prepareStatement(sqlCariDetail)) {
                psDetail.setInt(1, idPesanan);
                try (ResultSet rs = psDetail.executeQuery()) {
                    String sqlRestok = "UPDATE semen SET stok_qty = stok_qty + ? WHERE id_semen = ?";
                    try (PreparedStatement psRestok = conn.prepareStatement(sqlRestok)) {
                        while (rs.next()) {
                            int idSemen = rs.getInt("semen_id_semen");
                            int qty = rs.getInt("jumlah_qty");
                            psRestok.setInt(1, qty);
                            psRestok.setInt(2, idSemen);
                            psRestok.executeUpdate();
                        }
                    }
                }
            }

            String sqlPesanan = "UPDATE pesanan SET status = 'Dibatalkan' WHERE id_pesanan = ?";
            try (PreparedStatement psP = conn.prepareStatement(sqlPesanan)) {
                psP.setInt(1, idPesanan);
                psP.executeUpdate();
            }

            String sqlBayar = "UPDATE pembayaran p JOIN pesanan ps ON p.pesanan_id_pesanan = ps.id_pesanan " +
                             "SET p.status = 'Dibatalkan' WHERE ps.id_pesanan = ?";
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

    @Override
    public boolean updateStatusPembayaran(int idPesanan, String statusBayarBaru) throws SQLException {
        String sql = "UPDATE pembayaran SET status = ? WHERE pesanan_id_pesanan = ?";
        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, statusBayarBaru);
            ps.setInt(2, idPesanan);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateStatusPengiriman(int idPesanan, String statusKirimBaru) throws SQLException {
        String sql = "UPDATE pesanan SET status = ? WHERE id_pesanan = ?";
        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, statusKirimBaru);
            ps.setInt(2, idPesanan);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
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

    @Override
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

    @Override
    public boolean perbaruiStatusPesananDanBayar(int idPesanan, String statusKirimBaru, String statusBayarBaru) throws SQLException {
        Connection conn = null;
        try {
            conn = Koneksi.getKoneksi();
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
                        return false;
                    }
                }
            }

            if ("Refund".equalsIgnoreCase(statusKirimBaru) || "Dibatalkan".equalsIgnoreCase(statusKirimBaru)) {
                if (!"Refund".equalsIgnoreCase(statusKirimLama) && !"Dibatalkan".equalsIgnoreCase(statusKirimLama)) {
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

                    String sqlUpdateBayar = "UPDATE pembayaran SET status = 'Dibatalkan', tgl_bayar = CURDATE() WHERE pesanan_id_pesanan = ?";
                    try (PreparedStatement psBayar = conn.prepareStatement(sqlUpdateBayar)) {
                        psBayar.setInt(1, idPesanan);
                        psBayar.executeUpdate();
                    }
                }
            } else {
                String sqlUpdateBayar = "UPDATE pembayaran SET status = ?, jumlah_bayar_rp = ?, tgl_bayar = CURDATE() WHERE pesanan_id_pesanan = ?";
                try (PreparedStatement psBayar = conn.prepareStatement(sqlUpdateBayar)) {
                    psBayar.setString(1, statusBayarBaru);
                    psBayar.setDouble(2, "Lunas".equalsIgnoreCase(statusBayarBaru) ? totalTagihan : (totalTagihan / 2));
                    psBayar.setInt(3, idPesanan);
                    psBayar.executeUpdate();
                }
            }

            String sqlUpdatePesanan = "UPDATE pesanan SET status = ? WHERE id_pesanan = ?";
            try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdatePesanan)) {
                psUpdate.setString(1, statusKirimBaru);
                psUpdate.setInt(2, idPesanan);
                psUpdate.executeUpdate();
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

    private int getNextId(Connection conn, String table, String col) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(" + col + "), 0) + 1 FROM " + table)) {
            return rs.next() ? rs.getInt(1) : 1;
        }
    }

    @Override
    public Object[] getStatistikDashboardStaf() throws SQLException {
        double totalPendapatan = 0;
        int jumlahTransaksiSelesai = 0;
        String semenPalingLaku = "-";
        int totalQtyTerjual = 0;
        int pesananAktif = 0;
        int prosesKirim = 0;
        int menunggu = 0;

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

        String sqlLaku = "SELECT s.nama_semen, COALESCE(SUM(dp.jumlah_qty), 0) AS total_sak " +
                         "FROM detail_pesanan dp " +
                         "JOIN semen s ON dp.semen_id_semen = s.id_semen " +
                         "JOIN pesanan p ON dp.pesanan_id_pesanan = p.id_pesanan " +
                         "WHERE p.status NOT IN ('Refund', 'Dibatalkan') " +
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

        String sqlAktif = "SELECT " +
                          "COALESCE(SUM(CASE WHEN status IN ('Diproses', 'Dikirim', 'Sedang Dikirim') THEN 1 ELSE 0 END), 0) AS total_aktif, " +
                          "COALESCE(SUM(CASE WHEN status IN ('Dikirim', 'Sedang Dikirim') THEN 1 ELSE 0 END), 0) AS jml_kirim, " +
                          "COALESCE(SUM(CASE WHEN status = 'Diproses' THEN 1 ELSE 0 END), 0) AS jml_menunggu " +
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