package view;

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

public class FormStatusPengiriman extends javax.swing.JFrame {

    private DefaultTableModel modelTabel;
    private final Service.PesananService pesananService = new Service.PesananService();

    public FormStatusPengiriman() {
        initComponents();
        
        // Kunci ukuran sidebar & window secara presisi
        panelSidebar.setPreferredSize(new Dimension(240, 680));
        panelSidebar.setMinimumSize(new Dimension(240, 680));
        this.setSize(1120, 700);
        this.setPreferredSize(new Dimension(1120, 700));
        this.setMinimumSize(new Dimension(1120, 700));
        this.setLocationRelativeTo(null);
        
        rapikanTampilanTambahan();
        pasangLogoSidebar();

        // Inisialisasi opsi ComboBox
        cmbUbahStatusBayar.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {
            "Belum Lunas", "Lunas"
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
        try {
            java.util.List<Object[]> antrean = pesananService.getAntreanPengiriman();
            for (Object[] baris : antrean) {
                if (baris.length >= 4) {
                    if ("Refund".equalsIgnoreCase(String.valueOf(baris[2]))) baris[2] = "Dibatalkan";
                    if ("Refund".equalsIgnoreCase(String.valueOf(baris[3]))) baris[3] = "Dibatalkan";
                }
                modelTabel.addRow(baris);
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
        try {
            Model.DetailNota nota = pesananService.getDetailNota(idPesanan);
            if (nota != null) {
                lblMitraNama.setText(nota.getNamaUsaha());

                java.sql.Date tgl = nota.getTglPesanan();
                String tglStr = (tgl != null) ? new java.text.SimpleDateFormat("dd-MM-yyyy").format(tgl) : "-";
                
                String statusBayar = "Refund".equalsIgnoreCase(nota.getStatusBayar()) ? "Dibatalkan" : nota.getStatusBayar();
                String statusKirim = "Refund".equalsIgnoreCase(nota.getStatusKirim()) ? "Dibatalkan" : nota.getStatusKirim();

                String warnaBayar;
                if ("Lunas".equalsIgnoreCase(statusBayar)) {
                    warnaBayar = "#16A34A";
                } else if ("Dibatalkan".equalsIgnoreCase(statusBayar)) {
                    warnaBayar = "#DC2626";
                } else {
                    warnaBayar = "#D97706";
                }

                lblTotalDanBayar.setText("<html><body style='font-family:Segoe UI; font-size:10.5px; line-height:135%;'>" +
                                        "Tgl Pesan: <b>" + tglStr + "</b><br>" +
                                        "Total Tagihan: <b style='color:#0F172A;'>Rp " + formatRupiah(nota.getTotalTagihan()) + "</b><br>" +
                                        "Status Bayar: <b style='color:" + warnaBayar + ";'>" +
                                        statusBayar + " (" + nota.getMetodeBayar() + ")</b></body></html>");

                lblTujuanAlamat.setText("Tujuan: " + nota.getAlamatKirim());

                java.util.List<String> muatan = nota.getDaftarMuatan();
                lblMuatanSemen.setText(muatan.isEmpty() ? "Muatan: Semen" : "Muatan: " + String.join(", ", muatan));

                cmbUbahStatusBayar.setSelectedItem(statusBayar);

                cmbUbahStatus.removeAllItems();

                if ("Dibatalkan".equalsIgnoreCase(statusKirim) || "Diterima".equalsIgnoreCase(statusKirim)) {
                    cmbUbahStatus.addItem(statusKirim);
                    cmbUbahStatus.setSelectedItem(statusKirim);
                    cmbUbahStatus.setEnabled(false);
                    cmbUbahStatusBayar.setEnabled(false);
                    btnSimpanStatus.setEnabled(false);
                } else if ("Dikirim".equalsIgnoreCase(statusKirim) || "Sedang Dikirim".equalsIgnoreCase(statusKirim)) {
                    cmbUbahStatus.setEnabled(true);
                    cmbUbahStatusBayar.setEnabled(true);
                    btnSimpanStatus.setEnabled(true);

                    cmbUbahStatus.addItem("Sedang Dikirim");
                    cmbUbahStatus.addItem("Diterima");
                    cmbUbahStatus.addItem("Dibatalkan");
                    cmbUbahStatus.setSelectedItem("Sedang Dikirim");
                } else {
                    cmbUbahStatus.setEnabled(true);
                    cmbUbahStatusBayar.setEnabled(true);
                    btnSimpanStatus.setEnabled(true);

                    cmbUbahStatus.addItem("Diproses");
                    cmbUbahStatus.addItem("Sedang Dikirim");
                    cmbUbahStatus.addItem("Dibatalkan");
                    cmbUbahStatus.setSelectedItem("Diproses");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Pesanan dengan ID #" + idPesanan + " tidak ditemukan!", "Nota Tidak Ada", JOptionPane.WARNING_MESSAGE);
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
            } else if ("Dibatalkan".equalsIgnoreCase(textNilai) || "Refund".equalsIgnoreCase(textNilai)) {
                textNilai = "Dibatalkan";
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
                    textNilai = "Dikirim";
                    warnaBg = new Color(254, 243, 199);
                    warnaTeks = new Color(180, 83, 9);
                    break;
                case "Diterima":
                case "Selesai Diterima":
                    textNilai = "Diterima";
                    warnaBg = new Color(220, 252, 231);
                    warnaTeks = new Color(22, 101, 52);
                    break;
                case "Dibatalkan":
                case "Refund":
                    textNilai = "Dibatalkan";
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
        lblRoleText.setForeground(new java.awt.Color(255, 255, 255));
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
                .addComponent(lblDotStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addGroup(panelBadgeUserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblRoleText, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblWilayahText, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        panelBadgeUserLayout.setVerticalGroup(
            panelBadgeUserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBadgeUserLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(panelBadgeUserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDotStatus)
                    .addComponent(lblRoleText))
                .addGap(2, 2, 2)
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
        btnNavMenu.setMargin(new java.awt.Insets(2, 16, 2, 14));
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

        btnNavStatus.setBackground(new java.awt.Color(234, 88, 12));
        btnNavStatus.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnNavStatus.setForeground(new java.awt.Color(255, 255, 255));
        btnNavStatus.setText("PSPP");
        btnNavStatus.setBorderPainted(false);
        btnNavStatus.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNavStatus.setFocusPainted(false);
        btnNavStatus.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavStatus.setMargin(new java.awt.Insets(2, 16, 2, 14));

        btnLogout.setBackground(new java.awt.Color(33, 39, 50));
        btnLogout.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnLogout.setForeground(new java.awt.Color(239, 68, 68));
        btnLogout.setText("KELUAR");
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
                .addGap(16, 16, 16)
                .addGroup(panelSidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblLogoSidebar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelBadgeUser, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnNavMenu, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnNavPelanggan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnNavPesanan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnNavStatus, javax.swing.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
                    .addComponent(btnLogout, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addComponent(btnLogout, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );

        getContentPane().add(panelSidebar, java.awt.BorderLayout.WEST);

        panelKonten.setBackground(new java.awt.Color(248, 250, 252));

        lblHeaderTitle.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblHeaderTitle.setForeground(new java.awt.Color(15, 23, 34));
        lblHeaderTitle.setText("Pembaruan Status Pembayaran dan Pengiriman");

        panelCardForm.setBackground(new java.awt.Color(255, 255, 255));

        lblFormTitle.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblFormTitle.setForeground(new java.awt.Color(15, 23, 34));
        lblFormTitle.setText("Form Kontrol Status Logistik");

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
        lblMitraNama.setForeground(new java.awt.Color(15, 23, 34));
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
                .addGap(12, 12, 12)
                .addGroup(panelRingkasanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblRingkasanHeader)
                    .addComponent(lblMitraNama, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTotalDanBayar, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblMuatanSemen, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTujuanAlamat, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(20, Short.MAX_VALUE))
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
        cmbUbahStatusBayar.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Belum Lunas", "Lunas" }));

        lblUbahStatus.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblUbahStatus.setForeground(new java.awt.Color(71, 85, 105));
        lblUbahStatus.setText("Ubah Status Pengiriman");

        cmbUbahStatus.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        cmbUbahStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Diproses", "Sedang Dikirim", "Diterima", "Dibatalkan" }));

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
        lblTextInfoBatal.setText("<html><center>Jika dibatalkan, status otomatis menjadi batal<br>dan kuantitas semen dikembalikan ke stok gudang</center></html>");
        panelBoxInfoBatal.add(lblTextInfoBatal, new java.awt.GridBagConstraints());

        javax.swing.GroupLayout panelCardFormLayout = new javax.swing.GroupLayout(panelCardForm);
        panelCardForm.setLayout(panelCardFormLayout);
        panelCardFormLayout.setHorizontalGroup(
            panelCardFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCardFormLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(panelCardFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblFormTitle)
                    .addComponent(lblIdPesanan)
                    .addGroup(panelCardFormLayout.createSequentialGroup()
                        .addComponent(txtIdPesanan, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addComponent(btnCariNota, javax.swing.GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE))
                    .addComponent(panelRingkasan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblUbahStatusBayar)
                    .addComponent(cmbUbahStatusBayar, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblUbahStatus)
                    .addComponent(cmbUbahStatus, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnSimpanStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelBoxInfoBatal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(18, Short.MAX_VALUE))
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
                .addGap(8, 8, 8)
                .addComponent(panelBoxInfoBatal, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(14, Short.MAX_VALUE))
        );

        panelCardTabel.setBackground(new java.awt.Color(255, 255, 255));

        lblTabelTitle.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblTabelTitle.setForeground(new java.awt.Color(15, 23, 34));
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
        tblPengiriman.setRowHeight(48);
        tblPengiriman.setSelectionBackground(new java.awt.Color(241, 245, 249));
        tblPengiriman.setSelectionForeground(new java.awt.Color(15, 23, 42));
        tblPengiriman.setShowGrid(false);
        jScrollPaneTabel.setViewportView(tblPengiriman);

        javax.swing.GroupLayout panelCardTabelLayout = new javax.swing.GroupLayout(panelCardTabel);
        panelCardTabel.setLayout(panelCardTabelLayout);
        panelCardTabelLayout.setHorizontalGroup(
            panelCardTabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCardTabelLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(panelCardTabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTabelTitle)
                    .addComponent(jScrollPaneTabel, javax.swing.GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE))
                .addGap(18, 18, 18))
        );
        panelCardTabelLayout.setVerticalGroup(
            panelCardTabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCardTabelLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(lblTabelTitle)
                .addGap(14, 14, 14)
                .addComponent(jScrollPaneTabel, javax.swing.GroupLayout.DEFAULT_SIZE, 485, Short.MAX_VALUE)
                .addGap(18, 18, 18))
        );

        javax.swing.GroupLayout panelKontenLayout = new javax.swing.GroupLayout(panelKonten);
        panelKonten.setLayout(panelKontenLayout);
        panelKontenLayout.setHorizontalGroup(
            panelKontenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelKontenLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(panelKontenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblHeaderTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelKontenLayout.createSequentialGroup()
                        .addComponent(panelCardForm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(16, 16, 16)
                        .addComponent(panelCardTabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(25, Short.MAX_VALUE))
        );
        panelKontenLayout.setVerticalGroup(
            panelKontenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelKontenLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(lblHeaderTitle)
                .addGap(18, 18, 18)
                .addGroup(panelKontenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(panelCardForm, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelCardTabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(25, Short.MAX_VALUE))
        );

        getContentPane().add(panelKonten, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnNavMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavMenuActionPerformed
        new FormMenuStaf().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnNavMenuActionPerformed

    private void btnNavPelangganActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavPelangganActionPerformed
        new FormKelolaPelanggan().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnNavPelangganActionPerformed

    private void btnNavPesananActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavPesananActionPerformed
        new FormPencatatanPesanan().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnNavPesananActionPerformed

    private void btnSimpanStatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanStatusActionPerformed
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

        int barisTabel = tblPengiriman.getSelectedRow();
        if (barisTabel != -1) {
            String statusKirimLama = tblPengiriman.getValueAt(barisTabel, 3).toString();
            if (("Dikirim".equalsIgnoreCase(statusKirimLama) || "Sedang Dikirim".equalsIgnoreCase(statusKirimLama)) 
                    && "Diproses".equalsIgnoreCase(statusKirimBaru)) {
                JOptionPane.showMessageDialog(this,
                        "Aturan Alur Distribusi:\nPesanan semen yang SUDAH DIKIRIM tidak dapat dikembalikan ke status 'Diproses'!",
                        "Aksi Tidak Diizinkan",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        if (("Sedang Dikirim".equalsIgnoreCase(statusKirimBaru) || "Dikirim".equalsIgnoreCase(statusKirimBaru) || "Diterima".equalsIgnoreCase(statusKirimBaru)) 
                && !"Lunas".equalsIgnoreCase(statusBayarBaru)) {
            JOptionPane.showMessageDialog(this,
                    "Validasi SOP Gagal!\n" +
                    "Pesanan semen TIDAK BISA berstatus '" + statusKirimBaru + "' jika pembayaran masih Belum Lunas.\n" +
                    "Ubah status pembayaran menjadi 'Lunas' terlebih dahulu.",
                    "Pembayaran Belum Lunas",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if ("Dibatalkan".equalsIgnoreCase(statusKirimBaru)) {
            int konfirmasi = JOptionPane.showConfirmDialog(this,
                    "Apakah Anda yakin ingin membatalkan pesanan semen #" + idPesanan + "?\n" +
                    "• Stok sak semen akan otomatis dikembalikan ke inventaris fisik gudang.\n" +
                    "• Status transaksi akan terkunci permanen (Final).",
                    "Konfirmasi Pembatalan Pesanan",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (konfirmasi != JOptionPane.YES_OPTION) {
                return;
            }

            try {
                boolean suksesBatal = pesananService.prosesRefundPesanan(idPesanan);
                if (suksesBatal) {
                    JOptionPane.showMessageDialog(this,
                            "Pesanan #" + idPesanan + " berhasil DIBATALKAN!\n" +
                            "Stok semen telah otomatis dikembalikan ke gudang.",
                            "Pembatalan Sukses",
                            JOptionPane.INFORMATION_MESSAGE);

                    loadDataTabelPengiriman();

                    cmbUbahStatus.setEnabled(false);
                    cmbUbahStatusBayar.setEnabled(false);
                    btnSimpanStatus.setEnabled(false);
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal membatalkan atau pesanan sudah dibatalkan sebelumnya.", "Peringatan", JOptionPane.WARNING_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Gagal membatalkan pesanan: " + ex.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }

        try {
            boolean sukses = pesananService.perbaruiStatusPesananDanBayar(idPesanan, statusKirimBaru, statusBayarBaru);
            if (sukses) {
                JOptionPane.showMessageDialog(this,
                        "Status pesanan #" + idPesanan + " berhasil diperbarui!\n" +
                        "• Status Pengiriman: " + statusKirimBaru + "\n" +
                        "• Status Pembayaran: " + statusBayarBaru,
                        "Pembaruan Berhasil",
                        JOptionPane.INFORMATION_MESSAGE);

                loadDataTabelPengiriman();

                if ("Diterima".equalsIgnoreCase(statusKirimBaru)) {
                    cmbUbahStatus.setEnabled(false);
                    cmbUbahStatusBayar.setEnabled(false);
                    btnSimpanStatus.setEnabled(false);
                }
            } else {
                JOptionPane.showMessageDialog(this, "ID Pesanan #" + idPesanan + " tidak ditemukan!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal memperbarui status: " + ex.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnSimpanStatusActionPerformed

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