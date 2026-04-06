package dao.impl;

import connectDB.DatabaseConnection;
import dao.StatisticDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class StatisticDAOImpl implements StatisticDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public Map<String, Double> getDoanhThu7NgayQua() {
        Map<String, Double> result = new LinkedHashMap<>();
        
        String sql = """
            WITH CTE_Dates AS (
                SELECT CAST(GETDATE() - 6 AS DATE) AS d
                UNION ALL
                SELECT DATEADD(day, 1, d) FROM CTE_Dates WHERE d < CAST(GETDATE() AS DATE)
            )
            SELECT FORMAT(CTE_Dates.d, 'dd/MM') as Ngay, ISNULL(SUM(hd.tongTienPhaiTra), 0) as DoanhThu
            FROM CTE_Dates
            LEFT JOIN HoaDon hd ON CAST(hd.thoiGianThanhToan AS DATE) = CTE_Dates.d AND hd.trangThai = 'DA_THANH_TOAN'
            GROUP BY CTE_Dates.d
            ORDER BY CTE_Dates.d ASC
        """;
        
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            while (rs.next()) {
                result.put(rs.getString("Ngay"), rs.getDouble("DoanhThu"));
            }
        } catch (SQLException e) {
            System.err.println("StatisticDAO.getDoanhThu7NgayQua: " + e.getMessage());
        }
        return result;
    }

    @Override
    public Map<String, Integer> getTopMonBanChay(int top) {
        Map<String, Integer> result = new LinkedHashMap<>();
        
        // Đổi từ ChiTietDonHang JOIN DonHang → ChiTietHoaDon JOIN HoaDon
        String sql = """
            SELECT TOP (?) m.tenMon, SUM(ct.soLuong) as TongSL
            FROM ChiTietHoaDon ct
            JOIN HoaDon hd ON ct.maHD = hd.maHD
            JOIN Mon m ON ct.maMon = m.maMon
            WHERE hd.trangThai = 'DA_THANH_TOAN'
            GROUP BY m.tenMon
            ORDER BY TongSL DESC
        """;
        
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, top);
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
}
