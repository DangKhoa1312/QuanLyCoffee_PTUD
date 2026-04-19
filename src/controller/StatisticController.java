package controller;

import dao.StatisticDAO;
import dao.impl.StatisticDAOImpl;

import java.time.LocalDate;
import java.util.Map;

public class StatisticController {

    private final StatisticDAO statDAO;

    public StatisticController() {
        this.statDAO = new StatisticDAOImpl();
    }

    // ── Doanh thu theo ngày (khoảng tùy chọn) ──────────────────
    public Map<String, Double> getDoanhThuTheoNgay(LocalDate from, LocalDate to) {
        return statDAO.getDoanhThuTheoNgay(from, to);
    }

    // ── Top món bán chạy (khoảng tùy chọn) ─────────────────────
    public Map<String, Integer> getTopMonBanChay(int top, LocalDate from, LocalDate to) {
        return statDAO.getTopMonBanChay(top, from, to);
    }

    // ── Doanh thu theo loại món ─────────────────────────────────
    public Map<String, Double> getDoanhThuTheoLoaiMon(LocalDate from, LocalDate to) {
        return statDAO.getDoanhThuTheoLoaiMon(from, to);
    }

    // ── Số đơn theo giờ ─────────────────────────────────────────
    public Map<Integer, Integer> getSoDonTheoGio(LocalDate from, LocalDate to) {
        return statDAO.getSoDonTheoGio(from, to);
    }

    // ── KPI ─────────────────────────────────────────────────────
    public int getSoHoaDon(LocalDate from, LocalDate to) {
        return statDAO.getSoHoaDon(from, to);
    }

    public double getTongDoanhThu(LocalDate from, LocalDate to) {
        return statDAO.getTongDoanhThu(from, to);
    }

    // ── Convenience (giữ tương thích cũ) ────────────────────────
    public Map<String, Double> getDoanhThu7NgayQua() {
        return statDAO.getDoanhThu7NgayQua();
    }

    public Map<String, Integer> getTopMonBanChay(int top) {
        return statDAO.getTopMonBanChay(top);
    }
}
