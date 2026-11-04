/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Config;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
/**
 *
 * @author LOQ
 */
public class Koneksi {
    private static final String URL = "jdbc:mysql://localhost:3306/slate_fix";
    private static final String USER = "root";
    private static final String PASS = "";
    private static Connection conn;

    public static Connection getKoneksi() throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = DriverManager.getConnection(URL, USER, PASS);
        }
        return conn;
    }
}