package dao;

import dao.base.BaseDAO;
import entity.Ban;
import entity.DatBan;
import enums.TrangThaiDatBan;
import java.time.LocalDateTime;
import java.util.List;

public interface DatBanDAO extends BaseDAO<DatBan, String> {

    // ── Truy vấn ──────────────────────────────────────────────────────────
    /** Lấy tất cả đặt bàn đang hiển thị (hienThi = 1), sắp xếp theo giờ đến */
    List<DatBan> findVisible();

    /** Lấy tất cả đặt bàn đã bị ẩn (hienThi = 0) */
    List<DatBan> findHidden();

    /** Lấy đặt bàn theo bàn (bao gồm cả ẩn) */
    List<DatBan> findByBan(String maBan);

    /** Lấy đặt bàn còn hiệu lực (CHO_XAC_NHAN, DA_XAC_NHAN) */
    List<DatBan> findConHieuLuc();

    // ── Soft delete ───────────────────────────────────────────────────────
    /** Ẩn đặt bàn khỏi danh sách (SET hienThi = 0) */
    boolean hide(String maDatBan);

    /** Hiện lại đặt bàn đã ẩn (SET hienThi = 1) */
    boolean show(String maDatBan);

    // ── Cập nhật trạng thái ───────────────────────────────────────────────
    boolean updateTrangThai(String maDatBan, TrangThaiDatBan trangThai);

    /** Gán maHD khi khách thanh toán (chuyển trạng thái DA_DEN) */
    boolean updateMaHD(String maDatBan, String maHD);

    // ── Kiểm tra trùng giờ ───────────────────────────────────────────────
    /**
     * Kiểm tra xem bàn maBan có bị trùng giờ với đặt bàn khác không.
     * "Trùng" = có đặt CHO_XAC_NHAN/DA_XAC_NHAN trong khoảng ±60 phút.
     * @param maBan bàn cần kiểm tra
     * @param thoiGianDen giờ cần đặt
     * @param excludeMaDatBan mã đặt bàn cần bỏ qua (null nếu thêm mới)
     */
    boolean isTrungGio(String maBan, LocalDateTime thoiGianDen, String excludeMaDatBan);

    /**
     * Lấy bàn trống trong khu vực không bị trùng giờ.
     * Bàn hợp lệ = trangThai=TRONG VÀ không có đặt ±60 phút.
     */
    List<Ban> findBanTrongByKhuVuc(String maKhuVuc, LocalDateTime thoiGianDen, String excludeMaDatBan);

    // ── Sinh mã ──────────────────────────────────────────────────────────
    String generateNextMaDatBan();
}
