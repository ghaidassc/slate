package view;

import Model.Pengguna;
import dao.PenggunaDAO;
import Util.SesiPengguna;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class FormLogin extends javax.swing.JFrame {

    private static final String PLACEHOLDER_USER = "Masukkan username...";
    private static final String PLACEHOLDER_PASS = "••••••••";
    private final Color warnaBorderDefault = new Color(203, 213, 225);
    private final Color warnaBorderFokus = new Color(234, 88, 12);
    private final Color warnaPlaceholder = new Color(148, 163, 184);
    private final Color warnaTeksAktif = new Color(15, 23, 42);

    public FormLogin() {
        initComponents();
        this.setSize(880, 552);
        this.setLocationRelativeTo(null);
        rapikanGayaKomponen();
        pasangLogo();
    }

    private void rapikanGayaKomponen() {
        // Border lembut rounded dengan padding di dalam
        Border garisBiasa = new LineBorder(warnaBorderDefault, 1, true);
        Border paddingBiasa = new EmptyBorder(0, 14, 0, 14);
        Border styleBiasa = new CompoundBorder(garisBiasa, paddingBiasa);

        Border garisFokus = new LineBorder(warnaBorderFokus, 1, true);
        Border styleFokus = new CompoundBorder(garisFokus, paddingBiasa);

        txtUsername.setBorder(styleBiasa);
        txtPassword.setBorder(styleBiasa);
        cmbRole.setBorder(new EmptyBorder(0, 4, 0, 4));

        // Setup placeholder Username
        txtUsername.setText(PLACEHOLDER_USER);
        txtUsername.setForeground(warnaPlaceholder);
        txtUsername.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                txtUsername.setBorder(styleFokus);
                if (txtUsername.getText().equals(PLACEHOLDER_USER)) {
                    txtUsername.setText("");
                    txtUsername.setForeground(warnaTeksAktif);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                txtUsername.setBorder(styleBiasa);
                if (txtUsername.getText().trim().isEmpty()) {
                    txtUsername.setText(PLACEHOLDER_USER);
                    txtUsername.setForeground(warnaPlaceholder);
                }
            }
        });

        // Setup placeholder Password
        txtPassword.setEchoChar((char) 0);
        txtPassword.setText(PLACEHOLDER_PASS);
        txtPassword.setForeground(warnaPlaceholder);
        txtPassword.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                txtPassword.setBorder(styleFokus);
                String pass = new String(txtPassword.getPassword());
                if (pass.equals(PLACEHOLDER_PASS)) {
                    txtPassword.setText("");
                    txtPassword.setEchoChar('•');
                    txtPassword.setForeground(warnaTeksAktif);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                txtPassword.setBorder(styleBiasa);
                String pass = new String(txtPassword.getPassword()).trim();
                if (pass.isEmpty()) {
                    txtPassword.setEchoChar((char) 0);
                    txtPassword.setText(PLACEHOLDER_PASS);
                    txtPassword.setForeground(warnaPlaceholder);
                }
            }
        });
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
                int sampleRgb = imgAsli.getRGB(0, 0);
                Color warnaBgLogo = new Color((sampleRgb >> 16) & 0xFF, (sampleRgb >> 8) & 0xFF, sampleRgb & 0xFF);
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
        btnMasuk = new javax.swing.JButton();
        btnKeluar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("SLATE - Masuk Sistem");
        setResizable(false);

        panelKiri.setBackground(new java.awt.Color(22, 27, 34));
        panelKiri.setPreferredSize(new java.awt.Dimension(380, 552));
        panelKiri.setLayout(new java.awt.GridBagLayout());

        lblLogo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        panelKiri.add(lblLogo, new java.awt.GridBagConstraints());

        getContentPane().add(panelKiri, java.awt.BorderLayout.WEST);

        panelKanan.setBackground(new java.awt.Color(255, 255, 255));

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblTitle.setForeground(new java.awt.Color(15, 23, 34));
        lblTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitle.setText("SELAMAT DATANG!");

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

        btnMasuk.setBackground(new java.awt.Color(234, 88, 12));
        btnMasuk.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnMasuk.setForeground(new java.awt.Color(255, 255, 255));
        btnMasuk.setText("MASUK");
        btnMasuk.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnMasuk.setFocusPainted(false);
        btnMasuk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMasukActionPerformed(evt);
            }
        });

        btnKeluar.setBackground(new java.awt.Color(234, 88, 12));
        btnKeluar.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnKeluar.setForeground(new java.awt.Color(255, 255, 255));
        btnKeluar.setText("KELUAR");
        btnKeluar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnKeluar.setFocusPainted(false);
        btnKeluar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnKeluarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelKananLayout = new javax.swing.GroupLayout(panelKanan);
        panelKanan.setLayout(panelKananLayout);
        panelKananLayout.setHorizontalGroup(
            panelKananLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelKananLayout.createSequentialGroup()
                .addGap(55, 55, 55)
                .addGroup(panelKananLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblUsername)
                    .addComponent(txtUsername)
                    .addComponent(lblPassword)
                    .addComponent(txtPassword)
                    .addComponent(lblRole)
                    .addComponent(cmbRole, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnMasuk, javax.swing.GroupLayout.DEFAULT_SIZE, 385, Short.MAX_VALUE)
                    .addComponent(btnKeluar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(131, Short.MAX_VALUE))
        );
        panelKananLayout.setVerticalGroup(
            panelKananLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelKananLayout.createSequentialGroup()
                .addGap(45, 45, 45)
                .addComponent(lblTitle)
                .addGap(32, 32, 32)
                .addComponent(lblUsername)
                .addGap(6, 6, 6)
                .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(lblPassword)
                .addGap(6, 6, 6)
                .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(lblRole)
                .addGap(6, 6, 6)
                .addComponent(cmbRole, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addComponent(btnMasuk, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(btnKeluar, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(82, Short.MAX_VALUE))
        );

        getContentPane().add(panelKanan, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnMasukActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMasukActionPerformed
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String rolePilihan = cmbRole.getSelectedItem().toString();

        if (username.isEmpty() || username.equals(PLACEHOLDER_USER) || 
            password.isEmpty() || password.equals(PLACEHOLDER_PASS)) {
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
                                "Akun ini terdaftar sebagai Staf Penjualan!\nSilakan pilih Hak Akses: 'Staf Penjualan'.",
                                "Akses Ditolak",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    JOptionPane.showMessageDialog(this,
                            "Login Berhasil! Selamat datang, " + user.getNamaLengkap(),
                            "Sukses",
                            JOptionPane.INFORMATION_MESSAGE);
                    
                    try {
                        FormMenuStaf menuStaf = new FormMenuStaf();
                        menuStaf.setVisible(true);
                        this.dispose();
                    } catch (Throwable t) {
                        t.printStackTrace(); // Menampilkan error baris kode ke Output NetBeans
                        JOptionPane.showMessageDialog(this, 
                            "Gagal membuka Menu Staf: " + t.getMessage(), 
                            "Error Inisialisasi", 
                            JOptionPane.ERROR_MESSAGE);
                    }

                    new FormMenuStaf().setVisible(true);
                    this.dispose();

                // 2. Akun Admin Gudang
                } else if ("admin_gudang".equalsIgnoreCase(user.getRole())) {
                    if (!rolePilihan.equalsIgnoreCase("Admin Gudang")) {
                        JOptionPane.showMessageDialog(this,
                                "Akun ini terdaftar sebagai Admin Gudang!\nSilakan pilih Hak Akses: 'Admin Gudang'.",
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
                            "Peran pengguna tidak dikenali oleh sistem!",
                            "Peran Tidak Valid",
                            JOptionPane.ERROR_MESSAGE);
                }

            } else {
                JOptionPane.showMessageDialog(this,
                        "Username atau Password salah!",
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
    }//GEN-LAST:event_btnMasukActionPerformed

    private void btnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnKeluarActionPerformed
        int confirm = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin keluar dari sistem?",
                "Konfirmasi Keluar",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }//GEN-LAST:event_btnKeluarActionPerformed

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new FormLogin().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnKeluar;
    private javax.swing.JButton btnMasuk;
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