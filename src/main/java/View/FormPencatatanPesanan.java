package view;

import Model.Pelanggan;
import Model.Pengguna;
import Model.Pesanan;
import Model.StafPenjualan;
import Util.SesiPengguna;
import dao.PelangganDAO;
import dao.PesananDAO;
import dao.PesananDAOInterface;
import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class FormPencatatanPesanan extends javax.swing.JFrame {

    private final PesananDAOInterface pesananDAO;
    private final Pesanan pesananAktif;
    private final List<Object[]> listSemenData;
    private String metodeBayarDipilih = "Tunai";
    private int stokGudangAktif = 0;
    private double hargaSatuanAktif = 0;

    public FormPencatatanPesanan() {
        this.pesananDAO = new PesananDAO();
        this.pesananAktif = new Pesanan();
        this.listSemenData = new ArrayList<>();
        initComponents();

        this.setSize(1120, 700);
        this.setLocationRelativeTo(null);

        rapikanGayaKomponen();
        pasangLogoSidebar();
        tampilkanSesiPengguna();

        muatDaftarMitra();
        muatDaftarSemen();
        pasangListenerSubtotalInput();

        btnTambahItem.addActionListener(this::btnTambahItemActionPerformed);
    }

    private static String formatRupiah(double nominal) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat df = new DecimalFormat("#,###", symbols);
        return df.format(nominal);
    }

    private static DecimalFormat getFormatterRupiah() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        return new DecimalFormat("#,###", symbols);
    }

    private void rapikanGayaKomponen() {
        panelCard1.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true));
        panelCard2.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true));
        panelBoxTotal.setBorder(BorderFactory.createLineBorder(new Color(254, 215, 170), 1, true));
        panelDaftarItem.setBorder(BorderFactory.createLineBorder(new Color(241, 245, 249), 1, true));
        
        btnMetodeTunai.setBackground(new Color(255, 237, 213));
        btnMetodeTunai.setForeground(new Color(234, 88, 12));
        btnMetodeTransfer.setBackground(new Color(248, 250, 252));
        btnMetodeTransfer.setForeground(new Color(100, 116, 139));
    }

    private void tampilkanSesiPengguna() {
        try {
            Pengguna user = SesiPengguna.getPengguna();
            if (user instanceof StafPenjualan) {
                StafPenjualan staf = (StafPenjualan) user;
                lblRoleText.setText(staf.getNamaLengkap());
                lblWilayahText.setText(staf.getWilayahPenjualan() != null ? staf.getWilayahPenjualan() : "Samarinda Ulu");
            } else if (user != null) {
                lblRoleText.setText(user.getNamaLengkap());
                lblWilayahText.setText("Samarinda Ulu");
            } else {
                lblRoleText.setText("Budi Santoso");
                lblWilayahText.setText("Samarinda Ulu");
            }
        } catch (Exception ignored) {}
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
                Image imgResized = imgAsli.getScaledInstance(75, 75, Image.SCALE_SMOOTH);
                lblLogoSidebar.setIcon(new ImageIcon(imgResized));
                lblLogoSidebar.setText("");
            } else {
                lblLogoSidebar.setText("<html><center><span style='color:#FFFFFF; font-size:20px; font-weight:bold;'>SL<span style='color:#EA580C;'>A</span>TE</span></center></html>");
            }
        } catch (Exception ignored) {}
    }

    private void muatDaftarMitra() {
        try {
            cmbPelanggan.removeAllItems();
            List<Pelanggan> list = new PelangganDAO().getAll();
            for (Pelanggan p : list) {
                cmbPelanggan.addItem("[ID: " + p.getIdPelanggan() + "] " + p.getNamaUsaha());
            }
        } catch (SQLException ignored) {}
    }

    private void muatDaftarSemen() {
        try {
            listSemenData.clear();
            cmbProduk.removeAllItems();
            List<Object[]> list = pesananDAO.getAllSemen();
            
            for (Object[] row : list) {
                listSemenData.add(row);
                String nama = (String) row[1];
                double harga = (double) row[2];
                String satuan = (String) row[4];
                cmbProduk.addItem(nama + " (Rp " + formatRupiah(harga) + " / " + satuan + ")");
            }

            if (!listSemenData.isEmpty()) {
                cmbProduk.setSelectedIndex(0);
                perbaruiTampilanStokSemenDipilih();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void perbaruiTampilanStokSemenDipilih() {
        int idx = cmbProduk.getSelectedIndex();
        if (idx >= 0 && idx < listSemenData.size()) {
            Object[] data = listSemenData.get(idx);
            int idSemen = (int) data[0];
            this.hargaSatuanAktif = (double) data[2];

            try {
                this.stokGudangAktif = pesananDAO.getStokSemen(idSemen);
            } catch (SQLException e) {
                this.stokGudangAktif = (int) data[3];
            }

            lblStokGudang.setText("Stok Fisik Gudang Tersedia: " + formatRupiah(stokGudangAktif) + " Sak");
            hitungSubtotalItemInput();
        }
    }

    private void pasangListenerSubtotalInput() {
        cmbProduk.addActionListener(e -> perbaruiTampilanStokSemenDipilih());

        txtJumlah.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { hitungSubtotalItemInput(); }
            @Override public void removeUpdate(DocumentEvent e) { hitungSubtotalItemInput(); }
            @Override public void changedUpdate(DocumentEvent e) { hitungSubtotalItemInput(); }
        });
    }

    private void hitungSubtotalItemInput() {
        String str = txtJumlah.getText().trim();
        if (str.isEmpty()) {
            txtSubtotalItem.setText("Rp 0");
            return;
        }
        try {
            int qty = Integer.parseInt(str);
            txtSubtotalItem.setText("Rp " + formatRupiah(qty * hargaSatuanAktif));
        } catch (NumberFormatException ignored) {
            txtSubtotalItem.setText("Rp 0");
        }
    }

    private int getIdPelangganTerpilih() {
        String str = (String) cmbPelanggan.getSelectedItem();
        if (str != null && str.contains("[ID: ")) {
            int start = str.indexOf("[ID: ") + 5;
            int end = str.indexOf("]", start);
            return Integer.parseInt(str.substring(start, end).trim());
        }
        return 1;
    }

    private void perbaruiTampilanKeranjangUI() {
        lblItemNama.setText(pesananAktif.formatHTMLNamaItem());
        lblItemHarga.setText(pesananAktif.formatHTMLHargaItem(getFormatterRupiah()));
        lblRingkasanItem.setText("Total Item: " + pesananAktif.getJumlahItem() + " Macam Produk Semen");

        double total = pesananAktif.getTotalTagihan();
        lblTotalTagihanBawah.setText("Rp " + formatRupiah(total));
        lblTotalHarga.setText("Rp " + formatRupiah(total));

        if (cmbStatusBayar.getSelectedIndex() == 1) { // Lunas
            txtNominalBayar.setText(formatRupiah(total));
        } else {
            txtNominalBayar.setText(formatRupiah(total / 2));
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

        btnNavPesanan.setBackground(new java.awt.Color(234, 88, 12));
        btnNavPesanan.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnNavPesanan.setForeground(new java.awt.Color(255, 255, 255));
        btnNavPesanan.setText("Pencatatan Pesanan");
        btnNavPesanan.setBorderPainted(false);
        btnNavPesanan.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNavPesanan.setFocusPainted(false);
        btnNavPesanan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavPesanan.setMargin(new java.awt.Insets(2, 16, 2, 14));

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
                    .addComponent(btnNavPesanan, javax.swing.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
                    .addComponent(btnNavStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
        lblHeaderTitle.setText("Pencatatan Pesanan Semen");

        panelCard1.setBackground(new java.awt.Color(255, 255, 255));

        lblTitleCard1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblTitleCard1.setForeground(new java.awt.Color(15, 23, 34));
        lblTitleCard1.setText("1. Input Mitra dan Produk");

        lblPilihPelanggan.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblPilihPelanggan.setForeground(new java.awt.Color(71, 85, 105));
        lblPilihPelanggan.setText("Pilih ID Pelanggan (Mitra)");

        cmbPelanggan.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbPelangganItemStateChanged(evt);
            }
        });

        lblPilihVarian.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblPilihVarian.setForeground(new java.awt.Color(71, 85, 105));
        lblPilihVarian.setText("Pilih Varian Semen");

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

        lblSubtotalItem.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblSubtotalItem.setForeground(new java.awt.Color(71, 85, 105));
        lblSubtotalItem.setText("Subtotal Item");

        txtSubtotalItem.setEditable(false);
        txtSubtotalItem.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        txtSubtotalItem.setForeground(new java.awt.Color(234, 88, 12));
        txtSubtotalItem.setText("Rp 0");

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

        lblItemNama.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblItemNama.setForeground(new java.awt.Color(15, 23, 34));
        lblItemNama.setText("-");

        lblItemHarga.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblItemHarga.setForeground(new java.awt.Color(234, 88, 12));
        lblItemHarga.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblItemHarga.setText("Rp 0");

        lblRingkasanItem.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        lblRingkasanItem.setForeground(new java.awt.Color(100, 116, 139));
        lblRingkasanItem.setText("Total Item: 0 Macam Produk Semen");

        lblTextTotalTagihan.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTextTotalTagihan.setForeground(new java.awt.Color(15, 23, 34));
        lblTextTotalTagihan.setText("Total Tagihan:");

        lblTotalTagihanBawah.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblTotalTagihanBawah.setForeground(new java.awt.Color(234, 88, 12));
        lblTotalTagihanBawah.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTotalTagihanBawah.setText("Rp 0");

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
                    .addComponent(lblRingkasanItem, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                .addGap(12, 12, 12)
                .addComponent(lblRingkasanItem)
                .addGap(10, 10, 10)
                .addGroup(panelDaftarItemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTextTotalTagihan)
                    .addComponent(lblTotalTagihanBawah))
                .addContainerGap(62, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelCard1Layout = new javax.swing.GroupLayout(panelCard1);
        panelCard1.setLayout(panelCard1Layout);
        panelCard1Layout.setHorizontalGroup(
            panelCard1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCard1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(panelCard1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblTitleCard1)
                    .addComponent(lblPilihPelanggan)
                    .addComponent(cmbPelanggan, 0, 335, Short.MAX_VALUE)
                    .addComponent(lblPilihVarian)
                    .addComponent(cmbProduk, 0, 335, Short.MAX_VALUE)
                    .addComponent(panelBadgeStok, javax.swing.GroupLayout.DEFAULT_SIZE, 335, Short.MAX_VALUE)
                    .addGroup(panelCard1Layout.createSequentialGroup()
                        .addGroup(panelCard1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblJumlah)
                            .addComponent(txtJumlah, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(15, 15, 15)
                        .addGroup(panelCard1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblSubtotalItem)
                            .addComponent(txtSubtotalItem, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)))
                    .addComponent(btnTambahItem, javax.swing.GroupLayout.DEFAULT_SIZE, 335, Short.MAX_VALUE)
                    .addComponent(lblHeaderDaftarItem)
                    .addComponent(panelDaftarItem, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(18, Short.MAX_VALUE))
        );
        panelCard1Layout.setVerticalGroup(
            panelCard1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCard1Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(lblTitleCard1)
                .addGap(14, 14, 14)
                .addComponent(lblPilihPelanggan)
                .addGap(4, 4, 4)
                .addComponent(cmbPelanggan, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(lblPilihVarian)
                .addGap(4, 4, 4)
                .addComponent(cmbProduk, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(panelBadgeStok, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addGroup(panelCard1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblJumlah)
                    .addComponent(lblSubtotalItem))
                .addGap(4, 4, 4)
                .addGroup(panelCard1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtJumlah, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtSubtotalItem, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addComponent(btnTambahItem, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(lblHeaderDaftarItem)
                .addGap(6, 6, 6)
                .addComponent(panelDaftarItem, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(16, 16, 16))
        );

        panelCard2.setBackground(new java.awt.Color(255, 255, 255));

        lblTitleCard2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblTitleCard2.setForeground(new java.awt.Color(15, 23, 34));
        lblTitleCard2.setText("2. Kalkulasi Tagihan dan Status Bayar");

        panelBoxTotal.setBackground(new java.awt.Color(255, 255, 255));

        lblKetTotalBayar.setFont(new java.awt.Font("Segoe UI", 1, 10)); // NOI18N
        lblKetTotalBayar.setForeground(new java.awt.Color(100, 116, 139));
        lblKetTotalBayar.setText("TOTAL HARGA YANG HARUS DIBAYAR");

        lblTotalHarga.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblTotalHarga.setForeground(new java.awt.Color(234, 88, 12));
        lblTotalHarga.setText("Rp 0");

        javax.swing.GroupLayout panelBoxTotalLayout = new javax.swing.GroupLayout(panelBoxTotal);
        panelBoxTotal.setLayout(panelBoxTotalLayout);
        panelBoxTotalLayout.setHorizontalGroup(
            panelBoxTotalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBoxTotalLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(panelBoxTotalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblKetTotalBayar)
                    .addComponent(lblTotalHarga, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(29, Short.MAX_VALUE))
        );
        panelBoxTotalLayout.setVerticalGroup(
            panelBoxTotalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBoxTotalLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(lblKetTotalBayar)
                .addGap(4, 4, 4)
                .addComponent(lblTotalHarga)
                .addContainerGap(12, Short.MAX_VALUE))
        );

        lblMetodeBayar.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblMetodeBayar.setForeground(new java.awt.Color(71, 85, 105));
        lblMetodeBayar.setText("Metode Pembayaran");

        btnMetodeTunai.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnMetodeTunai.setForeground(new java.awt.Color(234, 88, 12));
        btnMetodeTunai.setText("Tunai");
        btnMetodeTunai.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnMetodeTunai.setFocusPainted(false);
        btnMetodeTunai.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMetodeTunaiActionPerformed(evt);
            }
        });

        btnMetodeTransfer.setForeground(new java.awt.Color(71, 85, 105));
        btnMetodeTransfer.setText("Transfer Bank");
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
        lblNominalUang.setText("Nominal Uang Masuk / DP (Rp)");

        txtNominalBayar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        txtNominalBayar.setText("0");

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
                .addGap(18, 18, 18)
                .addGroup(panelCard2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblTitleCard2)
                    .addComponent(panelBoxTotal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblMetodeBayar)
                    .addGroup(panelCard2Layout.createSequentialGroup()
                        .addComponent(btnMetodeTunai, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(15, 15, 15)
                        .addComponent(btnMetodeTransfer, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE))
                    .addComponent(lblStatusBayar)
                    .addComponent(cmbStatusBayar, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblNominalUang)
                    .addComponent(txtNominalBayar)
                    .addComponent(btnCatatPesanan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(18, Short.MAX_VALUE))
        );
        panelCard2Layout.setVerticalGroup(
            panelCard2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCard2Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(lblTitleCard2)
                .addGap(14, 14, 14)
                .addComponent(panelBoxTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(lblMetodeBayar)
                .addGap(6, 6, 6)
                .addGroup(panelCard2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnMetodeTunai, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnMetodeTransfer, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(16, 16, 16)
                .addComponent(lblStatusBayar)
                .addGap(6, 6, 6)
                .addComponent(cmbStatusBayar, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(lblNominalUang)
                .addGap(6, 6, 6)
                .addComponent(txtNominalBayar, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 90, Short.MAX_VALUE)
                .addComponent(btnCatatPesanan, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
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
                        .addComponent(panelCard1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(16, 16, 16)
                        .addComponent(panelCard2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(25, Short.MAX_VALUE))
        );
        panelKontenLayout.setVerticalGroup(
            panelKontenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelKontenLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(lblHeaderTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(panelKontenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(panelCard1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelCard2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

    private void cmbPelangganItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbPelangganItemStateChanged
    }//GEN-LAST:event_cmbPelangganItemStateChanged

    private void btnMetodeTunaiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMetodeTunaiActionPerformed
        metodeBayarDipilih = "Tunai";
        btnMetodeTunai.setBackground(new Color(255, 237, 213));
        btnMetodeTunai.setForeground(new Color(234, 88, 12));
        btnMetodeTransfer.setBackground(new Color(248, 250, 252));
        btnMetodeTransfer.setForeground(new Color(100, 116, 139));
    }//GEN-LAST:event_btnMetodeTunaiActionPerformed

    private void btnMetodeTransferActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMetodeTransferActionPerformed
        metodeBayarDipilih = "Transfer Bank";
        btnMetodeTransfer.setBackground(new Color(255, 237, 213));
        btnMetodeTransfer.setForeground(new Color(234, 88, 12));
        btnMetodeTunai.setBackground(new Color(248, 250, 252));
        btnMetodeTunai.setForeground(new Color(100, 116, 139));
    }//GEN-LAST:event_btnMetodeTransferActionPerformed

    private void cmbStatusBayarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbStatusBayarActionPerformed
        double total = pesananAktif.getTotalTagihan();
        if (cmbStatusBayar.getSelectedIndex() == 1) { // Lunas
            txtNominalBayar.setText(formatRupiah(total));
            txtNominalBayar.setEditable(false);
        } else { // Belum Lunas
            txtNominalBayar.setText(formatRupiah(total > 0 ? (total / 2) : 0));
            txtNominalBayar.setEditable(true);
        }
    }//GEN-LAST:event_cmbStatusBayarActionPerformed

    private void btnTambahItemActionPerformed(java.awt.event.ActionEvent evt) {
        int idx = cmbProduk.getSelectedIndex();
        if (idx < 0 || idx >= listSemenData.size()) {
            JOptionPane.showMessageDialog(this, "Pilih varian semen terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int qtyInput;
        try {
            qtyInput = Integer.parseInt(txtJumlah.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Jumlah pesanan harus berupa angka bulat!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            txtJumlah.requestFocus();
            return;
        }

        Object[] dataSemen = listSemenData.get(idx);
        int idSemen = (int) dataSemen[0];
        String namaSemen = (String) dataSemen[1];
        double hargaSatuan = (double) dataSemen[2];

        try {
            int stokRealtime = pesananDAO.getStokSemen(idSemen);
            this.stokGudangAktif = stokRealtime;

            // Model Pesanan memeriksa akumulasi stok dan menggabungkan item
            pesananAktif.tambahItem(idSemen, namaSemen, qtyInput, hargaSatuan, stokRealtime);

            perbaruiTampilanKeranjangUI();
            txtJumlah.setText("");
            txtSubtotalItem.setText("Rp 0");

        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Stok Kurang", JOptionPane.WARNING_MESSAGE);
            txtJumlah.requestFocus();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal mengambil data stok gudang: " + e.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void btnCatatPesananActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCatatPesananActionPerformed
        if (pesananAktif.getListItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Daftar item pesanan masih kosong! Silakan tambahkan item terlebih dahulu.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idPelanggan = getIdPelangganTerpilih();
        int idStaf = 1;
        try {
            Pengguna u = SesiPengguna.getPengguna();
            if (u != null) idStaf = u.getIdPengguna();
        } catch (Exception ignored) {}

        String statusBayar = cmbStatusBayar.getSelectedIndex() == 1 ? "Lunas" : "Belum Lunas";
        double totalTagihan = pesananAktif.getTotalTagihan();
        double nominalBayar = 0;
        try {
            String str = txtNominalBayar.getText().trim().replaceAll("[^0-9]", "");
            nominalBayar = str.isEmpty() ? 0 : Double.parseDouble(str);
        } catch (Exception ignored) {}

        try {
            int idPesananBaru = pesananDAO.simpanPesananLengkap(
                idPelanggan, idStaf, metodeBayarDipilih, statusBayar,
                totalTagihan, nominalBayar, pesananAktif.getListItems()
            );

            JOptionPane.showMessageDialog(this,
                "Pesanan semen #" + idPesananBaru + " berhasil dicatat!\n"
                + "Total Tagihan: Rp " + formatRupiah(totalTagihan) + "\n"
                + "Status Bayar: " + statusBayar + " (" + metodeBayarDipilih + ")\n"
                + "Stok gudang telah berhasil dipotong.",
                "Pencatatan Sukses",
                JOptionPane.INFORMATION_MESSAGE);

            pesananAktif.kosongkan();
            perbaruiTampilanKeranjangUI();
            perbaruiTampilanStokSemenDipilih();
            txtJumlah.setText("");
            txtSubtotalItem.setText("Rp 0");

        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Peringatan", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal mencatat pesanan: " + e.getMessage(), "Gagal Transaksi", JOptionPane.ERROR_MESSAGE);
            perbaruiTampilanStokSemenDipilih();
        }
    }//GEN-LAST:event_btnCatatPesananActionPerformed

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new FormPencatatanPesanan().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
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
    // End of variables declaration//GEN-END:variables
}