-- phpMyAdmin SQL Dump
-- version 5.2.0
-- https://www.phpmyadmin.net/
--
-- Host: localhost:3306
-- Generation Time: Oct 30, 2026 at 03:53 AM
-- Server version: 8.0.30
-- PHP Version: 8.1.10

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `slate`
--

-- --------------------------------------------------------

--
-- Table structure for table `admin_gudang`
--

CREATE TABLE `admin_gudang` (
  `id_pengguna` int NOT NULL,
  `shift` varchar(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `admin_gudang`
--

INSERT INTO `admin_gudang` (`id_pengguna`, `shift`) VALUES
(16, 'Pagi'),
(17, 'Siang'),
(18, 'Malam'),
(19, 'Pagi'),
(20, 'Siang'),
(21, 'Malam'),
(22, 'Pagi'),
(23, 'Siang'),
(24, 'Malam'),
(25, 'Pagi'),
(26, 'Siang'),
(27, 'Malam'),
(28, 'Pagi'),
(29, 'Siang'),
(30, 'Malam');

-- --------------------------------------------------------

--
-- Table structure for table `detail_pesanan`
--

CREATE TABLE `detail_pesanan` (
  `id_detail` int NOT NULL,
  `jumlah_qty` int NOT NULL,
  `harga_satuan_rp` decimal(12,2) NOT NULL,
  `subtotal_rp` decimal(14,2) NOT NULL,
  `pesanan_id_pesanan` int NOT NULL,
  `semen_id_semen` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `detail_pesanan`
--

INSERT INTO `detail_pesanan` (`id_detail`, `jumlah_qty`, `harga_satuan_rp`, `subtotal_rp`, `pesanan_id_pesanan`, `semen_id_semen`) VALUES
(1, 500, '55000.00', '27500000.00', 1, 1),
(2, 400, '68000.00', '27200000.00', 2, 2),
(3, 20, '1350000.00', '27000000.00', 3, 3),
(4, 1000, '55000.00', '55000000.00', 4, 1),
(5, 800, '68000.00', '54400000.00', 5, 2),
(6, 35, '1350000.00', '47250000.00', 6, 3),
(7, 300, '55000.00', '16500000.00', 7, 1),
(8, 600, '68000.00', '40800000.00', 8, 2),
(9, 50, '1350000.00', '67500000.00', 9, 3),
(10, 750, '55000.00', '41250000.00', 10, 1),
(11, 250, '68000.00', '17000000.00', 11, 2),
(12, 15, '1350000.00', '20250000.00', 12, 3),
(13, 200, '55000.00', '11000000.00', 13, 1),
(14, 150, '68000.00', '10200000.00', 14, 2),
(15, 40, '1350000.00', '54000000.00', 15, 3);

-- --------------------------------------------------------

--
-- Table structure for table `detail_stok_masuk`
--

CREATE TABLE `detail_stok_masuk` (
  `id_detail_stok` int NOT NULL,
  `batch` varchar(20) DEFAULT NULL,
  `jumlah_qty` int NOT NULL,
  `stok_masuk_id_stok_masuk` int NOT NULL,
  `semen_id_semen` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `detail_stok_masuk`
--

INSERT INTO `detail_stok_masuk` (`id_detail_stok`, `batch`, `jumlah_qty`, `stok_masuk_id_stok_masuk`, `semen_id_semen`) VALUES
(1, 'S40-2608-01', 8000, 1, 1),
(2, 'S50-2608-01', 6000, 2, 2),
(3, 'CRH-2608-01', 3000, 3, 3),
(4, 'S40-2608-02', 7000, 4, 1),
(5, 'S50-2608-02', 5000, 5, 2),
(6, 'CRH-2608-02', 2500, 6, 3),
(7, 'S40-2608-03', 6000, 7, 1),
(8, 'S50-2608-03', 7000, 8, 2),
(9, 'CRH-2608-03', 2000, 9, 3),
(10, 'S40-2608-04', 5000, 10, 1),
(11, 'S50-2608-04', 6000, 11, 2),
(12, 'CRH-2608-04', 3500, 12, 3),
(13, 'S40-2608-05', 8000, 13, 1),
(14, 'S50-2608-05', 5000, 14, 2),
(15, 'CRH-2608-05', 2500, 15, 3);

-- --------------------------------------------------------

--
-- Table structure for table `pelanggan`
--

CREATE TABLE `pelanggan` (
  `id_pelanggan` int NOT NULL,
  `nama_usaha` varchar(100) NOT NULL,
  `jenis_pelanggan` varchar(50) NOT NULL,
  `alamat_kirim` varchar(100) NOT NULL,
  `no_telepon` varchar(15) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `pelanggan`
--

INSERT INTO `pelanggan` (`id_pelanggan`, `nama_usaha`, `jenis_pelanggan`, `alamat_kirim`, `no_telepon`) VALUES
(1, 'Toko Bangunan Maju Jaya', 'Toko Bangunan', 'Jl. Juanda No. 12, Samarinda', '081250010001'),
(2, 'CV Karya Mandiri', 'Kontraktor', 'Jl. Pangeran Antasari No. 45, Samarinda', '081250010002'),
(3, 'Toko Sinar Abadi', 'Toko Bangunan', 'Jl. AW Syahranie No. 27, Samarinda', '081250010003'),
(4, 'PT Borneo Konstruksi', 'Kontraktor', 'Jl. M. Yamin No. 101, Samarinda', '081250010004'),
(5, 'UD Sumber Rezeki', 'Toko Bangunan', 'Jl. Sentosa No. 33, Samarinda', '081250010005'),
(6, 'CV Bumi Perkasa', 'Kontraktor', 'Jl. Ir. H. Juanda No. 76, Samarinda', '081250010006'),
(7, 'Toko Cahaya Bangunan', 'Toko Bangunan', 'Jl. Gatot Subroto No. 14, Samarinda', '081250010007'),
(8, 'PT Mahakam Bangun Sejahtera', 'Kontraktor', 'Jl. Slamet Riyadi No. 60, Samarinda', '081250010008'),
(9, 'UD Tiga Saudara', 'Toko Bangunan', 'Jl. Kadrie Oening No. 51, Samarinda', '081250010009'),
(10, 'CV Anugerah Teknik', 'Kontraktor', 'Jl. Pelita No. 9, Samarinda', '081250010010'),
(11, 'Toko Bangunan Berkah Mandiri', 'Toko Bangunan', 'Jl. Dr. Soetomo No. 8, Samarinda', '081250010011'),
(12, 'PT Kaltim Graha Utama', 'Kontraktor', 'Jl. Lambung Mangkurat No. 19, Samarinda', '081250010012'),
(13, 'Toko Material Jaya Abadi', 'Toko Bangunan', 'Jl. KH. Wahid Hasyim No. 22, Samarinda', '081250010013'),
(14, 'CV Dinamika Karya', 'Kontraktor', 'Jl. Cendana No. 5, Samarinda', '081250010014'),
(15, 'UD Borneo Material', 'Toko Bangunan', 'Jl. Wolter Monginsidi No. 3, Samarinda', '081250010015'),
(16, 'PT Bangun Persada', 'Kontraktor', 'Jl. Pahlawan No. 10, Samarinda', '081250010016');

-- --------------------------------------------------------

--
-- Table structure for table `pembayaran`
--

CREATE TABLE `pembayaran` (
  `id_pembayaran` int NOT NULL,
  `tgl_bayar` date NOT NULL,
  `metode_bayar` varchar(10) NOT NULL,
  `jumlah_bayar_rp` decimal(14,2) NOT NULL,
  `status` varchar(20) NOT NULL,
  `tgl_refund` date DEFAULT NULL,
  `pesanan_id_pesanan` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `pembayaran`
--

INSERT INTO `pembayaran` (`id_pembayaran`, `tgl_bayar`, `metode_bayar`, `jumlah_bayar_rp`, `status`, `tgl_refund`, `pesanan_id_pesanan`) VALUES
(1, '2026-09-01', 'Transfer', '27500000.00', 'Lunas', NULL, 1),
(2, '2026-09-03', 'Tunai', '27200000.00', 'Lunas', NULL, 2),
(3, '2026-09-05', 'Transfer', '27000000.00', 'Lunas', NULL, 3),
(4, '2026-09-08', 'Transfer', '55000000.00', 'Lunas', NULL, 4),
(5, '2026-09-10', 'Transfer', '54400000.00', 'Lunas', NULL, 5),
(6, '2026-09-12', 'Transfer', '47250000.00', 'Lunas', NULL, 6),
(7, '2026-09-15', 'Tunai', '16500000.00', 'Lunas', NULL, 7),
(8, '2026-09-18', 'Transfer', '40800000.00', 'Lunas', NULL, 8),
(9, '2026-09-24', 'Transfer', '67500000.00', 'Lunas', NULL, 9),
(10, '2026-09-27', 'Tunai', '41250000.00', 'Lunas', NULL, 10),
(11, '2026-10-01', 'Tunai', '8500000.00', 'Belum Lunas', NULL, 11),
(12, '2026-10-03', 'Transfer', '20250000.00', 'Lunas', NULL, 12),
(13, '2026-10-05', 'Transfer', '11000000.00', 'Lunas', '2026-10-07', 13),
(14, '2026-10-08', 'Tunai', '10200000.00', 'Lunas', '2026-10-10', 14),
(15, '2026-10-12', 'Transfer', '27000000.00', 'Belum Lunas', NULL, 15);

-- --------------------------------------------------------

--
-- Table structure for table `pengguna`
--

CREATE TABLE `pengguna` (
  `id_pengguna` int NOT NULL,
  `nama_pengguna` varchar(50) NOT NULL,
  `sandi_pengguna` varchar(50) NOT NULL,
  `nama_lengkap` varchar(100) NOT NULL,
  `role` varchar(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `pengguna`
--

INSERT INTO `pengguna` (`id_pengguna`, `nama_pengguna`, `sandi_pengguna`, `nama_lengkap`, `role`) VALUES
(1, 'budi.santoso', 'staf_pass_01', 'Budi Santoso', 'staf_penjualan'),
(2, 'siti.aminah', 'staf_pass_02', 'Siti Aminah', 'staf_penjualan'),
(3, 'agus.prasetyo', 'staf_pass_03', 'Agus Prasetyo', 'staf_penjualan'),
(4, 'dewi.lestari', 'staf_pass_04', 'Dewi Lestari', 'staf_penjualan'),
(5, 'rudi.hartono', 'staf_pass_05', 'Rudi Hartono', 'staf_penjualan'),
(6, 'rina.wulandari', 'staf_pass_06', 'Rina Wulandari', 'staf_penjualan'),
(7, 'andi.wijaya', 'staf_pass_07', 'Andi Wijaya', 'staf_penjualan'),
(8, 'maya.sari', 'staf_pass_08', 'Maya Sari', 'staf_penjualan'),
(9, 'hendra.gunawan', 'staf_pass_09', 'Hendra Gunawan', 'staf_penjualan'),
(10, 'lina.marlina', 'staf_pass_10', 'Lina Marlina', 'staf_penjualan'),
(11, 'fajar.nugroho', 'staf_pass_11', 'Fajar Nugroho', 'staf_penjualan'),
(12, 'nurul.hidayah', 'staf_pass_12', 'Nurul Hidayah', 'staf_penjualan'),
(13, 'yoga.pratama', 'staf_pass_13', 'Yoga Pratama', 'staf_penjualan'),
(14, 'putri.ayu', 'staf_pass_14', 'Putri Ayu Lestari', 'staf_penjualan'),
(15, 'dimas.saputra', 'staf_pass_15', 'Dimas Saputra', 'staf_penjualan'),
(16, 'joko.susilo', 'admin_pass_01', 'Joko Susilo', 'admin_gudang'),
(17, 'wati.rahayu', 'admin_pass_02', 'Wati Rahayu', 'admin_gudang'),
(18, 'bambang.irawan', 'admin_pass_03', 'Bambang Irawan', 'admin_gudang'),
(19, 'sri.mulyani', 'admin_pass_04', 'Sri Mulyani', 'admin_gudang'),
(20, 'eko.yulianto', 'admin_pass_05', 'Eko Yulianto', 'admin_gudang'),
(21, 'tuti.handayani', 'admin_pass_06', 'Tuti Handayani', 'admin_gudang'),
(22, 'slamet.riyadi', 'admin_pass_07', 'Slamet Riyadi', 'admin_gudang'),
(23, 'ani.suryani', 'admin_pass_08', 'Ani Suryani', 'admin_gudang'),
(24, 'rizal.fauzi', 'admin_pass_09', 'Rizal Fauzi', 'admin_gudang'),
(25, 'yuni.astuti', 'admin_pass_10', 'Yuni Astuti', 'admin_gudang'),
(26, 'doni.kurniawan', 'admin_pass_11', 'Doni Kurniawan', 'admin_gudang'),
(27, 'lia.permata', 'admin_pass_12', 'Lia Permata', 'admin_gudang'),
(28, 'arif.rahman', 'admin_pass_13', 'Arif Rahman', 'admin_gudang'),
(29, 'mega.safitri', 'admin_pass_14', 'Mega Safitri', 'admin_gudang'),
(30, 'candra.wijaya', 'admin_pass_15', 'Candra Wijaya', 'admin_gudang');

-- --------------------------------------------------------

--
-- Table structure for table `pesanan`
--

CREATE TABLE `pesanan` (
  `id_pesanan` int NOT NULL,
  `tgl_pesanan` date NOT NULL,
  `total_tagihan_rp` decimal(14,2) NOT NULL,
  `status` varchar(20) NOT NULL,
  `staf_penjualan_id_pengguna` int NOT NULL,
  `pembayaran_id_pembayaran` int NOT NULL,
  `pelanggan_id_pelanggan` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `pesanan`
--

INSERT INTO `pesanan` (`id_pesanan`, `tgl_pesanan`, `total_tagihan_rp`, `status`, `staf_penjualan_id_pengguna`, `pembayaran_id_pembayaran`, `pelanggan_id_pelanggan`) VALUES
(1, '2026-09-01', '27500000.00', 'Diterima', 1, 1, 1),
(2, '2026-09-03', '27200000.00', 'Diterima', 2, 2, 2),
(3, '2026-09-05', '27000000.00', 'Diterima', 3, 3, 3),
(4, '2026-09-08', '55000000.00', 'Diterima', 4, 4, 4),
(5, '2026-09-10', '54400000.00', 'Diterima', 5, 5, 5),
(6, '2026-09-12', '47250000.00', 'Diterima', 6, 6, 6),
(7, '2026-09-15', '16500000.00', 'Diterima', 7, 7, 7),
(8, '2026-09-18', '40800000.00', 'Diterima', 8, 8, 8),
(9, '2026-09-24', '67500000.00', 'Dikirim', 9, 9, 9),
(10, '2026-09-27', '41250000.00', 'Dikirim', 10, 10, 10),
(11, '2026-10-01', '17000000.00', 'Dikirim', 11, 11, 11),
(12, '2026-10-03', '20250000.00', 'Dikirim', 12, 12, 12),
(13, '2026-10-05', '11000000.00', 'Refund', 13, 13, 13),
(14, '2026-10-08', '10200000.00', 'Refund', 14, 14, 14),
(15, '2026-10-12', '54000000.00', 'Diproses', 15, 15, 15);

-- --------------------------------------------------------

--
-- Table structure for table `semen`
--

CREATE TABLE `semen` (
  `id_semen` int NOT NULL,
  `nama_semen` varchar(50) NOT NULL,
  `satuan` varchar(10) NOT NULL,
  `harga_jual_rp` decimal(12,2) NOT NULL,
  `stok_qty` int NOT NULL,
  `batas_aman` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `semen`
--

INSERT INTO `semen` (`id_semen`, `nama_semen`, `satuan`, `harga_jual_rp`, `stok_qty`, `batas_aman`) VALUES
(1, 'Semen Sak 40kg', 'Sak', '56000.00', 12000, 4000),
(2, 'Semen Sak 50kg', 'Sak', '70000.00', 10000, 4000),
(3, 'Semen Curah', 'Ton', '1400000.00', 8500, 3500);

-- --------------------------------------------------------

--
-- Table structure for table `staf_penjualan`
--

CREATE TABLE `staf_penjualan` (
  `id_pengguna` int NOT NULL,
  `wilayah_penjualan` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `staf_penjualan`
--

INSERT INTO `staf_penjualan` (`id_pengguna`, `wilayah_penjualan`) VALUES
(1, 'Samarinda Ulu'),
(2, 'Samarinda Ilir'),
(3, 'Samarinda Seberang'),
(4, 'Samarinda Utara'),
(5, 'Sungai Kunjang'),
(6, 'Palaran'),
(7, 'Loa Janan'),
(8, 'Sambutan'),
(9, 'Sungai Pinang'),
(10, 'Anggana'),
(11, 'Muara Jawa'),
(12, 'Tenggarong'),
(13, 'Balikpapan Utara'),
(14, 'Balikpapan Selatan'),
(15, 'Bontang');

-- --------------------------------------------------------

--
-- Table structure for table `stok_masuk`
--

CREATE TABLE `stok_masuk` (
  `id_stok_masuk` int NOT NULL,
  `tgl_masuk` date NOT NULL,
  `semen_id_semen` int NOT NULL,
  `admin_gudang_id_pengguna` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `stok_masuk`
--

INSERT INTO `stok_masuk` (`id_stok_masuk`, `tgl_masuk`, `semen_id_semen`, `admin_gudang_id_pengguna`) VALUES
(1, '2026-08-01', 1, 16),
(2, '2026-08-03', 2, 17),
(3, '2026-08-05', 3, 18),
(4, '2026-08-08', 1, 19),
(5, '2026-08-10', 2, 20),
(6, '2026-08-12', 3, 21),
(7, '2026-08-15', 1, 22),
(8, '2026-08-17', 2, 23),
(9, '2026-08-19', 3, 24),
(10, '2026-08-22', 1, 25),
(11, '2026-08-24', 2, 26),
(12, '2026-08-26', 3, 27),
(13, '2026-08-28', 1, 28),
(14, '2026-08-30', 2, 29),
(15, '2026-08-31', 3, 30);

-- --------------------------------------------------------

--
-- Stand-in structure for view `view_laporan_penjualan`
-- (See below for the actual view)
--
CREATE TABLE `view_laporan_penjualan` (
);

-- --------------------------------------------------------

--
-- Structure for view `view_laporan_penjualan`
--
DROP TABLE IF EXISTS `view_laporan_penjualan`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `view_laporan_penjualan`  AS SELECT `p`.`id_pesanan` AS `id_pesanan`, `p`.`tgl_pesanan` AS `tgl_pesanan`, `pl`.`nama_pelanggan` AS `nama_pelanggan`, `pl`.`jenis_pelanggan` AS `jenis_pelanggan`, `sp`.`wilayah_penjualan` AS `wilayah_penjualan`, `p`.`total_tagihan` AS `total_tagihan`, `p`.`status` AS `status_pengiriman`, `pb`.`metode_bayar` AS `metode_bayar`, `pb`.`status` AS `status_pembayaran` FROM (((`pesanan` `p` join `pelanggan` `pl` on((`p`.`pelanggan_id_pelanggan` = `pl`.`id_pelanggan`))) join `pembayaran` `pb` on((`p`.`pembayaran_id_pembayaran` = `pb`.`id_pembayaran`))) join `staf_penjualan` `sp` on((`p`.`staf_penjualan_id_pengguna` = `sp`.`id_pengguna`)))  ;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `admin_gudang`
--
ALTER TABLE `admin_gudang`
  ADD PRIMARY KEY (`id_pengguna`);

--
-- Indexes for table `detail_pesanan`
--
ALTER TABLE `detail_pesanan`
  ADD PRIMARY KEY (`id_detail`),
  ADD KEY `detail_pesanan_pesanan_FK` (`pesanan_id_pesanan`),
  ADD KEY `detail_pesanan_semen_FK` (`semen_id_semen`);

--
-- Indexes for table `detail_stok_masuk`
--
ALTER TABLE `detail_stok_masuk`
  ADD PRIMARY KEY (`id_detail_stok`),
  ADD KEY `detail_stok_masuk_stok_masuk_FK` (`stok_masuk_id_stok_masuk`),
  ADD KEY `detail_stok_masuk_semen_FK` (`semen_id_semen`);

--
-- Indexes for table `pelanggan`
--
ALTER TABLE `pelanggan`
  ADD PRIMARY KEY (`id_pelanggan`);

--
-- Indexes for table `pembayaran`
--
ALTER TABLE `pembayaran`
  ADD PRIMARY KEY (`id_pembayaran`),
  ADD UNIQUE KEY `pembayaran__IDX` (`pesanan_id_pesanan`);

--
-- Indexes for table `pengguna`
--
ALTER TABLE `pengguna`
  ADD PRIMARY KEY (`id_pengguna`);

--
-- Indexes for table `pesanan`
--
ALTER TABLE `pesanan`
  ADD PRIMARY KEY (`id_pesanan`),
  ADD UNIQUE KEY `pesanan__IDX` (`pembayaran_id_pembayaran`),
  ADD KEY `pesanan_staf_penjualan_FK` (`staf_penjualan_id_pengguna`),
  ADD KEY `pesanan_pelanggan_FK` (`pelanggan_id_pelanggan`);

--
-- Indexes for table `semen`
--
ALTER TABLE `semen`
  ADD PRIMARY KEY (`id_semen`);

--
-- Indexes for table `staf_penjualan`
--
ALTER TABLE `staf_penjualan`
  ADD PRIMARY KEY (`id_pengguna`);

--
-- Indexes for table `stok_masuk`
--
ALTER TABLE `stok_masuk`
  ADD PRIMARY KEY (`id_stok_masuk`),
  ADD KEY `stok_masuk_admin_gudang_FK` (`admin_gudang_id_pengguna`),
  ADD KEY `stok_masuk_semen_FK` (`semen_id_semen`);

--
-- Constraints for dumped tables
--

--
-- Constraints for table `admin_gudang`
--
ALTER TABLE `admin_gudang`
  ADD CONSTRAINT `admin_gudang_pengguna_FK` FOREIGN KEY (`id_pengguna`) REFERENCES `pengguna` (`id_pengguna`);

--
-- Constraints for table `detail_pesanan`
--
ALTER TABLE `detail_pesanan`
  ADD CONSTRAINT `detail_pesanan_pesanan_FK` FOREIGN KEY (`pesanan_id_pesanan`) REFERENCES `pesanan` (`id_pesanan`),
  ADD CONSTRAINT `detail_pesanan_semen_FK` FOREIGN KEY (`semen_id_semen`) REFERENCES `semen` (`id_semen`);

--
-- Constraints for table `detail_stok_masuk`
--
ALTER TABLE `detail_stok_masuk`
  ADD CONSTRAINT `detail_stok_masuk_semen_FK` FOREIGN KEY (`semen_id_semen`) REFERENCES `semen` (`id_semen`),
  ADD CONSTRAINT `detail_stok_masuk_stok_masuk_FK` FOREIGN KEY (`stok_masuk_id_stok_masuk`) REFERENCES `stok_masuk` (`id_stok_masuk`);

--
-- Constraints for table `pembayaran`
--
ALTER TABLE `pembayaran`
  ADD CONSTRAINT `pembayaran_pesanan_FK` FOREIGN KEY (`pesanan_id_pesanan`) REFERENCES `pesanan` (`id_pesanan`);

--
-- Constraints for table `pesanan`
--
ALTER TABLE `pesanan`
  ADD CONSTRAINT `pesanan_pelanggan_FK` FOREIGN KEY (`pelanggan_id_pelanggan`) REFERENCES `pelanggan` (`id_pelanggan`),
  ADD CONSTRAINT `pesanan_pembayaran_FK` FOREIGN KEY (`pembayaran_id_pembayaran`) REFERENCES `pembayaran` (`id_pembayaran`),
  ADD CONSTRAINT `pesanan_staf_penjualan_FK` FOREIGN KEY (`staf_penjualan_id_pengguna`) REFERENCES `staf_penjualan` (`id_pengguna`);

--
-- Constraints for table `staf_penjualan`
--
ALTER TABLE `staf_penjualan`
  ADD CONSTRAINT `staf_penjualan_pengguna_FK` FOREIGN KEY (`id_pengguna`) REFERENCES `pengguna` (`id_pengguna`);

--
-- Constraints for table `stok_masuk`
--
ALTER TABLE `stok_masuk`
  ADD CONSTRAINT `stok_masuk_admin_gudang_FK` FOREIGN KEY (`admin_gudang_id_pengguna`) REFERENCES `admin_gudang` (`id_pengguna`),
  ADD CONSTRAINT `stok_masuk_semen_FK` FOREIGN KEY (`semen_id_semen`) REFERENCES `semen` (`id_semen`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
