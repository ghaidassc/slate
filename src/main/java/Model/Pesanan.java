package Model;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Pesanan {
    private int idPesanan;
    private String tglPesanan;
    private double totalTagihan;
    private String status;
    private int stafId;
    private int pembayaranId;
    private int pelangganId;
    private String namaPelanggan;
    private String statusBayar;

    // Item keranjang yang dikelola langsung di dalam Model Pesanan
    private final List<Object[]> listItems = new ArrayList<>();
    private final List<String> listNamaProduk = new ArrayList<>();

    public Pesanan() {
    }

    public Pesanan(int idPesanan, String tglPesanan, double totalTagihan, String status, int stafId, int pembayaranId, int pelangganId) {
        this.idPesanan = idPesanan;
        this.tglPesanan = tglPesanan;
        this.totalTagihan = totalTagihan;
        this.status = status;
        this.stafId = stafId;
        this.pembayaranId = pembayaranId;
        this.pelangganId = pelangganId;
    }

    /**
     * Logika Model: Memvalidasi stok dan menambahkan item ke pesanan.
     * Mencegah stok fisik menjadi minus saat pemesanan varian yang sama dilakukan berulang.
     */
    public void tambahItem(int idSemen, String namaSemen, int qtyInput, double hargaSatuan, int stokGudang) 
            throws IllegalArgumentException {
        if (qtyInput <= 0) {
            throw new IllegalArgumentException("Jumlah pesanan harus lebih dari 0 sak!");
        }

        int qtySudahAda = getQtySudahDiKeranjang(idSemen);
        int totalAkanDipesan = qtySudahAda + qtyInput;

        // Validasi Akumulasi Stok: mencegah tembus stok minus
        if (totalAkanDipesan > stokGudang) {
            int sisaBisaDipesan = Math.max(0, stokGudang - qtySudahAda);
            throw new IllegalArgumentException("Stok fisik di gudang tidak mencukupi!\n"
                    + "Sisa stok: " + sisaBisaDipesan + " Sak");
        }

        // Jika varian semen yang sama sudah ada di daftar, gabungkan jumlahnya
        boolean sudahAda = false;
        for (int i = 0; i < listItems.size(); i++) {
            Object[] item = listItems.get(i);
            if ((int) item[0] == idSemen) {
                int totalQty = (int) item[1] + qtyInput;
                double subtotal = totalQty * hargaSatuan;
                item[1] = totalQty;
                item[3] = subtotal;
                sudahAda = true;
                break;
            }
        }

        if (!sudahAda) {
            double subtotal = qtyInput * hargaSatuan;
            listItems.add(new Object[]{idSemen, qtyInput, hargaSatuan, subtotal});
            listNamaProduk.add(namaSemen);
        }

        hitungTotalTagihan();
    }

    public int getQtySudahDiKeranjang(int idSemen) {
        for (Object[] item : listItems) {
            if ((int) item[0] == idSemen) {
                return (int) item[1];
            }
        }
        return 0;
    }

    public double hitungTotalTagihan() {
        double total = 0;
        for (Object[] item : listItems) {
            total += (double) item[3];
        }
        this.totalTagihan = total;
        return total;
    }

    public void kosongkan() {
        listItems.clear();
        listNamaProduk.clear();
        this.totalTagihan = 0;
    }

    public List<Object[]> getListItems() {
        return listItems;
    }

    public int getJumlahItem() {
        return listItems.size();
    }

    public String formatHTMLNamaItem() {
        if (listItems.isEmpty()) return "-";
        StringBuilder sb = new StringBuilder("<html><body style='font-family:Segoe UI;'>");
        for (int i = 0; i < listItems.size(); i++) {
            if (i > 0) sb.append("<br>");
            sb.append((i + 1)).append(". ").append(listNamaProduk.get(i))
              .append(" (").append(listItems.get(i)[1]).append(" Sak)");
        }
        sb.append("</body></html>");
        return sb.toString();
    }

    public String formatHTMLHargaItem(DecimalFormat df) {
        if (listItems.isEmpty()) return "Rp 0";
        StringBuilder sb = new StringBuilder("<html><body style='font-family:Segoe UI;'>");
        for (int i = 0; i < listItems.size(); i++) {
            if (i > 0) sb.append("<br>");
            sb.append("Rp ").append(df.format(listItems.get(i)[3]));
        }
        sb.append("</body></html>");
        return sb.toString();
    }

    // Getter dan Setter
    public int getIdPesanan() { return idPesanan; }
    public void setIdPesanan(int idPesanan) { this.idPesanan = idPesanan; }
    public String getTglPesanan() { return tglPesanan; }
    public void setTglPesanan(String tglPesanan) { this.tglPesanan = tglPesanan; }
    public double getTotalTagihan() { return totalTagihan; }
    public void setTotalTagihan(double totalTagihan) { this.totalTagihan = totalTagihan; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getStafId() { return stafId; }
    public void setStafId(int stafId) { this.stafId = stafId; }
    public int getPembayaranId() { return pembayaranId; }
    public void setPembayaranId(int pembayaranId) { this.pembayaranId = pembayaranId; }
    public int getPelangganId() { return pelangganId; }
    public void setPelangganId(int pelangganId) { this.pelangganId = pelangganId; }

    public String getNamaPelanggan() { return namaPelanggan; }
    public void setNamaPelanggan(String namaPelanggan) { this.namaPelanggan = namaPelanggan; }

    public String getStatusBayar() { return statusBayar; }
    public void setStatusBayar(String statusBayar) { this.statusBayar = statusBayar; }
}