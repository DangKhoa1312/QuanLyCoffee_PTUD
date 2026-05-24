package dao.impl;

import connectDB.DatabaseConnection;
import dao.StatisticDAO;

import java.sql.*;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class StatisticDAOImpl implements StatisticDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ── Doanh thu theo ngày ─────────────────────────────────────
    @Override
    public Map<String, Double> getDoanhThuTheoNgay(LocalDate from, LocalDate to) {
        Map<String, Double> result = new LinkedHashMap<>();

        String sql = """
            WITH CTE_Dates AS (
                SELECT CAST(? AS DATE) AS d
                UNION ALL
                SELECT DATEADD(day, 1, d) FROM CTE_Dates WHERE d < CAST(? AS DATE)
            )
            SELECT FORMAT(CTE_Dates.d, 'dd/MM') as Ngay,
                   ISNULL(SUM(hd.tongTienPhaiTra), 0) as DoanhThu
            FROM CTE_Dates
            LEFT JOIN HoaDon hd
                ON CAST(hd.thoiGianThanhToan AS DATE) = CTE_Dates.d
                AND hd.trangThai = 'DA_THANH_TOAN'
            GROUP BY CTE_Dates.d
            ORDER BY CTE_Dates.d ASC
            OPTION (MAXRECURSION 400)
        """;

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("Ngay"), rs.getDouble("DoanhThu"));
                }
            }
        } catch (SQLException e) {
            System.err.println("StatisticDAO.getDoanhThuTheoNgay: " + e.getMessage());
        }
        return result;
    }

    // ── Top món bán chạy (lọc theo ngày) ────────────────────────
    @Override
    public Map<String, Integer> getTopMonBanChay(int top, LocalDate from, LocalDate to) {
        Map<String, Integer> result = new LinkedHashMap<>();

        String sql = """
            SELECT TOP (?) m.tenMon, SUM(ct.soLuong) as TongSL
            FROM ChiTietHoaDon ct
            JOIN HoaDon hd ON ct.maHD = hd.maHD
            JOIN Mon m ON ct.maMon = m.maMon
            WHERE hd.trangThai = 'DA_THANH_TOAN'
              AND CAST(hd.thoiGianThanhToan AS DATE) BETWEEN ? AND ?
            GROUP BY m.tenMon
            ORDER BY TongSL DESC
        """;

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, top);
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("tenMon"), rs.getInt("TongSL"));
                }
            }
        } catch (SQLException e) {
            System.err.println("StatisticDAO.getTopMonBanChay: " + e.getMessage());
        }
        return result;
    }

    // ── Doanh thu theo loại món ─────────────────────────────────
    @Override
    public Map<String, Double> getDoanhThuTheoLoaiMon(LocalDate from, LocalDate to) {
        Map<String, Double> result = new LinkedHashMap<>();

        String sql = """
            SELECT m.loaiMon, SUM(ct.thanhTien) as DoanhThu
            FROM ChiTietHoaDon ct
            JOIN HoaDon hd ON ct.maHD = hd.maHD
            JOIN Mon m ON ct.maMon = m.maMon
            WHERE hd.trangThai = 'DA_THANH_TOAN'
              AND CAST(hd.thoiGianThanhToan AS DATE) BETWEEN ? AND ?
            GROUP BY m.loaiMon
            ORDER BY DoanhThu DESC
        """;

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String loai = rs.getString("loaiMon");
                    // Chuyển enum thành tên đẹp
                    String label = convertLoaiMon(loai);
                    result.put(label, rs.getDouble("DoanhThu"));
                }
            }
        } catch (SQLException e) {
            System.err.println("StatisticDAO.getDoanhThuTheoLoaiMon: " + e.getMessage());
        }
        return result;
    }

    // ── Số đơn theo giờ ─────────────────────────────────────────
    @Override
    public Map<Integer, Integer> getSoDonTheoGio(LocalDate from, LocalDate to) {
        // Khởi tạo 0-23 giờ
        Map<Integer, Integer> result = new LinkedHashMap<>();
        for (int i = 0; i < 24; i++) result.put(i, 0);

        String sql = """
            SELECT DATEPART(HOUR, hd.thoiGianThanhToan) as Gio,
                   COUNT(*) as SoDon
            FROM HoaDon hd
            WHERE hd.trangThai = 'DA_THANH_TOAN'
              AND CAST(hd.thoiGianThanhToan AS DATE) BETWEEN ? AND ?
            GROUP BY DATEPART(HOUR, hd.thoiGianThanhToan)
            ORDER BY Gio
        """;

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getInt("Gio"), rs.getInt("SoDon"));
                }
            }
        } catch (SQLException e) {
            System.err.println("StatisticDAO.getSoDonTheoGio: " + e.getMessage());
        }
        return result;
    }

    // ── Doanh thu theo giờ ──────────────────────────────────────
    @Override
    public Map<Integer, Double> getDoanhThuTheoGio(LocalDate from, LocalDate to) {
        // Khởi tạo 0-23 giờ
        Map<Integer, Double> result = new LinkedHashMap<>();
        for (int i = 0; i < 24; i++) result.put(i, 0.0);

        String sql = """
            SELECT DATEPART(HOUR, hd.thoiGianThanhToan) as Gio,
                   ISNULL(SUM(hd.tongTienPhaiTra), 0) as DoanhThu
            FROM HoaDon hd
            WHERE hd.trangThai = 'DA_THANH_TOAN'
              AND CAST(hd.thoiGianThanhToan AS DATE) BETWEEN ? AND ?
            GROUP BY DATEPART(HOUR, hd.thoiGianThanhToan)
            ORDER BY Gio
        """;

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getInt("Gio"), rs.getDouble("DoanhThu"));
                }
            }
        } catch (SQLException e) {
            System.err.println("StatisticDAO.getDoanhThuTheoGio: " + e.getMessage());
        }
        return result;
    }

    // ── Tổng số hóa đơn ────────────────────────────────────────
    @Override
    public int getSoHoaDon(LocalDate from, LocalDate to) {
        String sql = """
            SELECT COUNT(*) as SoHD
            FROM HoaDon
            WHERE trangThai = 'DA_THANH_TOAN'
              AND CAST(thoiGianThanhToan AS DATE) BETWEEN ? AND ?
        """;

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("SoHD");
            }
        } catch (SQLException e) {
            System.err.println("StatisticDAO.getSoHoaDon: " + e.getMessage());
        }
        return 0;
    }

    // ── Tổng doanh thu ─────────────────────────────────────────
    @Override
    public double getTongDoanhThu(LocalDate from, LocalDate to) {
        String sql = """
            SELECT ISNULL(SUM(tongTienPhaiTra), 0) as TongDT
            FROM HoaDon
            WHERE trangThai = 'DA_THANH_TOAN'
              AND CAST(thoiGianThanhToan AS DATE) BETWEEN ? AND ?
        """;

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("TongDT");
            }
        } catch (SQLException e) {
            System.err.println("StatisticDAO.getTongDoanhThu: " + e.getMessage());
        }
        return 0;
    }

    // ── Helper: convert loaiMon enum → label đẹp ───────────────
    private String convertLoaiMon(String loai) {
        if (loai == null) return "Khác";
        return switch (loai) {
            case "COFFEE"       -> "Cà Phê";
            case "COLD_BREW"    -> "Cold Brew";
            case "MATCHA_CACAO" -> "Matcha & Cacao";
            case "TRA"          -> "Trà";
            case "TRA_SUA"      -> "Trà Sữa";
            case "DA_XAY"       -> "Đá Xay";
            case "NUOC_EP"      -> "Nước Ép";
            case "SODA"         -> "Soda";
            case "YAOURT"       -> "Yaourt";
            case "DO_AN_NHE"    -> "Đồ Ăn Nhẹ";
            default             -> loai;
        };
    }
}
