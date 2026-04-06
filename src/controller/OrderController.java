package controller;

import dao.BanDAO;
import dao.impl.BanDAOImpl;
import dto.CartItem;
import entity.Ban;
import entity.DonHang;
import entity.Mon;
import enums.TrangThaiBan;
import exception.AppException;
import utils.OrderManager;
import utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Xử lý nghiệp vụ gọi món. Đơn hàng và giỏ hàng chỉ lưu tạm trên RAM
 * thông qua OrderManager, KHÔNG lưu database.
 */
public class OrderController {

    private final BanDAO banDAO;
    private final OrderManager orderManager;

    public OrderController() {
        this.banDAO = new BanDAOImpl();
        this.orderManager = OrderManager.getInstance();
    }

    /**
     * Load giỏ hàng đang phục vụ của một đơn hàng từ RAM.
     */
    public List<CartItem> loadCart(String maDonHang) {
        if (maDonHang == null) return new ArrayList<>();
        return orderManager.getCart(maDonHang);
    }

    /**
     * Lưu order vào RAM (OrderManager).
     *
     * @param donHangHienTai đơn hàng đang mở (có thể null nếu là tạo mới).
     * @param ban            bàn được đặt (nếu là MANG_VE thì ban=null hoặc có mã MANG_VE).
     * @param cart           danh sách các món trong giỏ.
     */
    public DonHang saveOrder(DonHang donHangHienTai, Ban ban, List<CartItem> cart) {
        if (!SessionManager.isCaDangMo()) {
            throw new AppException("Vui lòng mở ca làm việc trước khi gọi món!");
        }

        boolean isMangVe = (ban != null && "MANG_VE".equals(ban.getMaBan()));
        boolean isNew = (donHangHienTai == null);

        double tongTien = 0;
        for (CartItem item : cart) {
            tongTien += item.getThanhTien();
        }

        DonHang dh = donHangHienTai;
        if (isNew) {
            // Tạo đơn hàng mới trên RAM
            dh = orderManager.createOrder(ban,
                    SessionManager.getCurrentCa().getMaCa(),
                    SessionManager.getMaNVHienTai());
            dh.setTongTienTamTinh(tongTien);

            // Cập nhật trạng thái bàn = CÓ KHÁCH (nếu không phải mang về)
            if (!isMangVe && ban != null) {
                banDAO.updateTrangThai(ban.getMaBan(), TrangThaiBan.CO_KHACH);
            }
        } else {
            // Update tổng tiền
            dh.setTongTienTamTinh(tongTien);
            orderManager.putOrder(dh);
        }

        // Lưu giỏ hàng vào RAM
        orderManager.setCart(dh.getMaDonHang(), cart);

        return dh;
    }

    /**
     * Hủy đơn hàng đang chưa thanh toán.
     */
    public void huyDonHang(String maDonHang) {
        DonHang dh = orderManager.getOrder(maDonHang);
        if (dh != null) {
            // Bàn về TRỐNG
            String maBan = dh.getMaBan();
            if (maBan != null && !maBan.isEmpty() && !"MANG_VE".equals(maBan)) {
                banDAO.updateTrangThai(maBan, TrangThaiBan.TRONG);
            }

            // Xóa đơn hàng khỏi RAM
            orderManager.removeOrder(maDonHang);
        }
    }

    /**
     * Lấy danh sách các đơn MANG VỀ đang phục vụ.
     */
    public List<DonHang> getOpenTakeawayOrders() {
        return orderManager.getOpenTakeawayOrders();
    }

    /**
     * Lấy tóm tắt các món trong đơn hàng (ví dụ: "Cà phê sữa, Trà đào...")
     */
    public String getOrderSummary(String maDonHang) {
        List<CartItem> cart = orderManager.getCart(maDonHang);
        if (cart.isEmpty()) return "(Chưa có món)";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cart.size(); i++) {
            Mon m = cart.get(i).getMon();
            if (m != null) {
                sb.append(m.getTenMon());
                if (i < cart.size() - 1) sb.append(", ");
            }
            if (sb.length() > 50) {
                sb.append("...");
                break;
            }
        }
        return sb.toString();
    }
}
