/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

import Config.Koneksi;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
/**
 *
 * @author LOQ
 */
public class PenggunaDAO {

    public Pengguna login(String username, String password) throws SQLException {
        Connection conn = Koneksi.getKoneksi();
        if (conn == null) {
            throw new SQLException("Koneksi database bernilai null!");
        }

        // Cetak log ke console untuk verifikasi input
        System.out.println("--> Mencoba login dengan username: [" + username + "] | password: [" + password + "]");

        String sql = "SELECT p.*, sp.wilayah_penjualan, ag.shift "
                   + "FROM pengguna p "
                   + "LEFT JOIN staf_penjualan sp ON p.id_pengguna = sp.id_pengguna "
                   + "LEFT JOIN admin_gudang ag ON p.id_pengguna = ag.id_pengguna "
                   + "WHERE p.nama_pengguna = ? AND p.sandi_pengguna = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id_pengguna");
                    String user = rs.getString("nama_pengguna");
                    String pass = rs.getString("sandi_pengguna");
                    String nama = rs.getString("nama_lengkap");
                    String role = rs.getString("role");

                    System.out.println("--> Berhasil menemukan user: " + nama + " (Role DB: " + role + ")");

                    if ("staf_penjualan".equalsIgnoreCase(role)) {
                        String wilayah = rs.getString("wilayah_penjualan");
                        return new StafPenjualan(id, user, pass, nama, wilayah != null ? wilayah : "Umum");
                    } else if ("admin_gudang".equalsIgnoreCase(role)) {
                        String shift = rs.getString("shift");
                        return new AdminGudang(id, user, pass, nama, shift != null ? shift : "Pagi");
                    }
                }
            }
        }
        System.out.println("--> Gagal: Username atau Password tidak cocok di database.");
        return null;
    }
}