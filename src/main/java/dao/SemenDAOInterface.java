/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import Model.Semen;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author gedascc
 */
public interface SemenDAOInterface {
    List<Semen> getAll() throws SQLException;
    Semen getById(int id) throws SQLException;
    boolean insert(Semen semen) throws SQLException;
    boolean updateData(Semen semen) throws SQLException;
    boolean delete(int id) throws SQLException;
    boolean updateStok(int id, int qty) throws SQLException;
    boolean catatPasokanMasuk(int idSemen, String batch, int jumlah, int idAdmin) throws SQLException;
    List<Semen> getStokMenipis() throws SQLException;
}
