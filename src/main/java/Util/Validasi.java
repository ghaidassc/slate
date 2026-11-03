/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Util;
import java.util.Scanner;
/**
 *
 * @author LOQ
 */
public class Validasi {
    private static final Scanner sc = new Scanner(System.in);

    public static String readString(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    public static int readInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Output Input Tidak Valid! Masukkan angka.");
            }
        }
    }

    public static double readDouble(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Double.parseDouble(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Output Input Tidak Valid! Masukkan nominal angka.");
            }
        }
    }
    
    public static String readTelepon(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            
            // RegEx [0-9]{8,15} artinya: Harus berupa angka 0-9 dan panjangnya 8 sampai 15 digit
            if (input.matches("[0-9]{8,15}")) {
                return input;
            }
            System.out.println("[ERROR] Nomor telepon harus berupa angka (minimal 8 digit, maksimal 15 digit)!");
        }
    }

    public static boolean readKonfirmasi(String prompt) {
        System.out.print(prompt + " (y/n): ");
        String s = sc.nextLine().trim();
        return s.equalsIgnoreCase("y");
    }

    public static boolean isTeleponValid(String telp) {
        return telp != null && telp.matches("[0-9]{8,15}");
    }
}
