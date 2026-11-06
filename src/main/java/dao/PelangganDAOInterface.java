/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import Model.Pelanggan;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author gedascc
 */
public interface PelangganDAOInterface {
    List<Pelanggan> getAll() throws SQLException;
    boolean insert(Pelanggan p) throws SQLException;
    boolean update(Pelanggan p) throws SQLException;
    boolean delete(int id) throws SQLException;
    List<Pelanggan> searchByName(String keyword) throws SQLException;
    List<Pelanggan> searchWithFilter(String keyword, String jenisFilter) throws SQLException;
    int getNextIdPelanggan() throws SQLException;
}
