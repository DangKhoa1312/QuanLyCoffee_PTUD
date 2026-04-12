package controller;

import dao.ToppingDAO;
import dao.impl.ToppingDAOImpl;
import entity.Topping;
import utils.IDGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * Xử lý nghiệp vụ quản lý Topping (CRUD + tìm kiếm + toggle trạng thái).
 */
public class ToppingController {

    private final ToppingDAO toppingDAO;

    public ToppingController() {
        this.toppingDAO = new ToppingDAOImpl();
    }

    /** Lấy tất cả Topping */
    public List<Topping> getAllToppings() {
        return toppingDAO.findAll();
    }

    /** Tìm kiếm Topping theo tên (client-side filter) */
    public List<Topping> searchToppings(String keyword) {
        List<Topping> all = toppingDAO.findAll();
        if (keyword == null || keyword.trim().isEmpty()) return all;

        String lower = keyword.trim().toLowerCase();
        List<Topping> result = new ArrayList<>();
        for (Topping t : all) {
            if (t.getTenTopping().toLowerCase().contains(lower)
                || t.getMaTopping().toLowerCase().contains(lower)) {
                result.add(t);
            }
        }
        return result;
    }

    /** Thêm Topping mới */
    public boolean addTopping(Topping t) {
        return toppingDAO.insert(t);
    }

    /** Cập nhật thông tin Topping */
    public boolean updateTopping(Topping t) {
        return toppingDAO.update(t);
    }

    /**
     * Xóa mềm: chuyển trạng thái về false (Tạm ngưng).
     * Không xóa cứng để tránh FK constraint với ChiTietHoaDonTopping.
     */
    public boolean deactivateTopping(Topping t) {
        t.setTrangThai(false);
        return toppingDAO.update(t);
    }

    /** Toggle trạng thái Đang cung cấp ↔ Tạm ngưng */
    public boolean toggleTrangThai(Topping t) {
        t.setTrangThai(!t.isTrangThai());
        return toppingDAO.update(t);
    }

    /** Sinh mã Topping tự động */
    public String generateNextMaTopping() {
        return IDGenerator.newMaTopping();
    }

    /**
     * Kiểm tra tên Topping đã tồn tại chưa (bỏ qua chính mình khi edit).
     * @param tenTopping tên cần kiểm tra
     * @param maTopping  mã topping đang edit (null nếu thêm mới)
     */
    public boolean isTenToppingDuplicate(String tenTopping, String maTopping) {
        String normalized = tenTopping.trim().toLowerCase();
        for (Topping t : toppingDAO.findAll()) {
            if (t.getTenTopping().trim().toLowerCase().equals(normalized)) {
                if (maTopping != null && t.getMaTopping().equals(maTopping)) continue;
                return true;
            }
        }
        return false;
    }

    /** Tìm Topping theo mã */
    public Topping findById(String maTopping) {
        return toppingDAO.findById(maTopping);
    }
}
