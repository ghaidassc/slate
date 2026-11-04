package view;

import Model.Pengguna;
import Model.PenggunaDAO;
import Util.SesiPengguna;
import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

public class FormLogin extends javax.swing.JFrame {

    public FormLogin() {
        initComponents();
        this.setLocationRelativeTo(null);
        pasangLogo();
    }

    private void pasangLogo() {
        try {
            BufferedImage imgAsli = null;
            String[] daftarJalur = {
                "src/main/java/view/logo_slate.png.jpg",
                "src/main/java/view/logo_slate.png",
                "src/main/java/view/logonew.jpg"
            };

            for (String jalur : daftarJalur) {
                File f = new File(jalur);
                if (f.exists()) {
                    imgAsli = ImageIO.read(f);
                    break;
                }
            }

            if (imgAsli == null) {
                InputStream stream = getClass().getResourceAsStream("/view/logo_slate.png.jpg");
                if (stream == null) stream = getClass().getResourceAsStream("/view/logo_slate.png");
                if (stream == null) stream = getClass().getResourceAsStream("/view/logonew.jpg");
                if (stream != null) imgAsli = ImageIO.read(stream);
            }

            if (imgAsli != null) {
                // 1. Ambil warna background asli dari pojok gambar logo
                int sampleRgb = imgAsli.getRGB(0, 0);
                Color warnaBgLogo = new Color((sampleRgb >> 16) & 0xFF, (sampleRgb >> 8) & 0xFF, sampleRgb & 0xFF);
                
                // 2. Samakan warna panelKiri agar menyatu sempurna tanpa garis kotak
                panelKiri.setBackground(warnaBgLogo);

                Image imgResized = imgAsli.getScaledInstance(230, 230, Image.SCALE_SMOOTH);
                lblLogo.setIcon(new ImageIcon(imgResized));
                lblLogo.setText("");
            } else {
                lblLogo.setText("<html><center><span style='color:#FFFFFF; font-size:32px; font-weight:bold;'>SL<span style='color:#EA580C;'>A</span>TE</span></center></html>");
            }
        } catch (IOException e) {
            lblLogo.setText("<html><center><h1 style='color:#FFFFFF;'>SLATE</h1></center></html>");
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelKiri = new javax.swing.JPanel();
        lblLogo = new javax.swing.JLabel();
        panelKanan = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        lblUsername = new javax.swing.JLabel();
        txtUsername = new javax.swing.JTextField();
        lblPassword = new javax.swing.JLabel();
        txtPassword = new javax.swing.JPasswordField();
        lblRole = new javax.swing.JLabel();
        cmbRole = new javax.swing.JComboBox<>();
        btnLogin = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("SLATE - Login Sistem Informasi Semen");
        setPreferredSize(new java.awt.Dimension(900, 560));
        setResizable(false);

        panelKiri.setBackground(new java.awt.Color(22, 27, 34));
        panelKiri.setPreferredSize(new java.awt.Dimension(360, 560));
        panelKiri.setLayout(new java.awt.GridBagLayout());

        lblLogo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        panelKiri.add(lblLogo, new java.awt.GridBagConstraints());

        getContentPane().add(panelKiri, java.awt.BorderLayout.WEST);

        panelKanan.setBackground(new java.awt.Color(255, 255, 255));

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblTitle.setForeground(new java.awt.Color(15, 23, 42));
        lblTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitle.setText("LOGIN");

        lblUsername.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblUsername.setForeground(new java.awt.Color(71, 85, 105));
        lblUsername.setText("Username");

        txtUsername.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        lblPassword.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblPassword.setForeground(new java.awt.Color(71, 85, 105));
        lblPassword.setText("Password");

        txtPassword.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        lblRole.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblRole.setForeground(new java.awt.Color(71, 85, 105));
        lblRole.setText("Hak Akses (Role)");

        cmbRole.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cmbRole.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Staf Penjualan", "Admin Gudang" }));

        btnLogin.setBackground(new java.awt.Color(234, 88, 12));
        btnLogin.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnLogin.setForeground(new java.awt.Color(255, 255, 255));
        btnLogin.setText("LOGIN");
        btnLogin.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnLogin.setFocusPainted(false);
        btnLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoginActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelKananLayout = new javax.swing.GroupLayout(panelKanan);
        panelKanan.setLayout(panelKananLayout);
        panelKananLayout.setHorizontalGroup(
            panelKananLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelKananLayout.createSequentialGroup()
                .addGap(60, 60, 60)
                .addGroup(panelKananLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblUsername)
                    .addComponent(txtUsername)
                    .addComponent(lblPassword)
                    .addComponent(txtPassword)
                    .addComponent(lblRole)
                    .addComponent(cmbRole, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnLogin, javax.swing.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelKananLayout.setVerticalGroup(
            panelKananLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelKananLayout.createSequentialGroup()
                .addGap(55, 55, 55)
                .addComponent(lblTitle)
                .addGap(35, 35, 35)
                .addComponent(lblUsername)
                .addGap(8, 8, 8)
                .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblPassword)
                .addGap(8, 8, 8)
                .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblRole)
                .addGap(8, 8, 8)
                .addComponent(cmbRole, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(btnLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getContentPane().add(panelKanan, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoginActionPerformed
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String rolePilihan = cmbRole.getSelectedItem().toString();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Username dan Password tidak boleh kosong!",
                    "Validasi Masukan",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            PenggunaDAO dao = new PenggunaDAO();
            Pengguna user = dao.login(username, password);

            if (user != null) {
                SesiPengguna.setPengguna(user);

                // 1. Akun Staf Penjualan
                if ("staf_penjualan".equalsIgnoreCase(user.getRole())) {
                    if (!rolePilihan.equalsIgnoreCase("Staf Penjualan")) {
                        JOptionPane.showMessageDialog(this,
                                "Akun ini terdaftar sebagai Staf Penjualan!\nSilakan ubah pilihan Hak Akses menjadi 'Staf Penjualan'.",
                                "Akses Ditolak",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    JOptionPane.showMessageDialog(this,
                            "Login Berhasil! Selamat datang, " + user.getNamaLengkap(),
                            "Sukses",
                            JOptionPane.INFORMATION_MESSAGE);

                    new FormMenuStaf().setVisible(true);
                    this.dispose();

                // 2. Akun Admin Gudang
                } else if ("admin_gudang".equalsIgnoreCase(user.getRole())) {
                    if (!rolePilihan.equalsIgnoreCase("Admin Gudang")) {
                        JOptionPane.showMessageDialog(this,
                                "Akun ini terdaftar sebagai Admin Gudang!\nSilakan ubah pilihan Hak Akses menjadi 'Admin Gudang'.",
                                "Akses Ditolak",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    JOptionPane.showMessageDialog(this,
                            "Login Berhasil! Selamat datang, " + user.getNamaLengkap(),
                            "Sukses",
                            JOptionPane.INFORMATION_MESSAGE);

                    new FormMenuAdminGudang().setVisible(true);
                    this.dispose();

                } else {
                    JOptionPane.showMessageDialog(this,
                            "Role pengguna tidak dikenali sistem!",
                            "Error Role",
                            JOptionPane.ERROR_MESSAGE);
                }

            } else {
                JOptionPane.showMessageDialog(this,
                        "Username atau Password salah! Periksa kembali kredensial Anda.",
                        "Login Gagal",
                        JOptionPane.ERROR_MESSAGE);
                txtPassword.setText("");
                txtPassword.requestFocus();
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Gagal terhubung ke database!\nError: " + ex.getMessage(),
                    "Koneksi Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnLoginActionPerformed

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new FormLogin().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnLogin;
    private javax.swing.JComboBox<String> cmbRole;
    private javax.swing.JLabel lblLogo;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JLabel lblRole;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblUsername;
    private javax.swing.JPanel panelKanan;
    private javax.swing.JPanel panelKiri;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtUsername;
    // End of variables declaration//GEN-END:variables
}