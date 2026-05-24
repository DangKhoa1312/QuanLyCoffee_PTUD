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
        return getSoLuongConBanDuoc(maMon) > 0;
    }

    /**
     * Nhận vào list CartItem (Giỏ hàng đã thanh toán), tiến hành trừ kho.
     * Cần nhân Hệ số Size (S: 0.8, M: 1.0, L: 1.2) - (giả định)
     * Đồng thời tạo phiếu xuất tự động.
     */
    public boolean deductStock(List<CartItem> cartItems) {
        for (CartItem item : cartItems) {
            String maMon = item.getMon().getMaMon();
            int qty = item.getSoLuong();
            
            // [FIX] Lấy tỉ lệ từ thuộc tính tileSize của Size — không hardcode theo tên
            double heSoSize = (item.getSize() != null) ? item.getSize().getTileSize() : 1.0;

        // Đã xóa vòng lặp trừ TonKhoDAO trực tiếp. 
        // Trách nhiệm trừ tồn kho sẽ được giao cho việc tạo Phiếu Xuất.
        }

        // Tạo phiếu xuất tự động khi thanh toán
        String maNV = SessionManager.isLoggedIn() ? SessionManager.getCurrentUser().getMaNV() : "NV001";
        try {
            return khoController.processXuatKhoFromPayment(cartItems, maNV);
        } catch (Exception e) {
            System.err.println("InventoryController: Loi tao phieu xuat tu dong: " + e.getMessage());
            return false;
        }
    }

    /**
     * Kiểm tra xem CẢ GIỎ HÀNG có đủ nguyên liệu để thanh toán không?
     * @param cartItems Danh sách CartItem
     * @return String: null nếu đủ, chuỗi chứa danh sách tên nguyên liệu thiếu nếu không đủ
     */
    public String checkDuNguyenLieuChoCart(List<CartItem> cartItems) {
        // Map để cộng dồn số lượng nguyên liệu cần thiết cho toàn bộ giỏ hàng
        java.util.Map<String, Double> requiredNlMap = new java.util.HashMap<>();

        for (CartItem item : cartItems) {
            String maMon = item.getMon().getMaMon();
            int qty = item.getSoLuong();
            double heSoSize = (item.getSize() != null) ? item.getSize().getTileSize() : 1.0;

            List<DinhMucNguyenLieu> dinhmucs = dinhMucDAO.findByMon(maMon);
            for (DinhMucNguyenLieu dm : dinhmucs) {
                double totalDeduct = dm.getSoLuong() * qty * heSoSize;
                requiredNlMap.put(dm.getMaNL(), requiredNlMap.getOrDefault(dm.getMaNL(), 0.0) + totalDeduct);
            }
        }

        if (requiredNlMap.isEmpty()) return null; // Không cần nguyên liệu -> Luôn đủ

        // Lấy danh sách tồn kho hiện tại (đã là Base Units)
        List<TonKho> allKho = tonKhoDAO.findAll();
        java.util.Map<String, Double> actualTonKhoMap = new java.util.HashMap<>();
        for (TonKho tk : allKho) {
            actualTonKhoMap.put(tk.getMaNL(), actualTonKhoMap.getOrDefault(tk.getMaNL(), 0.0) + tk.getSoLuongTon());
        }

        // So sánh
        List<String> missingNl = new java.util.ArrayList<>();
        dao.NguyenLieuDAO nlDAO = new dao.impl.NguyenLieuDAOImpl();
        
        for (java.util.Map.Entry<String, Double> entry : requiredNlMap.entrySet()) {
            String maNL = entry.getKey();
            double reqQty = entry.getValue();
            double actualQty = actualTonKhoMap.getOrDefault(maNL, 0.0);

            if (actualQty < reqQty) {
                entity.NguyenLieu nl = nlDAO.findById(maNL);
                String tenNL = (nl != null) ? nl.getTenNL() : maNL;
                missingNl.add(tenNL + " (Cần: " + String.format("%.1f", reqQty) + ", Còn: " + String.format("%.1f", actualQty) + ")");
            }
        }

        if (missingNl.isEmpty()) {
            return null; // Đủ
        } else {
            return String.join("\n", missingNl);
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

            // Tìm tồn kho tương ứng (đã là Base Units)
            double tongTonBase = 0;
            for (TonKho tk : allKho) {
                if (tk.getMaNL().equals(dm.getMaNL())) {
                    tongTonBase += tk.getSoLuongTon();
                }
            }

            int servings = (int) Math.floor(tongTonBase / dm.getSoLuong());
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

            double tongTonBase = 0;
            for (TonKho tk : allKho) {
                if (tk.getMaNL().equals(dm.getMaNL())) {
                    tongTonBase += tk.getSoLuongTon();
                }
            }

            int servings = (int) Math.floor(tongTonBase / dm.getSoLuong());
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

