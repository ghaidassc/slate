/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import Config.Koneksi;
import Model.Semen;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author LOQ
 */
public class SemenDAO implements SemenDAOInterface {

    @Override
    public List<Semen> getAll() throws SQLException {
        List<Semen> list = new ArrayList<>();
        String sql = "SELECT * FROM semen ORDER BY id_semen ASC";
        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
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
        }
        return list;
    }

    @Override
    public Semen getById(int id) throws SQLException {
        String sql = "SELECT * FROM semen WHERE id_semen = ?";
        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
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
            }
        }
        return null;
    }

    @Override
    public boolean insert(Semen semen) throws SQLException {
        String sql = "INSERT INTO semen (nama_semen, satuan, harga_jual_rp, stok_qty, batas_aman) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, semen.getNamaSemen());
            ps.setString(2, semen.getSatuan());
            ps.setDouble(3, semen.getHargaJual());
            ps.setInt(4, semen.getStok());
            ps.setInt(5, semen.getBatasAman());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateData(Semen semen) throws SQLException {
        String sql = "UPDATE semen SET nama_semen = ?, satuan = ?, harga_jual_rp = ?, batas_aman = ? WHERE id_semen = ?";
        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, semen.getNamaSemen());
            ps.setString(2, semen.getSatuan());
            ps.setDouble(3, semen.getHargaJual());
            ps.setInt(4, semen.getBatasAman());
            ps.setInt(5, semen.getIdSemen());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM semen WHERE id_semen = ?";
        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateStok(int id, int qty) throws SQLException {
        String sql = "UPDATE semen SET stok_qty = stok_qty + ? WHERE id_semen = ?";
        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, qty);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean catatPasokanMasuk(int idSemen, String batch, int jumlah, int idAdmin) throws SQLException {
        Connection conn = null;
        try {
            conn = Koneksi.getKoneksi();
            conn.setAutoCommit(false);

            // 1. Tambah stok semen di gudang
            String sqlStok = "UPDATE semen SET stok_qty = stok_qty + ? WHERE id_semen = ?";
            try (PreparedStatement psStok = conn.prepareStatement(sqlStok)) {
                psStok.setInt(1, jumlah);
                psStok.setInt(2, idSemen);
                psStok.executeUpdate();
            }

            // 2. Catat histori pasokan masuk jika ada tabel pasokan
            try {
                String sqlHistori = "INSERT INTO pasokan (semen_id_semen, no_batch, jumlah_masuk, admin_id) VALUES (?, ?, ?, ?)";
                try (PreparedStatement psHistori = conn.prepareStatement(sqlHistori)) {
                    psHistori.setInt(1, idSemen);
                    psHistori.setString(2, batch);
                    psHistori.setInt(3, jumlah);
                    psHistori.setInt(4, idAdmin);
                    psHistori.executeUpdate();
                }
            } catch (SQLException ignored) {
                // Berjalan aman meskipun tabel pasokan opsional
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
    public List<Semen> getStokMenipis() throws SQLException {
        List<Semen> list = new ArrayList<>();
        String sql = "SELECT * FROM semen WHERE stok_qty <= batas_aman ORDER BY stok_qty ASC";
        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
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
        }
        return list;
    }
}