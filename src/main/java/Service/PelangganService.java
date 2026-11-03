/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;
import Model.Pelanggan;
import Model.PelangganDAO;
import java.sql.SQLException;
import java.util.List;
/**
 *
 * @author LOQ
 */
public class PelangganService {
    private final PelangganDAO pelangganDAO = new PelangganDAO();

    public List<Pelanggan> getAllPelanggan() throws SQLException {
        return pelangganDAO.getAll();
    }

    public boolean tambahPelanggan(String nama, String jenis, String alamat, String telp) throws SQLException {
        return pelangganDAO.insert(new Pelanggan(0, nama, jenis, alamat, telp));
    }

    public boolean ubahPelanggan(int id, String nama, String jenis, String alamat, String telp) throws SQLException {
        return pelangganDAO.update(new Pelanggan(id, nama, jenis, alamat, telp));
    }

    public boolean hapusPelanggan(int id) throws SQLException {
        return pelangganDAO.delete(id);
    }

    public List<Pelanggan> cariPelanggan(String keyword) throws SQLException {
        return pelangganDAO.searchByName(keyword);
    }
}
