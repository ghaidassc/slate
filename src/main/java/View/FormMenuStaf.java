package view;

import Model.Pengguna;
import Model.Pesanan;
import Model.StafPenjualan;
import Service.PesananService;
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
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class FormMenuStaf extends javax.swing.JFrame {

    private DefaultTableModel modelTabel;
    private final PesananService pesananService = new PesananService();

    public FormMenuStaf() {
        initComponents();
        panelSidebar.setPreferredSize(new Dimension(240, 680));
        panelSidebar.setMinimumSize(new Dimension(240, 680));
        this.setPreferredSize(new Dimension(1120, 700));
        this.setSize(1120, 700);
        this.setLocationRelativeTo(null);
        
        rapikanGayaKomponen();
        pasangLogoSidebar();

        try {
            Pengguna currentUser = SesiPengguna.getPengguna();
            if (currentUser instanceof StafPenjualan) {
                StafPenjualan staf = (StafPenjualan) currentUser;
                lblRoleText.setText(staf.getNamaLengkap());
                lblWilayahText.setText(staf.getWilayahPenjualan());
            }
        } catch (Exception ignored) {}

        modelTabel = (DefaultTableModel) tblRiwayat.getModel();
        desainTabelDanBadge();
        muatStatistikDashboard();
        muatTabelRekapPesanan();
    }

    private static String formatRupiah(double nominal) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat df = new DecimalFormat("#,###", symbols);
        return df.format(nominal);
    }

    private void rapikanGayaKomponen() {
        cardPendapatan.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(3, 1, 1, 1, new Color(234, 88, 12)),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));
        cardLaku.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(3, 1, 1, 1, new Color(203, 213, 225)),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));
        cardAktif.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(3, 1, 1, 1, new Color(59, 130, 246)),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));

        panelCardTabel.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true));
    }

    private void muatStatistikDashboard() {
        try {
            Object[] stats = pesananService.getStatistikDashboardStaf();
            double totalPendapatan = (double) stats[0];
            int totalSelesai = (int) stats[1];
            String semenLaku = (String) stats[2];
            int qtyLaku = (int) stats[3];
            int totalAktif = (int) stats[4];
            int kirim = (int) stats[5];
            int menunggu = (int) stats[6];

            lblValPendapatan.setText("Rp " + formatRupiah(totalPendapatan));
            lblSubPendapatan.setText("↑ " + totalSelesai + " transaksi selesai");

            lblValLaku.setText(semenLaku);
            lblSubLaku.setText("Terjual: " + formatRupiah(qtyLaku) + " Sak");

            lblValAktif.setText(totalAktif + " Pesanan");
            lblSubAktif.setText(kirim + " Proses Kirim • " + menunggu + " Menunggu");
        } catch (SQLException ex) {
            lblValPendapatan.setText("Rp 0");
            lblValLaku.setText("-");
            lblValAktif.setText("0 Pesanan");
        }
    }

    private void muatTabelRekapPesanan() {
        modelTabel.setRowCount(0);
        try {
            List<Pesanan> list = pesananService.getRekapSemuaPesanan();
            for (Pesanan p : list) {
                modelTabel.addRow(new Object[]{
                    String.valueOf(p.getIdPesanan()),
                    p.getNamaPelanggan(),
                    "Rp " + formatRupiah(p.getTotalTagihan()),
                    p.getStatusBayar(),
                    p.getStatus()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat rekap pesanan: " + ex.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void desainTabelDanBadge() {
        JTableHeader header = tblRiwayat.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(new Color(71, 85, 105));
        header.setPreferredSize(new Dimension(header.getWidth(), 38));
        header.setReorderingAllowed(false);

        tblRiwayat.getColumnModel().getColumn(0).setPreferredWidth(45);
        tblRiwayat.getColumnModel().getColumn(1).setPreferredWidth(170);
        tblRiwayat.getColumnModel().getColumn(2).setPreferredWidth(100);
        tblRiwayat.getColumnModel().getColumn(3).setPreferredWidth(90);
        tblRiwayat.getColumnModel().getColumn(4).setPreferredWidth(90);

        tblRiwayat.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                return c;
            }
        });

        tblRiwayat.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                setForeground(new Color(15, 23, 42));
                return c;
            }
        });

        tblRiwayat.getColumnModel().getColumn(3).setCellRenderer(new StatusBadgeBayarRenderer());
        tblRiwayat.getColumnModel().getColumn(4).setCellRenderer(new StatusBadgeKirimRenderer());
    }

    private static class StatusBadgeBayarRenderer extends DefaultTableCellRenderer {
        private String textNilai = "";
        private Color warnaBg = Color.LIGHT_GRAY;
        private Color warnaTeks = Color.BLACK;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            textNilai = (value != null) ? value.toString().trim() : "";

            if ("Lunas".equalsIgnoreCase(textNilai)) {
                warnaBg = new Color(220, 252, 231);
                warnaTeks = new Color(22, 101, 52);
            } else if ("Refund".equalsIgnoreCase(textNilai) || "Dibatalkan".equalsIgnoreCase(textNilai)) {
                warnaBg = new Color(254, 226, 226);
                warnaTeks = new Color(185, 28, 28);
            } else {
                warnaBg = new Color(254, 243, 199);
                warnaTeks = new Color(180, 83, 9);
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
            g2.fillRoundRect(x, y, lebarPill, tinggiPill, 8, 8);

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

    private static class StatusBadgeKirimRenderer extends DefaultTableCellRenderer {
        private String textNilai = "";
        private Color warnaBg = Color.LIGHT_GRAY;
        private Color warnaTeks = Color.BLACK;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            textNilai = (value != null) ? value.toString().trim() : "";

            switch (textNilai) {
                case "Diproses":
                    warnaBg = new Color(241, 245, 249);
                    warnaTeks = new Color(71, 85, 105);
                    break;
                case "Dikirim":
                case "Sedang Dikirim":
                    warnaBg = new Color(254, 243, 199);
                    warnaTeks = new Color(180, 83, 9);
                    break;
                case "Diterima":
                case "Selesai Diterima":
                    warnaBg = new Color(224, 242, 254);
                    warnaTeks = new Color(3, 105, 161);
                    break;
                case "Refund":
                case "Dibatalkan":
                    warnaBg = new Color(254, 226, 226);
                    warnaTeks = new Color(185, 28, 28);
                    break;
                default:
                    warnaBg = new Color(241, 245, 249);
                    warnaTeks = new Color(71, 85, 105);
                    break;
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
            g2.fillRoundRect(x, y, lebarPill, tinggiPill, 8, 8);

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

    private void pasangLogoSidebar() {
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
                panelSidebar.setBackground(warnaBgLogo);

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

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblRoleText = new javax.swing.JLabel();
        panelSidebar = new javax.swing.JPanel();
        lblLogoSidebar = new javax.swing.JLabel();
        panelBadgeUser = new javax.swing.JPanel();
        lblDotStatus = new javax.swing.JLabel();
        lblWilayahText = new javax.swing.JLabel();
        btnNavMenu = new javax.swing.JButton();
        btnNavPelanggan = new javax.swing.JButton();
        btnNavPesanan = new javax.swing.JButton();
        btnNavStatus = new javax.swing.JButton();
        btnKeluar = new javax.swing.JButton();
        panelUtama = new javax.swing.JPanel();
        lblJudulMenu = new javax.swing.JLabel();
        cardPendapatan = new javax.swing.JPanel();
        lblTitlePendapatan = new javax.swing.JLabel();
        lblValPendapatan = new javax.swing.JLabel();
        lblSubPendapatan = new javax.swing.JLabel();
        cardLaku = new javax.swing.JPanel();
        lblTitleLaku = new javax.swing.JLabel();
        lblValLaku = new javax.swing.JLabel();
        lblSubLaku = new javax.swing.JLabel();
        cardAktif = new javax.swing.JPanel();
        lblTitleAktif = new javax.swing.JLabel();
        lblValAktif = new javax.swing.JLabel();
        lblSubAktif = new javax.swing.JLabel();
        panelCardTabel = new javax.swing.JPanel();
        lblTabelTitle = new javax.swing.JLabel();
        jScrollPaneTabel = new javax.swing.JScrollPane();
        tblRiwayat = new javax.swing.JTable();

        lblRoleText.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblRoleText.setForeground(new java.awt.Color(255, 255, 255));
        lblRoleText.setText("Staf Penjualan");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("SLATE - Menu Staf Penjualan");
        setResizable(false);

        panelSidebar.setBackground(new java.awt.Color(22, 27, 34));
        panelSidebar.setPreferredSize(new java.awt.Dimension(240, 680));

        lblLogoSidebar.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        panelBadgeUser.setBackground(new java.awt.Color(33, 39, 50));

        lblDotStatus.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblDotStatus.setForeground(new java.awt.Color(34, 196, 95));
        lblDotStatus.setText("●");

        lblWilayahText.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblWilayahText.setForeground(new java.awt.Color(134, 148, 160));
        lblWilayahText.setText("Samarinda");

        javax.swing.GroupLayout panelBadgeUserLayout = new javax.swing.GroupLayout(panelBadgeUser);
        panelBadgeUser.setLayout(panelBadgeUserLayout);
        panelBadgeUserLayout.setHorizontalGroup(
            panelBadgeUserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBadgeUserLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(lblDotStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblWilayahText, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(16, Short.MAX_VALUE))
        );
        panelBadgeUserLayout.setVerticalGroup(
            panelBadgeUserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBadgeUserLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(lblDotStatus)
                .addGap(2, 2, 2)
                .addComponent(lblWilayahText)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnNavMenu.setBackground(new java.awt.Color(234, 88, 12));
        btnNavMenu.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnNavMenu.setForeground(new java.awt.Color(255, 255, 255));
        btnNavMenu.setText("Menu Staf Penjualan");
        btnNavMenu.setBorderPainted(false);
        btnNavMenu.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNavMenu.setFocusPainted(false);
        btnNavMenu.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavMenu.setMargin(new java.awt.Insets(2, 16, 2, 14));

        btnNavPelanggan.setBackground(new java.awt.Color(22, 27, 34));
        btnNavPelanggan.setForeground(new java.awt.Color(134, 148, 160));
        btnNavPelanggan.setText("Kelola Data Pelanggan");
        btnNavPelanggan.setBorderPainted(false);
        btnNavPelanggan.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNavPelanggan.setFocusPainted(false);
        btnNavPelanggan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavPelanggan.setMargin(new java.awt.Insets(2, 16, 2, 14));
        btnNavPelanggan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNavPelangganActionPerformed(evt);
            }
        });

        btnNavPesanan.setBackground(new java.awt.Color(22, 27, 34));
        btnNavPesanan.setForeground(new java.awt.Color(134, 148, 160));
        btnNavPesanan.setText("Pencatatan Pesanan");
        btnNavPesanan.setBorderPainted(false);
        btnNavPesanan.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNavPesanan.setFocusPainted(false);
        btnNavPesanan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavPesanan.setMargin(new java.awt.Insets(2, 16, 2, 14));
        btnNavPesanan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNavPesananActionPerformed(evt);
            }
        });

        btnNavStatus.setBackground(new java.awt.Color(22, 27, 34));
        btnNavStatus.setForeground(new java.awt.Color(134, 148, 160));
        btnNavStatus.setText("PSPP");
        btnNavStatus.setBorderPainted(false);
        btnNavStatus.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNavStatus.setFocusPainted(false);
        btnNavStatus.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavStatus.setMargin(new java.awt.Insets(2, 16, 2, 14));
        btnNavStatus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNavStatusActionPerformed(evt);
            }
        });

        btnKeluar.setBackground(new java.awt.Color(33, 39, 50));
        btnKeluar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnKeluar.setForeground(new java.awt.Color(239, 68, 68));
        btnKeluar.setText("KELUAR");
        btnKeluar.setBorderPainted(false);
        btnKeluar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnKeluar.setFocusPainted(false);
        btnKeluar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnKeluarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelSidebarLayout = new javax.swing.GroupLayout(panelSidebar);
        panelSidebar.setLayout(panelSidebarLayout);
        panelSidebarLayout.setHorizontalGroup(
            panelSidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSidebarLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(panelSidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblLogoSidebar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelBadgeUser, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnNavMenu, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnNavPelanggan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnNavPesanan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnNavStatus, javax.swing.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
                    .addComponent(btnKeluar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        panelSidebarLayout.setVerticalGroup(
            panelSidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSidebarLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(lblLogoSidebar, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(panelBadgeUser, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addComponent(btnNavMenu, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(btnNavPelanggan, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(btnNavPesanan, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(btnNavStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 234, Short.MAX_VALUE)
                .addComponent(btnKeluar, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );

        getContentPane().add(panelSidebar, java.awt.BorderLayout.WEST);

        panelUtama.setBackground(new java.awt.Color(248, 250, 252));

        lblJudulMenu.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblJudulMenu.setForeground(new java.awt.Color(15, 23, 34));
        lblJudulMenu.setText("Menu Staf Penjualan");

        cardPendapatan.setBackground(new java.awt.Color(255, 255, 255));

        lblTitlePendapatan.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        lblTitlePendapatan.setForeground(new java.awt.Color(71, 85, 105));
        lblTitlePendapatan.setText("Total Pendapatan (Bulan Ini)");

        lblValPendapatan.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblValPendapatan.setForeground(new java.awt.Color(15, 23, 34));
        lblValPendapatan.setText("Rp 0");

        lblSubPendapatan.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblSubPendapatan.setForeground(new java.awt.Color(22, 163, 52));
        lblSubPendapatan.setText("↑ 0 transaksi selesai");

        javax.swing.GroupLayout cardPendapatanLayout = new javax.swing.GroupLayout(cardPendapatan);
        cardPendapatan.setLayout(cardPendapatanLayout);
        cardPendapatanLayout.setHorizontalGroup(
            cardPendapatanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardPendapatanLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(cardPendapatanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblSubPendapatan)
                    .addComponent(lblValPendapatan)
                    .addComponent(lblTitlePendapatan))
                .addContainerGap(59, Short.MAX_VALUE))
        );
        cardPendapatanLayout.setVerticalGroup(
            cardPendapatanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardPendapatanLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(lblTitlePendapatan)
                .addGap(8, 8, 8)
                .addComponent(lblValPendapatan)
                .addGap(8, 8, 8)
                .addComponent(lblSubPendapatan)
                .addContainerGap(24, Short.MAX_VALUE))
        );

        cardLaku.setBackground(new java.awt.Color(255, 255, 255));

        lblTitleLaku.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        lblTitleLaku.setForeground(new java.awt.Color(71, 85, 105));
        lblTitleLaku.setText("Semen Paling Laku");

        lblValLaku.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblValLaku.setForeground(new java.awt.Color(15, 23, 34));
        lblValLaku.setText("-");

        lblSubLaku.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblSubLaku.setForeground(new java.awt.Color(100, 116, 139));
        lblSubLaku.setText("Terjual: 0 Sak");

        javax.swing.GroupLayout cardLakuLayout = new javax.swing.GroupLayout(cardLaku);
        cardLaku.setLayout(cardLakuLayout);
        cardLakuLayout.setHorizontalGroup(
            cardLakuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardLakuLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(cardLakuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblSubLaku)
                    .addComponent(lblValLaku)
                    .addComponent(lblTitleLaku))
                .addContainerGap(108, Short.MAX_VALUE))
        );
        cardLakuLayout.setVerticalGroup(
            cardLakuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardLakuLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(lblTitleLaku)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblValLaku)
                .addGap(8, 8, 8)
                .addComponent(lblSubLaku)
                .addContainerGap(27, Short.MAX_VALUE))
        );

        cardAktif.setBackground(new java.awt.Color(255, 255, 255));

        lblTitleAktif.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        lblTitleAktif.setForeground(new java.awt.Color(71, 85, 105));
        lblTitleAktif.setText("Pesanan Aktif Berjalan");

        lblValAktif.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblValAktif.setForeground(new java.awt.Color(15, 23, 34));
        lblValAktif.setText("0 Pesanan");

        lblSubAktif.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblSubAktif.setForeground(new java.awt.Color(100, 116, 139));
        lblSubAktif.setText("0 Proses Kirim • 0 Menunggu");

        javax.swing.GroupLayout cardAktifLayout = new javax.swing.GroupLayout(cardAktif);
        cardAktif.setLayout(cardAktifLayout);
        cardAktifLayout.setHorizontalGroup(
            cardAktifLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardAktifLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(cardAktifLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblSubAktif)
                    .addComponent(lblValAktif)
                    .addComponent(lblTitleAktif))
                .addContainerGap(77, Short.MAX_VALUE))
        );
        cardAktifLayout.setVerticalGroup(
            cardAktifLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardAktifLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(lblTitleAktif)
                .addGap(8, 8, 8)
                .addComponent(lblValAktif)
                .addGap(8, 8, 8)
                .addComponent(lblSubAktif)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        panelCardTabel.setBackground(new java.awt.Color(255, 255, 255));

        lblTabelTitle.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblTabelTitle.setForeground(new java.awt.Color(15, 23, 34));
        lblTabelTitle.setText("Daftar Riwayat & Rekap Pesanan");

        jScrollPaneTabel.setBorder(null);

        tblRiwayat.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID PESANAN", "NAMA USAHA (MITRA)", "TOTAL TAGIHAN", "STATUS BAYAR", "STATUS PESANAN"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
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
                    .addComponent(jScrollPaneTabel, javax.swing.GroupLayout.DEFAULT_SIZE, 650, Short.MAX_VALUE))
                .addGap(20, 20, 20))
        );
        panelCardTabelLayout.setVerticalGroup(
            panelCardTabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCardTabelLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(lblTabelTitle)
                .addGap(14, 14, 14)
                .addComponent(jScrollPaneTabel, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );

        javax.swing.GroupLayout panelUtamaLayout = new javax.swing.GroupLayout(panelUtama);
        panelUtama.setLayout(panelUtamaLayout);
        panelUtamaLayout.setHorizontalGroup(
            panelUtamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelUtamaLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(panelUtamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblJudulMenu, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelUtamaLayout.createSequentialGroup()
                        .addComponent(cardPendapatan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(14, 14, 14)
                        .addComponent(cardLaku, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(14, 14, 14)
                        .addComponent(cardAktif, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(panelCardTabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelUtamaLayout.setVerticalGroup(
            panelUtamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelUtamaLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(lblJudulMenu)
                .addGap(20, 20, 20)
                .addGroup(panelUtamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(cardPendapatan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cardLaku, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cardAktif, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(24, 24, 24)
                .addComponent(panelCardTabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getContentPane().add(panelUtama, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnNavPelangganActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavPelangganActionPerformed
        new FormKelolaPelanggan().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnNavPelangganActionPerformed

    private void btnNavPesananActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavPesananActionPerformed
        new FormPencatatanPesanan().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnNavPesananActionPerformed

    private void btnNavStatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavStatusActionPerformed
        new FormStatusPengiriman().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnNavStatusActionPerformed

    private void btnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnKeluarActionPerformed
        int confirm = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin keluar?",
                "Konfirmasi Keluar",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            SesiPengguna.setPengguna(null);
            new FormLogin().setVisible(true);
            this.dispose();
        }
    }//GEN-LAST:event_btnKeluarActionPerformed

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new FormMenuStaf().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnKeluar;
    private javax.swing.JButton btnNavMenu;
    private javax.swing.JButton btnNavPelanggan;
    private javax.swing.JButton btnNavPesanan;
    private javax.swing.JButton btnNavStatus;
    private javax.swing.JPanel cardAktif;
    private javax.swing.JPanel cardLaku;
    private javax.swing.JPanel cardPendapatan;
    private javax.swing.JScrollPane jScrollPaneTabel;
    private javax.swing.JLabel lblDotStatus;
    private javax.swing.JLabel lblJudulMenu;
    private javax.swing.JLabel lblLogoSidebar;
    private javax.swing.JLabel lblRoleText;
    private javax.swing.JLabel lblSubAktif;
    private javax.swing.JLabel lblSubLaku;
    private javax.swing.JLabel lblSubPendapatan;
    private javax.swing.JLabel lblTabelTitle;
    private javax.swing.JLabel lblTitleAktif;
    private javax.swing.JLabel lblTitleLaku;
    private javax.swing.JLabel lblTitlePendapatan;
    private javax.swing.JLabel lblValAktif;
    private javax.swing.JLabel lblValLaku;
    private javax.swing.JLabel lblValPendapatan;
    private javax.swing.JLabel lblWilayahText;
    private javax.swing.JPanel panelBadgeUser;
    private javax.swing.JPanel panelCardTabel;
    private javax.swing.JPanel panelSidebar;
    private javax.swing.JPanel panelUtama;
    private javax.swing.JTable tblRiwayat;
    // End of variables declaration//GEN-END:variables
}