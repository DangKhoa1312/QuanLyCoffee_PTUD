package controller;

import dao.*;
import dao.impl.*;
import entity.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller quản lý kho: Nguyên liệu, Nhà cung cấp, Phiếu nhập, Tồn kho.
 */
public class KhoController {

    private final NguyenLieuDAO nguyenLieuDAO = new NguyenLieuDAOImpl();
    private final NhaCungCapDAO nhaCungCapDAO = new NhaCungCapDAOImpl();
    private final PhieuNhapDAO phieuNhapDAO   = new PhieuNhapDAOImpl();
    private final ChiTietPhieuNhapDAO chiTietPNDAO = new ChiTietPhieuNhapDAOImpl();
    private final TonKhoDAO tonKhoDAO         = new TonKhoDAOImpl();
    private final KhoDAO khoDAO               = new KhoDAOImpl();

    // ==================== NGUYÊN LIỆU ====================

    public List<NguyenLieu> getAllNguyenLieu() {
        return nguyenLieuDAO.findAll();
    }

    public NguyenLieu getNguyenLieuById(String maNL) {
        return nguyenLieuDAO.findById(maNL);
    }

    public boolean addNguyenLieu(NguyenLieu nl) {
        return nguyenLieuDAO.insert(nl);
    }
    
    public boolean updateNguyenLieu(NguyenLieu nl) {
        return nguyenLieuDAO.update(nl);
    }

    public boolean deleteNguyenLieu(String maNL) {
        return nguyenLieuDAO.delete(maNL);
    }

    public String generateNextMaNL() {
        List<NguyenLieu> list = nguyenLieuDAO.findAll();
        int max = 0;
        for (NguyenLieu nl : list) {
            try {
                int num = Integer.parseInt(nl.getMaNL().replace("NL", ""));
                if (num > max) max = num;
            } catch (NumberFormatException ignored) {}
        }
        return String.format("NL%03d", max + 1);
    }

    // ==================== NHÀ CUNG CẤP ====================

    public List<NhaCungCap> getAllNhaCungCap() {
        return nhaCungCapDAO.findAll();
    }
    
    public NhaCungCap getNhaCungCapById(String maNCC) {
        return nhaCungCapDAO.findById(maNCC);
    }

    public boolean addNhaCungCap(NhaCungCap ncc) {
        return nhaCungCapDAO.insert(ncc);
    }
    
    public boolean updateNhaCungCap(NhaCungCap ncc) {
        return nhaCungCapDAO.update(ncc);
    }

    public boolean deleteNhaCungCap(String maNCC) {
        return nhaCungCapDAO.delete(maNCC);
    }

    public String generateNextMaNCC() {
        List<NhaCungCap> list = nhaCungCapDAO.findAll();
        int max = 0;
        for (NhaCungCap ncc : list) {
            try {
                int num = Integer.parseInt(ncc.getMaNCC().replace("NCC", ""));
                if (num > max) max = num;
            } catch (NumberFormatException ignored) {}
        }
        return String.format("NCC%03d", max + 1);
    }

    // ==================== KHO ====================

    public List<Kho> getAllKho() {
        return khoDAO.findAll();
    }

    public Kho getKhoById(String maKho) {
        return khoDAO.findById(maKho);
    }

    // ==================== TỒN KHO ====================

    public List<TonKho> getAllTonKho() {
        return tonKhoDAO.findAll();
    }

    public List<TonKho> getTonKhoSapHet(String maKho) {
        return tonKhoDAO.findSapHet(maKho);
    }

    public boolean updateMucToiThieu(String maTonKho, double mucMin) {
        TonKho tk = tonKhoDAO.findById(maTonKho);
        if (tk == null) return false;
        tk.setMucToiThieu(mucMin);
        tk.setNgayCapNhat(LocalDateTime.now());
        return tonKhoDAO.update(tk);
    }

    // ==================== PHIẾU NHẬP ====================

    public List<PhieuNhap> getAllPhieuNhap() {
        return phieuNhapDAO.findAll();
    }

    public List<ChiTietPhieuNhap> getChiTietByPhieuNhap(String maPN) {
        return chiTietPNDAO.findByPhieuNhap(maPN);
    }

    public String generateNextMaPN() {
        List<PhieuNhap> list = phieuNhapDAO.findAll();
        int max = 0;
        for (PhieuNhap pn : list) {
            try {
                int num = Integer.parseInt(pn.getMaPN().replace("PN", ""));
                if (num > max) max = num;
            } catch (NumberFormatException ignored) {}
        }
        return String.format("PN%03d", max + 1);
    }

    public String generateNextMaCTPN() {
        List<ChiTietPhieuNhap> list = chiTietPNDAO.findAll();
        int max = 0;
        for (ChiTietPhieuNhap ct : list) {
            try {
                int num = Integer.parseInt(ct.getMaCTPN().replace("CTPN", ""));
                if (num > max) max = num;
            } catch (NumberFormatException ignored) {}
        }
        return String.format("CTPN%03d", max + 1);
    }

    public String generateNextMaTonKho() {
        List<TonKho> list = tonKhoDAO.findAll();
        int max = 0;
        for (TonKho tk : list) {
            try {
                int num = Integer.parseInt(tk.getMaTonKho().replace("TK", ""));
                if (num > max) max = num;
            } catch (NumberFormatException ignored) {}
        }
        return String.format("TK%03d", max + 1);
    }

    public boolean processNhapKho(PhieuNhap phieuNhap, List<ChiTietPhieuNhap> chiTietList) {
        if (!phieuNhapDAO.insert(phieuNhap)) return false;

        // Lấy max CTPN hiện tại 1 lần, rồi tăng dần cho mỗi item
        int maxCTPN = 0;
        List<ChiTietPhieuNhap> allCTPN = chiTietPNDAO.findAll();
        for (ChiTietPhieuNhap existing : allCTPN) {
            try {
                int num = Integer.parseInt(existing.getMaCTPN().replace("CTPN", ""));
                if (num > maxCTPN) maxCTPN = num;
            } catch (NumberFormatException ignored) {}
        }

        for (int i = 0; i < chiTietList.size(); i++) {
            ChiTietPhieuNhap ct = chiTietList.get(i);
            ct.setMaPN(phieuNhap.getMaPN());
            ct.setMaCTPN(String.format("CTPN%03d", maxCTPN + i + 1));
            ct.tinhThanhTien();
            if (!chiTietPNDAO.insert(ct)) return false;

            boolean found = false;
            List<TonKho> allTK = tonKhoDAO.findAll();
            for (TonKho tk : allTK) {
                if (tk.getMaKho().equals(phieuNhap.getMaKho()) && tk.getMaNL().equals(ct.getMaNL())) {
                    tonKhoDAO.updateSoLuong(tk.getMaTonKho(), ct.getSoLuong());
                    found = true;
                    break;
                }
            }
            if (!found) {
                TonKho newTK = new TonKho();
                newTK.setMaTonKho(generateNextMaTonKho());
                newTK.setSoLuongTon(ct.getSoLuong());
                newTK.setMucToiThieu(0);
                newTK.setNgayCapNhat(LocalDateTime.now());
                newTK.setMaKho(phieuNhap.getMaKho());
                newTK.setMaNL(ct.getMaNL());
                tonKhoDAO.insert(newTK);
            }
        }
        return true;
    }

    /** Xuất kho: trừ số lượng tồn kho */
    public boolean xuatKho(String maTonKho, double soLuong) {
        return tonKhoDAO.updateSoLuong(maTonKho, -soLuong);
    }

    /** Nhập kho trả lại: cộng số lượng tồn kho */
    public boolean nhapKhoTraLai(String maTonKho, double soLuong) {
        return tonKhoDAO.updateSoLuong(maTonKho, soLuong);
    }
}
