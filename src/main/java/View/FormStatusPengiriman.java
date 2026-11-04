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
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class FormStatusPengiriman extends javax.swing.JFrame {

    private DefaultTableModel modelTabel;

    public FormStatusPengiriman() {
        initComponents();
        this.setSize(1080, 680);
        this.setLocationRelativeTo(null);
        rapikanTampilanTambahan();
        pasangLogoSidebar();

        // 1. Inisialisasi opsi ComboBox langsung dari kode (Aman dari bug parser NetBeans)
        cmbUbahStatusBayar.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {
            "Belum Lunas", "Lunas"
        }));

        cmbUbahStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {
            "Diproses", "Dikirim", "Diterima", "Refund"
        }));

        desainTabelDanBadge();

        try {
            Pengguna currentUser = SesiPengguna.getPengguna();
            if (currentUser instanceof StafPenjualan) {
                StafPenjualan staf = (StafPenjualan) currentUser;
                lblRoleText.setText(staf.getNamaLengkap());
                lblWilayahText.setText(staf.getWilayahPenjualan());
            }
        } catch (Exception ignored) {}

        modelTabel = (DefaultTableModel) tblPengiriman.getModel();
        loadDataTabelPengiriman();
        initAksiPengiriman();
    }

    private static String formatRupiah(double nominal) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat df = new DecimalFormat("#,###", symbols);
        return df.format(nominal);
    }

    private void loadDataTabelPengiriman() {
        modelTabel.setRowCount(0);
        String sql = "SELECT p.id_pesanan, pl.nama_usaha, pb.status AS status_bayar, p.status AS status_kirim " +
                     "FROM pesanan p " +
                     "JOIN pelanggan pl ON p.pelanggan_id_pelanggan = pl.id_pelanggan " +
                     "LEFT JOIN pembayaran pb ON p.pembayaran_id_pembayaran = pb.id_pembayaran " +
                     "ORDER BY p.id_pesanan ASC";

        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String id = String.valueOf(rs.getInt("id_pesanan"));
                String namaMitra = rs.getString("nama_usaha");
                String statusBayar = rs.getString("status_bayar");
                String statusKirim = rs.getString("status_kirim");

                if (statusBayar == null) statusBayar = "Belum Lunas";

                modelTabel.addRow(new Object[]{id, namaMitra, statusBayar, statusKirim});
            }

            if (modelTabel.getRowCount() > 0) {
                tblPengiriman.setRowSelectionInterval(0, 0);
                String idPertama = modelTabel.getValueAt(0, 0).toString();
                txtIdPesanan.setText(idPertama);
                muatDetailPesanan(Integer.parseInt(idPertama));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat antrean pengiriman: " + ex.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void muatDetailPesanan(int idPesanan) {
        String sql = "SELECT p.id_pesanan, p.tgl_pesanan, p.total_tagihan_rp, p.status AS status_kirim, " +
                     "pl.nama_usaha, pl.alamat_kirim, " +
                     "b.metode_bayar, b.status AS status_bayar, " +
                     "dp.jumlah_qty, s.nama_semen, s.satuan " +
                     "FROM pesanan p " +
                     "JOIN pelanggan pl ON p.pelanggan_id_pelanggan = pl.id_pelanggan " +
                     "LEFT JOIN pembayaran b ON p.pembayaran_id_pembayaran = b.id_pembayaran " +
                     "LEFT JOIN detail_pesanan dp ON p.id_pesanan = dp.pesanan_id_pesanan " +
                     "LEFT JOIN semen s ON dp.semen_id_semen = s.id_semen " +
                     "WHERE p.id_pesanan = ?";

        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idPesanan);
            try (ResultSet rs = ps.executeQuery()) {
                ArrayList<String> muatanList = new ArrayList<>();
                boolean headerTerisi = false;
                String statusKirim = "Diproses";
                String statusBayar = "Belum Lunas";

                while (rs.next()) {
                    if (!headerTerisi) {
                        String namaMitra = rs.getString("nama_usaha");
                        double total = rs.getDouble("total_tagihan_rp");
                        statusKirim = rs.getString("status_kirim");
                        String alamat = rs.getString("alamat_kirim");
                        String metode = rs.getString("metode_bayar");
                        statusBayar = rs.getString("status_bayar");

                        if (metode == null) metode = "Tunai";
                        if (statusBayar == null) statusBayar = "Belum Lunas";
                        if (alamat == null || alamat.trim().isEmpty()) alamat = "Samarinda";

                        lblMitraNama.setText(namaMitra);

                        java.sql.Date tgl = rs.getDate("tgl_pesanan");
                        String tglStr = (tgl != null) ? new java.text.SimpleDateFormat("dd-MM-yyyy").format(tgl) : "-";

                        String warnaBayar = "Lunas".equalsIgnoreCase(statusBayar) ? "#16A34A" : "#D97706";
                        
                        // Gunakan body dengan line-height dan margin agar spasi lega dan tidak terpotong
                        lblTotalDanBayar.setText("<html><body style='font-family:Segoe UI; font-size:10.5px; line-height:135%;'>" +
                                                "Tgl Pesan: <b>" + tglStr + "</b><br>" +
                                                "Total Tagihan: <b style='color:#0F172A;'>Rp " + formatRupiah(total) + "</b><br>" +
                                                "Status Bayar: <b style='color:" + warnaBayar + ";'>" +
                                                statusBayar + " (" + metode + ")</b></body></html>");

                        lblTujuanAlamat.setText("Tujuan: " + alamat);
                        headerTerisi = true;
                    }

                    int qty = rs.getInt("jumlah_qty");
                    String namaSemen = rs.getString("nama_semen");
                    String satuan = rs.getString("satuan");
                    if (namaSemen != null) {
                        muatanList.add(qty + " " + satuan + " (" + namaSemen + ")");
                    }
                }

                if (headerTerisi) {
                    lblMuatanSemen.setText(muatanList.isEmpty() ? "Muatan: Semen" : "Muatan: " + String.join(", ", muatanList));
                    cmbUbahStatus.setSelectedItem(statusKirim);
                    cmbUbahStatusBayar.setSelectedItem(statusBayar);
                } else {
                    JOptionPane.showMessageDialog(this, "Pesanan dengan ID #" + idPesanan + " tidak ditemukan!", "Nota Tidak Ada", JOptionPane.WARNING_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal mengambil data nota: " + ex.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initAksiPengiriman() {
        tblPengiriman.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblPengiriman.getSelectedRow();
                if (row != -1) {
                    String idStr = tblPengiriman.getValueAt(row, 0).toString();
                    txtIdPesanan.setText(idStr);
                    muatDetailPesanan(Integer.parseInt(idStr));
                }
            }
        });

        btnCariNota.addActionListener(evt -> {
            String idStr = txtIdPesanan.getText().trim();
            if (idStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Masukkan ID Pesanan yang ingin dicari!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                int id = Integer.parseInt(idStr);
                muatDetailPesanan(id);

                for (int i = 0; i < tblPengiriman.getRowCount(); i++) {
                    if (tblPengiriman.getValueAt(i, 0).toString().equals(idStr)) {
                        tblPengiriman.setRowSelectionInterval(i, i);
                        break;
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "ID Pesanan harus berupa angka!", "Format Salah", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void rapikanTampilanTambahan() {
        LineBorder borderField = new LineBorder(new Color(226, 232, 240), 1, true);
        EmptyBorder paddingField = new EmptyBorder(0, 10, 0, 10);
        txtIdPesanan.setBorder(BorderFactory.createCompoundBorder(borderField, paddingField));
        panelRingkasan.setBorder(new LineBorder(new Color(226, 232, 240), 1, true));
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
        JTableHeader header = tblPengiriman.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(new Color(71, 85, 105));
        header.setPreferredSize(new Dimension(header.getWidth(), 38));

        tblPengiriman.getColumnModel().getColumn(0).setPreferredWidth(35);
        tblPengiriman.getColumnModel().getColumn(1).setPreferredWidth(140);
        tblPengiriman.getColumnModel().getColumn(2).setPreferredWidth(95);
        tblPengiriman.getColumnModel().getColumn(3).setPreferredWidth(95);

        tblPengiriman.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                return c;
            }
        });

        tblPengiriman.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setFont(new Font("Segoe UI", Font.PLAIN, 12));
                setForeground(new Color(15, 23, 42));
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
                return c;
            }
        });

        tblPengiriman.getColumnModel().getColumn(2).setCellRenderer(new StatusBadgeBayarRenderer());
        tblPengiriman.getColumnModel().getColumn(3).setCellRenderer(new StatusBadgeKirimRenderer());
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
            } else if ("Refund".equalsIgnoreCase(textNilai)) {
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

            int lebarPill = 82;
            int tinggiPill = 24;
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
                    warnaBg = new Color(220, 252, 231);
                    warnaTeks = new Color(22, 101, 52);
                    break;
                case "Refund":
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

            int lebarPill = 82;
            int tinggiPill = 24;
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
        lblWilayahText = new javax.swing.JLabel();
        btnNavMenu = new javax.swing.JButton();
        btnNavPelanggan = new javax.swing.JButton();
        btnNavPesanan = new javax.swing.JButton();
        btnNavStatus = new javax.swing.JButton();
        btnLogout = new javax.swing.JButton();
        panelKonten = new javax.swing.JPanel();
        lblHeaderTitle = new javax.swing.JLabel();
        panelCardForm = new javax.swing.JPanel();
        lblFormTitle = new javax.swing.JLabel();
        lblIdPesanan = new javax.swing.JLabel();
        txtIdPesanan = new javax.swing.JTextField();
        btnCariNota = new javax.swing.JButton();
        panelRingkasan = new javax.swing.JPanel();
        lblRingkasanHeader = new javax.swing.JLabel();
        lblMitraNama = new javax.swing.JLabel();
        lblTotalDanBayar = new javax.swing.JLabel();
        lblMuatanSemen = new javax.swing.JLabel();
        lblTujuanAlamat = new javax.swing.JLabel();
        lblUbahStatusBayar = new javax.swing.JLabel();
        cmbUbahStatusBayar = new javax.swing.JComboBox<>();
        lblUbahStatus = new javax.swing.JLabel();
        cmbUbahStatus = new javax.swing.JComboBox<>();
        btnSimpanStatus = new javax.swing.JButton();
        panelBoxInfoBatal = new javax.swing.JPanel();
        lblTextInfoBatal = new javax.swing.JLabel();
        panelCardTabel = new javax.swing.JPanel();
        lblTabelTitle = new javax.swing.JLabel();
        jScrollPaneTabel = new javax.swing.JScrollPane();
        tblPengiriman = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("SLATE - Pembaruan Status Pengiriman & Pembayaran");
        setResizable(false);

        panelSidebar.setBackground(new java.awt.Color(22, 27, 34));
        panelSidebar.setPreferredSize(new java.awt.Dimension(240, 680));

        lblLogoSidebar.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        panelBadgeUser.setBackground(new java.awt.Color(33, 39, 50));

        lblDotStatus.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblDotStatus.setForeground(new java.awt.Color(34, 196, 95));
        lblDotStatus.setText("●");

        lblRoleText.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblRoleText.setForeground(new java.awt.Color(248, 248, 248));
        lblRoleText.setText("Staf Penjualan");

        lblWilayahText.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblWilayahText.setForeground(new java.awt.Color(134, 148, 160));
        lblWilayahText.setText("Samarinda Ulu");

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
                    .addComponent(lblWilayahText, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                .addComponent(lblWilayahText)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnNavMenu.setBackground(new java.awt.Color(22, 27, 34));
        btnNavMenu.setForeground(new java.awt.Color(134, 148, 160));
        btnNavMenu.setText("Menu Staf Penjualan");
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

        btnNavPelanggan.setBackground(new java.awt.Color(22, 27, 34));
        btnNavPelanggan.setForeground(new java.awt.Color(134, 148, 160));
        btnNavPelanggan.setText("Kelola Data Pelanggan");
        btnNavPelanggan.setBorderPainted(false);
        btnNavPelanggan.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNavPelanggan.setFocusPainted(false);
        btnNavPelanggan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavPelanggan.setMargin(new java.awt.Insets(2, 18, 2, 14));
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
        btnNavPesanan.setMargin(new java.awt.Insets(2, 18, 2, 14));
        btnNavPesanan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNavPesananActionPerformed(evt);
            }
        });

        btnNavStatus.setBackground(new java.awt.Color(234, 88, 12));
        btnNavStatus.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnNavStatus.setForeground(new java.awt.Color(255, 255, 255));
        btnNavStatus.setText("Pembaruan Status Pengiriman");
        btnNavStatus.setBorderPainted(false);
        btnNavStatus.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNavStatus.setFocusPainted(false);
        btnNavStatus.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavStatus.setMargin(new java.awt.Insets(2, 18, 2, 14));

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
                    .addComponent(btnNavPelanggan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnNavPesanan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnNavStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                .addComponent(btnNavPelanggan, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(btnNavPesanan, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(btnNavStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 204, Short.MAX_VALUE)
                .addComponent(btnLogout, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );

        getContentPane().add(panelSidebar, java.awt.BorderLayout.WEST);

        panelKonten.setBackground(new java.awt.Color(248, 250, 252));

        lblHeaderTitle.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblHeaderTitle.setForeground(new java.awt.Color(15, 15, 23));
        lblHeaderTitle.setText("Pembaruan Status Pengiriman & Pembayaran");

        panelCardForm.setBackground(new java.awt.Color(255, 255, 255));

        lblFormTitle.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblFormTitle.setForeground(new java.awt.Color(15, 15, 23));
        lblFormTitle.setText("Form Kontrol Status Pesanan");

        lblIdPesanan.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblIdPesanan.setForeground(new java.awt.Color(71, 85, 105));
        lblIdPesanan.setText("ID Pesanan");

        btnCariNota.setBackground(new java.awt.Color(30, 37, 43));
        btnCariNota.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnCariNota.setForeground(new java.awt.Color(255, 255, 255));
        btnCariNota.setText("Cari Nota");
        btnCariNota.setBorderPainted(false);
        btnCariNota.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCariNota.setFocusPainted(false);

        panelRingkasan.setBackground(new java.awt.Color(248, 250, 252));

        lblRingkasanHeader.setFont(new java.awt.Font("Segoe UI", 1, 9)); // NOI18N
        lblRingkasanHeader.setForeground(new java.awt.Color(100, 116, 139));
        lblRingkasanHeader.setText("RINGKASAN NOTA FAKTUR:");

        lblMitraNama.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblMitraNama.setForeground(new java.awt.Color(15, 15, 23));
        lblMitraNama.setText("- Pilih Nota Pesanan -");

        lblTotalDanBayar.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        lblTotalDanBayar.setForeground(new java.awt.Color(71, 85, 105));
        lblTotalDanBayar.setText("<html>Total Tagihan: <b>Rp 0</b><br>Status Pembayaran: -</html>");

        lblMuatanSemen.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblMuatanSemen.setForeground(new java.awt.Color(100, 116, 139));
        lblMuatanSemen.setText("Muatan: -");

        lblTujuanAlamat.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblTujuanAlamat.setForeground(new java.awt.Color(100, 116, 139));
        lblTujuanAlamat.setText("Tujuan: -");

        javax.swing.GroupLayout panelRingkasanLayout = new javax.swing.GroupLayout(panelRingkasan);
        panelRingkasan.setLayout(panelRingkasanLayout);
        panelRingkasanLayout.setHorizontalGroup(
            panelRingkasanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRingkasanLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(panelRingkasanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblRingkasanHeader)
                    .addComponent(lblMitraNama, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTotalDanBayar, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblMuatanSemen, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTujuanAlamat, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(22, Short.MAX_VALUE))
        );
        panelRingkasanLayout.setVerticalGroup(
            panelRingkasanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRingkasanLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(lblRingkasanHeader)
                .addGap(2, 2, 2)
                .addComponent(lblMitraNama)
                .addGap(4, 4, 4)
                .addComponent(lblTotalDanBayar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(lblMuatanSemen)
                .addGap(2, 2, 2)
                .addComponent(lblTujuanAlamat)
                .addContainerGap(8, Short.MAX_VALUE))
        );

        lblUbahStatusBayar.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblUbahStatusBayar.setForeground(new java.awt.Color(71, 85, 105));
        lblUbahStatusBayar.setText("Ubah Status Pembayaran");

        cmbUbahStatusBayar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N

        lblUbahStatus.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblUbahStatus.setForeground(new java.awt.Color(71, 85, 105));
        lblUbahStatus.setText("Ubah Status Pengiriman");

        cmbUbahStatus.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N

        btnSimpanStatus.setBackground(new java.awt.Color(234, 88, 12));
        btnSimpanStatus.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnSimpanStatus.setForeground(new java.awt.Color(255, 255, 255));
        btnSimpanStatus.setText("SIMPAN PERUBAHAN STATUS");
        btnSimpanStatus.setBorderPainted(false);
        btnSimpanStatus.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnSimpanStatus.setFocusPainted(false);
        btnSimpanStatus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSimpanStatusActionPerformed(evt);
            }
        });

        panelBoxInfoBatal.setBackground(new java.awt.Color(240, 246, 250));
        panelBoxInfoBatal.setLayout(new java.awt.GridBagLayout());

        lblTextInfoBatal.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblTextInfoBatal.setForeground(new java.awt.Color(71, 98, 121));
        lblTextInfoBatal.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTextInfoBatal.setText("<html><center>Jika dibatalkan (Refund), status otomatis batal<br>dan kuantitas semen dikembalikan ke stok gudang</center></html>");
        panelBoxInfoBatal.add(lblTextInfoBatal, new java.awt.GridBagConstraints());

        javax.swing.GroupLayout panelCardFormLayout = new javax.swing.GroupLayout(panelCardForm);
        panelCardForm.setLayout(panelCardFormLayout);
        panelCardFormLayout.setHorizontalGroup(
            panelCardFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCardFormLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(panelCardFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblFormTitle)
                    .addComponent(lblIdPesanan)
                    .addGroup(panelCardFormLayout.createSequentialGroup()
                        .addComponent(txtIdPesanan, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(btnCariNota, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))
                    .addComponent(panelRingkasan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblUbahStatusBayar)
                    .addComponent(cmbUbahStatusBayar, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblUbahStatus)
                    .addComponent(cmbUbahStatus, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnSimpanStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelBoxInfoBatal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        panelCardFormLayout.setVerticalGroup(
            panelCardFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCardFormLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(lblFormTitle)
                .addGap(12, 12, 12)
                .addComponent(lblIdPesanan)
                .addGap(4, 4, 4)
                .addGroup(panelCardFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtIdPesanan, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCariNota, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addComponent(panelRingkasan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(lblUbahStatusBayar)
                .addGap(4, 4, 4)
                .addComponent(cmbUbahStatusBayar, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(lblUbahStatus)
                .addGap(4, 4, 4)
                .addComponent(cmbUbahStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(btnSimpanStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(panelBoxInfoBatal, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(14, Short.MAX_VALUE))
        );

        panelCardTabel.setBackground(new java.awt.Color(255, 255, 255));

        lblTabelTitle.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblTabelTitle.setForeground(new java.awt.Color(15, 15, 23));
        lblTabelTitle.setText("Daftar Antrean & Status Pengiriman");

        jScrollPaneTabel.setBorder(null);

        tblPengiriman.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "MITRA TOKO", "STATUS BAYAR", "STATUS KIRIM"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblPengiriman.setRowHeight(50);
        tblPengiriman.setSelectionBackground(new java.awt.Color(241, 245, 249));
        tblPengiriman.setSelectionForeground(new java.awt.Color(15, 23, 42));
        tblPengiriman.setShowGrid(false);
        jScrollPaneTabel.setViewportView(tblPengiriman);

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
                    .addComponent(lblHeaderTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 600, javax.swing.GroupLayout.PREFERRED_SIZE)
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

    private void btnNavMenuActionPerformed(java.awt.event.ActionEvent evt) {
        new FormMenuStaf().setVisible(true);
        this.dispose();
    }

    private void btnNavPelangganActionPerformed(java.awt.event.ActionEvent evt) {
        new FormKelolaPelanggan().setVisible(true);
        this.dispose();
    }

    private void btnNavPesananActionPerformed(java.awt.event.ActionEvent evt) {
        new FormPencatatanPesanan().setVisible(true);
        this.dispose();
    }

    private void btnSimpanStatusActionPerformed(java.awt.event.ActionEvent evt) {
        String idStr = txtIdPesanan.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih atau cari ID Pesanan terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idPesanan;
        try {
            idPesanan = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "ID Pesanan tidak valid!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String statusKirimBaru = cmbUbahStatus.getSelectedItem().toString();
        String statusBayarBaru = cmbUbahStatusBayar.getSelectedItem().toString();

        Connection conn = null;
        try {
            conn = Koneksi.getKoneksi();
            conn.setAutoCommit(false);

            String statusKirimLama = "";
            String statusBayarLama = "";
            double totalTagihan = 0;

            String sqlCek = "SELECT p.status AS status_kirim, p.total_tagihan_rp, pb.status AS status_bayar " +
                            "FROM pesanan p " +
                            "LEFT JOIN pembayaran pb ON p.pembayaran_id_pembayaran = pb.id_pembayaran " +
                            "WHERE p.id_pesanan = ?";
            try (PreparedStatement psCek = conn.prepareStatement(sqlCek)) {
                psCek.setInt(1, idPesanan);
                try (ResultSet rs = psCek.executeQuery()) {
                    if (rs.next()) {
                        statusKirimLama = rs.getString("status_kirim");
                        statusBayarLama = rs.getString("status_bayar");
                        totalTagihan = rs.getDouble("total_tagihan_rp");
                        if (statusBayarLama == null) statusBayarLama = "Belum Lunas";
                    } else {
                        JOptionPane.showMessageDialog(this, "ID Pesanan #" + idPesanan + " tidak ditemukan!", "Error", JOptionPane.ERROR_MESSAGE);
                        conn.rollback();
                        return;
                    }
                }
            }

            // Jika diubah menjadi Refund -> Kembalikan stok semen ke tabel semen & tandai pembayaran refund
            if ("Refund".equalsIgnoreCase(statusKirimBaru) && !"Refund".equalsIgnoreCase(statusKirimLama)) {
                String sqlAmbilItem = "SELECT semen_id_semen, jumlah_qty FROM detail_pesanan WHERE pesanan_id_pesanan = ?";
                String sqlRestock = "UPDATE semen SET stok_qty = stok_qty + ? WHERE id_semen = ?";

                try (PreparedStatement psItem = conn.prepareStatement(sqlAmbilItem);
                     PreparedStatement psRestock = conn.prepareStatement(sqlRestock)) {
                    psItem.setInt(1, idPesanan);
                    try (ResultSet rsItem = psItem.executeQuery()) {
                        while (rsItem.next()) {
                            int idSemen = rsItem.getInt("semen_id_semen");
                            int qty = rsItem.getInt("jumlah_qty");

                            psRestock.setInt(1, qty);
                            psRestock.setInt(2, idSemen);
                            psRestock.executeUpdate();
                        }
                    }
                }

                String sqlUpdateBayar = "UPDATE pembayaran SET status = 'Refund', tgl_refund = CURDATE() WHERE pesanan_id_pesanan = ?";
                try (PreparedStatement psBayar = conn.prepareStatement(sqlUpdateBayar)) {
                    psBayar.setInt(1, idPesanan);
                    psBayar.executeUpdate();
                }
            } else {
                // Update status pembayaran (Belum Lunas / Lunas)
                String sqlUpdateBayar = "UPDATE pembayaran SET status = ?, jumlah_bayar_rp = ?, tgl_bayar = CURDATE() WHERE pesanan_id_pesanan = ?";
                try (PreparedStatement psBayar = conn.prepareStatement(sqlUpdateBayar)) {
                    psBayar.setString(1, statusBayarBaru);
                    psBayar.setDouble(2, "Lunas".equalsIgnoreCase(statusBayarBaru) ? totalTagihan : (totalTagihan / 2));
                    psBayar.setInt(3, idPesanan);
                    psBayar.executeUpdate();
                }
            }

            // Update status pesanan di tabel pesanan
            String sqlUpdatePesanan = "UPDATE pesanan SET status = ? WHERE id_pesanan = ?";
            try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdatePesanan)) {
                psUpdate.setString(1, statusKirimBaru);
                psUpdate.setInt(2, idPesanan);
                psUpdate.executeUpdate();
            }

            conn.commit();

            JOptionPane.showMessageDialog(this,
                    "Status pesanan #" + idPesanan + " berhasil diperbarui!\n" +
                    "• Status Pengiriman: " + statusKirimBaru + "\n" +
                    "• Status Pembayaran: " + ("Refund".equalsIgnoreCase(statusKirimBaru) ? "Refund" : statusBayarBaru),
                    "Pembaruan Berhasil",
                    JOptionPane.INFORMATION_MESSAGE);

            loadDataTabelPengiriman();

        } catch (SQLException ex) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            JOptionPane.showMessageDialog(this, "Gagal memperbarui status: " + ex.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
        }
    }

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin keluar?",
                "Konfirmasi Logout",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            SesiPengguna.setPengguna(null);
            new FormLogin().setVisible(true);
            this.dispose();
        }
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new FormStatusPengiriman().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCariNota;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnNavMenu;
    private javax.swing.JButton btnNavPelanggan;
    private javax.swing.JButton btnNavPesanan;
    private javax.swing.JButton btnNavStatus;
    private javax.swing.JButton btnSimpanStatus;
    private javax.swing.JComboBox<String> cmbUbahStatus;
    private javax.swing.JComboBox<String> cmbUbahStatusBayar;
    private javax.swing.JScrollPane jScrollPaneTabel;
    private javax.swing.JLabel lblDotStatus;
    private javax.swing.JLabel lblFormTitle;
    private javax.swing.JLabel lblHeaderTitle;
    private javax.swing.JLabel lblIdPesanan;
    private javax.swing.JLabel lblLogoSidebar;
    private javax.swing.JLabel lblMitraNama;
    private javax.swing.JLabel lblMuatanSemen;
    private javax.swing.JLabel lblRingkasanHeader;
    private javax.swing.JLabel lblRoleText;
    private javax.swing.JLabel lblTabelTitle;
    private javax.swing.JLabel lblTextInfoBatal;
    private javax.swing.JLabel lblTotalDanBayar;
    private javax.swing.JLabel lblTujuanAlamat;
    private javax.swing.JLabel lblUbahStatus;
    private javax.swing.JLabel lblUbahStatusBayar;
    private javax.swing.JLabel lblWilayahText;
    private javax.swing.JPanel panelBadgeUser;
    private javax.swing.JPanel panelBoxInfoBatal;
    private javax.swing.JPanel panelCardForm;
    private javax.swing.JPanel panelCardTabel;
    private javax.swing.JPanel panelKonten;
    private javax.swing.JPanel panelRingkasan;
    private javax.swing.JPanel panelSidebar;
    private javax.swing.JTable tblPengiriman;
    private javax.swing.JTextField txtIdPesanan;
    // End of variables declaration//GEN-END:variables
}