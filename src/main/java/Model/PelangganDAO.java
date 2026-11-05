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
public class PelangganDAO {

    public List<Pelanggan> getAll() throws SQLException {
        List<Pelanggan> list = new ArrayList<>();
        Connection conn = Koneksi.getKoneksi();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM pelanggan ORDER BY id_pelanggan");
        while (rs.next()) {
            list.add(new Pelanggan(
                rs.getInt("id_pelanggan"),
                rs.getString("nama_usaha"),
                rs.getString("jenis_pelanggan"),
                rs.getString("alamat_kirim"),
                rs.getString("no_telepon")
            ));
        }
        return list;
    }

    public boolean insert(Pelanggan p) throws SQLException {
        Connection conn = Koneksi.getKoneksi();
        String sql = "INSERT INTO pelanggan (id_pelanggan, nama_usaha, jenis_pelanggan, alamat_kirim, no_telepon) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, getNextId());
        ps.setString(2, p.getNamaUsaha());
        ps.setString(3, p.getJenisPelanggan());
        ps.setString(4, p.getAlamatKirim());
        ps.setString(5, p.getNoTelepon());
        return ps.executeUpdate() > 0;
    }

    public boolean update(Pelanggan p) throws SQLException {
        Connection conn = Koneksi.getKoneksi();
        String sql = "UPDATE pelanggan SET nama_usaha=?, jenis_pelanggan=?, alamat_kirim=?, no_telepon=? WHERE id_pelanggan=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, p.getNamaUsaha());
        ps.setString(2, p.getJenisPelanggan());
        ps.setString(3, p.getAlamatKirim());
        ps.setString(4, p.getNoTelepon());
        ps.setInt(5, p.getIdPelanggan());
        return ps.executeUpdate() > 0;
    }

    public boolean delete(int id) throws SQLException {
        Connection conn = Koneksi.getKoneksi();
        String sql = "DELETE FROM pelanggan WHERE id_pelanggan=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        return ps.executeUpdate() > 0;
    }

    public List<Pelanggan> searchByName(String keyword) throws SQLException {
        List<Pelanggan> list = new ArrayList<>();
        Connection conn = Koneksi.getKoneksi();
        String sql = "SELECT * FROM pelanggan WHERE nama_usaha LIKE ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, "%" + keyword + "%");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(new Pelanggan(
                rs.getInt("id_pelanggan"),
                rs.getString("nama_usaha"),
                rs.getString("jenis_pelanggan"),
                rs.getString("alamat_kirim"),
                rs.getString("no_telepon")
            ));
        }
        return list;
    }

    private int getNextId() throws SQLException {
        Connection conn = Koneksi.getKoneksi();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT MAX(id_pelanggan) FROM pelanggan");
        return (rs.next()) ? rs.getInt(1) + 1 : 1;
    }
    
    public int getNextIdPelanggan() throws SQLException {
    String sql = "SELECT COALESCE(MAX(id_pelanggan), 0) + 1 AS next_id FROM pelanggan";
    try (Connection conn = Koneksi.getKoneksi();
         Statement st = conn.createStatement();
         ResultSet rs = st.executeQuery(sql)) {
        if (rs.next()) {
            return rs.getInt("next_id");
        }
    }
    return 1;
}
}
