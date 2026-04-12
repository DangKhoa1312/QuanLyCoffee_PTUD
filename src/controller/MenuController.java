package controller;

import dao.BangGiaChiTietDAO;
import dao.BangGiaDAO;
import dao.MonDAO;
import dao.SizeDAO;
import dao.ToppingDAO;
import dao.impl.BangGiaChiTietDAOImpl;
import dao.impl.BangGiaDAOImpl;
import dao.impl.MonDAOImpl;
import dao.impl.SizeDAOImpl;
import dao.impl.ToppingDAOImpl;
import entity.BangGia;
import entity.BangGiaChiTiet;
import entity.Mon;
import entity.Size;
import entity.Topping;
import enums.LoaiMon;

import java.time.LocalDate;
import java.util.List;

/**
 * Cung cấp dữ liệu phục vụ việc gọi món: Menu, Size, Topping, Giá bán.
 * Tích hợp InventoryController kiểm tra Hết Hàng.
 */
public class MenuController {

    private final MonDAO monDAO;
    private final SizeDAO sizeDAO;
    private final ToppingDAO toppingDAO;
    private final BangGiaDAO bangGiaDAO;
    private final BangGiaChiTietDAO bgctDAO;
    private final InventoryController inventory;

    public MenuController() {
        this.monDAO = new MonDAOImpl();
        this.sizeDAO = new SizeDAOImpl();
        this.toppingDAO = new ToppingDAOImpl();
        this.bangGiaDAO = new BangGiaDAOImpl();
        this.bgctDAO = new BangGiaChiTietDAOImpl();
        this.inventory = new InventoryController();
    }

    /** Lấy tất cả loại món (Cà phê, Trà sữa...) */
    public LoaiMon[] getDanhMuc() {
        return LoaiMon.values();
    }

    /** Lấy danh sách món đang bán theo loại, hoặc tất cả nếu loaiMon null */
    public List<Mon> getMon(LoaiMon loaiMon) {
        if (loaiMon == null) {
            return monDAO.findDangBan();
        }
        return monDAO.findByLoai(loaiMon);
    }
    
    /** Lấy tất cả món để quán lý */
    public List<Mon> getAllMon() {
        return monDAO.findAll();
    }
    
    public boolean saveMon(Mon mon, boolean isEdit) {
        if (isEdit) return monDAO.update(mon);
        return monDAO.insert(mon);
    }

    public Mon getMonById(String maMon) {
        return monDAO.findById(maMon);
    }

    /** Lấy list Size của 1 món (chỉ những Size đang kinh doanh) */
    public List<Size> getSizeOfMon(String maMon) {
        return sizeDAO.findByMon(maMon);
    }
    
    /** Lấy toàn bộ list Size của 1 món (bao gồm cả Size đã bị xóa mềm) */
    public List<Size> getAllSizesOfMon(String maMon) {
        return sizeDAO.findAllByMon(maMon);
    }
    
    public Size getSizeById(String maSize) {
        return sizeDAO.findById(maSize);
    }

    /** Lấy tất cả Topping đang cung cấp */
    public List<Topping> getToppingDangCungCap() {
        return toppingDAO.findDangCungCap();
    }
    
    public Topping getToppingById(String maTopping) {
        return toppingDAO.findById(maTopping);
    }

    /** Lấy giá bán của 1 Size cụ thể trong bảng giá hiện hành (Hỗ trợ Fallback) */
    public double getGiaBan(String maSize) {
        List<BangGia> activeLists = bangGiaDAO.findTatCaHienHanh(LocalDate.now());
        if (activeLists == null || activeLists.isEmpty()) return 0.0;
        
        for (BangGia bg : activeLists) {
            BangGiaChiTiet chiTiet = bgctDAO.findGia(maSize, bg.getMaBangGia());
            if (chiTiet != null && chiTiet.getGiaBan() > 0) {
                return chiTiet.getGiaBan();
            }
        }
        return 0.0; // Nếu không tìm thấy ở bất kỳ bảng giá nào
    }

    /** Kiểm tra xem món có Đủ nguyên liệu tồn kho để bán không */
    public boolean isHetHang(String maMon) {
        return !inventory.checkTonKhoMoiMon(maMon);
    }

    // --- ADMIN METHODS ---

    public String generateNextMaMon() { return utils.IDGenerator.newMaMon(); }
    public String generateNextMaSize() { return utils.IDGenerator.newMaSize(); }
    public String generateNextMaBGCT() { return utils.IDGenerator.newMaBangGiaChiTiet(); }

    public boolean saveSizeAndPrice(Size size, double price, boolean isEdit) {
        boolean sizeOk;
        if (isEdit) sizeOk = sizeDAO.update(size);
        else sizeOk = sizeDAO.insert(size);

        if (!sizeOk) return false;

        // Cập nhật giá trong bảng giá hiện hành
        BangGia activeBG = bangGiaDAO.findHienHanh(LocalDate.now());
        if (activeBG == null) return true; // Không có bảng giá thì chỉ lưu Size

        BangGiaChiTiet existing = bgctDAO.findGia(size.getMaSize(), activeBG.getMaBangGia());
        if (existing != null) {
            existing.setGiaBan(price);
            return bgctDAO.update(existing);
        } else {
            String nextBGCT = generateNextMaBGCT();
            return bgctDAO.insert(new BangGiaChiTiet(nextBGCT, price, size.getMaSize(), activeBG.getMaBangGia()));
        }
    }

    public boolean deleteSize(String maSize) {
        return sizeDAO.delete(maSize);
    }

    /**
     * Lưu món ăn và toàn bộ danh sách size trong cùng một transaction logic.
     * Tự động insert/update/delete sizes so với dữ liệu DB hiện tại.
     */
    public boolean saveMonAndSizes(Mon mon, java.util.List<Size> newSizes, boolean isEdit) {
        // 1. Lưu thông tin món
        boolean monOk = isEdit ? monDAO.update(mon) : monDAO.insert(mon);
        if (!monOk) return false;

        // 2. Lấy toàn bộ danh sách size hiện tại trong DB (kể cả đã ẩn) để đối chiếu
        java.util.List<Size> existingSizes = sizeDAO.findAllByMon(mon.getMaMon());

        // 3. Xóa mềm (Soft Delete) các size không còn trong danh sách mới
        for (Size existing : existingSizes) {
            boolean stillPresent = newSizes.stream()
                    .anyMatch(s -> s.getMaSize().equals(existing.getMaSize()));
            if (!stillPresent) {
                existing.setTrangThai(false);
                sizeDAO.update(existing);
            }
        }

        // 4. Insert mới hoặc Update size hiện có
        for (Size s : newSizes) {
            boolean existsInDb = existingSizes.stream()
                    .anyMatch(e -> e.getMaSize().equals(s.getMaSize()));
            if (existsInDb) sizeDAO.update(s);
            else sizeDAO.insert(s);
        }
        return true;
    }

    /**
     * Kiểm tra tên món đã tồn tại trong DB chưa.
     * @param tenMon   Tên cần kiểm tra
     * @param maMon    Mã món hiện tại (bỏ qua khi edit, truyền null khi thêm mới)
     */
    public boolean isTenMonDuplicate(String tenMon, String maMon) {
        String normalized = tenMon.trim().toLowerCase();
        for (Mon m : monDAO.findAll()) {
            // Chỉ kiểm tra trùng tên với những món ĐANG KINH DOANH (trangThai=true)
            if (m.isTrangThai() && m.getTenMon().trim().toLowerCase().equals(normalized)) {
                // Khi edit: bỏ qua chính món đang sửa
                if (maMon != null && m.getMaMon().equals(maMon)) continue;
                return true;
            }
        }
        return false;
    }

    /**
     * Kiểm tra xem Size có xuất hiện trong BangGiaChiTiet không.
     * Dùng để cảnh báo người dùng trước khi xóa size.
     */
    public boolean isSizeUsedInBangGia(String maSize) {
        java.util.List<BangGia> allBangGia = bangGiaDAO.findAll();
        for (BangGia bg : allBangGia) {
            if (bgctDAO.findGia(maSize, bg.getMaBangGia()) != null) return true;
        }
        return false;
    }

    /**
     * Kiểm tra xem Size có tồn tại trong hóa đơn cũ nào không.
     * Dùng để cấm xóa Size để bảo vệ dữ liệu kế toán.
     */
    public boolean isSizeUsedInHoaDon(String maSize) {
        return sizeDAO.isSizeUsedInHoaDon(maSize);
    }
}