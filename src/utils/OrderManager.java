package utils;

import dto.CartItem;
import entity.Ban;
import entity.DonHang;
import enums.LoaiDon;
import enums.TrangThaiDonHang;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Singleton quản lý tất cả đơn hàng tạm thời trên RAM.
 * DonHang + giỏ hàng (List<CartItem>) KHÔNG lưu database,
 * chỉ tồn tại trong bộ nhớ cho đến khi khách thanh toán hoặc hủy.
 */
public class OrderManager {

    private static final OrderManager INSTANCE = new OrderManager();

    /** Map maDonHang -> DonHang */
    private final Map<String, DonHang> orders = new ConcurrentHashMap<>();

    /** Map maDonHang -> List<CartItem> (giỏ hàng tạm) */
    private final Map<String, List<CartItem>> carts = new ConcurrentHashMap<>();

    /** Counter tạo mã đơn hàng tạm (không cần DB) */
    private final AtomicInteger dhCounter = new AtomicInteger(0);

    private OrderManager() {}

    public static OrderManager getInstance() {
        return INSTANCE;
    }

    // ── Tạo mã đơn hàng tạm ──────────────────────────────────────────────
    public String generateMaDonHang() {
        return "DH" + String.format("%03d", dhCounter.incrementAndGet());
    }

    // ── CRUD DonHang ──────────────────────────────────────────────────────

    public DonHang createOrder(Ban ban, String maCa, String maNV) {
        boolean isMangVe = (ban != null && "MANG_VE".equals(ban.getMaBan()));
        String maDH = generateMaDonHang();

        DonHang dh = new DonHang(
            maDH,
            LocalDateTime.now(),
            null,
            0,
            "",
            TrangThaiDonHang.DANG_PHUC_VU,
            isMangVe ? LoaiDon.MANG_VE : LoaiDon.TAI_BAN,
            isMangVe ? null : ban.getMaBan(),
            null,
            maCa,
            maNV
        );

        orders.put(maDH, dh);
        carts.put(maDH, new ArrayList<>());
        return dh;
    }

    public void putOrder(DonHang dh) {
        orders.put(dh.getMaDonHang(), dh);
    }

    public DonHang getOrder(String maDonHang) {
        return orders.get(maDonHang);
    }

    /** Tìm đơn hàng DANG_PHUC_VU theo mã bàn */
    public DonHang getOrderByBan(String maBan) {
        for (DonHang dh : orders.values()) {
            if (maBan.equals(dh.getMaBan())
                && TrangThaiDonHang.DANG_PHUC_VU.equals(dh.getTrangThai())) {
                return dh;
            }
        }
        return null;
    }

    /** Lấy danh sách đơn MANG VỀ đang phục vụ */
    public List<DonHang> getOpenTakeawayOrders() {
        List<DonHang> list = new ArrayList<>();
        for (DonHang dh : orders.values()) {
            if (LoaiDon.MANG_VE.equals(dh.getLoaiDon())
                && TrangThaiDonHang.DANG_PHUC_VU.equals(dh.getTrangThai())) {
                list.add(dh);
            }
        }
        // Sắp xếp theo thời gian mở mới nhất
        list.sort((a, b) -> b.getThoiGianMo().compareTo(a.getThoiGianMo()));
        return list;
    }

    /** Xóa đơn hàng khỏi bộ nhớ (sau khi thanh toán hoặc hủy) */
    public void removeOrder(String maDonHang) {
        orders.remove(maDonHang);
        carts.remove(maDonHang);
    }

    // ── Giỏ hàng (CartItem) ───────────────────────────────────────────────

    public void setCart(String maDonHang, List<CartItem> cart) {
        carts.put(maDonHang, cart != null ? new ArrayList<>(cart) : new ArrayList<>());
    }

    public List<CartItem> getCart(String maDonHang) {
        List<CartItem> cart = carts.get(maDonHang);
        return cart != null ? cart : new ArrayList<>();
    }

    /** Tính tổng tiền tạm tính từ giỏ hàng */
    public double tinhTongTien(String maDonHang) {
        double total = 0;
        for (CartItem item : getCart(maDonHang)) {
            total += item.getThanhTien();
        }
        return total;
    }

    // ── Chuyển/Gộp bàn ───────────────────────────────────────────────────

    /** Chuyển đơn hàng sang bàn mới */
    public void chuyenBan(String maDonHang, String maBanMoi) {
        DonHang dh = orders.get(maDonHang);
        if (dh != null) {
            dh.setMaBan(maBanMoi);
        }
    }

    /** Gộp cart của đơn nguồn vào đơn đích, hủy đơn nguồn */
    public void gopDon(String maDonNguon, String maDonDich) {
        List<CartItem> cartNguon = getCart(maDonNguon);
        List<CartItem> cartDich = getCart(maDonDich);
        cartDich.addAll(cartNguon);
        carts.put(maDonDich, cartDich);

        // Cập nhật tổng tiền đơn đích
        DonHang dhDich = orders.get(maDonDich);
        if (dhDich != null) {
            dhDich.setTongTienTamTinh(tinhTongTien(maDonDich));
        }

        // Hủy đơn nguồn
        DonHang dhNguon = orders.get(maDonNguon);
        if (dhNguon != null) {
            dhNguon.setTrangThai(TrangThaiDonHang.DA_HUY);
        }
        removeOrder(maDonNguon);
    }

    /** Xóa tất cả đơn hàng (ví dụ khi đóng ca) */
    public void clearAll() {
        orders.clear();
        carts.clear();
    }
}
