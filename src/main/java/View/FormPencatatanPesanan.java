package view;

import Config.Koneksi;
import Model.Pengguna;
import Model.StafPenjualan;
import Util.SesiPengguna;
import java.awt.Color;
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
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class FormPencatatanPesanan extends javax.swing.JFrame {

    private static class ItemPelanggan {
        final int id;
        final String namaUsaha;

        ItemPelanggan(int id, String namaUsaha) {
            this.id = id;
            this.namaUsaha = namaUsaha;
        }

        @Override
        public String toString() {
            return "[ID: " + id + "] " + namaUsaha;
        }
    }

    private static class ItemProduk {
        final int id;
        final String nama;
        final double harga;
        final int stok;
        final String satuan;

        ItemProduk(int id, String nama, double harga, int stok, String satuan) {
            this.id = id;
            this.nama = nama;
            this.harga = harga;
            this.stok = stok;
            this.satuan = satuan;
        }

        @Override
        public String toString() {
            return nama + " (Rp " + formatRupiah(harga) + " / " + satuan + ")";
        }
    }

    private static class ItemKeranjang {
        final ItemProduk produk;
        final int jumlah;
        final double subtotal;

        ItemKeranjang(ItemProduk produk, int jumlah, double subtotal) {
            this.produk = produk;
            this.jumlah = jumlah;
            this.subtotal = subtotal;
        }
    }

    private final ArrayList<ItemKeranjang> listKeranjang = new ArrayList<>();
    private final ArrayList<ItemProduk> cacheProduk = new ArrayList<>();
    private String metodePembayaranPilihan = "Tunai";
    private double akumulasiTotalTagihan = 0;
    private int pelangganTerpilihIndex = 0;

    public FormPencatatanPesanan() {
        initComponents();
        this.setSize(1080, 680);
        this.setPreferredSize(new java.awt.Dimension(1100, 720));
        this.setMinimumSize(new java.awt.Dimension(1100, 720));
        this.pack(); // Mengunci tata letak border layout agar sidebar dan konten tampil bersamaan
        this.setLocationRelativeTo(null);

        // 1. Tampilkan data staf yang login
        try {
            Pengguna currentUser = SesiPengguna.getPengguna();
            if (currentUser instanceof StafPenjualan) {
                StafPenjualan staf = (StafPenjualan) currentUser;
                lblRoleText.setText(staf.getNamaLengkap());
                lblWilayahText.setText(staf.getWilayahPenjualan());
            }
        } catch (Exception ignored) {}

        // 2. Muat data dari database
        loadDataPelangganKeCombo();
        loadDataProdukKeCombo();

        // 3. Pasang logika transaksi
        initLogikaTransaksi();
    }

    private static String formatRupiah(double nominal) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat df = new DecimalFormat("#,###", symbols);
        return df.format(nominal);
    }

    private void loadDataPelangganKeCombo() {
        cmbPelanggan.removeAllItems();
        String sql = "SELECT id_pelanggan, nama_usaha FROM pelanggan ORDER BY id_pelanggan ASC";
        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                cmbPelanggan.addItem(new ItemPelanggan(rs.getInt("id_pelanggan"), rs.getString("nama_usaha")).toString());
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat pelanggan: " + ex.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
        }
        pelangganTerpilihIndex = cmbPelanggan.getSelectedIndex();
    }

    private void loadDataProdukKeCombo() {
        cmbProduk.removeAllItems();
        cacheProduk.clear();

        String sql = "SELECT id_semen, nama_semen, satuan, harga_jual_rp, stok_qty FROM semen ORDER BY id_semen ASC";
        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id_semen");
                String nama = rs.getString("nama_semen");
                String satuan = rs.getString("satuan");
                double harga = rs.getDouble("harga_jual_rp");
                int stok = rs.getInt("stok_qty");

                ItemProduk item = new ItemProduk(id, nama, harga, stok, satuan);
                cacheProduk.add(item);
                cmbProduk.addItem(item.toString());
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat produk semen: " + ex.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
        }

        updateTampilanProdukDanStok();
    }

    private void updateTampilanProdukDanStok() {
        int idx = cmbProduk.getSelectedIndex();
        if (idx >= 0 && idx < cacheProduk.size()) {
            ItemProduk p = cacheProduk.get(idx);
            lblStokGudang.setText("Stok Fisik Gudang Tersedia: " + formatRupiah(p.stok) + " " + p.satuan);
            hitungSubtotalItem();
        }
    }

    private void hitungSubtotalItem() {
        int idx = cmbProduk.getSelectedIndex();
        if (idx < 0 || idx >= cacheProduk.size()) return;

        ItemProduk p = cacheProduk.get(idx);
        String qtyStr = txtJumlah.getText().trim();
        if (qtyStr.isEmpty()) {
            txtSubtotalItem.setText("Rp 0");
            return;
        }

        try {
            int qty = Integer.parseInt(qtyStr);
            if (qty < 0) qty = 0;
            double sub = qty * p.harga;
            txtSubtotalItem.setText("Rp " + formatRupiah(sub));
        } catch (NumberFormatException e) {
            txtSubtotalItem.setText("Rp 0");
        }
    }

    public void kosongkanKeranjang() {
        listKeranjang.clear();
        akumulasiTotalTagihan = 0;
        lblItemNama.setText("- Belum ada item ditambahkan -");
        lblItemHarga.setText("Rp 0");
        lblRingkasanItem.setText("Total Item: 0 Macam Produk Semen");
        lblTotalTagihanBawah.setText("Rp 0");
        lblTotalHarga.setText("Rp 0");
        txtNominalBayar.setText("0");
        txtJumlah.setText("");
        txtSubtotalItem.setText("Rp 0");
    }

    private void refreshTampilanDaftarItem() {
        if (listKeranjang.isEmpty()) {
            kosongkanKeranjang();
            return;
        }

        StringBuilder namaSb = new StringBuilder("<html>");
        StringBuilder hargaSb = new StringBuilder("<html>");
        double total = 0;

        for (int i = 0; i < listKeranjang.size(); i++) {
            ItemKeranjang item = listKeranjang.get(i);
            total += item.subtotal;
            namaSb.append((i + 1)).append(". ").append(item.produk.nama)
                  .append(" (").append(item.jumlah).append(" ").append(item.produk.satuan).append(")<br>");
            hargaSb.append("Rp ").append(formatRupiah(item.subtotal)).append("<br>");
        }
        namaSb.append("</html>");
        hargaSb.append("</html>");

        lblItemNama.setText(namaSb.toString());
        lblItemHarga.setText(hargaSb.toString());
        lblRingkasanItem.setText("Total Item: " + listKeranjang.size() + " Macam Produk Semen (Klik untuk Reset)");
        lblTotalTagihanBawah.setText("Rp " + formatRupiah(total));
        lblTotalHarga.setText("Rp " + formatRupiah(total));
        akumulasiTotalTagihan = total;

        if (cmbStatusBayar.getSelectedIndex() == 1) { // Lunas
            txtNominalBayar.setText(String.valueOf((long) total));
        } else {
            txtNominalBayar.setText(String.valueOf((long) (total / 2)));
        }
    }

    private void initLogikaTransaksi() {
        cmbProduk.addActionListener(e -> updateTampilanProdukDanStok());

        txtJumlah.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { hitungSubtotalItem(); }
            @Override
            public void removeUpdate(DocumentEvent e) { hitungSubtotalItem(); }
            @Override
            public void changedUpdate(DocumentEvent e) { hitungSubtotalItem(); }
        });

        // Fitur reset keranjang jika salah input jumlah: klik panel daftar item
        panelDaftarItem.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        panelDaftarItem.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (!listKeranjang.isEmpty()) {
                    int konf = JOptionPane.showConfirmDialog(
                        FormPencatatanPesanan.this,
                        "Apakah Anda ingin mengosongkan/mereset seluruh isi item keranjang ini?",
                        "Reset Item Keranjang",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                    );
                    if (konf == JOptionPane.YES_OPTION) {
                        kosongkanKeranjang();
                    }
                }
            }
        });

        btnTambahItem.addActionListener(e -> {
            int idx = cmbProduk.getSelectedIndex();
            if (idx < 0 || idx >= cacheProduk.size()) {
                JOptionPane.showMessageDialog(this, "Pilih produk semen terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            ItemProduk p = cacheProduk.get(idx);
            String qtyStr = txtJumlah.getText().trim();
            if (qtyStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Masukkan jumlah pesanan!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                int qty = Integer.parseInt(qtyStr);
                if (qty <= 0) {
                    JOptionPane.showMessageDialog(this, "Jumlah pesanan harus lebih dari 0!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (qty > p.stok) {
                    JOptionPane.showMessageDialog(this, "Stok fisik di gudang tidak mencukupi!\nSisa stok: " + p.stok + " " + p.satuan, "Stok Kurang", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                double sub = qty * p.harga;
                listKeranjang.add(new ItemKeranjang(p, qty, sub));
                refreshTampilanDaftarItem();

                txtJumlah.setText("");
                txtSubtotalItem.setText("Rp 0");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Jumlah harus berupa bilangan bulat!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cmbStatusBayar.addActionListener(e -> {
            if (cmbStatusBayar.getSelectedIndex() == 1) {
                txtNominalBayar.setText(String.valueOf((long) akumulasiTotalTagihan));
            } else {
                txtNominalBayar.setText(String.valueOf((long) (akumulasiTotalTagihan / 2)));
            }
        });

        refreshTampilanDaftarItem();
    }

    private void cmbPelangganItemStateChanged(java.awt.event.ItemEvent evt) {
        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            int selectedBaru = cmbPelanggan.getSelectedIndex();
            if (selectedBaru == pelangganTerpilihIndex) return;

            // Jika keranjang sudah terisi, minta konfirmasi untuk reset
            if (!listKeranjang.isEmpty()) {
                int konfirmasi = JOptionPane.showConfirmDialog(
                    this,
                    "Mengganti mitra pemesan akan mengosongkan daftar pesanan saat ini.\nLanjutkan mengganti pelanggan?",
                    "Konfirmasi Ganti Pelanggan",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );

                if (konfirmasi == JOptionPane.YES_OPTION) {
                    kosongkanKeranjang();
                    pelangganTerpilihIndex = selectedBaru;
                } else {
                    // Batalkan pergantian pelanggan
                    java.awt.EventQueue.invokeLater(() -> cmbPelanggan.setSelectedIndex(pelangganTerpilihIndex));
                }
            } else {
                pelangganTerpilihIndex = selectedBaru;
            }
        }
    }

    private void rapikanTampilan() {
        LineBorder borderField = new LineBorder(new Color(226, 232, 240), 1, true);
        EmptyBorder paddingField = new EmptyBorder(0, 10, 0, 10);

        txtJumlah.setBorder(BorderFactory.createCompoundBorder(borderField, paddingField));
        txtSubtotalItem.setBorder(BorderFactory.createCompoundBorder(borderField, paddingField));
        txtNominalBayar.setBorder(BorderFactory.createCompoundBorder(borderField, paddingField));
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

    private void btnMetodeTunaiActionPerformed(java.awt.event.ActionEvent evt) {
        metodePembayaranPilihan = "Tunai";
        btnMetodeTunai.setForeground(new java.awt.Color(234, 88, 12));
        btnMetodeTransfer.setForeground(new java.awt.Color(71, 85, 105));
    }

    private void btnMetodeTransferActionPerformed(java.awt.event.ActionEvent evt) {
        metodePembayaranPilihan = "Transfer"; // ubah dari "Transfer Bank" jadi "Transfer"
        btnMetodeTransfer.setForeground(new java.awt.Color(234, 88, 12));
        btnMetodeTunai.setForeground(new java.awt.Color(71, 85, 105));
    }

    private void btnNavMenuActionPerformed(java.awt.event.ActionEvent evt) {
        new FormMenuStaf().setVisible(true);
        this.dispose();
    }

    private void btnNavPelangganActionPerformed(java.awt.event.ActionEvent evt) {
        new FormKelolaPelanggan().setVisible(true);
        this.dispose();
    }

    private void btnNavStatusActionPerformed(java.awt.event.ActionEvent evt) {
        new FormStatusPengiriman().setVisible(true);
        this.dispose();
    }

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {                                          
        int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin keluar dari sistem?", "Konfirmasi Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            new FormLogin().setVisible(true);
            this.dispose();
        }
    }
    
    private void cmbStatusBayarActionPerformed(java.awt.event.ActionEvent evt) {                                               
        if (cmbStatusBayar.getSelectedIndex() == 1) { // Pilihan: Lunas
            txtNominalBayar.setText(String.valueOf((long) akumulasiTotalTagihan));
        } else { // Pilihan: Belum Lunas
            txtNominalBayar.setText(String.valueOf((long) (akumulasiTotalTagihan / 2)));
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
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
        panelCard1 = new javax.swing.JPanel();
        lblTitleCard1 = new javax.swing.JLabel();
        lblPilihPelanggan = new javax.swing.JLabel();
        cmbPelanggan = new javax.swing.JComboBox<>();
        lblPilihVarian = new javax.swing.JLabel();
        cmbProduk = new javax.swing.JComboBox<>();
        panelBadgeStok = new javax.swing.JPanel();
        lblStokGudang = new javax.swing.JLabel();
        lblJumlah = new javax.swing.JLabel();
        txtJumlah = new javax.swing.JTextField();
        lblSubtotalItem = new javax.swing.JLabel();
        txtSubtotalItem = new javax.swing.JTextField();
        btnTambahItem = new javax.swing.JButton();
        lblHeaderDaftarItem = new javax.swing.JLabel();
        panelDaftarItem = new javax.swing.JPanel();
        lblItemNama = new javax.swing.JLabel();
        lblItemHarga = new javax.swing.JLabel();
        lblRingkasanItem = new javax.swing.JLabel();
        lblTextTotalTagihan = new javax.swing.JLabel();
        lblTotalTagihanBawah = new javax.swing.JLabel();
        panelCard2 = new javax.swing.JPanel();
        lblTitleCard2 = new javax.swing.JLabel();
        panelBoxTotal = new javax.swing.JPanel();
        lblKetTotalBayar = new javax.swing.JLabel();
        lblTotalHarga = new javax.swing.JLabel();
        lblMetodeBayar = new javax.swing.JLabel();
        btnMetodeTunai = new javax.swing.JButton();
        btnMetodeTransfer = new javax.swing.JButton();
        lblStatusBayar = new javax.swing.JLabel();
        cmbStatusBayar = new javax.swing.JComboBox<>();
        lblNominalUang = new javax.swing.JLabel();
        txtNominalBayar = new javax.swing.JTextField();
        btnCatatPesanan = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("SLATE - Pencatatan Pesanan Semen");
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

        btnNavPesanan.setBackground(new java.awt.Color(234, 88, 12));
        btnNavPesanan.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnNavPesanan.setForeground(new java.awt.Color(255, 255, 255));
        btnNavPesanan.setText("Pencatatan Pesanan");
        btnNavPesanan.setBorderPainted(false);
        btnNavPesanan.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNavPesanan.setFocusPainted(false);
        btnNavPesanan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavPesanan.setMargin(new java.awt.Insets(2, 18, 2, 14));

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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnLogout, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );

        getContentPane().add(panelSidebar, java.awt.BorderLayout.WEST);

        panelKonten.setBackground(new java.awt.Color(248, 250, 252));

        lblHeaderTitle.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblHeaderTitle.setForeground(new java.awt.Color(15, 15, 23));
        lblHeaderTitle.setText("Pencatatan Pesanan Semen");

        panelCard1.setBackground(new java.awt.Color(255, 255, 255));
        panelCard1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));

        lblTitleCard1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblTitleCard1.setForeground(new java.awt.Color(15, 15, 23));
        lblTitleCard1.setText("1. Input Mitra dan Produk");

        lblPilihPelanggan.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblPilihPelanggan.setForeground(new java.awt.Color(71, 85, 105));
        lblPilihPelanggan.setText("Pilih ID Pelanggan");

        cmbPelanggan.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "[ID: 11] Toko Bangunan Berkah Mandiri", "[ID: 1] Toko Bangunan Maju Jaya", "[ID: 2] CV Karya Mandiri" }));
        cmbPelanggan.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbPelangganItemStateChanged(evt);
            }
        });

        lblPilihVarian.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblPilihVarian.setForeground(new java.awt.Color(71, 85, 105));
        lblPilihVarian.setText("Pilih Varian Semen");

        cmbProduk.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Semen Sak 50kg (Rp 68.000 / Sak)", "Semen Sak 40kg (Rp 55.000 / Sak)", "Semen Curah (Rp 1.100.000 / Ton)" }));

        panelBadgeStok.setBackground(new java.awt.Color(220, 252, 231));
        panelBadgeStok.setLayout(new java.awt.GridBagLayout());

        lblStokGudang.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblStokGudang.setForeground(new java.awt.Color(22, 101, 52));
        lblStokGudang.setText("Stok Fisik Gudang Tersedia: 10.000 Sak");
        panelBadgeStok.add(lblStokGudang, new java.awt.GridBagConstraints());

        lblJumlah.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblJumlah.setForeground(new java.awt.Color(71, 85, 105));
        lblJumlah.setText("Jumlah Pesanan (Sak/Ton)");

        txtJumlah.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        txtJumlah.setText("250");

        lblSubtotalItem.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblSubtotalItem.setForeground(new java.awt.Color(71, 85, 105));
        lblSubtotalItem.setText("Subtotal Item");

        txtSubtotalItem.setEditable(false);
        txtSubtotalItem.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        txtSubtotalItem.setForeground(new java.awt.Color(234, 88, 12));
        txtSubtotalItem.setText("Rp 17.000.000");

        btnTambahItem.setBackground(new java.awt.Color(30, 37, 43));
        btnTambahItem.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnTambahItem.setForeground(new java.awt.Color(255, 255, 255));
        btnTambahItem.setText("+ Masukkan ke Daftar Item");
        btnTambahItem.setBorderPainted(false);
        btnTambahItem.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnTambahItem.setFocusPainted(false);

        lblHeaderDaftarItem.setFont(new java.awt.Font("Segoe UI", 1, 10)); // NOI18N
        lblHeaderDaftarItem.setForeground(new java.awt.Color(100, 116, 139));
        lblHeaderDaftarItem.setText("DAFTAR ITEM PESANAN:");

        panelDaftarItem.setBackground(new java.awt.Color(248, 249, 250));
        panelDaftarItem.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));

        lblItemNama.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblItemNama.setForeground(new java.awt.Color(15, 15, 23));
        lblItemNama.setText("1. Semen Sak 50kg (250 Sak)");

        lblItemHarga.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblItemHarga.setForeground(new java.awt.Color(234, 88, 12));
        lblItemHarga.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblItemHarga.setText("Rp 17.000.000");

        lblRingkasanItem.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        lblRingkasanItem.setForeground(new java.awt.Color(100, 116, 139));
        lblRingkasanItem.setText("Total Item: 1 Macam Produk Semen");

        lblTextTotalTagihan.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTextTotalTagihan.setForeground(new java.awt.Color(15, 15, 23));
        lblTextTotalTagihan.setText("Total Tagihan:");

        lblTotalTagihanBawah.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblTotalTagihanBawah.setForeground(new java.awt.Color(234, 88, 12));
        lblTotalTagihanBawah.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTotalTagihanBawah.setText("Rp 17.000.000");

        javax.swing.GroupLayout panelDaftarItemLayout = new javax.swing.GroupLayout(panelDaftarItem);
        panelDaftarItem.setLayout(panelDaftarItemLayout);
        panelDaftarItemLayout.setHorizontalGroup(
            panelDaftarItemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDaftarItemLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(panelDaftarItemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelDaftarItemLayout.createSequentialGroup()
                        .addComponent(lblItemNama, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblItemHarga, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelDaftarItemLayout.createSequentialGroup()
                        .addComponent(lblRingkasanItem, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(panelDaftarItemLayout.createSequentialGroup()
                        .addComponent(lblTextTotalTagihan, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblTotalTagihanBawah, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(14, 14, 14))
        );
        panelDaftarItemLayout.setVerticalGroup(
            panelDaftarItemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDaftarItemLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(panelDaftarItemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblItemNama)
                    .addComponent(lblItemHarga))
                .addGap(14, 14, 14)
                .addComponent(lblRingkasanItem)
                .addGap(10, 10, 10)
                .addGroup(panelDaftarItemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTextTotalTagihan)
                    .addComponent(lblTotalTagihanBawah))
                .addContainerGap(79, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelCard1Layout = new javax.swing.GroupLayout(panelCard1);
        panelCard1.setLayout(panelCard1Layout);
        panelCard1Layout.setHorizontalGroup(
            panelCard1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCard1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(panelCard1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblTitleCard1)
                    .addComponent(lblPilihPelanggan)
                    .addComponent(cmbPelanggan, 0, 338, Short.MAX_VALUE)
                    .addComponent(lblPilihVarian)
                    .addComponent(cmbProduk, 0, 338, Short.MAX_VALUE)
                    .addComponent(panelBadgeStok, javax.swing.GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE)
                    .addGroup(panelCard1Layout.createSequentialGroup()
                        .addGroup(panelCard1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblJumlah)
                            .addComponent(txtJumlah, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(panelCard1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblSubtotalItem)
                            .addComponent(txtSubtotalItem, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)))
                    .addComponent(btnTambahItem, javax.swing.GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE)
                    .addComponent(lblHeaderDaftarItem)
                    .addComponent(panelDaftarItem, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        panelCard1Layout.setVerticalGroup(
            panelCard1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCard1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(lblTitleCard1)
                .addGap(15, 15, 15)
                .addComponent(lblPilihPelanggan)
                .addGap(5, 5, 5)
                .addComponent(cmbPelanggan, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(lblPilihVarian)
                .addGap(5, 5, 5)
                .addComponent(cmbProduk, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(panelBadgeStok, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addGroup(panelCard1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblJumlah)
                    .addComponent(lblSubtotalItem))
                .addGap(5, 5, 5)
                .addGroup(panelCard1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtJumlah, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtSubtotalItem, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(14, 14, 14)
                .addComponent(btnTambahItem, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(lblHeaderDaftarItem)
                .addGap(6, 6, 6)
                .addComponent(panelDaftarItem, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(16, 16, 16))
        );

        panelCard2.setBackground(new java.awt.Color(255, 255, 255));
        panelCard2.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));

        lblTitleCard2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblTitleCard2.setForeground(new java.awt.Color(15, 15, 23));
        lblTitleCard2.setText("2. Kalkulasi Tagihan dan Status Bayar");

        panelBoxTotal.setBackground(new java.awt.Color(255, 255, 255));
        panelBoxTotal.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));

        lblKetTotalBayar.setFont(new java.awt.Font("Segoe UI", 1, 10)); // NOI18N
        lblKetTotalBayar.setForeground(new java.awt.Color(100, 116, 139));
        lblKetTotalBayar.setText("TOTAL HARGA YANG HARUS DIBAYAR");

        lblTotalHarga.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblTotalHarga.setForeground(new java.awt.Color(234, 88, 12));
        lblTotalHarga.setText("Rp 17.000.000");

        javax.swing.GroupLayout panelBoxTotalLayout = new javax.swing.GroupLayout(panelBoxTotal);
        panelBoxTotal.setLayout(panelBoxTotalLayout);
        panelBoxTotalLayout.setHorizontalGroup(
            panelBoxTotalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBoxTotalLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(panelBoxTotalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblKetTotalBayar)
                    .addComponent(lblTotalHarga, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelBoxTotalLayout.setVerticalGroup(
            panelBoxTotalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBoxTotalLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(lblKetTotalBayar)
                .addGap(4, 4, 4)
                .addComponent(lblTotalHarga)
                .addContainerGap(13, Short.MAX_VALUE))
        );

        lblMetodeBayar.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblMetodeBayar.setForeground(new java.awt.Color(71, 85, 105));
        lblMetodeBayar.setText("Metode Pembayaran");

        btnMetodeTunai.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnMetodeTunai.setForeground(new java.awt.Color(234, 88, 12));
        btnMetodeTunai.setText("Tunai");
        btnMetodeTunai.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        btnMetodeTunai.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnMetodeTunai.setFocusPainted(false);
        btnMetodeTunai.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMetodeTunaiActionPerformed(evt);
            }
        });

        btnMetodeTransfer.setForeground(new java.awt.Color(71, 85, 105));
        btnMetodeTransfer.setText("Transfer Bank");
        btnMetodeTransfer.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        btnMetodeTransfer.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnMetodeTransfer.setFocusPainted(false);
        btnMetodeTransfer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMetodeTransferActionPerformed(evt);
            }
        });

        lblStatusBayar.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblStatusBayar.setForeground(new java.awt.Color(71, 85, 105));
        lblStatusBayar.setText("Status Pembayaran Saat Ini");

        cmbStatusBayar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        cmbStatusBayar.setForeground(new java.awt.Color(180, 83, 9));
        cmbStatusBayar.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Belum Lunas (Menunggu Pembayaran)", "Lunas" }));
        cmbStatusBayar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbStatusBayarActionPerformed(evt);
            }
        });

        lblNominalUang.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblNominalUang.setForeground(new java.awt.Color(71, 85, 105));
        lblNominalUang.setText("Nominal Uang Masuk (Rp)");

        txtNominalBayar.setText("8.500.000");

        btnCatatPesanan.setBackground(new java.awt.Color(234, 88, 12));
        btnCatatPesanan.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnCatatPesanan.setForeground(new java.awt.Color(255, 255, 255));
        btnCatatPesanan.setText("CATAT PESANAN KE SISTEM");
        btnCatatPesanan.setBorderPainted(false);
        btnCatatPesanan.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCatatPesanan.setFocusPainted(false);
        btnCatatPesanan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCatatPesananActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelCard2Layout = new javax.swing.GroupLayout(panelCard2);
        panelCard2.setLayout(panelCard2Layout);
        panelCard2Layout.setHorizontalGroup(
            panelCard2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCard2Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(panelCard2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblTitleCard2)
                    .addComponent(panelBoxTotal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblMetodeBayar)
                    .addGroup(panelCard2Layout.createSequentialGroup()
                        .addComponent(btnMetodeTunai, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(14, 14, 14)
                        .addComponent(btnMetodeTransfer, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblStatusBayar)
                    .addComponent(cmbStatusBayar, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblNominalUang)
                    .addComponent(txtNominalBayar)
                    .addComponent(btnCatatPesanan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        panelCard2Layout.setVerticalGroup(
            panelCard2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCard2Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(lblTitleCard2)
                .addGap(15, 15, 15)
                .addComponent(panelBoxTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblMetodeBayar)
                .addGap(6, 6, 6)
                .addGroup(panelCard2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnMetodeTunai, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnMetodeTransfer, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(lblStatusBayar)
                .addGap(6, 6, 6)
                .addComponent(cmbStatusBayar, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblNominalUang)
                .addGap(6, 6, 6)
                .addComponent(txtNominalBayar, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 88, Short.MAX_VALUE)
                .addComponent(btnCatatPesanan, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(62, 62, 62))
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
                        .addComponent(panelCard1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20)
                        .addComponent(panelCard2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelKontenLayout.setVerticalGroup(
            panelKontenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelKontenLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(lblHeaderTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15)
                .addGroup(panelKontenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(panelCard1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelCard2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getContentPane().add(panelKonten, java.awt.BorderLayout.CENTER);

        setLocationRelativeTo(null);
    }// </editor-fold>                        

    private void btnCatatPesananActionPerformed(java.awt.event.ActionEvent evt) {
        if (listKeranjang.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Keranjang pesanan masih kosong! Tambahkan item semen terlebih dahulu.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (cmbPelanggan.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Pilih pelanggan pemesan terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Ambil ID Pelanggan
        String teksPelanggan = cmbPelanggan.getSelectedItem().toString();
        int idPelanggan = 1;
        try {
            if (teksPelanggan.contains("[ID: ") && teksPelanggan.contains("]")) {
                int start = teksPelanggan.indexOf("[ID: ") + 5;
                int end = teksPelanggan.indexOf("]");
                idPelanggan = Integer.parseInt(teksPelanggan.substring(start, end));
            }
        } catch (Exception ignored) {}

        // Ambil ID Staf login
        int idStaf = 1;
        try {
            Pengguna currentUser = SesiPengguna.getPengguna();
            if (currentUser != null) {
                idStaf = currentUser.getIdPengguna();
            }
        } catch (Exception ignored) {}

        String statusBayar = cmbStatusBayar.getSelectedIndex() == 1 ? "Lunas" : "Belum Lunas";
        String nominalStr = txtNominalBayar.getText().replace(".", "").replace(",", "").trim();
        double nominalBayar = nominalStr.isEmpty() ? 0 : Double.parseDouble(nominalStr);

        Connection conn = null;
        try {
            conn = Koneksi.getKoneksi();
            conn.setAutoCommit(false); // Transaksi Atomic

            // Matikan sementara pengecekan Foreign Key agar tidak circular dependency saat insert
            try (Statement st = conn.createStatement()) {
                st.execute("SET FOREIGN_KEY_CHECKS = 0");
            }

            // 1. Generate ID Pesanan dan Pembayaran berikutnya
            int nextIdPesanan = 1;
            String sqlMaxP = "SELECT COALESCE(MAX(id_pesanan), 0) + 1 AS next_id FROM pesanan";
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sqlMaxP)) {
                if (rs.next()) nextIdPesanan = rs.getInt("next_id");
            }

            int nextIdBayar = 1;
            String sqlMaxB = "SELECT COALESCE(MAX(id_pembayaran), 0) + 1 AS next_id FROM pembayaran";
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sqlMaxB)) {
                if (rs.next()) nextIdBayar = rs.getInt("next_id");
            }

            // 2. Simpan ke tabel pembayaran
            String sqlBayar = "INSERT INTO pembayaran (id_pembayaran, tgl_bayar, metode_bayar, jumlah_bayar_rp, status, pesanan_id_pesanan) VALUES (?, CURDATE(), ?, ?, ?, ?)";
            try (PreparedStatement psBayar = conn.prepareStatement(sqlBayar)) {
                psBayar.setInt(1, nextIdBayar);
                psBayar.setString(2, metodePembayaranPilihan);
                psBayar.setDouble(3, nominalBayar);
                psBayar.setString(4, statusBayar);
                psBayar.setInt(5, nextIdPesanan);
                psBayar.executeUpdate();
            }

            // 3. Simpan ke tabel pesanan
            String sqlPesanan = "INSERT INTO pesanan (id_pesanan, tgl_pesanan, total_tagihan_rp, status, staf_penjualan_id_pengguna, pembayaran_id_pembayaran, pelanggan_id_pelanggan) VALUES (?, CURDATE(), ?, 'Diproses', ?, ?, ?)";
            try (PreparedStatement psPesanan = conn.prepareStatement(sqlPesanan)) {
                psPesanan.setInt(1, nextIdPesanan);
                psPesanan.setDouble(2, akumulasiTotalTagihan);
                psPesanan.setInt(3, idStaf);
                psPesanan.setInt(4, nextIdBayar);
                psPesanan.setInt(5, idPelanggan);
                psPesanan.executeUpdate();
            }

            // 4. Simpan ke detail_pesanan & potong stok semen
            String sqlDetail = "INSERT INTO detail_pesanan (id_detail, jumlah_qty, harga_satuan_rp, subtotal_rp, pesanan_id_pesanan, semen_id_semen) VALUES (?, ?, ?, ?, ?, ?)";
            String sqlKurangStok = "UPDATE semen SET stok_qty = stok_qty - ? WHERE id_semen = ?";

            int nextIdDetail = 1;
            String sqlMaxD = "SELECT COALESCE(MAX(id_detail), 0) + 1 AS next_id FROM detail_pesanan";
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sqlMaxD)) {
                if (rs.next()) nextIdDetail = rs.getInt("next_id");
            }

            try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail);
                 PreparedStatement psStok = conn.prepareStatement(sqlKurangStok)) {

                for (ItemKeranjang item : listKeranjang) {
                    psDetail.setInt(1, nextIdDetail++);
                    psDetail.setInt(2, item.jumlah);
                    psDetail.setDouble(3, item.produk.harga);
                    psDetail.setDouble(4, item.subtotal);
                    psDetail.setInt(5, nextIdPesanan);
                    psDetail.setInt(6, item.produk.id);
                    psDetail.executeUpdate();

                    psStok.setInt(1, item.jumlah);
                    psStok.setInt(2, item.produk.id);
                    psStok.executeUpdate();
                }
            }

            // Aktifkan kembali pengecekan Foreign Key
            try (Statement st = conn.createStatement()) {
                st.execute("SET FOREIGN_KEY_CHECKS = 1");
            }

            conn.commit(); // Simpan perubahan permanen

            JOptionPane.showMessageDialog(this,
                    "Pesanan semen #" + nextIdPesanan + " berhasil dicatat!\n" +
                    "Total Tagihan: Rp " + formatRupiah(akumulasiTotalTagihan) + "\n" +
                    "Status Bayar: " + statusBayar + " (" + metodePembayaranPilihan + ")\n" +
                    "Stok gudang telah berhasil dipotong.",
                    "Pencatatan Sukses",
                    JOptionPane.INFORMATION_MESSAGE);

            // Bersihkan form input & sinkronisasi data produk terkini
            kosongkanKeranjang();
            loadDataProdukKeCombo();

        } catch (SQLException ex) {
            if (conn != null) {
                try {
                    conn.rollback();
                    try (Statement st = conn.createStatement()) {
                        st.execute("SET FOREIGN_KEY_CHECKS = 1");
                    }
                } catch (SQLException ignored) {}
            }
            JOptionPane.showMessageDialog(this, "Gagal mencatat pesanan: " + ex.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
        }
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new FormPencatatanPesanan().setVisible(true);
        });
    }

    // Variables declaration - do not modify                     
    private javax.swing.JButton btnCatatPesanan;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnMetodeTransfer;
    private javax.swing.JButton btnMetodeTunai;
    private javax.swing.JButton btnNavMenu;
    private javax.swing.JButton btnNavPelanggan;
    private javax.swing.JButton btnNavPesanan;
    private javax.swing.JButton btnNavStatus;
    private javax.swing.JButton btnTambahItem;
    private javax.swing.JComboBox<String> cmbPelanggan;
    private javax.swing.JComboBox<String> cmbProduk;
    private javax.swing.JComboBox<String> cmbStatusBayar;
    private javax.swing.JLabel lblDotStatus;
    private javax.swing.JLabel lblHeaderDaftarItem;
    private javax.swing.JLabel lblHeaderTitle;
    private javax.swing.JLabel lblItemHarga;
    private javax.swing.JLabel lblItemNama;
    private javax.swing.JLabel lblJumlah;
    private javax.swing.JLabel lblKetTotalBayar;
    private javax.swing.JLabel lblLogoSidebar;
    private javax.swing.JLabel lblMetodeBayar;
    private javax.swing.JLabel lblNominalUang;
    private javax.swing.JLabel lblPilihPelanggan;
    private javax.swing.JLabel lblPilihVarian;
    private javax.swing.JLabel lblRingkasanItem;
    private javax.swing.JLabel lblRoleText;
    private javax.swing.JLabel lblStatusBayar;
    private javax.swing.JLabel lblStokGudang;
    private javax.swing.JLabel lblSubtotalItem;
    private javax.swing.JLabel lblTextTotalTagihan;
    private javax.swing.JLabel lblTitleCard1;
    private javax.swing.JLabel lblTitleCard2;
    private javax.swing.JLabel lblTotalHarga;
    private javax.swing.JLabel lblTotalTagihanBawah;
    private javax.swing.JLabel lblWilayahText;
    private javax.swing.JPanel panelBadgeStok;
    private javax.swing.JPanel panelBadgeUser;
    private javax.swing.JPanel panelBoxTotal;
    private javax.swing.JPanel panelCard1;
    private javax.swing.JPanel panelCard2;
    private javax.swing.JPanel panelDaftarItem;
    private javax.swing.JPanel panelKonten;
    private javax.swing.JPanel panelSidebar;
    private javax.swing.JTextField txtJumlah;
    private javax.swing.JTextField txtNominalBayar;
    private javax.swing.JTextField txtSubtotalItem;
    // End of variables declaration                   
}