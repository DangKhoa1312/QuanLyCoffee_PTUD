package dao;

import java.time.LocalDate;
import java.util.Map;

public interface StatisticDAO {

    /**
     * Lấy doanh thu theo từng ngày trong khoảng [from, to].
     * @return Map<Chuỗi ngày (dd/MM), Tổng doanh thu>
     */
    Map<String, Double> getDoanhThuTheoNgay(LocalDate from, LocalDate to);

    /**
     * Top N món bán chạy nhất trong khoảng [from, to].
     * @return Map<Tên món, Số lượng bán>
     */
    Map<String, Integer> getTopMonBanChay(int top, LocalDate from, LocalDate to);

    /**
     * Doanh thu gom theo loại món (COFFEE, TRA_SUA, ...) trong khoảng [from, to].
     * @return Map<Loại món, Doanh thu>
     */
    Map<String, Double> getDoanhThuTheoLoaiMon(LocalDate from, LocalDate to);

    /**
     * Số đơn hàng và doanh thu theo từng giờ trong ngày (0-23) trong khoảng [from, to].
     * @return Map<Giờ (0-23), Số đơn>
     */
    Map<Integer, Integer> getSoDonTheoGio(LocalDate from, LocalDate to);

    /**
     * Tổng số hóa đơn đã thanh toán trong khoảng [from, to].
     */
    int getSoHoaDon(LocalDate from, LocalDate to);

    /**
     * Tổng doanh thu trong khoảng [from, to].
     */
    double getTongDoanhThu(LocalDate from, LocalDate to);

    // ── Convenience (giữ tương thích cũ) ─────────────────────
    default Map<String, Double> getDoanhThu7NgayQua() {
        return getDoanhThuTheoNgay(LocalDate.now().minusDays(6), LocalDate.now());
    }

    default Map<String, Integer> getTopMonBanChay(int top) {
        return getTopMonBanChay(top, LocalDate.of(2000, 1, 1), LocalDate.now());
    }
}
