package controller;

import dao.BanDAO;
import dao.ChiTietHoaDonDAO;
import dao.ChiTietHoaDonToppingDAO;
import dao.HoaDonDAO;
import dao.impl.BanDAOImpl;
import dao.impl.ChiTietHoaDonDAOImpl;
import dao.impl.ChiTietHoaDonToppingDAOImpl;
import dao.impl.HoaDonDAOImpl;
import entity.ChiTietHoaDon;
import entity.ChiTietHoaDonTopping;
import entity.DonHang;
import entity.HoaDon;
import dto.CartItem;
import enums.HinhThucThanhToan;
import enums.TrangThaiBan;
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
 */
public class PaymentController {

    private final HoaDonDAO hoaDonDAO;
    private final ChiTietHoaDonDAO ctHoaDonDAO;
    private final ChiTietHoaDonToppingDAO ctToppingDAO;
    private final BanDAO banDAO;
    private final InventoryController inventory;
    private final OrderManager orderManager;

    public PaymentController() {
        this.hoaDonDAO = new HoaDonDAOImpl();
        this.ctHoaDonDAO = new ChiTietHoaDonDAOImpl();
        this.ctToppingDAO = new ChiTietHoaDonToppingDAOImpl();
        this.banDAO = new BanDAOImpl();
        this.inventory = new InventoryController();
        this.orderManager = OrderManager.getInstance();
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

        // 3. Giải phóng Bàn (Chuyển về TRONG)
        String maBan = donHang.getMaBan();
        if (maBan != null && !maBan.isEmpty() && !"MANG_VE".equals(maBan)) {
            banDAO.updateTrangThai(maBan, TrangThaiBan.TRONG);
        }

        // 4. Xóa đơn hàng tạm khỏi RAM
        orderManager.removeOrder(donHang.getMaDonHang());

        // 5. Trừ tồn kho nguyên liệu
        inventory.deductStock(cart);

        return hd;
    }
}
