package dao;

import Config.Koneksi;
import Model.Pelanggan;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PelangganDAO implements PelangganDAOInterface {

    /**
     * Normalisasi otomatis: mendeteksi jika data di database tersimpan terbalik
     */
    private Pelanggan mapResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_pelanggan");
        String nama = rs.getString("nama_usaha");
        String jenis = rs.getString("jenis_pelanggan");
        String colAlamat = rs.getString("alamat_kirim");
        String colTelepon = rs.getString("no_telepon");

        String telepon = colTelepon != null ? colTelepon.trim() : "";
        String alamat = colAlamat != null ? colAlamat.trim() : "";

        // Jika kolom alamat di database berisi angka HP dan kolom telepon berisi teks alamat, posisinya ditukar otomatis
        if (alamat.matches("^[0-9+\\-\\s()]+$") && telepon.matches(".*[a-zA-Z].*")) {
            String temp = telepon;
            telepon = alamat;
            alamat = temp;
        }

        return new Pelanggan(id, nama, jenis, telepon, alamat);
    }

    @Override
    public List<Pelanggan> getAll() throws SQLException {
        List<Pelanggan> list = new ArrayList<>();
        String sql = "SELECT * FROM pelanggan ORDER BY id_pelanggan";
        try (Connection conn = Koneksi.getKoneksi();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        }
        return list;
    }

    @Override
    public boolean insert(Pelanggan p) throws SQLException {
        p.validasi(); // Validasi model dipanggil di DAO
        String sql = "INSERT INTO pelanggan (id_pelanggan, nama_usaha, jenis_pelanggan, alamat_kirim, no_telepon) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Koneksi.getKoneksi()) {
            int nextId = hitungNextId(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, nextId);
                ps.setString(2, p.getNamaUsaha());
                ps.setString(3, p.getJenisPelanggan());
                ps.setString(4, p.getAlamat());
                ps.setString(5, p.getNomorTelepon());
                return ps.executeUpdate() > 0;
            }
        }
    }

    @Override
    public boolean update(Pelanggan p) throws SQLException {
        p.validasi(); // Validasi model dipanggil di DAO
        String sql = "UPDATE pelanggan SET nama_usaha=?, jenis_pelanggan=?, alamat_kirim=?, no_telepon=? WHERE id_pelanggan=?";
        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNamaUsaha());
            ps.setString(2, p.getJenisPelanggan());
            ps.setString(3, p.getAlamat());
            ps.setString(4, p.getNomorTelepon());
            ps.setInt(5, p.getIdPelanggan());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM pelanggan WHERE id_pelanggan=?";
        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<Pelanggan> searchByName(String keyword) throws SQLException {
        return searchWithFilter(keyword, "Semua Jenis");
    }

    @Override
    public List<Pelanggan> searchWithFilter(String keyword, String jenisFilter) throws SQLException {
        List<Pelanggan> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM pelanggan WHERE (nama_usaha LIKE ? OR alamat_kirim LIKE ? OR no_telepon LIKE ?) ");
        
        boolean pakaiFilter = jenisFilter != null && !jenisFilter.equalsIgnoreCase("Semua Jenis");
        if (pakaiFilter) {
            sql.append("AND jenis_pelanggan LIKE ? ");
        }
        sql.append("ORDER BY id_pelanggan ASC");

        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            String param = "%" + keyword + "%";
            ps.setString(1, param);
            ps.setString(2, param);
            ps.setString(3, param);
            
            if (pakaiFilter) {
                ps.setString(4, "%" + jenisFilter + "%");
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        }
        return list;
    }

    @Override
    public int getNextIdPelanggan() throws SQLException {
        try (Connection conn = Koneksi.getKoneksi()) {
            return hitungNextId(conn);
        }
    }

    private int hitungNextId(Connection conn) throws SQLException {
        String sql = "SELECT COALESCE(MAX(id_pelanggan), 0) + 1 AS next_id FROM pelanggan";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("next_id");
            }
        }
        return 1;
    }
}