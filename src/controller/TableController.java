package controller;

import dao.BanDAO;
import dao.impl.BanDAOImpl;
import dao.KhuVucDAO;
import dao.impl.KhuVucDAOImpl;
import entity.Ban;
import entity.DonHang;
import entity.KhuVuc;
import enums.TrangThaiBan;
import utils.OrderManager;

import java.util.List;

/**
 * Xử lý nghiệp vụ bàn: lấy danh sách, đổi trạng thái, tìm đơn hàng đang mở.
 * Sử dụng OrderManager (RAM) thay vì DonHangDAO (DB).
 */
public class TableController {

    private final BanDAO banDAO;
    private final KhuVucDAO khuVucDAO;
    private final OrderManager orderManager;

    public TableController() {
        this.banDAO = new BanDAOImpl();
        this.khuVucDAO = new KhuVucDAOImpl();
        this.orderManager = OrderManager.getInstance();
    }

    // ═══════════════ KHU VỰC ═══════════════

    /** Lấy danh sách khu vực đang hoạt động (cho bán hàng) */
    public List<KhuVuc> getDanhSachKhuVuc() {
        return khuVucDAO.findActive();
    }

    /** Lấy tất cả khu vực kể cả tạm ngưng (cho admin) */
    public List<KhuVuc> getAllKhuVuc() {
        return khuVucDAO.findAll();
    }

    public boolean addKhuVuc(KhuVuc kv) {
        return khuVucDAO.insert(kv);
    }

    public boolean updateKhuVuc(KhuVuc kv) {
        return khuVucDAO.update(kv);
    }

    public boolean deleteKhuVuc(String maKV) {
        // Kiểm tra còn bàn không
        int count = banDAO.countByKhuVuc(maKV);
        if (count > 0) {
            return false; // Không cho xóa nếu còn bàn
        }
        return khuVucDAO.delete(maKV);
    }

    public boolean toggleKhuVuc(KhuVuc kv) {
        kv.setTrangThai(!kv.isTrangThai());
        return khuVucDAO.update(kv);
    }

    /**
     * Kiểm tra tên khu vực đã tồn tại chưa (bỏ qua chính mình khi edit).
     * @param tenKhuVuc tên cần kiểm tra
     * @param maKVHienTai mã khu vực đang edit (null nếu là thêm mới)
     */
    public boolean isTenKhuVucTrung(String tenKhuVuc, String maKVHienTai) {
        for (KhuVuc kv : khuVucDAO.findAll()) {
            if (maKVHienTai != null && kv.getMaKhuVuc().equals(maKVHienTai)) continue;
            if (kv.getTenKhuVuc().trim().equalsIgnoreCase(tenKhuVuc.trim())) return true;
        }
        return false;
    }

    /**
     * Đếm số bàn đang CÓ KHÁCH trong khu vực.
     */
    public int countBanCoKhachByKhuVuc(String maKV) {
        int count = 0;
        for (Ban ban : banDAO.findByKhuVuc(maKV)) {
            if (ban.getTrangThai() == TrangThaiBan.CO_KHACH) count++;
        }
        return count;
    }

    // ═══════════════ BÀN ═══════════════

    public List<Ban> getAllBan() {
        return banDAO.findAll();
    }

    public List<Ban> getBanByKhuVuc(String maKhuVuc) {
        if (maKhuVuc == null || maKhuVuc.isEmpty()) {
            return banDAO.findAll();
        }
        return banDAO.findByKhuVuc(maKhuVuc);
    }

    public boolean addBan(Ban ban) {
        return banDAO.insert(ban);
    }

    public boolean updateBan(Ban ban) {
        return banDAO.update(ban);
    }

    public boolean deleteBan(String maBan) {
        Ban ban = banDAO.findById(maBan);
        if (ban == null) return false;
        // Chỉ cho xóa bàn đang TRỐNG hoặc TẠM NGƯNG
        if (ban.getTrangThai() != TrangThaiBan.TRONG && ban.getTrangThai() != TrangThaiBan.TAM_NGUNG) {
            return false;
        }
        return banDAO.delete(maBan);
    }

    public void capNhatTrangThai(String maBan, TrangThaiBan trangThai) {
        banDAO.updateTrangThai(maBan, trangThai);
    }

    public int countBanByKhuVuc(String maKV) {
        return banDAO.countByKhuVuc(maKV);
    }

    public int countBanTrongByKhuVuc(String maKV) {
        return banDAO.countTrongByKhuVuc(maKV);
    }

    /**
     * Tìm đơn hàng DANG_PHUC_VU của bàn (đơn đang mở) - từ RAM.
     */
    public DonHang getDonHangDangMo(String maBan) {
        return orderManager.getOrderByBan(maBan);
    }

    public List<Ban> getBanTrong() {
        return banDAO.findByTrangThai(TrangThaiBan.TRONG);
    }

    public List<Ban> getBanDangCoKhach() {
        return banDAO.findByTrangThai(TrangThaiBan.CO_KHACH);
    }

    /**
     * Chuyển đơn hàng (từ bàn nguồn sang bàn đích trống) - thao tác RAM.
     */
    public void chuyenBan(String maDonHang, String maBanNguon, String maBanDich) {
        DonHang dh = orderManager.getOrder(maDonHang);
        if (dh == null) return;

        // Cập nhật mã bàn cho đơn hàng trên RAM
        orderManager.chuyenBan(maDonHang, maBanDich);

        // Cập nhật trạng thái bàn trong DB
        banDAO.updateTrangThai(maBanNguon, TrangThaiBan.TRONG);
        banDAO.updateTrangThai(maBanDich, TrangThaiBan.CO_KHACH);
    }

    /**
     * Gộp 2 bàn có khách (Gộp A -> B) - thao tác RAM.
     */
    public void gopBan(String maDonNguon, String maDonDich, String maBanNguon, String maBanDich) {
        // Gộp giỏ hàng trên RAM
        orderManager.gopDon(maDonNguon, maDonDich);

        // Cập nhật trạng thái bàn nguồn trong DB
        banDAO.updateTrangThai(maBanNguon, TrangThaiBan.TRONG);
    }
}
