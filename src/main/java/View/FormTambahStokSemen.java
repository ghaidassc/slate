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
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
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

public class FormTambahStokSemen extends javax.swing.JFrame {

    private static class VarianItem {
        private final int id;
        private final String nama;
        private final String satuan;

        public VarianItem(int id, String nama, String satuan) {
            this.id = id;
            this.nama = nama;
            this.satuan = satuan;
        }

        public int getId() { return id; }
        public String getSatuan() { return satuan; }

        @Override
        public String toString() {
            return "[ID: " + id + "] " + nama;
        }
    }

    private DefaultTableModel modelRiwayat;

    public FormTambahStokSemen() {
        initComponents();
        this.setLocationRelativeTo(null);
        rapikanTampilanTambahan();
        pasangLogoSidebar();

        // Pastikan kedua field input kosong dari teks default
        txtBatchProduksi.setText("");
        txtInputStokBaru.setText("");

        tampilkanSesiPengguna();
        loadVarianSemen();
        loadTabelRiwayat();
        desainTabel();

        cmbVarianSemen.addActionListener(e -> perbaruiInfoStokSaatIni());
    }

    private static String formatRupiah(double nominal) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat df = new DecimalFormat("#,###", symbols);
        return df.format(nominal);
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
                        lblPetugasNama.setText("Petugas: " + user.getNamaLengkap() + " (ID: " + user.getIdPengguna() + ") | Shift: " + shift);
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private void loadVarianSemen() {
        cmbVarianSemen.removeAllItems();
        String sql = "SELECT id_semen, nama_semen, satuan FROM semen ORDER BY id_semen ASC";
        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id_semen");
                String nama = rs.getString("nama_semen");
                String satuan = rs.getString("satuan");
                cmbVarianSemen.addItem(new VarianItem(id, nama, satuan));
            }

            perbaruiInfoStokSaatIni();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat varian semen: " + ex.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void perbaruiInfoStokSaatIni() {
        Object itemTerpilih = cmbVarianSemen.getSelectedItem();
        if (itemTerpilih instanceof VarianItem) {
            VarianItem varian = (VarianItem) itemTerpilih;
            String sql = "SELECT stok_qty FROM semen WHERE id_semen = ?";
            try (Connection conn = Koneksi.getKoneksi();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, varian.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int stok = rs.getInt("stok_qty");
                        lblBadgeStokSaatIni.setText("Stok Gudang Saat Ini: " + formatRupiah(stok) + " " + varian.getSatuan());
                    }
                }
            } catch (SQLException ex) {
                lblBadgeStokSaatIni.setText("Stok Gudang Saat Ini: -");
            }
        }
    }

    private void loadTabelRiwayat() {
        String[] kolom = {"TGL", "BATCH", "SEMEN", "MASUK"};
        modelRiwayat = new DefaultTableModel(null, kolom) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblRiwayat.setModel(modelRiwayat);

        String sql = "SELECT sm.tgl_masuk, dsm.batch, s.nama_semen, dsm.jumlah_qty " +
                     "FROM detail_stok_masuk dsm " +
                     "JOIN stok_masuk sm ON dsm.stok_masuk_id_stok_masuk = sm.id_stok_masuk " +
                     "JOIN semen s ON dsm.semen_id_semen = s.id_semen " +
                     "ORDER BY sm.id_stok_masuk DESC";

        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
            while (rs.next()) {
                java.sql.Date tgl = rs.getDate("tgl_masuk");
                String tglStr = (tgl != null) ? sdf.format(tgl) : "-";
                String batch = rs.getString("batch");
                String namaSemen = rs.getString("nama_semen");
                int qty = rs.getInt("jumlah_qty");

                String labelSemen = namaSemen.replace("Semen ", "");

                modelRiwayat.addRow(new Object[]{
                    tglStr,
                    batch,
                    labelSemen,
                    "+" + formatRupiah(qty)
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat riwayat pasokan: " + ex.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void rapikanTampilanTambahan() {
        LineBorder borderField = new LineBorder(new Color(226, 232, 240), 1, true);
        EmptyBorder paddingField = new EmptyBorder(0, 10, 0, 10);

        txtBatchProduksi.setBorder(BorderFactory.createCompoundBorder(borderField, paddingField));
        txtInputStokBaru.setBorder(BorderFactory.createCompoundBorder(borderField, paddingField));
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
        } catch (IOException e) {
            lblLogoSidebar.setText("<html><center><h2 style='color:#FFFFFF;'>SLATE</h2></center></html>");
        }
    }

    private void desainTabel() {
        JTableHeader header = tblRiwayat.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(new Color(71, 85, 105));
        header.setPreferredSize(new Dimension(header.getWidth(), 38));

        tblRiwayat.getColumnModel().getColumn(0).setPreferredWidth(60);
        tblRiwayat.getColumnModel().getColumn(1).setPreferredWidth(100);
        tblRiwayat.getColumnModel().getColumn(2).setPreferredWidth(80);
        tblRiwayat.getColumnModel().getColumn(3).setPreferredWidth(80);

        // Format kolom TGL (Dibuat BOLD)
        tblRiwayat.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.LEFT);
                setFont(new Font("Segoe UI", Font.PLAIN, 12)); 
                setForeground(new Color(15, 23, 42));        // Hitam tegas
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
                return c;
            }
        });

        // Format kolom BATCH (Dibuat BOLD)
        tblRiwayat.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setFont(new Font("Segoe UI", Font.BOLD, 12)); // BOLD
                setForeground(new Color(15, 23, 42));        // Hitam tegas
                return c;
            }
        });

        // Format kolom SEMEN (Dibuat BOLD)
        tblRiwayat.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setFont(new Font("Segoe UI", Font.BOLD, 12)); // BOLD
                setForeground(new Color(15, 23, 42));        // Hitam tegas
                return c;
            }
        });

        // Format kolom MASUK (Dibuat BOLD Hijau)
        tblRiwayat.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.RIGHT);
                setFont(new Font("Segoe UI", Font.BOLD, 12)); // BOLD
                setForeground(new Color(22, 163, 74));        // Hijau tegas
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
        lblPilihVarian = new javax.swing.JLabel();
        cmbVarianSemen = new javax.swing.JComboBox<>();
        panelBadgeStokSaatIni = new javax.swing.JPanel();
        lblBadgeStokSaatIni = new javax.swing.JLabel();
        lblBatchProduksi = new javax.swing.JLabel();
        txtBatchProduksi = new javax.swing.JTextField();
        lblInputStokBaru = new javax.swing.JLabel();
        txtInputStokBaru = new javax.swing.JTextField();
        panelAudit = new javax.swing.JPanel();
        lblAuditHeader = new javax.swing.JLabel();
        lblPetugasNama = new javax.swing.JLabel();
        panelValidasi = new javax.swing.JPanel();
        lblValidasiHeader = new javax.swing.JLabel();
        lblValidasiText = new javax.swing.JLabel();
        btnSimpanDatabase = new javax.swing.JButton();
        panelCardTabel = new javax.swing.JPanel();
        lblTabelTitle = new javax.swing.JLabel();
        jScrollPaneTabel = new javax.swing.JScrollPane();
        tblRiwayat = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("SLATE - Tambah Stok Semen");
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

        btnNavTambahStok.setBackground(new java.awt.Color(234, 88, 12));
        btnNavTambahStok.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnNavTambahStok.setForeground(new java.awt.Color(255, 255, 255));
        btnNavTambahStok.setText("Tambah Stok Semen");
        btnNavTambahStok.setBorderPainted(false);
        btnNavTambahStok.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNavTambahStok.setFocusPainted(false);
        btnNavTambahStok.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavTambahStok.setMargin(new java.awt.Insets(2, 18, 2, 14));

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
        lblHeaderTitle.setText("Tambah Stok Semen");

        panelCardForm.setBackground(new java.awt.Color(255, 255, 255));

        lblFormTitle.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblFormTitle.setForeground(new java.awt.Color(15, 15, 23));
        lblFormTitle.setText("Form Pasokan Semen Masuk");

        lblPilihVarian.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblPilihVarian.setForeground(new java.awt.Color(71, 85, 105));
        lblPilihVarian.setText("Pilih Varian Semen");

        panelBadgeStokSaatIni.setBackground(new java.awt.Color(240, 246, 252));
        panelBadgeStokSaatIni.setLayout(new java.awt.GridBagLayout());

        lblBadgeStokSaatIni.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        lblBadgeStokSaatIni.setForeground(new java.awt.Color(71, 85, 105));
        lblBadgeStokSaatIni.setText("Stok Gudang Saat Ini: -");
        panelBadgeStokSaatIni.add(lblBadgeStokSaatIni, new java.awt.GridBagConstraints());

        lblBatchProduksi.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblBatchProduksi.setForeground(new java.awt.Color(71, 85, 105));
        lblBatchProduksi.setText("Nomor Batch Produksi Pabrik");

        lblInputStokBaru.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblInputStokBaru.setForeground(new java.awt.Color(71, 85, 105));
        lblInputStokBaru.setText("Input Stok Semen Baru (Jumlah Qty)");

        panelAudit.setBackground(new java.awt.Color(248, 250, 252));

        lblAuditHeader.setFont(new java.awt.Font("Segoe UI", 1, 9)); // NOI18N
        lblAuditHeader.setForeground(new java.awt.Color(100, 116, 139));
        lblAuditHeader.setText("AUDIT PENERIMAAN GUDANG:");

        lblPetugasNama.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        lblPetugasNama.setForeground(new java.awt.Color(15, 15, 23));
        lblPetugasNama.setText("Petugas: Admin Gudang");

        javax.swing.GroupLayout panelAuditLayout = new javax.swing.GroupLayout(panelAudit);
        panelAudit.setLayout(panelAuditLayout);
        panelAuditLayout.setHorizontalGroup(
            panelAuditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelAuditLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(panelAuditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblAuditHeader)
                    .addComponent(lblPetugasNama, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(22, Short.MAX_VALUE))
        );
        panelAuditLayout.setVerticalGroup(
            panelAuditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelAuditLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(lblAuditHeader)
                .addGap(3, 3, 3)
                .addComponent(lblPetugasNama)
                .addContainerGap(11, Short.MAX_VALUE))
        );

        panelValidasi.setBackground(new java.awt.Color(240, 252, 240));

        lblValidasiHeader.setFont(new java.awt.Font("Segoe UI", 1, 9)); // NOI18N
        lblValidasiHeader.setForeground(new java.awt.Color(25, 100, 35));
        lblValidasiHeader.setText("VALIDASI NOMINAL SISTEM:");

        lblValidasiText.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblValidasiText.setForeground(new java.awt.Color(25, 100, 35));
        lblValidasiText.setText("<html>• Nominal harus berupa bilangan bulat positif.<br>• Stok fisik semen langsung bertambah di tabel semen.</html>");

        javax.swing.GroupLayout panelValidasiLayout = new javax.swing.GroupLayout(panelValidasi);
        panelValidasi.setLayout(panelValidasiLayout);
        panelValidasiLayout.setHorizontalGroup(
            panelValidasiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelValidasiLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(panelValidasiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblValidasiHeader)
                    .addComponent(lblValidasiText, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(22, Short.MAX_VALUE))
        );
        panelValidasiLayout.setVerticalGroup(
            panelValidasiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelValidasiLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(lblValidasiHeader)
                .addGap(2, 2, 2)
                .addComponent(lblValidasiText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnSimpanDatabase.setBackground(new java.awt.Color(234, 88, 12));
        btnSimpanDatabase.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnSimpanDatabase.setForeground(new java.awt.Color(255, 255, 255));
        btnSimpanDatabase.setText("SIMPAN KE DATABASE");
        btnSimpanDatabase.setBorderPainted(false);
        btnSimpanDatabase.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnSimpanDatabase.setFocusPainted(false);
        btnSimpanDatabase.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSimpanDatabaseActionPerformed(evt);
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
                    .addComponent(lblPilihVarian)
                    .addComponent(cmbVarianSemen, 0, 318, Short.MAX_VALUE)
                    .addComponent(panelBadgeStokSaatIni, javax.swing.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
                    .addComponent(lblBatchProduksi)
                    .addComponent(txtBatchProduksi, javax.swing.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
                    .addComponent(lblInputStokBaru)
                    .addComponent(txtInputStokBaru, javax.swing.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
                    .addComponent(panelAudit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelValidasi, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnSimpanDatabase, javax.swing.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        panelCardFormLayout.setVerticalGroup(
            panelCardFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCardFormLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(lblFormTitle)
                .addGap(15, 15, 15)
                .addComponent(lblPilihVarian)
                .addGap(5, 5, 5)
                .addComponent(cmbVarianSemen, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(panelBadgeStokSaatIni, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(lblBatchProduksi)
                .addGap(5, 5, 5)
                .addComponent(txtBatchProduksi, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(lblInputStokBaru)
                .addGap(5, 5, 5)
                .addComponent(txtInputStokBaru, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(panelAudit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(panelValidasi, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(btnSimpanDatabase, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(30, Short.MAX_VALUE))
        );

        panelCardTabel.setBackground(new java.awt.Color(255, 255, 255));

        lblTabelTitle.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblTabelTitle.setForeground(new java.awt.Color(15, 15, 23));
        lblTabelTitle.setText("Riwayat Pasokan Semen Masuk");

        jScrollPaneTabel.setBorder(null);

        tblRiwayat.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        tblRiwayat.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "TGL", "BATCH", "SEMEN", "MASUK"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblRiwayat.setRowHeight(48);
        tblRiwayat.setSelectionBackground(new java.awt.Color(241, 245, 249));
        tblRiwayat.setSelectionForeground(new java.awt.Color(15, 23, 42));
        tblRiwayat.setShowGrid(false);
        jScrollPaneTabel.setViewportView(tblRiwayat);

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

    private void btnNavUbahDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavUbahDataActionPerformed
        new FormUbahDataSemen().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnNavUbahDataActionPerformed

    private void btnNavKelolaSemenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavKelolaSemenActionPerformed
        new FormKelolaSemen().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnNavKelolaSemenActionPerformed

    private void btnSimpanDatabaseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanDatabaseActionPerformed
        Object itemTerpilih = cmbVarianSemen.getSelectedItem();
        if (!(itemTerpilih instanceof VarianItem)) {
            JOptionPane.showMessageDialog(this, "Pilih varian semen terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        VarianItem varian = (VarianItem) itemTerpilih;
        String batch = txtBatchProduksi.getText().trim();
        String jumlahStr = txtInputStokBaru.getText().trim();

        if (batch.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nomor batch produksi pabrik tidak boleh kosong!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            txtBatchProduksi.requestFocus();
            return;
        }

        if (jumlahStr.isEmpty() || !jumlahStr.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Jumlah pasokan harus berupa bilangan bulat positif!", "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
            txtInputStokBaru.requestFocus();
            return;
        }

        int jumlah = Integer.parseInt(jumlahStr);
        if (jumlah <= 0) {
            JOptionPane.showMessageDialog(this, "Jumlah stok masuk harus lebih dari 0!", "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
            txtInputStokBaru.requestFocus();
            return;
        }

        Pengguna userAktif = SesiPengguna.getPengguna();
        int idAdmin = (userAktif != null) ? userAktif.getIdPengguna() : 16;

        Connection conn = null;
        try {
            conn = Koneksi.getKoneksi();
            conn.setAutoCommit(false);

            // 1. Generate ID Baru untuk stok_masuk (MAX + 1)
            int newIdStokMasuk = 1;
            String sqlMaxMasuk = "SELECT COALESCE(MAX(id_stok_masuk), 0) + 1 AS next_id FROM stok_masuk";
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sqlMaxMasuk)) {
                if (rs.next()) {
                    newIdStokMasuk = rs.getInt("next_id");
                }
            }

            // 2. Generate ID Baru untuk detail_stok_masuk (MAX + 1)
            int newIdDetailStok = 1;
            String sqlMaxDetail = "SELECT COALESCE(MAX(id_detail_stok), 0) + 1 AS next_id FROM detail_stok_masuk";
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sqlMaxDetail)) {
                if (rs.next()) {
                    newIdDetailStok = rs.getInt("next_id");
                }
            }

            // 3. Simpan ke tabel stok_masuk
            String sqlStokMasuk = "INSERT INTO stok_masuk (id_stok_masuk, tgl_masuk, semen_id_semen, admin_gudang_id_pengguna) VALUES (?, CURDATE(), ?, ?)";
            try (PreparedStatement psMasuk = conn.prepareStatement(sqlStokMasuk)) {
                psMasuk.setInt(1, newIdStokMasuk);
                psMasuk.setInt(2, varian.getId());
                psMasuk.setInt(3, idAdmin);
                psMasuk.executeUpdate();
            }

            // 4. Simpan ke tabel detail_stok_masuk
            String sqlDetail = "INSERT INTO detail_stok_masuk (id_detail_stok, batch, jumlah_qty, stok_masuk_id_stok_masuk, semen_id_semen) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail)) {
                psDetail.setInt(1, newIdDetailStok);
                psDetail.setString(2, batch);
                psDetail.setInt(3, jumlah);
                psDetail.setInt(4, newIdStokMasuk);
                psDetail.setInt(5, varian.getId());
                psDetail.executeUpdate();
            }

            // 5. Tambahkan stok fisik langsung ke tabel semen
            String sqlUpdateStok = "UPDATE semen SET stok_qty = stok_qty + ? WHERE id_semen = ?";
            try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdateStok)) {
                psUpdate.setInt(1, jumlah);
                psUpdate.setInt(2, varian.getId());
                psUpdate.executeUpdate();
            }

            conn.commit();

            JOptionPane.showMessageDialog(this,
                    "Pasokan sebanyak " + formatRupiah(jumlah) + " " + varian.getSatuan() + " berhasil ditambahkan!\n" +
                    "Batch: " + batch + "\nStok semen di database telah diperbarui.",
                    "Penyimpanan Berhasil",
                    JOptionPane.INFORMATION_MESSAGE);

            txtBatchProduksi.setText("");
            txtInputStokBaru.setText("");
            perbaruiInfoStokSaatIni();
            loadTabelRiwayat();

        } catch (SQLException ex) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            JOptionPane.showMessageDialog(this, "Gagal menambahkan stok semen: " + ex.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
        }
    }//GEN-LAST:event_btnSimpanDatabaseActionPerformed

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
            new FormTambahStokSemen().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnNavKelolaSemen;
    private javax.swing.JButton btnNavMenu;
    private javax.swing.JButton btnNavTambahStok;
    private javax.swing.JButton btnNavUbahData;
    private javax.swing.JButton btnSimpanDatabase;
    private javax.swing.JComboBox<Object> cmbVarianSemen;
    private javax.swing.JScrollPane jScrollPaneTabel;
    private javax.swing.JLabel lblAuditHeader;
    private javax.swing.JLabel lblBadgeStokSaatIni;
    private javax.swing.JLabel lblBatchProduksi;
    private javax.swing.JLabel lblDotStatus;
    private javax.swing.JLabel lblFormTitle;
    private javax.swing.JLabel lblHeaderTitle;
    private javax.swing.JLabel lblInputStokBaru;
    private javax.swing.JLabel lblLogoSidebar;
    private javax.swing.JLabel lblPetugasNama;
    private javax.swing.JLabel lblPilihVarian;
    private javax.swing.JLabel lblRoleText;
    private javax.swing.JLabel lblShiftText;
    private javax.swing.JLabel lblTabelTitle;
    private javax.swing.JLabel lblValidasiHeader;
    private javax.swing.JLabel lblValidasiText;
    private javax.swing.JPanel panelAudit;
    private javax.swing.JPanel panelBadgeStokSaatIni;
    private javax.swing.JPanel panelBadgeUser;
    private javax.swing.JPanel panelCardForm;
    private javax.swing.JPanel panelCardTabel;
    private javax.swing.JPanel panelKonten;
    private javax.swing.JPanel panelSidebar;
    private javax.swing.JPanel panelValidasi;
    private javax.swing.JTable tblRiwayat;
    private javax.swing.JTextField txtBatchProduksi;
    private javax.swing.JTextField txtInputStokBaru;
    // End of variables declaration//GEN-END:variables
}