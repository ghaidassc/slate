package view;

import Config.Koneksi;
import Model.Pengguna;
import Util.SesiPengguna;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class FormMenuAdminGudang extends javax.swing.JFrame {

    private DefaultTableModel modelTabel;

    public FormMenuAdminGudang() {
        initComponents();
        this.setLocationRelativeTo(null);
        pasangLogoSidebar();
        tampilkanSesiPengguna();
        loadDataStokSemen();
        desainTabelDanBadge();
        pasangAksiNavigasi();
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

                // Ambil shift kerja admin gudang dari database
                String sqlShift = "SELECT shift FROM admin_gudang WHERE id_pengguna = ?";
                try (Connection conn = Koneksi.getKoneksi();
                     PreparedStatement ps = conn.prepareStatement(sqlShift)) {
                    ps.setInt(1, user.getIdPengguna());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            lblShiftText.setText("Shift Kerja: " + rs.getString("shift"));
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private void loadDataStokSemen() {
        // Model tabel dengan kolom STATUS STOK
        String[] kolom = {"ID", "NAMA SEMEN", "SATUAN", "HARGA JUAL", "STOK GUDANG", "BATAS AMAN", "STATUS STOK"};
        modelTabel = new DefaultTableModel(null, kolom) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblStokSemen.setModel(modelTabel);

        String sql = "SELECT id_semen, nama_semen, satuan, harga_jual_rp, stok_qty, batas_aman FROM semen ORDER BY id_semen ASC";
        ArrayList<String> daftarKritis = new ArrayList<>();

        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id_semen");
                String nama = rs.getString("nama_semen");
                String satuan = rs.getString("satuan");
                double harga = rs.getDouble("harga_jual_rp");
                int stok = rs.getInt("stok_qty");
                int batas = rs.getInt("batas_aman");

                // Evaluasi stok menipis
                String statusStok;
                if (stok <= batas) {
                    statusStok = "Kritis";
                    daftarKritis.add(nama + " (Sisa: " + formatRupiah(stok) + " " + satuan + ")");
                } else {
                    statusStok = "Aman";
                }

                modelTabel.addRow(new Object[]{
                    String.valueOf(id),
                    nama,
                    satuan,
                    "Rp " + formatRupiah(harga),
                    formatRupiah(stok) + " " + satuan,
                    formatRupiah(batas) + " " + satuan,
                    statusStok
                });
            }

            // Tampilkan peringatan visual jika ada stok yang menipis
            if (!daftarKritis.isEmpty()) {
                lblTabelTitle.setText("<html>Daftar Stok Semen &nbsp;&nbsp;<span style='color:#DC2626; font-size:11px; font-weight:normal;'>⚠️ Perhatian: Ada varian semen di bawah batas aman!</span></html>");
            } else {
                lblTabelTitle.setText("Daftar Stok Semen");
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data stok semen: " + ex.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void pasangAksiNavigasi() {
        // Logout
        btnLogout.addActionListener(evt -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Apakah Anda yakin ingin keluar?",
                    "Konfirmasi Logout",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                SesiPengguna.setPengguna(null);
                new FormLogin().setVisible(true);
                dispose();
            }
        });

        // 1. Tambah Stok Semen
        btnNavTambahStok.addActionListener(evt -> {
            new FormTambahStokSemen().setVisible(true);
            dispose();
        });
        cardMenu1.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                new FormTambahStokSemen().setVisible(true);
                dispose();
            }
        });

        // 2. Ubah Data Semen
        btnNavUbahData.addActionListener(evt -> {
            new FormUbahDataSemen().setVisible(true);
            dispose();
        });
        cardMenu2.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                new FormUbahDataSemen().setVisible(true);
                dispose();
            }
        });

        // 3. Tambah / Hapus Semen
        btnNavKelolaSemen.addActionListener(evt -> {
            new FormKelolaSemen().setVisible(true);
            dispose();
        });
        cardMenu3.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                new FormKelolaSemen().setVisible(true);
                dispose();
            }
        });
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

    private void desainTabelDanBadge() {
        JTableHeader header = tblStokSemen.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(new Color(71, 85, 105));
        header.setPreferredSize(new Dimension(header.getWidth(), 38));

        tblStokSemen.getColumnModel().getColumn(0).setPreferredWidth(35);  // ID
        tblStokSemen.getColumnModel().getColumn(1).setPreferredWidth(170); // NAMA SEMEN
        tblStokSemen.getColumnModel().getColumn(2).setPreferredWidth(60);  // SATUAN
        tblStokSemen.getColumnModel().getColumn(3).setPreferredWidth(100); // HARGA JUAL
        tblStokSemen.getColumnModel().getColumn(4).setPreferredWidth(110); // STOK GUDANG
        tblStokSemen.getColumnModel().getColumn(5).setPreferredWidth(95);  // BATAS AMAN
        tblStokSemen.getColumnModel().getColumn(6).setPreferredWidth(95);  // STATUS STOK

        // Kolom ID
        tblStokSemen.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                return c;
            }
        });

        // Kolom Nama Semen
        tblStokSemen.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                setForeground(new Color(15, 23, 42));
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
                return c;
            }
        });

        // Kolom Satuan
        tblStokSemen.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.LEFT);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                setForeground(new Color(15, 23, 42));
                setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
                return c;
            }
        });

        // Kolom Harga Jual
        tblStokSemen.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                setForeground(new Color(15, 23, 42));
                return c;
            }
        });

        // Kolom Stok Gudang (Ubah menjadi teks hitam tegas)
        tblStokSemen.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                setForeground(new Color(15, 23, 42)); // Hitam tegas (#0F172A)
                return c;
            }
        });

        // Kolom Batas Aman (Badge Lembut Abu)
        tblStokSemen.getColumnModel().getColumn(5).setCellRenderer(new BatasAmanBadgeRenderer());

        // Kolom Status Stok (Badge Pill Hijau / Merah Kritis)
        tblStokSemen.getColumnModel().getColumn(6).setCellRenderer(new StatusStokRenderer());
    }

    private static class BatasAmanBadgeRenderer extends DefaultTableCellRenderer {
        private String textNilai = "";

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            textNilai = (value != null) ? value.toString().trim() : "";
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int lebarPill = 84;
            int tinggiPill = 22;
            int x = (getWidth() - lebarPill) / 2;
            int y = (getHeight() - tinggiPill) / 2;

            g2.setColor(new Color(241, 245, 249));
            g2.fillRoundRect(x, y, lebarPill, tinggiPill, 10, 10);

            g2.setColor(new Color(71, 85, 105));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            int lebarTeks = g2.getFontMetrics().stringWidth(textNilai);
            int tinggiFont = g2.getFontMetrics().getAscent();
            int xTeks = x + (lebarPill - lebarTeks) / 2;
            int yTeks = y + (tinggiPill + tinggiFont) / 2 - 3;
            g2.drawString(textNilai, xTeks, yTeks);

            g2.dispose();
        }
    }

    // Renderer Status Stok Kritis / Aman
    private static class StatusStokRenderer extends DefaultTableCellRenderer {
        private String textNilai = "";
        private Color warnaBg = Color.LIGHT_GRAY;
        private Color warnaTeks = Color.BLACK;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            textNilai = (value != null) ? value.toString().trim() : "";

            if ("Kritis".equalsIgnoreCase(textNilai)) {
                warnaBg = new Color(254, 226, 226); // Merah muda
                warnaTeks = new Color(185, 28, 28); // Merah gelap
            } else {
                warnaBg = new Color(220, 252, 231); // Hijau muda
                warnaTeks = new Color(22, 101, 52); // Hijau gelap
            }
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int lebarPill = 76;
            int tinggiPill = 22;
            int x = (getWidth() - lebarPill) / 2;
            int y = (getHeight() - tinggiPill) / 2;

            g2.setColor(warnaBg);
            g2.fillRoundRect(x, y, lebarPill, tinggiPill, 10, 10);

            g2.setColor(warnaTeks);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            int lebarTeks = g2.getFontMetrics().stringWidth(textNilai);
            int tinggiFont = g2.getFontMetrics().getAscent();
            int xTeks = x + (lebarPill - lebarTeks) / 2;
            int yTeks = y + (tinggiPill + tinggiFont) / 2 - 3;
            g2.drawString(textNilai, xTeks, yTeks);

            g2.dispose();
        }
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
        cardMenu1 = new javax.swing.JPanel();
        boxBadge1 = new javax.swing.JPanel();
        lblAngka1 = new javax.swing.JLabel();
        lblTextCard1 = new javax.swing.JLabel();
        cardMenu2 = new javax.swing.JPanel();
        boxBadge2 = new javax.swing.JPanel();
        lblAngka2 = new javax.swing.JLabel();
        lblTextCard2 = new javax.swing.JLabel();
        cardMenu3 = new javax.swing.JPanel();
        boxBadge3 = new javax.swing.JPanel();
        lblAngka3 = new javax.swing.JLabel();
        lblTextCard3 = new javax.swing.JLabel();
        panelCardTabel = new javax.swing.JPanel();
        lblTabelTitle = new javax.swing.JLabel();
        jScrollPaneTabel = new javax.swing.JScrollPane();
        tblStokSemen = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("SLATE - Menu Admin Gudang");
        setPreferredSize(new java.awt.Dimension(1080, 680));
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

        btnNavMenu.setBackground(new java.awt.Color(234, 88, 12));
        btnNavMenu.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnNavMenu.setForeground(new java.awt.Color(255, 255, 255));
        btnNavMenu.setText("Menu Admin Gudang");
        btnNavMenu.setBorderPainted(false);
        btnNavMenu.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNavMenu.setFocusPainted(false);
        btnNavMenu.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavMenu.setMargin(new java.awt.Insets(2, 18, 2, 14));

        btnNavTambahStok.setBackground(new java.awt.Color(22, 27, 34));
        btnNavTambahStok.setForeground(new java.awt.Color(134, 148, 160));
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

        btnNavKelolaSemen.setBackground(new java.awt.Color(22, 27, 34));
        btnNavKelolaSemen.setForeground(new java.awt.Color(134, 148, 160));
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnLogout, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );

        getContentPane().add(panelSidebar, java.awt.BorderLayout.WEST);

        panelKonten.setBackground(new java.awt.Color(248, 250, 252));

        lblHeaderTitle.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblHeaderTitle.setForeground(new java.awt.Color(15, 15, 23));
        lblHeaderTitle.setText("Menu Admin Gudang");

        cardMenu1.setBackground(new java.awt.Color(255, 255, 255));
        cardMenu1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(226, 232, 240), 1, true));
        cardMenu1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        boxBadge1.setBackground(new java.awt.Color(255, 234, 232));
        boxBadge1.setLayout(new java.awt.GridBagLayout());

        lblAngka1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblAngka1.setForeground(new java.awt.Color(234, 43, 43));
        lblAngka1.setText("1");
        boxBadge1.add(lblAngka1, new java.awt.GridBagConstraints());

        lblTextCard1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTextCard1.setForeground(new java.awt.Color(15, 15, 23));
        lblTextCard1.setText("Tambah Stok Semen");

        javax.swing.GroupLayout cardMenu1Layout = new javax.swing.GroupLayout(cardMenu1);
        cardMenu1.setLayout(cardMenu1Layout);
        cardMenu1Layout.setHorizontalGroup(
            cardMenu1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardMenu1Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(boxBadge1, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(lblTextCard1, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(18, Short.MAX_VALUE))
        );
        cardMenu1Layout.setVerticalGroup(
            cardMenu1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardMenu1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(cardMenu1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(boxBadge1, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
                    .addComponent(lblTextCard1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        cardMenu2.setBackground(new java.awt.Color(255, 255, 255));
        cardMenu2.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(226, 232, 240), 1, true));
        cardMenu2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        boxBadge2.setBackground(new java.awt.Color(254, 237, 221));
        boxBadge2.setLayout(new java.awt.GridBagLayout());

        lblAngka2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblAngka2.setForeground(new java.awt.Color(234, 88, 12));
        lblAngka2.setText("2");
        boxBadge2.add(lblAngka2, new java.awt.GridBagConstraints());

        lblTextCard2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTextCard2.setForeground(new java.awt.Color(15, 15, 23));
        lblTextCard2.setText("Ubah Data Semen");

        javax.swing.GroupLayout cardMenu2Layout = new javax.swing.GroupLayout(cardMenu2);
        cardMenu2.setLayout(cardMenu2Layout);
        cardMenu2Layout.setHorizontalGroup(
            cardMenu2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardMenu2Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(boxBadge2, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(lblTextCard2, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(18, Short.MAX_VALUE))
        );
        cardMenu2Layout.setVerticalGroup(
            cardMenu2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardMenu2Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(cardMenu2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(boxBadge2, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
                    .addComponent(lblTextCard2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        cardMenu3.setBackground(new java.awt.Color(255, 255, 255));
        cardMenu3.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(226, 232, 240), 1, true));
        cardMenu3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        boxBadge3.setBackground(new java.awt.Color(254, 244, 215));
        boxBadge3.setLayout(new java.awt.GridBagLayout());

        lblAngka3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblAngka3.setForeground(new java.awt.Color(180, 131, 9));
        lblAngka3.setText("3");
        boxBadge3.add(lblAngka3, new java.awt.GridBagConstraints());

        lblTextCard3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTextCard3.setForeground(new java.awt.Color(15, 15, 23));
        lblTextCard3.setText("Tambah/Hapus Semen");

        javax.swing.GroupLayout cardMenu3Layout = new javax.swing.GroupLayout(cardMenu3);
        cardMenu3.setLayout(cardMenu3Layout);
        cardMenu3Layout.setHorizontalGroup(
            cardMenu3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardMenu3Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(boxBadge3, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(lblTextCard3, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(18, Short.MAX_VALUE))
        );
        cardMenu3Layout.setVerticalGroup(
            cardMenu3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardMenu3Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(cardMenu3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(boxBadge3, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
                    .addComponent(lblTextCard3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        panelCardTabel.setBackground(new java.awt.Color(255, 255, 255));
        panelCardTabel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(226, 232, 240), 1, true));

        lblTabelTitle.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        lblTabelTitle.setForeground(new java.awt.Color(15, 15, 23));
        lblTabelTitle.setText("Daftar Stok Semen");

        jScrollPaneTabel.setBorder(null);

        tblStokSemen.setRowHeight(54);
        tblStokSemen.setSelectionBackground(new java.awt.Color(241, 245, 249));
        tblStokSemen.setSelectionForeground(new java.awt.Color(15, 23, 42));
        tblStokSemen.setShowGrid(false);
        jScrollPaneTabel.setViewportView(tblStokSemen);

        javax.swing.GroupLayout panelCardTabelLayout = new javax.swing.GroupLayout(panelCardTabel);
        panelCardTabel.setLayout(panelCardTabelLayout);
        panelCardTabelLayout.setHorizontalGroup(
            panelCardTabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCardTabelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(panelCardTabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTabelTitle)
                    .addComponent(jScrollPaneTabel, javax.swing.GroupLayout.DEFAULT_SIZE, 696, Short.MAX_VALUE))
                .addGap(25, 25, 25))
        );
        panelCardTabelLayout.setVerticalGroup(
            panelCardTabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCardTabelLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(lblTabelTitle)
                .addGap(18, 18, 18)
                .addComponent(jScrollPaneTabel, javax.swing.GroupLayout.DEFAULT_SIZE, 347, Short.MAX_VALUE)
                .addGap(25, 25, 25))
        );

        javax.swing.GroupLayout panelKontenLayout = new javax.swing.GroupLayout(panelKonten);
        panelKonten.setLayout(panelKontenLayout);
        panelKontenLayout.setHorizontalGroup(
            panelKontenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelKontenLayout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addGroup(panelKontenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblHeaderTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 450, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelKontenLayout.createSequentialGroup()
                        .addComponent(cardMenu1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(16, 16, 16)
                        .addComponent(cardMenu2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(16, 16, 16)
                        .addComponent(cardMenu3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(panelCardTabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelKontenLayout.setVerticalGroup(
            panelKontenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelKontenLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(lblHeaderTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addGroup(panelKontenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(cardMenu1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cardMenu2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cardMenu3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(25, 25, 25)
                .addComponent(panelCardTabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getContentPane().add(panelKonten, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new FormMenuAdminGudang().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel boxBadge1;
    private javax.swing.JPanel boxBadge2;
    private javax.swing.JPanel boxBadge3;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnNavKelolaSemen;
    private javax.swing.JButton btnNavMenu;
    private javax.swing.JButton btnNavTambahStok;
    private javax.swing.JButton btnNavUbahData;
    private javax.swing.JPanel cardMenu1;
    private javax.swing.JPanel cardMenu2;
    private javax.swing.JPanel cardMenu3;
    private javax.swing.JScrollPane jScrollPaneTabel;
    private javax.swing.JLabel lblAngka1;
    private javax.swing.JLabel lblAngka2;
    private javax.swing.JLabel lblAngka3;
    private javax.swing.JLabel lblDotStatus;
    private javax.swing.JLabel lblHeaderTitle;
    private javax.swing.JLabel lblLogoSidebar;
    private javax.swing.JLabel lblRoleText;
    private javax.swing.JLabel lblShiftText;
    private javax.swing.JLabel lblTabelTitle;
    private javax.swing.JLabel lblTextCard1;
    private javax.swing.JLabel lblTextCard2;
    private javax.swing.JLabel lblTextCard3;
    private javax.swing.JPanel panelBadgeUser;
    private javax.swing.JPanel panelCardTabel;
    private javax.swing.JPanel panelKonten;
    private javax.swing.JPanel panelSidebar;
    private javax.swing.JTable tblStokSemen;
    // End of variables declaration//GEN-END:variables
}