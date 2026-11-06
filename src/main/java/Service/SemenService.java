/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;
import Model.Semen;
import dao.SemenDAO;
import java.sql.SQLException;
import java.util.List;
import dao.SemenDAOInterface;
/**
 *
 * @author LOQ
 */
public class SemenService {
    // Abstraction: mengacu pada interface
    private final SemenDAOInterface semenDAO;

    public SemenService() {
        this.semenDAO = new SemenDAO();
    }

    public List<Semen> getAllSemen() throws SQLException {
        return semenDAO.getAll();
    }

    public Semen getSemenById(int id) throws SQLException {
        return semenDAO.getById(id);
    }

    public boolean tambahStok(int id, int qty) throws SQLException {
        return semenDAO.updateStok(id, qty);
    }

    public boolean ubahDataSemen(int id, String nama, String satuan, double harga, int batasAman) throws SQLException {
        return semenDAO.updateData(new Semen(id, nama, satuan, harga, 0, batasAman));
    }

    public boolean tambahSemenBaru(String nama, String satuan, double harga, int stok, int batasAman) throws SQLException {
        return semenDAO.insert(new Semen(0, nama, satuan, harga, stok, batasAman));
    }

    public boolean hapusSemen(int id) throws SQLException {
        return semenDAO.delete(id);
    }

    public boolean catatPasokanMasuk(int idSemen, String batch, int jumlah, int idAdmin) throws SQLException {
        return semenDAO.catatPasokanMasuk(idSemen, batch, jumlah, idAdmin);
    }

    public List<Semen> getPeringatanStok() throws SQLException {
        return semenDAO.getStokMenipis();
    }
}
