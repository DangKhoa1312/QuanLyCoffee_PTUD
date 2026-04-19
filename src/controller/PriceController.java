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

    /** Soft Delete: đặt hoatDong=0 */
    public boolean deleteBangGia(String maBG) {
        return bgDAO.delete(maBG);
    }

    public String generateNextMaBG()   { return IDGenerator.newMaBangGia(); }
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
     * Xóa toàn bộ chi tiết giá của một bảng giá (dùng trước khi sao chép đè lên).
     */
    public void deleteAllDetailsOf(String maBG) {
        List<BangGiaChiTiet> details = bgctDAO.findByBangGia(maBG);
        for (BangGiaChiTiet d : details) {
            bgctDAO.delete(d.getMaBGCT());
        }
    }

    /**
     * Điều chỉnh giá hàng loạt theo phần trăm hoặc số tiền cố định.
     * @param percent     Tỉ lệ phần trăm (0.1 = tăng 10%, -0.05 = giảm 5%)
     * @param fixedAmount Số tiền cộng thêm cố định (VD: 5000)
     */
    public void batchAdjustPrice(String maBG, double percent, double fixedAmount) {
        List<BangGiaChiTiet> details = bgctDAO.findByBangGia(maBG);
        for (BangGiaChiTiet d : details) {
            double newPrice = d.getGiaBan() * (1 + percent) + fixedAmount;
            newPrice = Math.round(newPrice / 1000.0) * 1000.0;
            d.setGiaBan(newPrice);
            bgctDAO.update(d);
        }
    }

    /**
     * Lấy giá chi tiết của một bảng giá cụ thể.
     */
    public List<BangGiaChiTiet> getDetailsOf(String maBG) {
        return bgctDAO.findByBangGia(maBG);
    }

    public boolean saveDetail(BangGiaChiTiet detail, boolean exists) {
        if (exists) return bgctDAO.update(detail);
        return bgctDAO.insert(detail);
    }



    /**
     * Xử lý trạng thái hiển thị (Logic thuần túy View, không đụng vào DB).
     */
    public String getVisualStatus(BangGia bg) {
        if (!bg.isHoatDong()) return "Đã ẩn";
        if (!bg.isTrangThai()) return "Tạm ngưng";

        LocalDate today = LocalDate.now();
        if (bg.getNgayKetThuc() != null && today.isAfter(bg.getNgayKetThuc())) {
            return "Hết hạn";
        }
        if (bg.getNgayBatDau() != null && today.isBefore(bg.getNgayBatDau())) {
            return "Đang chờ";
        }

        // Bảng giá đang trong thời hạn VÀ có bật trangThai
        BangGia winner = getWinningPriceList();
        if (winner != null && bg.getMaBangGia().equals(winner.getMaBangGia())) {
            return "Đang áp dụng";
        }
        
        // Cũng trong ngày hôm nay nhưng bị 1 bảng giá khác đề lên (winner khác)
        return "Dự phòng";
    }

    /**
     * Đếm số lượng bảng giá đang hoạt động (chưa bị xóa mềm).
     */
    public long countActivePriceLists() {
        return bgDAO.findAll().stream().filter(BangGia::isHoatDong).count();
    }

    /**
     * Kiểm tra xem bảng giá có đủ giá cho tất cả món/size đang kinh doanh không.
     * Dùng để cảnh báo khi có món mới chưa được định giá.
     */
    public boolean isBangGiaComplete(String maBG) {
        List<BangGiaChiTiet> details = getDetailsOf(maBG);

        // [BUG-03 FIX] Dùng Set để lookup O(1): chỉ đưa vào Set khi giá > 0
        java.util.Set<String> pricedSizes = new java.util.HashSet<>();
        for (BangGiaChiTiet d : details) {
            if (d.getGiaBan() > 0) pricedSizes.add(d.getMaSize());
        }

        // Kiểm tra từng size đang active xem đã có giá chưa
        dao.MonDAO monDAO = new dao.impl.MonDAOImpl();
        List<entity.Mon> allMon = monDAO.findAll();
        for (entity.Mon m : allMon) {
            if (!m.isTrangThai()) continue;
            List<Size> sizes = sizeDAO.findByMon(m.getMaMon());
            for (Size s : sizes) {
                if (s.isTrangThai() && !pricedSizes.contains(s.getMaSize())) {
                    return false; // Tìm thấy size active chưa có giá hợp lệ
                }
            }
        }
        return true;
    }

    /**
     * Lấy bảng giá đang có hiệu lực cao nhất (Winning Price List) cho POS.
     * Điều kiện: hoatDong=true, ngayBatDau <= hôm nay, ngayKetThuc null hoặc >= hôm nay.
     * Ưu tiên bảng có ngayBatDau mới nhất (findAll() đã ORDER BY ngayBatDau DESC).
     */
    public BangGia getWinningPriceList() {
        LocalDate today = LocalDate.now();
        List<BangGia> all = bgDAO.findAll();
        for (BangGia bg : all) {
            if (bg.isHoatDong() && bg.isTrangThai()
                    && !today.isBefore(bg.getNgayBatDau())
                    && (bg.getNgayKetThuc() == null || !today.isAfter(bg.getNgayKetThuc()))) {
                return bg;
            }
        }
        return null;
    }
}
// Trigger IDE rebuild: Eclipse please reload此文件
