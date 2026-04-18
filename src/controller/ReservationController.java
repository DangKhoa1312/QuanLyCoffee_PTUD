package controller;

import dao.BanDAO;
import dao.DatBanDAO;
import dao.KhuVucDAO;
import dao.impl.BanDAOImpl;
import dao.impl.DatBanDAOImpl;
import dao.impl.KhuVucDAOImpl;
import entity.Ban;
import entity.DatBan;
import entity.KhuVuc;
import enums.TrangThaiBan;
import enums.TrangThaiDatBan;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller nghiệp vụ đặt bàn.
 * Xử lý CRUD, chuyển trạng thái, kiểm tra trùng giờ, auto-expire và tích hợp bán hàng.
 */
public class ReservationController {

    private final DatBanDAO datBanDAO;
    private final BanDAO    banDAO;
    private final KhuVucDAO khuVucDAO;

    public ReservationController() {
        this.datBanDAO = new DatBanDAOImpl();
        this.banDAO    = new BanDAOImpl();
        this.khuVucDAO = new KhuVucDAOImpl();
    }

    // ══ DANH SÁCH ════════════════════════════════════════════════════════════

    /** Lấy danh sách đặt bàn đang hiển thị (hienThi = 1) */
    public List<DatBan> getDanhSachHienThi() {
        return datBanDAO.findVisible();
    }

    /** Lấy danh sách đặt bàn đã ẩn (hienThi = 0) */
    public List<DatBan> getDanhSachDaAn() {
        return datBanDAO.findHidden();
    }

    /** Tìm theo mã */
    public DatBan findById(String maDatBan) {
        return datBanDAO.findById(maDatBan);
    }

    /** Lấy danh sách đặt bàn còn hiệu lực của một bàn cụ thể */
    public List<DatBan> findByBan(String maBan) {
        return datBanDAO.findByBan(maBan);
    }

    /** Lấy đặt bàn DA_XAC_NHAN đang hiệu lực của bàn (dùng cho TablePanel cảnh báo) */
    public DatBan findDatBanHienTaiCuaBan(String maBan) {
        for (DatBan db : datBanDAO.findByBan(maBan)) {
            if (db.getTrangThai() == TrangThaiDatBan.DA_XAC_NHAN ||
                db.getTrangThai() == TrangThaiDatBan.CHO_XAC_NHAN) {
                return db;
            }
        }
        return null;
    }

    // ══ THÊM ═════════════════════════════════════════════════════════════════

    /**
     * Thêm đặt bàn mới.
     * - Tự động set thoiGianDat = now, trangThai = CHO_XAC_NHAN
     * - Cập nhật trạng thái bàn → DA_DAT_TRUOC
     */
    public boolean them(DatBan db) {
        db.setThoiGianDat(LocalDateTime.now());
        db.setTrangThai(TrangThaiDatBan.CHO_XAC_NHAN);
        db.setHienThi(true);
        boolean ok = datBanDAO.insert(db);
        if (ok) {
            // Cập nhật bàn thành DA_DAT_TRUOC nếu chưa có khách
            Ban ban = banDAO.findById(db.getMaBan());
            if (ban != null && ban.getTrangThai() == TrangThaiBan.TRONG) {
                banDAO.updateTrangThai(db.getMaBan(), TrangThaiBan.DA_DAT_TRUOC);
            }
        }
        return ok;
    }

    // ══ SỬA ══════════════════════════════════════════════════════════════════

    /**
     * Cập nhật thông tin đặt bàn (tên, sđt, số người, giờ đến, bàn).
     * Nếu bàn thay đổi: cập nhật trạng thái bàn cũ và bàn mới.
     */
    public boolean sua(DatBan dbMoi, String maBanCu) {
        boolean ok = datBanDAO.update(dbMoi);
        if (ok && maBanCu != null && !maBanCu.equals(dbMoi.getMaBan())) {
            // Trả bàn cũ về TRONG nếu không còn đặt hiệu lực nào
            giaiPhongBanNeuRanh(maBanCu);
            // Đặt bàn mới thành DA_DAT_TRUOC
            Ban banMoi = banDAO.findById(dbMoi.getMaBan());
            if (banMoi != null && banMoi.getTrangThai() == TrangThaiBan.TRONG) {
                banDAO.updateTrangThai(dbMoi.getMaBan(), TrangThaiBan.DA_DAT_TRUOC);
            }
        }
        return ok;
    }

    // ══ ẨN (Soft-delete) ════════════════════════════════════════════════════

    /**
     * Ẩn đặt bàn (chỉ khi HET_HAN hoặc DA_HUY).
     * @return true nếu ẩn thành công, false nếu trạng thái không cho phép
     */
    public boolean an(String maDatBan) {
        DatBan db = datBanDAO.findById(maDatBan);
        if (db == null) return false;
        if (db.getTrangThai() != TrangThaiDatBan.HET_HAN &&
            db.getTrangThai() != TrangThaiDatBan.DA_HUY &&
            db.getTrangThai() != TrangThaiDatBan.DA_DEN) {
            return false; // Không được ẩn
        }
        return datBanDAO.hide(maDatBan);
    }

    /** Hiện lại đặt bàn đã ẩn */
    public boolean hien(String maDatBan) {
        return datBanDAO.show(maDatBan);
    }

    // ══ CHUYỂN TRẠNG THÁI ════════════════════════════════════════════════════

    /**
     * Xác nhận đặt bàn: CHO_XAC_NHAN → DA_XAC_NHAN.
     * Đảm bảo bàn là DA_DAT_TRUOC.
     */
    public boolean xacNhan(String maDatBan) {
        DatBan db = datBanDAO.findById(maDatBan);
        if (db == null || db.getTrangThai() != TrangThaiDatBan.CHO_XAC_NHAN) return false;
        boolean ok = datBanDAO.updateTrangThai(maDatBan, TrangThaiDatBan.DA_XAC_NHAN);
        if (ok) {
            // Xác nhận → bàn chuyển sang CO_KHACH
            banDAO.updateTrangThai(db.getMaBan(), TrangThaiBan.CO_KHACH);
        }
        return ok;
    }

    /**
     * Huỷ đặt bàn → DA_HUY.
     * Cập nhật bàn về TRONG nếu không còn đặt hiệu lực.
     */
    public boolean huy(String maDatBan) {
        DatBan db = datBanDAO.findById(maDatBan);
        if (db == null) return false;
        // Không được huỷ khi đã xác nhận hoặc đã đến
        if (db.getTrangThai() == TrangThaiDatBan.DA_XAC_NHAN ||
            db.getTrangThai() == TrangThaiDatBan.DA_DEN) {
            return false;
        }
        boolean ok = datBanDAO.updateTrangThai(maDatBan, TrangThaiDatBan.DA_HUY);
        if (ok) {
            giaiPhongBanNeuRanh(db.getMaBan());
        }
        return ok;
    }

    /**
     * Tự động kiểm tra và đánh dấu HET_HAN cho tất cả đặt bàn quá giờ 15 phút.
     * Gọi định kỳ từ Timer hoặc khi nhấn "Làm mới".
     */
    public void autoCheckExpired() {
        // Chỉ kiểm tra CHO_XAC_NHAN — DA_XAC_NHAN và DA_DEN không bao giờ hết hạn tự động
        for (DatBan db : datBanDAO.findConHieuLuc()) {
            if (db.getTrangThai() == TrangThaiDatBan.CHO_XAC_NHAN && db.isQuaHan()) {
                datBanDAO.updateTrangThai(db.getMaDatBan(), TrangThaiDatBan.HET_HAN);
                giaiPhongBanNeuRanh(db.getMaBan());
            }
        }
    }

    /**
     * Đánh dấu khách đã đến và gán hoá đơn (sau thanh toán).
     * @param maDatBan mã đặt bàn
     * @param maHD     mã hoá đơn (null nếu mới mở đơn, chưa thanh toán)
     */
    public boolean daDen(String maDatBan, String maHD) {
        DatBan db = datBanDAO.findById(maDatBan);
        if (db == null) return false;
        if (maHD != null && !maHD.isEmpty()) {
            return datBanDAO.updateMaHD(maDatBan, maHD); // cũng set DA_DEN
        }
        return datBanDAO.updateTrangThai(maDatBan, TrangThaiDatBan.DA_DEN);
    }

    // ══ BÀN TRỐNG ════════════════════════════════════════════════════════════

    /** Lấy danh sách khu vực đang hoạt động */
    public List<KhuVuc> getDanhSachKhuVuc() {
        return khuVucDAO.findActive();
    }

    /**
     * Lấy bàn trống trong khu vực không bị trùng giờ.
     * @param maKhuVuc       khu vực cần tìm
     * @param thoiGianDen    giờ khách đặt đến
     * @param excludeId      mã đặt bàn đang edit (null nếu thêm mới)
     */
    public List<Ban> getBanTrongChoKhuVuc(String maKhuVuc, LocalDateTime thoiGianDen, String excludeId) {
        return datBanDAO.findBanTrongByKhuVuc(maKhuVuc, thoiGianDen, excludeId);
    }

    // ══ SINH MÃ ══════════════════════════════════════════════════════════════

    public String sinhMaDatBan() {
        return datBanDAO.generateNextMaDatBan();
    }

    /** Tìm bàn theo mã */
    public Ban findBanById(String maBan) {
        return banDAO.findById(maBan);
    }

    // ══ KIỂM TRA ════════════════════════════════════════════════════════════

    /** Kiểm tra bàn có bị trùng giờ với đặt bàn khác không */
    public boolean isTrungGio(String maBan, LocalDateTime thoiGianDen, String excludeId) {
        return datBanDAO.isTrungGio(maBan, thoiGianDen, excludeId);
    }

    // ══ HELPER NỘI BỘ ════════════════════════════════════════════════════════

    /**
     * Nếu bàn không còn đặt hiệu lực nào (CHO_XAC_NHAN, DA_XAC_NHAN) thì
     * cập nhật trạng thái bàn về TRONG.
     */
    private void giaiPhongBanNeuRanh(String maBan) {
        if (maBan == null) return;
        long conHieuLuc = datBanDAO.findByBan(maBan).stream()
            .filter(d -> d.getTrangThai() == TrangThaiDatBan.CHO_XAC_NHAN ||
                         d.getTrangThai() == TrangThaiDatBan.DA_XAC_NHAN)
            .count();
        if (conHieuLuc == 0) {
            Ban ban = banDAO.findById(maBan);
            // Chỉ trả TRONG nếu bàn đang là DA_DAT_TRUOC (không động đến CO_KHACH)
            if (ban != null && ban.getTrangThai() == TrangThaiBan.DA_DAT_TRUOC) {
                banDAO.updateTrangThai(maBan, TrangThaiBan.TRONG);
            }
        }
    }
}
