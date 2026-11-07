package dao;

import java.sql.SQLException;

/**
 *
 * @author gedascc
 */
public interface AdminGudangDAOInterface {
    String getShiftByIdPengguna(int idPengguna) throws SQLException;
}