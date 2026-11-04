/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Util;

import Model.Pengguna;

/**
 *
 * @author gedascc
 */
public class SesiPengguna {
    private static Pengguna penggunaAktif;

    public static void setPengguna(Pengguna p) {
        penggunaAktif = p;
    }

    public static Pengguna getPengguna() {
        return penggunaAktif;
    }
}
