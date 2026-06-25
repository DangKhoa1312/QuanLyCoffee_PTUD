package utils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import dto.CartItem;
import entity.Ban;
import entity.DonHang;
import enums.LoaiDon;
import enums.TrangThaiDonHang;

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
    
    // File backup để lưu trạng thái tạm thời trong ổ cứng
    private static final String BACKUP_FILE = "orders_backup.dat";

    private OrderManager() {
        loadStateFromDisk();
    }

    public static OrderManager getInstance() {
        return INSTANCE;
    }
    // Lưu trạng thái hiện tại trên RAM xuống file vật lý
    private synchronized void saveStateToDisk() {
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(new java.io.FileOutputStream(BACKUP_FILE))) {
            oos.writeObject(orders);
            oos.writeObject(carts);
            oos.writeObject(dhCounter.get());
        } catch (Exception e) {
            System.err.println("Lỗi lưu trạng thái OrderManager: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    // Nạp trạng thái từ file vật lý lên RAM khi khởi động ứng dụng
    private synchronized void loadStateFromDisk() {
        java.io.File file = new java.io.File(BACKUP_FILE);
        if (!file.exists()) return;
        // Đọc ngược lại các đối tượng theo đúng thứ tự đã ghi
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(new java.io.FileInputStream(file))) {
            Map<String, DonHang> loadedOrders = (Map<String, DonHang>) ois.readObject();
            Map<String, List<CartItem>> loadedCarts = (Map<String, List<CartItem>>) ois.readObject();
            int counter = (Integer) ois.readObject();
            
            orders.clear(); orders.putAll(loadedOrders);
            carts.clear(); carts.putAll(loadedCarts);
            dhCounter.set(counter);
        } catch (Exception e) {
            System.err.println("Lỗi nạp trạng thái OrderManager: " + e.getMessage());
        }
    }

    // ── Tạo mã đơn hàng tạm ──────────────────────────────────────────────
    public String generateMaDonHang() {
        String code = "DH" + String.format("%03d", dhCounter.incrementAndGet());
        saveStateToDisk();
        return code;
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
        saveStateToDisk();
        return dh;
    }

    public void putOrder(DonHang dh) {
        orders.put(dh.getMaDonHang(), dh);
        saveStateToDisk();
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
        saveStateToDisk();
    }

    // ── Giỏ hàng (CartItem) ───────────────────────────────────────────────

    public void setCart(String maDonHang, List<CartItem> cart) {
        carts.put(maDonHang, cart != null ? new ArrayList<>(cart) : new ArrayList<>());
        saveStateToDisk();
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
            // Nếu đơn trước đó là MANG_VE và giờ được chuyển sang bàn thật thì đổi loại đơn
            if (enums.LoaiDon.MANG_VE.equals(dh.getLoaiDon()) && maBanMoi != null && !"MANG_VE".equals(maBanMoi)) {
                dh.setLoaiDon(enums.LoaiDon.TAI_BAN);
            }
            saveStateToDisk();
        }
    }

    /** Gộp cart của đơn nguồn vào đơn đích, hủy đơn nguồn */
    public void gopDon(String maDonNguon, String maDonDich) {
        List<CartItem> cartNguon = getCart(maDonNguon);
        List<CartItem> cartDich = getCart(maDonDich);
        
        for (CartItem itemNguon : cartNguon) {
            boolean merged = false;
            for (CartItem itemDich : cartDich) {
                if (itemDich.isIdentical(itemNguon)) {
                    itemDich.setSoLuong(itemDich.getSoLuong() + itemNguon.getSoLuong());
                    merged = true;
                    break;
                }
            }
            if (!merged) {
                cartDich.add(itemNguon);
            }
        }
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
        removeOrder(maDonNguon); // Đã có hàm saveStateToDisk() bên trong removeOrder
    }

    /** 
     * Tách món: chuyển 1 phần (hoặc toàn bộ) món từ đơn nguồn sang đơn đích. 
     * transferData: Map<CartItem, Integer> với Key là item gốc bên nguồn, Value là số lượng cần chuyển.
     */
    public void tachMon(String maDonNguon, String maDonDich, Map<CartItem, Integer> transferData) {
        List<CartItem> cartNguon = getCart(maDonNguon);
        List<CartItem> cartDich = getCart(maDonDich);

        for (Map.Entry<CartItem, Integer> entry : transferData.entrySet()) {
            CartItem itemNguon = entry.getKey();
            int slChuyen = entry.getValue();

            if (slChuyen <= 0) continue;

            // 1. Tạo item mới cho đơn đích (sao chép thuộc tính)
            CartItem itemMoi = new CartItem(itemNguon.getMon(), itemNguon.getSize(), slChuyen, itemNguon.getDonGiaSize(), itemNguon.getGhiChu());
            itemMoi.setDaPhucVu(itemNguon.isDaPhucVu()); 
            for (CartItem.CartTopping ct : itemNguon.getToppings()) {
                itemMoi.addTopping(ct.topping, ct.soLuong, ct.giaTopping);
            }

            // Gộp vào đơn đích nếu đã có
            boolean merged = false;
            for (CartItem itemDich : cartDich) {
                if (itemDich.isIdentical(itemMoi)) {
                    itemDich.setSoLuong(itemDich.getSoLuong() + slChuyen);
                    merged = true;
                    break;
                }
            }
            if (!merged) {
                cartDich.add(itemMoi);
            }

            // 2. Trừ số lượng ở đơn nguồn
            itemNguon.setSoLuong(itemNguon.getSoLuong() - slChuyen);
        }

        // 3. Xóa các món có số lượng <= 0 ở đơn nguồn
        cartNguon.removeIf(item -> item.getSoLuong() <= 0);

        carts.put(maDonNguon, cartNguon);
        carts.put(maDonDich, cartDich);

        // Cập nhật tổng tiền
        DonHang dhDich = orders.get(maDonDich);
        if (dhDich != null) dhDich.setTongTienTamTinh(tinhTongTien(maDonDich));

        DonHang dhNguon = orders.get(maDonNguon);
        if (dhNguon != null) dhNguon.setTongTienTamTinh(tinhTongTien(maDonNguon));

        saveStateToDisk();
    }

    /** Xóa tất cả đơn hàng (ví dụ khi đóng ca) */
    public void clearAll() {
        orders.clear();
        carts.clear();
        saveStateToDisk();
    }

    /** Đếm số lượng bàn đang phục vụ của một nhân viên cụ thể */
    public int countOpenOrdersByEmployee(String maNV) {
        int count = 0;
        for (DonHang dh : orders.values()) {
            // Không tính Bàn Ma (kho tạm) vào số lượng bàn đang phục vụ
            if ("BAN_MA".equals(dh.getMaBan())) {
                continue;
            }
            
            if (maNV.equals(dh.getMaNV()) && TrangThaiDonHang.DANG_PHUC_VU.equals(dh.getTrangThai())) {
                // Chỉ đếm các đơn có món hoặc là đơn mới tạo (thực tế nếu cần khắt khe có thể check thêm carts.get(dh.getMaDonHang()).size() > 0)
                count++;
            }
        }
        return count;
    }
}
