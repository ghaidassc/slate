/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;
import Config.Koneksi;
import java.sql.*;
/**
 *
 * @author LOQ
 */
public class PenggunaDAO {

    public Pengguna login(String username, String password) throws SQLException {
        Connection conn = Koneksi.getKoneksi();
        // Menggunakan LEFT JOIN untuk mengambil detail wilayah_penjualan atau shift sekaligus
        String sql = "SELECT p.*, sp.wilayah_penjualan, ag.shift "
                   + "FROM pengguna p "
                   + "LEFT JOIN staf_penjualan sp ON p.id_pengguna = sp.id_pengguna "
                   + "LEFT JOIN admin_gudang ag ON p.id_pengguna = ag.id_pengguna "
                   + "WHERE p.nama_pengguna = ? AND p.sandi_pengguna = ?";

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, username);
        ps.setString(2, password);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            int id = rs.getInt("id_pengguna");
            String user = rs.getString("nama_pengguna");
            String pass = rs.getString("sandi_pengguna");
            String nama = rs.getString("nama_lengkap");
            String role = rs.getString("role");

            if ("staf_penjualan".equalsIgnoreCase(role)) {
                String wilayah = rs.getString("wilayah_penjualan");
                return new StafPenjualan(id, user, pass, nama, wilayah != null ? wilayah : "Umum");
            } else if ("admin_gudang".equalsIgnoreCase(role)) {
                String shift = rs.getString("shift");
                return new AdminGudang(id, user, pass, nama, shift != null ? shift : "Pagi");
            }
        }
        return null;
    }
}
