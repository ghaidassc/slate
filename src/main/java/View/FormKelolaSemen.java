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
import java.sql.Statement;
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

public class FormKelolaSemen extends javax.swing.JFrame {

    private DefaultTableModel modelKatalog;

    public FormKelolaSemen() {
        initComponents();
        this.setLocationRelativeTo(null);
        rapikanTampilanTambahan();
        pasangLogoSidebar();

        bersihkanFormTambah();
        txtIdHapus.setText("");

        tampilkanSesiPengguna();
        loadTabelKatalogMaster();
        desainTabel();

        // Klik baris pada tabel katalog otomatis mengisi field ID Hapus
        tblKatalogMaster.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblKatalogMaster.getSelectedRow();
                if (row != -1) {
                    txtIdHapus.setText(tblKatalogMaster.getValueAt(row, 0).toString());
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

    private void bersihkanFormTambah() {
        txtNamaSemen.setText("");
        txtSatuan.setText("");
        txtStokAwal.setText("");
        txtHargaJual.setText("");
        txtBatasAman.setText("");
    }

    private void tampilkanSesiPengguna() {
    try {
        Pengguna user = SesiPengguna.getPengguna();
        if (user instanceof Model.AdminGudang) {
            Model.AdminGudang admin = (Model.AdminGudang) user;
            lblRoleText.setText(admin.getNamaLengkap());
            lblShiftText.setText("Shift Kerja: " + admin.getShift());
        }
    } catch (Exception ignored) {}
}

    private void loadTabelKatalogMaster() {
        String[] kolom = {"ID", "NAMA SEMEN", "HARGA JUAL", "STOK"};
        modelKatalog = new DefaultTableModel(null, kolom) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblKatalogMaster.setModel(modelKatalog);

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
            JOptionPane.showMessageDialog(this, "Gagal memuat katalog master semen: " + ex.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void rapikanTampilanTambahan() {
        LineBorder borderCard = new LineBorder(new Color(226, 232, 240), 1, true);
        panelCardForm.setBorder(borderCard);
        panelCardTabel.setBorder(borderCard);
        panelBoxHapus.setBorder(new LineBorder(new Color(254, 226, 226), 1, true));

        EmptyBorder paddingField = new EmptyBorder(0, 10, 0, 10);
        txtNamaSemen.setBorder(BorderFactory.createCompoundBorder(borderCard, paddingField));
        txtSatuan.setBorder(BorderFactory.createCompoundBorder(borderCard, paddingField));
        txtStokAwal.setBorder(BorderFactory.createCompoundBorder(borderCard, paddingField));
        txtHargaJual.setBorder(BorderFactory.createCompoundBorder(borderCard, paddingField));
        txtBatasAman.setBorder(BorderFactory.createCompoundBorder(borderCard, paddingField));
        txtIdHapus.setBorder(BorderFactory.createCompoundBorder(borderCard, paddingField));
    }

    private void pasangLogoSidebar() {
        try {
            BufferedImage imgAsli = null;
            String[] jalur = {
                "src/main/java/view/logo_slate.png.jpg",
                "src/main/java/view/logo_slate.png",
                "src/main/java/view/logonew.jpg"
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
        JTableHeader header = tblKatalogMaster.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(new Color(71, 85, 105));
        header.setPreferredSize(new Dimension(header.getWidth(), 38));

        tblKatalogMaster.getColumnModel().getColumn(0).setPreferredWidth(45);
        tblKatalogMaster.getColumnModel().getColumn(1).setPreferredWidth(160);
        tblKatalogMaster.getColumnModel().getColumn(2).setPreferredWidth(110);
        tblKatalogMaster.getColumnModel().getColumn(3).setPreferredWidth(75);

        // Kolom ID
        tblKatalogMaster.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                return c;
            }
        });

        // Kolom NAMA SEMEN
        tblKatalogMaster.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                setForeground(new Color(15, 23, 42));
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
                return c;
            }
        });

        // Kolom HARGA JUAL
        tblKatalogMaster.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                setForeground(new Color(15, 23, 42));
                return c;
            }
        });

        // Kolom STOK
        tblKatalogMaster.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
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
        btnTabTambah = new javax.swing.JButton();
        btnTabHapus = new javax.swing.JButton();
        lblNamaSemen = new javax.swing.JLabel();
        txtNamaSemen = new javax.swing.JTextField();
        lblSatuan = new javax.swing.JLabel();
        txtSatuan = new javax.swing.JTextField();
        lblStokAwal = new javax.swing.JLabel();
        txtStokAwal = new javax.swing.JTextField();
        lblHargaJual = new javax.swing.JLabel();
        txtHargaJual = new javax.swing.JTextField();
        lblBatasAman = new javax.swing.JLabel();
        txtBatasAman = new javax.swing.JTextField();
        panelBoxHapus = new javax.swing.JPanel();
        lblHapusHeader = new javax.swing.JLabel();
        txtIdHapus = new javax.swing.JTextField();
        btnHapus = new javax.swing.JButton();
        btnSimpanTambah = new javax.swing.JButton();
        panelCardTabel = new javax.swing.JPanel();
        lblTabelTitle = new javax.swing.JLabel();
        jScrollPaneTabel = new javax.swing.JScrollPane();
        tblKatalogMaster = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("SLATE - Tambah / Hapus Semen");
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

        btnNavUbahData.setBackground(new java.awt.Color(22, 27, 34));
        btnNavUbahData.setForeground(new java.awt.Color(134, 148, 160));
        btnNavUbahData.setText("Ubah Data Semen");
        btnNavUbahData.setBorderPainted(false);
        btnNavUbahData.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNavUbahData.setFocusPainted(false);
        btnNavUbahData.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavUbahData.setMargin(new java.awt.Insets(2, 18, 2, 14));
        btnNavUbahData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNavUbahDataActionPerformed(evt);
            }
        });

        btnNavKelolaSemen.setBackground(new java.awt.Color(234, 88, 12));
        btnNavKelolaSemen.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnNavKelolaSemen.setForeground(new java.awt.Color(255, 255, 255));
        btnNavKelolaSemen.setText("Tambah/Hapus Semen");
        btnNavKelolaSemen.setBorderPainted(false);
        btnNavKelolaSemen.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNavKelolaSemen.setFocusPainted(false);
        btnNavKelolaSemen.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavKelolaSemen.setMargin(new java.awt.Insets(2, 18, 2, 14));

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
        lblHeaderTitle.setText("Tambah / Hapus Semen");

        panelCardForm.setBackground(new java.awt.Color(255, 255, 255));

        btnTabTambah.setBackground(new java.awt.Color(234, 88, 12));
        btnTabTambah.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnTabTambah.setForeground(new java.awt.Color(255, 255, 255));
        btnTabTambah.setText("1. Tambah Semen");
        btnTabTambah.setBorderPainted(false);
        btnTabTambah.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnTabTambah.setFocusPainted(false);
        btnTabTambah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTabTambahActionPerformed(evt);
            }
        });

        btnTabHapus.setBackground(new java.awt.Color(235, 241, 248));
        btnTabHapus.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnTabHapus.setForeground(new java.awt.Color(71, 85, 105));
        btnTabHapus.setText("2. Hapus Semen");
        btnTabHapus.setBorderPainted(false);
        btnTabHapus.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnTabHapus.setFocusPainted(false);
        btnTabHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTabHapusActionPerformed(evt);
            }
        });

        lblNamaSemen.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblNamaSemen.setForeground(new java.awt.Color(71, 85, 105));
        lblNamaSemen.setText("Nama / Varian Semen Baru");

        lblSatuan.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblSatuan.setForeground(new java.awt.Color(71, 85, 105));
        lblSatuan.setText("Satuan");

        lblStokAwal.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblStokAwal.setForeground(new java.awt.Color(71, 85, 105));
        lblStokAwal.setText("Stok Awal Fisik");

        lblHargaJual.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblHargaJual.setForeground(new java.awt.Color(71, 85, 105));
        lblHargaJual.setText("Harga Jual per Satuan (Rp)");

        lblBatasAman.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblBatasAman.setForeground(new java.awt.Color(71, 85, 105));
        lblBatasAman.setText("Batas Aman Peringatan");

        panelBoxHapus.setBackground(new java.awt.Color(248, 250, 252));

        lblHapusHeader.setFont(new java.awt.Font("Segoe UI", 1, 9)); // NOI18N
        lblHapusHeader.setForeground(new java.awt.Color(220, 28, 28));
        lblHapusHeader.setText("ID Semen yang Ingin Dihapus");

        btnHapus.setBackground(new java.awt.Color(254, 226, 226));
        btnHapus.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnHapus.setForeground(new java.awt.Color(185, 28, 28));
        btnHapus.setText("Hapus");
        btnHapus.setBorderPainted(false);
        btnHapus.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnHapus.setFocusPainted(false);
        btnHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHapusActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelBoxHapusLayout = new javax.swing.GroupLayout(panelBoxHapus);
        panelBoxHapus.setLayout(panelBoxHapusLayout);
        panelBoxHapusLayout.setHorizontalGroup(
            panelBoxHapusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBoxHapusLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(panelBoxHapusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblHapusHeader)
                    .addGroup(panelBoxHapusLayout.createSequentialGroup()
                        .addComponent(txtIdHapus, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addComponent(btnHapus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(12, 12, 12))
        );
        panelBoxHapusLayout.setVerticalGroup(
            panelBoxHapusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBoxHapusLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(lblHapusHeader)
                .addGap(6, 6, 6)
                .addGroup(panelBoxHapusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtIdHapus, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnHapus, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(10, Short.MAX_VALUE))
        );

        btnSimpanTambah.setBackground(new java.awt.Color(234, 88, 12));
        btnSimpanTambah.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnSimpanTambah.setForeground(new java.awt.Color(255, 255, 255));
        btnSimpanTambah.setText("+ TAMBAH SEMEN KE DATABASE");
        btnSimpanTambah.setBorderPainted(false);
        btnSimpanTambah.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnSimpanTambah.setFocusPainted(false);
        btnSimpanTambah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSimpanTambahActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelCardFormLayout = new javax.swing.GroupLayout(panelCardForm);
        panelCardForm.setLayout(panelCardFormLayout);
        panelCardFormLayout.setHorizontalGroup(
            panelCardFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCardFormLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(panelCardFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(panelCardFormLayout.createSequentialGroup()
                        .addComponent(btnTabTambah, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(btnTabHapus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(lblNamaSemen)
                    .addComponent(txtNamaSemen)
                    .addGroup(panelCardFormLayout.createSequentialGroup()
                        .addGroup(panelCardFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblSatuan)
                            .addComponent(txtSatuan, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10)
                        .addGroup(panelCardFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblStokAwal)
                            .addComponent(txtStokAwal)))
                    .addComponent(lblHargaJual)
                    .addComponent(txtHargaJual)
                    .addComponent(lblBatasAman)
                    .addComponent(txtBatasAman)
                    .addComponent(panelBoxHapus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnSimpanTambah, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        panelCardFormLayout.setVerticalGroup(
            panelCardFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCardFormLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(panelCardFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnTabTambah, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnTabHapus, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15)
                .addComponent(lblNamaSemen)
                .addGap(5, 5, 5)
                .addComponent(txtNamaSemen, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addGroup(panelCardFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSatuan)
                    .addComponent(lblStokAwal))
                .addGap(5, 5, 5)
                .addGroup(panelCardFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtSatuan, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtStokAwal, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addComponent(lblHargaJual)
                .addGap(5, 5, 5)
                .addComponent(txtHargaJual, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(lblBatasAman)
                .addGap(5, 5, 5)
                .addComponent(txtBatasAman, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(panelBoxHapus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(btnSimpanTambah, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(25, Short.MAX_VALUE))
        );

        panelCardTabel.setBackground(new java.awt.Color(255, 255, 255));

        lblTabelTitle.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblTabelTitle.setForeground(new java.awt.Color(15, 15, 23));
        lblTabelTitle.setText("Katalog Master Semen Terdaftar");

        jScrollPaneTabel.setBorder(null);

        tblKatalogMaster.setModel(new javax.swing.table.DefaultTableModel(
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
        tblKatalogMaster.setRowHeight(48);
        tblKatalogMaster.setSelectionBackground(new java.awt.Color(241, 245, 249));
        tblKatalogMaster.setSelectionForeground(new java.awt.Color(15, 23, 42));
        tblKatalogMaster.setShowGrid(false);
        jScrollPaneTabel.setViewportView(tblKatalogMaster);

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

    private void btnNavUbahDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavUbahDataActionPerformed
        new FormUbahDataSemen().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnNavUbahDataActionPerformed

    private void btnTabTambahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTabTambahActionPerformed
        btnTabTambah.setBackground(new Color(234, 88, 12));
        btnTabTambah.setForeground(Color.WHITE);
        btnTabHapus.setBackground(new Color(235, 241, 248));
        btnTabHapus.setForeground(new Color(71, 85, 105));
        txtNamaSemen.requestFocus();
    }//GEN-LAST:event_btnTabTambahActionPerformed

    private void btnTabHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTabHapusActionPerformed
        btnTabHapus.setBackground(new Color(234, 88, 12));
        btnTabHapus.setForeground(Color.WHITE);
        btnTabTambah.setBackground(new Color(235, 241, 248));
        btnTabTambah.setForeground(new Color(71, 85, 105));
        txtIdHapus.requestFocus();
    }//GEN-LAST:event_btnTabHapusActionPerformed

    private void btnSimpanTambahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanTambahActionPerformed
        String nama = txtNamaSemen.getText().trim();
        String satuan = txtSatuan.getText().trim();
        String stokStr = txtStokAwal.getText().trim();
        String hargaStr = txtHargaJual.getText().trim();
        String batasStr = txtBatasAman.getText().trim();

        if (nama.isEmpty() || satuan.isEmpty() || stokStr.isEmpty() || hargaStr.isEmpty() || batasStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua isian penambahan semen harus diisi!", "Validasi Formulir", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int stok;
        double harga;
        int batas;

        try {
            stok = Integer.parseInt(stokStr);
            harga = Double.parseDouble(hargaStr);
            batas = Integer.parseInt(batasStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Stok, Harga Jual, dan Batas Aman harus berupa angka valid!", "Format Salah", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (stok < 0 || harga <= 0 || batas < 0) {
            JOptionPane.showMessageDialog(this, "Harga harus > 0, stok dan batas aman minimal 0!", "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Generate ID baru (MAX + 1)
        int newId = 1;
        String sqlMax = "SELECT COALESCE(MAX(id_semen), 0) + 1 AS next_id FROM semen";
        String sqlInsert = "INSERT INTO semen (id_semen, nama_semen, satuan, harga_jual_rp, stok_qty, batas_aman) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = Koneksi.getKoneksi()) {
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sqlMax)) {
                if (rs.next()) {
                    newId = rs.getInt("next_id");
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
                ps.setInt(1, newId);
                ps.setString(2, nama);
                ps.setString(3, satuan);
                ps.setDouble(4, harga);
                ps.setInt(5, stok);
                ps.setInt(6, batas);
                ps.executeUpdate();
            }

            JOptionPane.showMessageDialog(this,
                    "Varian semen baru [ID: " + newId + "] " + nama + " berhasil didaftarkan!",
                    "Penambahan Berhasil",
                    JOptionPane.INFORMATION_MESSAGE);

            bersihkanFormTambah();
            loadTabelKatalogMaster();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal menambahkan varian semen: " + ex.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnSimpanTambahActionPerformed

    private void btnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHapusActionPerformed
        String idStr = txtIdHapus.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Masukkan ID Semen yang ingin dihapus!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "ID Semen harus berupa angka!", "Format Salah", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Konfirmasi Validasi (y/n)
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Apakah Anda yakin ingin menghapus data semen ID #" + id + "?\nPerhatian: Data yang terhapus tidak dapat dipulihkan.",
                "Konfirmasi Hapus Data (y/n)",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String sqlDelete = "DELETE FROM semen WHERE id_semen = ?";
        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sqlDelete)) {
            ps.setInt(1, id);
            int affected = ps.executeUpdate();

            if (affected > 0) {
                JOptionPane.showMessageDialog(this, "Data semen ID #" + id + " berhasil dihapus dari database!", "Berhasil", JOptionPane.INFORMATION_MESSAGE);
                txtIdHapus.setText("");
                loadTabelKatalogMaster();
            } else {
                JOptionPane.showMessageDialog(this, "ID Semen #" + id + " tidak ditemukan di katalog!", "Gagal Hapus", JOptionPane.WARNING_MESSAGE);
            }

        } catch (SQLException ex) {
            // Tangani proteksi foreign key jika semen sudah memiliki relasi pesanan/stok masuk
            if (ex.getMessage().toLowerCase().contains("foreign key") || ex.getErrorCode() == 1451) {
                JOptionPane.showMessageDialog(this,
                        "Semen ini tidak dapat dihapus karena sudah memiliki riwayat transaksi/pesanan sebelumnya!\n" +
                        "Untuk menjaga integritas laporan, ubah stok menjadi 0 di menu 'Ubah Data Semen'.",
                        "Proteksi Integritas Relasi",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menghapus semen: " + ex.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnHapusActionPerformed

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
            new FormKelolaSemen().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnHapus;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnNavKelolaSemen;
    private javax.swing.JButton btnNavMenu;
    private javax.swing.JButton btnNavTambahStok;
    private javax.swing.JButton btnNavUbahData;
    private javax.swing.JButton btnSimpanTambah;
    private javax.swing.JButton btnTabHapus;
    private javax.swing.JButton btnTabTambah;
    private javax.swing.JScrollPane jScrollPaneTabel;
    private javax.swing.JLabel lblBatasAman;
    private javax.swing.JLabel lblDotStatus;
    private javax.swing.JLabel lblHapusHeader;
    private javax.swing.JLabel lblHargaJual;
    private javax.swing.JLabel lblHeaderTitle;
    private javax.swing.JLabel lblLogoSidebar;
    private javax.swing.JLabel lblNamaSemen;
    private javax.swing.JLabel lblRoleText;
    private javax.swing.JLabel lblSatuan;
    private javax.swing.JLabel lblShiftText;
    private javax.swing.JLabel lblStokAwal;
    private javax.swing.JLabel lblTabelTitle;
    private javax.swing.JPanel panelBadgeUser;
    private javax.swing.JPanel panelBoxHapus;
    private javax.swing.JPanel panelCardForm;
    private javax.swing.JPanel panelCardTabel;
    private javax.swing.JPanel panelKonten;
    private javax.swing.JPanel panelSidebar;
    private javax.swing.JTable tblKatalogMaster;
    private javax.swing.JTextField txtBatasAman;
    private javax.swing.JTextField txtHargaJual;
    private javax.swing.JTextField txtIdHapus;
    private javax.swing.JTextField txtNamaSemen;
    private javax.swing.JTextField txtSatuan;
    private javax.swing.JTextField txtStokAwal;
    // End of variables declaration//GEN-END:variables
}