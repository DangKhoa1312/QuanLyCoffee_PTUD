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

    public List<KhuVuc> getDanhSachKhuVuc() {
        return khuVucDAO.findActive();
    }

    public List<Ban> getAllBan() {
        return banDAO.findAll();
    }

    public List<Ban> getBanByKhuVuc(String maKhuVuc) {
        if (maKhuVuc == null || maKhuVuc.isEmpty()) {
            return banDAO.findAll();
        }
        return banDAO.findByKhuVuc(maKhuVuc);
    }

    public void capNhatTrangThai(String maBan, TrangThaiBan trangThai) {
        banDAO.updateTrangThai(maBan, trangThai);
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
