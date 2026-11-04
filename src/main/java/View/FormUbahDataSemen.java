package view;

import Config.Koneksi;
import Model.Pengguna;
import Util.SesiPengguna;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class FormUbahDataSemen extends javax.swing.JFrame {

    private DefaultTableModel modelKatalog;

    public FormUbahDataSemen() {
        initComponents();
        this.setLocationRelativeTo(null);
        rapikanTampilanTambahan();
        pasangLogoSidebar();

        // Kosongkan form input awal
        bersihkanForm();

        tampilkanSesiPengguna();
        loadTabelKatalog();
        desainTabel();

        // Klik baris pada tabel katalog otomatis mengisi form
        tblKatalogSemen.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblKatalogSemen.getSelectedRow();
                if (row != -1) {
                    String idStr = tblKatalogSemen.getValueAt(row, 0).toString();
                    txtIdSemen.setText(idStr);
                    muatDataSemenKeForm(Integer.parseInt(idStr));
                }
            }
        });
    }

    private static String formatRupiah(double nominal) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat df = new DecimalFormat("#,###", symbols);
        return df.format(nominal);
    }

    private void bersihkanForm() {
        txtIdSemen.setText("");
        txtNamaSemen.setText("");
        txtSatuan.setText("");
        txtHargaJual.setText("");
        txtBatasAman.setText("");
    }

    private void tampilkanSesiPengguna() {
        try {
            Pengguna user = SesiPengguna.getPengguna();
            if (user != null) {
                lblRoleText.setText(user.getNamaLengkap());

                String sqlShift = "SELECT shift FROM admin_gudang WHERE id_pengguna = ?";
                try (Connection conn = Koneksi.getKoneksi();
                     PreparedStatement ps = conn.prepareStatement(sqlShift)) {
                    ps.setInt(1, user.getIdPengguna());
                    try (ResultSet rs = ps.executeQuery()) {
                        String shift = rs.next() ? rs.getString("shift") : "Pagi";
                        lblShiftText.setText("Shift Kerja: " + shift);
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private void loadTabelKatalog() {
        String[] kolom = {"ID", "NAMA SEMEN", "HARGA JUAL", "STOK"};
        modelKatalog = new DefaultTableModel(null, kolom) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblKatalogSemen.setModel(modelKatalog);

        String sql = "SELECT id_semen, nama_semen, harga_jual_rp, stok_qty FROM semen ORDER BY id_semen ASC";
        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id_semen");
                String nama = rs.getString("nama_semen");
                double harga = rs.getDouble("harga_jual_rp");
                int stok = rs.getInt("stok_qty");

                modelKatalog.addRow(new Object[]{
                    String.valueOf(id),
                    nama,
                    "Rp " + formatRupiah(harga),
                    formatRupiah(stok)
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat katalog semen: " + ex.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void muatDataSemenKeForm(int idSemen) {
        String sql = "SELECT nama_semen, satuan, harga_jual_rp, batas_aman FROM semen WHERE id_semen = ?";
        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idSemen);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    txtNamaSemen.setText(rs.getString("nama_semen"));
                    txtSatuan.setText(rs.getString("satuan"));
                    txtHargaJual.setText(String.valueOf((long) rs.getDouble("harga_jual_rp")));
                    txtBatasAman.setText(String.valueOf(rs.getInt("batas_aman")));
                } else {
                    JOptionPane.showMessageDialog(this, "Data semen dengan ID #" + idSemen + " tidak ditemukan!", "Tidak Ditemukan", JOptionPane.WARNING_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal mengambil data semen: " + ex.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void rapikanTampilanTambahan() {
        LineBorder borderCard = new LineBorder(new Color(226, 232, 240), 1, true);
        panelCardForm.setBorder(borderCard);
        panelCardTabel.setBorder(borderCard);

        LineBorder borderField = new LineBorder(new Color(226, 232, 240), 1, true);
        EmptyBorder paddingField = new EmptyBorder(0, 10, 0, 10);

        txtIdSemen.setBorder(BorderFactory.createCompoundBorder(borderField, paddingField));
        txtNamaSemen.setBorder(BorderFactory.createCompoundBorder(borderField, paddingField));
        txtSatuan.setBorder(BorderFactory.createCompoundBorder(borderField, paddingField));
        txtHargaJual.setBorder(BorderFactory.createCompoundBorder(borderField, paddingField));
        txtBatasAman.setBorder(BorderFactory.createCompoundBorder(borderField, paddingField));
    }

    private void pasangLogoSidebar() {
        try {
            BufferedImage imgAsli = null;
            String[] jalur = {
                "src/main/java/view/logo_slate.png.jpg",
                "src/main/java/view/logo_slate.png",
                "src/main/java/view/logonew.jpg",
                "D:/Semester_3/PA CAPSTONE/Apaan/src/main/java/view/logo_slate.png.jpg"
            };

            for (String j : jalur) {
                File f = new File(j);
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
                int rgbPojok = imgAsli.getRGB(0, 0);
                Color warnaAsliLogo = new Color((rgbPojok >> 16) & 0xFF, (rgbPojok >> 8) & 0xFF, rgbPojok & 0xFF);
                panelSidebar.setBackground(warnaAsliLogo);

                Image imgResized = imgAsli.getScaledInstance(75, 75, Image.SCALE_SMOOTH);
                lblLogoSidebar.setIcon(new ImageIcon(imgResized));
                lblLogoSidebar.setText("");
            } else {
                lblLogoSidebar.setText("<html><center><span style='color:#FFFFFF; font-size:20px; font-weight:bold;'>SL<span style='color:#EA580C;'>A</span>TE</span></center></html>");
            }
        } catch (Exception e) {
            lblLogoSidebar.setText("<html><center><h2 style='color:#FFFFFF;'>SLATE</h2></center></html>");
        }
    }

    private void desainTabel() {
        JTableHeader header = tblKatalogSemen.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(new Color(71, 85, 105));
        header.setPreferredSize(new Dimension(header.getWidth(), 38));

        tblKatalogSemen.getColumnModel().getColumn(0).setPreferredWidth(45);
        tblKatalogSemen.getColumnModel().getColumn(1).setPreferredWidth(160);
        tblKatalogSemen.getColumnModel().getColumn(2).setPreferredWidth(110);
        tblKatalogSemen.getColumnModel().getColumn(3).setPreferredWidth(75);

        // Format kolom ID
        tblKatalogSemen.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                return c;
            }
        });

        // Format kolom NAMA SEMEN
        tblKatalogSemen.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                setForeground(new Color(15, 23, 42));
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
                return c;
            }
        });

        // Format kolom HARGA JUAL (Hitam polos)
        tblKatalogSemen.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                setForeground(new Color(15, 23, 42));
                return c;
            }
        });

        // Format kolom STOK (Hitam polos)
        tblKatalogSemen.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                setForeground(new Color(15, 23, 42));
                setHorizontalAlignment(SwingConstants.RIGHT);
                setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));
                return c;
            }
        });
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelSidebar = new javax.swing.JPanel();
        lblLogoSidebar = new javax.swing.JLabel();
        panelBadgeUser = new javax.swing.JPanel();
        lblDotStatus = new javax.swing.JLabel();
        lblRoleText = new javax.swing.JLabel();
        lblShiftText = new javax.swing.JLabel();
        btnNavMenu = new javax.swing.JButton();
        btnNavTambahStok = new javax.swing.JButton();
        btnNavUbahData = new javax.swing.JButton();
        btnNavKelolaSemen = new javax.swing.JButton();
        btnLogout = new javax.swing.JButton();
        panelKonten = new javax.swing.JPanel();
        lblHeaderTitle = new javax.swing.JLabel();
        panelCardForm = new javax.swing.JPanel();
        lblFormTitle = new javax.swing.JLabel();
        lblIdSemen = new javax.swing.JLabel();
        txtIdSemen = new javax.swing.JTextField();
        btnCekId = new javax.swing.JButton();
        lblNamaSemen = new javax.swing.JLabel();
        txtNamaSemen = new javax.swing.JTextField();
        lblSatuan = new javax.swing.JLabel();
        txtSatuan = new javax.swing.JTextField();
        lblHargaJual = new javax.swing.JLabel();
        txtHargaJual = new javax.swing.JTextField();
        lblBatasAman = new javax.swing.JLabel();
        txtBatasAman = new javax.swing.JTextField();
        btnSimpanPerubahan = new javax.swing.JButton();
        panelCardTabel = new javax.swing.JPanel();
        lblTabelTitle = new javax.swing.JLabel();
        jScrollPaneTabel = new javax.swing.JScrollPane();
        tblKatalogSemen = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("SLATE - Ubah Data Semen");
        setResizable(false);

        panelSidebar.setBackground(new java.awt.Color(22, 27, 34));
        panelSidebar.setPreferredSize(new java.awt.Dimension(240, 680));

        lblLogoSidebar.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        panelBadgeUser.setBackground(new java.awt.Color(33, 39, 50));

        lblDotStatus.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblDotStatus.setForeground(new java.awt.Color(32, 128, 255));
        lblDotStatus.setText("●");

        lblRoleText.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblRoleText.setForeground(new java.awt.Color(248, 248, 248));
        lblRoleText.setText("Admin Gudang");

        lblShiftText.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblShiftText.setForeground(new java.awt.Color(134, 148, 160));
        lblShiftText.setText("Shift Kerja: Pagi");

        javax.swing.GroupLayout panelBadgeUserLayout = new javax.swing.GroupLayout(panelBadgeUser);
        panelBadgeUser.setLayout(panelBadgeUserLayout);
        panelBadgeUserLayout.setHorizontalGroup(
            panelBadgeUserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBadgeUserLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(lblDotStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addGroup(panelBadgeUserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblRoleText, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblShiftText, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelBadgeUserLayout.setVerticalGroup(
            panelBadgeUserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBadgeUserLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(panelBadgeUserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDotStatus)
                    .addComponent(lblRoleText))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblShiftText)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnNavMenu.setBackground(new java.awt.Color(22, 27, 34));
        btnNavMenu.setForeground(new java.awt.Color(134, 148, 160));
        btnNavMenu.setText("Menu Admin Gudang");
        btnNavMenu.setBorderPainted(false);
        btnNavMenu.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNavMenu.setFocusPainted(false);
        btnNavMenu.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavMenu.setMargin(new java.awt.Insets(2, 18, 2, 14));
        btnNavMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNavMenuActionPerformed(evt);
            }
        });

        btnNavTambahStok.setBackground(new java.awt.Color(22, 27, 34));
        btnNavTambahStok.setForeground(new java.awt.Color(134, 148, 160));
        btnNavTambahStok.setText("Tambah Stok Semen");
        btnNavTambahStok.setBorderPainted(false);
        btnNavTambahStok.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNavTambahStok.setFocusPainted(false);
        btnNavTambahStok.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavTambahStok.setMargin(new java.awt.Insets(2, 18, 2, 14));
        btnNavTambahStok.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNavTambahStokActionPerformed(evt);
            }
        });

        btnNavUbahData.setBackground(new java.awt.Color(234, 88, 12));
        btnNavUbahData.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnNavUbahData.setForeground(new java.awt.Color(255, 255, 255));
        btnNavUbahData.setText("Ubah Data Semen");
        btnNavUbahData.setBorderPainted(false);
        btnNavUbahData.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNavUbahData.setFocusPainted(false);
        btnNavUbahData.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavUbahData.setMargin(new java.awt.Insets(2, 18, 2, 14));

        btnNavKelolaSemen.setBackground(new java.awt.Color(22, 27, 34));
        btnNavKelolaSemen.setForeground(new java.awt.Color(134, 148, 160));
        btnNavKelolaSemen.setText("Tambah/Hapus Semen");
        btnNavKelolaSemen.setBorderPainted(false);
        btnNavKelolaSemen.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNavKelolaSemen.setFocusPainted(false);
        btnNavKelolaSemen.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavKelolaSemen.setMargin(new java.awt.Insets(2, 18, 2, 14));
        btnNavKelolaSemen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNavKelolaSemenActionPerformed(evt);
            }
        });

        btnLogout.setBackground(new java.awt.Color(33, 39, 50));
        btnLogout.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnLogout.setForeground(new java.awt.Color(239, 68, 68));
        btnLogout.setText("Logout");
        btnLogout.setBorderPainted(false);
        btnLogout.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogoutActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelSidebarLayout = new javax.swing.GroupLayout(panelSidebar);
        panelSidebar.setLayout(panelSidebarLayout);
        panelSidebarLayout.setHorizontalGroup(
            panelSidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSidebarLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(panelSidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblLogoSidebar, javax.swing.GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE)
                    .addComponent(panelBadgeUser, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnNavMenu, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnNavTambahStok, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnNavUbahData, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnNavKelolaSemen, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnLogout, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(15, Short.MAX_VALUE))
        );
        panelSidebarLayout.setVerticalGroup(
            panelSidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSidebarLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(lblLogoSidebar, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15)
                .addComponent(panelBadgeUser, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25)
                .addComponent(btnNavMenu, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(btnNavTambahStok, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(btnNavUbahData, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(btnNavKelolaSemen, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 204, Short.MAX_VALUE)
                .addComponent(btnLogout, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );

        getContentPane().add(panelSidebar, java.awt.BorderLayout.WEST);

        panelKonten.setBackground(new java.awt.Color(248, 250, 252));

        lblHeaderTitle.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblHeaderTitle.setForeground(new java.awt.Color(15, 15, 23));
        lblHeaderTitle.setText("Ubah Data Semen");

        panelCardForm.setBackground(new java.awt.Color(255, 255, 255));

        lblFormTitle.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblFormTitle.setForeground(new java.awt.Color(15, 15, 23));
        lblFormTitle.setText("Form Ubah Harga dan Detail Semen");

        lblIdSemen.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblIdSemen.setForeground(new java.awt.Color(71, 85, 105));
        lblIdSemen.setText("ID Semen yang Ingin Diubah");

        btnCekId.setBackground(new java.awt.Color(30, 37, 43));
        btnCekId.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnCekId.setForeground(new java.awt.Color(255, 255, 255));
        btnCekId.setText("Cek ID");
        btnCekId.setBorderPainted(false);
        btnCekId.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCekId.setFocusPainted(false);
        btnCekId.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCekIdActionPerformed(evt);
            }
        });

        lblNamaSemen.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblNamaSemen.setForeground(new java.awt.Color(71, 85, 105));
        lblNamaSemen.setText("Nama / Varian Semen");

        lblSatuan.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblSatuan.setForeground(new java.awt.Color(71, 85, 105));
        lblSatuan.setText("Satuan (Sak/Ton)");

        lblHargaJual.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblHargaJual.setForeground(new java.awt.Color(71, 85, 105));
        lblHargaJual.setText("Harga Jual Baru (Rp)");

        lblBatasAman.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblBatasAman.setForeground(new java.awt.Color(71, 85, 105));
        lblBatasAman.setText("Batas Aman");

        btnSimpanPerubahan.setBackground(new java.awt.Color(234, 88, 12));
        btnSimpanPerubahan.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnSimpanPerubahan.setForeground(new java.awt.Color(255, 255, 255));
        btnSimpanPerubahan.setText("SIMPAN PERUBAHAN DATA");
        btnSimpanPerubahan.setBorderPainted(false);
        btnSimpanPerubahan.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnSimpanPerubahan.setFocusPainted(false);
        btnSimpanPerubahan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSimpanPerubahanActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelCardFormLayout = new javax.swing.GroupLayout(panelCardForm);
        panelCardForm.setLayout(panelCardFormLayout);
        panelCardFormLayout.setHorizontalGroup(
            panelCardFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCardFormLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(panelCardFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblFormTitle)
                    .addComponent(lblIdSemen)
                    .addGroup(panelCardFormLayout.createSequentialGroup()
                        .addComponent(txtIdSemen, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(btnCekId, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))
                    .addComponent(lblNamaSemen)
                    .addComponent(txtNamaSemen)
                    .addComponent(lblSatuan)
                    .addComponent(txtSatuan)
                    .addComponent(lblHargaJual)
                    .addComponent(txtHargaJual)
                    .addComponent(lblBatasAman)
                    .addComponent(txtBatasAman)
                    .addComponent(btnSimpanPerubahan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        panelCardFormLayout.setVerticalGroup(
            panelCardFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCardFormLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(lblFormTitle)
                .addGap(15, 15, 15)
                .addComponent(lblIdSemen)
                .addGap(5, 5, 5)
                .addGroup(panelCardFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtIdSemen, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCekId, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addComponent(lblNamaSemen)
                .addGap(5, 5, 5)
                .addComponent(txtNamaSemen, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(lblSatuan)
                .addGap(5, 5, 5)
                .addComponent(txtSatuan, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(lblHargaJual)
                .addGap(5, 5, 5)
                .addComponent(txtHargaJual, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(lblBatasAman)
                .addGap(5, 5, 5)
                .addComponent(txtBatasAman, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(35, 35, 35)
                .addComponent(btnSimpanPerubahan, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(40, Short.MAX_VALUE))
        );

        panelCardTabel.setBackground(new java.awt.Color(255, 255, 255));

        lblTabelTitle.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblTabelTitle.setForeground(new java.awt.Color(15, 15, 23));
        lblTabelTitle.setText("Katalog Semen");

        jScrollPaneTabel.setBorder(null);

        tblKatalogSemen.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "NAMA SEMEN", "HARGA JUAL", "STOK"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblKatalogSemen.setRowHeight(48);
        tblKatalogSemen.setSelectionBackground(new java.awt.Color(241, 245, 249));
        tblKatalogSemen.setSelectionForeground(new java.awt.Color(15, 23, 42));
        tblKatalogSemen.setShowGrid(false);
        jScrollPaneTabel.setViewportView(tblKatalogSemen);

        javax.swing.GroupLayout panelCardTabelLayout = new javax.swing.GroupLayout(panelCardTabel);
        panelCardTabel.setLayout(panelCardTabelLayout);
        panelCardTabelLayout.setHorizontalGroup(
            panelCardTabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCardTabelLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(panelCardTabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTabelTitle)
                    .addComponent(jScrollPaneTabel, javax.swing.GroupLayout.DEFAULT_SIZE, 358, Short.MAX_VALUE))
                .addGap(20, 20, 20))
        );
        panelCardTabelLayout.setVerticalGroup(
            panelCardTabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCardTabelLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(lblTabelTitle)
                .addGap(14, 14, 14)
                .addComponent(jScrollPaneTabel, javax.swing.GroupLayout.DEFAULT_SIZE, 481, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );

        javax.swing.GroupLayout panelKontenLayout = new javax.swing.GroupLayout(panelKonten);
        panelKonten.setLayout(panelKontenLayout);
        panelKontenLayout.setHorizontalGroup(
            panelKontenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelKontenLayout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addGroup(panelKontenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblHeaderTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 450, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelKontenLayout.createSequentialGroup()
                        .addComponent(panelCardForm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20)
                        .addComponent(panelCardTabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelKontenLayout.setVerticalGroup(
            panelKontenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelKontenLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(lblHeaderTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15)
                .addGroup(panelKontenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(panelCardForm, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelCardTabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getContentPane().add(panelKonten, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnNavMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavMenuActionPerformed
        new FormMenuAdminGudang().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnNavMenuActionPerformed

    private void btnNavTambahStokActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavTambahStokActionPerformed
        new FormTambahStokSemen().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnNavTambahStokActionPerformed

    private void btnNavKelolaSemenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavKelolaSemenActionPerformed
        new FormKelolaSemen().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnNavKelolaSemenActionPerformed

    private void btnCekIdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCekIdActionPerformed
        String idStr = txtIdSemen.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Masukkan ID Semen yang ingin dicari!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            muatDataSemenKeForm(id);

            // Sorot baris pada tabel jika ada
            for (int i = 0; i < tblKatalogSemen.getRowCount(); i++) {
                if (tblKatalogSemen.getValueAt(i, 0).toString().equals(idStr)) {
                    tblKatalogSemen.setRowSelectionInterval(i, i);
                    break;
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "ID Semen harus berupa angka!", "Format Salah", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnCekIdActionPerformed

    private void btnSimpanPerubahanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanPerubahanActionPerformed
        String idStr = txtIdSemen.getText().trim();
        String nama = txtNamaSemen.getText().trim();
        String satuan = txtSatuan.getText().trim();
        String hargaStr = txtHargaJual.getText().trim();
        String batasStr = txtBatasAman.getText().trim();

        if (idStr.isEmpty() || nama.isEmpty() || satuan.isEmpty() || hargaStr.isEmpty() || batasStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field formulir harus diisi!", "Validasi Form", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id;
        double harga;
        int batas;

        try {
            id = Integer.parseInt(idStr);
            harga = Double.parseDouble(hargaStr);
            batas = Integer.parseInt(batasStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Harga Jual dan Batas Aman harus berupa angka valid!", "Format Salah", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (harga <= 0 || batas < 0) {
            JOptionPane.showMessageDialog(this, "Nominal harga harus > 0 dan batas aman >= 0!", "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "UPDATE semen SET nama_semen = ?, satuan = ?, harga_jual_rp = ?, batas_aman = ? WHERE id_semen = ?";
        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nama);
            ps.setString(2, satuan);
            ps.setDouble(3, harga);
            ps.setInt(4, batas);
            ps.setInt(5, id);

            int affected = ps.executeUpdate();
            if (affected > 0) {
                JOptionPane.showMessageDialog(this, "Data semen [ID: " + id + "] berhasil diperbarui!", "Pembaruan Berhasil", JOptionPane.INFORMATION_MESSAGE);
                loadTabelKatalog();
                bersihkanForm();
            } else {
                JOptionPane.showMessageDialog(this, "ID Semen #" + id + " tidak ditemukan di database!", "Gagal Simpan", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal memperbarui data semen: " + ex.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnSimpanPerubahanActionPerformed

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
        int confirm = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin keluar?",
                "Konfirmasi Logout",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            SesiPengguna.setPengguna(null);
            new FormLogin().setVisible(true);
            this.dispose();
        }
    }//GEN-LAST:event_btnLogoutActionPerformed

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new FormUbahDataSemen().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCekId;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnNavKelolaSemen;
    private javax.swing.JButton btnNavMenu;
    private javax.swing.JButton btnNavTambahStok;
    private javax.swing.JButton btnNavUbahData;
    private javax.swing.JButton btnSimpanPerubahan;
    private javax.swing.JScrollPane jScrollPaneTabel;
    private javax.swing.JLabel lblBatasAman;
    private javax.swing.JLabel lblDotStatus;
    private javax.swing.JLabel lblFormTitle;
    private javax.swing.JLabel lblHargaJual;
    private javax.swing.JLabel lblHeaderTitle;
    private javax.swing.JLabel lblIdSemen;
    private javax.swing.JLabel lblLogoSidebar;
    private javax.swing.JLabel lblNamaSemen;
    private javax.swing.JLabel lblRoleText;
    private javax.swing.JLabel lblSatuan;
    private javax.swing.JLabel lblShiftText;
    private javax.swing.JLabel lblTabelTitle;
    private javax.swing.JPanel panelBadgeUser;
    private javax.swing.JPanel panelCardForm;
    private javax.swing.JPanel panelCardTabel;
    private javax.swing.JPanel panelKonten;
    private javax.swing.JPanel panelSidebar;
    private javax.swing.JTable tblKatalogSemen;
    private javax.swing.JTextField txtBatasAman;
    private javax.swing.JTextField txtHargaJual;
    private javax.swing.JTextField txtIdSemen;
    private javax.swing.JTextField txtNamaSemen;
    private javax.swing.JTextField txtSatuan;
    // End of variables declaration//GEN-END:variables
}