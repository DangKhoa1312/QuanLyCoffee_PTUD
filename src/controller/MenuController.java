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

    /** Lấy list Size của 1 món */
    public List<Size> getSizeOfMon(String maMon) {
        return sizeDAO.findByMon(maMon);
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
}