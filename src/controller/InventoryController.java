package controller;

import dao.DinhMucNguyenLieuDAO;
import dao.TonKhoDAO;
import dao.impl.DinhMucNguyenLieuDAOImpl;
import dao.impl.TonKhoDAOImpl;
import dto.CartItem;
import entity.DinhMucNguyenLieu;
import entity.TonKho;

import java.util.List;

public class InventoryController {
    
    private final DinhMucNguyenLieuDAO dinhMucDAO;
    private final TonKhoDAO tonKhoDAO;

    public InventoryController() {
        this.dinhMucDAO = new DinhMucNguyenLieuDAOImpl();
        this.tonKhoDAO = new TonKhoDAOImpl();
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
     */
    public void deductStock(List<CartItem> cartItems) {
        for (CartItem item : cartItems) {
            String maMon = item.getMon().getMaMon();
            int qty = item.getSoLuong();
            
            // Xử lý hệ số hao hụt do Size
            double heSoSize = 1.0;
            if (item.getSize() != null) {
                String tenSize = item.getSize().getTenSize().toUpperCase();
                if (tenSize.contains("S")) heSoSize = 0.8;
                else if (tenSize.contains("L")) heSoSize = 1.25;
            }

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
    }
}
