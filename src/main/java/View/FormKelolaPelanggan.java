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

public class FormKelolaPelanggan extends javax.swing.JFrame {

    private final ArrayList<String> daftarAlamatDb = new ArrayList<>();
    private boolean isModeEdit = false;

    public FormKelolaPelanggan() {
        initComponents();
        this.setSize(1080, 680);
        this.setLocationRelativeTo(null);
        rapikanTampilanTambahan();
        pasangLogoSidebar();

        cmbJenis.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {
            "Toko Bangunan", "Kontraktor"
        }));

        desainTabelDanBadge();

        // Ambil data staf login
        try {
            Pengguna currentUser = SesiPengguna.getPengguna();
            if (currentUser instanceof StafPenjualan) {
                StafPenjualan staf = (StafPenjualan) currentUser;
                lblRoleText.setText(staf.getNamaLengkap());
                lblWilayahText.setText(staf.getWilayahPenjualan());
            }
        } catch (Exception ignored) {}

        // Inisialisasi event & muat data
        loadDataPelanggan("");
        initActionEvents();
        resetFormKeModeBaru();
    }

    private void rapikanTampilanTambahan() {
        LineBorder borderField = new LineBorder(new Color(226, 232, 240), 1, true);
        EmptyBorder paddingField = new EmptyBorder(0, 10, 0, 10);
        
        txtIdPelanggan.setBorder(BorderFactory.createCompoundBorder(borderField, paddingField));
        txtNamaUsaha.setBorder(BorderFactory.createCompoundBorder(borderField, paddingField));
        txtTelepon.setBorder(BorderFactory.createCompoundBorder(borderField, paddingField));
        txtCari.setBorder(BorderFactory.createCompoundBorder(borderField, paddingField));
        txtAlamat.setBorder(new EmptyBorder(6, 8, 6, 8));
        
        panelFormCard.setBorder(borderField);
        panelTabelCard.setBorder(borderField);
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

    // Mengembalikan form ke mode input manual
    private void resetFormKeModeBaru() {
        isModeEdit = false;
        txtIdPelanggan.setEditable(true);
        txtIdPelanggan.setText("");
        txtNamaUsaha.setText("");
        cmbJenis.setSelectedIndex(0);
        txtTelepon.setText("");
        txtAlamat.setText("");
        tblPelanggan.clearSelection();

        lblFormTitle.setText("Form Data Mitra (Tambah Baru)");
        btnSimpan.setEnabled(true);
        btnUbah.setEnabled(false);
        btnHapus.setEnabled(false);
        txtIdPelanggan.requestFocus();
    }

    private void loadDataPelanggan(String keyword) {
        DefaultTableModel model = (DefaultTableModel) tblPelanggan.getModel();
        model.setRowCount(0);
        daftarAlamatDb.clear();

        String sql = "SELECT * FROM pelanggan";
        if (keyword != null && !keyword.trim().isEmpty() && !keyword.equals("Cari nama toko / kontraktor...")) {
            sql += " WHERE nama_usaha LIKE ? OR jenis_pelanggan LIKE ?";
        }
        sql += " ORDER BY id_pelanggan ASC";

        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (keyword != null && !keyword.trim().isEmpty() && !keyword.equals("Cari nama toko / kontraktor...")) {
                ps.setString(1, "%" + keyword.trim() + "%");
                ps.setString(2, "%" + keyword.trim() + "%");
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id_pelanggan");
                String nama = rs.getString("nama_usaha");
                String jenis = rs.getString("jenis_pelanggan");
                String telp = rs.getString("no_telepon");
                String alamat = rs.getString("alamat_kirim");

                daftarAlamatDb.add(alamat != null ? alamat : "");
                model.addRow(new Object[]{id, nama, jenis, telp});
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data pelanggan: " + ex.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initActionEvents() {
        // Klik Baris Tabel -> Masuk Mode Edit & Isi Field Form
        tblPelanggan.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int selectedRow = tblPelanggan.getSelectedRow();
                if (selectedRow != -1) {
                    isModeEdit = true;
                    String idTerpilih = tblPelanggan.getValueAt(selectedRow, 0).toString();
                    
                    // Kunci ID saat mode edit agar tidak diubah sembarangan
                    txtIdPelanggan.setText(idTerpilih);
                    txtIdPelanggan.setEditable(false);
                    
                    txtNamaUsaha.setText(tblPelanggan.getValueAt(selectedRow, 1).toString());
                    
                    String jenis = tblPelanggan.getValueAt(selectedRow, 2).toString();
                    if (jenis.equalsIgnoreCase("Toko") || jenis.equalsIgnoreCase("Toko Bangunan")) {
                        cmbJenis.setSelectedItem("Toko Bangunan");
                    } else {
                        cmbJenis.setSelectedItem("Kontraktor");
                    }

                    txtTelepon.setText(tblPelanggan.getValueAt(selectedRow, 3).toString());
                    
                    if (selectedRow < daftarAlamatDb.size()) {
                        txtAlamat.setText(daftarAlamatDb.get(selectedRow));
                    }

                    lblFormTitle.setText("Form Data Mitra (Edit Data - ID: #" + idTerpilih + ")");
                    btnSimpan.setEnabled(false);
                    btnUbah.setEnabled(true);
                    btnHapus.setEnabled(true);
                }
            }
        });

        // Tombol Simpan (Input Manual ID Pelanggan)
        btnSimpan.addActionListener(e -> {
            if (isModeEdit) {
                JOptionPane.showMessageDialog(this, "Data ini sudah dipilih dari tabel. Klik 'Ubah' untuk menyimpan perubahan atau klik 'Kosongkan Form' untuk menambah data baru!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String idStr = txtIdPelanggan.getText().trim();
            String nama = txtNamaUsaha.getText().trim();
            String jenis = cmbJenis.getSelectedItem().toString();
            String telp = txtTelepon.getText().trim();
            String alamat = txtAlamat.getText().trim();

            if (idStr.isEmpty() || nama.isEmpty() || telp.isEmpty() || alamat.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Mohon lengkapi seluruh isian data mitra (termasuk ID Pelanggan)!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int idInput;
            try {
                idInput = Integer.parseInt(idStr);
                if (idInput <= 0) {
                    JOptionPane.showMessageDialog(this, "ID Pelanggan harus berupa angka positif lebih dari 0!", "Validasi Masukan", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "ID Pelanggan harus berupa angka valid!", "Format Salah", JOptionPane.ERROR_MESSAGE);
                txtIdPelanggan.requestFocus();
                return;
            }

            // Validasi apakah ID Pelanggan sudah ada di database
            String sqlCek = "SELECT id_pelanggan FROM pelanggan WHERE id_pelanggan = ?";
            try (Connection conn = Koneksi.getKoneksi();
                 PreparedStatement psCek = conn.prepareStatement(sqlCek)) {
                psCek.setInt(1, idInput);
                try (ResultSet rsCek = psCek.executeQuery()) {
                    if (rsCek.next()) {
                        JOptionPane.showMessageDialog(this, "ID Pelanggan #" + idInput + " sudah terdaftar di database! Masukkan ID yang berbeda.", "ID Duplikat", JOptionPane.WARNING_MESSAGE);
                        txtIdPelanggan.requestFocus();
                        return;
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Gagal memeriksa ID: " + ex.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Simpan pelanggan baru dengan ID manual
            String sql = "INSERT INTO pelanggan (id_pelanggan, nama_usaha, jenis_pelanggan, no_telepon, alamat_kirim) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = Koneksi.getKoneksi();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idInput);
                ps.setString(2, nama);
                ps.setString(3, jenis);
                ps.setString(4, telp);
                ps.setString(5, alamat);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Pelanggan baru [ID: #" + idInput + "] " + nama + " berhasil ditambahkan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                loadDataPelanggan("");
                resetFormKeModeBaru();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Gagal menambah pelanggan: " + ex.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Tombol Ubah
        btnUbah.addActionListener(e -> {
            String idStr = txtIdPelanggan.getText().trim();
            if (idStr.isEmpty() || !isModeEdit) {
                JOptionPane.showMessageDialog(this, "Pilih data pelanggan dari tabel terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String nama = txtNamaUsaha.getText().trim();
            String jenis = cmbJenis.getSelectedItem().toString();
            String telp = txtTelepon.getText().trim();
            String alamat = txtAlamat.getText().trim();

            if (nama.isEmpty() || telp.isEmpty() || alamat.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Mohon lengkapi semua isian data!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String sql = "UPDATE pelanggan SET nama_usaha = ?, jenis_pelanggan = ?, no_telepon = ?, alamat_kirim = ? WHERE id_pelanggan = ?";
            try (Connection conn = Koneksi.getKoneksi();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, nama);
                ps.setString(2, jenis);
                ps.setString(3, telp);
                ps.setString(4, alamat);
                ps.setInt(5, Integer.parseInt(idStr));
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Data Pelanggan #" + idStr + " berhasil diperbarui!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                loadDataPelanggan("");
                resetFormKeModeBaru();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Gagal mengubah data: " + ex.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Tombol Hapus
        btnHapus.addActionListener(e -> {
            String idStr = txtIdPelanggan.getText().trim();
            if (idStr.isEmpty() || !isModeEdit) {
                JOptionPane.showMessageDialog(this, "Pilih data pelanggan dari tabel terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                this, 
                "Apakah Anda yakin ingin menghapus data pelanggan ID #" + idStr + "?", 
                "Konfirmasi Hapus", 
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            if (confirm == JOptionPane.YES_OPTION) {
                String sql = "DELETE FROM pelanggan WHERE id_pelanggan = ?";
                try (Connection conn = Koneksi.getKoneksi();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, Integer.parseInt(idStr));
                    ps.executeUpdate();

                    JOptionPane.showMessageDialog(this, "Data Pelanggan #" + idStr + " berhasil dihapus!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    loadDataPelanggan("");
                    resetFormKeModeBaru();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Gagal menghapus data (Pelanggan sedang terkait dengan transaksi pesanan): " + ex.getMessage(), "Proteksi Relasi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Fitur Cari
        btnCari.addActionListener(e -> loadDataPelanggan(txtCari.getText().trim()));
        txtCari.addActionListener(e -> loadDataPelanggan(txtCari.getText().trim()));

        txtCari.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (txtCari.getText().equals("Cari nama toko / kontraktor...")) {
                    txtCari.setText("");
                    txtCari.setForeground(new Color(15, 23, 42));
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (txtCari.getText().trim().isEmpty()) {
                    txtCari.setText("Cari nama toko / kontraktor...");
                    txtCari.setForeground(new Color(100, 130, 148));
                    loadDataPelanggan("");
                }
            }
        });
    }

    private void desainTabelDanBadge() {
        tblPelanggan.setRowHeight(68);

        JTableHeader header = tblPelanggan.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(new Color(71, 85, 105));
        header.setPreferredSize(new Dimension(header.getWidth(), 38));

        tblPelanggan.getColumnModel().getColumn(0).setPreferredWidth(32);
        tblPelanggan.getColumnModel().getColumn(1).setPreferredWidth(175);
        tblPelanggan.getColumnModel().getColumn(2).setPreferredWidth(85);
        tblPelanggan.getColumnModel().getColumn(3).setPreferredWidth(95);

        tblPelanggan.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                return c;
            }
        });

        tblPelanggan.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String nama = (value != null) ? value.toString() : "";
                String almt = (row >= 0 && row < daftarAlamatDb.size()) ? daftarAlamatDb.get(row) : "";
                
                setText("<html><body style='padding-left:4px;'>"
                        + "<span style='font-family:Segoe UI; font-weight:bold; font-size:11px; color:#0F172A;'>" + nama + "</span><br>"
                        + "<span style='font-family:Segoe UI; font-size:9.5px; color:#64748B;'>" + almt + "</span>"
                        + "</body></html>");
                setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
                return c;
            }
        });

        tblPelanggan.getColumnModel().getColumn(2).setCellRenderer(new JenisPillRenderer());

        tblPelanggan.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                setForeground(new Color(71, 85, 105));
                setFont(new Font("Segoe UI", Font.PLAIN, 11));
                return c;
            }
        });
    }

    private static class JenisPillRenderer extends DefaultTableCellRenderer {
        private String labelTeks = "";
        private Color warnaBg = Color.LIGHT_GRAY;
        private Color warnaTeks = Color.BLACK;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String val = (value != null) ? value.toString().trim() : "";
            
            if (val.equalsIgnoreCase("Toko Bangunan") || val.equalsIgnoreCase("Toko")) {
                labelTeks = "Toko";
                warnaBg = new Color(224, 242, 254);
                warnaTeks = new Color(3, 105, 161);
            } else if (val.equalsIgnoreCase("Kontraktor")) {
                labelTeks = "Kontraktor";
                warnaBg = new Color(254, 243, 199);
                warnaTeks = new Color(180, 83, 9);
            } else {
                labelTeks = val;
                warnaBg = new Color(241, 245, 249);
                warnaTeks = new Color(71, 85, 105);
            }
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Font fontBadge = new Font("Segoe UI", Font.BOLD, 10);
            g2.setFont(fontBadge);

            int lebarTeks = g2.getFontMetrics().stringWidth(labelTeks);
            int paddingHorisontal = 16;
            int lebarPill = Math.max(54, lebarTeks + paddingHorisontal);
            int tinggiPill = 22;

            int x = (getWidth() - lebarPill) / 2;
            int y = (getHeight() - tinggiPill) / 2;

            g2.setColor(warnaBg);
            g2.fillRoundRect(x, y, lebarPill, tinggiPill, 10, 10);

            g2.setColor(warnaTeks);
            int tinggiFont = g2.getFontMetrics().getAscent();
            int xTeks = x + (lebarPill - lebarTeks) / 2;
            int yTeks = y + (tinggiPill + tinggiFont) / 2 - 3;
            g2.drawString(labelTeks, xTeks, yTeks);

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
        panelFormCard = new javax.swing.JPanel();
        lblFormTitle = new javax.swing.JLabel();
        lblIdPelanggan = new javax.swing.JLabel();
        txtIdPelanggan = new javax.swing.JTextField();
        lblNamaUsaha = new javax.swing.JLabel();
        txtNamaUsaha = new javax.swing.JTextField();
        lblJenisPelanggan = new javax.swing.JLabel();
        cmbJenis = new javax.swing.JComboBox<>();
        lblTelepon = new javax.swing.JLabel();
        txtTelepon = new javax.swing.JTextField();
        lblAlamat = new javax.swing.JLabel();
        jScrollPaneAlamat = new javax.swing.JScrollPane();
        txtAlamat = new javax.swing.JTextArea();
        btnSimpan = new javax.swing.JButton();
        btnUbah = new javax.swing.JButton();
        btnHapus = new javax.swing.JButton();
        btnReset = new javax.swing.JButton();
        panelTabelCard = new javax.swing.JPanel();
        txtCari = new javax.swing.JTextField();
        btnCari = new javax.swing.JButton();
        jScrollPaneTabel = new javax.swing.JScrollPane();
        tblPelanggan = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("SLATE - Kelola Data Pelanggan");
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 199, Short.MAX_VALUE)
                .addComponent(btnLogout, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );

        getContentPane().add(panelSidebar, java.awt.BorderLayout.WEST);

        panelKonten.setBackground(new java.awt.Color(248, 250, 252));

        lblHeaderTitle.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblHeaderTitle.setForeground(new java.awt.Color(15, 15, 23));
        lblHeaderTitle.setText("Kelola Data Pelanggan");

        panelFormCard.setBackground(new java.awt.Color(255, 255, 255));

        lblFormTitle.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblFormTitle.setForeground(new java.awt.Color(15, 15, 23));
        lblFormTitle.setText("Form Data Mitra");

        lblIdPelanggan.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblIdPelanggan.setForeground(new java.awt.Color(71, 85, 105));
        lblIdPelanggan.setText("ID Pelanggan");

        lblNamaUsaha.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblNamaUsaha.setForeground(new java.awt.Color(71, 85, 105));
        lblNamaUsaha.setText("Nama Usaha (Toko / Kontraktor)");

        lblJenisPelanggan.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblJenisPelanggan.setForeground(new java.awt.Color(71, 85, 105));
        lblJenisPelanggan.setText("Jenis Pelanggan");

        lblTelepon.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblTelepon.setForeground(new java.awt.Color(71, 85, 105));
        lblTelepon.setText("No. Telepon Aktif");

        lblAlamat.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblAlamat.setForeground(new java.awt.Color(71, 85, 105));
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

        btnUbah.setBackground(new java.awt.Color(30, 37, 43));
        btnUbah.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnUbah.setForeground(new java.awt.Color(255, 255, 255));
        btnUbah.setText("Ubah");
        btnUbah.setBorderPainted(false);
        btnUbah.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnUbah.setFocusPainted(false);

        btnHapus.setBackground(new java.awt.Color(254, 235, 235));
        btnHapus.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnHapus.setForeground(new java.awt.Color(234, 43, 43));
        btnHapus.setText("Hapus");
        btnHapus.setBorderPainted(false);
        btnHapus.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnHapus.setFocusPainted(false);

        btnReset.setBackground(new java.awt.Color(241, 245, 249));
        btnReset.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnReset.setForeground(new java.awt.Color(71, 85, 105));
        btnReset.setText("Kosongkan Form");
        btnReset.setBorderPainted(false);
        btnReset.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnReset.setFocusPainted(false);
        btnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelFormCardLayout = new javax.swing.GroupLayout(panelFormCard);
        panelFormCard.setLayout(panelFormCardLayout);
        panelFormCardLayout.setHorizontalGroup(
            panelFormCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFormCardLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(panelFormCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblFormTitle)
                    .addComponent(lblIdPelanggan)
                    .addComponent(txtIdPelanggan, javax.swing.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE)
                    .addComponent(lblNamaUsaha)
                    .addComponent(txtNamaUsaha, javax.swing.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE)
                    .addComponent(lblJenisPelanggan)
                    .addComponent(cmbJenis, 0, 305, Short.MAX_VALUE)
                    .addComponent(lblTelepon)
                    .addComponent(txtTelepon, javax.swing.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE)
                    .addComponent(lblAlamat)
                    .addComponent(jScrollPaneAlamat, javax.swing.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE)
                    .addGroup(panelFormCardLayout.createSequentialGroup()
                        .addComponent(btnSimpan, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addComponent(btnUbah, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addComponent(btnHapus, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE))
                    .addComponent(btnReset, javax.swing.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE))
                .addContainerGap(18, Short.MAX_VALUE))
        );
        panelFormCardLayout.setVerticalGroup(
            panelFormCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFormCardLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(lblFormTitle)
                .addGap(15, 15, 15)
                .addComponent(lblIdPelanggan)
                .addGap(4, 4, 4)
                .addComponent(txtIdPelanggan, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(lblNamaUsaha)
                .addGap(4, 4, 4)
                .addComponent(txtNamaUsaha, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(lblJenisPelanggan)
                .addGap(4, 4, 4)
                .addComponent(cmbJenis, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(lblTelepon)
                .addGap(4, 4, 4)
                .addComponent(txtTelepon, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(lblAlamat)
                .addGap(4, 4, 4)
                .addComponent(jScrollPaneAlamat, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addGroup(panelFormCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSimpan, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnUbah, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnHapus, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(23, Short.MAX_VALUE))
        );

        panelTabelCard.setBackground(new java.awt.Color(255, 255, 255));

        txtCari.setForeground(new java.awt.Color(100, 130, 148));
        txtCari.setText("Cari nama toko / kontraktor...");

        btnCari.setBackground(new java.awt.Color(234, 88, 12));
        btnCari.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnCari.setForeground(new java.awt.Color(255, 255, 255));
        btnCari.setText("Cari Data");
        btnCari.setBorderPainted(false);
        btnCari.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCari.setFocusPainted(false);

        jScrollPaneTabel.setBorder(null);

        tblPelanggan.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "NAMA USAHA", "JENIS", "KONTAK"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblPelanggan.setRowHeight(68);
        tblPelanggan.setSelectionBackground(new java.awt.Color(248, 248, 248));
        tblPelanggan.setShowGrid(false);
        jScrollPaneTabel.setViewportView(tblPelanggan);

        javax.swing.GroupLayout panelTabelCardLayout = new javax.swing.GroupLayout(panelTabelCard);
        panelTabelCard.setLayout(panelTabelCardLayout);
        panelTabelCardLayout.setHorizontalGroup(
            panelTabelCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTabelCardLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(panelTabelCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(panelTabelCardLayout.createSequentialGroup()
                        .addComponent(txtCari, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addComponent(btnCari, javax.swing.GroupLayout.DEFAULT_SIZE, 107, Short.MAX_VALUE))
                    .addComponent(jScrollPaneTabel, javax.swing.GroupLayout.DEFAULT_SIZE, 365, Short.MAX_VALUE))
                .addContainerGap(18, Short.MAX_VALUE))
        );
        panelTabelCardLayout.setVerticalGroup(
            panelTabelCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTabelCardLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(panelTabelCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCari, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCari, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addComponent(jScrollPaneTabel, javax.swing.GroupLayout.PREFERRED_SIZE, 460, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(17, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelKontenLayout = new javax.swing.GroupLayout(panelKonten);
        panelKonten.setLayout(panelKontenLayout);
        panelKontenLayout.setHorizontalGroup(
            panelKontenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelKontenLayout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addGroup(panelKontenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblHeaderTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelKontenLayout.createSequentialGroup()
                        .addComponent(panelFormCard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20)
                        .addComponent(panelTabelCard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelKontenLayout.setVerticalGroup(
            panelKontenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelKontenLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(lblHeaderTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15)
                .addGroup(panelKontenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(panelFormCard, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelTabelCard, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

    private void btnNavPesananActionPerformed(java.awt.event.ActionEvent evt) {
        new FormPencatatanPesanan().setVisible(true);
        this.dispose();
    }

    private void btnNavStatusActionPerformed(java.awt.event.ActionEvent evt) {
        new FormStatusPengiriman().setVisible(true);
        this.dispose();
    }

    private void btnResetActionPerformed(java.awt.event.ActionEvent evt) {
        resetFormKeModeBaru();
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
            new FormKelolaPelanggan().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCari;
    private javax.swing.JButton btnHapus;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnNavMenu;
    private javax.swing.JButton btnNavPelanggan;
    private javax.swing.JButton btnNavPesanan;
    private javax.swing.JButton btnNavStatus;
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnSimpan;
    private javax.swing.JButton btnUbah;
    private javax.swing.JComboBox<String> cmbJenis;
    private javax.swing.JScrollPane jScrollPaneAlamat;
    private javax.swing.JScrollPane jScrollPaneTabel;
    private javax.swing.JLabel lblAlamat;
    private javax.swing.JLabel lblDotStatus;
    private javax.swing.JLabel lblFormTitle;
    private javax.swing.JLabel lblHeaderTitle;
    private javax.swing.JLabel lblIdPelanggan;
    private javax.swing.JLabel lblJenisPelanggan;
    private javax.swing.JLabel lblLogoSidebar;
    private javax.swing.JLabel lblNamaUsaha;
    private javax.swing.JLabel lblRoleText;
    private javax.swing.JLabel lblTelepon;
    private javax.swing.JLabel lblWilayahText;
    private javax.swing.JPanel panelBadgeUser;
    private javax.swing.JPanel panelFormCard;
    private javax.swing.JPanel panelKonten;
    private javax.swing.JPanel panelSidebar;
    private javax.swing.JPanel panelTabelCard;
    private javax.swing.JTable tblPelanggan;
    private javax.swing.JTextArea txtAlamat;
    private javax.swing.JTextField txtCari;
    private javax.swing.JTextField txtIdPelanggan;
    private javax.swing.JTextField txtNamaUsaha;
    private javax.swing.JTextField txtTelepon;
    // End of variables declaration//GEN-END:variables
}