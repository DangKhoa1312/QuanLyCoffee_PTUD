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
    private final ReservationController reservationController;

    public OrderController() {
        this.banDAO = new BanDAOImpl();
        this.orderManager = OrderManager.getInstance();
        this.reservationController = new ReservationController();
    }

    /**
     * Load giỏ hàng đang phục vụ của một đơn hàng từ RAM.
     */
    public List<CartItem> loadCart(String maDonHang) {
        if (maDonHang == null) return new ArrayList<>();
        return new ArrayList<>(orderManager.getCart(maDonHang));
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
            // Kiểm tra trạng thái bàn và đưa về đúng trạng thái
            String maBan = dh.getMaBan();
            if (maBan != null && !maBan.isEmpty() && !"MANG_VE".equals(maBan)) {
                entity.DatBan datBan = reservationController.findDatBanHienTaiCuaBan(maBan);
                if (datBan != null) {
                    reservationController.daDen(datBan.getMaDatBan(), null);
                }
                reservationController.resetTrangThaiBan(maBan);
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

    // =========================================================================
    // XỬ LÝ NGHIỆP VỤ BÀN MA (GHOST TABLE)
    // =========================================================================

    /**
     * Lấy hoặc tạo mới Đơn hàng của Bàn Ma (Nơi chứa các món đã nấu nhưng bị hủy)
     */
    public DonHang getGhostOrder() {
        DonHang ghost = orderManager.getOrderByBan("BAN_MA");
        if (ghost == null) {
            Ban banMa = new Ban("BAN_MA", "Bàn Ma", "KV_NV", 4, TrangThaiBan.CO_KHACH);
            ghost = orderManager.createOrder(banMa, SessionManager.getCurrentCa().getMaCa(), SessionManager.getMaNVHienTai());
        }
        return ghost;
    }

    /**
     * Chuyển danh sách các món Đã Nấu sang Bàn Ma.
     */
    public void moveToGhostTable(String maDonNguon, List<CartItem> cookedItems) {
        if (cookedItems == null || cookedItems.isEmpty()) return;
        DonHang ghost = getGhostOrder();
        
        java.util.Map<CartItem, Integer> transferData = new java.util.HashMap<>();
        for (CartItem item : cookedItems) {
            transferData.put(item, item.getSoLuong());
        }
        
        orderManager.tachMon(maDonNguon, ghost.getMaDonHang(), transferData);
    }

    /**
     * Lấy danh sách toàn bộ món đang nằm ở Bàn Ma.
     */
    public List<CartItem> getGhostTableItems() {
        DonHang ghost = orderManager.getOrderByBan("BAN_MA");
        if (ghost == null) return new ArrayList<>();
        return loadCart(ghost.getMaDonHang());
    }

    /**
     * Rút 1 phần của món có sẵn từ Bàn Ma, để thêm vào đơn hiện tại.
     * Trả về item đã được clone (số lượng 1, isDaPhucVu = true).
     */
    public CartItem takeOneFromGhostTable(CartItem ghostItem) {
        DonHang ghost = getGhostOrder();
        List<CartItem> ghostCart = loadCart(ghost.getMaDonHang());
        for (CartItem gi : ghostCart) {
            if (gi.isIdentical(ghostItem)) {
                gi.setSoLuong(gi.getSoLuong() - 1);
                if (gi.getSoLuong() <= 0) ghostCart.remove(gi);
                
                // Lưu lại
                orderManager.setCart(ghost.getMaDonHang(), ghostCart);
                ghost.setTongTienTamTinh(orderManager.tinhTongTien(ghost.getMaDonHang()));
                
                // Trả về item copy (SL = 1)
                CartItem taken = new CartItem(gi.getMon(), gi.getSize(), 1, gi.getDonGiaSize(), gi.getGhiChu());
                taken.setDaPhucVu(true); // Đã nấu rồi
                for (dto.CartItem.CartTopping ct : gi.getToppings()) {
                    taken.addTopping(ct.topping, ct.soLuong);
                }
                return taken;
            }
        }
        return null;
    }
}
