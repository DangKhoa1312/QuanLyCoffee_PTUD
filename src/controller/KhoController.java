package controller;

import dao.*;
import dao.impl.*;
import dto.CartItem;
import entity.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller quản lý kho: Nguyên liệu, Nhà cung cấp, Phiếu nhập, Phiếu xuất, Tồn kho.
 */
public class KhoController {

    private final NguyenLieuDAO nguyenLieuDAO = new NguyenLieuDAOImpl();
    private final NhaCungCapDAO nhaCungCapDAO = new NhaCungCapDAOImpl();
    private final PhieuNhapDAO phieuNhapDAO   = new PhieuNhapDAOImpl();
    private final ChiTietPhieuNhapDAO chiTietPNDAO = new ChiTietPhieuNhapDAOImpl();
    private final PhieuXuatDAO phieuXuatDAO   = new PhieuXuatDAOImpl();
    private final ChiTietPhieuXuatDAO chiTietPXDAO = new ChiTietPhieuXuatDAOImpl();
    private final TonKhoDAO tonKhoDAO         = new TonKhoDAOImpl();
    private final KhoDAO khoDAO               = new KhoDAOImpl();
    private final DinhMucNguyenLieuDAO dinhMucDAO = new DinhMucNguyenLieuDAOImpl();

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

    public synchronized String generateNextMaNL() {
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

    public synchronized String generateNextMaNCC() {
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

    /**
     * CHẠY 1 LẦN DUY NHẤT: Chuyển đổi toàn bộ dữ liệu Tồn kho hiện tại từ Đơn vị đóng gói sang Đơn vị cơ bản (Gram/ml).
     * @return true nếu thành công
     */
    public boolean runMigrationToBaseUnits() {
        java.sql.Connection conn = connectDB.DatabaseConnection.getInstance().getConnection();
        if (conn == null) return false;
        try {
            conn.setAutoCommit(false);
            List<TonKho> allTK = tonKhoDAO.findAll();
            for (TonKho tk : allTK) {
                NguyenLieu nl = getNguyenLieuById(tk.getMaNL());
                if (nl != null && nl.getKhoiLuongDongGoi() > 0) {
                    // Update SoLuongTon
                    double newTon = tk.getSoLuongTon() * nl.getKhoiLuongDongGoi();
                    double newMuc = tk.getMucToiThieu() * nl.getKhoiLuongDongGoi();
                    
                    // Ghi đè vào DB
                    String sql = "UPDATE TonKho SET SoLuongTon = ?, MucToiThieu = ? WHERE MaTonKho = ?";
                    try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setDouble(1, newTon);
                        stmt.setDouble(2, newMuc);
                        stmt.setString(3, tk.getMaTonKho());
                        stmt.executeUpdate();
                    }
                }
            }
            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (Exception ex) {}
            return false;
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (Exception ex) {}
        }
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

    public synchronized String generateNextMaPN() {
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

    public synchronized String generateNextMaCTPN() {
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

    public synchronized String generateNextMaTonKho() {
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
        java.sql.Connection conn = connectDB.DatabaseConnection.getInstance().getConnection();
        if (conn == null) return false;
        try {
            conn.setAutoCommit(false); // Bắt đầu transaction

            if (!phieuNhapDAO.insert(conn, phieuNhap)) {
                conn.rollback();
                return false;
            }

            int maxCTPN = 0;
            List<ChiTietPhieuNhap> allCTPN = chiTietPNDAO.findAll();
            for (ChiTietPhieuNhap existing : allCTPN) {
                try {
                    int num = Integer.parseInt(existing.getMaCTPN().replace("CTPN", ""));
                    if (num > maxCTPN) maxCTPN = num;
                } catch (NumberFormatException ignored) {}
            }

            List<TonKho> allTK = tonKhoDAO.findAll();
            for (int i = 0; i < chiTietList.size(); i++) {
                ChiTietPhieuNhap ct = chiTietList.get(i);
                ct.setMaPN(phieuNhap.getMaPN());
                ct.setMaCTPN(String.format("CTPN%03d", maxCTPN + i + 1));
                ct.tinhThanhTien();
                
                if (!chiTietPNDAO.insert(conn, ct)) {
                    conn.rollback();
                    return false;
                }

                // Lấy khối lượng đóng gói để quy đổi ra Base Units
                NguyenLieu nl = getNguyenLieuById(ct.getMaNL());
                double kldg = (nl != null && nl.getKhoiLuongDongGoi() > 0) ? nl.getKhoiLuongDongGoi() : 1.0;
                double slBaseUnits = ct.getSoLuong() * kldg;

                boolean found = false;
                for (TonKho tk : allTK) {
                    if (tk.getMaKho().equals(phieuNhap.getMaKho()) && tk.getMaNL().equals(ct.getMaNL())) {
                        tonKhoDAO.updateSoLuong(conn, tk.getMaTonKho(), slBaseUnits);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    TonKho newTK = new TonKho();
                    newTK.setMaTonKho(generateNextMaTonKho());
                    newTK.setSoLuongTon(slBaseUnits);
                    newTK.setMucToiThieu(0);
                    newTK.setNgayCapNhat(LocalDateTime.now());
                    newTK.setMaKho(phieuNhap.getMaKho());
                    newTK.setMaNL(ct.getMaNL());
                    tonKhoDAO.insert(conn, newTK);
                    allTK.add(newTK);
                }
            }

            conn.commit(); // Thành công tất cả
            return true;
        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (Exception ex) {}
            return false;
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (Exception ex) {}
            // connectDB.DatabaseConnection.getInstance().closeConnection(); -> không đóng vì là singleton
        }
    }

    /** Xuất kho: trừ số lượng tồn kho */
    public boolean xuatKho(String maTonKho, double soLuong) {
        return tonKhoDAO.updateSoLuong(maTonKho, -soLuong);
    }

    /** Nhập kho trả lại: cộng số lượng tồn kho */
    public boolean nhapKhoTraLai(String maTonKho, double soLuong) {
        return tonKhoDAO.updateSoLuong(maTonKho, soLuong);
    }

    // ==================== PHIẾU XUẤT ====================

    public List<PhieuXuat> getAllPhieuXuat() {
        return phieuXuatDAO.findAll();
    }

    public List<ChiTietPhieuXuat> getChiTietByPhieuXuat(String maPX) {
        return chiTietPXDAO.findByPhieuXuat(maPX);
    }

    public synchronized String generateNextMaPX() {
        List<PhieuXuat> list = phieuXuatDAO.findAll();
        int max = 0;
        for (PhieuXuat px : list) {
            try {
                int num = Integer.parseInt(px.getMaPX().replace("PX", ""));
                if (num > max) max = num;
            } catch (NumberFormatException ignored) {}
        }
        return String.format("PX%03d", max + 1);
    }

    public synchronized String generateNextMaCTPX() {
        List<ChiTietPhieuXuat> list = chiTietPXDAO.findAll();
        int max = 0;
        for (ChiTietPhieuXuat ct : list) {
            try {
                int num = Integer.parseInt(ct.getMaCTPX().replace("CTPX", ""));
                if (num > max) max = num;
            } catch (NumberFormatException ignored) {}
        }
        return String.format("CTPX%03d", max + 1);
    }

    /**
     * Xử lý xuất kho thủ công: tạo phiếu xuất + trừ tồn kho.
     */
    public boolean processXuatKho(PhieuXuat phieuXuat, List<ChiTietPhieuXuat> chiTietList) {
        java.sql.Connection conn = connectDB.DatabaseConnection.getInstance().getConnection();
        if (conn == null) return false;
        try {
            conn.setAutoCommit(false); // Bắt đầu transaction

            if (!phieuXuatDAO.insert(conn, phieuXuat)) {
                conn.rollback();
                return false;
            }

            int maxCTPX = 0;
            List<ChiTietPhieuXuat> allCTPX = chiTietPXDAO.findAll();
            for (ChiTietPhieuXuat existing : allCTPX) {
                try {
                    int num = Integer.parseInt(existing.getMaCTPX().replace("CTPX", ""));
                    if (num > maxCTPX) maxCTPX = num;
                } catch (NumberFormatException ignored) {}
            }

            List<TonKho> allTK = tonKhoDAO.findAll();
            for (int i = 0; i < chiTietList.size(); i++) {
                ChiTietPhieuXuat ct = chiTietList.get(i);
                ct.setMaPX(phieuXuat.getMaPX());
                ct.setMaCTPX(String.format("CTPX%03d", maxCTPX + i + 1));
                
                if (!chiTietPXDAO.insert(conn, ct)) {
                    conn.rollback();
                    return false;
                }

                // Trừ tồn kho
                for (TonKho tk : allTK) {
                    if (tk.getMaKho().equals(phieuXuat.getMaKho()) && tk.getMaNL().equals(ct.getMaNL())) {
                        
                        // THÊM: Kiểm tra đủ không
                        if (tk.getSoLuongTon() < ct.getSoLuong()) {
                            conn.rollback(); // Không đủ → rollback
                            return false; 
                        }

                        tonKhoDAO.updateSoLuong(conn, tk.getMaTonKho(), -ct.getSoLuong());
                        break;
                    }
                }
            }
            conn.commit();
            return true;
        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (Exception ex) {}
            return false;
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (Exception ex) {}
            // connectDB.DatabaseConnection.getInstance().closeConnection(); -> không đóng vì là singleton
        }
    }

    /**
     * Tự động tạo phiếu xuất khi thanh toán.
     * Dựa trên công thức (DinhMucNguyenLieu) của các món trong giỏ hàng.
     */
    public boolean processXuatKhoFromPayment(List<CartItem> cart, String maNV) {
        // Lấy kho đầu tiên
        List<Kho> listKho = khoDAO.findAll();
        if (listKho.isEmpty()) return false;
        Kho kho = listKho.get(0);

        java.sql.Connection conn = connectDB.DatabaseConnection.getInstance().getConnection();
        if (conn == null) return false;
        
        try {
            conn.setAutoCommit(false); // Bắt đầu transaction

            PhieuXuat px = new PhieuXuat();
            px.setMaPX(generateNextMaPX());
            px.setNgayXuat(LocalDateTime.now());
            px.setLyDoXuat("Thanh to\u00e1n \u0111\u01a1n h\u00e0ng");
            px.setMaNV(maNV);
            px.setMaKho(kho.getMaKho());

            if (!phieuXuatDAO.insert(conn, px)) {
                conn.rollback();
                return false;
            }

            int maxCTPX = 0;
            List<ChiTietPhieuXuat> allCTPX = chiTietPXDAO.findAll();
            for (ChiTietPhieuXuat existing : allCTPX) {
                try {
                    int num = Integer.parseInt(existing.getMaCTPX().replace("CTPX", ""));
                    if (num > maxCTPX) maxCTPX = num;
                } catch (NumberFormatException ignored) {}
            }

            int counter = 0;
            List<TonKho> allTK = tonKhoDAO.findAll();
            for (CartItem item : cart) {
                String maMon = item.getMon().getMaMon();
                int qty = item.getSoLuong();

                // [FIX] Lấy tỉ lệ từ thuoc tính tileSize của Size — không hardcode theo tên
                double heSoSize = (item.getSize() != null) ? item.getSize().getTileSize() : 1.0;

                List<DinhMucNguyenLieu> dinhmucs = dinhMucDAO.findByMon(maMon);
                for (DinhMucNguyenLieu dm : dinhmucs) {
                    double totalDeductBase = dm.getSoLuong() * qty * heSoSize;

                    counter++;
                    ChiTietPhieuXuat ct = new ChiTietPhieuXuat();
                    ct.setMaCTPX(String.format("CTPX%03d", maxCTPX + counter));
                    ct.setSoLuong(totalDeductBase);
                    ct.setMaPX(px.getMaPX());
                    ct.setMaNL(dm.getMaNL());
                    
                    if (!chiTietPXDAO.insert(conn, ct)) {
                        conn.rollback();
                        return false;
                    }
                    
                    // Trừ tồn kho tại đây (Trừ theo Đơn vị cơ bản)
                    for (TonKho tk : allTK) {
                        if (tk.getMaKho().equals(px.getMaKho()) && tk.getMaNL().equals(dm.getMaNL())) {
                            tonKhoDAO.updateSoLuong(conn, tk.getMaTonKho(), -totalDeductBase);
                            break;
                        }
                    }
                }
            }
            conn.commit();
            return true;
        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (Exception ex) {}
            return false;
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (Exception ex) {}
            // connectDB.DatabaseConnection.getInstance().closeConnection(); -> không đóng vì là singleton
        }
    }
}
