package controller;

import dao.DinhMucNguyenLieuDAO;
import dao.TonKhoDAO;
import dao.impl.DinhMucNguyenLieuDAOImpl;
import dao.impl.TonKhoDAOImpl;
import dto.CartItem;
import entity.DinhMucNguyenLieu;
import entity.TonKho;
import utils.SessionManager;

import java.util.List;

public class InventoryController {
    
    private final DinhMucNguyenLieuDAO dinhMucDAO;
    private final TonKhoDAO tonKhoDAO;
    private final KhoController khoController;

    public InventoryController() {
        this.dinhMucDAO = new DinhMucNguyenLieuDAOImpl();
        this.tonKhoDAO = new TonKhoDAOImpl();
        this.khoController = new KhoController();
    }

    /**
     * Kiểm tra xem món này có đủ Tồn Kho (các nguyên liệu > mức tối thiểu) để pha chế không?
     * Mặc định kiểm tra 1 ly size M. 
     * @param maMon String
     * @return boolean
     */
    public boolean checkTonKhoMoiMon(String maMon) {
        List<DinhMucNguyenLieu> dinhmucs = dinhMucDAO.findByMon(maMon);
        if (dinhmucs.isEmpty()) return true; // KHÔNG set định mức -> Luôn bán được (vd: Nước suối chai)
        
        List<TonKho> allKho = tonKhoDAO.findAll();
        for (DinhMucNguyenLieu dm : dinhmucs) {
            // Check in list tonkho if this NL has enough SoLuongTon
            for (TonKho tk : allKho) {
                if (tk.getMaNL().equals(dm.getMaNL())) {
                    if (tk.getSoLuongTon() < dm.getSoLuong()) {
                        return false; // Ko đủ nguyên liệu cho 1 đơn vị
                    }
                }
            }
        }
        return true;
    }

    /**
     * Nhận vào list CartItem (Giỏ hàng đã thanh toán), tiến hành trừ kho.
     * Cần nhân Hệ số Size (S: 0.8, M: 1.0, L: 1.2) - (giả định)
     * Đồng thời tạo phiếu xuất tự động.
     */
    public void deductStock(List<CartItem> cartItems) {
        for (CartItem item : cartItems) {
            String maMon = item.getMon().getMaMon();
            int qty = item.getSoLuong();
            
            // [FIX] Lấy tỉ lệ từ thuộc tính tileSize của Size — không hardcode theo tên
            double heSoSize = (item.getSize() != null) ? item.getSize().getTileSize() : 1.0;

            List<DinhMucNguyenLieu> dinhmucs = dinhMucDAO.findByMon(maMon);
            for (DinhMucNguyenLieu dm : dinhmucs) {
                double totalDeduct = dm.getSoLuong() * qty * heSoSize;
                
                // Trừ kho (delta = âm)
                // Tìm dòng Tồn kho của NL này (có thể có nhiều kho, lấy kho đầu tiên)
                List<TonKho> allKho = tonKhoDAO.findAll();
                for (TonKho tk : allKho) {
                    if (tk.getMaNL().equals(dm.getMaNL())) {
                        tonKhoDAO.updateSoLuong(tk.getMaTonKho(), -totalDeduct);
                        break; // Chỉ trừ ở 1 kho
                    }
                }
            }
        }

        // Tạo phiếu xuất tự động khi thanh toán
        String maNV = SessionManager.isLoggedIn() ? SessionManager.getCurrentUser().getMaNV() : "NV001";
        try {
            khoController.processXuatKhoFromPayment(cartItems, maNV);
        } catch (Exception e) {
            System.err.println("InventoryController: Loi tao phieu xuat tu dong: " + e.getMessage());
        }
    }

    /** Ngưỡng cảnh báo: còn bán được ≤ bao nhiêu phần thì hiện ⚠ */
    public static final int NGUONG_CANH_BAO = 10;

    /**
     * Tính số phần còn bán được cho 1 món (dựa trên TonKho / DinhMuc).
     * Trả về Integer.MAX_VALUE nếu món không có định mức (vd: nước suối chai).
     * Trả về 0 nếu bất kỳ nguyên liệu nào đã hết.
     */
    public int getSoLuongConBanDuoc(String maMon) {
        List<DinhMucNguyenLieu> dinhmucs = dinhMucDAO.findByMon(maMon);
        if (dinhmucs.isEmpty()) return Integer.MAX_VALUE; // Không có định mức → luôn bán được

        List<TonKho> allKho = tonKhoDAO.findAll();
        int minServings = Integer.MAX_VALUE;

        for (DinhMucNguyenLieu dm : dinhmucs) {
            if (dm.getSoLuong() <= 0) continue;

            // Tìm tồn kho tương ứng
            double tongTon = 0;
            for (TonKho tk : allKho) {
                if (tk.getMaNL().equals(dm.getMaNL())) {
                    tongTon += tk.getSoLuongTon();
                }
            }

            int servings = (int) Math.floor(tongTon / dm.getSoLuong());
            if (servings < minServings) {
                minServings = servings;
            }
        }
        return minServings;
    }

    /**
     * Lấy danh sách tên nguyên liệu đang sắp hết cho 1 món.
     * Chỉ trả về các NL mà số phần còn lại ≤ NGUONG_CANH_BAO.
     */
    public List<String> getCanhBaoNguyenLieu(String maMon) {
        List<String> warnings = new java.util.ArrayList<>();
        List<DinhMucNguyenLieu> dinhmucs = dinhMucDAO.findByMon(maMon);
        List<TonKho> allKho = tonKhoDAO.findAll();

        for (DinhMucNguyenLieu dm : dinhmucs) {
            if (dm.getSoLuong() <= 0) continue;

            double tongTon = 0;
            for (TonKho tk : allKho) {
                if (tk.getMaNL().equals(dm.getMaNL())) {
                    tongTon += tk.getSoLuongTon();
                }
            }

            int servings = (int) Math.floor(tongTon / dm.getSoLuong());
            if (servings <= NGUONG_CANH_BAO) {
                // Lấy tên nguyên liệu
                entity.NguyenLieu nl = new dao.impl.NguyenLieuDAOImpl().findById(dm.getMaNL());
                String tenNL = (nl != null) ? nl.getTenNL() : dm.getMaNL();
                warnings.add(tenNL + " (còn " + servings + " phần)");
            }
        }
        return warnings;
    }
}

