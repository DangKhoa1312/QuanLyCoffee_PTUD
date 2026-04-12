package controller;

import dao.DinhMucNguyenLieuDAO;
import dao.MonDAO;
import dao.NguyenLieuDAO;
import dao.impl.DinhMucNguyenLieuDAOImpl;
import dao.impl.MonDAOImpl;
import dao.impl.NguyenLieuDAOImpl;
import entity.DinhMucNguyenLieu;
import entity.Mon;
import entity.NguyenLieu;

import java.util.List;

/**
 * Controller điều phối nghiệp vụ Quản lý Công Thức (Định mức nguyên liệu).
 */
public class RecipeController {

    private final MonDAO monDAO = new MonDAOImpl();
    private final NguyenLieuDAO nlDAO = new NguyenLieuDAOImpl();
    private final DinhMucNguyenLieuDAO dmDAO = new DinhMucNguyenLieuDAOImpl();

    public List<Mon> getAllMon(String searchQuery) {
        List<Mon> all = monDAO.findAll();
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            return all;
        }
        String q = searchQuery.trim().toLowerCase();
        all.removeIf(m -> !m.getTenMon().toLowerCase().contains(q));
        return all;
    }

    public List<NguyenLieu> getAllNguyenLieu() {
        return nlDAO.findAll();
    }

    public List<DinhMucNguyenLieu> getDinhMucByMon(String maMon) {
        return dmDAO.findByMon(maMon);
    }

    public NguyenLieu getNguyenLieuById(String maNL) {
        return nlDAO.findById(maNL);
    }

    public boolean addDinhMuc(String maMon, String maNL, double soLuong) {
        // Kiểm tra nguyên liệu đã tồn tại trong công thức chưa (Tránh lỗi SQL Unique Constraint)
        List<DinhMucNguyenLieu> existing = dmDAO.findByMon(maMon);
        for (DinhMucNguyenLieu exist : existing) {
            if (exist.getMaNL().equals(maNL)) {
                return false; 
            }
        }
        DinhMucNguyenLieu dm = new DinhMucNguyenLieu();
        dm.setMaDinhMuc(generateNextMaDinhMuc());
        dm.setMaMon(maMon);
        dm.setMaNL(maNL);
        dm.setSoLuong(soLuong);
        return dmDAO.insert(dm);
    }

    public boolean updateDinhMuc(String maDinhMuc, String maMon, String maNL, double soLuong) {
        DinhMucNguyenLieu param = new DinhMucNguyenLieu();
        param.setMaDinhMuc(maDinhMuc);
        param.setMaMon(maMon);
        param.setMaNL(maNL);
        param.setSoLuong(soLuong);
        return dmDAO.update(param);
    }

    public boolean deleteDinhMuc(String maDinhMuc) {
        return dmDAO.delete(maDinhMuc);
    }

    /**
     * Sinh mã định mức tự động
     */
    private String generateNextMaDinhMuc() {
        List<DinhMucNguyenLieu> list = dmDAO.findAll();
        int max = 0;
        for (DinhMucNguyenLieu dm : list) {
            try {
                int num = Integer.parseInt(dm.getMaDinhMuc().replace("DM", ""));
                if (num > max) max = num;
            } catch (NumberFormatException ignored) {}
        }
        return String.format("DM%03d", max + 1);
    }
}
