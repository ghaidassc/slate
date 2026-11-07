package view;

import Model.AdminGudang;
import Model.Pengguna;
import Model.Semen;
import Util.SesiPengguna;
import dao.AdminGudangDAO;
import dao.AdminGudangDAOInterface;
import dao.SemenDAO;
import dao.SemenDAOInterface;
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
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class FormMenuAdminGudang extends javax.swing.JFrame {

    private final SemenDAOInterface semenDAO;
    private final AdminGudangDAOInterface adminGudangDAO;
    private DefaultTableModel modelTabel;

    public FormMenuAdminGudang() {
        this.semenDAO = new SemenDAO();
        this.adminGudangDAO = new AdminGudangDAO();
        initComponents();

        // 1. Kunci ukuran Sidebar persis 240 x 680
        panelSidebar.setPreferredSize(new Dimension(240, 680));
        panelSidebar.setMinimumSize(new Dimension(240, 680));
        panelSidebar.setMaximumSize(new Dimension(240, 32767));

        // 2. Kunci ukuran Window Frame persis 1120 x 700 (Konsisten antar form)
        this.setSize(1120, 700);
        this.setPreferredSize(new Dimension(1120, 700));
        this.setMinimumSize(new Dimension(1120, 700));
        this.setMaximumSize(new Dimension(1120, 700));
        this.setResizable(false);
        this.setLocationRelativeTo(null);

        rapikanKomponenCard();
        pasangLogoSidebar();
        tampilkanSesiPengguna();

        modelTabel = (DefaultTableModel) tblStokSemen.getModel();
        desainTabelModern();
        muatRingkasanDanStok();
    }

    private static String formatAngka(double nominal) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat df = new DecimalFormat("#,###", symbols);
        return df.format(nominal);
    }

    private void rapikanKomponenCard() {
        LineBorder borderCard = new LineBorder(new Color(222, 229, 237), 1, true);
        panelCardStatistik1.setBorder(borderCard);
        panelCardStatistik2.setBorder(borderCard);
        panelCardTabel.setBorder(borderCard);
    }

    private void tampilkanSesiPengguna() {
        try {
            Pengguna currentUser = SesiPengguna.getPengguna();
            if (currentUser != null) {
                lblRoleText.setText(currentUser.getNamaLengkap());

                // Dipisah lewat objek Model & DAO khusus
                if (currentUser instanceof AdminGudang) {
                    AdminGudang admin = (AdminGudang) currentUser;
                    lblShiftText.setText("Shift Kerja: " + admin.getShift());
                } else {
                    String shift = adminGudangDAO.getShiftByIdPengguna(currentUser.getIdPengguna());
                    lblShiftText.setText("Shift Kerja: " + (shift != null ? shift : "Pagi"));
                }
            }
        } catch (Exception ignored) {}
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
                Image imgResized = imgAsli.getScaledInstance(75, 75, Image.SCALE_SMOOTH);
                lblLogoSidebar.setIcon(new ImageIcon(imgResized));
                lblLogoSidebar.setText("");
            } else {
                lblLogoSidebar.setText("<html><center><span style='color:#FFFFFF; font-size:22px; font-weight:bold; letter-spacing:2px;'>S L A T E</span></center></html>");
            }
        } catch (Exception e) {
            lblLogoSidebar.setText("<html><center><h2 style='color:#FFFFFF;'>SLATE</h2></center></html>");
        }
    }

    private void desainTabelModern() {
        JTableHeader header = tblStokSemen.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(new Color(71, 85, 105));
        header.setPreferredSize(new Dimension(header.getWidth(), 42));
        header.setReorderingAllowed(false);

        tblStokSemen.getColumnModel().getColumn(0).setPreferredWidth(45);
        tblStokSemen.getColumnModel().getColumn(1).setPreferredWidth(170);
        tblStokSemen.getColumnModel().getColumn(2).setPreferredWidth(70);
        tblStokSemen.getColumnModel().getColumn(3).setPreferredWidth(110);
        tblStokSemen.getColumnModel().getColumn(4).setPreferredWidth(110);
        tblStokSemen.getColumnModel().getColumn(5).setPreferredWidth(110);
        tblStokSemen.getColumnModel().getColumn(6).setPreferredWidth(100);

        // Kolom 0: ID
        tblStokSemen.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                setForeground(new Color(15, 23, 42));
                return c;
            }
        });

        // Kolom 1: NAMA SEMEN
        tblStokSemen.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setForeground(new Color(15, 23, 42));
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
                return lbl;
            }
        });

        // Kolom 2: SATUAN
        tblStokSemen.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                setForeground(new Color(15, 23, 42));
                return c;
            }
        });

        // Kolom 3: HARGA JUAL
        tblStokSemen.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                setForeground(new Color(15, 23, 42));
                return c;
            }
        });

        // Kolom 4: STOK GUDANG
        tblStokSemen.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                setForeground(new Color(15, 23, 42));
                return c;
            }
        });

        // Kolom 5: BATAS AMAN
        tblStokSemen.getColumnModel().getColumn(5).setCellRenderer(new BatasAmanBadgeRenderer());

        // Kolom 6: STATUS STOK
        tblStokSemen.getColumnModel().getColumn(6).setCellRenderer(new StatusStokBadgeRenderer());
    }

    private static class BatasAmanBadgeRenderer extends DefaultTableCellRenderer {
        private String textNilai = "";

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            textNilai = (value != null) ? value.toString().trim() : "";
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int lebarPill = 82;
            int tinggiPill = 24;
            int x = (getWidth() - lebarPill) / 2;
            int y = (getHeight() - tinggiPill) / 2;

            g2.setColor(new Color(241, 245, 249));
            g2.fillRoundRect(x, y, lebarPill, tinggiPill, 6, 6);

            g2.setColor(new Color(30, 41, 59));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            int lebarTeks = g2.getFontMetrics().stringWidth(textNilai);
            int tinggiFont = g2.getFontMetrics().getAscent();
            int xTeks = x + (lebarPill - lebarTeks) / 2;
            int yTeks = y + (tinggiPill + tinggiFont) / 2 - 3;
            g2.drawString(textNilai, xTeks, yTeks);

            g2.dispose();
        }
    }

    private static class StatusStokBadgeRenderer extends DefaultTableCellRenderer {
        private String textNilai = "Aman";
        private Color warnaBg = new Color(220, 252, 231);
        private Color warnaTeks = new Color(22, 101, 52);

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            textNilai = (value != null) ? value.toString().trim() : "Aman";

            if ("Aman".equalsIgnoreCase(textNilai)) {
                warnaBg = new Color(220, 252, 231);
                warnaTeks = new Color(22, 101, 52);
            } else if ("Menipis".equalsIgnoreCase(textNilai)) {
                warnaBg = new Color(254, 243, 199);
                warnaTeks = new Color(180, 83, 9);
            } else {
                warnaBg = new Color(254, 226, 226);
                warnaTeks = new Color(185, 28, 28);
            }
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int lebarPill = 76;
            int tinggiPill = 24;
            int x = (getWidth() - lebarPill) / 2;
            int y = (getHeight() - tinggiPill) / 2;

            g2.setColor(warnaBg);
            g2.fillRoundRect(x, y, lebarPill, tinggiPill, 6, 6);

            g2.setColor(warnaTeks);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            int lebarTeks = g2.getFontMetrics().stringWidth(textNilai);
            int tinggiFont = g2.getFontMetrics().getAscent();
            int xTeks = x + (lebarPill - lebarTeks) / 2;
            int yTeks = y + (tinggiPill + tinggiFont) / 2 - 3;
            g2.drawString(textNilai, xTeks, yTeks);

            g2.dispose();
        }
    }

    public void muatRingkasanDanStok() {
        // 1. Muat Kartu Ringkasan lewat DAO
        try {
            String cepatHabis = semenDAO.getProdukPalingCepatHabis();
            int totalKeluar = semenDAO.getTotalSemenKeluarHariIni();

            lblNamaSemenCepatHabis.setText(cepatHabis);
            lblTotalSemenKeluar.setText(formatAngka(totalKeluar) + " Sak");
        } catch (SQLException ignored) {}

        // 2. Muat Tabel Semen lewat DAO (getAll())
        modelTabel.setRowCount(0);
        try {
            List<Semen> list = semenDAO.getAll();
            for (Semen s : list) {
                String satuan = s.getSatuan();
                int stok = s.getStok();
                int batas = s.getBatasAman();

                String status;
                if (stok <= batas / 2) {
                    status = "Kritis";
                } else if (stok <= batas) {
                    status = "Menipis";
                } else {
                    status = "Aman";
                }

                modelTabel.addRow(new Object[]{
                    s.getIdSemen(),
                    s.getNamaSemen(),
                    satuan,
                    "Rp " + formatAngka(s.getHargaJual()),
                    formatAngka(stok) + " " + satuan,
                    formatAngka(batas) + " " + satuan,
                    status
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat daftar stok semen: " + e.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
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
        lblJudulHalaman = new javax.swing.JLabel();
        panelCardStatistik1 = new javax.swing.JPanel();
        barisAksenOrange = new javax.swing.JPanel();
        lblKategoriCepatHabis = new javax.swing.JLabel();
        lblNamaSemenCepatHabis = new javax.swing.JLabel();
        lblKeteranganOrange = new javax.swing.JLabel();
        panelCardStatistik2 = new javax.swing.JPanel();
        barisAksenBiru = new javax.swing.JPanel();
        lblKategoriTotalKeluar = new javax.swing.JLabel();
        lblTotalSemenKeluar = new javax.swing.JLabel();
        lblKeteranganBiru = new javax.swing.JLabel();
        lblSubJudulTabel = new javax.swing.JLabel();
        panelCardTabel = new javax.swing.JPanel();
        jScrollPaneTabel = new javax.swing.JScrollPane();
        tblStokSemen = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Menu Admin Gudang (Daftar Stok Semen)");
        setMinimumSize(new java.awt.Dimension(1120, 700));
        setPreferredSize(new java.awt.Dimension(1120, 700));
        setResizable(false);

        panelSidebar.setBackground(new java.awt.Color(22, 27, 34));
        panelSidebar.setMaximumSize(new java.awt.Dimension(240, 32767));
        panelSidebar.setMinimumSize(new java.awt.Dimension(240, 680));
        panelSidebar.setPreferredSize(new java.awt.Dimension(240, 680));

        lblLogoSidebar.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        panelBadgeUser.setBackground(new java.awt.Color(33, 39, 50));

        lblDotStatus.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblDotStatus.setForeground(new java.awt.Color(37, 107, 235));
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
        btnLogout.setText("Keluar");
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

        panelKonten.setBackground(new java.awt.Color(255, 255, 255));

        lblJudulHalaman.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblJudulHalaman.setForeground(new java.awt.Color(15, 23, 42));
        lblJudulHalaman.setText("Menu Admin Gudang");

        panelCardStatistik1.setBackground(new java.awt.Color(255, 255, 255));

        barisAksenOrange.setBackground(new java.awt.Color(234, 88, 12));

        javax.swing.GroupLayout barisAksenOrangeLayout = new javax.swing.GroupLayout(barisAksenOrange);
        barisAksenOrange.setLayout(barisAksenOrangeLayout);
        barisAksenOrangeLayout.setHorizontalGroup(
            barisAksenOrangeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        barisAksenOrangeLayout.setVerticalGroup(
            barisAksenOrangeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 4, Short.MAX_VALUE)
        );

        lblKategoriCepatHabis.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblKategoriCepatHabis.setForeground(new java.awt.Color(100, 116, 139));
        lblKategoriCepatHabis.setText("Paling Cepat Habis");

        lblNamaSemenCepatHabis.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblNamaSemenCepatHabis.setForeground(new java.awt.Color(15, 23, 42));
        lblNamaSemenCepatHabis.setText("Semen Sak 50kg (PCC)");

        lblKeteranganOrange.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblKeteranganOrange.setForeground(new java.awt.Color(234, 88, 12));
        lblKeteranganOrange.setText("Permintaan toko paling tinggi");

        javax.swing.GroupLayout panelCardStatistik1Layout = new javax.swing.GroupLayout(panelCardStatistik1);
        panelCardStatistik1.setLayout(panelCardStatistik1Layout);
        panelCardStatistik1Layout.setHorizontalGroup(
            panelCardStatistik1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(barisAksenOrange, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panelCardStatistik1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(panelCardStatistik1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblKeteranganOrange, javax.swing.GroupLayout.PREFERRED_SIZE, 340, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblNamaSemenCepatHabis, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblKategoriCepatHabis, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(27, Short.MAX_VALUE))
        );
        panelCardStatistik1Layout.setVerticalGroup(
            panelCardStatistik1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCardStatistik1Layout.createSequentialGroup()
                .addComponent(barisAksenOrange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(lblKategoriCepatHabis)
                .addGap(6, 6, 6)
                .addComponent(lblNamaSemenCepatHabis)
                .addGap(8, 8, 8)
                .addComponent(lblKeteranganOrange)
                .addContainerGap(16, Short.MAX_VALUE))
        );

        panelCardStatistik2.setBackground(new java.awt.Color(255, 255, 255));

        barisAksenBiru.setBackground(new java.awt.Color(37, 100, 230));

        javax.swing.GroupLayout barisAksenBiruLayout = new javax.swing.GroupLayout(barisAksenBiru);
        barisAksenBiru.setLayout(barisAksenBiruLayout);
        barisAksenBiruLayout.setHorizontalGroup(
            barisAksenBiruLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        barisAksenBiruLayout.setVerticalGroup(
            barisAksenBiruLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 4, Short.MAX_VALUE)
        );

        lblKategoriTotalKeluar.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblKategoriTotalKeluar.setForeground(new java.awt.Color(100, 116, 139));
        lblKategoriTotalKeluar.setText("Total Semen Keluar Hari Ini");

        lblTotalSemenKeluar.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblTotalSemenKeluar.setForeground(new java.awt.Color(15, 23, 42));
        lblTotalSemenKeluar.setText("1.250 Sak");

        lblKeteranganBiru.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblKeteranganBiru.setForeground(new java.awt.Color(37, 100, 230));
        lblKeteranganBiru.setText("Dari transaksi yang diproses");

        javax.swing.GroupLayout panelCardStatistik2Layout = new javax.swing.GroupLayout(panelCardStatistik2);
        panelCardStatistik2.setLayout(panelCardStatistik2Layout);
        panelCardStatistik2Layout.setHorizontalGroup(
            panelCardStatistik2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(barisAksenBiru, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panelCardStatistik2Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(panelCardStatistik2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblKeteranganBiru, javax.swing.GroupLayout.PREFERRED_SIZE, 340, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTotalSemenKeluar, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblKategoriTotalKeluar, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(37, Short.MAX_VALUE))
        );
        panelCardStatistik2Layout.setVerticalGroup(
            panelCardStatistik2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCardStatistik2Layout.createSequentialGroup()
                .addComponent(barisAksenBiru, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(lblKategoriTotalKeluar)
                .addGap(4, 4, 4)
                .addComponent(lblTotalSemenKeluar)
                .addGap(6, 6, 6)
                .addComponent(lblKeteranganBiru)
                .addContainerGap(16, Short.MAX_VALUE))
        );

        lblSubJudulTabel.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblSubJudulTabel.setForeground(new java.awt.Color(15, 23, 42));
        lblSubJudulTabel.setText("Daftar Stok Semen");

        panelCardTabel.setBackground(new java.awt.Color(255, 255, 255));

        jScrollPaneTabel.setBackground(new java.awt.Color(255, 255, 255));
        jScrollPaneTabel.setBorder(null);

        tblStokSemen.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "NAMA SEMEN", "SATUAN", "HARGA JUAL", "STOK GUDANG", "BATAS AMAN", "STATUS STOK"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblStokSemen.setRowHeight(54);
        tblStokSemen.setSelectionBackground(new java.awt.Color(235, 241, 247));
        tblStokSemen.setSelectionForeground(new java.awt.Color(26, 31, 38));
        tblStokSemen.setShowGrid(false);
        jScrollPaneTabel.setViewportView(tblStokSemen);

        javax.swing.GroupLayout panelCardTabelLayout = new javax.swing.GroupLayout(panelCardTabel);
        panelCardTabel.setLayout(panelCardTabelLayout);
        panelCardTabelLayout.setHorizontalGroup(
            panelCardTabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPaneTabel, javax.swing.GroupLayout.DEFAULT_SIZE, 820, Short.MAX_VALUE)
        );
        panelCardTabelLayout.setVerticalGroup(
            panelCardTabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPaneTabel, javax.swing.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout panelKontenLayout = new javax.swing.GroupLayout(panelKonten);
        panelKonten.setLayout(panelKontenLayout);
        panelKontenLayout.setHorizontalGroup(
            panelKontenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelKontenLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(panelKontenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblJudulHalaman, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelKontenLayout.createSequentialGroup()
                        .addComponent(panelCardStatistik1, javax.swing.GroupLayout.PREFERRED_SIZE, 395, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20)
                        .addComponent(panelCardStatistik2, javax.swing.GroupLayout.PREFERRED_SIZE, 405, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblSubJudulTabel, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panelCardTabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(30, Short.MAX_VALUE))
        );
        panelKontenLayout.setVerticalGroup(
            panelKontenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelKontenLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(lblJudulHalaman, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addGroup(panelKontenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(panelCardStatistik1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelCardStatistik2, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(22, 22, 22)
                .addComponent(lblSubJudulTabel)
                .addGap(12, 12, 12)
                .addComponent(panelCardTabel, javax.swing.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)
                .addGap(35, 35, 35))
        );

        getContentPane().add(panelKonten, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnNavTambahStokActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavTambahStokActionPerformed
        new FormTambahStokSemen().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnNavTambahStokActionPerformed

    private void btnNavUbahDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavUbahDataActionPerformed
        new FormUbahDataSemen().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnNavUbahDataActionPerformed

    private void btnNavKelolaSemenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavKelolaSemenActionPerformed
        new FormKelolaSemen().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnNavKelolaSemenActionPerformed

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
        int konfirm = JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin keluar?", "Konfirmasi Keluar", JOptionPane.YES_NO_OPTION);
        if (konfirm == JOptionPane.YES_OPTION) {
            SesiPengguna.setPengguna(null);
            new FormLogin().setVisible(true);
            this.dispose();
        }
    }//GEN-LAST:event_btnLogoutActionPerformed

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        java.awt.EventQueue.invokeLater(() -> {
            new FormMenuAdminGudang().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel barisAksenBiru;
    private javax.swing.JPanel barisAksenOrange;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnNavKelolaSemen;
    private javax.swing.JButton btnNavMenu;
    private javax.swing.JButton btnNavTambahStok;
    private javax.swing.JButton btnNavUbahData;
    private javax.swing.JScrollPane jScrollPaneTabel;
    private javax.swing.JLabel lblDotStatus;
    private javax.swing.JLabel lblJudulHalaman;
    private javax.swing.JLabel lblKategoriCepatHabis;
    private javax.swing.JLabel lblKategoriTotalKeluar;
    private javax.swing.JLabel lblKeteranganBiru;
    private javax.swing.JLabel lblKeteranganOrange;
    private javax.swing.JLabel lblLogoSidebar;
    private javax.swing.JLabel lblNamaSemenCepatHabis;
    private javax.swing.JLabel lblRoleText;
    private javax.swing.JLabel lblShiftText;
    private javax.swing.JLabel lblSubJudulTabel;
    private javax.swing.JLabel lblTotalSemenKeluar;
    private javax.swing.JPanel panelBadgeUser;
    private javax.swing.JPanel panelCardStatistik1;
    private javax.swing.JPanel panelCardStatistik2;
    private javax.swing.JPanel panelCardTabel;
    private javax.swing.JPanel panelKonten;
    private javax.swing.JPanel panelSidebar;
    private javax.swing.JTable tblStokSemen;
    // End of variables declaration//GEN-END:variables
}