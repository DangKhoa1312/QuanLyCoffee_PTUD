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
     * Lấy bảng giá đang có hiệu lực cao nhất (Winning Price List) cho POS.
     * Điều kiện: hoatDong=true, ngayBatDau <= hôm nay, ngayKetThuc null hoặc >= hôm nay.
     * Ưu tiên bảng có ngayBatDau mới nhất (findAll() đã ORDER BY ngayBatDau DESC).
     */
    public BangGia getWinningPriceList() {
        LocalDate today = LocalDate.now();
        List<BangGia> all = bgDAO.findAll();
        for (BangGia bg : all) {
            if (bg.isHoatDong()
                    && !today.isBefore(bg.getNgayBatDau())
                    && (bg.getNgayKetThuc() == null || !today.isAfter(bg.getNgayKetThuc()))) {
                return bg;
            }
        }
        return null;
    }

    /**
     * Tự động cập nhật trạng thái bảng giá dựa trên ngày hiện tại.
     * Logic: Chỉ có bảng giá Winner (mới nhất, hiệu lực hôm nay) mới có trangThai=1.
     * Các bảng khác dù hợp lệ ngày nhưng sẽ là trangThai=0 (Standby).
     * Cập nhật DB chỉ khi có thay đổi (giảm số lần ghi).
     */
    public void autoUpdateStatus() {
        BangGia winner = getWinningPriceList();
        List<BangGia> all = bgDAO.findAll();

        for (BangGia bg : all) {
            boolean isWinner = (winner != null && bg.getMaBangGia().equals(winner.getMaBangGia()));
            if (bg.isTrangThai() != isWinner) {
                bg.setTrangThai(isWinner);
                bgDAO.update(bg);
            }
        }
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
        dao.MonDAO monDAO = new dao.impl.MonDAOImpl();
        List<entity.Mon> allMon = monDAO.findAll();
        List<BangGiaChiTiet> details = getDetailsOf(maBG);

        int totalSizes = 0;
        for (entity.Mon m : allMon) {
            if (m.isTrangThai()) {
                List<Size> sizes = sizeDAO.findByMon(m.getMaMon());
                // Size trong TARGET không có trangThai DB column → mặc định luôn active
                totalSizes += sizes.size();
            }
        }
        return details.size() >= totalSizes;
    }
}
