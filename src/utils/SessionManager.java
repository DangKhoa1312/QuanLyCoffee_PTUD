package utils;

import entity.CaLamViec;
import entity.NhanVien;

/**
 * Quản lý phiên làm việc hiện tại (Singleton static).
 * Lưu thông tin NhanVien đang đăng nhập và CaLamViec đang mở.
 * Được set khi login/mở ca, clear khi logout/đóng ca.
 */
public class SessionManager {

    private static NhanVien   currentUser = null;
    private static CaLamViec  currentCa   = null;

    private SessionManager() {} // Không cho khởi tạo instance

    // ── NhanVien ──────────────────────────────────────────────────────────

    public static void setCurrentUser(NhanVien nv) {
        currentUser = nv;
    }

    public static NhanVien getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean isQuanLy() {
        if (currentUser == null) return false;
        return enums.VaiTro.QUAN_LY.equals(currentUser.getVaiTro()) 
            || enums.VaiTro.ADMIN.equals(currentUser.getVaiTro());
    }

    public static boolean isAdmin() {
        if (currentUser == null) return false;
        return enums.VaiTro.ADMIN.equals(currentUser.getVaiTro());
    }

    // ── CaLamViec ─────────────────────────────────────────────────────────

    public static void setCurrentCa(CaLamViec ca) {
        currentCa = ca;
    }

    public static CaLamViec getCurrentCa() {
        return currentCa;
    }

    public static boolean isCaDangMo() {
        return currentCa != null && currentCa.getMaCa() != null
            && enums.TrangThaiCa.DANG_MO.equals(currentCa.getTrangThai());
    }

    /** Tiện ích lấy maCa đang mở (dùng khi tạo DonHang) */
    public static String getMaCaHienTai() {
        return currentCa != null ? currentCa.getMaCa() : null;
    }

    /** Tiện ích lấy maNV đang đăng nhập */
    public static String getMaNVHienTai() {
        return currentUser != null ? currentUser.getMaNV() : null;
    }

    // ── Clear session ─────────────────────────────────────────────────────

    public static void clear() {
        currentUser = null;
        currentCa   = null;
    }
}
