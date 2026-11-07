package view;

import Model.Pelanggan;
import Model.Pengguna;
import Model.StafPenjualan;
import Util.SesiPengguna;
import dao.PelangganDAO;
import dao.PelangganDAOInterface;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class FormKelolaPelanggan extends javax.swing.JFrame {

    private final PelangganDAOInterface pelangganDAO;
    private DefaultTableModel modelTabel;
    private int selectedId = -1;
    private static final String PLACEHOLDER_CARI = "Cari nama toko / kontraktor...";

    public FormKelolaPelanggan() {
        this.pelangganDAO = new PelangganDAO();
        initComponents();

        panelSidebar.setPreferredSize(new Dimension(240, 680));
        panelSidebar.setMinimumSize(new Dimension(240, 680));
        this.setSize(1120, 700);
        this.setPreferredSize(new Dimension(1120, 700));
        this.setMinimumSize(new Dimension(1120, 700));
        this.setLocationRelativeTo(null);

        rapikanKomponenForm();
        pasangPlaceholderCari();
        pasangLogoSidebar();

        try {
            Pengguna currentUser = SesiPengguna.getPengguna();
            if (currentUser instanceof StafPenjualan) {
                StafPenjualan staf = (StafPenjualan) currentUser;
                lblRoleText.setText(staf.getNamaLengkap());
                lblWilayahText.setText(staf.getWilayahPenjualan());
            }
        } catch (Exception ignored) {}

        modelTabel = (DefaultTableModel) tblPelanggan.getModel();
        desainTabelModern();
        kosongkanForm();
        muatDataPelanggan("", "Semua Jenis");
    }

    private void pasangPlaceholderCari() {
        txtCari.setText(PLACEHOLDER_CARI);
        txtCari.setForeground(new Color(148, 163, 184));

        txtCari.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtCari.getText().equals(PLACEHOLDER_CARI)) {
                    txtCari.setText("");
                    txtCari.setForeground(new Color(15, 23, 42));
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (txtCari.getText().trim().isEmpty()) {
                    txtCari.setText(PLACEHOLDER_CARI);
                    txtCari.setForeground(new Color(148, 163, 184));
                }
            }
        });
    }

    private void rapikanKomponenForm() {
        LineBorder borderCard = new LineBorder(new Color(222, 229, 237), 1, true);
        panelCardForm.setBorder(borderCard);
        panelCardTabel.setBorder(borderCard);

        LineBorder borderField = new LineBorder(new Color(226, 232, 240), 1, true);
        EmptyBorder paddingField = new EmptyBorder(0, 10, 0, 10);
        
        txtIdPelanggan.setBorder(BorderFactory.createCompoundBorder(borderField, paddingField));
        txtNamaUsaha.setBorder(BorderFactory.createCompoundBorder(borderField, paddingField));
        txtTelepon.setBorder(BorderFactory.createCompoundBorder(borderField, paddingField));
        txtCari.setBorder(BorderFactory.createCompoundBorder(borderField, paddingField));
        jScrollPaneAlamat.setBorder(borderField);
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
        JTableHeader header = tblPelanggan.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(new Color(71, 85, 105));
        header.setPreferredSize(new Dimension(header.getWidth(), 42));
        header.setReorderingAllowed(false);

        tblPelanggan.getColumnModel().getColumn(0).setPreferredWidth(45);
        tblPelanggan.getColumnModel().getColumn(1).setPreferredWidth(190);
        tblPelanggan.getColumnModel().getColumn(2).setPreferredWidth(100);
        tblPelanggan.getColumnModel().getColumn(3).setPreferredWidth(110);
        
        tblPelanggan.getColumnModel().getColumn(4).setMinWidth(0);
        tblPelanggan.getColumnModel().getColumn(4).setMaxWidth(0);
        tblPelanggan.getColumnModel().getColumn(4).setWidth(0);

        tblPelanggan.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                setForeground(new Color(15, 23, 42));
                return c;
            }
        });

        tblPelanggan.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                String nama = value != null ? value.toString() : "";
                
                Object alamatObj = table.getModel().getValueAt(row, 4);
                String alamat = (alamatObj != null && !alamatObj.toString().trim().isEmpty()) ? alamatObj.toString() : "-";

                lbl.setText("<html><body style='font-family:Segoe UI; padding-left:4px;'>" + 
                            "<div style='color:#0F172A; font-weight:600; font-size:11px; line-height:120%;'>" + nama + "</div>" + 
                            "<div style='color:#64748B; font-weight:normal; font-size:9.5px; margin-top:2px;'>" + alamat + "</div>" + 
                            "</body></html>");
                lbl.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
                return lbl;
            }
        });

        tblPelanggan.getColumnModel().getColumn(2).setCellRenderer(new BadgeJenisRenderer());

        tblPelanggan.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                lbl.setForeground(new Color(100, 116, 139));
                if (value != null) {
                    String str = value.toString().trim();
                    if (str.length() > 5) {
                        lbl.setText("..." + str.substring(str.length() - 5));
                    } else {
                        lbl.setText(str);
                    }
                }
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
                return lbl;
            }
        });
    }

    private static class BadgeJenisRenderer extends DefaultTableCellRenderer {
        private String textNilai = "";
        private Color warnaBg = new Color(224, 242, 254);
        private Color warnaTeks = new Color(3, 105, 161);

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            textNilai = (value != null) ? value.toString().trim() : "";
            if (textNilai.toLowerCase().contains("toko")) {
                textNilai = "Toko";
                warnaBg = new Color(224, 242, 254);
                warnaTeks = new Color(3, 105, 161);
            } else {
                textNilai = "Kontraktor";
                warnaBg = new Color(254, 243, 199);
                warnaTeks = new Color(180, 83, 9);
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
            g2.fillRoundRect(x, y, lebarPill, tinggiPill, 10, 10);

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

    public void muatDataPelanggan(String keyword, String filterJenis) {
        if (PLACEHOLDER_CARI.equalsIgnoreCase(keyword)) {
            keyword = "";
        }

        modelTabel.setRowCount(0);
        try {
            List<Pelanggan> list = pelangganDAO.searchWithFilter(keyword, filterJenis);
            for (Pelanggan p : list) {
                modelTabel.addRow(new Object[]{
                    p.getIdPelanggan(),
                    p.getNamaUsaha(),
                    p.getJenisPelanggan(),
                    p.getAlamat(),
                    p.getNomorTelepon()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data pelanggan: " + e.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void kosongkanForm() {
        selectedId = -1;
        try {
            txtIdPelanggan.setText(String.valueOf(pelangganDAO.getNextIdPelanggan()));
        } catch (SQLException e) {
            txtIdPelanggan.setText("1");
        }
        txtNamaUsaha.setText("");
        cmbJenisPelanggan.setSelectedIndex(0);
        txtTelepon.setText("");
        txtAlamat.setText("");
        tblPelanggan.clearSelection();
        btnSimpan.setEnabled(true);
        btnUbah.setEnabled(false);
        btnHapus.setEnabled(false);
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
        lblJudulHalaman = new javax.swing.JLabel();
        panelCardForm = new javax.swing.JPanel();
        lblJudulForm = new javax.swing.JLabel();
        lblIdPelanggan = new javax.swing.JLabel();
        txtIdPelanggan = new javax.swing.JTextField();
        lblNamaUsaha = new javax.swing.JLabel();
        txtNamaUsaha = new javax.swing.JTextField();
        lblJenisPelanggan = new javax.swing.JLabel();
        cmbJenisPelanggan = new javax.swing.JComboBox<>();
        lblTelepon = new javax.swing.JLabel();
        txtTelepon = new javax.swing.JTextField();
        lblAlamat = new javax.swing.JLabel();
        jScrollPaneAlamat = new javax.swing.JScrollPane();
        txtAlamat = new javax.swing.JTextArea();
        btnSimpan = new javax.swing.JButton();
        btnUbah = new javax.swing.JButton();
        btnHapus = new javax.swing.JButton();
        btnKosongkan = new javax.swing.JButton();
        panelCardTabel = new javax.swing.JPanel();
        txtCari = new javax.swing.JTextField();
        cmbFilterJenis = new javax.swing.JComboBox<>();
        btnCariData = new javax.swing.JButton();
        jScrollPaneTabel = new javax.swing.JScrollPane();
        tblPelanggan = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Menu Staf Penjualan (Kelola Pelanggan)");
        setMinimumSize(new java.awt.Dimension(1120, 700));
        setResizable(false);

        panelSidebar.setBackground(new java.awt.Color(22, 27, 34));
        panelSidebar.setMaximumSize(new java.awt.Dimension(240, 32767));
        panelSidebar.setMinimumSize(new java.awt.Dimension(240, 680));
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

        btnNavPelanggan.setBackground(new java.awt.Color(234, 88, 12));
        btnNavPelanggan.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnNavPelanggan.setForeground(new java.awt.Color(255, 255, 255));
        btnNavPelanggan.setText("Kelola Pelanggan");
        btnNavPelanggan.setBorderPainted(false);
        btnNavPelanggan.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNavPelanggan.setFocusPainted(false);
        btnNavPelanggan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavPelanggan.setMargin(new java.awt.Insets(2, 18, 2, 14));

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

        btnNavStatus.setBackground(new java.awt.Color(22, 27, 34));
        btnNavStatus.setForeground(new java.awt.Color(134, 148, 160));
        btnNavStatus.setText("PSPP");
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

        panelKonten.setBackground(new java.awt.Color(255, 255, 255));

        lblJudulHalaman.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblJudulHalaman.setForeground(new java.awt.Color(15, 23, 23));
        lblJudulHalaman.setText("Kelola Data Pelanggan");

        panelCardForm.setBackground(new java.awt.Color(255, 255, 255));

        lblJudulForm.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblJudulForm.setForeground(new java.awt.Color(15, 23, 23));
        lblJudulForm.setText("Form Data Mitra");

        lblIdPelanggan.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblIdPelanggan.setForeground(new java.awt.Color(39, 49, 58));
        lblIdPelanggan.setText("ID Pelanggan");

        txtIdPelanggan.setEditable(false);
        txtIdPelanggan.setBackground(new java.awt.Color(248, 250, 251));

        lblNamaUsaha.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblNamaUsaha.setForeground(new java.awt.Color(39, 49, 58));
        lblNamaUsaha.setText("Nama Usaha (Toko / Kontraktor)");

        lblJenisPelanggan.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblJenisPelanggan.setForeground(new java.awt.Color(39, 49, 58));
        lblJenisPelanggan.setText("Jenis Pelanggan");

        cmbJenisPelanggan.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Toko Bangunan", "Kontraktor" }));

        lblTelepon.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblTelepon.setForeground(new java.awt.Color(39, 49, 58));
        lblTelepon.setText("No. Telepon Aktif");

        lblAlamat.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblAlamat.setForeground(new java.awt.Color(39, 49, 58));
        lblAlamat.setText("Alamat Kirim Muatan Semen");

        txtAlamat.setColumns(20);
        txtAlamat.setLineWrap(true);
        txtAlamat.setRows(3);
        txtAlamat.setWrapStyleWord(true);
        jScrollPaneAlamat.setViewportView(txtAlamat);

        btnSimpan.setBackground(new java.awt.Color(234, 88, 12));
        btnSimpan.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnSimpan.setForeground(new java.awt.Color(255, 255, 255));
        btnSimpan.setText("Simpan");
        btnSimpan.setBorderPainted(false);
        btnSimpan.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnSimpan.setFocusPainted(false);
        btnSimpan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSimpanActionPerformed(evt);
            }
        });

        btnUbah.setBackground(new java.awt.Color(30, 37, 43));
        btnUbah.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnUbah.setForeground(new java.awt.Color(255, 255, 255));
        btnUbah.setText("Ubah");
        btnUbah.setBorderPainted(false);
        btnUbah.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnUbah.setFocusPainted(false);
        btnUbah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUbahActionPerformed(evt);
            }
        });

        btnHapus.setBackground(new java.awt.Color(254, 220, 219));
        btnHapus.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnHapus.setForeground(new java.awt.Color(220, 38, 38));
        btnHapus.setText("Hapus");
        btnHapus.setBorderPainted(false);
        btnHapus.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnHapus.setFocusPainted(false);
        btnHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHapusActionPerformed(evt);
            }
        });

        btnKosongkan.setBackground(new java.awt.Color(235, 241, 247));
        btnKosongkan.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnKosongkan.setForeground(new java.awt.Color(59, 70, 88));
        btnKosongkan.setText("Kosongkan Form");
        btnKosongkan.setBorderPainted(false);
        btnKosongkan.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnKosongkan.setFocusPainted(false);
        btnKosongkan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnKosongkanActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelCardFormLayout = new javax.swing.GroupLayout(panelCardForm);
        panelCardForm.setLayout(panelCardFormLayout);
        panelCardFormLayout.setHorizontalGroup(
            panelCardFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCardFormLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(panelCardFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblJudulForm)
                    .addComponent(lblIdPelanggan)
                    .addComponent(txtIdPelanggan, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
                    .addComponent(lblNamaUsaha)
                    .addComponent(txtNamaUsaha, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
                    .addComponent(lblJenisPelanggan)
                    .addComponent(cmbJenisPelanggan, 0, 280, Short.MAX_VALUE)
                    .addComponent(lblTelepon)
                    .addComponent(txtTelepon, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
                    .addComponent(lblAlamat)
                    .addComponent(jScrollPaneAlamat, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
                    .addGroup(panelCardFormLayout.createSequentialGroup()
                        .addComponent(btnSimpan, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addComponent(btnUbah, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addComponent(btnHapus, javax.swing.GroupLayout.DEFAULT_SIZE, 88, Short.MAX_VALUE))
                    .addComponent(btnKosongkan, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        panelCardFormLayout.setVerticalGroup(
            panelCardFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCardFormLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(lblJudulForm)
                .addGap(14, 14, 14)
                .addComponent(lblIdPelanggan)
                .addGap(4, 4, 4)
                .addComponent(txtIdPelanggan, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(lblNamaUsaha)
                .addGap(4, 4, 4)
                .addComponent(txtNamaUsaha, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(lblJenisPelanggan)
                .addGap(4, 4, 4)
                .addComponent(cmbJenisPelanggan, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(lblTelepon)
                .addGap(4, 4, 4)
                .addComponent(txtTelepon, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(lblAlamat)
                .addGap(4, 4, 4)
                .addComponent(jScrollPaneAlamat, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(panelCardFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSimpan, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnUbah, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnHapus, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addComponent(btnKosongkan, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelCardTabel.setBackground(new java.awt.Color(255, 255, 255));

        txtCari.setText("Cari nama toko / kontraktor...");

        cmbFilterJenis.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Semua Jenis", "Toko", "Kontraktor" }));
        cmbFilterJenis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbFilterJenisActionPerformed(evt);
            }
        });

        btnCariData.setBackground(new java.awt.Color(234, 88, 12));
        btnCariData.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnCariData.setForeground(new java.awt.Color(255, 255, 255));
        btnCariData.setText("Cari Data");
        btnCariData.setBorderPainted(false);
        btnCariData.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCariData.setFocusPainted(false);
        btnCariData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCariDataActionPerformed(evt);
            }
        });

        jScrollPaneTabel.setBackground(new java.awt.Color(255, 255, 255));
        jScrollPaneTabel.setBorder(null);

        tblPelanggan.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "NAMA USAHA", "JENIS", "KONTAK", "ALAMAT"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblPelanggan.setRowHeight(54);
        tblPelanggan.setSelectionBackground(new java.awt.Color(235, 241, 247));
        tblPelanggan.setSelectionForeground(new java.awt.Color(26, 31, 38));
        tblPelanggan.setShowGrid(false);
        tblPelanggan.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblPelangganMouseClicked(evt);
            }
        });
        jScrollPaneTabel.setViewportView(tblPelanggan);

        javax.swing.GroupLayout panelCardTabelLayout = new javax.swing.GroupLayout(panelCardTabel);
        panelCardTabel.setLayout(panelCardTabelLayout);
        panelCardTabelLayout.setHorizontalGroup(
            panelCardTabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCardTabelLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(panelCardTabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(panelCardTabelLayout.createSequentialGroup()
                        .addComponent(txtCari, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addComponent(cmbFilterJenis, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addComponent(btnCariData, javax.swing.GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE))
                    .addComponent(jScrollPaneTabel))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        panelCardTabelLayout.setVerticalGroup(
            panelCardTabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCardTabelLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(panelCardTabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCari, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbFilterJenis, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCariData, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(14, 14, 14)
                .addComponent(jScrollPaneTabel, javax.swing.GroupLayout.DEFAULT_SIZE, 466, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );

        javax.swing.GroupLayout panelKontenLayout = new javax.swing.GroupLayout(panelKonten);
        panelKonten.setLayout(panelKontenLayout);
        panelKontenLayout.setHorizontalGroup(
            panelKontenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelKontenLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(panelKontenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblJudulHalaman, javax.swing.GroupLayout.PREFERRED_SIZE, 600, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelKontenLayout.createSequentialGroup()
                        .addComponent(panelCardForm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20)
                        .addComponent(panelCardTabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(30, Short.MAX_VALUE))
        );
        panelKontenLayout.setVerticalGroup(
            panelKontenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelKontenLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(lblJudulHalaman, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15)
                .addGroup(panelKontenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(panelCardForm, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelCardTabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(35, Short.MAX_VALUE))
        );

        getContentPane().add(panelKonten, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnCariDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCariDataActionPerformed
        String keyword = txtCari.getText().trim();
        String filterJenis = (String) cmbFilterJenis.getSelectedItem();
        muatDataPelanggan(keyword, filterJenis);
    }//GEN-LAST:event_btnCariDataActionPerformed

    private void cmbFilterJenisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbFilterJenisActionPerformed
        String keyword = txtCari.getText().trim();
        String filterJenis = (String) cmbFilterJenis.getSelectedItem();
        muatDataPelanggan(keyword, filterJenis);
    }//GEN-LAST:event_cmbFilterJenisActionPerformed

    private void tblPelangganMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblPelangganMouseClicked
        int row = tblPelanggan.getSelectedRow();
        if (row != -1) {
            selectedId = Integer.parseInt(tblPelanggan.getValueAt(row, 0).toString());
            txtIdPelanggan.setText(String.valueOf(selectedId));
            txtNamaUsaha.setText(tblPelanggan.getValueAt(row, 1).toString());
            
            String jenisStr = tblPelanggan.getValueAt(row, 2).toString();
            if (jenisStr.toLowerCase().contains("toko")) {
                cmbJenisPelanggan.setSelectedItem("Toko Bangunan");
            } else {
                cmbJenisPelanggan.setSelectedItem("Kontraktor");
            }
            
            txtTelepon.setText(tblPelanggan.getModel().getValueAt(row, 3).toString());
            txtAlamat.setText(tblPelanggan.getModel().getValueAt(row, 4).toString());
            
            btnSimpan.setEnabled(false);
            btnUbah.setEnabled(true);
            btnHapus.setEnabled(true);
        }
    }//GEN-LAST:event_tblPelangganMouseClicked

    private void btnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanActionPerformed
        String nama = txtNamaUsaha.getText().trim();
        String jenis = (String) cmbJenisPelanggan.getSelectedItem();
        String telepon = txtTelepon.getText().trim();
        String alamat = txtAlamat.getText().trim();

        if (nama.isEmpty() || telepon.isEmpty() || alamat.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua data formulir wajib diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Pelanggan p = new Pelanggan(0, nama, jenis, alamat, telepon);
            boolean sukses = pelangganDAO.insert(p);
            if (sukses) {
                JOptionPane.showMessageDialog(this, "Data mitra berhasil disimpan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                kosongkanForm();
                muatDataPelanggan("", (String) cmbFilterJenis.getSelectedItem());
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan: " + e.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnSimpanActionPerformed

    private void btnUbahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUbahActionPerformed
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih data dari tabel terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nama = txtNamaUsaha.getText().trim();
        String jenis = (String) cmbJenisPelanggan.getSelectedItem();
        String telepon = txtTelepon.getText().trim();
        String alamat = txtAlamat.getText().trim();

        if (nama.isEmpty() || telepon.isEmpty() || alamat.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua data formulir wajib diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Pelanggan p = new Pelanggan(selectedId, nama, jenis, alamat, telepon);
            boolean sukses = pelangganDAO.update(p);
            if (sukses) {
                JOptionPane.showMessageDialog(this, "Data mitra berhasil diperbarui!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                kosongkanForm();
                muatDataPelanggan("", (String) cmbFilterJenis.getSelectedItem());
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal mengubah: " + e.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnUbahActionPerformed

    private void btnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHapusActionPerformed
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih data dari tabel yang ingin dihapus!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int konfirm = JOptionPane.showConfirmDialog(this, 
                "Apakah Anda yakin ingin menghapus data mitra ini?", 
                "Konfirmasi Hapus", 
                JOptionPane.YES_NO_OPTION);

        if (konfirm == JOptionPane.YES_OPTION) {
            try {
                boolean sukses = pelangganDAO.delete(selectedId);
                if (sukses) {
                    JOptionPane.showMessageDialog(this, "Data mitra berhasil dihapus!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    kosongkanForm();
                    muatDataPelanggan("", (String) cmbFilterJenis.getSelectedItem());
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Data tidak dapat dihapus karena sudah memiliki riwayat transaksi!", "Gagal Hapus", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnHapusActionPerformed

    private void btnKosongkanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnKosongkanActionPerformed
        kosongkanForm();
    }//GEN-LAST:event_btnKosongkanActionPerformed

    private void btnNavMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavMenuActionPerformed
        new FormMenuStaf().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnNavMenuActionPerformed

    private void btnNavPesananActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavPesananActionPerformed
        new FormPencatatanPesanan().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnNavPesananActionPerformed

    private void btnNavStatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavStatusActionPerformed
        new FormStatusPengiriman().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnNavStatusActionPerformed

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
        int konfirm = JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin keluar?", "Konfirmasi Keluar", JOptionPane.YES_NO_OPTION);
        if (konfirm == JOptionPane.YES_OPTION) {
            SesiPengguna.setPengguna(null);
            new FormLogin().setVisible(true);
            this.dispose();
        }
    }//GEN-LAST:event_btnLogoutActionPerformed

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new FormKelolaPelanggan().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCariData;
    private javax.swing.JButton btnHapus;
    private javax.swing.JButton btnKosongkan;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnNavMenu;
    private javax.swing.JButton btnNavPelanggan;
    private javax.swing.JButton btnNavPesanan;
    private javax.swing.JButton btnNavStatus;
    private javax.swing.JButton btnSimpan;
    private javax.swing.JButton btnUbah;
    private javax.swing.JComboBox<String> cmbFilterJenis;
    private javax.swing.JComboBox<String> cmbJenisPelanggan;
    private javax.swing.JScrollPane jScrollPaneAlamat;
    private javax.swing.JScrollPane jScrollPaneTabel;
    private javax.swing.JLabel lblAlamat;
    private javax.swing.JLabel lblDotStatus;
    private javax.swing.JLabel lblIdPelanggan;
    private javax.swing.JLabel lblJenisPelanggan;
    private javax.swing.JLabel lblJudulForm;
    private javax.swing.JLabel lblJudulHalaman;
    private javax.swing.JLabel lblLogoSidebar;
    private javax.swing.JLabel lblNamaUsaha;
    private javax.swing.JLabel lblRoleText;
    private javax.swing.JLabel lblTelepon;
    private javax.swing.JLabel lblWilayahText;
    private javax.swing.JPanel panelBadgeUser;
    private javax.swing.JPanel panelCardForm;
    private javax.swing.JPanel panelCardTabel;
    private javax.swing.JPanel panelKonten;
    private javax.swing.JPanel panelSidebar;
    private javax.swing.JTable tblPelanggan;
    private javax.swing.JTextArea txtAlamat;
    private javax.swing.JTextField txtCari;
    private javax.swing.JTextField txtIdPelanggan;
    private javax.swing.JTextField txtNamaUsaha;
    private javax.swing.JTextField txtTelepon;
    // End of variables declaration//GEN-END:variables
}