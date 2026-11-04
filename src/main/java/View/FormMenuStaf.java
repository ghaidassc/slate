package view;

import Config.Koneksi;
import Model.Pengguna;
import Model.StafPenjualan;
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
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;
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

    public FormMenuStaf() {
        initComponents();
        
        pasangLogoSidebar();
        desainTabelDanBadgeWarna();
        loadDataPesanan();

        // 1. Tampilkan nama staf & wilayah dari sesi login
        try {
            Pengguna currentUser = SesiPengguna.getPengguna();
            if (currentUser instanceof StafPenjualan) {
                StafPenjualan staf = (StafPenjualan) currentUser;
                lblNamaStaf.setText(staf.getNamaLengkap());
                lblWilayah.setText(staf.getWilayahPenjualan());
            }
        } catch (Exception ignored) {}

        // 2. Navigasi Sidebar - Kelola Pelanggan
        btnNavPelanggan.addActionListener(evt -> {
            new FormKelolaPelanggan().setVisible(true);
            dispose();
        });

        // 3. Navigasi Sidebar - Pencatatan Pesanan
        btnNavPesanan.addActionListener(evt -> {
            new FormPencatatanPesanan().setVisible(true);
            dispose();
        });
        
        // 4. Navigasi Sidebar - Pembaruan Status Pengiriman
        btnNavStatus.addActionListener(evt -> {
            new FormStatusPengiriman().setVisible(true);
            dispose();
        });

        // 5. Kartu Atas 1 - Kelola Pelanggan
        cardPelanggan.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        cardPelanggan.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                new FormKelolaPelanggan().setVisible(true);
                dispose();
            }
        });

        // 6. Kartu Atas 2 - Pencatatan Pesanan
        cardPesanan.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        cardPesanan.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                new FormPencatatanPesanan().setVisible(true);
                dispose();
            }
        });

        // 7. Kartu Atas 3 - Pembaruan Status
        cardStatus.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        cardStatus.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                new FormStatusPengiriman().setVisible(true);
                dispose();
            }
        });
    }

    // Memuat data riwayat pesanan dari database MySQL sesuai staf yang login
    private void loadDataPesanan() {
        DefaultTableModel model = (DefaultTableModel) tblRekapPesanan.getModel();
        model.setRowCount(0);

        Pengguna currentUser = SesiPengguna.getPengguna();
        if (currentUser == null) return;

        String sql = "SELECT p.id_pesanan, pl.nama_usaha, p.total_tagihan_rp, pb.status AS status_bayar, p.status AS status_pesanan "
                   + "FROM pesanan p "
                   + "JOIN pelanggan pl ON p.pelanggan_id_pelanggan = pl.id_pelanggan "
                   + "JOIN pembayaran pb ON p.pembayaran_id_pembayaran = pb.id_pembayaran "
                   + "WHERE p.staf_penjualan_id_pengguna = ? "
                   + "ORDER BY p.id_pesanan ASC";

        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, currentUser.getIdPengguna());
            ResultSet rs = ps.executeQuery();

            NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

            while (rs.next()) {
                int idPesanan = rs.getInt("id_pesanan");
                String namaUsaha = rs.getString("nama_usaha");
                double totalTagihan = rs.getDouble("total_tagihan_rp");
                String statusBayar = rs.getString("status_bayar");
                String statusPesanan = rs.getString("status_pesanan");

                String totalFormatted = formatRupiah.format(totalTagihan).replace("Rp", "Rp ").replace(",00", "");

                model.addRow(new Object[]{
                    idPesanan,
                    namaUsaha,
                    totalFormatted,
                    statusBayar,
                    statusPesanan
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Gagal memuat rekap pesanan: " + ex.getMessage(), 
                "Error Database", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // Memuat logo SLATE dan menyelaraskan dengan warna sidebar
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

    // Mengatur header tabel dan kapsul status warna
    private void desainTabelDanBadgeWarna() {
        JTableHeader header = tblRekapPesanan.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(new Color(71, 85, 105));
        header.setPreferredSize(new Dimension(header.getWidth(), 38));

        // Format kolom ID Pesanan
        tblRekapPesanan.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.LEFT);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
                return c;
            }
        });

        // Format kolom Total Tagihan tebal
        tblRekapPesanan.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                setForeground(new Color(15, 23, 42));
                return c;
            }
        });

        // Kolom Status Bayar & Status Pesanan diberi badge kapsul warna
        tblRekapPesanan.getColumnModel().getColumn(3).setCellRenderer(new BadgePillRenderer());
        tblRekapPesanan.getColumnModel().getColumn(4).setCellRenderer(new BadgePillRenderer());
    }

    // Renderer khusus badge pill warna pastel
    private static class BadgePillRenderer extends DefaultTableCellRenderer {
        private String textNilai = "";
        private Color warnaBg = Color.LIGHT_GRAY;
        private Color warnaTeks = Color.BLACK;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            textNilai = (value != null) ? value.toString().trim() : "";
            
            switch (textNilai) {
                case "Lunas":
                    warnaBg = new Color(220, 252, 231);  // Hijau pastel
                    warnaTeks = new Color(22, 101, 52);   // Hijau tua
                    break;
                case "Belum Lunas":
                case "Refund":
                    warnaBg = new Color(254, 226, 226);  // Merah muda pastel
                    warnaTeks = new Color(153, 27, 27);   // Merah tua
                    break;
                case "Diterima":
                    warnaBg = new Color(224, 242, 254);  // Biru muda pastel
                    warnaTeks = new Color(7, 89, 133);    // Biru tua
                    break;
                case "Dikirim":
                    warnaBg = new Color(254, 243, 199);  // Kuning pastel
                    warnaTeks = new Color(146, 64, 14);   // Cokelat/jingga
                    break;
                case "Diproses":
                default:
                    warnaBg = new Color(241, 245, 249);  // Abu-abu muda
                    warnaTeks = new Color(71, 85, 105);   // Abu-abu gelap
                    break;
            }
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int lebarPill = 84;
            int tinggiPill = 24;
            int x = (getWidth() - lebarPill) / 2;
            int y = (getHeight() - tinggiPill) / 2;

            g2.setColor(warnaBg);
            g2.fillRoundRect(x, y, lebarPill, tinggiPill, 12, 12);

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

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelSidebar = new javax.swing.JPanel();
        lblLogoSidebar = new javax.swing.JLabel();
        panelBadgeUser = new javax.swing.JPanel();
        lblDotStatus = new javax.swing.JLabel();
        lblNamaStaf = new javax.swing.JLabel();
        lblWilayah = new javax.swing.JLabel();
        btnNavMenu = new javax.swing.JButton();
        btnNavPelanggan = new javax.swing.JButton();
        btnNavPesanan = new javax.swing.JButton();
        btnNavStatus = new javax.swing.JButton();
        btnLogout = new javax.swing.JButton();
        panelKonten = new javax.swing.JPanel();
        lblHeaderTitle = new javax.swing.JLabel();
        card1 = new javax.swing.JPanel();
        badgeBox1 = new javax.swing.JPanel();
        lblAngka1 = new javax.swing.JLabel();
        cardPelanggan = new javax.swing.JLabel();
        card2 = new javax.swing.JPanel();
        badgeBox2 = new javax.swing.JPanel();
        lblAngka2 = new javax.swing.JLabel();
        cardPesanan = new javax.swing.JLabel();
        card3 = new javax.swing.JPanel();
        badgeBox3 = new javax.swing.JPanel();
        lblAngka3 = new javax.swing.JLabel();
        cardStatus = new javax.swing.JLabel();
        panelTabelContainer = new javax.swing.JPanel();
        lblTabelTitle = new javax.swing.JLabel();
        jScrollPaneTabel = new javax.swing.JScrollPane();
        tblRekapPesanan = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("SLATE - Menu Staf Penjualan");
        setPreferredSize(new java.awt.Dimension(1080, 680));
        setResizable(false);

        panelSidebar.setBackground(new java.awt.Color(22, 27, 34));
        panelSidebar.setPreferredSize(new java.awt.Dimension(240, 680));
        panelSidebar.setLayout(null);

        lblLogoSidebar.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        panelSidebar.add(lblLogoSidebar);
        lblLogoSidebar.setBounds(20, 20, 200, 70);

        panelBadgeUser.setBackground(new java.awt.Color(33, 39, 50));
        panelBadgeUser.setLayout(null);

        lblDotStatus.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblDotStatus.setForeground(new java.awt.Color(34, 196, 95));
        lblDotStatus.setText("●");
        panelBadgeUser.add(lblDotStatus);
        lblDotStatus.setBounds(14, 8, 15, 20);

        lblNamaStaf.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblNamaStaf.setForeground(new java.awt.Color(248, 248, 248));
        lblNamaStaf.setText("Staf Penjualan");
        panelBadgeUser.add(lblNamaStaf);
        lblNamaStaf.setBounds(32, 7, 150, 18);

        lblWilayah.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblWilayah.setForeground(new java.awt.Color(134, 148, 160));
        lblWilayah.setText("Samarinda Ulu");
        panelBadgeUser.add(lblWilayah);
        lblWilayah.setBounds(32, 24, 150, 16);

        panelSidebar.add(panelBadgeUser);
        panelBadgeUser.setBounds(20, 105, 200, 46);

        btnNavMenu.setBackground(new java.awt.Color(234, 88, 12));
        btnNavMenu.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnNavMenu.setForeground(new java.awt.Color(255, 255, 255));
        btnNavMenu.setText("Menu Staf Penjualan");
        btnNavMenu.setBorderPainted(false);
        btnNavMenu.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNavMenu.setFocusPainted(false);
        btnNavMenu.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavMenu.setMargin(new java.awt.Insets(2, 18, 2, 14));
        panelSidebar.add(btnNavMenu);
        btnNavMenu.setBounds(15, 175, 210, 42);

        btnNavPelanggan.setBackground(new java.awt.Color(22, 27, 34));
        btnNavPelanggan.setForeground(new java.awt.Color(134, 148, 160));
        btnNavPelanggan.setText("Kelola Data Pelanggan");
        btnNavPelanggan.setBorderPainted(false);
        btnNavPelanggan.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNavPelanggan.setFocusPainted(false);
        btnNavPelanggan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavPelanggan.setMargin(new java.awt.Insets(2, 18, 2, 14));
        panelSidebar.add(btnNavPelanggan);
        btnNavPelanggan.setBounds(15, 225, 210, 42);

        btnNavPesanan.setBackground(new java.awt.Color(22, 27, 34));
        btnNavPesanan.setForeground(new java.awt.Color(134, 148, 160));
        btnNavPesanan.setText("Pencatatan Pesanan");
        btnNavPesanan.setBorderPainted(false);
        btnNavPesanan.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNavPesanan.setFocusPainted(false);
        btnNavPesanan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavPesanan.setMargin(new java.awt.Insets(2, 18, 2, 14));
        panelSidebar.add(btnNavPesanan);
        btnNavPesanan.setBounds(15, 275, 210, 42);

        btnNavStatus.setBackground(new java.awt.Color(22, 27, 34));
        btnNavStatus.setForeground(new java.awt.Color(134, 148, 160));
        btnNavStatus.setText("Pembaruan Status Pengiriman");
        btnNavStatus.setBorderPainted(false);
        btnNavStatus.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNavStatus.setFocusPainted(false);
        btnNavStatus.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavStatus.setMargin(new java.awt.Insets(2, 18, 2, 14));
        btnNavStatus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNavStatusActionPerformed(evt);
            }
        });
        panelSidebar.add(btnNavStatus);
        btnNavStatus.setBounds(15, 325, 210, 42);

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
        panelSidebar.add(btnLogout);
        btnLogout.setBounds(15, 585, 210, 42);

        getContentPane().add(panelSidebar, java.awt.BorderLayout.WEST);

        panelKonten.setBackground(new java.awt.Color(248, 250, 252));
        panelKonten.setPreferredSize(new java.awt.Dimension(840, 680));
        panelKonten.setLayout(null);

        lblHeaderTitle.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblHeaderTitle.setForeground(new java.awt.Color(15, 15, 23));
        lblHeaderTitle.setText("Menu Staf Penjualan");
        panelKonten.add(lblHeaderTitle);
        lblHeaderTitle.setBounds(35, 25, 400, 35);

        card1.setBackground(new java.awt.Color(255, 255, 255));
        card1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(226, 232, 240), 1, true));
        card1.setLayout(null);

        badgeBox1.setBackground(new java.awt.Color(254, 235, 235));
        badgeBox1.setLayout(new java.awt.GridBagLayout());

        lblAngka1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblAngka1.setForeground(new java.awt.Color(234, 81, 58));
        lblAngka1.setText("1");
        badgeBox1.add(lblAngka1, new java.awt.GridBagConstraints());

        card1.add(badgeBox1);
        badgeBox1.setBounds(15, 16, 36, 42);

        cardPelanggan.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        cardPelanggan.setForeground(new java.awt.Color(15, 15, 23));
        cardPelanggan.setText("Kelola Pelanggan");
        card1.add(cardPelanggan);
        cardPelanggan.setBounds(62, 27, 155, 20);

        panelKonten.add(card1);
        card1.setBounds(35, 75, 230, 75);

        card2.setBackground(new java.awt.Color(255, 255, 255));
        card2.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(226, 232, 240), 1, true));
        card2.setLayout(null);

        badgeBox2.setBackground(new java.awt.Color(254, 238, 223));
        badgeBox2.setLayout(new java.awt.GridBagLayout());

        lblAngka2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblAngka2.setForeground(new java.awt.Color(217, 112, 24));
        lblAngka2.setText("2");
        badgeBox2.add(lblAngka2, new java.awt.GridBagConstraints());

        card2.add(badgeBox2);
        badgeBox2.setBounds(15, 16, 36, 42);

        cardPesanan.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        cardPesanan.setForeground(new java.awt.Color(15, 15, 23));
        cardPesanan.setText("Pencatatan Pesanan");
        card2.add(cardPesanan);
        cardPesanan.setBounds(62, 27, 155, 20);

        panelKonten.add(card2);
        card2.setBounds(280, 75, 230, 75);

        card3.setBackground(new java.awt.Color(255, 255, 255));
        card3.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(226, 232, 240), 1, true));
        card3.setLayout(null);

        badgeBox3.setBackground(new java.awt.Color(254, 245, 213));
        badgeBox3.setLayout(new java.awt.GridBagLayout());

        lblAngka3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblAngka3.setForeground(new java.awt.Color(217, 119, 6));
        lblAngka3.setText("3");
        badgeBox3.add(lblAngka3, new java.awt.GridBagConstraints());

        card3.add(badgeBox3);
        badgeBox3.setBounds(15, 16, 36, 42);

        cardStatus.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        cardStatus.setForeground(new java.awt.Color(15, 15, 23));
        cardStatus.setText("Pembaruan Status");
        card3.add(cardStatus);
        cardStatus.setBounds(62, 27, 155, 20);

        panelKonten.add(card3);
        card3.setBounds(525, 75, 230, 75);

        panelTabelContainer.setBackground(new java.awt.Color(255, 255, 255));
        panelTabelContainer.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(226, 232, 240), 1, true));
        panelTabelContainer.setLayout(null);

        lblTabelTitle.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblTabelTitle.setForeground(new java.awt.Color(15, 15, 23));
        lblTabelTitle.setText("Daftar Riwayat & Rekap Pesanan");
        panelTabelContainer.add(lblTabelTitle);
        lblTabelTitle.setBounds(24, 20, 300, 20);

        jScrollPaneTabel.setBorder(null);

        tblRekapPesanan.setModel(new javax.swing.table.DefaultTableModel(
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
        tblRekapPesanan.setRowHeight(48);
        tblRekapPesanan.setSelectionBackground(new java.awt.Color(248, 248, 248));
        tblRekapPesanan.setShowGrid(false);
        jScrollPaneTabel.setViewportView(tblRekapPesanan);

        panelTabelContainer.add(jScrollPaneTabel);
        jScrollPaneTabel.setBounds(24, 55, 720, 375);

        panelKonten.add(panelTabelContainer);
        panelTabelContainer.setBounds(35, 175, 770, 450);

        getContentPane().add(panelKonten, java.awt.BorderLayout.CENTER);

        pack();
        setSize(1080, 680);
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Apakah Anda yakin ingin logout dari sistem?",
            "Konfirmasi Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            SesiPengguna.setPengguna(null);
            new FormLogin().setVisible(true);
            this.dispose();
        }
    }//GEN-LAST:event_btnLogoutActionPerformed

    private void btnNavStatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavStatusActionPerformed
        new FormStatusPengiriman().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnNavStatusActionPerformed

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new FormMenuStaf().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel badgeBox1;
    private javax.swing.JPanel badgeBox2;
    private javax.swing.JPanel badgeBox3;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnNavMenu;
    private javax.swing.JButton btnNavPelanggan;
    private javax.swing.JButton btnNavPesanan;
    private javax.swing.JButton btnNavStatus;
    private javax.swing.JPanel card1;
    private javax.swing.JPanel card2;
    private javax.swing.JPanel card3;
    private javax.swing.JLabel cardPelanggan;
    private javax.swing.JLabel cardPesanan;
    private javax.swing.JLabel cardStatus;
    private javax.swing.JScrollPane jScrollPaneTabel;
    private javax.swing.JLabel lblAngka1;
    private javax.swing.JLabel lblAngka2;
    private javax.swing.JLabel lblAngka3;
    private javax.swing.JLabel lblDotStatus;
    private javax.swing.JLabel lblHeaderTitle;
    private javax.swing.JLabel lblLogoSidebar;
    private javax.swing.JLabel lblNamaStaf;
    private javax.swing.JLabel lblTabelTitle;
    private javax.swing.JLabel lblWilayah;
    private javax.swing.JPanel panelBadgeUser;
    private javax.swing.JPanel panelKonten;
    private javax.swing.JPanel panelSidebar;
    private javax.swing.JPanel panelTabelContainer;
    private javax.swing.JTable tblRekapPesanan;
    // End of variables declaration//GEN-END:variables
}