package controller;

import dao.BangGiaChiTietDAO;
import dao.BangGiaDAO;
import dao.SizeDAO;
import dao.impl.BangGiaChiTietDAOImpl;
import dao.impl.BangGiaDAOImpl;
import dao.impl.SizeDAOImpl;
import entity.BangGia;
import entity.BangGiaChiTiet;
import entity.Size;
import utils.IDGenerator;

import java.time.LocalDate;
import java.util.List;

/**
 * PriceController: Quản lý bảng giá, sao chép và điều chỉnh giá hàng loạt.
 */
public class PriceController {

    private final BangGiaDAO bgDAO = new BangGiaDAOImpl();
    private final BangGiaChiTietDAO bgctDAO = new BangGiaChiTietDAOImpl();
    private final SizeDAO sizeDAO = new SizeDAOImpl();

    public List<BangGia> getAllBangGia() {
        return bgDAO.findAll();
    }

    public boolean saveBangGia(BangGia bg, boolean isEdit) {
        if (isEdit) return bgDAO.update(bg);
        return bgDAO.insert(bg);
    }

    public boolean deleteBangGia(String maBG) {
        return bgDAO.delete(maBG);
    }

    public String generateNextMaBG() { return IDGenerator.newMaBangGia(); }
    public String generateNextMaBGCT() { return IDGenerator.newMaBangGiaChiTiet(); }

    /**
     * Sao chép toàn bộ giá từ bảng giá nguồn sang bảng giá đích.
     */
    public void clonePriceList(String fromMaBG, String toMaBG) {
        List<BangGiaChiTiet> details = bgctDAO.findByBangGia(fromMaBG);
        for (BangGiaChiTiet d : details) {
            BangGiaChiTiet clone = new BangGiaChiTiet(
                generateNextMaBGCT(),
                d.getGiaBan(),
                d.getMaSize(),
                toMaBG
            );
            bgctDAO.insert(clone);
        }
    }

    /**
     * Điều chỉnh giá hàng loạt theo phần trăm hoặc số tiền cố định.
     * @param percent Tỉ lệ phần trăm (Ví dụ: 0.1 là tăng 10%, -0.05 là giảm 5%)
     * @param fixedAmount Số tiền cố định cộng thêm (Ví dụ: 5000)
     */
    public void batchAdjustPrice(String maBG, double percent, double fixedAmount) {
        List<BangGiaChiTiet> details = bgctDAO.findByBangGia(maBG);
        for (BangGiaChiTiet d : details) {
            double newPrice = d.getGiaBan() * (1 + percent) + fixedAmount;
            // Làm tròn đến hàng nghìn (Tùy chọn cho đẹp)
            newPrice = Math.round(newPrice / 1000.0) * 1000.0;
            d.setGiaBan(newPrice);
            bgctDAO.update(d);
        }
    }

    /**
     * Lấy giá chi tiết của một bảng giá cụ thể
     */
    public List<BangGiaChiTiet> getDetailsOf(String maBG) {
        return bgctDAO.findByBangGia(maBG);
    }
    
    public boolean saveDetail(BangGiaChiTiet detail, boolean exists) {
        if (exists) return bgctDAO.update(detail);
        return bgctDAO.insert(detail);
    }

    /**
     * Tự động cập nhật trạng thái bảng giá dựa trên ngày hiện tại.
     * Kích hoạt bảng giá nếu hôm nay nằm trong [ngayBatDau, ngayKetThuc].
     */
    public void autoUpdateStatus() {
        LocalDate today = LocalDate.now();
        List<BangGia> all = bgDAO.findAll();
        for (BangGia bg : all) {
            boolean shouldBeActive = !today.isBefore(bg.getNgayBatDau()) && 
                                   (bg.getNgayKetThuc() == null || !today.isAfter(bg.getNgayKetThuc()));
            
            if (bg.isTrangThai() != shouldBeActive) {
                bg.setTrangThai(shouldBeActive);
                bgDAO.update(bg);
            }
        }
    }
}
