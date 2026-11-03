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
}
