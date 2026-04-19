package controller;

import dao.BanDAO;
import dao.ChiTietHoaDonDAO;
import dao.ChiTietHoaDonToppingDAO;
import dao.DatBanDAO;
import dao.HoaDonDAO;
import dao.impl.BanDAOImpl;
import dao.impl.ChiTietHoaDonDAOImpl;
import dao.impl.ChiTietHoaDonToppingDAOImpl;
import dao.impl.DatBanDAOImpl;
import dao.impl.HoaDonDAOImpl;
import entity.ChiTietHoaDon;
import entity.ChiTietHoaDonTopping;
import entity.DatBan;
import entity.DonHang;
import entity.HoaDon;
import dto.CartItem;
import enums.HinhThucThanhToan;
import enums.TrangThaiBan;
import enums.TrangThaiDatBan;
import enums.TrangThaiHoaDon;
import exception.AppException;
import utils.IDGenerator;
import utils.OrderManager;
import utils.SessionManager;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Xử lý nghiệp vụ thanh toán:
 * Chuyển đơn hàng tạm (RAM) thành HoaDon + ChiTietHoaDon (DB).
 *
 * Luồng mới đặt bàn:
 * - Nếu bàn có đặt bàn DA_XAC_NHAN → mark DA_THANH_TOAN sau khi thanh toán.
 * - Sau thanh toán: bàn về DA_DAT_TRUOC nếu còn CHO_XAC_NHAN, ngược lại về TRONG.
 */
public class PaymentController {

    private final HoaDonDAO hoaDonDAO;
    private final ChiTietHoaDonDAO ctHoaDonDAO;
    private final ChiTietHoaDonToppingDAO ctToppingDAO;
    private final BanDAO banDAO;
    private final DatBanDAO datBanDAO;
    private final InventoryController inventory;
    private final OrderManager orderManager;
    private final ReservationController reservationController;

    public PaymentController() {
        this.hoaDonDAO = new HoaDonDAOImpl();
        this.ctHoaDonDAO = new ChiTietHoaDonDAOImpl();
        this.ctToppingDAO = new ChiTietHoaDonToppingDAOImpl();
        this.banDAO = new BanDAOImpl();
        this.datBanDAO = new DatBanDAOImpl();
        this.inventory = new InventoryController();
        this.orderManager = OrderManager.getInstance();
        this.reservationController = new ReservationController();
    }

    /**
     * Thực hiện thanh toán cho một đơn hàng.
     * Tạo HoaDon + ChiTietHoaDon + ChiTietHoaDonTopping trong DB.
     * Xóa đơn hàng tạm khỏi RAM.
     */
    public HoaDon thanhToan(DonHang donHang, List<CartItem> cart, double tongTienPhaiTra, HinhThucThanhToan hinhThuc) {
        if (!SessionManager.isCaDangMo()) {
            throw new AppException("Vui lòng mở ca làm việc trước khi thanh toán!");
        }

        if (donHang == null) {
            throw new AppException("Đơn hàng không hợp lệ!");
        }

        // 0. Kiểm tra kho trước khi thanh toán (Chặn giao dịch nếu thiếu)
        String missingInfo = inventory.checkDuNguyenLieuChoCart(cart);
        if (missingInfo != null) {
            throw new AppException("Không đủ nguyên liệu để thanh toán đơn hàng này:\n" + missingInfo);
        }

        // 1. Tạo HoaDon (chứa trực tiếp maBan, maCa, loaiDon, ghiChu)
        String maHD = IDGenerator.newMaHoaDon();
        HoaDon hd = new HoaDon(
            maHD,
            LocalDateTime.now(), // thoiGianXuat
            LocalDateTime.now(), // thoiGianThanhToan
            tongTienPhaiTra,
            TrangThaiHoaDon.DA_THANH_TOAN,
            hinhThuc,
            donHang.getMaBan(),                          // maBan từ DonHang
            SessionManager.getCurrentCa().getMaCa(),     // maCa
            donHang.getLoaiDon(),                        // loaiDon
            donHang.getGhiChu(),                         // ghiChu
            SessionManager.getMaNVHienTai()               // maNV thu ngân
        );
        // Gán tenNV để in PDF hiển thị đúng tên nhân viên
        if (SessionManager.getCurrentUser() != null) {
            hd.setTenNV(SessionManager.getCurrentUser().getTenNV());
        }

        boolean ok = hoaDonDAO.insert(hd);
        if (!ok) {
            throw new AppException("Lỗi hệ thống khi lưu hóa đơn!");
        }

        // 2. Tạo ChiTietHoaDon + ChiTietHoaDonTopping cho mỗi món
        for (CartItem item : cart) {
            String maCTHD = IDGenerator.newMaChiTietHoaDon();
            double thanhTienMon = item.getDonGiaSize() * item.getSoLuong();

            ChiTietHoaDon cthd = new ChiTietHoaDon(
                maCTHD,
                item.getSoLuong(),
                item.getDonGiaSize(),
                thanhTienMon,
                item.getGhiChu(),
                maHD,
                item.getMon().getMaMon(),
                item.getSize().getMaSize()
            );
            ctHoaDonDAO.insert(cthd);

            // Insert toppings
            for (CartItem.CartTopping top : item.getToppings()) {
                ChiTietHoaDonTopping ctht = new ChiTietHoaDonTopping(
                    IDGenerator.newMaCTHDTopping(),
                    top.soLuong,
                    top.giaTopping,
                    maCTHD,
                    top.topping.getMaTopping()
                );
                ctToppingDAO.insert(ctht);
            }
        }

        // 3. Xử lý đặt bàn nếu là bàn thật
        String maBan = donHang.getMaBan();
        if (maBan != null && !maBan.isEmpty() && !"MANG_VE".equals(maBan)) {
            // Tìm đặt bàn DA_XAC_NHAN của bàn này (nếu có)
            DatBan datBanXacNhan = findDatBanDaXacNhan(maBan);
            if (datBanXacNhan != null) {
                // Có đặt bàn đã xác nhận → mark DA_THANH_TOAN kèm mã hoá đơn
                datBanDAO.updateMaHD(datBanXacNhan.getMaDatBan(), maHD);
            }

            // Quyết định trạng thái bàn sau thanh toán bằng hàm dùng chung
            reservationController.resetTrangThaiBan(maBan);
        }

        // 4. Xóa đơn hàng tạm khỏi RAM
        orderManager.removeOrder(donHang.getMaDonHang());

        // 5. Trừ tồn kho nguyên liệu
        inventory.deductStock(cart);

        return hd;
    }

    /**
     * Tìm đặt bàn DA_XAC_NHAN đang hiệu lực của bàn.
     */
    private DatBan findDatBanDaXacNhan(String maBan) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        DatBan closest = null;
        long minDiff = Long.MAX_VALUE;

        for (DatBan db : datBanDAO.findByBan(maBan)) {
            if (db.getTrangThai() == TrangThaiDatBan.DA_XAC_NHAN) {
                if (db.getThoiGianDen() != null) {
                    long diff = Math.abs(java.time.Duration.between(now, db.getThoiGianDen()).toMinutes());
                    if (diff < minDiff) {
                        minDiff = diff;
                        closest = db;
                    }
                }
            }
        }
        return closest;
    }
}
