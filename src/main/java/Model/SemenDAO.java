/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

import Config.Koneksi;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author LOQ
 */
public class SemenDAO {

    public List<Semen> getAll() throws SQLException {
        List<Semen> list = new ArrayList<>();
        Connection conn = Koneksi.getKoneksi();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM semen ORDER BY id_semen");
        while (rs.next()) {
            list.add(new Semen(
                rs.getInt("id_semen"),
                rs.getString("nama_semen"),
                rs.getString("satuan"),
                rs.getDouble("harga_jual_rp"),
                rs.getInt("stok_qty"),
                rs.getInt("batas_aman")
            ));
        }
        return list;
    }

    public Semen getById(int id) throws SQLException {
        Connection conn = Koneksi.getKoneksi();
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM semen WHERE id_semen=?");
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return new Semen(
                rs.getInt("id_semen"),
                rs.getString("nama_semen"),
                rs.getString("satuan"),
                rs.getDouble("harga_jual_rp"),
                rs.getInt("stok_qty"),
                rs.getInt("batas_aman")
            );
        }
        return null;
    }

    public boolean updateStok(int idSemen, int penambahanQty) throws SQLException {
        Connection conn = Koneksi.getKoneksi();
        String sql = "UPDATE semen SET stok_qty = stok_qty + ? WHERE id_semen = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, penambahanQty);
        ps.setInt(2, idSemen);
        return ps.executeUpdate() > 0;
    }

    public boolean updateData(Semen s) throws SQLException {
        Connection conn = Koneksi.getKoneksi();
        String sql = "UPDATE semen SET nama_semen=?, satuan=?, harga_jual_rp=?, batas_aman=? WHERE id_semen=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, s.getNamaSemen());
        ps.setString(2, s.getSatuan());
        ps.setDouble(3, s.getHargaJual());
        ps.setInt(4, s.getBatasAman());
        ps.setInt(5, s.getIdSemen());
        return ps.executeUpdate() > 0;
    }

    public boolean insert(Semen s) throws SQLException {
        Connection conn = Koneksi.getKoneksi();
        String sql = "INSERT INTO semen (id_semen, nama_semen, satuan, harga_jual_rp, stok_qty, batas_aman) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, getNextId());
        ps.setString(2, s.getNamaSemen());
        ps.setString(3, s.getSatuan());
        ps.setDouble(4, s.getHargaJual());
        ps.setInt(5, s.getStok());
        ps.setInt(6, s.getBatasAman());
        return ps.executeUpdate() > 0;
    }

    public boolean delete(int id) throws SQLException {
        Connection conn = Koneksi.getKoneksi();
        PreparedStatement ps = conn.prepareStatement("DELETE FROM semen WHERE id_semen=?");
        ps.setInt(1, id);
        return ps.executeUpdate() > 0;
    }

    public List<Semen> getStokMenipis() throws SQLException {
        List<Semen> list = new ArrayList<>();
        Connection conn = Koneksi.getKoneksi();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM semen WHERE stok_qty <= batas_aman");
        while (rs.next()) {
            list.add(new Semen(
                rs.getInt("id_semen"),
                rs.getString("nama_semen"),
                rs.getString("satuan"),
                rs.getDouble("harga_jual_rp"),
                rs.getInt("stok_qty"),
                rs.getInt("batas_aman")
            ));
        }
        return list;
    }

    private int getNextId() throws SQLException {
        Connection conn = Koneksi.getKoneksi();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT MAX(id_semen) FROM semen");
        return (rs.next()) ? rs.getInt(1) + 1 : 1;
    }

    public boolean catatPasokanMasuk(int idSemen, String batch, int jumlah, int idAdmin) throws SQLException {
        Connection conn = Koneksi.getKoneksi();
        try {
            conn.setAutoCommit(false);

            // 1. Generate ID Baru untuk stok_masuk (MAX + 1)
            int newIdStokMasuk = 1;
            String sqlMaxMasuk = "SELECT COALESCE(MAX(id_stok_masuk), 0) + 1 AS next_id FROM stok_masuk";
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sqlMaxMasuk)) {
                if (rs.next()) {
                    newIdStokMasuk = rs.getInt("next_id");
                }
            }

            // 2. Generate ID Baru untuk detail_stok_masuk (MAX + 1)
            int newIdDetailStok = 1;
            String sqlMaxDetail = "SELECT COALESCE(MAX(id_detail_stok), 0) + 1 AS next_id FROM detail_stok_masuk";
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sqlMaxDetail)) {
                if (rs.next()) {
                    newIdDetailStok = rs.getInt("next_id");
                }
            }

            // 3. Simpan ke tabel stok_masuk
            String sqlStokMasuk = "INSERT INTO stok_masuk (id_stok_masuk, tgl_masuk, semen_id_semen, admin_gudang_id_pengguna) VALUES (?, CURDATE(), ?, ?)";
            try (PreparedStatement psMasuk = conn.prepareStatement(sqlStokMasuk)) {
                psMasuk.setInt(1, newIdStokMasuk);
                psMasuk.setInt(2, idSemen);
                psMasuk.setInt(3, idAdmin);
                psMasuk.executeUpdate();
            }

            // 4. Simpan ke tabel detail_stok_masuk
            String sqlDetail = "INSERT INTO detail_stok_masuk (id_detail_stok, batch, jumlah_qty, stok_masuk_id_stok_masuk, semen_id_semen) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail)) {
                psDetail.setInt(1, newIdDetailStok);
                psDetail.setString(2, batch);
                psDetail.setInt(3, jumlah);
                psDetail.setInt(4, newIdStokMasuk);
                psDetail.setInt(5, idSemen);
                psDetail.executeUpdate();
            }

            // 5. Tambahkan stok fisik semen
            String sqlUpdateStok = "UPDATE semen SET stok_qty = stok_qty + ? WHERE id_semen = ?";
            try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdateStok)) {
                psUpdate.setInt(1, jumlah);
                psUpdate.setInt(2, idSemen);
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
}