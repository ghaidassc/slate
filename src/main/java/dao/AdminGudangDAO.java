package dao;

import Config.Koneksi;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author gedascc
 */
public class AdminGudangDAO implements AdminGudangDAOInterface {

    @Override
    public String getShiftByIdPengguna(int idPengguna) throws SQLException {
        String sql = "SELECT shift FROM admin_gudang WHERE id_pengguna = ?";
        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPengguna);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("shift");
                }
            }
        }
        return "Pagi";
    }
}